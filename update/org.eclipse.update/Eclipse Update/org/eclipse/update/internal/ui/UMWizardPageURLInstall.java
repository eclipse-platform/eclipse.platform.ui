package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Presents update URL locations for selection.
 */
import java.lang.reflect.InvocationTargetException;import java.util.Iterator;import java.util.TreeSet;import org.eclipse.core.internal.boot.update.IComponentDescriptor;import org.eclipse.core.internal.boot.update.IComponentEntryDescriptor;import org.eclipse.core.internal.boot.update.IManifestDescriptor;import org.eclipse.core.internal.boot.update.IProductDescriptor;import org.eclipse.core.internal.boot.update.IUMRegistry;import org.eclipse.core.internal.boot.update.UMRegistryManager;import org.eclipse.core.internal.boot.update.UpdateManagerConstants;import org.eclipse.core.runtime.IProgressMonitor;import org.eclipse.jface.operation.IRunnableWithProgress;import org.eclipse.jface.viewers.ColumnLayoutData;import org.eclipse.jface.viewers.ColumnWeightData;import org.eclipse.jface.viewers.TableLayout;import org.eclipse.jface.wizard.WizardPage;import org.eclipse.swt.SWT;import org.eclipse.swt.custom.TableTree;import org.eclipse.swt.custom.TableTreeItem;import org.eclipse.swt.events.SelectionEvent;import org.eclipse.swt.events.SelectionListener;import org.eclipse.swt.layout.GridData;import org.eclipse.swt.layout.GridLayout;import org.eclipse.swt.widgets.Button;import org.eclipse.swt.widgets.Composite;import org.eclipse.swt.widgets.Display;import org.eclipse.swt.widgets.Label;import org.eclipse.swt.widgets.TableColumn;import org.eclipse.update.internal.core.UpdateManagerStrings;

public class UMWizardPageURLInstall extends WizardPage implements SelectionListener {
	protected boolean _bInitialized = false;
	protected UMWizard _wizard = null;
	protected TableTree _tableTreeItems = null;
	protected TreeSet _treeSetItemsProducts = new TreeSet(new UMWizardTreeItemComparator());
	protected TreeSet _treeSetItemsComponents = new TreeSet(new UMWizardTreeItemComparator());
	protected Button _checkboxLookForLatestVersions = null;
	protected Label _labelLocation = null;
	protected IUMRegistry _registry = null;
	protected IManifestDescriptor _descriptor = null;
	/**
	 * ScriptNewScriptWizardPage1 constructor comment.
	 */
	public UMWizardPageURLInstall(UMWizard wizard, String strName) {
		super(strName);
		_wizard = wizard;

		this.setTitle(UpdateManagerStrings.getString("S_Install_Components"));
		this.setDescription(UpdateManagerStrings.getString("S_The_following_items_will_be_installed"));
	}
	/**
	 */
	public void connectToTree() {

		// Remove all existing tree items
		//-------------------------------
		_tableTreeItems.removeAll();

		UMWizardTreeItem item = null;

		// Products
		//---------
		Iterator iter = _treeSetItemsProducts.iterator();

		while (iter.hasNext() == true) {

			item = (UMWizardTreeItem) iter.next();

			// Create a new tree item for the top level
			//-----------------------------------------
			TableTreeItem treeItem = new TableTreeItem(_tableTreeItems, SWT.NULL);

			if (item._strName != null)
				treeItem.setText(0, item._strName);

			if (item._strVersionCurrent != null)
				treeItem.setText(1, item._strVersionCurrent);

			if (item._strVendorName != null)
				treeItem.setText(2, item._strVendorName);

			treeItem.setData(item);

			// Create child tree items
			//------------------------
			if (item._vectorChildren != null) {
				for (int i = 0; i < item._vectorChildren.size(); ++i) {
					connectToTree((UMWizardTreeItem) item._vectorChildren.elementAt(i), treeItem);
				}
			}

			treeItem.setExpanded(true);
		}

		// Components
		//-----------
		iter = _treeSetItemsComponents.iterator();

		while (iter.hasNext() == true) {

			item = (UMWizardTreeItem) iter.next();

			// Create a new tree item for the top level
			//-----------------------------------------
			TableTreeItem treeItem = new TableTreeItem(_tableTreeItems, SWT.NULL);

			if (item._strName != null)
				treeItem.setText(0, item._strName);

			if (item._strVersionCurrent != null)
				treeItem.setText(1, item._strVersionCurrent);

			if (item._strVendorName != null)
				treeItem.setText(2, item._strVendorName);

			treeItem.setData(item);

			// Create child tree items
			//------------------------
			if (item._vectorChildren != null) {
				for (int i = 0; i < item._vectorChildren.size(); ++i) {
					connectToTree((UMWizardTreeItem) item._vectorChildren.elementAt(i), treeItem);
				}
			}

			treeItem.setExpanded(true);
		}

	}
	/**
	 * Connects items to a new tree widget.
	 * @param tree org.eclipse.swt.widgets.Tree
	 */
	public void connectToTree(UMWizardTreeItem item, TableTreeItem treeItemParent) {

		// Create a new tree item
		//-----------------------
		TableTreeItem treeItem = new TableTreeItem(treeItemParent, SWT.NULL);
		treeItem.setText(0, item._strName);

		if (item._iType == UpdateManagerConstants.TYPE_COMPONENT) {
			if (item._strVendorName != null)
				treeItem.setText(1, item._strVendorName);
		}

		treeItem.setData(item);

		// Create child tree items
		//------------------------
		if (item._vectorChildren != null) {
			for (int i = 0; i < item._vectorChildren.size(); ++i) {
				connectToTree((UMWizardTreeItem) item._vectorChildren.elementAt(i), treeItem);
			}
		}

		treeItem.setExpanded(true);

	}
	/**
	 * createContents method comment.
	 */
	public void createControl(Composite compositeParent) {
		// Content
		//--------
		Composite compositeContent = new Composite(compositeParent, SWT.NULL);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		compositeContent.setLayout(layout);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		compositeContent.setLayoutData(gridData);

		// Label: Location
		//----------------
		Label label = new Label(compositeContent, SWT.NULL);
		label.setText(UpdateManagerStrings.getString("S_Source_location") + ":");

		_labelLocation = new Label(compositeContent, SWT.NULL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		_labelLocation.setLayoutData(gridData);

		// Checkbox: Look for latest versions
		//-----------------------------------
		_checkboxLookForLatestVersions = new Button(compositeContent, SWT.CHECK);
		_checkboxLookForLatestVersions.setSelection(true);
		_checkboxLookForLatestVersions.setText(UpdateManagerStrings.getString("S_Search_for_more_recent_versions_to_install"));
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		gridData.horizontalIndent = 8;
		gridData.widthHint = 500;
		_checkboxLookForLatestVersions.setLayoutData(gridData);
		_checkboxLookForLatestVersions.addSelectionListener(this);

		// Tree: Installable
		//------------------
		_tableTreeItems = new TableTree(compositeContent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER /* | SWT.CHECK */);

		String[] columnTitles = { UpdateManagerStrings.getString("S_Component"), UpdateManagerStrings.getString("S_Version"), UpdateManagerStrings.getString("S_Provider")};
		int[] iColumnWeight = { 50, 25, 25, };
		TableLayout layoutTable = new TableLayout();

		for (int i = 0; i < columnTitles.length; i++) {
			TableColumn tableColumn = new TableColumn(_tableTreeItems.getTable(), SWT.NONE);
			tableColumn.setText(columnTitles[i]);
			ColumnLayoutData cLayout = new ColumnWeightData(iColumnWeight[i], true);
			layoutTable.addColumnData(cLayout);
		}

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 2;
		_tableTreeItems.setLayoutData(gridData);

		_tableTreeItems.getTable().setLinesVisible(true);
		_tableTreeItems.getTable().setHeaderVisible(true);
		_tableTreeItems.getTable().setLayout(layout);
		_tableTreeItems.getTable().setLayout(layoutTable);
		_tableTreeItems.addSelectionListener(this);

		setControl(compositeContent);

		setPageComplete(false);
	}
	/**
	 */
	public IManifestDescriptor getDescriptor() {
		return _descriptor;
	}
	/**
	 * Obtains a list of registered component URLs from the local update registry.
	 * Obtains a list of bookmarked URLs from the persistent data.
	 * Creates a tree for all of the URLs.
	 */
	protected void initializeContent() {

		if (_bInitialized == true || _wizard._urlInstall == null || _wizard._strInstallId == null)
			return;

		// Obtain registry
		//----------------
		final UMRegistryManager registryManager = _wizard._updateManager.getRegistryManager();

		// Update tree with location information
		//--------------------------------------
		IRunnableWithProgress operation = new IRunnableWithProgress() {
			public void run(IProgressMonitor progressMonitor) throws InvocationTargetException {

				progressMonitor.beginTask(UpdateManagerStrings.getString("S_Obtaining_information"), IProgressMonitor.UNKNOWN);
				_registry = registryManager.getRegistryAt(_wizard._urlInstall);
				progressMonitor.done();
			}
		};

		try {
			_wizard.getContainer().run(true, true, operation);
		}
		catch (InterruptedException e) {
		}
		catch (InvocationTargetException e) {
		}

		UMWizardTreeItem itemProduct = null;
		UMWizardTreeItem itemComponent = null;
		UMWizardTreeItem itemComponentEntry = null;

		// Obtain the descriptor
		//----------------------
		_descriptor = _registry.getProductDescriptor(_wizard._strInstallId);

		if (_descriptor == null) {
			_descriptor = _registry.getComponentDescriptor(_wizard._strInstallId);
		}

		if (_descriptor != null) {

			_treeSetItemsProducts = new TreeSet(new UMWizardTreeItemComparator());

			if (_descriptor instanceof IProductDescriptor) {

				itemProduct = new UMWizardTreeItem();
				itemProduct._iType = UpdateManagerConstants.TYPE_PRODUCT;
				itemProduct._strName = ((IProductDescriptor) _descriptor).getLabel();
				itemProduct._strVendorName = ((IProductDescriptor) _descriptor).getProviderName();
				itemProduct._strVersionCurrent = ((IProductDescriptor) _descriptor).getVersionStr();
				itemProduct._descriptorCurrent = _descriptor;

				_treeSetItemsProducts.add(itemProduct);

				// Component entries of the product
				//---------------------------------
				IComponentEntryDescriptor[] descriptorsEntry = ((IProductDescriptor) _descriptor).getComponentEntries();

				for (int j = 0; j < descriptorsEntry.length; ++j) {

					// Turn on selected flag
					//----------------------
					descriptorsEntry[j].isSelected(true);
					itemComponentEntry = new UMWizardTreeItem();
					itemComponentEntry._iType = UpdateManagerConstants.TYPE_COMPONENT_ENTRY;
					itemComponentEntry._strName = descriptorsEntry[j].getLabel();
					itemComponentEntry._strId = descriptorsEntry[j].getUniqueIdentifier();
					itemComponentEntry._strVersionAvailable = descriptorsEntry[j].getVersionStr();
					itemComponentEntry._descriptorEntry = descriptorsEntry[j];

					itemProduct.addChildItem(itemComponentEntry);
				}
			}

			else if (_descriptor instanceof IComponentDescriptor) {

				itemComponent = new UMWizardTreeItem();

				itemComponent._iType = UpdateManagerConstants.TYPE_COMPONENT;
				itemComponent._strDescription = ((IComponentDescriptor) _descriptor).getDescription();
				itemComponent._strName = ((IComponentDescriptor) _descriptor).getLabel();
				itemComponent._strId = ((IComponentDescriptor) _descriptor).getUniqueIdentifier();
				itemComponent._strVendorName = ((IComponentDescriptor) _descriptor).getProviderName();
				itemComponent._strVersionCurrent = ((IComponentDescriptor) _descriptor).getVersionStr();
				itemComponent._descriptorCurrent = _descriptor;

				_treeSetItemsComponents.add(itemComponent);
			}

			// Create tree widget items
			//-------------------------
			connectToTree();

			if (_descriptor != null) {
				_labelLocation.setText(_descriptor.getUMRegistry().getRegistryBaseURL().toExternalForm());
				setPageComplete(true);
			}
		}

		_bInitialized = true;

		return;
	}
	/**
	 *
	 */
	public boolean isLookForLaterVersionsChecked() {
		return _checkboxLookForLatestVersions.getSelection();
	}
	/**
	 * 
	 */
	public void setVisible(boolean bVisible) {

		super.setVisible(bVisible);

		if (bVisible == true) {
			Display d = this.getControl().getDisplay();
			d.asyncExec(new Runnable() {
				public void run() {
					initializeContent();
				}
			});
		}
	}
	/**
	 * Sent when default selection occurs in the control.
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
	}
	/**
	 */
	public void widgetSelected(SelectionEvent event) {

		if (event.widget == _checkboxLookForLatestVersions) {
			UMWizardPageURLInstallable pageInstallable = (UMWizardPageURLInstallable) _wizard.getPage("installable");
			pageInstallable._bRefreshRequired = true;
		}
	}
}