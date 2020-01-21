/**
 * Copyright 2013-2020 Asakusa Framework Team.
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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.model.build.BuildEnvironment;

import com.asakusafw.shafu.core.gradle.GradleUtil.OperationHandler;
import com.asakusafw.shafu.core.util.IRunnable;
import com.asakusafw.shafu.internal.core.Activator;

/**
 * Launches Gradle and build the target project.
 * @see GradleException
 * @version 0.2.2
 */
public class GradleBuildTask implements IRunnable {

    /**
     * The task name of build project.
     */
    public static final String TASK_BUILD_PROJECT = "testClasses"; //$NON-NLS-1$

    /**
     * The task name of de-configure project for Eclipse.
     * @since 0.2.2
     */
    public static final String TASK_DECONFIGURE_ECLIPSE = "cleanEclipse"; //$NON-NLS-1$

    /**
     * The task name of configure project for Eclipse.
     */
    public static final String TASK_CONFIGURE_ECLIPSE = "eclipse"; //$NON-NLS-1$

    private final GradleContext configuration;

    private final List<String> tasks;

    /**
     * Creates a new instance.
     * @param configuration the task configuration
     * @param tasks the task names
     */
    public GradleBuildTask(GradleContext configuration, List<String> tasks) {
        this.configuration = configuration;
        this.tasks = new ArrayList<>(tasks);
    }

    /**
     * Performs this task.
     * @throws GradleException if and only if the build was failed
     */
    @Override
    public void run(IProgressMonitor monitor) throws GradleException, CoreException {
        SubMonitor sub = SubMonitor.convert(monitor, Messages.GradleBuildTask_monitorRun, 100);
        try {
            try {
                GradleUtil.enhance(sub.newChild(10, SubMonitor.SUPPRESS_NONE), configuration);
                ProjectConnection connection = createProjectConnection(sub.newChild(10, SubMonitor.SUPPRESS_NONE));
                try {
                    buildProject(sub.newChild(60, SubMonitor.SUPPRESS_NONE), connection);
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
        monitor.beginTask(Messages.GradleBuildTask_monitorConnect, 100);
        return GradleUtil.createConnector(configuration).connect();
    }

    private void buildProject(SubMonitor monitor, ProjectConnection connection) throws CoreException {
        monitor.beginTask(Messages.GradleBuildTask_monitorBuild, 100);
        try {
            GradleUtil.checkCancel(monitor);
            BuildEnvironment environment = GradleUtil.getEnvironment(connection);
            BuildLauncher builder = connection.newBuild();
            builder.forTasks(tasks.toArray(new String[tasks.size()]));
            OperationHandler<Void> handler = GradleUtil.configureOperation(environment, builder, configuration);
            try {
                builder.run(handler);
                while (handler.await() == false) {
                    GradleUtil.checkCancel(monitor, handler);
                    ProgressEvent event = handler.takeProgressEvent();
                    if (event != null) {
                        monitor.setTaskName(String.format("[Gradle] %s", event.getDescriptor())); //$NON-NLS-1$
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
                                    Messages.GradleBuildTask_errorFailedToBuildProject,
                                    configuration.projectDirectory.getName()),
                            handler.getException()), handler.getException());
                }
            } finally {
                handler.close();
            }
        } catch (InterruptedException e) {
            throw new CoreException(Status.CANCEL_STATUS);
        }
    }

    private void disconnectProject(SubMonitor monitor, ProjectConnection connection) {
        monitor.beginTask(Messages.GradleBuildTask_monitorDisconnect, 100);
        connection.close();
    }
}
