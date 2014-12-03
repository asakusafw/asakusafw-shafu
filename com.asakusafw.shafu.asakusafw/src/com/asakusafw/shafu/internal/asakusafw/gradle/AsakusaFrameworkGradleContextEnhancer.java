/**
 *
 */
package com.asakusafw.shafu.internal.asakusafw.gradle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.Bundle;

import com.asakusafw.shafu.core.gradle.GradleContext;
import com.asakusafw.shafu.core.gradle.IGradleContextEnhancer;
import com.asakusafw.shafu.core.util.StatusUtils;
import com.asakusafw.shafu.internal.asakusafw.Activator;
import com.asakusafw.shafu.internal.asakusafw.preferences.ShafuAsakusaPreferenceConstants;

/**
 * Enhances {@link GradleContext} for Asakusa Framework.
 * @since 0.2.10
 */
public class AsakusaFrameworkGradleContextEnhancer implements IGradleContextEnhancer {

    private static final IPath SCRIPT_BASE_PATH = Path.fromPortableString("scripts"); //$NON-NLS-1$

    private static final IPath SCRIPT_PATH = SCRIPT_BASE_PATH.append("init.gradle"); //$NON-NLS-1$

    private static final String PREFIX_KEY = Activator.PLUGIN_ID + "."; //$NON-NLS-1$

    private static final String KEY_EMULATION_MODE = PREFIX_KEY + "enableEmulationMode"; //$NON-NLS-1$

    @Override
    public void enhance(IProgressMonitor monitor, GradleContext context) throws CoreException {
        SubMonitor sub = SubMonitor.convert(monitor, Messages.AsakusaFrameworkGradleContextEnhancer_monitorEnhance, 100);
        try {
            File script = resolveScript(sub.newChild(80));
            configureContext(sub.newChild(20), context, script);
        } finally {
            monitor.done();
        }
    }

    private void configureContext(SubMonitor monitor, GradleContext context, File script) throws CoreException {
        StatusUtils.checkCanceled(monitor);
        configureGradleArguments(context, script);
        configureExtensionSettings(context);
    }

    private void configureGradleArguments(GradleContext context, File script) {
        List<String> newArguments = new ArrayList<String>();
        newArguments.add("--init-script"); //$NON-NLS-1$
        newArguments.add(script.getAbsolutePath());
        newArguments.addAll(context.getGradleArguments());
        context.setGradleArguments(newArguments);
    }

    private void configureExtensionSettings(GradleContext context) {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        if (prefs.getBoolean(ShafuAsakusaPreferenceConstants.KEY_EMULATION_MODE)) {
            context.withJvmArguments(String.format("-D%s=true", KEY_EMULATION_MODE)); //$NON-NLS-1$
        }
    }

    private File resolveScript(SubMonitor monitor) throws CoreException {
        try {
            StatusUtils.checkCanceled(monitor);
            IPath path = resolveBuiltinPath(Activator.getDefault().getBundle(), SCRIPT_PATH);
            return path.toFile();
        } catch (IOException e) {
            throw new CoreException(new Status(
                    IStatus.ERROR,
                    Activator.PLUGIN_ID,
                    Messages.AsakusaFrameworkGradleContextEnhancer_errorExtractExtraScript));
        }
    }

    private static IPath resolveBuiltinPath(Bundle bundle, IPath relativePath) throws IOException {
        assert relativePath != null;
        URL entry = bundle.getEntry(relativePath.toPortableString());
        if (entry == null) {
            throw new FileNotFoundException(relativePath.toPortableString());
        }
        URL fileUrl = FileLocator.toFileURL(entry);
        if (fileUrl.getProtocol().equals("file") == false) { //$NON-NLS-1$
            throw new FileNotFoundException(fileUrl.toExternalForm());
        }
        // Test file exists
        InputStream input = fileUrl.openStream();
        input.close();
        return toPath(fileUrl);
    }

    private static IPath toPath(URL url) throws IOException {
        assert url != null;
        assert url.getProtocol().equals("file"); //$NON-NLS-1$
        try {
            URI fileUri = url.toURI();
            File file = new File(fileUri);
            file = file.getAbsoluteFile().getCanonicalFile();
            IPath path = Path.fromOSString(file.getAbsolutePath());
            if (path.toFile().exists()) {
                return path;
            }
            // continue...
        }
        catch (URISyntaxException ignore) {
            // continue...
        }

        String filePath = url.getFile();
        IPath path = Path.fromPortableString(filePath);
        if (path.toFile().exists()) {
            return path;
        }

        if (filePath.isEmpty() == false && filePath.startsWith("/")) { //$NON-NLS-1$
            path = Path.fromPortableString(filePath.substring(1));
            if (path.toFile().exists()) {
                return path;
            }
        }
        throw new FileNotFoundException(url.toExternalForm());
    }
}
