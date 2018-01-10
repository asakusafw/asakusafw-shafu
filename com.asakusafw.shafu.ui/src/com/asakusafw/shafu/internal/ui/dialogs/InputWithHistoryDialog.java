/**
 * Copyright 2013-2018 Asakusa Framework Team.
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

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * String input dialog with its input history.
 * @since 0.2.5
 */
public class InputWithHistoryDialog extends Dialog {

    private final String dialogTitle;

    private final String fieldTitle;

    private final String defaultValue;

    private final List<String> history;

    private Combo valueField;

    private String result;

    /**
     * Creates a new instance.
     * @param shell the parent shell
     * @param dialogTitle the dialog title
     * @param fieldTitle the field title
     * @param defaultValue the default value (nullable)
     * @param history the history values (nullable)
     */
    public InputWithHistoryDialog(
            Shell shell,
            String dialogTitle,
            String fieldTitle,
            String defaultValue,
            List<String> history) {
        super(shell);
        this.dialogTitle = dialogTitle;
        this.fieldTitle = fieldTitle;
        this.defaultValue = defaultValue;
        this.history = history == null ? Collections.<String>emptyList() : history;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        if (defaultValue == null) {
            valueField.setFocus();
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite pane = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) pane.getLayout();
        layout.verticalSpacing = 0;

        Label label = new Label(pane, SWT.NONE);
        if (fieldTitle != null) {
            label.setText(fieldTitle);
        }
        label.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false)
                .hint(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH, SWT.DEFAULT)
                .create());

        this.valueField = new Combo(pane, SWT.DROP_DOWN);
        valueField.setItems(history.toArray(new String[history.size()]));
        if (defaultValue != null) {
            valueField.setText(defaultValue);
        }
        valueField.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false)
                .create());
        valueField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                onModify();
            }
        });
        onModify();

        applyDialogFont(pane);
        return pane;
    }

    void onModify() {
        this.result = valueField.getText();
    }

    /**
     * Returns the result text.
     * @return the result
     */
    public String getResult() {
        return result;
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        if (dialogTitle != null) {
            newShell.setText(dialogTitle);
        }
    }

    /**
     * Opens this dialog and returns the input value.
     * @param shell the parent shell
     * @param dialogTitle the dialog title
     * @param fieldTitle the field title
     * @param defaultValue the default value (nullable)
     * @param history the history values (nullable)
     * @return the input value, or {@code null} if canceled
     */
    public static String open(
            Shell shell,
            String dialogTitle,
            String fieldTitle,
            String defaultValue,
            List<String> history) {
        InputWithHistoryDialog dialog = new InputWithHistoryDialog(shell, dialogTitle, fieldTitle, defaultValue, history);
        if (dialog.open() != Window.OK) {
            return null;
        }
        return dialog.getResult();
    }
}
