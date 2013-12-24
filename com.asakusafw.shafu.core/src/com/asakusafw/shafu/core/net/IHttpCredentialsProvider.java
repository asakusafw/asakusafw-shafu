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
package com.asakusafw.shafu.core.net;

import org.eclipse.core.runtime.CoreException;

/**
 * Provides {@link IHttpCredentials} for the target.
 */
public interface IHttpCredentialsProvider {

    /**
     * Provides {@link IHttpCredentials} for the target host name.
     * @param hostName the target host name
     * @return the related credentials, or {@code null} if this provider does not support the target host name
     * @throws CoreException if failed to detect credentials for the host name
     */
    IHttpCredentials find(String hostName) throws CoreException;
}
