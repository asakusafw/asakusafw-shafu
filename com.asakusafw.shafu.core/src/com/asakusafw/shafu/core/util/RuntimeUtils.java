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

import java.io.File;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;

import com.asakusafw.shafu.internal.core.LogUtil;

/**
 * Utilities for current runtime.
 * @since 0.4.0
 */
public final class RuntimeUtils {

    private static final Pattern JRE_FILE_PATTERN = Pattern.compile("jre[0-9_\\-\\.]*"); //$NON-NLS-1$

    private RuntimeUtils() {
        return;
    }

    /**
     * Returns the workspace Java home path.
     * @return the workspace Java home path, or {@code null} if it is not defined
     */
    public static File getJavaHome() {
        IVMInstall install = JavaRuntime.getDefaultVMInstall();
        if (install == null) {
            return null;
        }
        return toJavaHome(install);
    }

    /**
     * Returns the project Java home path.
     * @param project the target project
     * @return the project Java home path, or {@code null} if it is not defined
     */
    public static File getJavaHome(IProject project) {
        if (isJavaProject(project) == false) {
            return getJavaHome();
        }
        IJavaProject javaProject = JavaCore.create(project);
        try {
            IVMInstall install = JavaRuntime.getVMInstall(javaProject);
            if (install == null) {
                return null;
            }
            return toJavaHome(install);
        } catch (CoreException e) {
            LogUtil.log(e.getStatus());
            return null;
        }
    }

    private static boolean isJavaProject(IProject project) {
        if (project == null || project.isOpen() == false) {
            return false;
        }
        try {
            return project.hasNature(JavaCore.NATURE_ID);
        } catch (CoreException e) {
            LogUtil.log(e.getStatus());
        }
        return false;
    }

    private static File toJavaHome(IVMInstall install) {
        return install.getInstallLocation();
    }

    /**
     * Returns whether the target directory may be JDK installation path.
     * @param javaHome the target directory
     * @return {@code true} if the target path may have JDK installation, or {@code false} otherwise
     */
    public static boolean isJavaDevelopmentKitLike(File javaHome) {
        if (javaHome == null || javaHome.exists() == false) {
            return false;
        }
        String name = javaHome.getName();
        if (JRE_FILE_PATTERN.matcher(name).matches()) {
            return false;
        }
        return true;
    }
}
