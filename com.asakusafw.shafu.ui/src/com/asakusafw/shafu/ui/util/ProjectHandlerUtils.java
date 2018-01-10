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
package com.asakusafw.shafu.ui.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Utilities for handling {@link IProject}s.
 * @version 0.2.5
 */
public class ProjectHandlerUtils {

    private static final List<Class<? extends IResource>> RESOURCE_KIND;
    static {
        List<Class<? extends IResource>> classes = new ArrayList<>();
        classes.add(IResource.class);
        classes.add(IContainer.class);
        classes.add(IProject.class);
        classes.add(IFolder.class);
        classes.add(IFile.class);
        RESOURCE_KIND = Collections.unmodifiableList(classes);
    }

    /**
     * Obtains a project in current selection.
     * @param event current event object
     * @return the project in current selection, or {@code null} if there is no projects
     * @throws ExecutionException if event state is invalid
     * @throws IllegalArgumentException if the argument is null
     */
    public static IProject getTargetProject(ExecutionEvent event) throws ExecutionException {
        IResource resource = getTargetResource(event);
        if (resource == null) {
            return null;
        }
        return resource.getProject();
    }

    /**
     * Obtains a resource in current selection.
     * @param event current event object
     * @return the resource in current selection, or {@code null} if there is no projects
     * @throws ExecutionException if event state is invalid
     * @throws IllegalArgumentException if the argument is null
     */
    public static IResource getTargetResource(ExecutionEvent event) throws ExecutionException {
        if (event == null) {
            throw new IllegalArgumentException("event is null"); //$NON-NLS-1$
        }
        IResource resource;
        resource = getSelectedResource(event);
        if (resource != null) {
            return resource;
        }
        resource = getEditingResource(event);
        return resource;
    }

    private static IResource getSelectedResource(ExecutionEvent event) {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection == null || selection.isEmpty()) {
            return null;
        } else if (selection instanceof IStructuredSelection) {
            return adapt(((IStructuredSelection) selection).getFirstElement(), RESOURCE_KIND);
        }
        return null;
    }

    private static IResource getEditingResource(ExecutionEvent event) {
        IWorkbenchPart part = HandlerUtil.getActivePart(event);
        if (part == null || (part instanceof IEditorPart) == false) {
            return null;
        }
        IEditorPart editor = (IEditorPart) part;
        return adapt(editor.getEditorInput(), RESOURCE_KIND);
    }

    private static <T> T adapt(
            Object adaptable,
            List<? extends Class<? extends T>> adapterClasses) {
        assert adapterClasses != null;
        if ((adaptable instanceof IAdaptable) == false) {
            return null;
        }
        for (Class<? extends T> aClass : adapterClasses) {
            Object adapter = ((IAdaptable) adaptable).getAdapter(aClass);
            if (adapter != null) {
                return aClass.cast(adapter);
            }
        }
        return null;
    }

    private ProjectHandlerUtils() {
        return;
    }
}
