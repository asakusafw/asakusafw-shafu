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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

/**
 * Inputs a new project information.
 */
public class ProjectInformationPage extends WizardNewProjectCreationPage {

    private static final String[] WORKING_SETS = {
        "org.eclipse.ui.resourceWorkingSetPage", //$NON-NLS-1$
        "org.eclipse.jdt.ui.JavaWorkingSetPage", //$NON-NLS-1$
    };

    private IStructuredSelection selection;

    /**
     * Creates a new instance.
     */
    public ProjectInformationPage() {
        super("ProjectInformationPage"); //$NON-NLS-1$
        setTitle(Messages.ProjectInformationPage_title);
        setDescription(Messages.ProjectInformationPage_description);
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        Composite pane = (Composite) getControl();

        createWorkingSetGroup(pane, selection, WORKING_SETS);

        Dialog.applyDialogFont(pane);
    }

    void setSelection(IStructuredSelection selection) {
        this.selection = selection;
    }

    @Override
    public void setPageComplete(boolean complete) {
        if (complete && useDefaults() == false) {
            String name = getProjectName();
            String lastSegment = getLocationPath().lastSegment();
            if (name.equals(lastSegment) == false) {
                setErrorMessage(Messages.ProjectInformationPage_errorProjectNameInconsistent);
                super.setPageComplete(false);
                return;
            }
        }
        super.setPageComplete(complete);
    }
}
