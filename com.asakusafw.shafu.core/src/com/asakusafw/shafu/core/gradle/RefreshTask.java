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
package com.asakusafw.shafu.core.gradle;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.asakusafw.shafu.core.util.IRunnable;

/**
 * Refreshes the resources.
 */
public class RefreshTask implements IRunnable {

    private final List<IResource> targets;

    /**
     * Creates a new instance.
     * @param targets the target resources
     */
    public RefreshTask(List<IResource> targets) {
        this.targets = targets;
    }

    /**
     * Creates a new instance.
     * @param targets the target resources
     */
    public RefreshTask(IResource... targets) {
        this.targets = Arrays.asList(targets);
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        if (targets.isEmpty()) {
            return;
        }
        monitor.beginTask(Messages.RefreshTask_monitor, targets.size());
        try {
            for (IResource resource : targets) {
                resource.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 1));
            }
        } finally {
            monitor.done();
        }
    }
}
