/**
 * Copyright 2013-2015 Asakusa Framework Team.
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

import static com.asakusafw.shafu.internal.ui.preferences.ShafuPreferenceConstants.*;
import static com.asakusafw.shafu.ui.util.PreferenceUtils.*;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.asakusafw.shafu.internal.ui.Activator;
import com.asakusafw.shafu.internal.ui.LogUtil;

/**
 * Initializes the plug-in preferences.
 */
public class ShafuPreferencesInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        LogUtil.debug("Initializing Preferences"); //$NON-NLS-1$
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        prefs.setDefault(KEY_LOG_LEVEL, DEFAULT_LOG_LEVEL.getSymbol());
        prefs.setDefault(KEY_STACK_TRACE, DEFAULT_STACK_TRACE.getSymbol());
        prefs.setDefault(KEY_NETWORK_MODE, DEFAULT_NETWORK_MODE.getSymbol());
        prefs.setDefault(KEY_PROJECT_PROPERTIES, encodeMap(DEFAULT_PROJECT_PROPERTIES));
        prefs.setDefault(KEY_SYSTEM_PROPERTIES, encodeMap(DEFAULT_SYSTEM_PROPERTIES));
        prefs.setDefault(KEY_GRADLE_USER_HOME, encodeFile(DEFAULT_GRADLE_USER_HOME));
        prefs.setDefault(KEY_JAVA_HOME, encodeFile(DEFAULT_JAVA_HOME));
        prefs.setDefault(KEY_GRADLE_VERSION, encodeVersion(DEFAULT_GRADLE_VERSION));
        prefs.setDefault(KEY_USE_HTTPS, DEFAULT_USE_HTTPS);
        prefs.setDefault(KEY_GRADLE_DISTRIBUTION, encodeUri(DEFAULT_GRADLE_DISTRIBUTION));
    }
}
