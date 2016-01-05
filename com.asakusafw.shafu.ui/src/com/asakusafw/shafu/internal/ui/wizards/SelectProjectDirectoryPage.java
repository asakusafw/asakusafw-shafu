/**
 * Copyright 2013-2016 Asakusa Framework Team.
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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.asakusafw.shafu.core.gradle.GradleContext;
import com.asakusafw.shafu.internal.ui.Activator;

/**
 * Import target project selection page.
 */
public class SelectProjectDirectoryPage extends WizardPage {

    private static final String KEY_DIALOG_PATH = "path"; //$NON-NLS-1$

    Text fileField;

    private volatile File targetDirectory;

    /**
     * Creates a new instance.
     */
    public SelectProjectDirectoryPage() {
        super("SelectProjectDirectoryPage"); //$NON-NLS-1$
        setTitle(Messages.SelectProjectDirectoryPage_title);
        setDescription(Messages.SelectProjectDirectoryPage_description);
    }

    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        Composite pane = new Composite(parent, SWT.NONE);
        pane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setControl(pane);

        pane.setLayout(new GridLayout(1, false));

        Label descriptionLabel = new Label(pane, SWT.NONE);
        descriptionLabel.setText(Messages.SelectProjectDirectoryPage_fieldLabel);
        descriptionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

        Composite field = new Composite(pane, SWT.NONE);
        field.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        field.setLayout(new GridLayout(2, false));

        this.fileField = new Text(field, SWT.BORDER);
        fileField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        fileField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                refreshText(((Text) e.getSource()).getText());
            }
        });

        Button fileButton = new Button(field, SWT.PUSH);
        fileButton.setText(Messages.SelectProjectDirectoryPage_fieldButton);
        fileButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        fileButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(getShell());
                dialog.setText(Messages.SelectProjectDirectoryPage_directoryDialogTitle);
                String current = fileField.getText();
                if (current.isEmpty() == false) {
                    dialog.setFilterPath(current);
                }
                String result = dialog.open();
                if (result != null) {
                    fileField.setText(result);
                }
            }
        });

        Dialog.applyDialogFont(pane);

        IDialogSettings settings = Activator.getDialogSettings(getClass().getSimpleName());
        String defaultPath = settings.get(KEY_DIALOG_PATH);
        if (defaultPath != null && defaultPath.isEmpty() == false) {
            fileField.setText(defaultPath);
        } else {
            setPageComplete(false);
        }
    }

    void refreshText(String text) {
        if (text.isEmpty()) {
            this.targetDirectory = null;
            setErrorMessage(Messages.SelectProjectDirectoryPage_errorDirectoryEmpty);
            setPageComplete(false);
        } else {
            File directory = new File(text);
            if (directory.isDirectory() == false) {
                setErrorMessage(Messages.SelectProjectDirectoryPage_errorDirectoryMissing);
                setPageComplete(false);
            } else {
                setErrorMessage(null);
                File script = new File(directory, GradleContext.DEFAULT_BUILD_SCRIPT_NAME);
                if (script.exists() == false) {
                    setMessage(MessageFormat.format(
                            Messages.SelectProjectDirectoryPage_errorDirectoryNotContain,
                            GradleContext.DEFAULT_BUILD_SCRIPT_NAME), WARNING);
                } else {
                    setMessage(null);
                }
                this.targetDirectory = directory;
                setPageComplete(true);
                saveDialogSettings(directory);
            }
        }
    }

    private void saveDialogSettings(File file) {
        IDialogSettings settings = Activator.getDialogSettings(getClass().getSimpleName());
        settings.put(KEY_DIALOG_PATH, file.getAbsolutePath());
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            rebuild();
        }
        super.setVisible(visible);
    }

    private void rebuild() {
        if (targetDirectory != null) {
            refreshText(fileField.getText());
        }
    }

    File getTargetDirectory() {
        return targetDirectory;
    }
}
