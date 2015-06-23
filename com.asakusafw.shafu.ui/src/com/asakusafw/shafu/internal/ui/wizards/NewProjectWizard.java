/**
 * Copyright 2013-2015 Asakusa Framework Team.
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

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import com.asakusafw.shafu.core.gradle.GradleBuildTask;
import com.asakusafw.shafu.core.gradle.GradleContext;
import com.asakusafw.shafu.core.net.IContentProcessor;
import com.asakusafw.shafu.core.net.ShafuNetwork;
import com.asakusafw.shafu.core.util.IRunnable;
import com.asakusafw.shafu.core.util.IoUtils;
import com.asakusafw.shafu.core.util.StatusUtils;
import com.asakusafw.shafu.internal.ui.Activator;
import com.asakusafw.shafu.internal.ui.LogUtil;
import com.asakusafw.shafu.ui.ShafuUi;
import com.asakusafw.shafu.ui.consoles.ShafuConsole;
import com.asakusafw.shafu.ui.util.ProgressUtils;

/**
 * Wizard of creating project from application templates.
 */
public class NewProjectWizard extends Wizard implements INewWizard {

    private IWorkbench currentWorkbench;

    private IStructuredSelection currentSelection;

    private SelectProjectTemplatePage templatePage;

    private ProjectInformationPage informationPage;

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.currentWorkbench = workbench;
        this.currentSelection = selection;
        setNeedsProgressMonitor(true);
        setWindowTitle(Messages.NewProjectWizard_title);
    }

    @Override
    public void addPages() {
        this.templatePage = new SelectProjectTemplatePage();
        this.informationPage = new ProjectInformationPage();
        addPage(informationPage);
        addPage(templatePage);
        informationPage.setSelection(currentSelection);
    }

    @Override
    public boolean canFinish() {
        if (getContainer().getCurrentPage().getNextPage() != null) {
            return false;
        }
        return super.canFinish();
    }

    @Override
    public boolean performFinish() {
        final File projectDirectory = computeProjectDirectory();
        if (projectDirectory.exists()) {
            boolean delete = MessageDialog.openConfirm(
                    getShell(),
                    Messages.NewProjectWizard_dialogOverwriteTitle,
                    MessageFormat.format(
                            Messages.NewProjectWizard_dialogOverwriteMessage,
                            projectDirectory));
            if (delete == false) {
                return false;
            }
        }

        final IWorkingSet[] workingSets = informationPage.getSelectedWorkingSets();
        final List<String> taskNames = templatePage.getTaskNames();
        final ShafuConsole console = templatePage.getConsole();
        final AtomicReference<Stage> stageRef = new AtomicReference<Stage>(Stage.INIT);
        final Archive templateArchive = new Archive(templatePage.getTargetFile(), templatePage.getTargetUrl());
        try {
            ProgressUtils.run(getContainer(), new IRunnable() {
                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    SubMonitor sub = SubMonitor.convert(monitor);
                    try {
                        perform(sub, projectDirectory, workingSets, templateArchive, taskNames, console, stageRef);
                    } finally {
                        monitor.done();
                    }
                }
            });
        } catch (RuntimeException e) {
            IStatus status = new Status(
                    IStatus.ERROR,
                    Activator.PLUGIN_ID,
                    Messages.NewProjectWizard_errorUnknown,
                    e);
            LogUtil.log(status);
            ErrorDialog.openError(getShell(), null, null, status);
            templatePage.setPageComplete(false);
            return false;
        } catch (CoreException e) {
            if (StatusUtils.hasCancel(e.getStatus())) {
                return false;
            }
            switch (stageRef.get()) {
            case BUILD:
                templatePage.setErrorMessage(Messages.NewProjectWizard_errorTemplateBuild);
                templatePage.openConsole();
                break;
            default:
                templatePage.setErrorMessage(e.getStatus().getMessage());
                LogUtil.log(e.getStatus());
                break;
            }
            templatePage.setPageComplete(false);
            return false;
        } finally {
            templateArchive.close();
        }
        return true;
    }

    private File computeProjectDirectory() {
        if (informationPage.useDefaults()) {
            return informationPage.getLocationPath().append(informationPage.getProjectName()).toFile();
        } else {
            return informationPage.getLocationPath().toFile();
        }
    }

    void perform(
            SubMonitor monitor,
            File projectDirectory, IWorkingSet[] workingSets,
            Archive archive,
            List<String> taskNames, ShafuConsole console, AtomicReference<Stage> stageRef) throws CoreException {
        monitor.beginTask(Messages.NewProjectWizard_monitorMain, 100);
        extractProjectContents(monitor.newChild(20, SubMonitor.SUPPRESS_NONE), projectDirectory, archive);
        boolean succeed = false;
        try {
            stageRef.set(Stage.BUILD);
            buildProject(monitor.newChild(60, SubMonitor.SUPPRESS_NONE), projectDirectory, taskNames, console);

            stageRef.set(Stage.DEPLOY);
            deployProject(monitor.newChild(20, SubMonitor.SUPPRESS_NONE), projectDirectory, workingSets);

            succeed = true;
        } finally {
            if (succeed == false && projectDirectory.exists()) {
                IoUtils.deleteQuietly(projectDirectory);
            }
        }
    }

    private void extractProjectContents(
            SubMonitor monitor,
            File projectDirectory,
            Archive archive) throws CoreException {
        monitor.beginTask(Messages.NewProjectWizard_monitorExtractContents, 100);
        StatusUtils.checkCanceled(monitor);
        File temporaryFolder = archive.extract(monitor.newChild(80));
        try {
            File temporaryProjectFolder = detectProjectEntry(monitor.newChild(10), temporaryFolder);
            if (projectDirectory.exists()) {
                IoUtils.deleteQuietly(projectDirectory);
            }
            copyContents(monitor.newChild(10), temporaryProjectFolder, projectDirectory);
        } finally {
            IoUtils.deleteQuietly(temporaryFolder);
        }
    }

    private File detectProjectEntry(SubMonitor monitor, File temporaryFolder) throws CoreException {
        monitor.beginTask(Messages.NewProjectWizard_monitorInspectProject, 100);
        List<File> results = new ArrayList<File>();
        detectProjectEntry0(monitor, temporaryFolder, results);
        if (results.isEmpty()) {
            throw new CoreException(new Status(
                    IStatus.ERROR,
                    Activator.PLUGIN_ID,
                    MessageFormat.format(
                            Messages.NewProjectWizard_errorTemplateInvalid,
                            GradleContext.DEFAULT_BUILD_SCRIPT_NAME)));
        } else {
            File result = selectProjectEntry(temporaryFolder, results);
            if (result == null) {
                throw new CoreException(Status.CANCEL_STATUS);
            }
            return result;
        }
    }

    private File selectProjectEntry(File temporaryFolder, List<File> results) {
        assert results.isEmpty() == false;
        if (results.size() == 1) {
            return results.get(0).getParentFile();
        }
        final List<IPath> paths = new ArrayList<IPath>();
        IPath base = Path.fromOSString(temporaryFolder.getAbsolutePath());
        for (File file : results) {
            IPath path = Path.fromOSString(file.getAbsolutePath());
            paths.add(path.removeFirstSegments(base.segmentCount()));
        }
        Collections.sort(paths, new Comparator<IPath>() {
            @Override
            public int compare(IPath o1, IPath o2) {
                return o1.toPortableString().compareToIgnoreCase(o2.toPortableString());
            }
        });
        IPath selection = selectProjectEntryByDialog(paths);
        if (selection == null) {
            return null;
        }
        return base.append(selection).toFile().getParentFile();
    }

    private IPath selectProjectEntryByDialog(final List<IPath> paths) {
        final AtomicReference<IPath> selectionResult = new AtomicReference<IPath>();
        Activator.getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                ListDialog dialog = new ListDialog(getShell()) {
                    @Override
                    protected int getTableStyle() {
                        return super.getTableStyle() | SWT.SINGLE;
                    }
                };
                dialog.setTitle(Messages.NewProjectWizard_selectEntryTitle);
                dialog.setMessage(Messages.NewProjectWizard_selectEntryMessage);
                dialog.setInitialSelections(new Object[] { paths.get(0) });
                dialog.setInput(paths);
                dialog.setContentProvider(ArrayContentProvider.getInstance());
                dialog.setLabelProvider(new LabelProvider());
                if (dialog.open() != Window.OK) {
                    return;
                }
                Object[] selected = dialog.getResult();
                if (selected.length >= 1) {
                    selectionResult.set((IPath) selected[0]);
                }
            }
        });
        return selectionResult.get();
    }

    private void detectProjectEntry0(SubMonitor monitor, File folder, List<File> results) {
        File file = new File(folder, GradleContext.DEFAULT_BUILD_SCRIPT_NAME);
        if (file.isFile()) {
            results.add(file);
        } else {
            for (File child : folder.listFiles()) {
                if (child.isDirectory()) {
                    detectProjectEntry0(monitor, child, results);
                }
            }
        }
        monitor.worked(1);
        monitor.setWorkRemaining(100);
    }

    private void copyContents(SubMonitor monitor, File from, File to) throws CoreException {
        monitor.beginTask(Messages.NewProjectWizard_monitorCopyContents, 100);
        try {
            IoUtils.move(monitor.newChild(100), from, to);
        } catch (IOException e) {
            throw new CoreException(new Status(
                    IStatus.ERROR,
                    Activator.PLUGIN_ID,
                    MessageFormat.format(
                            Messages.NewProjectWizard_errorProjectFailedToCopyContent,
                            to)));
        }
    }

    private void buildProject(
            SubMonitor monitor,
            File projectDirectory,
            List<String> taskNames,
            ShafuConsole console) throws CoreException {
        GradleContext configuration = ShafuUi.createContext(projectDirectory);
        console.clearConsole();
        console.attachTo(configuration);
        new GradleBuildTask(configuration, taskNames).run(monitor);
    }

    private void deployProject(
            SubMonitor monitor,
            File projectDirectory, IWorkingSet[] workingSets) throws CoreException {
        monitor.beginTask(Messages.NewProjectWizard_monitorLoadProject, 100);
        IProjectDescription description = loadDescription(monitor.newChild(20), projectDirectory);
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
        if (project.exists()) {
            throw new CoreException(new Status(
                    IStatus.ERROR,
                    Activator.PLUGIN_ID,
                    MessageFormat.format(
                            Messages.NewProjectWizard_errorProjectConflict,
                            project.getName())));
        }
        project.create(description, new SubProgressMonitor(monitor, 20));
        project.open(new SubProgressMonitor(monitor, 10));
        if (workingSets.length > 0) {
            currentWorkbench.getWorkingSetManager().addToWorkingSets(project, workingSets);
        }
        BasicNewResourceWizard.selectAndReveal(project, currentWorkbench.getActiveWorkbenchWindow());
    }

    private IProjectDescription loadDescription(SubMonitor monitor, File projectDirectory) throws CoreException {
        monitor.beginTask(Messages.NewProjectWizard_monitorLoadDescription, 1);
        try {
            File file = new File(projectDirectory, IProjectDescription.DESCRIPTION_FILE_NAME);
            IPath path = Path.fromOSString(file.getAbsolutePath());
            return ResourcesPlugin.getWorkspace().loadProjectDescription(path);
        } finally {
            monitor.done();
        }
    }

    private enum Stage {

        INIT,

        BUILD,

        DEPLOY,
    }

    private static class Archive implements Closeable {

        private final File file;

        private final URL url;

        private File temporaryFile;

        public Archive(File file, URL url) {
            if (file == null && url == null) {
                throw new IllegalArgumentException();
            }
            this.file = file;
            this.url = url;
        }

        public File extract(SubMonitor monitor) throws CoreException {
            monitor.beginTask(Messages.NewProjectWizard_monitorExtractArchive, 100);
            File archive = getFile(monitor.newChild(50));
            String name = getFileName();
            try {
                if (name.endsWith(".zip") || name.endsWith(".jar")) { //$NON-NLS-1$ //$NON-NLS-2$
                    return IoUtils.extractZip(monitor, archive);
                } else if (name.endsWith(".tar.gz")) { //$NON-NLS-1$
                    return IoUtils.extractTarGz(monitor, archive);
                } else {
                    throw new CoreException(new Status(
                            IStatus.ERROR,
                            Activator.PLUGIN_ID,
                            Messages.NewProjectWizard_errorArchiveNotSupport));
                }
            } catch (IOException e) {
                throw new CoreException(new Status(
                        IStatus.ERROR,
                        Activator.PLUGIN_ID,
                        Messages.NewProjectWizard_errorArchiveExtract));
            }
        }

        private String getFileName() {
            if (file != null) {
                return file.getName();
            } else {
                String path = url.getPath();
                int index = path.lastIndexOf('/');
                return path.substring(index + 1);
            }
        }

        private File getFile(final SubMonitor monitor) throws CoreException {
            if (file != null) {
                return file;
            } else if (temporaryFile != null) {
                return temporaryFile;
            }
            try {
                temporaryFile = File.createTempFile("tmp", ".bin"); //$NON-NLS-1$ //$NON-NLS-2$
                final OutputStream output = new FileOutputStream(temporaryFile);
                try {
                    ShafuNetwork.processContent(url, new IContentProcessor<Void>() {
                        @Override
                        public Void process(InputStream input) throws IOException {
                            byte[] buf = new byte[1024];
                            while (true) {
                                if (monitor.isCanceled()) {
                                    throw new OperationCanceledException();
                                }
                                int read = input.read(buf);
                                if (read < 0) {
                                    break;
                                }
                                monitor.setWorkRemaining(10000);
                                monitor.worked(read);
                                output.write(buf, 0, read);
                            }
                            return null;
                        }
                    });
                } finally {
                    output.close();
                }
                return temporaryFile;
            } catch (OperationCanceledException e) {
                throw new CoreException(Status.CANCEL_STATUS);
            } catch (IOException e) {
                throw new CoreException(new Status(
                        IStatus.ERROR,
                        Activator.PLUGIN_ID,
                        MessageFormat.format(
                                Messages.NewProjectWizard_errorArchiveDownload,
                                url),
                        e));
            }
        }

        @Override
        public void close() {
            if (temporaryFile != null) {
                temporaryFile.delete();
            }
        }
    }
}
