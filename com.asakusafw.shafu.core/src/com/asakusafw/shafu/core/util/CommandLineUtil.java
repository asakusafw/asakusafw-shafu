/**
 * Copyright 2014 Asakusa Framework Team.
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
import java.util.Collections;
import java.util.List;

/**
 * Utilities for command lines.
 * @since 0.2.4
 */
public final class CommandLineUtil {

    private static final char ESCAPE = '\\';

    private static final char SEPARATOR_CHAR = '#';

    private static final String SEPARATOR = String.valueOf(SEPARATOR_CHAR);

    /**
     * Extracts and returns the Gradle tasks names from the command line string.
     * @param commandLine the command line string
     * @return the parsed tokens
     */
    public static List<String> parseGradleTaskNames(String commandLine) {
        List<String> tokens = parseCommandLineTokens(commandLine);
        int separatorAt = findSeparator(tokens);
        if (separatorAt < 0) {
            return tokens;
        } else {
            return tokens.subList(0, separatorAt);
        }
    }

    /**
     * Extracts and returns the Gradle build arguments from the command line string.
     * @param commandLine the command line string
     * @return the parsed tokens
     */
    public static List<String> parseGradleBuildArguments(String commandLine) {
        List<String> tokens = parseCommandLineTokens(commandLine);
        int separatorAt = findSeparator(tokens);
        if (separatorAt < 0) {
            return Collections.emptyList();
        } else {
            return tokens.subList(separatorAt + 1, tokens.size());
        }
    }

    /**
     * Parses the command line.
     * @param commandLine the command line
     * @return the parsed tokens
     */
    public static List<String> parseCommandLineTokens(String commandLine) {
        List<String> results = new ArrayList<String>();
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

    private static int findSeparator(List<String> tokens) {
        return tokens.indexOf(SEPARATOR);
    }
}
