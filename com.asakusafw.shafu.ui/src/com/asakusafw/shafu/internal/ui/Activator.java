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
package com.asakusafw.shafu.internal.ui;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.asakusafw.shafu.ui.consoles.ShafuConsole;

/**
 * The activator for this plug-in.
 */
public class Activator extends AbstractUIPlugin {

    /**
     * The plug-in ID.
     */
    public static final String PLUGIN_ID = "com.asakusafw.shafu.ui"; //$NON-NLS-1$

    /**
     * The prefix of extension point IDs in this plug-in.
     */
    public static final String EXTENSION_PREFIX = PLUGIN_ID + '.';

    private static Activator plugin;

    private ShafuConsole console;

    private ExtensionManager extensions;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        extensions = new ExtensionManager();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        extensions = null;
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance.
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns the extension points manager.
     * @return the extension points manager
     */
    public static ExtensionManager getExtensions() {
        return getDefault().extensions;
    }

    /**
     * Returns the current standard display.
     * @return the current standard display
     */
    public static Display getDisplay() {
        Display result = Display.getCurrent();
        if (result == null) {
            result = Display.getDefault();
        }
        return result;
    }

    /**
     * Returns the message console for this plug-in.
     * @return the message console
     */
    public synchronized static ShafuConsole getConsole() {
        Activator service = getDefault();
        if (service == null) {
            throw new IllegalStateException();
        }
        if (service.console == null) {
            LogUtil.debug("Initializing Console Service"); //$NON-NLS-1$
            service.console = new ShafuConsole();
        }
        return service.console;
    }

    /**
     * Returns dialog settings for the specified section ID.
     * @param id target section ID
     * @return the dialog settings store
     */
    public static IDialogSettings getDialogSettings(String id) {
        String qid = String.format("%s.%s", PLUGIN_ID, id); //$NON-NLS-1$
        IDialogSettings settings = getDefault().getDialogSettings();
        IDialogSettings result = settings.getSection(qid);
        if (result == null) {
            result = settings.addNewSection(qid);
        }
        return result;
    }
}
