/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *		IBM Corporation - initial API and implementation 
 *  	Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 * 		font should be activated and used by other components.
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.about.AboutBundleData;
import org.eclipse.ui.internal.about.AboutData;
import org.eclipse.ui.internal.util.BundleUtility;
import org.osgi.framework.Bundle;

/**
 * Displays information about the product plugins.
 *
 * PRIVATE
 *	this class is internal to the ide
 */
public class AboutPluginsDialog extends ProductInfoDialog {

    /**
     * Table height in dialog units (value 200).
     */
    private static final int TABLE_HEIGHT = 200;

	private static final IPath baseNLPath = new Path("$nl$"); //$NON-NLS-1$

    private static final String PLUGININFO = "about.html"; //$NON-NLS-1$

    private final static int MORE_ID = IDialogConstants.CLIENT_ID + 1;

    private Table vendorInfo;

    private Button moreInfo;

    private String title;

    private String message;

    private String helpContextId;

    private String columnTitles[] = {
            WorkbenchMessages.AboutPluginsDialog_provider,
            WorkbenchMessages.AboutPluginsDialog_pluginName,
            WorkbenchMessages.AboutPluginsDialog_version, 
            WorkbenchMessages.AboutPluginsDialog_pluginId, 
    };

    private String productName;

    private AboutBundleData[] bundleInfos;

    private int lastColumnChosen = 0; // initially sort by provider

    private boolean reverseSort = false; // initially sort ascending

    private AboutBundleData lastSelection = null;

    /**
     * Constructor for AboutPluginsDialog.
     * 
     * @param parentShell the parent shell
     * @param productName the product name
     */
    public AboutPluginsDialog(Shell parentShell, String productName) {
        this(parentShell, productName, WorkbenchPlugin.getDefault()
                .getBundles(), null, null, IWorkbenchHelpContextIds.ABOUT_PLUGINS_DIALOG);
    }

    /**
     * Constructor for AboutPluginsDialog.
     * 
     * @param parentShell 
     * 			  the parent shell
     * @param productName
     *            must not be null
     * @param bundles
     *            must not be null
     * @param title 
     *            the title
     * @param message 
     * 			  the message
     * @param helpContextId 
     *            the help context id
     */
    public AboutPluginsDialog(Shell parentShell, String productName,
            Bundle[] bundles, String title, String message, String helpContextId) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        this.title = title;
        this.message = message;
        this.helpContextId = helpContextId;
        this.productName = productName;

        // create a data object for each bundle, remove duplicates, and include only resolved bundles (bug 65548)
        Map map = new HashMap();
        for (int i = 0; i < bundles.length; ++i) {
            AboutBundleData data = new AboutBundleData(bundles[i]);
            if (BundleUtility.isReady(data.getState()) && !map.containsKey(data.getVersionedId()))
                map.put(data.getVersionedId(), data);
        }
        bundleInfos = (AboutBundleData[]) map.values().toArray(
                new AboutBundleData[0]);

        AboutData.sortByProvider(reverseSort, bundleInfos);
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected void buttonPressed(int buttonId) {
        switch (buttonId) {
        case MORE_ID:
            handleMoreInfoPressed();
            break;
        default:
            super.buttonPressed(buttonId);
            break;
        }
    }

    /*
     * (non-Javadoc) Method declared on Window.
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        if (title == null && productName != null)
            title = NLS.bind(WorkbenchMessages.AboutPluginsDialog_shellTitle, productName );

        if (title != null)
            newShell.setText(title);

        PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell,
				helpContextId);
    }

    /**
     * Add buttons to the dialog's button bar.
     * 
     * Subclasses should override.
     * 
     * @param parent
     *            the button bar composite
     */
    protected void createButtonsForButtonBar(Composite parent) {
        parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        moreInfo = createButton(parent, MORE_ID, WorkbenchMessages.AboutPluginsDialog_moreInfo, false); 
        moreInfo.setEnabled(tableHasSelection() && selectionHasInfo());

        Label l = new Label(parent, SWT.NONE);
        l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = (GridLayout) parent.getLayout();
        layout.numColumns++;
        layout.makeColumnsEqualWidth = false;

        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
    }

    /**
     * Create the contents of the dialog (above the button bar).
     * 
     * Subclasses should overide.
     * 
     * @param parent
     *            the parent composite to contain the dialog area
     * @return the dialog area control
     */
    protected Control createDialogArea(Composite parent) {
        Composite outer = (Composite) super.createDialogArea(parent);

        if (message != null) {
            Label label = new Label(outer, SWT.NONE);
            label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            label.setFont(parent.getFont());
            label.setText(message);
        }

        createTable(outer);

        return outer;
    }

    /**
     * Create the table part of the dialog.
     * 
     * @param parent
     *            the parent composite to contain the dialog area
     */
    protected void createTable(Composite parent) {
        vendorInfo = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
                | SWT.FULL_SELECTION | SWT.BORDER);
        vendorInfo.setHeaderVisible(true);
        vendorInfo.setLinesVisible(true);
        vendorInfo.setFont(parent.getFont());
        vendorInfo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                // enable if there is an item selected and that
                // item has additional info
                moreInfo.setEnabled(e.item != null && selectionHasInfo());
            }
        });

        int[] columnWidths = { convertHorizontalDLUsToPixels(120),
                convertHorizontalDLUsToPixels(120),
                convertHorizontalDLUsToPixels(70),
                convertHorizontalDLUsToPixels(130) };

        // create table headers
        for (int i = 0; i < columnTitles.length; i++) {
            TableColumn column = new TableColumn(vendorInfo, SWT.NULL);
            column.setWidth(columnWidths[i]);
            column.setText(columnTitles[i]);
            final int columnIndex = i;
            column.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    sort(columnIndex);
                }
            });
        }

        // create a row for each member of the bundleInfo array
        for (int i = 0; i < bundleInfos.length; ++i) {
            TableItem item = new TableItem(vendorInfo, SWT.NULL);
            item.setText(createRow(bundleInfos[i]));
            item.setData(bundleInfos[i]);
        }

        GridData gridData = new GridData(GridData.FILL, GridData.FILL, true,
                true);
        gridData.heightHint = convertVerticalDLUsToPixels(TABLE_HEIGHT);
        vendorInfo.setLayoutData(gridData);
    }

    /**
     * Check if the currently selected plugin has additional information to
     * show.
     * 
     * @return true if the selected plugin has additional info available to
     *         display
     */
    private boolean selectionHasInfo() {
        TableItem[] items = vendorInfo.getSelection();
        if (items.length <= 0)
            return false;

        AboutBundleData bundleInfo = bundleInfos[vendorInfo.getSelectionIndex()];
        URL infoURL = getMoreInfoURL(bundleInfo, false);

        // only report ini problems if the -debug command line argument is used
        if (infoURL == null && WorkbenchPlugin.DEBUG)
            WorkbenchPlugin.log("Problem reading plugin info for: " //$NON-NLS-1$
                    + bundleInfo.getName());

        return infoURL != null;
    }

    /**
     * Create the button to provide more info on the selected plugin.
     * 
     * @return true if there is an item selected in the table, false otherwise
     */
    private boolean tableHasSelection() {
        return vendorInfo == null ? false : vendorInfo.getSelectionCount() > 0;
    }

    /** 
     * The More Info button was pressed.  Open a browser showing the license information
     * for the selected bundle or an error dialog if the browser cannot be opened.
     */
    protected void handleMoreInfoPressed() {
        if (vendorInfo == null)
            return;

        TableItem[] items = vendorInfo.getSelection();
        if (items.length <= 0)
            return;

        AboutBundleData bundleInfo = (AboutBundleData) items[0].getData();
        if (bundleInfo == null)
            return;

        if (!openBrowser(getMoreInfoURL(bundleInfo, true)))
            MessageDialog.openError(getShell(), WorkbenchMessages.AboutPluginsDialog_errorTitle,
                    NLS.bind(WorkbenchMessages.AboutPluginsDialog_unableToOpenFile,PLUGININFO, bundleInfo.getId()));
    }

    /**
     * Sort the rows of the table based on the selected column.
     * 
     * @param column
     *            index of table column selected as sort criteria
     */
    private void sort(int column) {
        if (lastColumnChosen == column)
            reverseSort = !reverseSort;
        else {
            reverseSort = false;
            lastColumnChosen = column;
        }

        if (vendorInfo.getItemCount() <= 1)
            return;

        int sel = vendorInfo.getSelectionIndex();
        if (sel != -1)
            lastSelection = bundleInfos[sel];

        switch (column) {
        case 0:
            AboutData.sortByProvider(reverseSort, bundleInfos);
            break;
        case 1:
            AboutData.sortByName(reverseSort, bundleInfos);
            break;
        case 2:
            AboutData.sortByVersion(reverseSort, bundleInfos);
            break;
        case 3:
            AboutData.sortById(reverseSort, bundleInfos);
            break;
        }

        refreshTable(column);
    }

    /**
     * Refresh the rows of the table based on the selected column. Maintain
     * selection from before sort action request.
     */
    private void refreshTable(int col) {
        TableItem[] items = vendorInfo.getItems();

        // create new order of table items
        for (int i = 0; i < items.length; i++) {
            items[i].setText(createRow(bundleInfos[i]));
            items[i].setData(bundleInfos[i]);
        }

        // maintain the original selection
        int sel = -1;
        if (lastSelection != null) {
            String oldId = lastSelection.getId();
            for (int k = 0; k < bundleInfos.length; k++)
                if (oldId.equalsIgnoreCase(bundleInfos[k].getId()))
                    sel = k;

            vendorInfo.setSelection(sel);
            vendorInfo.showSelection();
        }

        moreInfo.setEnabled(tableHasSelection() && selectionHasInfo());
    }

    /**
     * Return an array of strings containing the argument's information in the
     * proper order for this table's columns.
     * 
     * @param info
     *            the source information for the new row, must not be null
     */
    private static String[] createRow(AboutBundleData info) {
        return new String[] { info.getProviderName(), info.getName(),
                info.getVersion(), info.getId() };
    }

    /**
     * Return an url to the plugin's about.html file (what is shown when "More info" is
     * pressed) or null if no such file exists.  The method does nl lookup to allow for
     * i18n.
     * 
     * @param bundleInfo the bundle info
     * @param makeLocal whether to make the about content local
     * @return the url or <code>null</code>
     */
    private URL getMoreInfoURL(AboutBundleData bundleInfo, boolean makeLocal) {
        Bundle bundle = Platform.getBundle(bundleInfo.getId());
        if (bundle == null)
            return null;

        URL aboutUrl = Platform.find(bundle, baseNLPath.append(PLUGININFO), null);
        if (!makeLocal) {
            return aboutUrl;
        }
		if (aboutUrl != null) {
		    try {
				URL result = Platform.asLocalURL(aboutUrl);
				try {
				    // Make local all content in the "about" directory.
				    // This is needed to handle jar'ed plug-ins.
				    // See Bug 88240 [About] About dialog needs to extract subdirs.
					URL about = new URL(aboutUrl, "about_files"); //$NON-NLS-1$
					if (about != null)
						Platform.asLocalURL(about);
				} catch (IOException e) {
					// skip the about dir if its not found or there are other problems.
				}
				return result;
		    } catch(IOException e) {
		        // do nothing
		    }
        }
		return null;
    }
}
