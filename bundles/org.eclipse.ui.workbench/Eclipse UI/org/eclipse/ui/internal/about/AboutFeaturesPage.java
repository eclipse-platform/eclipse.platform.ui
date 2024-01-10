/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *  	Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 *      font should be activated and used by other components.
 *      Remy Chi Jian Suen <remy.suen@gmail.com> - Fix for bug 265739 -
 *      [About] 'Features' tab in 'Eclipse SDK Installation Details' dialog
 *      does not display sort direction
 *******************************************************************************/
package org.eclipse.ui.internal.about;

import static org.eclipse.swt.events.SelectionListener.widgetDefaultSelectedAdapter;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.ConfigureColumns;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.AboutPluginsDialog;
import org.osgi.framework.Bundle;

/**
 * Displays information about the product plugins.
 *
 * PRIVATE This class is internal to the workbench and must not be called
 * outside the workbench.
 */
public class AboutFeaturesPage extends ProductInfoPage {

	public class BundleGroupTableLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof AboutBundleGroupData data) {

				return switch (columnIndex) {
				case 0 -> data.getProviderName();
				case 1 -> data.getName();
				case 2 -> data.getVersion();
				case 3 -> data.getId();
				default -> ""; //$NON-NLS-1$
				};
			}
			return ""; //$NON-NLS-1$
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}

	// used as the page id when this page is launched in its own dialog
	private static final String ID = "productInfo.features"; //$NON-NLS-1$
	/**
	 * Table height in dialog units (value 150).
	 */
	private static final int TABLE_HEIGHT = 150;

	private static final int INFO_HEIGHT = 100;

	private static final int MORE_ID = IDialogConstants.CLIENT_ID + 1;

	private static final int PLUGINS_ID = IDialogConstants.CLIENT_ID + 2;

	private static final int COLUMNS_ID = IDialogConstants.CLIENT_ID + 3;

	private Table table;

	private Label imageLabel;

	private StyledText text;

	private AboutTextManager textManager;

	private Composite infoArea;

	private Map<ImageDescriptor, Image> cachedImages = new HashMap<>();

	private AboutBundleGroupData[] bundleGroupInfos;

	private String columnTitles[] = { WorkbenchMessages.AboutFeaturesDialog_provider,
			WorkbenchMessages.AboutFeaturesDialog_featureName, WorkbenchMessages.AboutFeaturesDialog_version,
			WorkbenchMessages.AboutFeaturesDialog_featureId, };

	private int lastColumnChosen = 0; // initially sort by provider

	private boolean reverseSort = false; // initially sort ascending

	private AboutBundleGroupData lastSelection = null;

	private Button pluginsButton, moreButton;

	private static Map<?, ?> featuresMap;

	public void setBundleGroupInfos(AboutBundleGroupData[] bundleGroupInfos) {
		this.bundleGroupInfos = bundleGroupInfos;
	}

	@Override
	String getId() {
		return ID;
	}

	private void initializeBundleGroupInfos() {
		if (bundleGroupInfos == null) {
			IBundleGroupProvider[] providers = Platform.getBundleGroupProviders();

			// create a descriptive object for each BundleGroup
			LinkedList<AboutBundleGroupData> groups = new LinkedList<>();
			if (providers != null) {
				for (IBundleGroupProvider provider : providers) {
					IBundleGroup[] bundleGroups = provider.getBundleGroups();
					for (IBundleGroup bundleGroup : bundleGroups) {
						groups.add(new AboutBundleGroupData(bundleGroup));
					}
				}
			}
			bundleGroupInfos = groups.toArray(new AboutBundleGroupData[0]);
		} else {
			// the order of the array may be changed due to sorting, so create a
			// copy, since the client set this value.
			AboutBundleGroupData[] clientArray = bundleGroupInfos;
			bundleGroupInfos = new AboutBundleGroupData[clientArray.length];
			System.arraycopy(clientArray, 0, bundleGroupInfos, 0, clientArray.length);
		}
		AboutData.sortByProvider(reverseSort, bundleGroupInfos);
	}

	/**
	 * The Plugins button was pressed. Open an about dialog on the plugins for the
	 * selected feature.
	 */
	private void handlePluginInfoPressed() {
		TableItem[] items = table.getSelection();
		if (items.length <= 0) {
			return;
		}

		AboutBundleGroupData info = (AboutBundleGroupData) items[0].getData();
		IBundleGroup bundleGroup = info.getBundleGroup();
		Bundle[] bundles = bundleGroup == null ? new Bundle[0] : bundleGroup.getBundles();

		AboutPluginsDialog d = new AboutPluginsDialog(getShell(), getProductName(), bundles,
				WorkbenchMessages.AboutFeaturesDialog_pluginInfoTitle,
				NLS.bind(WorkbenchMessages.AboutFeaturesDialog_pluginInfoMessage, bundleGroup.getIdentifier()),
				IWorkbenchHelpContextIds.ABOUT_FEATURES_PLUGINS_DIALOG);
		d.open();
	}

	@Override
	public void createPageButtons(Composite parent) {
		moreButton = createButton(parent, MORE_ID, WorkbenchMessages.AboutFeaturesDialog_moreInfo);
		pluginsButton = createButton(parent, PLUGINS_ID, WorkbenchMessages.AboutFeaturesDialog_pluginsInfo);
		createButton(parent, COLUMNS_ID, WorkbenchMessages.AboutFeaturesDialog_columns);
		TableItem[] items = table.getSelection();
		AboutBundleGroupData info = null;
		if (items.length > 0) {
			info = (AboutBundleGroupData) items[0].getData();
		}
		updateButtons(info);
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		parent.getShell().addDisposeListener(arg0 -> disposeImages());
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IWorkbenchHelpContextIds.ABOUT_FEATURES_DIALOG);

		Composite outer = createOuterComposite(parent);

		createTable(outer);
		createInfoArea(outer);
		setControl(outer);
	}

	/**
	 * Create the info area containing the image and text
	 */
	protected void createInfoArea(Composite parent) {
		Font font = parent.getFont();

		infoArea = new Composite(parent, SWT.BORDER);
		infoArea.setBackground(infoArea.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		infoArea.setBackgroundMode(SWT.INHERIT_FORCE);
		GridData data = new GridData(GridData.FILL, GridData.FILL, true, false);
		// need to provide space for arbitrary feature infos, not just the
		// one selected by default
		data.heightHint = convertVerticalDLUsToPixels(INFO_HEIGHT);
		infoArea.setLayoutData(data);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		infoArea.setLayout(layout);

		imageLabel = new Label(infoArea, SWT.NONE);
		data = new GridData(GridData.FILL, GridData.BEGINNING, false, false);
		data.widthHint = 32;
		data.heightHint = 32;
		imageLabel.setLayoutData(data);
		imageLabel.setFont(font);

		// text on the right
		text = new StyledText(infoArea, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL);
		text.setAlwaysShowScrollBars(false);

		// Don't set caret to 'null' as this causes https://bugs.eclipse.org/293263.
//		text.setCaret(null);

		text.setFont(parent.getFont());
		data = new GridData(GridData.FILL, GridData.FILL, true, true);
		text.setLayoutData(data);
		text.setFont(font);
		text.setCursor(null);

		textManager = new AboutTextManager(text);

		TableItem[] items = table.getSelection();
		if (items.length > 0) {
			updateInfoArea((AboutBundleGroupData) items[0].getData());
		}
	}

	/**
	 * Create the table part of the dialog.
	 *
	 * @param parent the parent composite to contain the dialog area
	 */
	protected void createTable(Composite parent) {

		final Text filterText = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
		filterText.setLayoutData(GridDataFactory.fillDefaults().create());
		filterText.setMessage(WorkbenchMessages.AboutPluginsDialog_filterTextMessage);
		filterText.setFocus();

		initializeBundleGroupInfos();

		TableViewer tableViewer = new TableViewer(parent,
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);

		table = tableViewer.getTable();
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.heightHint = convertVerticalDLUsToPixels(TABLE_HEIGHT);
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);

		table.setLinesVisible(true);
		table.setFont(parent.getFont());
		table.addSelectionListener(widgetSelectedAdapter(e -> {
			// If there is no item, nothing we can do.
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=266177
			if (e.item == null)
				return;
			AboutBundleGroupData info = (AboutBundleGroupData) e.item.getData();
			updateInfoArea(info);
			updateButtons(info);
		}));
		table.addSelectionListener(widgetDefaultSelectedAdapter(e -> {
			handlePluginInfoPressed();
		}));

		int[] columnWidths = { convertHorizontalDLUsToPixels(120), convertHorizontalDLUsToPixels(120),
				convertHorizontalDLUsToPixels(70), convertHorizontalDLUsToPixels(130) };

		for (int i = 0; i < columnTitles.length; i++) {
			TableColumn tableColumn = new TableColumn(table, SWT.NULL);
			tableColumn.setWidth(columnWidths[i]);
			tableColumn.setText(columnTitles[i]);
			final int columnIndex = i;
			tableColumn.addSelectionListener(widgetSelectedAdapter(e -> sort(columnIndex)));
		}

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setLabelProvider(new BundleGroupTableLabelProvider());

		final BundlePatternFilter searchFilter = new BundlePatternFilter();
		filterText.addModifyListener(e -> {
			searchFilter.setPattern(filterText.getText());
			table.setRedraw(false);
			try {
				tableViewer.refresh();
			} finally {
				table.setRedraw(true);
			}
		});
		tableViewer.addFilter(searchFilter);

		Map<String, AboutBundleGroupData> features = new HashMap<>();

		for (AboutBundleGroupData data : bundleGroupInfos) {
			features.put(data.getVersionedId(), data);
		}
		tableViewer.setInput(features.values());
		// tableViewer.refresh(false);
		addCopySupport(table);
	}

	private void disposeImages() {
		Iterator<Image> iter = cachedImages.values().iterator();
		while (iter.hasNext()) {
			Image image = iter.next();
			image.dispose();
		}
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

		// Creating the feature map is too much just to determine enablement, so
		// if
		// it doesn't already exist, just enable the buttons. If this was the
		// wrong
		// choice, then when the button is actually pressed an dialog will be
		// opened.
		if (featuresMap == null) {
			moreButton.setEnabled(true);
			pluginsButton.setEnabled(true);
			return;
		}

		moreButton.setEnabled(info.getLicenseUrl() != null);
		pluginsButton.setEnabled(true);
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
		Image image = cachedImages.get(desc);
		if (image == null && desc != null) {
			image = desc.createImage();
			cachedImages.put(desc, image);
		}
		imageLabel.setImage(image);

		String aboutText = info.getAboutText();
		textManager.setItem(null);
		if (aboutText != null) {
			textManager.setItem(AboutUtils.scan(aboutText));
		}

		if (textManager.getItem() == null) {
			text.setText(WorkbenchMessages.AboutFeaturesDialog_noInformation);
		}
	}

	/**
	 * Select the initial selection
	 *
	 * @param info the info
	 */
	public void setInitialSelection(AboutBundleGroupData info) {
		lastSelection = info;
	}

	/**
	 * Sort the rows of the table based on the selected column.
	 *
	 * @param column index of table column selected as sort criteria
	 */
	private void sort(int column) {
		if (lastColumnChosen == column) {
			reverseSort = !reverseSort;
		} else {
			reverseSort = false;
			lastColumnChosen = column;
		}

		if (table.getItemCount() <= 1) {
			return;
		}

		// Remember the last selection
		int sel = table.getSelectionIndex();
		if (sel != -1) {
			lastSelection = bundleGroupInfos[sel];
		}

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
		// set the sort column and directional indicator
		table.setSortColumn(table.getColumn(column));
		table.setSortDirection(reverseSort ? SWT.DOWN : SWT.UP);

		refreshTable();
	}

	/**
	 * Refresh the rows of the table based on the selected column. Maintain
	 * selection from before sort action request.
	 */
	private void refreshTable() {
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
			for (int k = 0; k < bundleGroupInfos.length; k++) {
				if (oldId.equalsIgnoreCase(bundleGroupInfos[k].getId())) {
					sel = k;
				}
			}

			table.setSelection(sel);
			table.showSelection();
		}

		updateInfoArea(lastSelection);
	}

	/**
	 * Return an array of strings containing the argument's information in the
	 * proper order for this table's columns.
	 *
	 * @param info the source information for the new row, must not be null
	 */
	private static String[] createRow(AboutBundleGroupData info) {
		return new String[] { info.getProviderName(), info.getName(), info.getVersion(), info.getId() };
	}

	protected Collection<Object> getSelectionValue() {
		if (table == null || table.isDisposed())
			return null;
		TableItem[] items = table.getSelection();
		if (items.length <= 0) {
			return null;
		}
		ArrayList<Object> list = new ArrayList<>(1);
		list.add(items[0].getData());
		return list;
	}

	private void handleColumnsPressed() {
		ConfigureColumns.forTable(table, this);
	}

	/**
	 * The More Info button was pressed. Open a browser with the license for the
	 * selected item or an information dialog if there is no license, or the browser
	 * cannot be opened.
	 */
	private void handleMoreInfoPressed() {
		TableItem[] items = table.getSelection();
		if (items.length <= 0) {
			return;
		}

		AboutBundleGroupData info = (AboutBundleGroupData) items[0].getData();
		if (info == null || !AboutUtils.openBrowser(getShell(), info.getLicenseUrl())) {
			MessageDialog.openInformation(getShell(), WorkbenchMessages.AboutFeaturesDialog_noInfoTitle,
					WorkbenchMessages.AboutFeaturesDialog_noInformation);
		}
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case MORE_ID:
			handleMoreInfoPressed();
			break;
		case PLUGINS_ID:
			handlePluginInfoPressed();
			break;
		case COLUMNS_ID:
			handleColumnsPressed();
			break;
		default:
			super.buttonPressed(buttonId);
			break;
		}
	}
}

