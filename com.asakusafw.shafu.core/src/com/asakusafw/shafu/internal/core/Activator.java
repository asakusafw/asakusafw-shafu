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
package com.asakusafw.shafu.internal.core;

import java.io.Closeable;
import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.asakusafw.shafu.internal.core.net.ShafuCredentialsProvider;

/**
 * The activator for this plug-in.
 */
public class Activator extends Plugin {

    /**
     * The plug-in ID.
     */
    public static final String PLUGIN_ID = "com.asakusafw.shafu.core"; //$NON-NLS-1$

    /**
     * The prefix of extension point IDs in this plug-in.
     */
    public static final String EXTENSION_PREFIX = PLUGIN_ID + '.';

    private static Activator plugin;

    private ExtensionManager extensions;

    private CloseableHttpClient httpClient;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        extensions = new ExtensionManager();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        extensions = null;
        synchronized (this) {
            closeQuietly(httpClient);
            httpClient = null;
        }
        plugin = null;
        super.stop(context);
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the shared instance.
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns the extension points manager.
     * @return the extension points manager
     */
    public static ExtensionManager getExtensions() {
        return getDefault().getExtensions0();
    }

    private synchronized ExtensionManager getExtensions0() {
        return extensions;
    }

    /**
     * Returns a HTTP client.
     * @return HTTP client
     */
    public static HttpClient getHttpClient() {
        return getDefault().getHttpClient0();
    }

    private synchronized HttpClient getHttpClient0() {
        if (httpClient == null) {
            httpClient = HttpClientBuilder.create()
                    .useSystemProperties()
                    .setDefaultCredentialsProvider(new ShafuCredentialsProvider())
                    .build();
        }
        return httpClient;
    }
}
