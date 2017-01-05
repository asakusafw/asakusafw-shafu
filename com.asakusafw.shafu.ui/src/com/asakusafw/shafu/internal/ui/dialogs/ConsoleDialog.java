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
package com.asakusafw.shafu.internal.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.console.TextConsoleViewer;

/**
 * Shows contents of {@link TextConsole}.
 */
public class ConsoleDialog extends Dialog {

    private final TextConsole console;

    /**
     * Creates a new instance.
     * @param parentShell the parent shell
     * @param console the target console
     */
    public ConsoleDialog(Shell parentShell, TextConsole console) {
        super(parentShell);
        this.console = console;
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(Messages.ConsoleDialog_title);
        TextConsoleViewer viewer = new TextConsoleViewer(parent, console);
        viewer.getControl().setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, true)
                .hint(convertWidthInCharsToPixels(80), convertHeightInCharsToPixels(20))
                .create());
        viewer.setEditable(false);
        applyDialogFont(viewer.getControl());
        return viewer.getControl();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }
}
