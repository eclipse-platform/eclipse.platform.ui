/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Feb 9, 2004
 *  
 */
package org.eclipse.ui.internal.navigator.filters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonFilterDescriptor;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.INavigatorViewerDescriptor;

/**
 * 
 * @since 3.2
 * 
 */
public class CommonFilterSelectionDialog extends Dialog {
  
	private static final int TAB_WIDTH_IN_DLUS = 200;

	private static final int TAB_HEIGHT_IN_DLUS = 150;

	private final CommonViewer commonViewer;

	private final INavigatorContentService contentService;

	private TabFolder customizationsTabFolder;

	private CommonFiltersTab commonFiltersTab;

	private ContentExtensionsTab contentExtensionsTab;

	private Text descriptionText;

	private ISelectionChangedListener updateDescriptionSelectionListener;

	protected CommonFilterSelectionDialog(CommonViewer aCommonViewer) {
		super(aCommonViewer.getControl().getShell());
		setShellStyle(SWT.RESIZE | getShellStyle());

		commonViewer = aCommonViewer;
		contentService = commonViewer.getNavigatorContentService();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		getShell()
				.setText(
						CommonNavigatorMessages.CommonFilterSelectionDialog_Available_customization_);
		 
		
		Composite superComposite = (Composite) super.createDialogArea(parent);
		 
		createCustomizationsTabFolder(superComposite); 
		
		commonFiltersTab = new CommonFiltersTab(customizationsTabFolder,
				contentService);
		createTabItem(
				customizationsTabFolder,
				CommonNavigatorMessages.CommonFilterSelectionDialog_Available_Filters,
				commonFiltersTab);

		boolean hideExtensionsTab = contentService.getViewerDescriptor()
				.getBooleanConfigProperty(
						INavigatorViewerDescriptor.PROP_HIDE_AVAILABLE_EXT_TAB);

		if (!hideExtensionsTab) { 
			contentExtensionsTab = new ContentExtensionsTab(
					customizationsTabFolder, contentService);

			createTabItem(
					customizationsTabFolder,
					CommonNavigatorMessages.CommonFilterSelectionDialog_Available_Content,
					contentExtensionsTab);
		}

		createDescriptionText(superComposite);

		if (commonFiltersTab != null) {
			commonFiltersTab.addSelectionChangedListener(getSelectionListener());
		}

		if (contentExtensionsTab != null) {
			contentExtensionsTab
					.addSelectionChangedListener(getSelectionListener());
		}

		return customizationsTabFolder;
	}

	private void createCustomizationsTabFolder(Composite superComposite) {
		customizationsTabFolder = new TabFolder(superComposite, SWT.RESIZE);
 
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = convertHorizontalDLUsToPixels(TAB_WIDTH_IN_DLUS);
		gd.heightHint = convertVerticalDLUsToPixels(TAB_HEIGHT_IN_DLUS);
		
		customizationsTabFolder.setLayout(new GridLayout());
		customizationsTabFolder.setLayoutData(gd);

		customizationsTabFolder.setFont(superComposite.getFont());

		customizationsTabFolder.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (descriptionText != null) {
					descriptionText.setText(""); //$NON-NLS-1$
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}

		});
	}

	private void createTabItem(TabFolder aTabFolder, String label,
			Composite composite) {
		TabItem extensionsTabItem = new TabItem(aTabFolder, SWT.NONE);
		extensionsTabItem.setText(label);
 		extensionsTabItem.setControl(composite);
	}

	private void createDescriptionText(Composite composite) {

		descriptionText = new Text(composite, SWT.WRAP | SWT.V_SCROLL
				| SWT.BORDER);
		descriptionText.setFont(composite.getFont());
		descriptionText.setBackground(composite.getBackground());
		GridData descriptionTextGridData = new GridData(
				GridData.FILL_HORIZONTAL);
		descriptionTextGridData.heightHint = convertHeightInCharsToPixels(3);
		descriptionText.setLayoutData(descriptionTextGridData);
	}

	private ISelectionChangedListener getSelectionListener() {
		if (updateDescriptionSelectionListener == null) {
			updateDescriptionSelectionListener = new FilterDialogSelectionListener(
					descriptionText);
		}
		return updateDescriptionSelectionListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {

		String[] contentExtensionIdsToActivate = new String[0];
		if (contentExtensionsTab != null) {
			List checkedExtensions = new ArrayList();
			TableItem[] tableItems = contentExtensionsTab.getTable().getItems();
			INavigatorContentDescriptor descriptor;
			for (int i = 0; i < tableItems.length; i++) {
				descriptor = (INavigatorContentDescriptor) tableItems[i]
						.getData();

				if (tableItems[i].getChecked()) {
					checkedExtensions.add(descriptor.getId());
				}
			}
			if (checkedExtensions.size() != 0) {
				contentExtensionIdsToActivate = (String[]) checkedExtensions
						.toArray(new String[checkedExtensions.size()]);
			}

		}

		String[] filterIdsToActivate = new String[0];
		if (commonFiltersTab != null) {
			List checkedFilters = new ArrayList();
			TableItem[] tableItems = commonFiltersTab.getTable().getItems();
			ICommonFilterDescriptor descriptor;
			for (int i = 0; i < tableItems.length; i++) {
				descriptor = (ICommonFilterDescriptor) tableItems[i].getData();

				if (tableItems[i].getChecked()) {
					checkedFilters.add(descriptor.getId());
				}
			}

			if (checkedFilters.size() != 0) {
				filterIdsToActivate = (String[]) checkedFilters
						.toArray(new String[checkedFilters.size()]);
			}

		}

		UpdateActiveExtensionsOperation updateExtensions = new UpdateActiveExtensionsOperation(
				commonViewer, contentExtensionIdsToActivate);
		UpdateActiveFiltersOperation updateFilters = new UpdateActiveFiltersOperation(
				commonViewer, filterIdsToActivate, true);

		updateExtensions.execute(null, null);
		updateFilters.execute(null, null);
		super.okPressed();
	}
}
