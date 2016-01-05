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
package com.asakusafw.shafu.internal.asakusafw.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * Utilities about Asakusa Framework specific information.
 * @since 0.4.0
 */
public final class AsakusaFrameworkInfo {

    static final String ENV_ASAKUSA_HOME = "ASAKUSA_HOME"; //$NON-NLS-1$

    static final String ENV_HADOOP_COMMAND = "HADOOP_CMD"; //$NON-NLS-1$

    static final String ENV_HADOOP_HOME = "HADOOP_HOME"; //$NON-NLS-1$

    static final String ENV_PATH = "PATH"; //$NON-NLS-1$

    static final String NAME_HADOOP_COMMAND = "hadoop"; //$NON-NLS-1$

    static final IPath HOME_RELATIVE_HADOOP_COMMAND = Path.fromPortableString("bin") //$NON-NLS-1$
            .append(NAME_HADOOP_COMMAND);

    private static final String ENV_WINDOWS_PATH_EXTENSIONS = "PATHEXT"; //$NON-NLS-1$

    private static final String[] COMMAND_EXTENSIONS;
    static {
        List<String> extensions = new ArrayList<String>();
        String os = Platform.getOS();
        if (os != null && os.equals(Platform.OS_WIN32)) {
            String extString = System.getenv(ENV_WINDOWS_PATH_EXTENSIONS);
            if (extString != null) {
                for (String ext : extString.split(Pattern.quote(File.pathSeparator))) {
                    if (ext.startsWith(".")) { //$NON-NLS-1$
                        ext = ext.substring(1);
                    }
                    if (ext.isEmpty()) {
                        continue;
                    }
                    extensions.add(ext.toLowerCase(Locale.ENGLISH));
                }
            }
        }
        COMMAND_EXTENSIONS = extensions.toArray(new String[extensions.size()]);
    }

    private AsakusaFrameworkInfo() {
        return;
    }

    /**
     * Returns the Asakusa Framework installation path.
     * @return the installation path, or {@code null} if it is not specified
     */
    public static File findInstallation() {
        return findInstallation(System.getenv());
    }

    static File findInstallation(Map<String, String> env) {
        String value = env.get(ENV_ASAKUSA_HOME);
        if (value == null) {
            return null;
        }
        return new File(value);
    }

    /**
     * Returns the {@code "hadoop"} command path.
     * @return the command path, or {@code null} if it is not specified
     */
    public static File findHadoopCommand() {
        return findHadoopCommand(System.getenv());
    }

    static File findHadoopCommand(Map<String, String> env) {
        String cmdValue = env.get(ENV_HADOOP_COMMAND);
        if (cmdValue != null && cmdValue.isEmpty() == false) {
            return new File(cmdValue);
        }
        String homeValue = env.get(ENV_HADOOP_HOME);
        if (homeValue != null && homeValue.isEmpty() == false) {
            return Path.fromOSString(homeValue).append(HOME_RELATIVE_HADOOP_COMMAND).toFile();
        }
        String pathValue = env.get(ENV_PATH);
        if (pathValue != null && pathValue.isEmpty() == false) {
            for (String s : pathValue.split(Pattern.quote(File.pathSeparator))) {
                if (s.isEmpty()) {
                    continue;
                }
                IPath path = Path.fromOSString(s).append(NAME_HADOOP_COMMAND);
                if (COMMAND_EXTENSIONS.length == 0) {
                    File file = path.toFile();
                    if (file.exists()) {
                        return file;
                    }
                } else {
                    for (String extension : COMMAND_EXTENSIONS) {
                        File file = path.addFileExtension(extension).toFile();
                        if (file.exists()) {
                            return file;
                        }
                    }
                }
            }
        }
        return null;
    }
}
