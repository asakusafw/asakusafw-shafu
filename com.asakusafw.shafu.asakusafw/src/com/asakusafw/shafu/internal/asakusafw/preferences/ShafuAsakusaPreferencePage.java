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
package com.asakusafw.shafu.internal.asakusafw.preferences;

import static com.asakusafw.shafu.internal.asakusafw.preferences.ShafuAsakusaPreferenceConstants.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.asakusafw.shafu.internal.asakusafw.Activator;
import com.asakusafw.shafu.internal.asakusafw.util.AsakusaFrameworkInfo;
import com.asakusafw.shafu.ui.fields.FieldPreferencePage;
import com.asakusafw.shafu.ui.fields.PreferenceField;

/**
 * Preference page for Shafu Asakusa Plug-in.
 */
public class ShafuAsakusaPreferencePage extends FieldPreferencePage implements IWorkbenchPreferencePage {

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite pane = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        pane.setLayout(layout);

        createUrlField(pane, KEY_CATALOG_URL, 1, Messages.ShafuAsakusaPreferencePage_itemCatalogUrl);
        createSettingsView(pane);

        return pane;
    }

    private void createUrlField(Composite parent, final String key, int span, final String title) {
        Composite pane = new Composite(parent, SWT.NONE);
        pane.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false)
                .span(span, 1)
                .create());

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        pane.setLayout(layout);

        Label label = new Label(pane, SWT.NONE);
        label.setText(title + ':');
        label.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.END)
                .grab(true, false)
                .create());

        final Text text = new Text(pane, SWT.BORDER);
        text.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.END)
                .grab(true, false)
                .create());

        registerField(new PreferenceField(key, text) {
            @Override
            public void refresh() {
                String current = getPreferenceValue(key);
                text.setText(current);
            }
        });
        text.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                String value = text.getText();
                try {
                    URL url = new URL(value);
                    setPreferenceValue(key, url.toExternalForm());
                } catch (MalformedURLException e) {
                    setError(key, MessageFormat.format(
                            Messages.ShafuAsakusaPreferencePage_errorUrlInvalid,
                            title,
                            value));
                }
            }
        });
    }

    private void createSettingsView(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(Messages.ShafuAsakusaPreferencePage_groupSettingsView);
        group.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .indent(0, convertHeightInCharsToPixels(1) / 2)
                .grab(true, false)
                .create());

        group.setLayout(new GridLayout(2, false));
        addPathSettingView(
                group,
                Messages.ShafuAsakusaPreferencePage_itemAsakusaHome,
                AsakusaFrameworkInfo.findInstallation());
        addPathSettingView(
                group,
                Messages.ShafuAsakusaPreferencePage_itemHadoopCommand,
                AsakusaFrameworkInfo.findHadoopCommand());
    }

    private void addPathSettingView(Group group, String title, File path) {
        Label label = new Label(group, SWT.NONE);
        label.setText(title + ':');
        label.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false)
                .span(2, 1)
                .create());
        final Text text = new Text(group, SWT.BORDER);
        text.setText(path == null ? Messages.ShafuAsakusaPreferencePage_valuePathNotAvailable : path.getPath());
        text.setEditable(false);
        text.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false)
                .span(2, 1)
                .create());
        if (path == null) {
            text.setEnabled(false);
        } else {
            text.addListener(SWT.FOCUSED, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    text.setSelection(0, text.getText().length());
                }
            });
        }
    }
}
