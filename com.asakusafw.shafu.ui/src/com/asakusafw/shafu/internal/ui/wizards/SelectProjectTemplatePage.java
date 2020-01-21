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
package com.asakusafw.shafu.internal.ui.wizards;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.asakusafw.shafu.core.gradle.GradleBuildTask;
import com.asakusafw.shafu.core.util.IRunnable;
import com.asakusafw.shafu.core.util.StatusUtils;
import com.asakusafw.shafu.internal.ui.Activator;
import com.asakusafw.shafu.internal.ui.LogUtil;
import com.asakusafw.shafu.internal.ui.dialogs.FilteredListDialog;
import com.asakusafw.shafu.ui.IProjectTemplateProvider;
import com.asakusafw.shafu.ui.ShafuUi;
import com.asakusafw.shafu.ui.consoles.ShafuConsole;
import com.asakusafw.shafu.ui.util.ProgressUtils;

/**
 * Project template selection page.
 * @since 0.1.0
 * @version 0.2.9
 */
public class SelectProjectTemplatePage extends WizardPage {

    private static final String KEY_DIALOG_USE_LOCAL = "local"; //$NON-NLS-1$

    private static final String KEY_DIALOG_FILE = "file"; //$NON-NLS-1$

    private static final String KEY_DIALOG_URL = "url"; //$NON-NLS-1$

    private static final String KEY_DIALOG_BUILD = "build"; //$NON-NLS-1$

    private static final List<String> ARCHIVE_EXTENSIONS = Collections.unmodifiableList(Arrays.asList(new String[] {
            ".zip", //$NON-NLS-1$
            ".tar.gz", //$NON-NLS-1$
    }));

    private static final Pattern PATTERN_TEMPLATE_FILTER = Pattern.compile(
            Messages.SelectProjectTemplatePage_templateFilterPattern);

    private ShafuConsole console;

    private Button buildCheck;

    private Button openConsoleButton;

    private volatile File targetFile;

    private volatile URL targetUrl;

    private Button useUrl;

    private Button useFile;

    Text urlText;

    Text fileText;

    /**
     * Creates a new instance.
     */
    public SelectProjectTemplatePage() {
        super("SelectProjectTemplatePage"); //$NON-NLS-1$
        setTitle(Messages.SelectProjectTemplatePage_title);
        setDescription(Messages.SelectProjectTemplatePage_description);
    }

    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        Composite pane = new Composite(parent, SWT.NONE);
        pane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setControl(pane);

        pane.setLayout(new GridLayout(1, false));

        this.useUrl = new Button(pane, SWT.RADIO);
        useUrl.setText(Messages.SelectProjectTemplatePage_useUrlLabel);
        useUrl.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false)
                .create());

        final Composite urlArea = new Composite(pane, SWT.NONE);
        urlArea.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false)
                .indent(convertWidthInCharsToPixels(2), 0)
                .create());
        urlArea.setLayout(new GridLayout(2, false));

        this.urlText = new Text(urlArea, SWT.BORDER);
        urlText.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.CENTER)
                .grab(true, false)
                .create());

        final Button urlButton = new Button(urlArea, SWT.PUSH);
        urlButton.setText(Messages.SelectProjectTemplatePage_urlSelect);
        urlButton.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.CENTER, SWT.CENTER)
                .grab(false, false)
                .create());
        urlButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String result = selectUrl(urlText.getText());
                if (result != null) {
                    urlText.setText(result);
                }
            }
        });

        this.useFile = new Button(pane, SWT.RADIO);
        useFile.setText(Messages.SelectProjectTemplatePage_useFileLabel);
        useFile.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false)
                .create());

        final Composite fileArea = new Composite(pane, SWT.NONE);
        fileArea.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false)
                .indent(convertWidthInCharsToPixels(2), 0)
                .create());
        fileArea.setLayout(new GridLayout(2, false));

        this.fileText = new Text(fileArea, SWT.BORDER);
        fileText.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.CENTER)
                .grab(true, false)
                .create());

        final Button fileButton = new Button(fileArea, SWT.PUSH);
        fileButton.setText(Messages.SelectProjectTemplatePage_openFileButton);
        fileButton.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.CENTER, SWT.CENTER)
                .grab(false, false)
                .create());
        fileButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String result = selectFile(fileText.getText());
                if (result != null) {
                    fileText.setText(result);
                }
            }
        });

        createGradleSettings(pane);
        createConsoleButton(pane);

        setDefaults(fileArea, urlArea);
        handleGroup(useFile, fileArea, urlArea, fileText);
        handleGroup(useUrl, urlArea, fileArea, urlText);

        setPageComplete(false);
        if (useFile.getSelection()) {
            enable(fileArea, true);
            enable(urlArea, false);
            if (fileText.getText().isEmpty() == false) {
                refreshText();
            }
        } else {
            enable(fileArea, false);
            enable(urlArea, true);
            if (urlText.getText().isEmpty() == false) {
                refreshText();
            }
        }
    }

    String selectFile(String oldValue) {
        StringBuilder extensions = new StringBuilder();
        for (String extension : ARCHIVE_EXTENSIONS) {
            if (extensions.length() > 0) {
                extensions.append(';');
            }
            extensions.append('*');
            extensions.append(extension);
        }

        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
        dialog.setText(Messages.SelectProjectTemplatePage_fileDialogTitle);
        if (extensions.length() > 0) {
            dialog.setFilterExtensions(new String[] { extensions.toString() });
        }
        dialog.setOverwrite(true);
        if (oldValue.isEmpty() == false) {
            dialog.setFileName(oldValue);
        }
        String result = dialog.open();
        return result;
    }

    String selectUrl(String oldValue) {
        Map<String, URL> templates = prepareTemplates();
        if (templates.isEmpty()) {
            MessageDialog.openInformation(
                    getShell(),
                    Messages.SelectProjectTemplatePage_urlNoTemplateTitle,
                    Messages.SelectProjectTemplatePage_urlNoTemplateMessage);
            return null;
        }

        FilteredListDialog dialog = new FilteredListDialog(getShell());
        dialog.setTitle(Messages.SelectProjectTemplatePage_urlSelectTemplateTitle);
        dialog.setMessage(Messages.SelectProjectTemplatePage_urlSelectTemplateMessage);
        dialog.setContentProvider(new ArrayContentProvider());
        dialog.setLabelProvider(new LabelProvider());
        dialog.setFilter(
                new RegexFilter(PATTERN_TEMPLATE_FILTER),
                Messages.SelectProjectTemplatePage_disableTemplateFilterLabel);
        dialog.setFilterEnabled(true);
        dialog.setInputData(templates.keySet().toArray());
        dialog.setBlockOnOpen(true);

        for (Map.Entry<String, URL> entry : templates.entrySet()) {
            String value = entry.getValue().toExternalForm();
            if (value.equals(oldValue)) {
                dialog.setInitialElementSelections(Collections.singletonList(entry.getKey()));
                break;
            }
        }

        if (dialog.open() != Window.OK) {
            return null;
        }
        Object[] results = dialog.getResult();
        if (results.length == 0) {
            return null;
        }
        URL resolved = templates.get(results[0]);
        if (resolved == null) {
            return null;
        }
        return resolved.toExternalForm();
    }

    private void createGradleSettings(Composite pane) {
        Group group = new Group(pane, SWT.NONE);
        group.setText(Messages.SelectProjectTemplatePage_gradleGroupLabel);
        group.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false)
                .indent(convertWidthInCharsToPixels(1), convertHeightInCharsToPixels(1))
                .create());
        group.setLayout(new GridLayout(2, false));

        this.buildCheck = new Button(group, SWT.CHECK);
        buildCheck.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.BEGINNING, SWT.CENTER)
                .create());
        buildCheck.setText(Messages.SelectProjectTemplatePage_buildCheckLabel);
        buildCheck.setToolTipText(MessageFormat.format(
                Messages.SelectProjectTemplatePage_buildCheckTooltip,
                GradleBuildTask.TASK_BUILD_PROJECT));

        final IDialogSettings settings = Activator.getDialogSettings(getClass().getSimpleName());
        String defaultValue = settings.get(KEY_DIALOG_BUILD);
        if (defaultValue == null || defaultValue.equals(String.valueOf(true))) {
            buildCheck.setSelection(true);
        } else {
            buildCheck.setSelection(false);
        }
        buildCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean selection = ((Button) e.getSource()).getSelection();
                settings.put(KEY_DIALOG_BUILD, String.valueOf(selection));
                refreshText();
            }
        });
    }

    private void createConsoleButton(Composite pane) {
        this.openConsoleButton = new Button(pane, SWT.PUSH);
        openConsoleButton.setText(Messages.SelectProjectTemplatePage_openConsoleLabel);
        openConsoleButton.setToolTipText(Messages.SelectProjectTemplatePage_openConsoleTooltip);
        openConsoleButton.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.END, SWT.END)
                .create());
        openConsoleButton.setEnabled(false);
        openConsoleButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openConsole();
            }
        });
    }

    void openConsole() {
        ShafuUi.open(getShell(), getConsole());
    }

    private void handleGroup(Button target, final Composite enable, final Composite disable, Text field) {
        field.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                refreshText();
            }
        });
        target.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                enable(enable, true);
                enable(disable, false);
                refreshText();
            }
        });
    }

    void enable(Composite area, boolean enable) {
        area.setEnabled(enable);
        for (Control child : area.getChildren()) {
            if (child instanceof Composite) {
                enable((Composite) child, enable);
            } else {
                child.setEnabled(enable);
            }
        }
    }

    private void setDefaults(Composite fileArea, Composite urlArea) {
        IDialogSettings settings = Activator.getDialogSettings(getClass().getSimpleName());
        String defaultUseLocal = settings.get(KEY_DIALOG_USE_LOCAL);
        if (defaultUseLocal == null || defaultUseLocal.equals("true") == false) { //$NON-NLS-1$
            useUrl.setSelection(true);
            useFile.setSelection(false);
            enable(urlArea, true);
            enable(fileArea, false);
        } else {
            useUrl.setSelection(false);
            useFile.setSelection(true);
            enable(urlArea, false);
            enable(fileArea, true);
        }
        String defaultUrl = settings.get(KEY_DIALOG_URL);
        if (defaultUrl != null) {
            urlText.setText(defaultUrl);
        }
        String defaultFile = settings.get(KEY_DIALOG_FILE);
        if (defaultFile != null) {
            fileText.setText(defaultFile);
        }
    }

    private Map<String, URL> prepareTemplates() {
        final Map<String, URL> results = new TreeMap<>();
        final List<IProjectTemplateProvider> providers = Activator.getExtensions().getProjectTemplateProviders();
        try {
            ProgressUtils.run(getContainer(), new IRunnable() {
                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    SubMonitor sub = SubMonitor.convert(monitor, Messages.SelectProjectTemplatePage_monitorExtractTemplate, providers.size());
                    for (IProjectTemplateProvider provider : providers) {
                        StatusUtils.checkCanceled(monitor);
                        try {
                            results.putAll(provider.getProjectTemplates(sub.newChild(1)));
                        } catch (CoreException e) {
                            StatusUtils.rethrowIfCancel(e);
                            LogUtil.log(e.getStatus());
                        }
                    }
                }
            });
        } catch (CoreException e) {
            if (StatusUtils.hasCancel(e.getStatus()) == false) {
                LogUtil.log(e.getStatus());
            }
        }
        return results;
    }

    void refreshText() {
        this.targetFile = null;
        this.targetUrl = null;
        if (useFile.getSelection()) {
            String text = fileText.getText();
            if (text.isEmpty()) {
                setErrorMessage(Messages.SelectProjectTemplatePage_errorFileEmpty);
                setPageComplete(false);
                return;
            }
            File file = new File(text);
            if (file.isFile() == false) {
                setErrorMessage(Messages.SelectProjectTemplatePage_errorFileMissing);
                setPageComplete(false);
                return;
            } else if (isSupportedFileName(file.getName()) == false) {
                setErrorMessage(Messages.SelectProjectTemplatePage_errorFileNotSupport);
                setPageComplete(false);
                return;
            }
            this.targetFile = file;
            setErrorMessage(null);
            setPageComplete(true);
            IDialogSettings settings = Activator.getDialogSettings(getClass().getSimpleName());
            settings.put(KEY_DIALOG_USE_LOCAL, String.valueOf(true));
            settings.put(KEY_DIALOG_FILE, text);
        } else  {
            String text = urlText.getText();
            if (text.isEmpty()) {
                setErrorMessage(Messages.SelectProjectTemplatePage_errorUrlEmpty);
                setPageComplete(false);
                return;
            }
            URL url;
            try {
                url = new URL(text);
            } catch (MalformedURLException e) {
                setErrorMessage(Messages.SelectProjectTemplatePage_errorUrlInvalid);
                setPageComplete(false);
                return;
            }
            if (isSupportedFileName(url.getPath()) == false) {
                setErrorMessage(Messages.SelectProjectTemplatePage_errorFileNotSupport);
                setPageComplete(false);
                return;
            }
            this.targetUrl = url;
            setErrorMessage(null);
            setPageComplete(true);
            IDialogSettings settings = Activator.getDialogSettings(getClass().getSimpleName());
            settings.put(KEY_DIALOG_USE_LOCAL, String.valueOf(false));
            settings.put(KEY_DIALOG_URL, text);
        }
    }

    private boolean isSupportedFileName(String name) {
        if (name == null) {
            return false;
        }
        for (String extension : ARCHIVE_EXTENSIONS) {
            if (name.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    File getTargetFile() {
        return targetFile;
    }

    URL getTargetUrl() {
        return targetUrl;
    }

    List<String> getTaskNames() {
        List<String> results = new ArrayList<>();
        if (buildCheck.getSelection()) {
            results.add(GradleBuildTask.TASK_BUILD_PROJECT);
        }
        results.add(GradleBuildTask.TASK_DECONFIGURE_ECLIPSE);
        results.add(GradleBuildTask.TASK_CONFIGURE_ECLIPSE);
        return results;
    }

    ShafuConsole getConsole() {
        if (console == null) {
            console = new ShafuConsole();
            openConsoleButton.setEnabled(true);
        }
        return console;
    }

    @Override
    public void dispose() {
        try {
            if (console != null) {
                console.destroy();
                console = null;
            }
        } finally {
            super.dispose();
        }
    }

    private static final class RegexFilter extends ViewerFilter {

        private final Pattern pattern;

        public RegexFilter(Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            Matcher matcher = pattern.matcher((CharSequence) element);
            return matcher.find() == false;
        }
    }
}
