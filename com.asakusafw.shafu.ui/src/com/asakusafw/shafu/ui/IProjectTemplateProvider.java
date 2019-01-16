/**
 * Copyright 2013-2019 Asakusa Framework Team.
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
package com.asakusafw.shafu.ui;

import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An abstract super interface of providing project templates.
 */
public interface IProjectTemplateProvider {

    /**
     * Returns project templates.
     * @param monitor the current monitor
     * @return the map of project template description and its location
     * @throws CoreException if failed to provide templates
     */
    Map<String, URL> getProjectTemplates(IProgressMonitor monitor) throws CoreException;
}
