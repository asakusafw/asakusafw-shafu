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
package com.asakusafw.shafu.core.net;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.OperationCanceledException;

/**
 * An abstract super interface of processes contents.
 * @param <T> the result type
 */
public interface IContentProcessor<T> {

    /**
     * Processes the content.
     * @param input the process target
     * @return the process results
     * @throws IOException if failed to process the content
     * @throws OperationCanceledException if the operation was canceled
     */
    T process(InputStream input) throws IOException;
}
