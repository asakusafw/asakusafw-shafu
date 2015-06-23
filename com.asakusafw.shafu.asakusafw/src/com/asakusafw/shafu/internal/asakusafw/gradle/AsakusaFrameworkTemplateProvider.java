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
package com.asakusafw.shafu.internal.asakusafw.gradle;

import static com.asakusafw.shafu.internal.asakusafw.preferences.ShafuAsakusaPreferenceConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

import com.asakusafw.shafu.core.net.IContentProcessor;
import com.asakusafw.shafu.core.net.ShafuNetwork;
import com.asakusafw.shafu.core.util.StatusUtils;
import com.asakusafw.shafu.internal.asakusafw.Activator;
import com.asakusafw.shafu.internal.asakusafw.LogUtil;
import com.asakusafw.shafu.ui.IProjectTemplateProvider;

/**
 * Provides project templates for Asakusa batch application projects.
 */
public class AsakusaFrameworkTemplateProvider implements IProjectTemplateProvider {

    private static final char FIELD_SEPARATOR = '|';

    static final Charset ENCODING = Charset.forName("UTF-8"); //$NON-NLS-1$

    @Override
    public Map<String, URL> getProjectTemplates(IProgressMonitor monitor) throws CoreException {
        SubMonitor sub = SubMonitor.convert(monitor, Messages.AsakusaFrameworkTemplateProvider_monitorGetProjectTemplates, 100);
        try {
            URL catalogLocation = getCatalogLocation(sub.newChild(10));
            if (catalogLocation == null) {
                return Collections.emptyMap();
            }
            Map<String, URL> results = fetchCatalog(sub.newChild(90), catalogLocation);
            return results;
        } catch (CoreException e) {
            StatusUtils.rethrowIfCancel(e);
            LogUtil.log(e.getStatus());
            return Collections.emptyMap();
        } finally {
            monitor.done();
        }
    }

    private URL getCatalogLocation(SubMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.AsakusaFrameworkTemplateProvider_monitorGetCatalogLocation, 10);
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        String location = prefs.getString(KEY_CATALOG_URL);
        if (location == null || location.trim().isEmpty()) {
            // catalog is disabled
            return null;
        }
        try {
            return new URL(location);
        } catch (MalformedURLException e) {
            throw new CoreException(new Status(
                    IStatus.WARNING,
                    Activator.PLUGIN_ID,
                    MessageFormat.format(
                            Messages.AsakusaFrameworkTemplateProvider_errorCatalogUrlInvalid,
                            location),
                    e));
        }
    }

    private Map<String, URL> fetchCatalog(final SubMonitor monitor, final URL catalog) throws CoreException {
        monitor.beginTask(Messages.AsakusaFrameworkTemplateProvider_monitorFetchCatalog, 100);
        try {
            return ShafuNetwork.processContent(catalog, new IContentProcessor<Map<String, URL>>() {
                @Override
                public Map<String, URL> process(InputStream input) throws IOException {
                    Map<String, URL> results = new TreeMap<String, URL>();
                    Scanner scanner = new Scanner(new InputStreamReader(input, ENCODING));
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine().trim();
                        if (line.isEmpty()) {
                            continue;
                        }
                        parseLine(catalog, line, results);
                    }
                    scanner.close();
                    return results;
                }
            });
        } catch (IOException e) {
            throw new CoreException(new Status(
                    IStatus.WARNING,
                    Activator.PLUGIN_ID,
                    MessageFormat.format(
                            Messages.AsakusaFrameworkTemplateProvider_errorFailedToDownloadCatalog,
                            catalog),
                    e));
        }
    }

    static void parseLine(URL catalog, String line, Map<String, URL> results) {
        int index = line.lastIndexOf(FIELD_SEPARATOR);
        if (index < 0) {
            LogUtil.log(new Status(
                    IStatus.WARNING,
                    Activator.PLUGIN_ID,
                    MessageFormat.format(
                            Messages.AsakusaFrameworkTemplateProvider_errorCatalogEntryRecordInvalid,
                            catalog,
                            line)));
            return;
        }
        String name = line.substring(0, index);
        String location = line.substring(index + 1);
        try {
            URL url = new URL(catalog, location);
            results.put(name, url);
        } catch (MalformedURLException e) {
            LogUtil.log(new Status(
                    IStatus.WARNING,
                    Activator.PLUGIN_ID,
                    MessageFormat.format(
                            Messages.AsakusaFrameworkTemplateProvider_errorCatalogEntryUrlInvalid,
                            catalog,
                            location)));
        }
    }
}
