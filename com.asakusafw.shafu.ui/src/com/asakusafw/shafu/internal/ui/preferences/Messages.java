/**
 * Copyright 2013-2018 Asakusa Framework Team.
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

import org.eclipse.osgi.util.NLS;

final class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.asakusafw.shafu.internal.ui.preferences.messages"; //$NON-NLS-1$
    public static String GradleLogLevel_debugDescription;
    public static String GradleLogLevel_infoDescription;
    public static String GradleLogLevel_lifecycleDescription;
    public static String GradleLogLevel_quietDescription;
    public static String GradleNetworkMode_offlineDescription;
    public static String GradleNetworkMode_onlineDescription;
    public static String GradleStackTrace_allDescription;
    public static String GradleStackTrace_neverDescription;
    public static String GradleStackTrace_userDescription;
    public static String ShafuPreferencePage_buttonDirectorySelection;
    public static String ShafuPreferencePage_dialogDirectorySelection;
    public static String ShafuPreferencePage_errorDirectoryEmpty;
    public static String ShafuPreferencePage_errorDirectoryMissing;
    public static String ShafuPreferencePage_groupEnvironment;
    public static String ShafuPreferencePage_groupLogging;
    public static String ShafuPreferencePage_groupWrapper;
    public static String ShafuPreferencePage_hintOptionalText;
    public static String ShafuPreferencePage_hintWrapperConfigurationPaths;
    public static String ShafuPreferencePage_itemEnvironmentVariables;
    public static String ShafuPreferencePage_itemGradleUserHome;
    public static String ShafuPreferencePage_itemGradleVersion;
    public static String ShafuPreferencePage_itemJavaHome;
    public static String ShafuPreferencePage_itemLogLevel;
    public static String ShafuPreferencePage_itemNetworkMode;
    public static String ShafuPreferencePage_itemProjectProperties;
    public static String ShafuPreferencePage_itemStackTrace;
    public static String ShafuPreferencePage_itemSystemProperties;
    public static String ShafuPreferencePage_itemUseProjectJavaHome;
    public static String ShafuPreferencePage_itemUseWrapperConfiguration;
    public static String ShafuPreferencePage_itemWrapperConfigurationPaths;
    public static String ShafuPreferencePage_propertiesAddLabel;
    public static String ShafuPreferencePage_propertiesKeyLabel;
    public static String ShafuPreferencePage_propertiesRemoveLabel;
    public static String ShafuPreferencePage_propertiesValueLabel;
    public static String ShafuPreferencePage_tabBasic;
    public static String ShafuPreferencePage_tabJvm;
    public static String ShafuPreferencePage_tabProject;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        return;
    }
}
