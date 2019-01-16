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
package com.asakusafw.shafu.core.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Builds {@link IRunnable}.
 */
public class RunnableBuilder {

    final String taskName;

    final List<Entry> entries = new ArrayList<>();

    /**
     * Creates a new instance.
     * @param taskName the task name
     */
    public RunnableBuilder(String taskName) {
        this.taskName = taskName;
    }

    /**
     * Adds a task.
     * @param task the task
     * @param taskSize the task size
     * @return this
     */
    public RunnableBuilder add(IRunnable task, int taskSize) {
        entries.add(new Entry(task, taskSize));
        return this;
    }

    /**
     * Builds a new task from previously added tasks.
     * @return the created task
     */
    public IRunnable build() {
        if (entries.size() == 1) {
            return entries.get(0).task;
        }
        final List<Entry> tasks = new ArrayList<>(entries);
        final int totalSize = computeTotal();
        return new IRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                monitor.beginTask(taskName, totalSize);
                try {
                    for (Entry entry : tasks) {
                        entry.task.run(new SubProgressMonitor(monitor, entry.taskSize));
                    }
                } finally {
                    monitor.done();
                }
            }
        };
    }

    /**
     * Builds a new task from previously added tasks.
     * @return the created task
     */
    public IWorkspaceRunnable buildWorkspaceRunnable() {
        return toWorkspaceRunnable(build());
    }

    /**
     * Builds a new {@link WorkspaceJob} from previously added tasks.
     * @return the created task
     */
    public WorkspaceJob buildWorkspaceJob() {
        return toWorkspaceJob(taskName, build());
    }

    /**
     * Converts {@link IRunnable} into {@link WorkspaceJob}.
     * @param name the job name
     * @param runnable the original operation
     * @return the converted operation
     */
    public static WorkspaceJob toWorkspaceJob(String name, final IRunnable runnable) {
        return new WorkspaceJob(name) {
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                runnable.run(monitor);
                return Status.OK_STATUS;
            }
        };
    }

    /**
     * Converts {@link IRunnable} into {@link IWorkspaceRunnable}.
     * @param runnable the original operation
     * @return the converted operation
     */
    public static IWorkspaceRunnable toWorkspaceRunnable(final IRunnable runnable) {
        if (runnable instanceof IWorkspaceRunnable) {
            return (IWorkspaceRunnable) runnable;
        }
        return new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                runnable.run(monitor);
            }
        };
    }

    private int computeTotal() {
        int result = 0;
        for (Entry entry : entries) {
            result += entry.taskSize;
        }
        return result;
    }

    private static final class Entry {

        final IRunnable task;

        final int taskSize;

        public Entry(IRunnable task, int taskSize) {
            this.task = task;
            this.taskSize = Math.min(1, taskSize);
        }
    }
}
