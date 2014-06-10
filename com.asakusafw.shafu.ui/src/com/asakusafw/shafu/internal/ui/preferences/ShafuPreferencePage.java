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
package com.asakusafw.shafu.internal.ui.preferences;

import static com.asakusafw.shafu.internal.ui.preferences.ShafuPreferenceConstants.*;
import static com.asakusafw.shafu.ui.util.PreferenceUtils.*;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.asakusafw.shafu.internal.ui.Activator;
import com.asakusafw.shafu.internal.ui.LogUtil;
import com.asakusafw.shafu.internal.ui.dialogs.PropertyEntryInputDialog;
import com.asakusafw.shafu.ui.fields.BasicField;
import com.asakusafw.shafu.ui.fields.FieldPreferencePage;
import com.asakusafw.shafu.ui.fields.PreferenceField;

/**
 * A root preference page for Shafu.
 */
public class ShafuPreferencePage extends FieldPreferencePage implements IWorkbenchPreferencePage {

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    protected Control createContents(Composite parent) {
        TabFolder folder = new TabFolder(parent, SWT.NONE);
        folder.setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, true)
                .create());

        Composite basicTab = new Composite(folder, SWT.NONE);
        createBasicTab(basicTab);

        Composite projectTab = new Composite(folder, SWT.NONE);
        createProjectTab(projectTab);

        Composite jvmTab = new Composite(folder, SWT.NONE);
        createJvmTab(jvmTab);

        createTabItem(folder, basicTab, Messages.ShafuPreferencePage_tabBasic);
        createTabItem(folder, projectTab, Messages.ShafuPreferencePage_tabProject);
        createTabItem(folder, jvmTab, Messages.ShafuPreferencePage_tabJvm);

        applyDialogFont(folder);
        return folder;
    }

    private void createBasicTab(Composite pane) {
        pane.setLayout(new GridLayout(1, false));

        Group loggingGroup = new Group(pane, SWT.NONE);
        loggingGroup.setText(Messages.ShafuPreferencePage_groupLogging);
        loggingGroup.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false)
                .create());
        loggingGroup.setLayout(new GridLayout(2, false));
        createComboField(loggingGroup, KEY_LOG_LEVEL, GradleLogLevel.values(), Messages.ShafuPreferencePage_itemLogLevel);
        createComboField(loggingGroup, KEY_STACK_TRACE, GradleStackTrace.values(), Messages.ShafuPreferencePage_itemStackTrace);

        Group environmentGroup = new Group(pane, SWT.NONE);
        environmentGroup.setText(Messages.ShafuPreferencePage_groupEnvironment);
        environmentGroup.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .grab(true, false)
                .create());
        environmentGroup.setLayout(new GridLayout(2, false));
        createVersionField(environmentGroup, KEY_GRADLE_VERSION, Messages.ShafuPreferencePage_itemGradleVersion, 10, false);
        createComboField(environmentGroup, KEY_NETWORK_MODE, GradleNetworkMode.values(), Messages.ShafuPreferencePage_itemNetworkMode);
        createDirectoryField(environmentGroup, KEY_GRADLE_USER_HOME, 2, Messages.ShafuPreferencePage_itemGradleUserHome, false);
    }

    private void createProjectTab(Composite pane) {
        pane.setLayout(new GridLayout(1, false));
        createPropertiesField(pane, KEY_PROJECT_PROPERTIES, 1, Messages.ShafuPreferencePage_itemProjectProperties);
    }

    private void createJvmTab(Composite pane) {
        pane.setLayout(new GridLayout(1, false));
        createDirectoryField(pane, KEY_JAVA_HOME, 1, Messages.ShafuPreferencePage_itemJavaHome, false);
        createPropertiesField(pane, KEY_SYSTEM_PROPERTIES, 1, Messages.ShafuPreferencePage_itemSystemProperties);
    }

    private void createTabItem(TabFolder folder, Composite content, String label) {
        TabItem item = new TabItem(folder, SWT.NONE);
        item.setText(label);
        item.setControl(content);
    }

    private void createVersionField(
            Composite pane, final String key, String title, int columns, final boolean mandatory) {
        Label label = new Label(pane, SWT.NONE);
        label.setText(title + ':');
        label.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.CENTER)
                .indent(BasicField.getDecorationWidth(), 0)
                .create());

        final Text text = new Text(pane, SWT.BORDER);
        text.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.BEGINNING, SWT.CENTER)
                .indent(convertWidthInCharsToPixels(2) + BasicField.getDecorationWidth(), 0)
                .hint(convertWidthInCharsToPixels(columns + 1), SWT.DEFAULT)
                .create());

        registerField(new PreferenceField(key, text) {
            @Override
            public void refresh() {
                String current = getPreferenceValue(key);
                String value = decodeVersion(current);
                value = value == null ? "" : value; //$NON-NLS-1$
                text.setText(value);
            }
            @Override
            protected IStatus getDefaultStatus() {
                if (mandatory) {
                    return Status.OK_STATUS;
                } else {
                    return new Status(
                            IStatus.INFO,
                            Activator.PLUGIN_ID,
                            Messages.ShafuPreferencePage_hintOptionalText);
                }
            }
        });
        text.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
                setPreferenceValue(key, encodeVersion(text.getText()));
            }
        });
    }

    private void createComboField(Composite pane, final String key, GradleOption[] options, String title) {
        Label label = new Label(pane, SWT.NONE);
        label.setText(title + ':');
        label.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.CENTER)
                .indent(BasicField.getDecorationWidth(), 0)
                .create());

        final Combo combo = new Combo(pane, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.BEGINNING, SWT.CENTER)
                .indent(convertWidthInCharsToPixels(2) + BasicField.getDecorationWidth(), 0)
                .create());
        final List<String> itemLabels = new ArrayList<String>(options.length);
        final List<String> itemValues = new ArrayList<String>(options.length);
        for (GradleOption option : options) {
            itemLabels.add(option.getDescription());
            itemValues.add(option.name());
        }
        combo.setItems(itemLabels.toArray(new String[itemLabels.size()]));

        registerField(new PreferenceField(key, combo) {
            @Override
            public void refresh() {
                String current = getPreferenceValue(key);
                int index = itemValues.indexOf(current);
                if (index < 0) {
                    index = 0;
                }
                combo.select(index);
            }
        });

        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String itemLabel = combo.getText();
                int index = itemLabels.indexOf(itemLabel);
                if (index < 0) {
                    LogUtil.log(new Status(
                            IStatus.WARNING,
                            Activator.PLUGIN_ID,
                            MessageFormat.format(
                                    "[INTERNAL] Missing option value: {0}", //$NON-NLS-1$
                                    itemLabel)));
                    setPreferenceValue(key, null);
                } else {
                    setPreferenceValue(key, itemValues.get(index));
                }
            }
        });
    }

    private void createDirectoryField(
            Composite pane,
            final String key,
            int span,
            final String title,
            final boolean mandatory) {

        Composite fieldPane = new Composite(pane, span);
        fieldPane.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .span(span, 1)
                .grab(true, false)
                .create());
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        fieldPane.setLayout(layout);

        Label fieldLabel = new Label(fieldPane, SWT.NONE);
        fieldLabel.setText(title + ':');
        fieldLabel.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.BEGINNING, SWT.END)
                .span(2, 1)
                .indent(BasicField.getDecorationWidth(), 0)
                .create());

        final Text fieldText = new Text(fieldPane, SWT.BORDER);
        fieldText.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.CENTER)
                .grab(true, false)
                .indent(BasicField.getDecorationWidth(), 0)
                .create());

        Button fieldButton = new Button(fieldPane, SWT.PUSH);
        fieldButton.setText(Messages.ShafuPreferencePage_buttonDirectorySelection);
        fieldButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(getShell());
                dialog.setText(Messages.ShafuPreferencePage_dialogDirectorySelection);
                String current = fieldText.getText();
                if (current.isEmpty() == false) {
                    dialog.setFilterPath(current);
                }
                String result = dialog.open();
                if (result != null) {
                    fieldText.setText(result);
                }
            }
        });

        registerField(new PreferenceField(key, fieldText) {
            @Override
            public void refresh() {
                String current = getPreferenceValue(key);
                fieldText.setText(current);
            }
            @Override
            protected IStatus getDefaultStatus() {
                if (mandatory) {
                    return Status.OK_STATUS;
                } else {
                    return new Status(
                            IStatus.INFO,
                            Activator.PLUGIN_ID,
                            Messages.ShafuPreferencePage_hintOptionalText);
                }
            }
        });

        fieldText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String value = fieldText.getText();
                if (value.isEmpty()) {
                    if (mandatory) {
                        setError(key, MessageFormat.format(
                                Messages.ShafuPreferencePage_errorDirectoryEmpty,
                                title));
                    } else {
                        setPreferenceValue(key, value);
                    }
                    return;
                }
                File file = new File(value);
                if (file.isDirectory() == false) {
                    setError(key, MessageFormat.format(
                            Messages.ShafuPreferencePage_errorDirectoryMissing,
                            title));
                    return;
                }
                setPreferenceValue(key, value);
            }
        });
    }

    private void createPropertiesField(
            Composite pane,
            final String key,
            int span,
            String title) {
        Group group = new Group(pane, SWT.NONE);
        group.setText(title);
        group.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.FILL)
                .grab(true, true)
                .span(span, 1)
                .create());
        group.setLayout(new GridLayout(2, false));

        final Map<String, String> contents = new LinkedHashMap<String, String>();
        final TableViewer viewer = new TableViewer(
                group,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
        viewer.getControl().setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, true)
                .span(1, 3)
                .indent(BasicField.getDecorationWidth(), 0)
                .create());
        viewer.setContentProvider(new PropertiesContentProvider());
        viewer.setLabelProvider(new PropertiesLabelProvider());
        viewer.setInput(contents);
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        TableColumn keyColumn = new TableColumn(viewer.getTable(), SWT.NONE, 0);
        keyColumn.setResizable(true);
        keyColumn.setText(Messages.ShafuPreferencePage_propertiesKeyLabel);
        keyColumn.setWidth(convertWidthInCharsToPixels(20));
        TableColumn valueColumn = new TableColumn(viewer.getTable(), SWT.NONE, 1);
        valueColumn.setResizable(true);
        valueColumn.setText(Messages.ShafuPreferencePage_propertiesValueLabel);
        valueColumn.setWidth(convertWidthInCharsToPixels(40));
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                ISelection s = event.getSelection();
                if (s.isEmpty()) {
                    return;
                }
                if ((s instanceof IStructuredSelection) == false) {
                    return;
                }
                Map.Entry<?, ?> first = (Map.Entry<?, ?>) ((IStructuredSelection) s).iterator().next();
                String oldKey = (String) first.getKey();
                String oldValue = (String) first.getValue();
                PropertyEntryInputDialog dialog = new PropertyEntryInputDialog(getShell(), contents, oldKey, oldValue);
                if (dialog.open() != Window.OK) {
                    return;
                }
                contents.remove(oldKey);
                contents.put(dialog.getInputKey(), dialog.getInputValue());
                viewer.refresh();
                setPreferenceValue(key, encodeMap(contents));
            }
        });

        final Button addButton = new Button(group, SWT.PUSH);
        addButton.setText(Messages.ShafuPreferencePage_propertiesAddLabel);
        addButton.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .create());
        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                PropertyEntryInputDialog dialog = new PropertyEntryInputDialog(getShell(), contents);
                if (dialog.open() != Window.OK) {
                    return;
                }
                contents.put(dialog.getInputKey(), dialog.getInputValue());
                viewer.refresh();
                setPreferenceValue(key, encodeMap(contents));
            }
        });

        final Button removeButton = new Button(group, SWT.PUSH);
        removeButton.setText(Messages.ShafuPreferencePage_propertiesRemoveLabel);
        removeButton.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .create());
        removeButton.setEnabled(false);
        removeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ISelection selection = viewer.getSelection();
                if (selection instanceof IStructuredSelection) {
                    Iterator<?> iter = ((IStructuredSelection) selection).iterator();
                    Set<String> keys = new HashSet<String>();
                    while (iter.hasNext()) {
                        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) iter.next();
                        keys.add((String) entry.getKey());
                    }
                    contents.keySet().removeAll(keys);
                    viewer.refresh();
                    setPreferenceValue(key, encodeMap(contents));
                }
            }
        });

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                removeButton.setEnabled(event.getSelection().isEmpty() == false);
            }
        });

        registerField(new PreferenceField(key, viewer.getControl()) {
            @Override
            public void refresh() {
                String value = getPreferenceValue(key);
                Map<String, String> map = decodeToMap(value);
                contents.clear();
                contents.putAll(map);
                viewer.refresh();
                setPreferenceValue(key, encodeMap(contents));
            }
        });
    }

    private static class PropertiesContentProvider implements IStructuredContentProvider {

        PropertiesContentProvider() {
            return;
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            return;
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return ((Map<?, ?>) inputElement).entrySet().toArray();
        }

        @Override
        public void dispose() {
            return;
        }
    }

    private static class PropertiesLabelProvider extends LabelProvider implements ITableLabelProvider {

        PropertiesLabelProvider() {
            return;
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            Map.Entry<?, ?> entry = (Entry<?, ?>) element;
            if (columnIndex == 0) {
                return String.valueOf(entry.getKey());
            } else {
                return String.valueOf(entry.getValue());
            }
        }
    }
}
