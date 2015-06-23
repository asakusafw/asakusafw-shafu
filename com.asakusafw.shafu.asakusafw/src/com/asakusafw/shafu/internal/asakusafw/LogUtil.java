/**
 * Copyright 2013-2015 Asakusa Framework Team.
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
package com.asakusafw.shafu.internal.asakusafw;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Utilities for logging.
 */
public class LogUtil {

    private static final String ID = Activator.PLUGIN_ID;
    /**
     * Adds a log record.
     * @param severity {@code org.eclipse.core.runtime.IStatus}
     * @param message log message
     */
    public static void log(int severity, String message) {
        log(severity, ID, message);
    }

    /**
     * Adds a log record.
     * @param severity {@code org.eclipse.core.runtime.IStatus}
     * @param message log message
     * @param exception an exception
     */
    public static void log(int severity, String message, Throwable exception) {
        log(severity, ID, message, exception);
    }

    /**
     * Adds a log record.
     * @param status status
     */
    public static void log(IStatus status) {
        if (status == null) {
            throw new NullPointerException("status"); //$NON-NLS-1$
        }
        log0(status);
    }

    /**
     * Adds a debug log record.
     * @param pattern the message pattern in {@link MessageFormat}
     * @param arguments the message arguments
     */
    public static void debug(String pattern, Object... arguments) {
        if (Activator.getDefault().isDebugging()) {
            log(IStatus.INFO, ID, MessageFormat.format(pattern, arguments));
        }
    }

    /**
     * Adds a debug log record.
     * @param status status
     */
    public static void debug(IStatus status) {
        if (Activator.getDefault().isDebugging()) {
            log0(status);
        }
    }

    private static void log(int severity, String pluginId, String message) {
        Status status = new Status(severity, pluginId, message);
        log0(status);
    }

    private static void log(int severity, String pluginId, String message, Throwable exception) {
        Status status = new Status(
            severity,
            pluginId,
            message == null ? "" : message, //$NON-NLS-1$
            exception);
        log0(status);
    }

    private static void log0(IStatus status) {
        Activator.getDefault().getLog().log(status);
    }

    private LogUtil() {
        return;
    }
}
