package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Installs previously selected products and components.
 */
import java.lang.reflect.InvocationTargetException;import org.eclipse.core.internal.boot.update.IInstallable;import org.eclipse.core.internal.boot.update.UpdateManagerConstants;import org.eclipse.core.runtime.IProgressMonitor;import org.eclipse.jface.operation.IRunnableWithProgress;import org.eclipse.jface.viewers.ColumnLayoutData;import org.eclipse.jface.viewers.ColumnWeightData;import org.eclipse.jface.viewers.TableLayout;import org.eclipse.jface.wizard.WizardPage;import org.eclipse.swt.SWT;import org.eclipse.swt.custom.TableTree;import org.eclipse.swt.custom.TableTreeItem;import org.eclipse.swt.layout.GridData;import org.eclipse.swt.layout.GridLayout;import org.eclipse.swt.widgets.Button;import org.eclipse.swt.widgets.Composite;import org.eclipse.swt.widgets.TableColumn;import org.eclipse.update.internal.core.UMSessionManagerSession;import org.eclipse.update.internal.core.UpdateManagerException;import org.eclipse.update.internal.core.UpdateManagerStrings;

public class UMWizardPageInstalling extends WizardPage {
	protected boolean _bUpdate = false;
	protected boolean _bInitialized = false;
	protected UMWizard _wizard = null;
	protected TableTree _tableTreeItems = null;
	protected Button _buttonInstall = null;
	protected UMWizardTreeItem[] _items = null;
	protected UMSessionManagerSession _session = null;
	/**
	 * ScriptNewScriptWizardPage1 constructor comment.
	 */
	public UMWizardPageInstalling(UMWizard wizard, String strName, boolean bUpdate) {
		super(strName);
		_wizard = wizard;
		_bUpdate = bUpdate;

		if (bUpdate == true) {
			this.setTitle(UpdateManagerStrings.getString("S_New_Component_Updates"));
			this.setDescription(UpdateManagerStrings.getString("S_The_following_items_will_be_updated"));
		}
		else {
			this.setTitle(UpdateManagerStrings.getString("S_New_Components"));
			this.setDescription(UpdateManagerStrings.getString("S_The_following_items_will_be_installed"));
		}
	}
	/**
	 */
	public void createControl(Composite compositeParent) {
		// Content
		//--------
		Composite compositeContent = new Composite(compositeParent, SWT.NULL);

		GridLayout layout = new GridLayout();
		compositeContent.setLayout(layout);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		compositeContent.setLayoutData(gridData);

		// Tree: Installable
		//------------------
		_tableTreeItems = new TableTree(compositeContent, SWT.READ_ONLY | SWT.FULL_SELECTION | SWT.BORDER);

		String[] columnTitles = { UpdateManagerStrings.getString("S_Component"), UpdateManagerStrings.getString("S_Version"), UpdateManagerStrings.getString("S_Provider")};
		int[] iColumnWeight = { 60, 20, 20 };
		TableLayout layoutTable = new TableLayout();

		for (int i = 0; i < columnTitles.length; i++) {
			TableColumn tableColumn = new TableColumn(_tableTreeItems.getTable(), SWT.NONE);
			tableColumn.setText(columnTitles[i]);
			ColumnLayoutData cLayout = new ColumnWeightData(iColumnWeight[i], true);
			layoutTable.addColumnData(cLayout);
		}
		_tableTreeItems.getTable().setLinesVisible(true);
		_tableTreeItems.getTable().setHeaderVisible(true);
		_tableTreeItems.getTable().setLayout(layout);
		_tableTreeItems.setLayoutData(new GridData(GridData.FILL_BOTH));
		_tableTreeItems.getTable().setLayout(layoutTable);

		setControl(compositeContent);
	}
	/**
	 * 
	 */
	public void doInstalls() {

		if (_items.length <= 0)
			return;

		// Obtain the product/component/component entry descriptors
		//---------------------------------------------------------
		IInstallable[] installables = new IInstallable[_items.length];

		for (int i = 0; i < _items.length; ++i) {
			if (_items[i]._iType == UpdateManagerConstants.TYPE_PRODUCT || _items[i]._iType == UpdateManagerConstants.TYPE_COMPONENT) {
				installables[i] = _items[i]._descriptorAvailable;
			}
			else if (_items[i]._iType == UpdateManagerConstants.TYPE_COMPONENT_ENTRY) {
				installables[i] = _items[i]._descriptorEntry;
			}
		}

		try {
			_session = _wizard._updateManager.createSession(installables, true);

			// Install the product/components
			//-------------------------------
			IRunnableWithProgress operation = new IRunnableWithProgress() {
				public void run(IProgressMonitor progressMonitor) throws InvocationTargetException {
					try {
						// Do
						//---
						_wizard._updateManager.executeSession(_session, progressMonitor);

						// Undo
						//-----
						if (_session.getStatus().equals(UpdateManagerConstants.STATUS_FAILED) == true) {
							_wizard._updateManager.executeSessionUndo(_session, progressMonitor);
						}

						// Update launch information
						//--------------------------
						_wizard._updateManager.updateLaunchInfoAndRegistry(_session);

						// Cleanup staging area
						//---------------------
						_wizard._updateManager.cleanup();
					}
					catch (UpdateManagerException ex) {
					}
				}
			};

			try {
				_wizard.getContainer().run(true, true, operation);
			}
			catch (InterruptedException e) {
			}
			catch (InvocationTargetException e) {
			}
		}
		catch (UpdateManagerException ex) {
		}
	}
	/**
	 */
	public UMSessionManagerSession getSession() {
		return _session;
	}
	/**
	 * Obtains a list of registered component URLs from the local update registry.
	 * Obtains a list of bookmarked URLs from the persistent data.
	 * Creates a tree for all of the URLs.
	 */
	protected void initializeContent() {

		_tableTreeItems.removeAll();

		// Obtain all checked URL items from the updateable page
		//------------------------------------------------------
		UMWizardPageInstallable pageInstallable = (UMWizardPageInstallable) _wizard.getPage("installable");
		_items = pageInstallable.getSelectedItems();

		TableTreeItem tableItem = null;

		for (int i = 0; i < _items.length; ++i) {

			// Products and components
			//------------------------
			if (_items[i]._iType == UpdateManagerConstants.TYPE_COMPONENT || _items[i]._iType == UpdateManagerConstants.TYPE_PRODUCT || _items[i]._iType == UpdateManagerConstants.TYPE_COMPONENT_ENTRY) {

				tableItem = new TableTreeItem(_tableTreeItems, SWT.NULL);

				// Component entries
				//------------------
				if (_items[i]._iType == UpdateManagerConstants.TYPE_PRODUCT) {

					if (_items[i]._strName != null)
						tableItem.setText(0, _items[i]._strName);

					if (_items[i]._strVersionAvailable != null)
						tableItem.setText(1, _items[i]._strVersionAvailable);

					if (_items[i]._strVendorName != null)
						tableItem.setText(2, _items[i]._strVendorName);

					if (_items[i]._vectorChildren != null) {
						for (int j = 0; j < _items[i]._vectorChildren.size(); ++j) {

							UMWizardTreeItem item = (UMWizardTreeItem) _items[i]._vectorChildren.elementAt(j);

							if (item._iType == UpdateManagerConstants.TYPE_COMPONENT_ENTRY) {

								if (item._descriptorEntry.isSelected() == true) {
									TableTreeItem tableItemEntry = new TableTreeItem(tableItem, SWT.NULL);

									if (item._strName != null)
										tableItemEntry.setText(0, item._strName);

									if (_items[i]._strVersionAvailable != null)
										tableItem.setText(1, _items[i]._strVersionAvailable);

									if (_items[i]._strVendorName != null)
										tableItem.setText(2, _items[i]._strVendorName);
								}
							}
						}
					}
				}

				else if (_items[i]._iType == UpdateManagerConstants.TYPE_COMPONENT) {

					if (_items[i]._strName != null)
						tableItem.setText(0, _items[i]._strName);

					if (_items[i]._strVersionAvailable != null)
						tableItem.setText(1, _items[i]._strVersionAvailable);

					if (_items[i]._strVendorName != null)
						tableItem.setText(2, _items[i]._strVendorName);
				}

				else if (_items[i]._iType == UpdateManagerConstants.TYPE_COMPONENT_ENTRY) {

					if (_items[i]._strName != null)
						tableItem.setText(0, _items[i]._strName);

					if (_items[i]._strVersionAvailable != null)
						tableItem.setText(1, _items[i]._strVersionAvailable);

					if (_items[i]._strVendorName != null)
						tableItem.setText(2, _items[i]._strVendorName);
				}

				tableItem.setExpanded(true);
			}
		}

		return;
	}
	/**
	 * 
	 */
	public void setVisible(boolean bVisible) {

		if (bVisible == true)
			initializeContent();

		super.setVisible(bVisible);
	}
}