/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * @since 3.0
 */
class PreferenceImportExportSettingsSelectionPage extends
        AbstractPreferenceImportExportPage {

    /**
     * The message to display when there are no errors on this page.
     */
    private static final String EXPORT_MESSAGE = WorkbenchMessages
            .getString("ImportExportPages.exportSettingsSelect"); //$NON-NLS-1$

    /**
     * The message to display when there are no errors on this page.
     */
    private static final String IMPORT_MESSAGE = WorkbenchMessages
            .getString("ImportExportPages.importSettingsSelect"); //$NON-NLS-1$

    /**
     * The name of this page -- used to find the page later.
     */
    private static final String NAME = "org.eclipse.ui.preferences.importExportSettingsSelectionPage"; //$NON-NLS-1$

    /**
     * The table containing the list of settings to choose from.
     */
    private Table settingsTable;

    /**
     * Constructs a new instance of a settings selection page with the given
     * mode.
     * @param exportWizard Whether the preference selection if for an export
     * operation.
     */
    PreferenceImportExportSettingsSelectionPage(boolean exportWizard) {
        super(NAME, exportWizard);
    }

    /**
     * This page can always finish.  If no items are selected, it simply means
     * that nothing will be exported.
     * @return <code>true</code>.
     */
    boolean canFinish() {
        return true;
    }

    /**
     * This page is always the last page, and so you cannot flip to the next 
     * page.
     * @return <code>false</code>.
     */
    public boolean canFlipToNextPage() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Font parentFont = parent.getFont();
        final Composite page = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        page.setLayout(layout);
        initializeDialogUnits(page);

        // Set-up the title, subtitle and icon.
        if (export) {
            setTitle(EXPORT_TITLE);
            setMessage(EXPORT_MESSAGE);
            setImageDescriptor(getImageDescriptor("wizban/export_wiz.gif")); //$NON-NLS-1$
        } else {
            setTitle(IMPORT_TITLE);
            setMessage(IMPORT_MESSAGE);
            setImageDescriptor(getImageDescriptor("wizban/import_wiz.gif")); //$NON-NLS-1$
        }

        GridData layoutData;

        // Set-up the table and its columns.
        settingsTable = new Table(page, SWT.CHECK | SWT.BORDER);
        settingsTable.setFont(parentFont);
        layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.verticalSpan = 3;
        settingsTable.setLayoutData(layoutData);
        settingsTable.setLinesVisible(true);
        settingsTable.setHeaderVisible(true);
        final TableColumn columnChecked = new TableColumn(settingsTable,
                SWT.LEFT, 0);
        final TableColumn columnName = new TableColumn(settingsTable, SWT.LEFT,
                1);
        final TableColumn columnValue = new TableColumn(settingsTable,
                SWT.LEFT, 2);
        for (int i = 0; i < 50; i++) {
            TableItem item = new TableItem(settingsTable, SWT.NULL);
            item
                    .setText(new String[] {
                            "", "org.eclipse.sample.preference", "Sample value (ignore)" }); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            item.setChecked(true);
        }

        columnName.setText(WorkbenchMessages
                .getString("ImportExportPages.name")); //$NON-NLS-1$
        columnValue.setText(WorkbenchMessages
                .getString("ImportExportPages.value")); //$NON-NLS-1$
        columnChecked.pack();
        columnName.pack();
        columnValue.pack();

        // Set-up the select all button.
        final Button selectAllButton = new Button(page, SWT.PUSH);
        selectAllButton.setFont(parentFont);
        layoutData = new GridData();
        selectAllButton.setText(WorkbenchMessages
                .getString("ImportExportPages.selectAll")); //$NON-NLS-1$
        layoutData.widthHint = computePushButtonWidthHint(selectAllButton);
        layoutData.verticalAlignment = GridData.BEGINNING;
        selectAllButton.setLayoutData(layoutData);
        selectAllButton.addSelectionListener(new SelectionAdapter() {
            public final void widgetSelected(SelectionEvent event) {
                setCheckAll(true);
            }
        });

        // Set-up the deselect all button.
        final Button deselectAllButton = new Button(page, SWT.PUSH);
        deselectAllButton.setFont(parentFont);
        layoutData = new GridData();
        deselectAllButton.setText(WorkbenchMessages
                .getString("ImportExportPages.deselectAll")); //$NON-NLS-1$
        layoutData.widthHint = computePushButtonWidthHint(deselectAllButton);
        layoutData.verticalAlignment = GridData.BEGINNING;
        deselectAllButton.setLayoutData(layoutData);
        deselectAllButton.addSelectionListener(new SelectionAdapter() {
            public final void widgetSelected(SelectionEvent event) {
                setCheckAll(false);
            }
        });

        // Set-up the invert selection button.
        final Button invertSelectionButton = new Button(page, SWT.PUSH);
        invertSelectionButton.setFont(parentFont);
        layoutData = new GridData();
        invertSelectionButton.setText(WorkbenchMessages
                .getString("ImportExportPages.invertSelection")); //$NON-NLS-1$
        layoutData.widthHint = computePushButtonWidthHint(invertSelectionButton);
        layoutData.verticalAlignment = GridData.BEGINNING;
        invertSelectionButton.setLayoutData(layoutData);
        invertSelectionButton.addSelectionListener(new SelectionAdapter() {
            public final void widgetSelected(SelectionEvent event) {
                invertSelection();
            }
        });

        // Remember the composite as the top-level control.
        setControl(page);

        // Restore all the controls to their previous values.
        init();
    }

    /**
     * Initializes all of the controls to their previous values.  This page
     * doesn't really remember anything right now, so this just checks all of
     * the table items.
     */
    private void init() {
        setCheckAll(true);
    }

    /**
     * Inverts the current selection (checked state) of all the settings.
     */
    private void invertSelection() {
        final TableItem[] items = settingsTable.getItems();
        for (int i = 0; i < items.length; i++) {
            TableItem item = items[i];
            item.setChecked(!item.getChecked());
        }
    }

    /**
     * This sets the checked state on all the settings to the given value.  This
     * can be used as either a select all or a deselect all. 
     * @param checked The state to set.
     */
    private void setCheckAll(final boolean checked) {
        TableItem[] items = settingsTable.getItems();
        for (int i = 0; i < items.length; i++) {
            items[i].setChecked(checked);
        }
    }

    boolean validate() {
        return true;
    }
}