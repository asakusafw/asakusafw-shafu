/**
 * Copyright 2013-2017 Asakusa Framework Team.
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import org.gradle.tooling.BuildCancelledException;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.LongRunningOperation;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.build.GradleEnvironment;
import org.gradle.util.DistributionLocator;
import org.gradle.util.GradleVersion;
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

    static final long DEFAULT_SOFT_CANCELLATION_TIMEOUT_MILLIS = 3000L;

    private static final GradleVersion MIN_ENVIRONMENT_VARIABLES_VERSION = GradleVersion.version("3.5"); //$NON-NLS-1$

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
     * Detects whether cancel is requested or not and raises {@link CoreException} with {@link Status#CANCEL_STATUS}.
     * @param monitor the current monitor
     * @param handler current running operation
     * @throws CoreException if cancel was requested
     */
    public static void checkCancel(IProgressMonitor monitor, OperationHandler<?> handler) throws CoreException {
        if (monitor.isCanceled()) {
            try {
                if (handler.cancel()) {
                    return;
                }
            } catch (InterruptedException e) {
                monitor.setCanceled(true);
            }
            throw new CoreException(Status.CANCEL_STATUS);
        }
    }

    /**
     * Creates a new Gradle connector for the context.
     * @param context the target context
     * @return a connector
     */
    public static GradleConnector createConnector(GradleContext context) {
        GradleConnector connector = GradleConnector.newConnector();
        connector.forProjectDirectory(context.getProjectDirectory().getAbsoluteFile());
        String versionLabel;
        if (context.getGradleDistribution() != null) {
            versionLabel = context.getGradleDistribution().toString();
            connector.useDistribution(context.getGradleDistribution());
        } else {
            String version = context.getGradleVersion();
            version = version == null ? GradleVersion.current().getVersion() : version;
            versionLabel = version;
            URI distribution = toDistributionUri(version, context.isUseHttps());
            if (distribution != null) {
                connector.useDistribution(distribution);
            } else {
                connector.useGradleVersion(version);
            }
        }
        context.information(MessageFormat.format(
                Messages.GradleUtil_infoPrepareDaemon,
                versionLabel));
        if (context.getGradleUserHomeDir() != null) {
            connector.useGradleUserHomeDir(context.getGradleUserHomeDir().getAbsoluteFile());
        }
        return connector;
    }

    private static URI toDistributionUri(String gradleVersionString, boolean useHttps) {
        GradleVersion version;
        try {
            version = GradleVersion.version(gradleVersionString);
        } catch (RuntimeException e) {
            LogUtil.log(IStatus.WARNING, MessageFormat.format(
                    Messages.GradleUtil_warnInvalidGradleVersion,
                    gradleVersionString), e);
            return null;
        }
        URI uri = new DistributionLocator().getDistributionFor(version);
        String scheme = useHttps ? "https" : "http"; //$NON-NLS-1$ //$NON-NLS-2$
        if (scheme.equals(uri.getScheme())) {
            return uri;
        }
        try {
            return new URI(
                    scheme,
                    uri.getUserInfo(), uri.getHost(), uri.getPort(),
                    uri.getPath(), uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
            LogUtil.log(IStatus.WARNING, MessageFormat.format(
                    Messages.GradleUtil_warnInvalidGradleDistributionUri,
                    uri), e);
            return null;
        }
    }

    /**
     * Returns the current build environment.
     * @param connection the connection
     * @return the current environment
     * @since 0.5.2
     */
    public static BuildEnvironment getEnvironment(ProjectConnection connection) {
        return connection.getModel(BuildEnvironment.class);
    }

    /**
     * Configures the operation and creates an {@link OperationHandler} for it.
     * @param operation the target operation
     * @param context the target context
     * @param <T> the operation result type
     * @return the created handler
     * @deprecated Use {@link #configureOperation(BuildEnvironment, LongRunningOperation, GradleContext)} instead
     */
    @Deprecated
    public static <T> OperationHandler<T> configureOperation(
            LongRunningOperation operation,
            GradleContext context) {
        return configureOperation(null, operation, context);
    }

    /**
     * Configures the operation and creates an {@link OperationHandler} for it.
     * @param operation the target operation
     * @param context the target context
     * @param <T> the operation result type
     * @param environment the current environment
     * @return the created handler
     * @since 0.5.2
     */
    public static <T> OperationHandler<T> configureOperation(
            BuildEnvironment environment,
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
        if (context.environmentVariables.isEmpty() == false) {
            if (environment != null && require(environment.getGradle(), MIN_ENVIRONMENT_VARIABLES_VERSION)) {
                injectEnvironmentVariables(operation, context);
            } else {
                context.information(MessageFormat.format(
                        Messages.GradleUtil_infoCustomEnvironmentVariablesDisabled,
                        environment == null ? "N/A" : environment.getGradle().getGradleVersion(), //$NON-NLS-1$
                        MIN_ENVIRONMENT_VARIABLES_VERSION.getVersion()));
            }
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
            final Properties newProperties = copyProperties(properties);
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

    private static boolean require(GradleEnvironment environment, GradleVersion version) {
        try {
            GradleVersion current = GradleVersion.version(environment.getGradleVersion());
            return current.isValid() && current.compareTo(version) >= 0;
        } catch (RuntimeException e) {
            LogUtil.log(IStatus.WARNING, MessageFormat.format(
                    Messages.GradleUtil_warnInvalidGradleVersion,
                    environment.getGradleVersion()), e);
            return false;
        }
    }

    private static void injectEnvironmentVariables(LongRunningOperation operation, GradleContext context) {
        if (context.environmentVariables.isEmpty() == false) {
            Map<String, String> env = AccessController.doPrivileged(new PrivilegedAction<Map<String, String>>() {
                @Override
                public Map<String, String> run() {
                    Map<String, String> results = new LinkedHashMap<String, String>();
                    results.putAll(System.getenv());
                    return results;
                }
            });
            for (Map.Entry<String, String> entry : context.environmentVariables.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value == null) {
                    env.remove(key);
                } else {
                    env.put(key, value);
                }
            }
            operation.setEnvironmentVariables(env);
        }
    }

    /**
     * Reports the occurred exception.
     * @param context the current configuration
     * @param exception the occurred exception
     * @since 0.3.3
     */
    public static void reportException(GradleContext context, GradleConnectionException exception) {
        if (exception == null) {
            return;
        }
        context.information("--"); //$NON-NLS-1$
        context.information(Messages.GradleUtil_infoReportException);
        Throwable reason = findInformativeException(exception);
        context.information(String.valueOf(reason));
    }

    private static Throwable findInformativeException(GradleConnectionException exception) {
        for (Throwable current = exception; current != null; current = current.getCause()) {
            if (current instanceof BuildCancelledException) {
                return current;
            }
            String name = current.getClass().getName();
            if (name.equals("org.gradle.api.BuildCancelledException")) { //$NON-NLS-1$
                return current;
            }
        }
        Throwable reason = exception.getCause();
        if (reason != null) {
            return reason;
        }
        return exception;
    }

    private static Properties copyProperties(Properties properties) {
        Properties newProperties = new Properties();
        for (Map.Entry<?, ?> entry : properties.entrySet()) {
            if (checkPropertyEntry(entry)) {
                newProperties.put(entry.getKey(), entry.getValue());
            }
        }
        return newProperties;
    }

    private static boolean checkPropertyEntry(Map.Entry<?, ?> entry) {
        Object key = entry.getKey();
        Object value = entry.getValue();
        if (key instanceof String && (value == null || value instanceof String)) {
            return true;
        }
        LogUtil.debug("Invalid System property: {0}={1} ({2})", //$NON-NLS-1$
                key,
                value,
                value == null ? "null" : value.getClass().getName()); //$NON-NLS-1$
        return false;
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

        private final CancellationTokenSource cancellator;

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
            this.cancellator = GradleConnector.newCancellationTokenSource();
            operation.addProgressListener(new ProgressListener() {
                @Override
                public void statusChanged(ProgressEvent event) {
                    eventRef.set(event);
                }
            });
            operation.withCancellationToken(cancellator.token());
            this.systemProperties = systemProperties;
            this.cancelFile = cancelFile;
        }

        public boolean await() throws InterruptedException {
            return latch.await(100, TimeUnit.MILLISECONDS);
        }

        public boolean cancel() throws InterruptedException {
            cancellator.cancel();
            return latch.await(DEFAULT_SOFT_CANCELLATION_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
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
