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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.IStatus;

import com.asakusafw.shafu.internal.core.LogUtil;

/**
 * Represents a Gradle context.
 * @since 0.1.0
 * @version 0.5.2
 */
public final class GradleContext {

    /**
     * The default file name of Gradle build script.
     */
    public static final String DEFAULT_BUILD_SCRIPT_NAME = "build.gradle"; //$NON-NLS-1$

    final File projectDirectory;

    volatile String gradleVersionOrNull;

    volatile boolean useHttps;

    volatile URI gradleDistributionOrNull;

    volatile File gradleUserHomeDirOrNull;

    volatile File javaHomeDirOrNull;

    volatile InputStream standardInputOrNull;

    volatile OutputStream standardOutputOrNull;

    volatile OutputStream standardErrorOutputOrNull;

    private volatile PrintWriter informationOutputOrNull;

    volatile List<String> jvmArguments = new ArrayList<>();

    volatile List<String> gradleArguments = new ArrayList<>();

    volatile Map<String, String> environmentVariables = new LinkedHashMap<>();

    final List<IWorkspaceRunnable> disposeActions = new ArrayList<>();

    /**
     * Creates a new instance.
     * @param projectDirectory the target project directory
     */
    public GradleContext(File projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    /**
     * Returns the target project directory.
     * @return the project directory
     */
    public File getProjectDirectory() {
        return projectDirectory;
    }

    /**
     * Returns the Gradle version.
     * This will be used only if {@link #getGradleDistribution() Gradle distribution URI} is not set.
     * @return the Gradle version, or {@code null} if the target Gradle version is not specified
     * @since 0.2.7
     */
    public String getGradleVersion() {
        return gradleVersionOrNull;
    }

    /**
     * Sets the Gradle version.
     * @param version the target Gradle version
     * @since 0.2.7
     */
    public void setGradleVersion(String version) {
        this.gradleVersionOrNull = version;
    }

    /**
     * Returns whether the Tooling API uses HTTPS on downloading Gradle distributions or not.
     * This will be used only if {@link #getGradleDistribution() Gradle distribution URI} is not set.
     * @return {@code true} to use HTTPS, otherwise {@code false}
     * @since 0.3.1
     */
    public boolean isUseHttps() {
        return useHttps;
    }

    /**
     * Sets whether the Tooling API uses HTTPS on downloading Gradle distributions or not.
     * @param use {@code true} to use HTTPS, otherwise {@code false}
     * @since 0.3.1
     */
    public void setUseHttps(boolean use) {
        this.useHttps = use;
    }

    /**
     * Returns the target distribution.
     * @return the target distribution, or {@code null} if the target distribution is not specified
     */
    public URI getGradleDistribution() {
        return gradleDistributionOrNull;
    }

    /**
     * Sets the Gradle distribution location.
     * @param identifier the target distribution
     */
    public void setGradleDistribution(URI identifier) {
        this.gradleDistributionOrNull = identifier;
    }

    /**
     * Returns the Gradle user home (for detecting {@code .gradle}).
     * @return the Gradle user home, or {@code null} if the Gradle uses {@code user.home}
     */
    public File getGradleUserHomeDir() {
        return gradleUserHomeDirOrNull;
    }

    /**
     * Sets the Gradle user home (for detecting {@code .gradle}).
     * @param directory the target directory
     */
    public void setGradleUserHomeDir(File directory) {
        this.gradleUserHomeDirOrNull = directory;
    }

    /**
     * Returns the Java installation directory.
     * @return the Java installation directory, or {@code null} if the Gradle uses {@code java.home}
     */
    public File getJavaHomeDir() {
        return javaHomeDirOrNull;
    }

    /**
     * Sets the Java installation directory.
     * @param directory the target directory
     */
    public void setJavaHomeDir(File directory) {
        this.javaHomeDirOrNull = directory;
    }

    /**
     * Returns the Java VM arguments.
     * The result list is not modifiable.
     * @return the Java VM arguments
     */
    public List<String> getJvmArguments() {
        return Collections.unmodifiableList(jvmArguments);
    }

    /**
     * Sets the Java VM arguments.
     * @param arguments the Java VM arguments
     */
    public void setJvmArguments(List<String> arguments) {
        this.jvmArguments = new ArrayList<>(arguments);
    }

    /**
     * Returns the Gradle arguments.
     * The result list is not modifiable.
     * @return the Gradle arguments
     */
    public List<String> getGradleArguments() {
        return Collections.unmodifiableList(gradleArguments);
    }

    /**
     * Sets the Gradle arguments.
     * @param arguments the Gradle arguments
     */
    public void setGradleArguments(List<String> arguments) {
        this.gradleArguments = new ArrayList<>(arguments);
    }

    /**
     * Returns the environment variables.
     * @return the environment variables
     * @since 0.5.2
     */
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    /**
     * Sets the environment variables.
     * @param variables the environment variables
     * @since 0.5.2
     */
    public void setEnvironmentVariables(Map<String, String> variables) {
        this.environmentVariables = new LinkedHashMap<>(variables);
    }

    /**
     * Sets the gradle distribution URI.
     * @param uri the distribution URI
     * @return this
     */
    public GradleContext withGradleDistribution(URI uri) {
        this.gradleDistributionOrNull = uri;
        return this;
    }

    /**
     * Sets the gradle user home (default: {@code ~/.gradle}).
     * @param directory the target directory
     * @return this
     */
    public GradleContext withGradleUserHome(File directory) {
        this.gradleUserHomeDirOrNull = directory;
        return this;
    }

    /**
     * Sets the Java installation directory.
     * @param directory the target directory
     * @return this
     */
    public GradleContext withJavaHome(File directory) {
        this.javaHomeDirOrNull = directory;
        return this;
    }

    /**
     * Sets the standard input for build process.
     * @param stream the target stream
     * @return this
     */
    public GradleContext withStandardInput(InputStream stream) {
        this.standardInputOrNull = stream;
        return this;
    }

    /**
     * Sets the standard output for build process.
     * @param stream the target stream
     * @return this
     */
    public GradleContext withStandardOutput(OutputStream stream) {
        this.standardOutputOrNull = stream;
        return this;
    }

    /**
     * Sets the standard error output for build process.
     * @param stream the target stream
     * @return this
     */
    public GradleContext withStandardError(OutputStream stream) {
        this.standardErrorOutputOrNull = stream;
        return this;
    }

    /**
     * Sets the information output for build process.
     * @param stream the target stream
     * @return this
     * @since 0.3.3
     */
    public GradleContext withInformationOutput(PrintWriter stream) {
        this.informationOutputOrNull = stream;
        return this;
    }

    /**
     * Print information message into the current context console.
     * @param message the information message
     * @since 0.3.3
     */
    public void information(String message) {
        PrintWriter writer = informationOutputOrNull;
        if (writer != null) {
            writer.println(message);
        } else {
            LogUtil.log(IStatus.INFO, message);
        }
    }

    /**
     * Appends the Java VM arguments for build process.
     * @param arguments the arguments
     * @return this
     */
    public GradleContext withJvmArguments(List<String> arguments) {
        this.jvmArguments.addAll(arguments);
        return this;
    }

    /**
     * Appends the Java VM arguments for build process.
     * @param arguments the arguments
     * @return this
     */
    public GradleContext withJvmArguments(String... arguments) {
        Collections.addAll(this.jvmArguments, arguments);
        return this;
    }

    /**
     * Appends the build arguments for build process.
     * @param arguments the arguments
     * @return this
     */
    public GradleContext withGradleArguments(List<String> arguments) {
        this.gradleArguments.addAll(arguments);
        return this;
    }

    /**
     * Appends the build arguments for build process.
     * @param arguments the arguments
     * @return this
     */
    public GradleContext withGradleArguments(String... arguments) {
        Collections.addAll(this.gradleArguments, arguments);
        return this;
    }

    /**
     * Adds the environment variables for build process.
     * @param variables the variables
     * @return this
     * @since 0.5.2
     */
    public GradleContext withEnvironmentVariables(Map<String, String> variables) {
        environmentVariables.putAll(variables);
        return this;
    }

    /**
     * Adds an action on dispose this context.
     * @param action an action on dispose this context
     * @return this
     */
    public GradleContext withDisposeAction(IWorkspaceRunnable action) {
        this.disposeActions.add(action);
        return this;
    }
}
