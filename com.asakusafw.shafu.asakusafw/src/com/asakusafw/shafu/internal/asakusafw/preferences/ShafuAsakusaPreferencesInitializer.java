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
package com.asakusafw.shafu.internal.asakusafw.preferences;

import static com.asakusafw.shafu.internal.asakusafw.preferences.ShafuAsakusaPreferenceConstants.*;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.asakusafw.shafu.internal.asakusafw.Activator;
import com.asakusafw.shafu.internal.asakusafw.LogUtil;

/**
 * Preferences initializer for Shafu Asakusa Plug-in.
 */
public class ShafuAsakusaPreferencesInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        LogUtil.debug("Initializing Preferences"); //$NON-NLS-1$
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        prefs.setDefault(KEY_CATALOG_URL, DEFAULT_CATALOG_URL);
    }
}
