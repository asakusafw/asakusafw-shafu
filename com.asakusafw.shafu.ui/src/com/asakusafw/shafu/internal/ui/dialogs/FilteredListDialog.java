/**
 *
 */
package com.asakusafw.shafu.internal.ui.dialogs;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * List dialog with configurable filters.
 * @since 0.2.9
 */
public class FilteredListDialog extends SelectionDialog {

    private Object inputData;

    private IStructuredContentProvider contentProvider;

    private ILabelProvider labelProvider;

    private ViewerFilter[] filters;

    private String filterLabel;

    private TableViewer tableViewer;

    private boolean filterEnabled;

    /**
     * Creates a new instance.
     * @param parent the parent shell
     */
    public FilteredListDialog(Shell parent) {
        super(parent);
    }

    /**
     * Sets the input data.
     * @param value the input data.
     */
    public void setInputData(Object value) {
        this.inputData = value;
    }

    /**
     * Sets the content provider.
     * @param provider the content provider
     */
    public void setContentProvider(IStructuredContentProvider provider) {
        this.contentProvider = provider;
    }

    /**
     * Sets the label provider.
     * @param provider the label provider
     */
    public void setLabelProvider(ILabelProvider provider) {
        this.labelProvider = provider;
    }

    /**
     * Sets the filter and its label text.
     * @param filter the filter
     * @param label the label text
     */
    public void setFilter(ViewerFilter filter, String label) {
        this.filters = new ViewerFilter[] { filter };
        this.filterLabel = label;
    }

    /**
     * Sets whether the filter is initially enabled.
     * @param enable {@code true} if enabled, otherwise {@code false}
     */
    public void setFilterEnabled(boolean enable) {
        this.filterEnabled = enable;
    }

    /**
     * Returns the style flags for the list (as a {@link TableViewer}).
     * @return the table style
     */
    protected int getTableStyle() {
        return SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER;
    }

    @Override
    protected Control createDialogArea(Composite root) {
        Composite parent = (Composite) super.createDialogArea(root);
        createMessageArea(parent);
        this.tableViewer = new TableViewer(parent, getTableStyle());
        this.tableViewer.setContentProvider(contentProvider);
        this.tableViewer.setLabelProvider(labelProvider);
        this.tableViewer.setInput(inputData);
        this.tableViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                okPressed();
            }
        });
        List<?> initialSelection = getInitialElementSelections();
        if (initialSelection != null) {
            tableViewer.setSelection(new StructuredSelection(initialSelection));
        }
        this.tableViewer.getTable().setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.FILL)
                .grab(true, true)
                .hint(convertWidthInCharsToPixels(50), convertHeightInCharsToPixels(15))
                .create());
        if (filters != null && filterLabel != null) {
            final Button showAll = new Button(parent, SWT.CHECK);
            showAll.setText(filterLabel);
            showAll.setLayoutData(GridDataFactory.swtDefaults()
                    .align(SWT.BEGINNING, SWT.BEGINNING)
                    .grab(true, false)
                    .create());
            showAll.setSelection(filterEnabled == false);
            showAll.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    refreshFilter(showAll.getSelection() == false);
                }
            });
        }
        refreshFilter(filterEnabled);

        applyDialogFont(parent);
        return parent;
    }

    void refreshFilter(boolean enabled) {
        if (enabled) {
            if (filters != null) {
                this.tableViewer.setFilters(filters);
            } else {
                this.tableViewer.resetFilters();
            }
            this.filterEnabled = enabled;
        } else {
            this.tableViewer.resetFilters();
        }
        ISelection selection = this.tableViewer.getSelection();
        if (selection.isEmpty()) {
            Object first = this.tableViewer.getElementAt(0);
            if (first != null) {
                this.tableViewer.setSelection(new StructuredSelection(first));
            }
        }
    }

    /**
     * Returns whether the filter is enabled or not.
     * @return {@code true} if filter is enabled, otherwise {@code false}
     */
    public boolean isFilterEnabled() {
        return filterEnabled;
    }

    @Override
    protected void okPressed() {
        IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
        setResult(selection.toList());
        super.okPressed();
    }
}
