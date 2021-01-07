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
package com.asakusafw.shafu.internal.ui.preferences;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Preferences constants for Shafu UI.
 * @since 0.1.0
 * @version 0.7.0
 */
public final class ShafuPreferenceConstants {

    private ShafuPreferenceConstants() {
        return;
    }

    /**
     * The log level property key.
     */
    public static final String KEY_LOG_LEVEL = "logLevel"; //$NON-NLS-1$

    /**
     * The stack trace property key.
     */
    public static final String KEY_STACK_TRACE = "stackTrace"; //$NON-NLS-1$

    /**
     * The network mode property key.
     */
    public static final String KEY_NETWORK_MODE = "networkMode"; //$NON-NLS-1$

    /**
     * The project properties property key.
     */
    public static final String KEY_PROJECT_PROPERTIES = "projectProperties"; //$NON-NLS-1$

    /**
     * The system properties property key.
     */
    public static final String KEY_SYSTEM_PROPERTIES = "systemProperties"; //$NON-NLS-1$

    /**
     * The environment variables key.
     * @since 0.5.2
     */
    public static final String KEY_ENVIRONMENT_VARIABLES = "environmentVariables"; //$NON-NLS-1$

    /**
     * The Gradle user home property key.
     */
    public static final String KEY_GRADLE_USER_HOME = "gradleUserHome"; //$NON-NLS-1$

    /**
     * The Java home property key.
     */
    public static final String KEY_JAVA_HOME = "javaHome"; //$NON-NLS-1$

    /**
     * The property key whether or not detect Java home from project runtime environment.
     * @since 0.7.0
     */
    public static final String KEY_USE_PROJECT_JAVA_HOME = "useProjectJavaHome"; //$NON-NLS-1$

    /**
     * The Gradle distribution property key.
     */
    public static final String KEY_GRADLE_VERSION = "gradleVersion"; //$NON-NLS-1$

    /**
     * The property key whether use HTTPS on downloading Gradle distributions.
     * @since 0.3.1
     */
    public static final String KEY_USE_HTTPS = "useHttps"; //$NON-NLS-1$

    /**
     * The property key whether or not use Gradle wrapper configuration for detecting distribution URL.
     * @since 0.7.0
     */
    public static final String KEY_USE_WRAPPER_CONFIGURATION = "useWrapperConf"; //$NON-NLS-1$

    /**
     * The Gradle wrapper configuration paths (separated by comma) property key.
     * This property will be activated only if {@link #KEY_USE_WRAPPER_CONFIGURATION} is enabled.
     * @since 0.7.0
     * @see #KEY_USE_WRAPPER_CONFIGURATION
     */
    public static final String KEY_WRAPPER_CONFIGURATION_PATHS = "wrapperConfPaths"; //$NON-NLS-1$

    /**
     * The Gradle distribution property key.
     */
    public static final String KEY_GRADLE_DISTRIBUTION = "gradleDistribution"; //$NON-NLS-1$

    /**
     * The log level default value.
     */
    public static final GradleLogLevel DEFAULT_LOG_LEVEL = GradleLogLevel.LIFECYCLE;

    /**
     * The stack trace default value.
     */
    public static final GradleStackTrace DEFAULT_STACK_TRACE = GradleStackTrace.NEVER;

    /**
     * The network mode default value.
     */
    public static final GradleNetworkMode DEFAULT_NETWORK_MODE = GradleNetworkMode.ONLINE;

    /**
     * The project properties default value.
     */
    public static final Map<String, String> DEFAULT_PROJECT_PROPERTIES = Collections.emptyMap();

    /**
     * The system properties default value.
     */
    public static final Map<String, String> DEFAULT_SYSTEM_PROPERTIES =
            Collections.singletonMap("file.encoding", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * The Gradle user home default value.
     */
    public static final File DEFAULT_GRADLE_USER_HOME = null;

    /**
     * The Java home default value.
     */
    public static final File DEFAULT_JAVA_HOME = null;

    /**
     * The default value of {@link #KEY_USE_PROJECT_JAVA_HOME}.
     * @since 0.7.0
     */
    public static final boolean DEFAULT_USE_PROJECT_JAVA_HOME = true;

    /**
     * The Gradle version default value ({@value}).
     * @since 0.2.7
     */
    public static final String DEFAULT_GRADLE_VERSION = "4.3.1"; //$NON-NLS-1$

    /**
     * The default value of {@link #KEY_USE_HTTPS}.
     *
     * NOTE: Shafu changed the default value since 0.5.1 -
     * Insecure location returns 301, but Gradle tooling API does not follow to the moved location.
     *
     * @since 0.3.1
     */
    public static final boolean DEFAULT_USE_HTTPS = true;

    /**
     * The default value of {@link #KEY_USE_WRAPPER_CONFIGURATION}.
     * @since 0.7.0
     */
    public static final boolean DEFAULT_USE_WRAPPER_CONFIGURATION = true;

    /**
     * The default value of {@link #KEY_WRAPPER_CONFIGURATION_PATHS}.
     * The Gradle wrapper configuration paths (separated by comma) property key.
     * This property will be activated only if {@link #KEY_USE_WRAPPER_CONFIGURATION} is enabled.
     * @since 0.7.0
     * @see #KEY_USE_WRAPPER_CONFIGURATION
     */
    public static final List<String> DEFAULT_WRAPPER_CONFIGURATION_PATHS = Collections.unmodifiableList(Arrays.asList(
            ".buildtools/gradlew.properties", //$NON-NLS-1$
            "gradle/wrapper/gradle-wrapper.properties")); //$NON-NLS-1$

    /**
     * The Gradle distribution default value.
     * @since 0.2.7
     */
    public static final URI DEFAULT_GRADLE_DISTRIBUTION = null;
}
