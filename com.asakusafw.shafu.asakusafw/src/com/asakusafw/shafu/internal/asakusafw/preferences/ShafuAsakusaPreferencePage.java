/**
 * Copyright 2013 Asakusa Framework Team.
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.asakusafw.shafu.internal.asakusafw.Activator;
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
}
