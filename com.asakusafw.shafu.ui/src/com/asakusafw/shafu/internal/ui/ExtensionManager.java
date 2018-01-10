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
package com.asakusafw.shafu.internal.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import com.asakusafw.shafu.core.extensions.ExtensionFilters;
import com.asakusafw.shafu.ui.IProjectTemplateProvider;

/**
 * Manages extension points for this plug-in.
 */
public class ExtensionManager {

    private static final String ID_PROJECT_TEMPLATES =
            Activator.EXTENSION_PREFIX + "projectTemplates"; //$NON-NLS-1$

    /**
     * Returns the project templates.
     * @return the project templates
     */
    public List<IProjectTemplateProvider> getProjectTemplateProviders() {
        LogUtil.debug("Loading Extension: {0}", ID_PROJECT_TEMPLATES); //$NON-NLS-1$
        IExtensionRegistry registory = Platform.getExtensionRegistry();
        IExtensionPoint point = registory.getExtensionPoint(ID_PROJECT_TEMPLATES);
        if (point == null) {
            throw new IllegalStateException(ID_PROJECT_TEMPLATES);
        }
        List<IProjectTemplateProvider> results = new ArrayList<>();
        final Map<String, URL> templates = new TreeMap<>();
        for (IExtension extension : point.getExtensions()) {
            if (ExtensionFilters.accepts(extension) == false) {
                LogUtil.debug("Extension is filtered: {0}", extension.getUniqueIdentifier()); //$NON-NLS-1$
                continue;
            }
            for (IConfigurationElement config : extension.getConfigurationElements()) {
                String name = config.getName();
                if (name.equals("template")) { //$NON-NLS-1$
                    extractTemplate(config, templates);
                } else if (name.equals("provider")) { //$NON-NLS-1$
                    IProjectTemplateProvider executable = getExecutable(IProjectTemplateProvider.class, config);
                    if (executable != null) {
                        results.add(executable);
                    }
                }
            }
        }
        if (templates.isEmpty() == false) {
            results.add(new IProjectTemplateProvider() {
                @Override
                public Map<String, URL> getProjectTemplates(IProgressMonitor monitor) {
                    monitor.beginTask("Resolving templates", 1); //$NON-NLS-1$
                    try {
                        return templates;
                    } finally {
                        monitor.done();
                    }
                }
            });
        }
        return results;
    }

    private static void extractTemplate(IConfigurationElement element, Map<String, URL> target) {
        String location = element.getAttribute("location"); //$NON-NLS-1$
        String description = element.getAttribute("description"); //$NON-NLS-1$
        if (description == null) {
            description = location;
        }
        try {
            URL url = new URL(location);
            target.put(description, url);
        } catch (MalformedURLException e) {
            LogUtil.log(new Status(
                    IStatus.WARNING,
                    Activator.PLUGIN_ID,
                    MessageFormat.format(
                            Messages.ExtensionManager_errorInvalidUrl,
                            element.getDeclaringExtension().getExtensionPointUniqueIdentifier(),
                            location,
                            element.getDeclaringExtension().getContributor().getName()),
                    e));
        }
    }

    private static <T> T getExecutable(Class<T> type, IConfigurationElement element) {
        try {
            Object object = element.createExecutableExtension("class"); //$NON-NLS-1$
            if (type.isInstance(object) == false) {
                throw new CoreException(new Status(
                        IStatus.ERROR,
                        Activator.PLUGIN_ID,
                        MessageFormat.format(
                                Messages.ExtensionManager_errorNotSubtype,
                                element.getDeclaringExtension().getExtensionPointUniqueIdentifier(),
                                type.getName(),
                                element.getContributor().getName())));
            }
            return type.cast(object);
        } catch (CoreException e) {
            LogUtil.log(e.getStatus());
            return null;
        }
    }
}
