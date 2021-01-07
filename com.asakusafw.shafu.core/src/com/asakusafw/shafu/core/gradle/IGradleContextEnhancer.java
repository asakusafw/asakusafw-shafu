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
package com.asakusafw.shafu.core.gradle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Enhances the {@link GradleContext}.
 */
public interface IGradleContextEnhancer {

    /**
     * Enhances the {@link GradleContext}.
     * @param monitor the current progress monitor
     * @param context the target context
     * @throws CoreException if failed to enhance the target context
     */
    void enhance(IProgressMonitor monitor, GradleContext context) throws CoreException;
}
