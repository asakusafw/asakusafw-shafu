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
package com.asakusafw.shafu.ui.util;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.asakusafw.shafu.core.util.ICallable;
import com.asakusafw.shafu.core.util.IRunnable;

/**
 * Progress Utilities.
 */
public final class ProgressUtils {

    private ProgressUtils() {
        return;
    }

    /**
     * Returns {@link ICallable} from {@link IRunnable}.
     * @param runnable the original operation
     * @return the wrapped operation
     */
    public static ICallable<Void> wrap(final IRunnable runnable) {
        return new ICallable<Void>() {
            @Override
            public Void call(IProgressMonitor monitor) throws CoreException {
                runnable.run(monitor);
                return null;
            }
        };
    }

    /**
     * Invokes {@link IRunnable#run(IProgressMonitor)} using {@link IRunnableContext the context}.
     * @param context the target context
     * @param runnable the target operation
     * @throws CoreException if the operation was failed
     */
    public static void run(IRunnableContext context, final IRunnable runnable) throws CoreException {
        call(context, wrap(runnable));
    }

    /**
     * Invokes {@link ICallable#call(IProgressMonitor)} using {@link IRunnableContext the context}.
     * @param context the target context
     * @param callable the target operation
     * @return the operation result
     * @param <T> the operation result type
     * @throws CoreException if the operation was failed
     */
    public static <T> T call(IRunnableContext context, final ICallable<T> callable) throws CoreException {
        try {
            final AtomicReference<T> result = new AtomicReference<>();
            context.run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        result.set(callable.call(monitor));
                    } catch (CoreException e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
            return result.get();
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CoreException) {
                throw (CoreException) cause;
            } else if (cause instanceof OperationCanceledException) {
                throw new CoreException(Status.CANCEL_STATUS);
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new AssertionError(cause);
            }
        } catch (InterruptedException e) {
            throw new CoreException(Status.CANCEL_STATUS);
        }
    }
}
