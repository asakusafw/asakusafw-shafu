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
package com.asakusafw.shafu.core.net;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import com.asakusafw.shafu.internal.core.Activator;

/**
 * Shafu Network API.
 */
public final class ShafuNetwork {

    private static final Set<String> HTTP_SCHEMES;
    static {
        Set<String> set = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        set.add("http"); //$NON-NLS-1$
        set.add("https"); //$NON-NLS-1$
        HTTP_SCHEMES = Collections.unmodifiableSet(set);
    }

    private ShafuNetwork() {
        return;
    }

    /**
     * Processes a content on the target URL.
     * @param url the target URL
     * @param processor the content processor
     * @param <T> the processing result type
     * @return the process result
     * @throws IOException if failed to process the content
     */
    public static <T> T processContent(URL url, IContentProcessor<? extends T> processor) throws IOException {
        String protocol = url.getProtocol();
        if (protocol != null && HTTP_SCHEMES.contains(protocol)) {
            return processHttpContent(url, processor);
        }
        InputStream input;
        try {
            input = url.openStream();
        } catch (IOException e) {
            throw new IOException(MessageFormat.format(
                    Messages.ShafuNetwork_failedToOpenContent,
                    url), e);
        }
        try {
            return processor.process(input);
        } finally {
            input.close();
        }
    }

    private static <T> T processHttpContent(URL url, IContentProcessor<T> processor) throws IOException {
        HttpClient client = Activator.getHttpClient();
        HttpGet request = new HttpGet(url.toExternalForm());
        HttpResponse response = client.execute(request);
        try {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return processor.process(response.getEntity().getContent());
            } else {
                throw new IOException(MessageFormat.format(
                        Messages.ShafuNetwork_failedToOpenHttpContent,
                        request.getURI(),
                        response.getStatusLine()));
            }
        } finally {
            closeQuietly(response);
        }
    }

    private static void closeQuietly(Object content) {
        if (content instanceof Closeable) {
            try {
                ((Closeable) content).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
