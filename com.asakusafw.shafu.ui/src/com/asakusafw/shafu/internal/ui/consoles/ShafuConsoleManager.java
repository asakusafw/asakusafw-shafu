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
package com.asakusafw.shafu.internal.ui.consoles;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;

import com.asakusafw.shafu.internal.ui.Activator;
import com.asakusafw.shafu.ui.consoles.ShafuConsole;

/**
 * Provides {@link ShafuConsole}.
 */
public class ShafuConsoleManager implements IConsoleFactory {

    /**
     * Returns the console.
     * @return the console
     * @throws IllegalStateException if the console is not enabled
     */
    public static ShafuConsole getConsole() {
        ShafuConsole console = Activator.getConsole();
        if (console != null) {
            return console;
        }
        throw new IllegalStateException(Messages.ShafuConsoleManager_errorNotAvailable);
    }

    /**
     * Shows the console.
     */
    public static void showConsole() {
        ShafuConsole console = Activator.getConsole();
        if (console != null) {
            openConsole0();
            IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
            manager.showConsoleView(console);
            console.activate();
        }
    }

    @Override
    public void openConsole() {
        openConsole0();
    }

    private static void openConsole0() {
        ShafuConsole console = Activator.getConsole();
        if (console != null) {
            IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
            for (IConsole other : manager.getConsoles()) {
                if (console == other) {
                    return;
                }
            }
            manager.addConsoles(new IConsole[] { console });
        }
    }
}
