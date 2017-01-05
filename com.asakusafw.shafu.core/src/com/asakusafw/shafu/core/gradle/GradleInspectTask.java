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

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProgressEvent;
import org.gradle.tooling.ProjectConnection;

import com.asakusafw.shafu.core.gradle.GradleUtil.OperationHandler;
import com.asakusafw.shafu.core.util.ICallable;
import com.asakusafw.shafu.internal.core.Activator;

/**
 * Launches Gradle and inspect the target project.
 * @param <T> the model type
 * @see GradleException
 */
public class GradleInspectTask<T> implements ICallable<T> {

    final GradleContext configuration;

    private final Class<T> modelClass;

    /**
     * Creates a new instance.
     * @param configuration the task configuration
     * @param modelClass the model class
     */
    public GradleInspectTask(GradleContext configuration, Class<T> modelClass) {
        this.configuration = configuration;
        this.modelClass = modelClass;
    }

    /**
     * Creates a new instance.
     * @param configuration the task configuration
     * @param modelClass the model class
     * @param <T> the Gradle model type
     * @return the created instance
     */
    public static <T> GradleInspectTask<T> newInstance(GradleContext configuration, Class<T> modelClass) {
        return new GradleInspectTask<T>(configuration, modelClass);
    }

    /**
     * Performs this task.
     * @throws GradleException if and only if the build was failed
     */
    @Override
    public T call(IProgressMonitor monitor) throws GradleException, CoreException {
        SubMonitor sub = SubMonitor.convert(monitor, Messages.GradleInspectTask_monitorRun, 100);
        try {
            try {
                GradleUtil.enhance(sub.newChild(10, SubMonitor.SUPPRESS_NONE), configuration);
                ProjectConnection connection = createProjectConnection(sub.newChild(10, SubMonitor.SUPPRESS_NONE));
                try {
                    return inspectProject(sub.newChild(60, SubMonitor.SUPPRESS_NONE), connection);
                } finally {
                    disconnectProject(sub.newChild(10, SubMonitor.SUPPRESS_NONE), connection);
                }
            } finally {
                GradleUtil.dispose(sub.newChild(10, SubMonitor.SUPPRESS_NONE), configuration);
            }
        } finally {
            monitor.done();
        }
    }

    private ProjectConnection createProjectConnection(SubMonitor monitor) {
        monitor.beginTask(Messages.GradleInspectTask_monitorConnect, 100);
        return GradleUtil.createConnector(configuration).connect();
    }

    private T inspectProject(SubMonitor monitor, ProjectConnection connection) throws CoreException {
        monitor.beginTask(Messages.GradleInspectTask_monitorInspect, 100);
        try {
            GradleUtil.checkCancel(monitor);
            ModelBuilder<T> builder = connection.model(modelClass);
            OperationHandler<T> handler = GradleUtil.configureOperation(builder, configuration);
            try {
                builder.get(handler);
                while (handler.await() == false) {
                    GradleUtil.checkCancel(monitor, handler);
                    ProgressEvent event = handler.takeProgressEvent();
                    if (event != null) {
                        monitor.setTaskName(String.format("[Gradle] %s", event.getDescription())); //$NON-NLS-1$
                    }
                    monitor.worked(1);
                    monitor.setWorkRemaining(100);
                }
                if (handler.hasException()) {
                    GradleUtil.reportException(configuration, handler.getException());
                    throw new GradleException(new Status(
                            IStatus.WARNING,
                            Activator.PLUGIN_ID,
                            MessageFormat.format(
                                    Messages.GradleInspectTask_errorFailedToInspectProject,
                                    configuration.getProjectDirectory().getName()),
                            handler.getException()), handler.getException());
                }
                return handler.getResult();
            } finally {
                handler.close();
            }
        } catch (InterruptedException e) {
            throw new CoreException(Status.CANCEL_STATUS);
        } finally {
            monitor.done();
        }
    }

    private void disconnectProject(SubMonitor monitor, ProjectConnection connection) {
        monitor.beginTask(Messages.GradleInspectTask_monitorDisconnect, 100);
        connection.close();
    }
}
