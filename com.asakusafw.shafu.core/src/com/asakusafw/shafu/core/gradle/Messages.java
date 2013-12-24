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
package com.asakusafw.shafu.core.gradle;

import org.eclipse.osgi.util.NLS;

final class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.asakusafw.shafu.core.gradle.messages"; //$NON-NLS-1$
    public static String GradleBuildTask_errorFailedToBuildProject;
    public static String GradleBuildTask_monitorBuild;
    public static String GradleBuildTask_monitorConnect;
    public static String GradleBuildTask_monitorDisconnect;
    public static String GradleBuildTask_monitorRun;
    public static String GradleUtil_monitorDispose;
    public static String GradleUtil_monitorEnhance;
    public static String GradleInspectTask_errorFailedToInspectProject;
    public static String GradleInspectTask_monitorConnect;
    public static String GradleInspectTask_monitorDisconnect;
    public static String GradleInspectTask_monitorInspect;
    public static String GradleInspectTask_monitorRun;
    public static String RefreshTask_monitor;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        return;
    }
}
