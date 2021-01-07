/**
 * Copyright 2013-2021 Asakusa Framework Team.
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
package com.asakusafw.shafu.core.gradle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.gradle.tooling.GradleConnectionException;

/**
 * A wrapper exception for {@link GradleConnectionException}.
 * @see #getGradleConnectionException()
 */
public class GradleException extends CoreException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.
     * @param status the status
     * @param cause the original cause
     */
    public GradleException(IStatus status, GradleConnectionException cause) {
        super(status);
        initCause(cause);
    }

    /**
     * Returns the original exception.
     * @return the original exception
     */
    public GradleConnectionException getGradleConnectionException() {
        return getCause();
    }

    @Override
    public GradleConnectionException getCause() {
        return (GradleConnectionException) super.getCause();
    }

}
