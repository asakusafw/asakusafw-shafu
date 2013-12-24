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
package com.asakusafw.shafu.internal.ui.wizards;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.asakusafw.shafu.core.gradle.GradleBuildTask;
import com.asakusafw.shafu.core.gradle.GradleContext;
import com.asakusafw.shafu.core.util.IRunnable;
import com.asakusafw.shafu.core.util.RunnableBuilder;
import com.asakusafw.shafu.core.util.StatusUtils;
import com.asakusafw.shafu.internal.ui.Activator;
import com.asakusafw.shafu.internal.ui.LogUtil;
import com.asakusafw.shafu.ui.ShafuUi;
import com.asakusafw.shafu.ui.consoles.ShafuConsole;
import com.asakusafw.shafu.ui.util.ProgressUtils;

/**
 * Import Gradle projects.
 */
public class ImportProjectsWizard extends Wizard implements IImportWizard {

    private SelectProjectDirectoryPage selectProjectDirectory;

    private SelectGradleProjectsPage selectGradleProjects;

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle(Messages.ImportProjectsWizard_title);
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        this.selectProjectDirectory = new SelectProjectDirectoryPage();
        this.selectGradleProjects = new SelectGradleProjectsPage();
        addPage(selectProjectDirectory);
        addPage(selectGradleProjects);
    }

    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        IWizardPage nextPage = super.getNextPage(page);
        prepareNextPage(nextPage);
        return nextPage;
    }

    private void prepareNextPage(IWizardPage nextPage) {
        if (nextPage == selectProjectDirectory) {
            // do nothing
        } else if (nextPage == selectGradleProjects) {
            File targetDirectory = selectProjectDirectory.getTargetDirectory();
            if (targetDirectory != null) {
                selectGradleProjects.setBaseTargetDirectory(targetDirectory);
            }
        }
    }

    @Override
    public boolean canFinish() {
        if (getContainer().getCurrentPage().getNextPage() != null) {
            return false;
        }
        return super.canFinish();
    }

    @Override
    public boolean performCancel() {
        return true;
    }

    @Override
    public boolean performFinish() {
        final File rootProjectDirectory = selectGradleProjects.getRootProjectDirectory();
        final List<File> projectDirectories = selectGradleProjects.getSelectedProjectDirectories();
        if (rootProjectDirectory == null || projectDirectories.isEmpty()) {
            return false;
        }
        final List<String> taskNames = selectGradleProjects.getTaskNames();
        final ShafuConsole console = selectGradleProjects.getConsole();
        console.clearConsole();
        try {
            ProgressUtils.run(getContainer(), new IRunnable() {
                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    monitor.beginTask(Messages.ImportProjectsWizard_monitorPeformFinish, 100);
                    try {
                        GradleContext context = ShafuUi.createContext(rootProjectDirectory);
                        console.attachTo(context);
                        new GradleBuildTask(context, taskNames)
                            .run(new SubProgressMonitor(monitor, 60));

                        ResourcesPlugin.getWorkspace().run(
                                RunnableBuilder.toWorkspaceRunnable(new ImportProject(projectDirectories)),
                                new SubProgressMonitor(monitor, 40));
                    } finally {
                        monitor.done();
                    }
                }
            });
        } catch (CoreException e) {
            if (StatusUtils.hasCancel(e.getStatus()) == false) {
                LogUtil.log(e.getStatus());
                ShafuUi.open(getShell(), console);
            }
            selectGradleProjects.reload();
            return false;
        }
        return true;
    }

    private static class ImportProject implements IRunnable {

        private final List<File> projectDirectories;

        public ImportProject(List<File> projectDirectories) {
            this.projectDirectories = projectDirectories;
        }

        @Override
        public void run(IProgressMonitor monitor) throws CoreException {
            SubMonitor sub = SubMonitor.convert(monitor);
            sub.beginTask(Messages.ImportProjectsWizard_monitorImportProjects, projectDirectories.size());
            try {
                List<IStatus> statuses = new ArrayList<IStatus>();
                for (File directory : projectDirectories) {
                    try {
                        perform(sub.newChild(1), directory);
                    } catch (CoreException e) {
                        if (StatusUtils.hasCancel(e.getStatus())) {
                            for (IStatus status : statuses) {
                                LogUtil.log(status);
                            }
                            throw e;
                        } else {
                            statuses.add(e.getStatus());
                        }
                    }
                }
                if (statuses.size() == 1) {
                    throw new CoreException(statuses.get(0));
                } else if (statuses.size() > 1) {
                    throw new CoreException(new MultiStatus(
                            Activator.PLUGIN_ID,
                            0,
                            statuses.toArray(new IStatus[statuses.size()]),
                            Messages.ImportProjectsWizard_statusMultiple,
                            null));
                }
            } finally {
                monitor.done();
            }
        }

        private boolean perform(SubMonitor monitor, File directory) throws CoreException {
            monitor.beginTask(Messages.ImportProjectsWizard_monitorLoadProject, 30);
            IProjectDescription description = loadDescription(monitor.newChild(10), directory);
            if (description == null) {
                return false;
            }
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IProject project = root.getProject(description.getName());
            if (project.exists()) {
                throw new CoreException(new Status(
                        IStatus.ERROR,
                        Activator.PLUGIN_ID,
                        MessageFormat.format(
                                Messages.ImportProjectsWizard_errorProjectConflict,
                                project.getName())));
            }
            project.create(description, new SubProgressMonitor(monitor, 10));
            project.open(new SubProgressMonitor(monitor, 10));
            return true;
        }

        private IProjectDescription loadDescription(SubMonitor monitor, File directory) throws CoreException {
            monitor.beginTask(Messages.ImportProjectsWizard_monitorLoadDescription, 1);
            File file = new File(directory, IProjectDescription.DESCRIPTION_FILE_NAME);
            if (file.exists() == false) {
                return null;
            }
            IPath path = Path.fromOSString(file.getAbsolutePath());
            return ResourcesPlugin.getWorkspace().loadProjectDescription(path);
        }
    }
}
