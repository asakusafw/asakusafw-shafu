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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;

import com.asakusafw.shafu.core.util.StatusUtils;

/**
 * An abstract implementation of {@link PreferencePage} using {@link IPreferenceField}s.
 */
public abstract class FieldPreferencePage extends PreferencePage {

    private static final String UNKNOWN_ID = "unknown"; //$NON-NLS-1$

    private final Map<String, IPreferenceField> fields = new LinkedHashMap<>();

    private final Map<String, String> values = new HashMap<>();

    private boolean bulkRefresh = false;

    /**
     * Creates a new instance.
     */
    public FieldPreferencePage() {
        super();
    }

    /**
     * Creates a new instance.
     * @param title the page title
     */
    public FieldPreferencePage(String title) {
        super(title);
    }

    /**
     * Creates a new instance.
     * @param title the page title
     * @param image the page image
     */
    public FieldPreferencePage(String title, ImageDescriptor image) {
        super(title, image);
    }

    /**
     * Registers a new preference field and refresh the field.
     * @param field the new field
     * @param <T> the field type
     * @return the target field
     */
    public <T extends IPreferenceField> T registerField(T field) {
        return registerField(field, null);
    }

    /**
     * Returns the registered field.
     * @param key the preference key
     * @return the registered field
     */
    public IPreferenceField getField(String key) {
        return fields.get(key);
    }

    /**
     * Registers a new preference field and refresh the field.
     * @param field the new field
     * @param status the initial status
     * @param <T> the field type
     * @return the target field
     */
    public <T extends IPreferenceField> T registerField(T field, IStatus status) {
        fields.put(field.getKey(), field);
        IPreferenceStore prefs = getPreferenceStore();
        values.put(field.getKey(), prefs.getString(field.getKey()));
        field.setStatus(status);
        field.refresh();
        return field;
    }

    /**
     * @deprecated Use {@link #getPreferenceValue(String)} and {@link #setPreferenceValue(String, String, IStatus)} instead
     */
    @Deprecated
    @Override
    public IPreferenceStore getPreferenceStore() {
        return super.getPreferenceStore();
    }

    /**
     * @deprecated Use {@link #setStatus(String, IStatus)} instead
     */
    @Deprecated
    @Override
    public void setMessage(String newMessage) {
        super.setMessage(newMessage);
    }

    /**
     * @deprecated Use {@link #setStatus(String, IStatus)} instead
     */
    @Deprecated
    @Override
    public void setMessage(String newMessage, int newType) {
        super.setMessage(newMessage, newType);
    }

    /**
     * @deprecated Use {@link #setError(String, String)} instead
     */
    @Deprecated
    @Override
    public void setErrorMessage(String newMessage) {
        super.setErrorMessage(newMessage);
    }

    /**
     * Notify OK status to the target field.
     * @param key the key
     */
    public void setOk(String key) {
        setStatus(key, Status.OK_STATUS);
    }

    /**
     * Notify an information message to the target field.
     * @param key the key
     * @param message the message
     */
    public void setInfo(String key, String message) {
        setStatus(key, new Status(IStatus.INFO, UNKNOWN_ID, message));
    }

    /**
     * Notify an warning message to the target field.
     * @param key the key
     * @param message the message
     */
    public void setWarn(String key, String message) {
        setStatus(key, new Status(IStatus.WARNING, UNKNOWN_ID, message));
    }

    /**
     * Notify an error message to the target field.
     * @param key the key
     * @param message the message
     */
    public void setError(String key, String message) {
        setStatus(key, new Status(IStatus.ERROR, UNKNOWN_ID, message));
    }

    /**
     * Notify a new status to the target field.
     * @param key the preference key
     * @param status the new status, or {@code null} to reset the status
     */
    public void setStatus(String key, IStatus status) {
        IPreferenceField field = fields.get(key);
        if (field != null) {
            field.setStatus(status);
            refreshPageStatus();
        }
    }

    /**
     * Returns the preference value of the target field.
     * @param key the preference key
     * @return the preference value
     */
    public String getPreferenceValue(String key) {
        String value = values.get(key);
        if (value == null) {
            return getPreferenceStore().getDefaultString(key);
        }
        return value;
    }

    /**
     * Notify a new preference value and turn the field status as the default status.
     * @param key the preference key
     * @param value the new value
     */
    public void setPreferenceValue(String key, String value) {
        setPreferenceValue(key, value, null);
    }

    /**
     * Notify a new preference value and turn the field status as the specified status.
     * @param key the preference key
     * @param value the new value
     * @param status the new status
     */
    public void setPreferenceValue(String key, String value, IStatus status) {
        values.put(key, value);
        setStatus(key, status);
    }

    /**
     * Refreshes all field values.
     */
    public void refreshFields() {
        bulkRefresh = true;
        try {
            for (IPreferenceField field : fields.values()) {
                field.refresh();
            }
        } finally {
            bulkRefresh = false;
        }
        refreshPageStatus();
    }

    /**
     * Refreshes the page status.
     */
    public void refreshPageStatus() {
        if (bulkRefresh) {
            return;
        }
        IStatus max = Status.OK_STATUS;
        for (IPreferenceField field : fields.values()) {
            IStatus status = field.getStatus();
            if (status.getSeverity() > max.getSeverity()) {
                max = status;
            }
        }
        switch (StatusUtils.severity(max)) {
        case ERROR:
            setMessage(null);
            setErrorMessage(max.getMessage());
            setValid(false);
            break;
        case WARN:
            setErrorMessage(null);
            setMessage(max.getMessage(), WARNING);
            setValid(true);
            break;
        default:
            setErrorMessage(null);
            setMessage(null);
            setValid(true);
            break;
        }
    }

    @Override
    public boolean performOk() {
        IPreferenceStore prefs = getPreferenceStore();
        for (IPreferenceField field : fields.values()) {
            String key = field.getKey();
            String value = values.get(key);
            if (value == null) {
                prefs.setToDefault(key);
            } else {
                prefs.setValue(key, value);
            }
        }
        return true;
    }

    @Override
    protected void performDefaults() {
        IPreferenceStore prefs = getPreferenceStore();
        for (IPreferenceField field : fields.values()) {
            String key = field.getKey();
            values.put(key, prefs.getDefaultString(key));
        }
        refreshFields();
        super.performDefaults();
    }

    @Override
    public void dispose() {
        try {
            for (IPreferenceField field : fields.values()) {
                field.dispose();
            }
            fields.clear();
        } finally {
            super.dispose();
        }
    }
}
