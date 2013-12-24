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
package com.asakusafw.shafu.internal.core.net;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.SystemDefaultCredentialsProvider;
import org.eclipse.core.runtime.CoreException;

import com.asakusafw.shafu.core.net.IHttpCredentials;
import com.asakusafw.shafu.core.net.IHttpCredentialsProvider;
import com.asakusafw.shafu.internal.core.Activator;
import com.asakusafw.shafu.internal.core.LogUtil;

/**
 * A credentials provider for Shafu networking.
 */
public class ShafuCredentialsProvider extends SystemDefaultCredentialsProvider {

    @Override
    public Credentials getCredentials(AuthScope authscope) {
        String host = authscope.getHost();
        if (host != AuthScope.ANY_HOST) {
            for (IHttpCredentialsProvider provider : Activator.getExtensions().createHttpCredentialsProvider()) {
                try {
                    IHttpCredentials creds = provider.find(host);
                    if (creds != null) {
                        return new UsernamePasswordCredentials(creds.getUserName(), creds.getPassword());
                    }
                } catch (CoreException e) {
                    LogUtil.log(e.getStatus());
                }
            }
        }
        return super.getCredentials(authscope);
    }
}
