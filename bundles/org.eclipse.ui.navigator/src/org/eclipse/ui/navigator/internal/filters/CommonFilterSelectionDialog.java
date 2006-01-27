/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
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
package org.eclipse.ui.navigator.internal.filters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonFilterDescriptor;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.INavigatorViewerDescriptor;
import org.eclipse.ui.navigator.internal.CommonNavigatorMessages;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 * 
 */
public class CommonFilterSelectionDialog extends Dialog {

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
		descriptionText.setBackground(superComposite.getBackground());

		commonFiltersTab.addSelectionChangedListener(getSelectionListener());

		if (contentExtensionsTab != null)
			contentExtensionsTab
					.addSelectionChangedListener(getSelectionListener());

		return customizationsTabFolder;
	}

	private void createCustomizationsTabFolder(Composite superComposite) {
		customizationsTabFolder = new TabFolder(superComposite, SWT.RESIZE);

		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		customizationsTabFolder.setLayout(layout);
		customizationsTabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		customizationsTabFolder.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if(descriptionText != null)
					descriptionText.setText(""); //$NON-NLS-1$
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
		GridData descriptionTextGridData = new GridData(
				GridData.FILL_HORIZONTAL);
		descriptionTextGridData.heightHint = convertHeightInCharsToPixels(3);
		descriptionText.setLayoutData(descriptionTextGridData);
	}

	private ISelectionChangedListener getSelectionListener() {
		if (updateDescriptionSelectionListener == null)
			updateDescriptionSelectionListener = new FilterDialogSelectionListener(
					descriptionText);
		return updateDescriptionSelectionListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {

		String[] activeContentExtensions = new String[0];
		String[] inactiveContentExtensions = new String[0];
		if (contentExtensionsTab != null) {
			List checkedExtensions = new ArrayList();
			List uncheckedExtensions = new ArrayList();
			TableItem[] tableItems = contentExtensionsTab.getTable().getItems();
			INavigatorContentDescriptor descriptor;
			for (int i = 0; i < tableItems.length; i++) {
				descriptor = (INavigatorContentDescriptor) tableItems[i]
						.getData();

				if (tableItems[i].getChecked())
					checkedExtensions.add(descriptor.getId());
				else
					uncheckedExtensions.add(descriptor.getId());
			}
			if (checkedExtensions.size() != 0)
				activeContentExtensions = (String[]) checkedExtensions
						.toArray(new String[checkedExtensions.size()]);

			if (uncheckedExtensions.size() != 0)
				inactiveContentExtensions = (String[]) uncheckedExtensions
						.toArray(new String[uncheckedExtensions.size()]);
		}

		String[] activeFilterIds = new String[0];
		String[] inactiveFilterIds = new String[0];
		if (commonFiltersTab != null) {
			List checkedFilters = new ArrayList();
			List uncheckedFilters = new ArrayList();
			TableItem[] tableItems = commonFiltersTab.getTable().getItems();
			ICommonFilterDescriptor descriptor;
			for (int i = 0; i < tableItems.length; i++) {
				descriptor = (ICommonFilterDescriptor) tableItems[i].getData();

				if (tableItems[i].getChecked())
					checkedFilters.add(descriptor.getId());
				else
					uncheckedFilters.add(descriptor.getId());
			}

			if (checkedFilters.size() != 0)
				activeFilterIds = (String[]) checkedFilters
						.toArray(new String[checkedFilters.size()]);

			if (uncheckedFilters.size() != 0)
				inactiveFilterIds = (String[]) uncheckedFilters
						.toArray(new String[uncheckedFilters.size()]);
		}

		UpdateFiltersOperation updateFilters = new UpdateFiltersOperation(
				commonViewer, contentService, activeFilterIds,
				inactiveFilterIds, activeContentExtensions,
				inactiveContentExtensions);

		updateFilters.execute(null, null);
		super.okPressed();
	}
}
