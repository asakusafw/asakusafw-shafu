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
package com.asakusafw.shafu.core.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Utilities about {@link IStatus}.
 */
public final class StatusUtils {

    private StatusUtils() {
        return;
    }

    /**
     * Detects the target status has the cancel severity.
     * @param status the target status
     * @return {@code true} iff the target status has the severity
     */
    public static boolean hasCancel(IStatus status) {
        return hasSeverity(status, IStatus.CANCEL);
    }

    /**
     * Detects the target status has the error severity.
     * @param status the target status
     * @return {@code true} iff the target status has the severity
     */
    public static boolean hasError(IStatus status) {
        return hasSeverity(status, IStatus.ERROR);
    }

    /**
     * Detects the target status has the warning severity.
     * @param status the target status
     * @return {@code true} iff the target status has the severity
     */
    public static boolean hasWarn(IStatus status) {
        return hasSeverity(status, IStatus.WARNING);
    }

    /**
     * Detects the target status has the information severity.
     * @param status the target status
     * @return {@code true} iff the target status has the severity
     */
    public static boolean hasInfo(IStatus status) {
        return hasSeverity(status, IStatus.INFO);
    }

    /**
     * Detects the target status has the specified severity.
     * @param status the target status
     * @param severity the severity
     * @return {@code true} iff the target status has the severity
     */
    public static boolean hasSeverity(IStatus status, int severity) {
        return (status.getSeverity() & severity) == severity;
    }

    /**
     * Returns the severity for the status.
     * @param status the target status
     * @return the related severity
     */
    public static Severity severity(IStatus status) {
        return Severity.from(status.getSeverity());
    }

    /**
     * Re-throws {@link CoreException} only if it has cancel status.
     * @param e the original exception
     * @throws CoreException if the original exception has cancel status
     */
    public static void rethrowIfCancel(CoreException e) throws CoreException {
        if (hasCancel(e.getStatus())) {
            throw e;
        }
    }

    /**
     * Throws {@link CoreException} with cancel status only if cancel is requested.
     * @param monitor the target monitor
     * @throws CoreException if cancel is requested
     */
    public static void checkCanceled(IProgressMonitor monitor) throws CoreException {
        if (monitor != null && monitor.isCanceled()) {
            throw new CoreException(Status.CANCEL_STATUS);
        }
    }

    /**
     * Represents a severity.
     */
    public enum Severity {

        /**
         * Cancel.
         */
        CANCEL(IStatus.CANCEL),

        /**
         * Error.
         */
        ERROR(IStatus.ERROR),

        /**
         * Warning.
         */
        WARN(IStatus.WARNING),

        /**
         * Information.
         */
        INFO(IStatus.INFO),

        /**
         * Ok.
         */
        OK(IStatus.OK),
        ;

        private final int code;

        private Severity(int code) {
            this.code = code;
        }

        /**
         * Returns the code.
         * @return the code
         * @see IStatus
         */
        public int getCode() {
            return code;
        }

        /**
         * Returns the constant for the specified severity.
         * @param severity the severity bits
         * @return corresponded constant
         */
        public static Severity from(int severity) {
            if (has(severity, CANCEL.code)) {
                return CANCEL;
            } else if (has(severity, ERROR.code)) {
                return ERROR;
            } else if (has(severity, WARN.code)) {
                return WARN;
            } else if (has(severity, INFO.code)) {
                return INFO;
            }
            return OK;
        }

        private static boolean has(int severity, int code) {
            return (severity & code) != 0;
        }
    }
}
