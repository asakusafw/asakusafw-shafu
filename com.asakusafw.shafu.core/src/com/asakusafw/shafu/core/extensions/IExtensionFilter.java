/**
 * Copyright 2013-2021 Asakusa Framework Team.
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
package com.asakusafw.shafu.core.extensions;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.InvalidRegistryObjectException;

/**
 * Filters extensions in Shafu.
 */
public interface IExtensionFilter {

    /**
     * An instance which accepts any extensions.
     */
    IExtensionFilter ANY = new IExtensionFilter() {
        @Override
        public boolean accept(IExtension extension) {
            return true;
        }
    };

    /**
     * Detects whether the target extension is accepted or not.
     * @param extension the target extension
     * @return {@code true} if this accepts the target extension, or otherwise {@code false}
     * @throws InvalidRegistryObjectException if target extension is not valid
     */
    boolean accept(IExtension extension);
}
