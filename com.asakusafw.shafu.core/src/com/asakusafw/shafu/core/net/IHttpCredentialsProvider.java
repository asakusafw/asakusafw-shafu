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
package com.asakusafw.shafu.core.net;

import org.eclipse.core.runtime.CoreException;

/**
 * Provides {@link IHttpCredentials} for the target.
 */
public interface IHttpCredentialsProvider {

    /**
     * Provides {@link IHttpCredentials} for the target host name.
     * @param scope the target scope
     * @return the related credentials, or {@code null} if this provider does not support the target host name
     * @throws CoreException if failed to detect credentials for the host name
     */
    IHttpCredentials find(Scope scope) throws CoreException;

    /**
     * The target scope for providing Credentials.
     */
    public static class Scope {

        private final String scheme;

        private final String hostName;

        private final int port;

        private final String realm;

        /**
         * Creates a new instance.
         * @param scheme the target scheme name, or {@code null} to be don't care
         * @param hostName the target host name
         * @param port the target port number, or {@code -1} to be don't care
         * @param realm the target realm, or {@code null} to be don't care
         */
        public Scope(String scheme, String hostName, int port, String realm) {
            if (hostName == null) {
                throw new IllegalArgumentException();
            }
            this.scheme = scheme;
            this.hostName = hostName;
            this.port = port;
            this.realm = realm;
        }

        /**
         * Returns the target host name.
         * @return the host name
         */
        public String getHostName() {
            return hostName;
        }

        /**
         * Returns the target scheme name.
         * @return the scheme name, or {@code null}
         */
        public String getScheme() {
            return scheme;
        }

        /**
         * Returns the target port number.
         * @return the port number, or {@code -1}
         */
        public int getPort() {
            return port;
        }

        /**
         * Returns the target realm.
         * @return the realm, or {@code null}
         */
        public String getRealm() {
            return realm;
        }
    }
}
