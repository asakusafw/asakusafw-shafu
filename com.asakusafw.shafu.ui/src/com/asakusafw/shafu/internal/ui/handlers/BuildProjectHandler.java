/**
 * Copyright 2013 Asakusa Framework Team.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;

import com.asakusafw.shafu.internal.ui.Activator;
import com.asakusafw.shafu.ui.ShafuUi;
import com.asakusafw.shafu.ui.util.ProjectHandlerUtils;

/**
 * Handles build command.
 */
public class BuildProjectHandler extends AbstractHandler {

    private static final String PARAMETER_TASK_NAMES = "taskNames"; //$NON-NLS-1$

    private static final String PROPERTY_TASK_NAMES = "taskNames"; //$NON-NLS-1$

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IProject project = ProjectHandlerUtils.getTargetProject(event);
        if (project == null) {
            return null;
        }
        List<String> tasks = getTasks(event);
        if (tasks == null) {
            return null;
        }

        ShafuUi.scheduleTasks(project, tasks);
        return null;
    }

    private List<String> getTasks(ExecutionEvent event) throws ExecutionException {
        String taskNames = event.getParameter(PARAMETER_TASK_NAMES);
        if (taskNames == null) {
            String defaultTaskNames = loadDefaultTaskNames();
            InputDialog dialog = new InputDialog(
                    HandlerUtil.getActiveShellChecked(event),
                    Messages.BuildProjectHandler_inputTitle,
                    Messages.BuildProjectHandler_inputLabel,
                    defaultTaskNames,
                    null);
            if (dialog.open() != Window.OK) {
                return null;
            }
            taskNames = dialog.getValue();
            saveDefaultTaskNames(taskNames);
        }
        List<String> tasks = new ArrayList<String>();
        for (String field : taskNames.split("\\s+")) { //$NON-NLS-1$
            String task = field.trim();
            if (task.isEmpty() == false) {
                tasks.add(task);
            }
        }
        return tasks;
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
    }
}
