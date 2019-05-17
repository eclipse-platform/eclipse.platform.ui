/*******************************************************************************
 * Copyright (c) 2003, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
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
public class CommonFilterSelectionDialog extends TrayDialog {

	private static final String FILTER_ICON = "icons/full/elcl16/filter_ps.png"; //$NON-NLS-1$
	private static final String CONTENT_ICON = "icons/full/elcl16/content.png"; //$NON-NLS-1$

	private static final int TAB_WIDTH_IN_DLUS = 300;

	private static final int TAB_HEIGHT_IN_DLUS = 150;

	private final CommonViewer commonViewer;

	private final INavigatorContentService contentService;

	private CTabFolder customizationsTabFolder;

	private CommonFiltersTab commonFiltersTab;

	private ContentExtensionsTab contentExtensionsTab;

	private Label descriptionText;

	private ISelectionChangedListener updateDescriptionSelectionListener;

	private String helpContext;

	private UserFiltersTab userFiltersTab;

	/**
	 * Public only for tests.
	 *
	 * @param aCommonViewer
	 */
	public CommonFilterSelectionDialog(CommonViewer aCommonViewer) {
		super(aCommonViewer.getControl().getShell());
		setShellStyle(SWT.RESIZE | getShellStyle());

		commonViewer = aCommonViewer;
		contentService = commonViewer.getNavigatorContentService();

		INavigatorViewerDescriptor viewerDescriptor = contentService.getViewerDescriptor();
		helpContext = viewerDescriptor
				.getStringConfigProperty(INavigatorViewerDescriptor.PROP_CUSTOMIZE_VIEW_DIALOG_HELP_CONTEXT);

		if (helpContext != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(
					aCommonViewer.getControl().getShell(), helpContext);
		}
	}

	@Override
	public boolean isHelpAvailable() {
		return helpContext != null;
	}

	@Override
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
				commonFiltersTab, FILTER_ICON);

		if (contentService.getViewerDescriptor()
				.isVisibleContentExtension("org.eclipse.ui.navigator.resourceContent")) { //$NON-NLS-1$
			this.userFiltersTab = new UserFiltersTab(customizationsTabFolder, this.commonViewer);
			createTabItem(customizationsTabFolder,
					CommonNavigatorMessages.CommonFilterSelectionDialog_User_Resource_Filters, userFiltersTab,
					FILTER_ICON);
		}

		boolean hideExtensionsTab = contentService.getViewerDescriptor()
				.getBooleanConfigProperty(
						INavigatorViewerDescriptor.PROP_HIDE_AVAILABLE_EXT_TAB);

		if (!hideExtensionsTab) {
			contentExtensionsTab = new ContentExtensionsTab(
					customizationsTabFolder, contentService);

			createTabItem(
					customizationsTabFolder,
					CommonNavigatorMessages.CommonFilterSelectionDialog_Available_Content,
					contentExtensionsTab, CONTENT_ICON);

		}

		createDescriptionText(superComposite);

		if (commonFiltersTab != null) {
			commonFiltersTab.addSelectionChangedListener(getSelectionListener());
		}

		if (contentExtensionsTab != null) {
			contentExtensionsTab
					.addSelectionChangedListener(getSelectionListener());
		}

		commonFiltersTab.setInitialFocus();

		return customizationsTabFolder;
	}

	private void createCustomizationsTabFolder(Composite superComposite) {
		customizationsTabFolder = new CTabFolder (superComposite, SWT.RESIZE | SWT.BORDER);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = convertHorizontalDLUsToPixels(TAB_WIDTH_IN_DLUS);
		gd.heightHint = convertVerticalDLUsToPixels(TAB_HEIGHT_IN_DLUS);

		customizationsTabFolder.setLayout(new GridLayout());
		customizationsTabFolder.setLayoutData(gd);

		customizationsTabFolder.setFont(superComposite.getFont());

		customizationsTabFolder.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (descriptionText != null) {
					descriptionText.setText(""); //$NON-NLS-1$
				}
			}


		});

		customize();

	}

	private void customize() {
		ColorRegistry reg = JFaceResources.getColorRegistry();
		Color c1 = reg.get("org.eclipse.ui.workbench.ACTIVE_TAB_BG_START"), //$NON-NLS-1$
		c2 = reg.get("org.eclipse.ui.workbench.ACTIVE_TAB_BG_END"); //$NON-NLS-1$
		customizationsTabFolder.setSelectionBackground(new Color[] {c1, c2},	new int[] {100}, true);
		customizationsTabFolder.setSelectionForeground(reg.get("org.eclipse.ui.workbench.ACTIVE_TAB_TEXT_COLOR")); //$NON-NLS-1$
		customizationsTabFolder.setSimple(true);
	}

	private CTabItem createTabItem(CTabFolder aTabFolder, String label,
			Composite composite, String imageKey) {
		CTabItem extensionsTabItem = new CTabItem(aTabFolder, SWT.BORDER);
		extensionsTabItem.setText(label);
		extensionsTabItem.setControl(composite);
		extensionsTabItem.setImage(NavigatorPlugin.getDefault().getImage(imageKey));
		return extensionsTabItem;
	}

	private void createDescriptionText(Composite composite) {

		descriptionText = new Label(composite, SWT.WRAP);
		descriptionText.setFont(composite.getFont());
		descriptionText.setBackground(composite.getBackground());
		GridData descriptionTextGridData = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
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

	@Override
	protected void okPressed() {

		if (contentExtensionsTab != null) {
			List<String> checkedExtensions = new ArrayList<String>();
			TableItem[] tableItems = contentExtensionsTab.getTable().getItems();
			INavigatorContentDescriptor descriptor;
			for (TableItem tableItem : tableItems) {
				descriptor = (INavigatorContentDescriptor) tableItem
						.getData();

				if (tableItem.getChecked()) {
					checkedExtensions.add(descriptor.getId());
				}
			}
			String[] contentExtensionIdsToActivate = checkedExtensions
					.toArray(new String[checkedExtensions.size()]);
			UpdateActiveExtensionsOperation updateExtensions = new UpdateActiveExtensionsOperation(
					commonViewer, contentExtensionIdsToActivate);
			updateExtensions.execute(null, null);
		}

		List<String> filterIdsToActivate = new ArrayList<>();
		if (commonFiltersTab != null) {
			Set<ICommonFilterDescriptor> checkedFilters = commonFiltersTab.getCheckedItems();
			for (ICommonFilterDescriptor descriptor : checkedFilters) {
				filterIdsToActivate.add(descriptor.getId());
			}
		}
		if (this.userFiltersTab != null) {
			this.commonViewer.setData(NavigatorPlugin.RESOURCE_REGEXP_FILTER_DATA, this.userFiltersTab.getUserFilters());
			if (!this.userFiltersTab.getUserFilters().isEmpty()) {
				filterIdsToActivate.add(NavigatorPlugin.RESOURCE_REGEXP_FILTER_FILTER_ID);
			}
		}
		if (this.userFiltersTab != null || this.commonFiltersTab != null) {
			UpdateActiveFiltersOperation updateFilters = new UpdateActiveFiltersOperation(
					commonViewer, filterIdsToActivate.toArray(new String[filterIdsToActivate.size()]));
			updateFilters.execute(null, null);
		}

		super.okPressed();
	}

	protected ICommonFilterDescriptor[] getFilterDescriptorChangeHistory() {
		if (commonFiltersTab != null) {
			return commonFiltersTab.getFilterDescriptorChangeHistory();
		}
		return new ICommonFilterDescriptor[0];
	}
}
