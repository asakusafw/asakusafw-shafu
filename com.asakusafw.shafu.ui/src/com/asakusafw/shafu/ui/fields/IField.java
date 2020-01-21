/**
 * Copyright 2013-2020 Asakusa Framework Team.
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
import org.eclipse.swt.widgets.Control;

/**
 * Represents a field.
 */
public interface IField {

    /**
     * Returns the field key.
     * @return the field key
     */
    String getKey();

    /**
     * Returns the target control.
     * @return the target control
     */
    Control getControl();

    /**
     * Disposes this field.
     */
    void dispose();

    /**
     * Returns the status of this field.
     * @return the status
     */
    IStatus getStatus();

    /**
     * Sets the status of this field.
     * @param status the status, or {@code null} to reset to the default
     */
    void setStatus(IStatus status);
}
