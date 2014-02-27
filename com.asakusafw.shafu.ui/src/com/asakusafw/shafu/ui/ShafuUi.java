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
package com.asakusafw.shafu.ui;

import static com.asakusafw.shafu.internal.ui.preferences.ShafuPreferenceConstants.*;
import static com.asakusafw.shafu.ui.util.PreferenceUtils.*;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.asakusafw.shafu.core.gradle.GradleBuildTask;
import com.asakusafw.shafu.core.gradle.GradleContext;
import com.asakusafw.shafu.core.gradle.GradleInspectTask;
import com.asakusafw.shafu.core.gradle.RefreshTask;
import com.asakusafw.shafu.core.util.RunnableBuilder;
import com.asakusafw.shafu.internal.ui.Activator;
import com.asakusafw.shafu.internal.ui.consoles.ShafuConsoleManager;
import com.asakusafw.shafu.internal.ui.dialogs.ConsoleDialog;
import com.asakusafw.shafu.internal.ui.preferences.GradleLogLevel;
import com.asakusafw.shafu.internal.ui.preferences.GradleNetworkMode;
import com.asakusafw.shafu.internal.ui.preferences.GradleOption;
import com.asakusafw.shafu.internal.ui.preferences.GradleStackTrace;
import com.asakusafw.shafu.ui.consoles.ShafuConsole;

/**
 * Core APIs of Shafu UI Plug-in.
 * @version 0.2.4
 */
public final class ShafuUi {

    private ShafuUi() {
        return;
    }

    /**
     * Schedules the Gradle tasks.
     * @param project the target project
     * @param tasks the target tasks
     */
    public static void scheduleTasks(IProject project, List<String> tasks) {
        scheduleTasks(project, tasks, Collections.<String>emptyList());
    }

    /**
     * Schedules the Gradle tasks.
     * @param project the target project
     * @param tasks the target tasks
     * @param arguments the build arguments
     * @since 0.2.4
     */
    public static void scheduleTasks(IProject project, List<String> tasks, List<String> arguments) {
        GradleContext configuration = ShafuUi.createContext(project.getLocation().toFile(), arguments);
        ShafuConsole console = ShafuUi.getGlobalConsole(true);
        console.clearConsole();
        console.attachTo(configuration);

        new RunnableBuilder(Messages.ShafuUi_buildJobName)
            .add(new GradleBuildTask(configuration, tasks), 90)
            .add(new RefreshTask(project), 10)
            .buildWorkspaceJob()
            .schedule();
    }

    /**
     * Creates a new {@link GradleContext} configured by Shafu UI.
     * @param projectDirectory the target project directory
     * @return the created {@link GradleContext}
     * @see GradleBuildTask
     * @see GradleInspectTask
     */
    public static GradleContext createContext(File projectDirectory) {
        return createContext(projectDirectory, Collections.<String>emptyList());
    }

    private static GradleContext createContext(File projectDirectory, List<String> arguments) {
        GradleContext context = new GradleContext(projectDirectory);

        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        GradleLogLevel logLevel = GradleLogLevel.fromSymbol(prefs.getString(KEY_LOG_LEVEL));
        GradleStackTrace stackTrace = GradleStackTrace.fromSymbol(prefs.getString(KEY_STACK_TRACE));
        GradleNetworkMode networkMode = GradleNetworkMode.fromSymbol(prefs.getString(KEY_NETWORK_MODE));
        Map<String, String> projectProps = decodeToMap(prefs.getString(KEY_PROJECT_PROPERTIES));
        Map<String, String> systemProps = decodeToMap(prefs.getString(KEY_SYSTEM_PROPERTIES));
        File gradleUserHome = decodeFile(prefs.getString(KEY_GRADLE_USER_HOME));
        File javaHome = decodeFile(prefs.getString(KEY_JAVA_HOME));

        if (appearsIn(GradleLogLevel.values(), arguments) == false) {
            context.withGradleArguments(logLevel.getArguments());
        }
        if (appearsIn(GradleStackTrace.values(), arguments) == false) {
            context.withGradleArguments(stackTrace.getArguments());
        }
        if (appearsIn(GradleNetworkMode.values(), arguments) == false) {
            context.withGradleArguments(networkMode.getArguments());
        }
        for (Map.Entry<String, String> entry : projectProps.entrySet()) {
            context.withGradleArguments(String.format("-P%s=%s", entry.getKey(), entry.getValue())); //$NON-NLS-1$
        }
        context.withGradleArguments(arguments);

        for (Map.Entry<String, String> entry : systemProps.entrySet()) {
            context.withJvmArguments(String.format("-D%s=%s", entry.getKey(), entry.getValue())); //$NON-NLS-1$
        }
        context.setGradleUserHomeDir(gradleUserHome);
        context.setJavaHomeDir(javaHome);

        return context;
    }

    private static boolean appearsIn(GradleOption[] options, List<String> arguments) {
        if (arguments.isEmpty()) {
            return false;
        }
        Set<String> candidates = new HashSet<String>();
        for (GradleOption option : options) {
            if (option.getOptionName() != null) {
                candidates.add(option.getOptionName());
            }
            if (option.getLongOptionName() != null) {
                candidates.add(option.getLongOptionName());
            }
        }
        for (String argument : arguments) {
            if (candidates.contains(argument)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the global console instance of Shafu UI.
     * @param activate {@code true} to activate the global console
     * @return the global console
     */
    public static ShafuConsole getGlobalConsole(boolean activate) {
        if (activate) {
            ShafuConsoleManager.showConsole();
        }
        return ShafuConsoleManager.getConsole();
    }

    /**
     * Opens a new console dialog.
     * @param shell the parent shell
     * @param console the target console
     */
    public static void open(Shell shell, ShafuConsole console) {
        ConsoleDialog dialog = new ConsoleDialog(shell, console);
        dialog.open();
    }
}
