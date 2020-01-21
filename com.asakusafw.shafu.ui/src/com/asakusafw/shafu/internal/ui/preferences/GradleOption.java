/**
 * Copyright 2013-2020 Asakusa Framework Team.
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
package com.asakusafw.shafu.internal.ui.preferences;

import java.util.List;

/**
 * Represents a Gradle option.
 * @version 0.2.4
 */
public interface GradleOption {

    /**
     * Returns the option name.
     * @return the option name
     */
    String name();

    /**
     * Returns the description of this option.
     * @return the description
     */
    String getDescription();

    /**
     * Returns the symbol for this option.
     * @return the corresponded symbol
     */
    String getSymbol();

    /**
     * Returns the Gradle arguments for this option.
     * @return the arguments
     */
    List<String> getArguments();

    /**
     * Returns the short option name.
     * @return the short option name, or {@code null} if it is not defined
     * @since 0.2.4
     */
    String getOptionName();

    /**
     * Returns the long option name.
     * @return the long option name, or {@code null} if it is not defined
     * @since 0.2.4
     */
    String getLongOptionName();
}
