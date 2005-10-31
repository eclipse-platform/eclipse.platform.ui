/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.navigator.CommonActivitiesUtilities;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.NavigatorActionService;
import org.eclipse.ui.navigator.NavigatorContentService;
import org.eclipse.ui.navigator.internal.NavigatorMessages;
import org.eclipse.ui.navigator.internal.extensions.NavigatorActivationService;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptorRegistry;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 *  
 */
public class CommonFilterSelectionDialog extends Dialog {

	private static final NavigatorActivationService NAVIGATOR_ACTIVATION_SERVICE = NavigatorActivationService.getInstance();
	private static final NavigatorContentDescriptorRegistry CONTENT_DESCRIPTOR_REGISTRY = NavigatorContentDescriptorRegistry.getInstance();
	private static final Object[] NO_CHILDREN = new Object[0];
	private CheckboxTableViewer extensionsCheckboxTableViewer;
	private Text descriptionText;
	private ISelectionChangedListener updateDescriptionSelectionListener;
	private TabFolder filtersTabFolder;
	private CheckboxTableViewer filtersCheckboxTableViewer;
	
	private CommonNavigator commonNavigator;
	private final CommonViewer commonViewer;
	private final NavigatorContentService contentService;

	/**
	 * @param arg0
	 */
	public CommonFilterSelectionDialog(CommonNavigator aCommonNavigator) {
		super(aCommonNavigator.getSite().getShell());
		setShellStyle(SWT.RESIZE | getShellStyle());
		
		commonNavigator = aCommonNavigator;
		commonViewer = commonNavigator.getCommonViewer();
		contentService = commonViewer.getNavigatorContentService();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		getShell().setText(NavigatorMessages.getString("CommonFilterSelectionDialog.0")); //$NON-NLS-1$		
		Composite superComposite = (Composite) super.createDialogArea(parent);

		createFiltersTabFolder(superComposite);

		Composite extensionsComposite = createContainerComposite(filtersTabFolder);
		/* createTitleLabel(extensionsComposite); */
		createExtensionsTableViewer(extensionsComposite);

		Composite filtersComposite = createContainerComposite(filtersTabFolder);
		createFiltersTableViewer(filtersComposite);

		createDescriptionText(superComposite);

		createTabItem(filtersTabFolder, NavigatorMessages.getString("CommonFilterSelectionDialog.0"), filtersComposite); //$NON-NLS-1$
		createTabItem(filtersTabFolder, NavigatorMessages.getString("CommonFilterSelectionDialog.1"), extensionsComposite); //$NON-NLS-1$

		descriptionText.setBackground(superComposite.getBackground());

		updateCheckedItems();

		return extensionsComposite;
	}

	/**
	 * @param superComposite
	 */
	private void createFiltersTabFolder(Composite superComposite) {
		filtersTabFolder = new TabFolder(superComposite, SWT.RESIZE);
		createStandardLayout(filtersTabFolder);
		filtersTabFolder.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				descriptionText.setText(""); //$NON-NLS-1$
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}

		});
	}

	/**
	 * @param filtersTabFolderArg
	 * @param composite
	 */
	private void createTabItem(TabFolder filtersTabFolderArg, String label, Composite composite) {
		TabItem extensionsTabItem = new TabItem(filtersTabFolderArg, SWT.NONE);
		extensionsTabItem.setText(label);
		extensionsTabItem.setControl(composite);
	}

	/**
	 * @param composite
	 */
	private void createStandardLayout(Composite composite) {
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	/**
	 * @param superComposite
	 * @return
	 */
	private Composite createContainerComposite(Composite superComposite) {
		Composite composite = new Composite(superComposite, SWT.RESIZE);

		createStandardLayout(composite);
		return composite;
	}



	/**
	 * @param composite
	 */
	private void createExtensionsInstructionsLabel(Composite composite) {
		Label extensionsInstructionLabel = new Label(composite, SWT.BOLD | SWT.V_SCROLL);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = convertHeightInCharsToPixels(1) + 3;
		gridData.horizontalIndent = convertHorizontalDLUsToPixels(2);
		/* gridData.verticalSpan = convertVerticalDLUsToPixels(2); */
		gridData.verticalAlignment = GridData.VERTICAL_ALIGN_FILL;
		gridData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_FILL;

		extensionsInstructionLabel.setLayoutData(gridData);
		extensionsInstructionLabel.setText(NavigatorMessages.getString("CommonFilterSelectionDialog.3")); //$NON-NLS-1$
	}

	/**
	 * @param composite
	 */
	private void createFiltersInstructionsLabel(Composite composite) {
		Label filtersInstructionLabel = new Label(composite, SWT.BOLD | SWT.V_SCROLL);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = convertHeightInCharsToPixels(1) + 3;
		gridData.horizontalIndent = convertHorizontalDLUsToPixels(2);
		/* gridData.verticalSpan = convertVerticalDLUsToPixels(2); */
		gridData.verticalAlignment = GridData.VERTICAL_ALIGN_FILL;
		gridData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_FILL;

		filtersInstructionLabel.setLayoutData(gridData);
		filtersInstructionLabel.setText(NavigatorMessages.getString("CommonFilterSelectionDialog.4")); //$NON-NLS-1$
	}

	/**
	 * @param composite
	 */
	private void createDescriptionText(Composite composite) {
		descriptionText = new Text(composite, SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		GridData descriptionTextGridData = new GridData(GridData.FILL_HORIZONTAL);
		descriptionTextGridData.heightHint = convertHeightInCharsToPixels(3);
		descriptionText.setLayoutData(descriptionTextGridData);
	}


	/**
	 * @param composite
	 */
	private void createFiltersTableViewer(Composite composite) {
		filtersCheckboxTableViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.RESIZE | SWT.FULL_SELECTION);

		filtersCheckboxTableViewer.setContentProvider(new CommonFilterContentProvider());
		filtersCheckboxTableViewer.setLabelProvider(new CommonFilterLabelProvider());
		filtersCheckboxTableViewer.setInput(contentService);

		createFiltersInstructionsLabel(composite);

		createCheckboxTable(filtersCheckboxTableViewer);
	}

	/**
	 * @param composite
	 */
	private void createExtensionsTableViewer(Composite composite) {
		extensionsCheckboxTableViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.RESIZE);

		extensionsCheckboxTableViewer.setContentProvider(new ExtensionContentProvider());
		extensionsCheckboxTableViewer.setLabelProvider(new CommonFilterLabelProvider());
		extensionsCheckboxTableViewer.setInput(contentService);

		createExtensionsInstructionsLabel(composite);

		createCheckboxTable(extensionsCheckboxTableViewer);
	}


	/**
	 *  
	 */
	private void createCheckboxTable(CheckboxTableViewer tableViewer) {
		Table table = tableViewer.getTable();
		GridLayout tableLayout = new GridLayout();
		tableLayout.marginHeight = 0; //convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		tableLayout.marginWidth = 0; //convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		tableLayout.verticalSpacing = 0; //convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		tableLayout.horizontalSpacing = 0; //convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		tableLayout.numColumns = 2;
		GridData tableGridData = new GridData(GridData.FILL_BOTH);
		tableGridData.widthHint = convertHorizontalDLUsToPixels(100);
		tableGridData.heightHint = convertVerticalDLUsToPixels(50);
		table.setLayout(tableLayout);
		table.setLayoutData(tableGridData);
	}

	public class UpdateDescriptionListener implements ISelectionChangedListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		public void selectionChanged(SelectionChangedEvent event) {

			IStructuredSelection structuredSelection = (IStructuredSelection) event.getSelection();
			Object element = structuredSelection.getFirstElement();
			if (element instanceof NavigatorContentDescriptor) {
				NavigatorContentDescriptor ncd = (NavigatorContentDescriptor) element;
				String desc = NavigatorMessages.format("CommonFilterSelectionDialog.2", new Object[]{ncd.getName()}); //$NON-NLS-1$ 
				descriptionText.setText(desc);
			} else if (element instanceof ExtensionFilterDescriptor) {
				ExtensionFilterDescriptor efd = (ExtensionFilterDescriptor) element;
				descriptionText.setText(efd.getDescription());
			}

		}

	}

	/**
	 *  
	 */
	protected void updateCheckedItems() {

		extensionsCheckboxTableViewer.addSelectionChangedListener(getSelectionListener());
		filtersCheckboxTableViewer.addSelectionChangedListener(getSelectionListener());

		updateExtensionsCheckState();
		updateFiltersCheckState();
	}

	/**
	 *  
	 */
	private void updateExtensionsCheckState() {
		NavigatorContentDescriptor descriptor;
		boolean enabled;

		TableItem[] descriptorTableItems = extensionsCheckboxTableViewer.getTable().getItems();
		for (int i = 0; i < descriptorTableItems.length; i++) {
			if (descriptorTableItems[i].getData() instanceof NavigatorContentDescriptor) {
				descriptor = (NavigatorContentDescriptor) descriptorTableItems[i].getData();
				enabled = NAVIGATOR_ACTIVATION_SERVICE.isNavigatorExtensionActive(contentService.getViewerId(), descriptor.getId());
				extensionsCheckboxTableViewer.setChecked(descriptor, enabled);
			}
		}
	}

	/**
	 *  
	 */
	private void updateFiltersCheckState() {
		IStructuredContentProvider contentProvider = (IStructuredContentProvider) filtersCheckboxTableViewer.getContentProvider();
		Object[] children = contentProvider.getElements(contentService);
		ExtensionFilterViewerRegistry filterRegistry = ExtensionFilterRegistryManager.getInstance().getViewerRegistry(contentService.getViewerId());
		ExtensionFilterDescriptor filterDescriptor = null;
		for (int i = 0; i < children.length; i++) {
			filterDescriptor = (ExtensionFilterDescriptor) children[i];
			filtersCheckboxTableViewer.setChecked(children[i], filterRegistry.getActivationManager().isFilterActive(filterDescriptor));
		}
	}

	/**
	 * @return
	 */
	private ISelectionChangedListener getSelectionListener() {
		if (updateDescriptionSelectionListener == null)
			updateDescriptionSelectionListener = new UpdateDescriptionListener();
		return updateDescriptionSelectionListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {

		boolean updateExtensionActivation = false;
		boolean updateFilterActivation = false;
		boolean enabled = false;
		NavigatorContentDescriptor descriptor = null;
		ExtensionFilterDescriptor filterDescriptor = null;

		ExtensionFilterViewerRegistry filterRegistry = ExtensionFilterRegistryManager.getInstance().getViewerRegistry(contentService.getViewerId());
		TableItem[] extensionTableItems = extensionsCheckboxTableViewer.getTable().getItems();
		for (int descriptorIndex = 0; descriptorIndex < extensionTableItems.length; descriptorIndex++) {

			if (extensionTableItems[descriptorIndex].getData() instanceof NavigatorContentDescriptor) {
				descriptor = (NavigatorContentDescriptor) extensionTableItems[descriptorIndex].getData();

				enabled = extensionsCheckboxTableViewer.getChecked(descriptor);
				if (enabled != NAVIGATOR_ACTIVATION_SERVICE.isNavigatorExtensionActive(contentService.getViewerId(), descriptor.getId())) {
					updateExtensionActivation = true;
					NAVIGATOR_ACTIVATION_SERVICE.activateNavigatorExtension(contentService.getViewerId(), descriptor.getId(), enabled);
				}
			}
		}

		TableItem[] filterTableItems = filtersCheckboxTableViewer.getTable().getItems();
		for (int descriptorIndex = 0; descriptorIndex < filterTableItems.length; descriptorIndex++) {

			if (filterTableItems[descriptorIndex].getData() instanceof ExtensionFilterDescriptor) {
				filterDescriptor = (ExtensionFilterDescriptor) filterTableItems[descriptorIndex].getData();
				enabled = filtersCheckboxTableViewer.getChecked(filterDescriptor);
				if (filterRegistry.getActivationManager().isFilterActive(filterDescriptor) != enabled) {
					updateFilterActivation = true;
					filterRegistry.getActivationManager().activateFilter(filterDescriptor, enabled);
				}
			}
		}
		if (updateExtensionActivation)
			NAVIGATOR_ACTIVATION_SERVICE.persistExtensionActivations(contentService.getViewerId());
		if (updateFilterActivation)
			filterRegistry.getActivationManager().persistFilterActivations();
		if (updateExtensionActivation || updateFilterActivation) {
			contentService.update();
			
			NavigatorActionService actionService = commonNavigator.getNavigatorActionService();
			actionService.refresh();
		}

		super.okPressed();
	}

	class ExtensionContentProvider implements ITreeContentProvider {



		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.wst.common.navigator.internal.views.navigator.INavigatorContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer aViewer, Object anOldInput, Object aNewInput) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object aParentElement) {
			return NO_CHILDREN;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object anElement) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object anElement) {
			return getChildren(anElement).length != 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object anInputElement) {
			return CommonActivitiesUtilities.filterByActivity(CONTENT_DESCRIPTOR_REGISTRY.getAllContentDescriptors());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {

		}

	}
}