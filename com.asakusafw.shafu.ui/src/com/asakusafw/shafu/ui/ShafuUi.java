/**
 * Copyright 2013-2017 Asakusa Framework Team.
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.asakusafw.shafu.core.gradle.GradleBuildTask;
import com.asakusafw.shafu.core.gradle.GradleContext;
import com.asakusafw.shafu.core.gradle.GradleInspectTask;
import com.asakusafw.shafu.core.gradle.RefreshTask;
import com.asakusafw.shafu.core.util.RunnableBuilder;
import com.asakusafw.shafu.core.util.RuntimeUtils;
import com.asakusafw.shafu.internal.ui.Activator;
import com.asakusafw.shafu.internal.ui.LogUtil;
import com.asakusafw.shafu.internal.ui.consoles.ShafuConsoleManager;
import com.asakusafw.shafu.internal.ui.dialogs.ConsoleDialog;
import com.asakusafw.shafu.internal.ui.preferences.GradleLogLevel;
import com.asakusafw.shafu.internal.ui.preferences.GradleNetworkMode;
import com.asakusafw.shafu.internal.ui.preferences.GradleOption;
import com.asakusafw.shafu.internal.ui.preferences.GradleStackTrace;
import com.asakusafw.shafu.ui.consoles.ShafuConsole;

/**
 * Core APIs of Shafu UI Plug-in.
 * @since 0.1.0
 * @version 0.4.3
 */
public final class ShafuUi {

    private static final String PATH_FLAT_ROOT_PROJECT = "master"; //$NON-NLS-1$

    private static final String PATH_GRADLE_PROJECT_SETTINGS = "settings.gradle"; //$NON-NLS-1$

    private static final String KEY_DISTRIBUTION_URL = "distributionUrl"; //$NON-NLS-1$

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
        GradleContext configuration = ShafuUi.createContext(project, project.getLocation().toFile(), arguments);
        scheduleTasks(project, configuration, tasks);
    }

    /**
     * Schedules the Gradle tasks.
     * @param project the target project
     * @param configuration the Gradle configuration
     * @param tasks the target tasks
     * @since 0.4.3
     */
    public static void scheduleTasks(IProject project, GradleContext configuration, List<String> tasks) {
        ShafuConsole console = ShafuUi.getGlobalConsole(true);
        console.reset();
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
        return createContext(null, projectDirectory, Collections.<String>emptyList());
    }

    /**
     * Creates a new {@link GradleContext} configured by Shafu UI.
     * @param project the target project
     * @param arguments the build arguments
     * @return the created {@link GradleContext}
     * @see GradleBuildTask
     * @see GradleInspectTask
     * @since 0.4.3
     */
    public static GradleContext createContext(IProject project, List<String> arguments) {
        return createContext(project, project.getLocation().toFile(), arguments);
    }

    private static GradleContext createContext(IProject project, File projectDirectory, List<String> arguments) {
        GradleContext context = new GradleContext(projectDirectory);

        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        GradleLogLevel logLevel = GradleLogLevel.fromSymbol(prefs.getString(KEY_LOG_LEVEL));
        GradleStackTrace stackTrace = GradleStackTrace.fromSymbol(prefs.getString(KEY_STACK_TRACE));
        GradleNetworkMode networkMode = GradleNetworkMode.fromSymbol(prefs.getString(KEY_NETWORK_MODE));
        Map<String, String> projectProps = decodeToMap(prefs.getString(KEY_PROJECT_PROPERTIES));
        Map<String, String> systemProps = decodeToMap(prefs.getString(KEY_SYSTEM_PROPERTIES));
        Map<String, String> environments = decodeToMap(prefs.getString(KEY_ENVIRONMENT_VARIABLES));
        File gradleUserHome = decodeFile(prefs.getString(KEY_GRADLE_USER_HOME));
        File javaHome = computeJavaHome(project, prefs);
        String gradleVersion = decodeVersion(prefs.getString(KEY_GRADLE_VERSION));
        URI gradleDistribution = decodeUri(prefs.getString(KEY_GRADLE_DISTRIBUTION));
        boolean useHttps = prefs.getBoolean(KEY_USE_HTTPS);
        boolean useWrapper = prefs.getBoolean(KEY_USE_WRAPPER_CONFIGURATION);
        List<String> wrapperPaths = decodeToList(prefs.getString(KEY_WRAPPER_CONFIGURATION_PATHS));

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
        context.withEnvironmentVariables(environments);

        context.setGradleUserHomeDir(gradleUserHome);
        context.setJavaHomeDir(javaHome);
        context.setGradleVersion(gradleVersion);
        context.setUseHttps(useHttps);
        if (gradleDistribution != null) {
            context.setGradleDistribution(gradleDistribution);
        } else if (useWrapper) {
            URI wrapperGradleDistribution = findGradleDistributionFromWrapper(projectDirectory, wrapperPaths);
            context.setGradleDistribution(wrapperGradleDistribution);
        } else {
            context.setGradleDistribution(null);
        }

        return context;
    }

    private static URI findGradleDistributionFromWrapper(File projectDirectory, List<String> wrapperPaths) {
        URI candidate = findGradleDistributionFromWrapper0(projectDirectory, wrapperPaths);
        if (candidate != null) {
            return candidate;
        }
        File rootProjectDirectory = findRootProjectDirectory(projectDirectory);
        if (rootProjectDirectory != null && rootProjectDirectory.equals(projectDirectory) == false) {
            return findGradleDistributionFromWrapper0(rootProjectDirectory, wrapperPaths);
        }
        return null;
    }

    private static URI findGradleDistributionFromWrapper0(File baseDirectory, List<String> wrapperPaths) {
        for (String path : wrapperPaths) {
            String p = path.trim();
            if (p.isEmpty()) {
                continue;
            }
            File confFile = new File(baseDirectory, p);
            if (confFile.isFile() == false || confFile.canRead() == false) {
                continue;
            }
            Properties properties = new Properties();
            try (InputStream input = new FileInputStream(confFile)) {
                properties.load(input);
            } catch (IOException e) {
                LogUtil.log(IStatus.WARNING, MessageFormat.format(
                        "Invalid Gradle wrapper configuration file: {0}", //$NON-NLS-1$
                        confFile), e);
                return null;
            }
            URI uri = decodeUri(properties.getProperty(KEY_DISTRIBUTION_URL));
            if (uri == null) {
                continue;
            }
            LogUtil.debug("found wrapper: {0} (->{1})", confFile, uri); //$NON-NLS-1$
            return uri;
        }
        return null;
    }

    private static File findRootProjectDirectory(File projectDirectory) {
        if (hasSettingsFile(projectDirectory)) {
            LogUtil.debug("found settings.gradle: {0}", projectDirectory); //$NON-NLS-1$
            return projectDirectory;
        }

        // https://docs.gradle.org/4.4/userguide/build_lifecycle.html#sec:initialization
        File parentDirectory = projectDirectory.getParentFile();
        // It looks in a directory called 'master' which has the same nesting level as the current dir.
        if (parentDirectory != null && parentDirectory.isDirectory()) {
            File masterDirectory = new File(parentDirectory, PATH_FLAT_ROOT_PROJECT);
            if (hasSettingsFile(masterDirectory)) {
                LogUtil.debug("found master: {0}", masterDirectory); //$NON-NLS-1$
                return masterDirectory;
            }
        }

        // If not found yet, it searches parent directories.
        for (File dir = parentDirectory; dir != null && dir.isDirectory(); dir = dir.getParentFile()) {
            if (hasSettingsFile(dir)) {
                LogUtil.debug("found root project: {0}", dir); //$NON-NLS-1$
                return dir;
            }
        }

        // If not found yet, the build is executed as a single project build.
        LogUtil.debug("settings.gradle is not found: {0}", projectDirectory); //$NON-NLS-1$
        return projectDirectory;
    }

    private static boolean hasSettingsFile(File directory) {
        return directory.isDirectory() && new File(directory, PATH_GRADLE_PROJECT_SETTINGS).isFile();
    }

    private static File computeJavaHome(IProject project, IPreferenceStore prefs) {
        File javaHome = decodeFile(prefs.getString(KEY_JAVA_HOME));
        if (javaHome != null) {
            return javaHome;
        }
        File javaHomeCandidate = RuntimeUtils.getJavaHome(project);
        if (javaHomeCandidate != null && RuntimeUtils.isJavaDevelopmentKitLike(javaHomeCandidate)) {
            return javaHomeCandidate;
        }
        return null;
    }

    private static boolean appearsIn(GradleOption[] options, List<String> arguments) {
        if (arguments.isEmpty()) {
            return false;
        }
        Set<String> candidates = new HashSet<>();
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
