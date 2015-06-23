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
package com.asakusafw.shafu.internal.ui.handlers;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.asakusafw.shafu.core.util.CommandLineUtil;
import com.asakusafw.shafu.internal.ui.Activator;
import com.asakusafw.shafu.internal.ui.LogUtil;
import com.asakusafw.shafu.internal.ui.dialogs.InputWithHistoryDialog;
import com.asakusafw.shafu.ui.ShafuUi;
import com.asakusafw.shafu.ui.util.ProjectHandlerUtils;

/**
 * Handles build command.
 * @version 0.2.8
 */
public class BuildProjectHandler extends AbstractHandler {

    private static final String PARAMETER_TASK_NAMES = "taskNames"; //$NON-NLS-1$

    private static final String PROPERTY_TASK_NAMES = "taskNames"; //$NON-NLS-1$

    private static final String PROPERTY_COMMAND_LINE_HISTORY = "taskHistory"; //$NON-NLS-1$

    static final String KEY_HISTORY_SIZE_LIMIT = "com.asakusafw.shafu.ui.history.limit"; //$NON-NLS-1$

    static final int DEFAULT_HISTORY_SIZE_LIMIT = 10;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IProject project = ProjectHandlerUtils.getTargetProject(event);
        if (project == null) {
            return null;
        }
        String commandLine = getTaskNames(event, project.getName());
        if (commandLine == null) {
            return null;
        }
        if (PlatformUI.getWorkbench().saveAllEditors(true) == false) {
            return null;
        }
        List<String> tasks = CommandLineUtil.parseGradleTaskNames(commandLine);
        List<String> arguments = CommandLineUtil.parseGradleBuildArguments(commandLine);
        ShafuUi.scheduleTasks(project, tasks, arguments);
        return null;
    }

    private String getTaskNames(ExecutionEvent event, String location) throws ExecutionException {
        String taskNames = event.getParameter(PARAMETER_TASK_NAMES);
        if (taskNames == null) {
            String defaultTaskNames = loadDefaultTaskNames();
            List<String> commandLineHistory = loadCommandLineHisotry();
            taskNames = InputWithHistoryDialog.open(
                    HandlerUtil.getActiveShellChecked(event),
                    MessageFormat.format(Messages.BuildProjectHandler_inputTitle, location),
                    Messages.BuildProjectHandler_inputLabel,
                    defaultTaskNames,
                    commandLineHistory);
            if (taskNames == null) {
                return null;
            }
            saveDefaultTaskNames(taskNames);
        }
        return taskNames;
    }

    private String loadDefaultTaskNames() {
        IDialogSettings settings = Activator.getDialogSettings(getClass().getSimpleName());
        String result = settings.get(PROPERTY_TASK_NAMES);
        if (result == null) {
            return ""; //$NON-NLS-1$
        }
        return result;
    }

    private void saveDefaultTaskNames(String taskNames) {
        IDialogSettings settings = Activator.getDialogSettings(getClass().getSimpleName());
        settings.put(PROPERTY_TASK_NAMES, taskNames);

        LinkedList<String> history = loadCommandLineHisotry();
        history.remove(taskNames);
        history.addFirst(taskNames);
        while (history.size() > Lazy.HISTORY_SIZE_LIMIT) {
            history.removeLast();
        }

        settings.put(PROPERTY_COMMAND_LINE_HISTORY, history.toArray(new String[history.size()]));
    }

    private LinkedList<String> loadCommandLineHisotry() {
        IDialogSettings settings = Activator.getDialogSettings(getClass().getSimpleName());
        String[] values = settings.getArray(PROPERTY_COMMAND_LINE_HISTORY);
        LinkedList<String> results = new LinkedList<String>();
        if (values != null) {
            Collections.addAll(results, values);
        }
        return results;
    }

    private static final class Lazy {

        static final int HISTORY_SIZE_LIMIT;
        static {
            String value = System.getProperty(KEY_HISTORY_SIZE_LIMIT);
            int result = DEFAULT_HISTORY_SIZE_LIMIT;
            if (value != null && value.trim().isEmpty() == false) {
                try {
                    result = Integer.parseInt(value.trim());
                } catch (NumberFormatException e) {
                    LogUtil.log(IStatus.ERROR,
                            MessageFormat.format(
                                    "Invalid history size: {0}={1}", //$NON-NLS-1$
                                    KEY_HISTORY_SIZE_LIMIT,
                                    value),
                            e);
                }
            }
            HISTORY_SIZE_LIMIT = Math.max(result, DEFAULT_HISTORY_SIZE_LIMIT);
        }

        private Lazy() {
            return;
        }
    }
}
