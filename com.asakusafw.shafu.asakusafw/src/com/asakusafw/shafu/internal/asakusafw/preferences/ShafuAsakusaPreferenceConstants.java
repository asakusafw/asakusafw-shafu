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
package com.asakusafw.shafu.internal.asakusafw.preferences;

/**
 * Preference constants of Shafu Asakusa Plug-in.
 * @since 0.1.0
 * @version 0.5.3
 */
public final class ShafuAsakusaPreferenceConstants {

    /**
     * The URL (prefix) of download site.
     */
    private static final String URL_DOWNLOAD_SITE = "http://www.asakusafw.com/download/gradle-plugin/"; //$NON-NLS-1$

    /**
     * The preference key of catalog URL.
     */
    public static final String KEY_CATALOG_URL = "catalog"; //$NON-NLS-1$

    /**
     * The default value of catalog URL.
     */
    public static final String DEFAULT_CATALOG_URL = URL_DOWNLOAD_SITE + "template-catalog-release.txt"; //$NON-NLS-1$

    private ShafuAsakusaPreferenceConstants() {
        return;
    }
}
