/**
 * Copyright 2013 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.shafu.core.gradle;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.LongRunningOperation;
import org.gradle.tooling.ProgressEvent;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ResultHandler;
import org.osgi.framework.Bundle;

import com.asakusafw.shafu.core.util.IoUtils;
import com.asakusafw.shafu.core.util.StatusUtils;
import com.asakusafw.shafu.internal.core.Activator;
import com.asakusafw.shafu.internal.core.LogUtil;

/**
 * Utilities for {@link GradleContext}.
 */
final class GradleUtil {

    private static final IPath SCRIPT_BASE_PATH = Path.fromPortableString("scripts"); //$NON-NLS-1$

    private static final IPath SCRIPT_PATH = SCRIPT_BASE_PATH.append("init.gradle"); //$NON-NLS-1$

    private static final String SYSTEM_PROPERTY_PREFIX = "-D"; //$NON-NLS-1$

    private static final char SYSTEM_PROPERTY_FIELD_SEPARATOR = '=';

    private static final String KEY_CANCEL_FILE = "com.asakusafw.shafu.core.cancelFile"; //$NON-NLS-1$

    private GradleUtil() {
        return;
    }

    /**
     * Enhances the target context.
     * @param monitor the current monitor
     * @param context the target context
     * @throws CoreException if failed to enhance the target
     */
    public static void enhance(IProgressMonitor monitor, GradleContext context) throws CoreException {
        List<IGradleContextEnhancer> enhancers = Activator.getExtensions().createGradleContextEnhancers();
        monitor.beginTask(Messages.GradleUtil_monitorEnhance, enhancers.size());
        try {
            for (IGradleContextEnhancer enhancer : enhancers) {
                checkCancel(monitor);
                enhancer.enhance(new SubProgressMonitor(monitor, 1), context);
            }
        } finally {
            monitor.done();
        }
    }

    /**
     * Disposes the target context.
     * @param monitor the current monitor
     * @param context the target context
     */
    public static void dispose(IProgressMonitor monitor, GradleContext context) {
        List<IWorkspaceRunnable> actions = context.disposeActions;
        if (actions.isEmpty()) {
            return;
        }
        monitor.beginTask(Messages.GradleUtil_monitorDispose, actions.size());
        try {
            for (IWorkspaceRunnable action : actions) {
                try {
                    action.run(new SubProgressMonitor(monitor, 1));
                } catch (OperationCanceledException e) {
                    monitor.setCanceled(true);
                } catch (CoreException e) {
                    if (StatusUtils.hasCancel(e.getStatus())) {
                        monitor.setCanceled(true);
                    } else {
                        LogUtil.log(e.getStatus());
                    }
                }
            }
        } finally {
            monitor.done();
        }
    }

    /**
     * Detects whether cancel is requested or not and raises {@link CoreException} with {@link Status#CANCEL_STATUS}.
     * @param monitor the current monitor
     * @throws CoreException if cancel was requested
     */
    public static void checkCancel(IProgressMonitor monitor) throws CoreException {
        StatusUtils.checkCanceled(monitor);
    }

    /**
     * Creates a new Gradle connector for the context.
     * @param context the target context
     * @return a connector
     */
    public static GradleConnector createConnector(GradleContext context) {
        GradleConnector connector = GradleConnector.newConnector();
        connector.forProjectDirectory(context.getProjectDirectory().getAbsoluteFile());
        if (context.getGradleDistribution() != null) {
            connector.useDistribution(context.getGradleDistribution());
        } else if (context.getGradleVersion() != null) {
            connector.useGradleVersion(context.getGradleVersion());
        }
        if (context.getGradleUserHomeDir() != null) {
            connector.useGradleUserHomeDir(context.getGradleUserHomeDir().getAbsoluteFile());
        }
        return connector;
    }

    /**
     * Configures the operation and creates an {@link OperationHandler} for it.
     * @param operation the target operation
     * @param context the target context
     * @param <T> the operation result type
     * @return the created handler
     */
    public static <T> OperationHandler<T> configureOperation(
            LongRunningOperation operation,
            GradleContext context) {
        if (context.getJavaHomeDir() != null) {
            operation.setJavaHome(context.getJavaHomeDir().getAbsoluteFile());
        }
        if (context.standardInputOrNull != null) {
            operation.setStandardInput(context.standardInputOrNull);
        }
        if (context.standardOutputOrNull != null) {
            operation.setStandardOutput(context.standardOutputOrNull);
        }
        if (context.standardErrorOutputOrNull != null) {
            operation.setStandardError(context.standardErrorOutputOrNull);
        }

        Properties properties = AccessController.doPrivileged(new PrivilegedAction<Properties>() {
            @Override
            public Properties run() {
                return System.getProperties();
            }
        });

        File cancelFile = prepareCancelFile(context);
        boolean succeed = false;
        try {
            operation.setJvmArguments(toArray(context.getJvmArguments()));
            operation.withArguments(toArray(context.getGradleArguments()));
            final Properties newProperties = new Properties();
            newProperties.putAll(properties);
            newProperties.put("user.dir", context.getProjectDirectory().getAbsolutePath()); //$NON-NLS-1$
            newProperties.putAll(extractSystemProperties(context));
            OperationHandler<T> results = new OperationHandler<T>(operation, properties, cancelFile);

            // install modified properties
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    System.setProperties(newProperties);
                    return null;
                }
            });
            succeed = true;
            return results;
        } finally {
            if (succeed == false) {
                IoUtils.deleteQuietly(cancelFile);
            }
        }
    }

    private static Properties extractSystemProperties(GradleContext context) {
        Properties properties = new Properties();
        for (String argument : context.getJvmArguments()) {
            if (argument.startsWith(SYSTEM_PROPERTY_PREFIX) == false) {
                continue;
            }
            int index = argument.indexOf(SYSTEM_PROPERTY_FIELD_SEPARATOR, SYSTEM_PROPERTY_PREFIX.length());
            String key;
            String value;
            if (index >= 0) {
                key = argument.substring(SYSTEM_PROPERTY_PREFIX.length(), index);
                value = argument.substring(index + 1);
            } else {
                key = argument.substring(SYSTEM_PROPERTY_PREFIX.length());
                value = ""; //$NON-NLS-1$
            }
            properties.setProperty(key, value);
        }
        return properties;
    }

    private static String[] toArray(List<String> list) {
        return list.toArray(new String[list.size()]);
    }

    private static File prepareCancelFile(GradleContext context) {
        List<String> newArguments = new ArrayList<String>();
        try {
            IPath scriptPath = resolveBuiltinPath(Activator.getDefault().getBundle(), SCRIPT_PATH);
            newArguments.add("--init-script"); //$NON-NLS-1$
            newArguments.add(scriptPath.toFile().getAbsolutePath());

            // pass -D... into Gradle args (not JVM args)
            File cancelFile = File.createTempFile("gradle", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
            newArguments.add(String.format("-D%s=%s", KEY_CANCEL_FILE, cancelFile.getAbsolutePath())); //$NON-NLS-1$

            newArguments.addAll(context.getGradleArguments());
            context.setGradleArguments(newArguments);
            return cancelFile;
        } catch (IOException e) {
            LogUtil.log(IStatus.WARNING, Messages.GradleUtil_errorFailedToCreateCancelMarker);
        }
        return null;
    }

    private static IPath resolveBuiltinPath(Bundle bundle, IPath relativePath) throws IOException {
        assert relativePath != null;
        URL entry = bundle.getEntry(relativePath.toPortableString());
        if (entry == null) {
            throw new FileNotFoundException(relativePath.toPortableString());
        }
        URL fileUrl = FileLocator.toFileURL(entry);
        if (fileUrl.getProtocol().equals("file") == false) { //$NON-NLS-1$
            throw new FileNotFoundException(fileUrl.toExternalForm());
        }
        // Test file exists
        InputStream input = fileUrl.openStream();
        input.close();
        return toPath(fileUrl);
    }

    private static IPath toPath(URL url) throws IOException {
        assert url != null;
        assert url.getProtocol().equals("file"); //$NON-NLS-1$
        try {
            URI fileUri = url.toURI();
            File file = new File(fileUri);
            file = file.getAbsoluteFile().getCanonicalFile();
            IPath path = Path.fromOSString(file.getAbsolutePath());
            if (path.toFile().exists()) {
                return path;
            }
            // continue...
        }
        catch (URISyntaxException ignore) {
            // continue...
        }

        String filePath = url.getFile();
        IPath path = Path.fromPortableString(filePath);
        if (path.toFile().exists()) {
            return path;
        }

        if (filePath.isEmpty() == false && filePath.startsWith("/")) { //$NON-NLS-1$
            path = Path.fromPortableString(filePath.substring(1));
            if (path.toFile().exists()) {
                return path;
            }
        }
        throw new FileNotFoundException(url.toExternalForm());
    }

    /**
     * Handles Gradle operations.
     * @param <T> the operation result type
     */
    public static class OperationHandler<T> implements ResultHandler<T>, Closeable {

        private final CountDownLatch latch = new CountDownLatch(1);

        final AtomicReference<ProgressEvent> eventRef = new AtomicReference<ProgressEvent>();

        private final AtomicReference<T> resultRef = new AtomicReference<T>();

        private final AtomicReference<GradleConnectionException> exceptionRef =
                new AtomicReference<GradleConnectionException>();

        final Properties systemProperties;

        private final File cancelFile;

        /**
         * Creates a new instance.
         * @param operation the target operation
         * @param systemProperties the original system properties
         * @param cancelFile cancel marker file
         */
        public OperationHandler(
                LongRunningOperation operation,
                Properties systemProperties,
                File cancelFile) {
            operation.addProgressListener(new ProgressListener() {
                @Override
                public void statusChanged(ProgressEvent event) {
                    eventRef.set(event);
                }
            });
            this.systemProperties = systemProperties;
            this.cancelFile = cancelFile;
        }

        public boolean await() throws InterruptedException {
            return latch.await(100, TimeUnit.MILLISECONDS);
        }

        public ProgressEvent takeProgressEvent() {
            return eventRef.getAndSet(null);
        }

        public T getResult() {
            return resultRef.get();
        }

        public boolean hasException() {
            return exceptionRef.get() != null;
        }

        public GradleConnectionException getException() {
            return exceptionRef.get();
        }

        @Override
        public void onComplete(T result) {
            resultRef.set(result);
            latch.countDown();
        }

        @Override
        public void onFailure(GradleConnectionException exception) {
            exceptionRef.set(exception);
            latch.countDown();
        }

        /**
         * Disposes this handler.
         */
        @Override
        public void close() {
            try {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    @Override
                    public Void run() {
                        System.setProperties(systemProperties);
                        return null;
                    }
                });
            } finally {
                if (cancelFile != null && cancelFile.exists()) {
                    try {
                        IoUtils.delete(cancelFile);
                    } catch (IOException e) {
                        LogUtil.log(IStatus.WARNING, MessageFormat.format(
                                Messages.GradleUtil_errorFailedToDeleteCancelMarker,
                                cancelFile));
                    }
                }
            }
        }
    }
}
