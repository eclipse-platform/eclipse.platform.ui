/******************************************************************************* 
 * Copyright (c) 2000, 2004 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *      IBM Corporation - initial API and implementation 
 *  	Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 *      font should be activated and used by other components.
************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.ide.AboutBundleGroupData;
import org.eclipse.ui.internal.ide.AboutData;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IHelpContextIds;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.configuration.IInstallConfiguration;
import org.eclipse.update.configuration.ILocalSite;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IFeatureReference;
import org.eclipse.update.core.IURLEntry;
import org.eclipse.update.core.SiteManager;
import org.osgi.framework.Bundle;

/**
 * Displays information about the product plugins.
 *
 * @private
 *		This class is internal to the workbench and must not be called outside
 *		the workbench.
 */
public class AboutFeaturesDialog extends ProductInfoDialog {

    /**
     * Table height in dialog units (value 150).
     */
    private static final int TABLE_HEIGHT = 150;
    private static final int INFO_HEIGHT = 100;
    private final static int MORE_ID = IDialogConstants.CLIENT_ID + 1;
    private final static int PLUGINS_ID = IDialogConstants.CLIENT_ID + 2;

	private Table table;
	private Label imageLabel;
	private StyledText text;
	private Composite infoArea;

	private Map cachedImages = new HashMap();

	private String columnTitles[] = {
	    IDEWorkbenchMessages.getString("AboutFeaturesDialog.provider"), //$NON-NLS-1$
		IDEWorkbenchMessages.getString("AboutFeaturesDialog.featureName"), //$NON-NLS-1$
		IDEWorkbenchMessages.getString("AboutFeaturesDialog.version"), //$NON-NLS-1$
		IDEWorkbenchMessages.getString("AboutFeaturesDialog.featureId"), //$NON-NLS-1$
	};

	private String productName;
	private AboutBundleGroupData[] bundleGroupInfos;

	private int lastColumnChosen = 0; // initially sort by provider
	private boolean reverseSort = false; // initially sort ascending
	private AboutBundleGroupData lastSelection = null;
	private Button moreButton;
	private Button pluginsButton;

	private static Map featuresMap;

	/**
	 * Constructor for AboutFeaturesDialog
	 */
	public AboutFeaturesDialog(Shell parentShell, String productName,
            AboutBundleGroupData[] bundleGroupInfos) {
        super(parentShell);
        setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);

        this.productName = productName;
        this.bundleGroupInfos = bundleGroupInfos;

        AboutData.sortByProvider(reverseSort, bundleGroupInfos);
    }

	/**
	 * The More Info button was pressed.  Open a browser with the license for the
	 * selected item or an information dialog if there is no license, or the browser
	 * cannot be opened. 
	 */
	private void handleMoreInfoPressed() {
        TableItem[] items = table.getSelection();
        if (items.length <= 0)
            return;

        AboutBundleGroupData info = (AboutBundleGroupData) items[0].getData();
        IFeature feature = getFeatureFor(info);
        IURLEntry entry = feature == null ? null : feature.getLicense();
        if (feature == null || entry == null || !openBrowser(entry.getURL())) {
            MessageDialog.openInformation(getShell(), IDEWorkbenchMessages
                    .getString("AboutFeaturesDialog.noInfoTitle"), //$NON-NLS-1$
                    IDEWorkbenchMessages
                            .getString("AboutFeaturesDialog.noInformation")); //$NON-NLS-1$
        }
    }

	/**
     * The Plugins button was pressed. Open an about dialog on the plugins for
     * the selected feature.
     */
    private void handlePluginInfoPressed() {
        TableItem[] items = table.getSelection();
        if (items.length <= 0)
            return;

        AboutBundleGroupData info = (AboutBundleGroupData) items[0].getData();
        IBundleGroup bundleGroup = info.getBundleGroup();
        Bundle[] bundles = bundleGroup == null ? new Bundle[0] : bundleGroup
                .getBundles();

        AboutPluginsDialog d = new AboutPluginsDialog(getShell(), productName,
                bundles, IDEWorkbenchMessages
                        .getString("AboutFeaturesDialog.pluginInfoTitle"), //$NON-NLS-1$
                IDEWorkbenchMessages.format(
                        "AboutFeaturesDialog.pluginInfoMessage", //$NON-NLS-1$
                        new Object[] { bundleGroup.getIdentifier()}),
                IHelpContextIds.ABOUT_FEATURES_PLUGINS_DIALOG);
        d.open();
    }

	/*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected void buttonPressed(int buttonId) {
        switch (buttonId) {
        case MORE_ID:
            handleMoreInfoPressed();
            break;
        case PLUGINS_ID:
            handlePluginInfoPressed();
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
        if (productName != null)
            newShell.setText(IDEWorkbenchMessages.format(
                    "AboutFeaturesDialog.shellTitle", //$NON-NLS-1$
                    new Object[] { productName }));

        WorkbenchHelp.setHelp(newShell, IHelpContextIds.ABOUT_FEATURES_DIALOG);
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

        moreButton = createButton(parent, MORE_ID, IDEWorkbenchMessages
                .getString("AboutFeaturesDialog.moreInfo"), false); //$NON-NLS-1$
        pluginsButton = createButton(parent, PLUGINS_ID, IDEWorkbenchMessages
                .getString("AboutFeaturesDialog.pluginsInfo"), false); //$NON-NLS-1$

        Label l = new Label(parent, SWT.NONE);
        l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = (GridLayout) parent.getLayout();
        layout.numColumns++;
        layout.makeColumnsEqualWidth = false;

        Button b = createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        b.setFocus();

        TableItem[] items = table.getSelection();
        if (items.length > 0)
            updateButtons((AboutBundleGroupData) items[0].getData());
    }

	/**
     * Create the contents of the dialog (above the button bar).
     * 
     * Subclasses should overide.
     * 
     * @param the
     *            parent composite to contain the dialog area
     * @return the dialog area control
     */
    protected Control createDialogArea(Composite parent) {
        setHandCursor(new Cursor(parent.getDisplay(), SWT.CURSOR_HAND));
        setBusyCursor(new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT));
        getShell().addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (getHandCursor() != null)
                    getHandCursor().dispose();
                if (getBusyCursor() != null)
                    getBusyCursor().dispose();
            }
        });

        Composite outer = (Composite) super.createDialogArea(parent);

        createTable(outer);
        createInfoArea(outer);

        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL,
                GridData.VERTICAL_ALIGN_FILL, true, true);
        gridData.heightHint = convertVerticalDLUsToPixels(TABLE_HEIGHT);
        table.setLayoutData(gridData);

        return outer;
    }

    /** 
	 * Create the info area containing the image and text
	 */
	protected void createInfoArea(Composite parent) {
		Font font = parent.getFont();

		infoArea = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		infoArea.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		int infoAreaHeight = convertVerticalDLUsToPixels(INFO_HEIGHT);
		data.heightHint = infoAreaHeight;
		infoArea.setLayoutData(data);

		imageLabel = new Label(infoArea, SWT.NONE);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		data.heightHint = 32;
		data.widthHint = 32;
		imageLabel.setLayoutData(data);
		imageLabel.setFont(font);

		// text on the right
		text = new StyledText(infoArea, SWT.MULTI | SWT.READ_ONLY);
		text.setCaret(null);
		text.setFont(parent.getFont());
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		data.grabExcessHorizontalSpace = true;
		data.heightHint = infoAreaHeight;
		text.setLayoutData(data);
		text.setFont(font);
		text.setCursor(null);
		text.setBackground(infoArea.getBackground());
		addListeners(text);

		TableItem[] items = table.getSelection();
		if (items.length > 0)
			updateInfoArea((AboutBundleGroupData) items[0].getData());
	}

	/**
	 * Create the table part of the dialog.
	 *
	 * @param the parent composite to contain the dialog area
	 */
	protected void createTable(Composite parent) {
        table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
                | SWT.FULL_SELECTION | SWT.BORDER);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setFont(parent.getFont());
        table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				AboutBundleGroupData info = (AboutBundleGroupData) e.item.getData();
				updateInfoArea(info);
				updateButtons(info);
			}
		});

    	int[] columnWidths = {
    	        convertHorizontalDLUsToPixels(120),
                convertHorizontalDLUsToPixels(120),
                convertHorizontalDLUsToPixels(70),
                convertHorizontalDLUsToPixels(130)
    	};

		for (int i = 0; i < columnTitles.length; i++) {
            TableColumn tableColumn = new TableColumn(table, SWT.NULL);
            tableColumn.setWidth(columnWidths[i]);
            tableColumn.setText(columnTitles[i]);
            final int columnIndex = i;
            tableColumn.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    sort(columnIndex);
                }
            });
        }

		// create a table row for each bundle group
		String selId = lastSelection == null ? null : lastSelection.getId();
		int sel = 0;
		for (int i = 0; i < bundleGroupInfos.length; i++) {
			if (bundleGroupInfos[i].getId().equals(selId))
			    sel = i;

			TableItem item = new TableItem(table, SWT.NULL);
			item.setText(createRow(bundleGroupInfos[i]));
			item.setData(bundleGroupInfos[i]);
		}

		// if an item was specified during construction, it should be
		// selected when the table is created
		if (bundleGroupInfos.length > 0) {
			table.setSelection(sel);
			table.showSelection();
		}
	}

	/**
     * @see Window#close()
     */
    public boolean close() {
        boolean ret = super.close();

        Iterator iter = cachedImages.values().iterator();
        while (iter.hasNext()) {
            Image image = (Image) iter.next();
            image.dispose();
        }

        return ret;
    }

	/**
     * Returns a mapping from feature id to feature.
     */
    private Map getFeaturesMap() {
        if (featuresMap != null) return featuresMap;

        featuresMap = new HashMap();

        final ILocalSite[] localSiteArray = new ILocalSite[1];
        BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
            public void run() {
                // this may take a few seconds
                try {
                    localSiteArray[0] = SiteManager.getLocalSite();
                } catch (CoreException e) {
                    MessageDialog
                    	.openError(
                    	        getShell(),
                                IDEWorkbenchMessages
                                	.getString("AboutFeaturesDialog.errorTitle"), //$NON-NLS-1$
                                IDEWorkbenchMessages
                                	.getString("AboutFeaturesDialog.unableToObtainFeatureInfo")); //$NON-NLS-1$
                }
            }
        });
        if (localSiteArray[0] == null)
            return featuresMap;

        IInstallConfiguration installConfiguration = localSiteArray[0]
                .getCurrentConfiguration();
        IConfiguredSite[] configuredSites = installConfiguration
                .getConfiguredSites();

        for (int i = 0; i < configuredSites.length; i++) {
            IFeatureReference[] featureReferences = configuredSites[i]
                    .getConfiguredFeatures();

            for (int j = 0; j < featureReferences.length; j++) {
                try {
                    IFeature feature = featureReferences[j]
                            .getFeature(new NullProgressMonitor());

                    String key = feature.getVersionedIdentifier().toString();
                    featuresMap.put(key, feature);
                } catch (CoreException e) {
                    // do nothing
                }
            }
        }

        return featuresMap;
    }

	/**
     * Return the feature for the given bundle group.
     */
    private IFeature getFeatureFor(AboutBundleGroupData info) {
        Map map = getFeaturesMap();
        if (map == null)
            return null;

        String key = info.getId() + "_" + info.getVersion(); //$NON-NLS-1$
        return (IFeature) map.get(key);
    }

	/**
     * Update the button enablement
     */
    private void updateButtons(AboutBundleGroupData info) {
        if (info == null) {
            moreButton.setEnabled(false);
            pluginsButton.setEnabled(false);
            return;
        }

        // Creating the feature map is too much just to determine enablement, so if
        // it doesn't already exist, just enable the buttons.  If this was the wrong
        // choice, then when the button is actually pressed an dialog will be opened. 
        if(featuresMap == null) {
            moreButton.setEnabled(true);
            pluginsButton.setEnabled(true);
            return;
        }

        IFeature feature = getFeatureFor(info);
        moreButton.setEnabled(feature != null && feature.getLicense() != null);
        pluginsButton.setEnabled(feature != null);
    }

	/**
     * Update the info area
     */
    private void updateInfoArea(AboutBundleGroupData info) {
        if (info == null) {
            imageLabel.setImage(null);
            text.setText(""); //$NON-NLS-1$
            return;
        }

        ImageDescriptor desc = info.getFeatureImage();
        Image image = (Image) cachedImages.get(desc);
        if (image == null && desc != null) {
            image = desc.createImage();
            cachedImages.put(desc, image);
        }
        imageLabel.setImage(image);

        String aboutText = info.getAboutText();
        setItem(null);
        if (aboutText != null)
            setItem(scan(aboutText));

        if (getItem() == null)
            text.setText(IDEWorkbenchMessages
                    .getString("AboutFeaturesDialog.noInformation")); //$NON-NLS-1$
        else {
            text.setText(getItem().getText());
            text.setCursor(null);
            setLinkRanges(text, getItem().getLinkRanges());
        }
    }

	/**
     * Select the initial selection
     */
    public void setInitialSelection(AboutBundleGroupData info) {
        lastSelection = info;
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

        if (table.getItemCount() <= 1)
            return;

        // Remember the last selection
        int sel = table.getSelectionIndex();
        if (sel != -1)
            lastSelection = bundleGroupInfos[sel];

        switch (column) {
        case 0:
            AboutData.sortByProvider(reverseSort, bundleGroupInfos);
            break;
        case 1:
            AboutData.sortByName(reverseSort, bundleGroupInfos);
            break;
        case 2:
            AboutData.sortByVersion(reverseSort, bundleGroupInfos);
            break;
        case 3:
            AboutData.sortById(reverseSort, bundleGroupInfos);
            break;
        }

        refreshTable(column);
    }

	/**
     * Refresh the rows of the table based on the selected column. Maintain
     * selection from before sort action request.
     */
    private void refreshTable(int col) {
        TableItem[] items = table.getItems();

        // create new order of table items
        for (int i = 0; i < items.length; i++) {
            items[i].setText(createRow(bundleGroupInfos[i]));
            items[i].setData(bundleGroupInfos[i]);
        }

        // Maintain the original selection
        int sel = -1;
        if (lastSelection != null) {
            String oldId = lastSelection.getId();
            for (int k = 0; k < bundleGroupInfos.length; k++)
                if (oldId.equalsIgnoreCase(bundleGroupInfos[k].getId()))
                    sel = k;

            table.setSelection(sel);
            table.showSelection();
        }

        updateInfoArea(lastSelection);
    }

    /**
     * Return an array of strings containing the argument's information in the
     * proper order for this table's columns.
     * 
     * @param info
     *            the source information for the new row, must not be null
     */
    private static String[] createRow(AboutBundleGroupData info) {
        return new String[] { info.getProviderName(), info.getName(),
                info.getVersion(), info.getId()};
    }
}
