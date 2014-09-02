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
package com.asakusafw.shafu.internal.asakusafw.gradle;

import org.eclipse.osgi.util.NLS;

final class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.asakusafw.shafu.internal.asakusafw.gradle.messages"; //$NON-NLS-1$
    public static String AsakusaFrameworkGradleContextEnhancer_errorExtractExtraScript;
    public static String AsakusaFrameworkGradleContextEnhancer_monitorEnhance;
    public static String AsakusaFrameworkTemplateProvider_errorCatalogEntryRecordInvalid;
    public static String AsakusaFrameworkTemplateProvider_errorCatalogEntryUrlInvalid;
    public static String AsakusaFrameworkTemplateProvider_errorCatalogUrlInvalid;
    public static String AsakusaFrameworkTemplateProvider_errorFailedToDownloadCatalog;
    public static String AsakusaFrameworkTemplateProvider_monitorFetchCatalog;
    public static String AsakusaFrameworkTemplateProvider_monitorGetCatalogLocation;
    public static String AsakusaFrameworkTemplateProvider_monitorGetProjectTemplates;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        return;
    }
}
