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
package com.asakusafw.shafu.internal.ui.wizards;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WorkingSetConfigurationBlock;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleTask;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.asakusafw.shafu.core.gradle.GradleBuildTask;
import com.asakusafw.shafu.core.gradle.GradleContext;
import com.asakusafw.shafu.core.gradle.GradleException;
import com.asakusafw.shafu.core.gradle.GradleInspectTask;
import com.asakusafw.shafu.core.util.StatusUtils;
import com.asakusafw.shafu.internal.ui.Activator;
import com.asakusafw.shafu.internal.ui.LogUtil;
import com.asakusafw.shafu.ui.ShafuUi;
import com.asakusafw.shafu.ui.consoles.ShafuConsole;
import com.asakusafw.shafu.ui.util.ProgressUtils;

/**
 * Selects Gradle projects to import into the workspace.
 */
public class SelectGradleProjectsPage extends WizardPage {

    private static final String[] WORKING_SETS = {
        "org.eclipse.ui.resourceWorkingSetPage", //$NON-NLS-1$
        "org.eclipse.jdt.ui.JavaWorkingSetPage", //$NON-NLS-1$
    };

    private static final String KEY_DIALOG_BUILD = "build"; //$NON-NLS-1$

    private IStructuredSelection selection;

    private CheckboxTableViewer viewer;

    private WorkingSetConfigurationBlock workingSets;

    private Button buildCheck;

    private Button openConsoleButton;

    private ShafuConsole console;

    private File baseTargetDirectory;

    private File rootProjectDirectory;

    private List<File> selectedProjectDirectories;

    /**
     * Creates a new instance.
     */
    public SelectGradleProjectsPage() {
        super("SelectGradleProjectsPage"); //$NON-NLS-1$
        setTitle(Messages.SelectGradleProjectsPage_title);
        setDescription(Messages.SelectGradleProjectsPage_description);
    }

    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        Composite pane = new Composite(parent, SWT.NONE);
        pane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setControl(pane);

        pane.setLayout(new GridLayout(1, false));

        Label descriptionLabel = new Label(pane, SWT.NONE);
        descriptionLabel.setText(Messages.SelectGradleProjectsPage_targetLabel);
        descriptionLabel.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.BEGINNING, SWT.BEGINNING)
                .create());

        this.viewer = CheckboxTableViewer.newCheckList(pane, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        viewer.getTable().setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, true)
                .create());
        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.setLabelProvider(new ProjectLabelProvider());
        viewer.getTable().setEnabled(false);
        viewer.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                Table tree = (Table) e.getSource();
                Point point = new Point(e.x, e.y);
                TableItem item = tree.getItem(point);
                if (item == null) {
                    return;
                }
                ProjectEntry entry = (ProjectEntry) item.getData();
                if (item.getBounds().contains(point) && entry.isEnabled()) {
                    boolean checked = !item.getChecked();
                    item.setChecked(checked);
                    refreshSelection();
                }
            }
        });

        createGradleSettings(pane);
        createWorkingSetSettings(pane);
        createConsoleButton(pane);

        Dialog.applyDialogFont(pane);
        setPageComplete(false);
    }

    private void createGradleSettings(Composite pane) {
        Group group = new Group(pane, SWT.NONE);
        group.setText(Messages.SelectGradleProjectsPage_gradleGroupLabel);
        group.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false)
                .indent(convertWidthInCharsToPixels(1), convertHeightInCharsToPixels(1))
                .create());
        group.setLayout(new GridLayout(2, false));

        this.buildCheck = new Button(group, SWT.CHECK);
        buildCheck.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.BEGINNING, SWT.CENTER)
                .create());
        buildCheck.setText(Messages.SelectGradleProjectsPage_buildCheckLabel);
        buildCheck.setToolTipText(MessageFormat.format(
                Messages.SelectGradleProjectsPage_buildCheckTooltip,
                GradleBuildTask.TASK_BUILD_PROJECT));

        final IDialogSettings settings = Activator.getDialogSettings(getClass().getSimpleName());
        String defaultValue = settings.get(KEY_DIALOG_BUILD);
        if (defaultValue == null || defaultValue.equals(String.valueOf(true))) {
            buildCheck.setSelection(true);
        } else {
            buildCheck.setSelection(false);
        }
        buildCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean doBuild = ((Button) e.getSource()).getSelection();
                settings.put(KEY_DIALOG_BUILD, String.valueOf(doBuild));
                refreshSelection();
            }
        });
    }

    private void createWorkingSetSettings(Composite pane) {
        Group group = new Group(pane, SWT.NONE);
        group.setText(Messages.SelectGradleProjectsPage_workingSetGroupLabel);
        group.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false)
                .indent(convertWidthInCharsToPixels(1), convertHeightInCharsToPixels(1))
                .create());
        group.setLayout(new GridLayout());
        this.workingSets = new WorkingSetConfigurationBlock(
                WORKING_SETS,
                Activator.getDialogSettings(getClass().getSimpleName()));
        if (selection != null) {
            workingSets.setWorkingSets(workingSets.findApplicableWorkingSets(selection));
        }
        workingSets.createContent(group);
    }

    private void createConsoleButton(Composite pane) {
        this.openConsoleButton = new Button(pane, SWT.PUSH);
        openConsoleButton.setText(Messages.SelectGradleProjectsPage_openConsoleLabel);
        openConsoleButton.setToolTipText(Messages.SelectGradleProjectsPage_openConsoleTooltip);
        openConsoleButton.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.END, SWT.END)
                .create());
        openConsoleButton.setEnabled(false);
        openConsoleButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openConsole();
            }
        });
    }

    void refreshSelection() {
        if (viewer.getTable().isEnabled() == false) {
            setPageComplete(false);
            return;
        }
        this.rootProjectDirectory = null;
        this.selectedProjectDirectories = null;
        List<File> results = new ArrayList<File>();
        for (TableItem item : viewer.getTable().getItems()) {
            ProjectEntry entry = (ProjectEntry) item.getData();
            if (entry.isRoot()) {
                this.rootProjectDirectory = entry.getDirectory();
            }
            if (item.getChecked() && entry.isEnabled()) {
                results.add(entry.getDirectory());
            }
        }
        if (results.isEmpty()) {
            setErrorMessage(Messages.SelectGradleProjectsPage_errorNotSelect);
            setPageComplete(false);
        } else {
            this.selectedProjectDirectories = results;
            setErrorMessage(null);
            setPageComplete(true);
        }
    }

    void openConsole() {
        if (console == null) {
            MessageDialog.openError(
                    getShell(),
                    null,
                    Messages.SelectGradleProjectsPage_errorConsoleNotActive);
            return;
        }
        ShafuUi.open(getShell(), console);
    }

    void setBaseTargetDirectory(File baseTargetDirectory) {
        this.baseTargetDirectory = baseTargetDirectory;
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            rebuild();
        }
        super.setVisible(visible);
    }

    private void rebuild() {
        if (baseTargetDirectory == null) {
            return;
        }
        openConsoleButton.setEnabled(false);
        viewer.getTable().setEnabled(false);

        List<ProjectEntry> projects = computeProjects();
        if (projects == null) {
            setPageComplete(false);
            return;
        }

        viewer.setInput(projects);
        reload();
    }

    void reload() {
        TableItem[] items = viewer.getTable().getItems();
        if (items.length == 0) {
            setErrorMessage(Messages.SelectGradleProjectsPage_errorProjectNotAvailable);
            setPageComplete(false);
            return;
        }

        boolean sawEnabled = false;
        for (TableItem item : items) {
            ProjectEntry entry = (ProjectEntry) item.getData();
            if (entry.isEnabled()) {
                sawEnabled = true;
            }
            viewer.setChecked(entry, entry.isEnabled());
        }
        if (sawEnabled == false) {
            setErrorMessage(null);
            setMessage(Messages.SelectGradleProjectsPage_infoProjectAlreadyImport);
            setPageComplete(false);
            return;
        }

        setErrorMessage(null);
        setMessage(null);
        viewer.getTable().setEnabled(true);
        refreshSelection();
    }

    private List<ProjectEntry> computeProjects() {
        prepareConsole();
        GradleContext context = ShafuUi.createContext(baseTargetDirectory);
        console.clearConsole();
        console.attachTo(context);

        EclipseProject model;
        try {
            model = ProgressUtils.call(
                    getContainer(),
                    GradleInspectTask.newInstance(context, EclipseProject.class));
        } catch (GradleException e) {
            setErrorMessage(Messages.SelectGradleProjectsPage_errorFailedToInspectProject);
            return null;
        } catch (CoreException e) {
            if (StatusUtils.hasCancel(e.getStatus())) {
                setErrorMessage(Messages.SelectGradleProjectsPage_errorCanceledToInspectProject);
                return null;
            } else {
                setErrorMessage(Messages.SelectGradleProjectsPage_errorFailedToInspectProject);
                LogUtil.log(e.getStatus());
                return null;
            }
        }

        return toProjectEntries(model);
    }

    private List<ProjectEntry> toProjectEntries(EclipseProject model) {
        EclipseProject current = model;
        while (true) {
            EclipseProject parent = current.getParent();
            if (parent == null) {
                break;
            }
            current = parent;
        }
        List<ProjectEntry> results = new ArrayList<ProjectEntry>();
        LinkedList<EclipseProject> work = new LinkedList<EclipseProject>();
        work.add(current);
        while (work.isEmpty() == false) {
            EclipseProject next = work.removeFirst();
            results.add(new ProjectEntry(next));
            work.addAll(next.getChildren());
        }

        Set<IPath> projectLocation = new HashSet<IPath>();
        Set<String> projectNames = new HashSet<String>();
        for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
            projectLocation.add(project.getLocation());
            projectNames.add(project.getName());
        }

        for (ProjectEntry entry : results) {
            if (isEclipseSupported(entry)) {
                entry.setSupported(true);
            }
            if (projectLocation.contains(entry.getLocation())) {
                entry.setLocationConflict(true);
            }
            if (projectNames.contains(entry.getName())) {
                entry.setNameConflict(true);
            }
        }
        return results;
    }

    private boolean isEclipseSupported(ProjectEntry entry) {
        EclipseProject model = entry.getModel();
        DomainObjectSet<? extends GradleTask> tasks = model.getGradleProject().getTasks();
        for (GradleTask task : tasks) {
            if (task.getName().equals(GradleBuildTask.TASK_CONFIGURE_ECLIPSE)) {
                return true;
            }
        }
        return false;
    }

    ShafuConsole getConsole() {
        prepareConsole();
        return console;
    }

    private void prepareConsole() {
        if (console == null) {
            console = new ShafuConsole();
        }
        if (openConsoleButton != null) {
            openConsoleButton.setEnabled(true);
        }
    }

    void setSelection(IStructuredSelection selection) {
        this.selection = selection;
    }

    List<String> getTaskNames() {
        List<String> results = new ArrayList<String>();
        if (buildCheck.getSelection()) {
            results.add(GradleBuildTask.TASK_BUILD_PROJECT);
        }
        results.add(GradleBuildTask.TASK_DECONFIGURE_ECLIPSE);
        results.add(GradleBuildTask.TASK_CONFIGURE_ECLIPSE);
        return results;
    }

    File getRootProjectDirectory() {
        return rootProjectDirectory;
    }

    List<File> getSelectedProjectDirectories() {
        if (selectedProjectDirectories == null) {
            return Collections.emptyList();
        }
        return selectedProjectDirectories;
    }

    IWorkingSet[] getWorkingSets() {
        return workingSets.getSelectedWorkingSets();
    }

    @Override
    public void dispose() {
        try {
            if (console != null) {
                console.destroy();
                console = null;
            }
        } finally {
            super.dispose();
        }
    }

    private static class ProjectEntry {

        private final EclipseProject model;

        private boolean supported = false;

        private boolean nameConflict = false;

        private boolean locationConflict = false;

        ProjectEntry(EclipseProject model) {
            this.model = model;
        }

        public EclipseProject getModel() {
            return model;
        }

        public boolean isRoot() {
            return model.getParent() == null;
        }

        public File getDirectory() {
            return model.getProjectDirectory();
        }

        public IPath getLocation() {
            return Path.fromOSString(model.getProjectDirectory().getAbsolutePath());
        }

        public String getName() {
            return model.getName();
        }

        public boolean isImported() {
            return this.nameConflict || this.locationConflict;
        }

        public boolean isSupported() {
            return supported;
        }

        public boolean isEnabled() {
            return supported && isImported() == false;
        }

        public void setSupported(boolean supported) {
            this.supported = supported;
        }

        public void setNameConflict(boolean conflict) {
            this.nameConflict = conflict;
        }

        public void setLocationConflict(boolean conflict) {
            this.locationConflict = conflict;
        }

        @Override
        public String toString() {
            return String.format("%s (@%s)", model.getName(), model.getProjectDirectory());
        }
    }

    private class ProjectLabelProvider extends LabelProvider implements IColorProvider {

        public ProjectLabelProvider() {
            return;
        }

        @Override
        public String getText(Object element) {
            ProjectEntry entry = (ProjectEntry) element;
            return entry.getName();
        }

        @Override
        public Image getImage(Object element) {
            ProjectEntry entry = (ProjectEntry) element;
            if (entry.isSupported()) {
                if (entry.isImported()) {
                    return PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT);
                } else {
                    return PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT_CLOSED);
                }
            }
            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
        }

        @Override
        public Color getForeground(Object element) {
            ProjectEntry entry = (ProjectEntry) element;
            if (entry.isEnabled() == false) {
                return getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY);
            }
            return null;
        }

        @Override
        public Color getBackground(Object element) {
            return null;
        }
    }
}
