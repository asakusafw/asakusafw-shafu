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
package com.asakusafw.shafu.internal.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import com.asakusafw.shafu.core.gradle.IGradleContextEnhancer;
import com.asakusafw.shafu.core.net.IHttpCredentialsProvider;

/**
 * Manages extension points for this plug-in.
 */
public class ExtensionManager {

    private static final String ID_HTTP_CREDENTIALS =
            Activator.EXTENSION_PREFIX + "httpCredentials"; //$NON-NLS-1$

    private static final String ID_GRADLE_CONTEXT_ENHANCERS =
            Activator.EXTENSION_PREFIX + "gradleContextEnhancers"; //$NON-NLS-1$

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

    private static <T> Collection<T> getClasses(Class<T> type, String pointId) {
        LogUtil.debug("Loading Extension: {0}", pointId); //$NON-NLS-1$
        IExtensionRegistry registory = Platform.getExtensionRegistry();
        IExtensionPoint point = registory.getExtensionPoint(pointId);
        if (point == null) {
            throw new IllegalStateException(pointId);
        }
        Collection<T> results = new ArrayList<T>();
        for (IExtension extension : point.getExtensions()) {
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
