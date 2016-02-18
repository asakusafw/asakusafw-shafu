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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a set of Gradle arguments.
 * @since 0.4.6
 */
public class GradleArguments {

    private static final char ESCAPE = '\\';

    private static final char SEPARATOR_CHAR = '#';

    private static final char VERSION_CHAR = '@';

    private static final String SEPARATOR = String.valueOf(SEPARATOR_CHAR);

    private final String gradleVersion;

    private final List<String> taskNames;

    private final List<String> gradleOptions;

    /**
     * Creates a new instance.
     * @param gradleVersion the special Gradle version (nullable)
     * @param taskNames the task names (includes task options)
     * @param gradleOptions the Gradle command options
     */
    public GradleArguments(String gradleVersion, List<String> taskNames, List<String> gradleOptions) {
        this.gradleVersion = gradleVersion;
        this.taskNames = taskNames;
        this.gradleOptions = gradleOptions;
    }

    /**
     * Returns the special Gradle version.
     * @return the Gradle version, or {@code null} if it is not specified
     */
    public String getGradleVersion() {
        return gradleVersion;
    }

    /**
     * Returns the task names.
     * @return the task names (includes task options)
     */
    public List<String> getTaskNames() {
        return taskNames;
    }

    /**
     * Returns the Gradle command options.
     * @return the command options
     */
    public List<String> getGradleOptions() {
        return gradleOptions;
    }

    /**
     * Parses a Shafu command line and returns the corresponding Gradle arguments.
     * @param commandLine the command line string
     * @return the parsed arguments
     */
    public static GradleArguments parse(String commandLine) {
        LinkedList<String> tokens = parseTokens(commandLine);
        String version = consumeVersion(tokens);
        List<String> options = consumeOptions(tokens);
        List<String> tasks = new ArrayList<String>(tokens);
        return new GradleArguments(version, tasks, options);
    }

    static LinkedList<String> parseTokens(String commandLine) {
        LinkedList<String> results = new LinkedList<String>();
        StringBuilder buf = new StringBuilder();
        boolean sawEscape = false;
        for (int index = 0, n = commandLine.length(); index < n; index++) {
            char c = commandLine.charAt(index);
            if (sawEscape) {
                sawEscape = false;
                buf.append(c);
            } else if (c == ESCAPE) {
                sawEscape = true;
            } else if (c == SEPARATOR_CHAR) {
                if (buf.length() != 0) {
                    results.add(buf.toString());
                    buf.setLength(0);
                }
                results.add(SEPARATOR);
            } else if (Character.isWhitespace(c)) {
                if (buf.length() != 0) {
                    results.add(buf.toString());
                    buf.setLength(0);
                }
            } else {
                buf.append(c);
            }
        }
        if (buf.length() != 0) {
            results.add(buf.toString());
        }
        return results;
    }

    private static String consumeVersion(LinkedList<String> tokens) {
        if (tokens.isEmpty()) {
            return null;
        }
        String first = tokens.getFirst();
        if (first.length() >= 2 && first.charAt(0) == VERSION_CHAR) {
            tokens.removeFirst();
            return first.substring(1);
        }
        return null;
    }

    private static List<String> consumeOptions(LinkedList<String> tokens) {
        List<String> results = new ArrayList<String>();
        boolean sawTaskName = false;
        boolean sawSeparator = false;
        for (Iterator<String> iter = tokens.iterator(); iter.hasNext();) {
            String token = iter.next();

            // tokens after "#" mean Gradle command options
            if (sawSeparator) {
                iter.remove();
                results.add(token);
                continue;
            }
            if (token.equals(SEPARATOR)) {
                sawSeparator = true;
                iter.remove();
                continue;
            }

            // if there are NO leading task names, "--*" must be a command option
            if (token.startsWith("--") && sawTaskName == false) { //$NON-NLS-1$
                iter.remove();
                results.add(token);
                continue;
            }

            // "-*" must be a command option
            if (token.startsWith("-") && token.startsWith("--") == false) { //$NON-NLS-1$ //$NON-NLS-2$
                iter.remove();
                results.add(token);
                continue;
            }

            // rest tokens are task names and their task options
            sawTaskName = true;
        }
        return results;
    }
}
