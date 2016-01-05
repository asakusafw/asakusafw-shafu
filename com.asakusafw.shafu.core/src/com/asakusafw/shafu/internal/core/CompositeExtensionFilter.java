/**
 * Copyright 2013-2016 Asakusa Framework Team.
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
package com.asakusafw.shafu.internal.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IExtension;

import com.asakusafw.shafu.core.extensions.IExtensionFilter;

/**
 * Composition of {@link IExtensionFilter}.
 */
public class CompositeExtensionFilter implements IExtensionFilter {

    private final IExtensionFilter[] filters;

    /**
     * Creates a new instance.
     * @param filters the element filters
     */
    public CompositeExtensionFilter(Collection<? extends IExtensionFilter> filters) {
        this.filters = filters.toArray(new IExtensionFilter[filters.size()]);
    }

    /**
     * Returns composite filter.
     * @param filters the element filters
     * @return the composite filter which accepts only if all elements accept the target extension
     */
    public static IExtensionFilter composite(Collection<? extends IExtensionFilter> filters) {
        if (filters.size() == 1) {
            return filters.iterator().next();
        }
        List<IExtensionFilter> newFilters = new ArrayList<IExtensionFilter>();
        for (IExtensionFilter filter : filters) {
            if (filter instanceof CompositeExtensionFilter) {
                Collections.addAll(newFilters, ((CompositeExtensionFilter) filter).filters);
            } else {
                newFilters.add(filter);
            }
        }
        return new CompositeExtensionFilter(newFilters);
    }

    @Override
    public boolean accept(IExtension extension) {
        for (IExtensionFilter filter : filters) {
            if (filter.accept(extension) == false) {
                return false;
            }
        }
        return true;
    }
}
