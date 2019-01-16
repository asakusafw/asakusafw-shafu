/**
 * Copyright 2013-2019 Asakusa Framework Team.
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

import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.asakusafw.shafu.internal.ui.Activator;
import com.asakusafw.shafu.ui.fields.BasicField;
import com.asakusafw.shafu.ui.fields.IField;

/**
 * A dialog for input a property entry.
 */
public class PropertyEntryInputDialog extends Dialog {

    private final Map<String, String> properties;

    private final String defaultKey;

    private final String defaultValue;

    private String inputKey;

    private String inputValue;

    private Text textKey;

    private Text textValue;

    private IField fieldKey;

    private Button buttonOk;

    /**
     * Creates a new instance.
     * @param shell the parent shell
     * @param properties target properties
     */
    public PropertyEntryInputDialog(Shell shell, Map<String, String> properties) {
        this(shell, properties, null, null);
    }

    /**
     * Creates a new instance.
     * @param shell the parent shell
     * @param properties target properties
     * @param defaultKey the default key (nullable)
     * @param defaultValue the default value (nullable)
     */
    public PropertyEntryInputDialog(
            Shell shell,
            Map<String, String> properties,
            String defaultKey,
            String defaultValue) {
        super(shell);
        this.properties = properties;
        this.defaultKey = defaultKey;
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the input key.
     * @return the input key, or {@code null} if this dialog is not completed normally
     */
    public String getInputKey() {
        return inputKey;
    }

    /**
     * Returns the input value.
     * @return the input value, or {@code null} if this dialog is not completed normally
     */
    public String getInputValue() {
        return inputValue;
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            this.inputKey = textKey.getText().trim();
            this.inputValue = textValue.getText();
        } else {
            this.inputKey = null;
            this.inputValue = null;
        }
        super.buttonPressed(buttonId);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.PropertyEntryInputDialog_title);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        this.buttonOk = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

        if (defaultKey == null) {
            textKey.setFocus();
            buttonOk.setEnabled(false);
        } else {
            if (defaultKey.isEmpty()) {
                textKey.setFocus();
            } else {
                textValue.setFocus();
                textValue.selectAll();
            }
            validate();
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite pane = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) pane.getLayout();
        layout.verticalSpacing = 0;

        createLabel(pane, Messages.PropertyEntryInputDialog_keyLabel, 0);
        this.textKey = createText(pane, defaultKey, 0);
        this.fieldKey = new BasicField(null, textKey, true);
        this.fieldKey.setStatus(null);
        textKey.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validate();
            }
        });

        createLabel(pane, Messages.PropertyEntryInputDialog_valueLabel, 5);
        this.textValue = createText(pane, defaultValue, 0);
        this.textValue.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validate();
            }
        });

        applyDialogFont(pane);
        return pane;
    }

    private Label createLabel(Composite pane, String title, int hMargin) {
        Label label = new Label(pane, SWT.NONE);
        label.setText(title);
        label.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false)
                .indent(BasicField.getDecorationWidth(), this.convertVerticalDLUsToPixels(hMargin))
                .hint(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH, SWT.DEFAULT)
                .create());
        return label;
    }

    private Text createText(Composite pane, String initialValue, int hMargin) {
        Text text = new Text(pane, SWT.BORDER);
        if (initialValue != null) {
            text.setText(initialValue);
        }
        text.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false)
                .indent(BasicField.getDecorationWidth(), this.convertVerticalDLUsToPixels(hMargin))
                .create());
        return text;
    }

    void validate() {
        String key = textKey.getText().trim();
        if (key.isEmpty()) {
            String message = Messages.PropertyEntryInputDialog_errorKeyEmpty;
            fieldKey.setStatus(new Status(
                    IStatus.ERROR,
                    Activator.PLUGIN_ID,
                    message));
            buttonOk.setEnabled(false);
        } else if (key.equals(defaultKey) == false && properties.containsKey(key)) {
            String message = MessageFormat.format(
                    Messages.PropertyEntryInputDialog_errorKeyConflict,
                    key);
            fieldKey.setStatus(new Status(
                    IStatus.ERROR,
                    Activator.PLUGIN_ID,
                    message));
            buttonOk.setEnabled(false);
        } else {
            fieldKey.setStatus(null);
            buttonOk.setEnabled(true);
        }
    }

    @Override
    public boolean close() {
        if (fieldKey != null) {
            fieldKey.dispose();
            fieldKey = null;
        }
        return super.close();
    }
}
