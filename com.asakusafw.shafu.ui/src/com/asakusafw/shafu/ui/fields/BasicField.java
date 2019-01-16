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
package com.asakusafw.shafu.ui.fields;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

import com.asakusafw.shafu.core.util.StatusUtils;

/**
 * A basic implementation of {@link IField}.
 */
public class BasicField implements IField {

    private static final FieldDecoration DECORATION_MANDATORY =
            FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED);

    private static final FieldDecoration DECORATION_INFO =
            FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION);

    private static final FieldDecoration DECORATION_WARN =
            FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_WARNING);

    private static final FieldDecoration DECORATION_ERROR =
            FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);

    private final String key;

    private final ControlDecoration decoration;

    private final boolean mandatory;

    private IStatus status;

    /**
     * Creates a new instance.
     * @param key the field key
     * @param control the target control
     */
    public BasicField(String key, Control control) {
        this(key, control, false);
    }

    /**
     * Creates a new instance.
     * @param key the field key
     * @param control the target control
     * @param mandatory whether the field is mandatory or not
     */
    public BasicField(String key, Control control, boolean mandatory) {
        this.key = key;
        this.decoration = new ControlDecoration(control, SWT.LEFT | SWT.TOP);
        this.status = Status.OK_STATUS;
        this.mandatory = mandatory;
    }

    /**
     * Returns the decoration width in pixels.
     * @return the decoration width in pixels
     */
    public static int getDecorationWidth() {
        return FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Control getControl() {
        return decoration.getControl();
    }

    @Override
    public IStatus getStatus() {
        if (this.status == null) {
            return getDefaultStatus();
        }
        return status;
    }

    /**
     * Returns the default status.
     * @return the default status
     */
    protected IStatus getDefaultStatus() {
        return Status.OK_STATUS;
    }

    @Override
    public void setStatus(IStatus status) {
        if (status == null) {
            this.status = getDefaultStatus();
        } else {
            this.status = status;
        }
        String message = this.status.getMessage();
        switch (StatusUtils.severity(this.status)) {
        case ERROR:
            decoration.setImage(DECORATION_ERROR.getImage());
            decoration.setDescriptionText(message);
            break;
        case WARN:
            decoration.setImage(DECORATION_WARN.getImage());
            decoration.setDescriptionText(message);
            break;
        case INFO:
            decoration.setImage(DECORATION_INFO.getImage());
            decoration.setDescriptionText(message);
            break;
        default:
            if (mandatory) {
                decoration.setImage(DECORATION_MANDATORY.getImage());
                decoration.setDescriptionText(Messages.BasicField_required);
            } else {
                decoration.setImage(null);
                decoration.setDescriptionText(null);
            }
            break;
        }
    }

    @Override
    public void dispose() {
        decoration.dispose();
    }
}
