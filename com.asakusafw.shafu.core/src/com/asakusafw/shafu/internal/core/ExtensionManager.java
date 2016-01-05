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
package com.asakusafw.shafu.internal.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import com.asakusafw.shafu.core.extensions.IExtensionFilter;
import com.asakusafw.shafu.core.gradle.IGradleContextEnhancer;
import com.asakusafw.shafu.core.net.IHttpCredentialsProvider;

/**
 * Manages extension points for this plug-in.
 */
public class ExtensionManager {

    private static final String ID_EXTENSION_FILTERS =
            Activator.EXTENSION_PREFIX + "extensionFilters"; //$NON-NLS-1$

    private static final String ID_HTTP_CREDENTIALS =
            Activator.EXTENSION_PREFIX + "httpCredentials"; //$NON-NLS-1$

    private static final String ID_GRADLE_CONTEXT_ENHANCERS =
            Activator.EXTENSION_PREFIX + "gradleContextEnhancers"; //$NON-NLS-1$

    private final AtomicReference<Map<String, IExtensionFilter>> extensionFilterCache =
            new AtomicReference<Map<String, IExtensionFilter>>();

    /**
     * Detects whether the target extension is accepted or not.
     * @param extension the target extension
     * @return {@code true} if this accepts the target extension, or otherwise {@code false}
     * @throws InvalidRegistryObjectException if target extension is not valid
     */
    public boolean accepts(IExtension extension) {
        String id = extension.getUniqueIdentifier();
        if (id == null) {
            return true;
        }
        Map<String, IExtensionFilter> filters = getExtensionFilters();
        IExtensionFilter filter = filters.get(id);
        if (filter == null) {
            return true;
        }
        return filter.accept(extension);
    }

    private Map<String, IExtensionFilter> getExtensionFilters() {
        Map<String, IExtensionFilter> cached = extensionFilterCache.get();
        if (cached != null) {
            return cached;
        }
        Map<String, IExtensionFilter> results = getExtensionFilters0();
        extensionFilterCache.compareAndSet(null, Collections.unmodifiableMap(results));
        return results;
    }

    private Map<String, IExtensionFilter> getExtensionFilters0() {
        LogUtil.debug("Loading Extension: {0}", ID_EXTENSION_FILTERS); //$NON-NLS-1$
        IExtensionRegistry registory = Platform.getExtensionRegistry();
        IExtensionPoint point = registory.getExtensionPoint(ID_EXTENSION_FILTERS);
        if (point == null) {
            throw new IllegalStateException(ID_EXTENSION_FILTERS);
        }
        Map<String, IExtensionFilter> results = new HashMap<String, IExtensionFilter>();
        for (IExtension extension : point.getExtensions()) {
            for (IConfigurationElement config : extension.getConfigurationElements()) {
                String targetId = config.getAttribute("targetId"); //$NON-NLS-1$
                try {
                    Object object = config.createExecutableExtension("class"); //$NON-NLS-1$
                    if ((object instanceof IExtensionFilter) == false) {
                        throw new CoreException(new Status(
                                IStatus.ERROR,
                                Activator.PLUGIN_ID,
                                MessageFormat.format(
                                        Messages.ExtensionManager_errorNotSubtype,
                                        ID_EXTENSION_FILTERS,
                                        IExtensionFilter.class.getName(),
                                        extension.getContributor().getName())));
                    }
                    IExtensionFilter filter = results.get(targetId);
                    if (filter == null) {
                        filter = (IExtensionFilter) object;
                    } else {
                        filter = CompositeExtensionFilter.composite(Arrays.asList(filter, (IExtensionFilter) object));
                    }
                    results.put(targetId, filter);
                } catch (CoreException e) {
                    LogUtil.log(e.getStatus());
                }
            }
        }
        return results;
    }

    /**
     * Returns new {@link IHttpCredentialsProvider}s.
     * @return the created instances
     */
    public Collection<IHttpCredentialsProvider> createHttpCredentialsProvider() {
        return getClasses(IHttpCredentialsProvider.class, ID_HTTP_CREDENTIALS);
    }

    /**
     * Returns new {@link IGradleContextEnhancer}s.
     * @return the created instances
     */
    public List<IGradleContextEnhancer> createGradleContextEnhancers() {
        List<IGradleContextEnhancer> results = new ArrayList<IGradleContextEnhancer>();
        for (IGradleContextEnhancer provider : getClasses(IGradleContextEnhancer.class, ID_GRADLE_CONTEXT_ENHANCERS)) {
            results.add(provider);
        }
        return results;
    }

    private <T> Collection<T> getClasses(Class<T> type, String pointId) {
        LogUtil.debug("Loading Extension: {0}", pointId); //$NON-NLS-1$
        IExtensionRegistry registory = Platform.getExtensionRegistry();
        IExtensionPoint point = registory.getExtensionPoint(pointId);
        if (point == null) {
            throw new IllegalStateException(pointId);
        }
        Collection<T> results = new ArrayList<T>();
        for (IExtension extension : point.getExtensions()) {
            if (accepts(extension) == false) {
                LogUtil.debug("Extension is filtered: {0}", extension.getUniqueIdentifier()); //$NON-NLS-1$
                continue;
            }
            for (IConfigurationElement config : extension.getConfigurationElements()) {
                try {
                    Object object = config.createExecutableExtension("class"); //$NON-NLS-1$
                    if (type.isInstance(object) == false) {
                        throw new CoreException(new Status(
                                IStatus.ERROR,
                                Activator.PLUGIN_ID,
                                MessageFormat.format(
                                        Messages.ExtensionManager_errorNotSubtype,
                                        pointId,
                                        type.getName(),
                                        extension.getContributor().getName())));
                    }
                    results.add(type.cast(object));
                } catch (CoreException e) {
                    LogUtil.log(e.getStatus());
                }
            }
        }
        return results;
    }
}
