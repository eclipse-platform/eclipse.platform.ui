package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Vector;import org.eclipse.core.internal.boot.LaunchInfo;import org.eclipse.core.internal.boot.update.ComponentDescriptor;import org.eclipse.core.internal.boot.update.IComponentDescriptor;import org.eclipse.core.internal.boot.update.IManifestDescriptor;import org.eclipse.core.internal.boot.update.IProductDescriptor;import org.eclipse.core.internal.boot.update.IUMRegistry;import org.eclipse.core.internal.boot.update.ProductDescriptor;import org.eclipse.core.internal.boot.update.UMRegistryManager;import org.eclipse.core.internal.boot.update.UpdateManagerConstants;import org.eclipse.jface.dialogs.Dialog;import org.eclipse.jface.dialogs.IDialogConstants;import org.eclipse.jface.dialogs.MessageDialog;import org.eclipse.jface.viewers.ColumnLayoutData;import org.eclipse.jface.viewers.ColumnWeightData;import org.eclipse.jface.viewers.TableLayout;import org.eclipse.jface.wizard.WizardDialog;import org.eclipse.swt.SWT;import org.eclipse.swt.custom.BusyIndicator;import org.eclipse.swt.custom.SashForm;import org.eclipse.swt.custom.TableTree;import org.eclipse.swt.custom.TableTreeItem;import org.eclipse.swt.events.MouseEvent;import org.eclipse.swt.events.MouseMoveListener;import org.eclipse.swt.events.MouseTrackListener;import org.eclipse.swt.events.SelectionEvent;import org.eclipse.swt.events.SelectionListener;import org.eclipse.swt.graphics.Point;import org.eclipse.swt.layout.GridData;import org.eclipse.swt.layout.GridLayout;import org.eclipse.swt.widgets.Button;import org.eclipse.swt.widgets.Composite;import org.eclipse.swt.widgets.Control;import org.eclipse.swt.widgets.Display;import org.eclipse.swt.widgets.Label;import org.eclipse.swt.widgets.Shell;import org.eclipse.swt.widgets.TableColumn;import org.eclipse.swt.widgets.TableItem;import org.eclipse.swt.widgets.Text;import org.eclipse.update.internal.core.UpdateManager;import org.eclipse.update.internal.core.UpdateManagerException;import org.eclipse.update.internal.core.UpdateManagerStrings;
/**
 * 
 */
public class UMDialog extends Dialog implements MouseMoveListener, MouseTrackListener, SelectionListener {

	protected Button _buttonAdd = null;
	protected Button _buttonUpdateAll = null;
	protected Button _buttonUpdateSelected = null;
	protected Button _buttonRemove = null;
	protected Button _buttonRevert = null;
	protected TableTree _tableTreeItems = null;
	protected boolean _bRestartMessageRequired = false;

	protected SashForm _sashFormTopBottom = null;
	protected Text _textDescription = null;
	protected String _strDescription = null;
	protected UpdateManager _updateManager = null;
	protected IUMRegistry _registry = null;
	protected UMWizardTreeItem _umTreeItemComponentCategory = null;
	protected UMWizardTreeItem _umTreeItemProductCategory = null;

	protected UMWizardPersistentProperties _properties = null;

	protected static final String _strEmpty = new String();
	/**
	 *
	 */
	public UMDialog(Shell shell) {
		super(shell);

		// Persistent Properties
		// The properties are loaded by the constructor
		//---------------------------------------------
		_properties = new UMWizardPersistentProperties();
	}
	/**
	 *
	 */
	public void connectToTree() {

		// Remove all existing tree items
		//-------------------------------
		_tableTreeItems.removeAll();

		// Products
		//---------

		if (_umTreeItemProductCategory != null) {

			// Create child tree items
			//------------------------
			if (_umTreeItemProductCategory._vectorChildren != null) {
				for (int i = 0; i < _umTreeItemProductCategory._vectorChildren.size(); ++i) {
					connectToTree((UMWizardTreeItem) _umTreeItemProductCategory._vectorChildren.elementAt(i), null);
				}
			}
		}

		// Components
		//-----------
		if (_umTreeItemComponentCategory != null) {

			// Create child tree items
			//------------------------
			if (_umTreeItemComponentCategory._vectorChildren != null) {
				for (int i = 0; i < _umTreeItemComponentCategory._vectorChildren.size(); ++i) {
					connectToTree((UMWizardTreeItem) _umTreeItemComponentCategory._vectorChildren.elementAt(i), null);
				}
			}
		}

		if (_tableTreeItems.getItemCount() == 0) {
			_buttonUpdateAll.setEnabled(false);
		}
	}
	/**
	 * Connects items to a new tree widget.
	 */
	public void connectToTree(UMWizardTreeItem item, TableTreeItem treeItemParent) {

		// Create a new tree item
		//-----------------------
		TableTreeItem treeItem = null;

		if (treeItemParent != null)
			treeItem = new TableTreeItem(treeItemParent, SWT.NULL);
		else
			treeItem = new TableTreeItem(_tableTreeItems, SWT.NULL);

		treeItem.setText(0, item._strName);

		if (item._iType == UpdateManagerConstants.TYPE_COMPONENT || item._iType == UpdateManagerConstants.TYPE_PRODUCT) {

			if (item._strVersionCurrent != null)
				treeItem.setText(1, item._strVersionCurrent);

			if (item._strVendorName != null)
				treeItem.setText(2, item._strVendorName);
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
	 * Add buttons to the dialog's button bar.
	 *
	 * Subclasses should override.
	 *
	 * @param parent the button bar composite
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}
	/**
	 * Creates and returns the contents of the upper part 
	 * of the dialog (above the button bar).
	 */
	protected Control createDialogArea(Composite compositeParent) {

		getShell().setText(UpdateManagerStrings.getString("S_Software_Updates"));

		// Content
		//--------
		Composite compositeContent = new Composite(compositeParent, SWT.NULL);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		//	gridLayout.marginWidth = 0;
		//	gridLayout.marginHeight = 0;
		compositeContent.setLayout(gridLayout);

		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		compositeContent.setLayoutData(gridData);

		// Label: Installed Products and Components
		//-----------------------------------------
		Label label = new Label(compositeContent, SWT.LEFT);
		label.setText(UpdateManagerStrings.getString("S_Installed_Components"));
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);

		// SashForm
		//---------
		_sashFormTopBottom = new SashForm(compositeContent, SWT.VERTICAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.widthHint = 400;
		gridData.heightHint = 400;
		_sashFormTopBottom.setLayoutData(gridData);

		// Tree: Installable
		//------------------
		_tableTreeItems = new TableTree(_sashFormTopBottom, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER /* | SWT.CHECK */);

		String[] columnTitles = { UpdateManagerStrings.getString("S_Component"), UpdateManagerStrings.getString("S_Version"), UpdateManagerStrings.getString("S_Provider")};
		int[] iColumnWeight = { 50, 25, 25 };
		TableLayout layoutTable = new TableLayout();

		for (int i = 0; i < columnTitles.length; i++) {
			TableColumn tableColumn = new TableColumn(_tableTreeItems.getTable(), SWT.NONE);
			tableColumn.setText(columnTitles[i]);
			ColumnLayoutData cLayout = new ColumnWeightData(iColumnWeight[i], true);
			layoutTable.addColumnData(cLayout);
		}

		_tableTreeItems.getTable().setLinesVisible(true);
		_tableTreeItems.getTable().setHeaderVisible(true);
		_tableTreeItems.getTable().setLayout(layoutTable);
		_tableTreeItems.addSelectionListener(this);
		_tableTreeItems.getTable().addMouseMoveListener(this);
		_tableTreeItems.getTable().addMouseTrackListener(this);
		_tableTreeItems.addSelectionListener(this);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		_tableTreeItems.setLayoutData(gridData);

		// Text: Description
		//------------------
		_textDescription = new Text(_sashFormTopBottom, SWT.WRAP | SWT.READ_ONLY | SWT.MULTI | SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.widthHint = 500;
		gridData.heightHint = 50;
		_textDescription.setLayoutData(gridData);

		_sashFormTopBottom.setWeights(new int[] { 80, 20 });

		// Buttons
		//--------
		Composite compositeGroup = new Composite(compositeContent, SWT.NULL);
		gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		compositeGroup.setLayout(gridLayout);
		compositeGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		// Button: Add
		//------------
		_buttonAdd = new Button(compositeGroup, SWT.PUSH);
		_buttonAdd.setText(UpdateManagerStrings.getString("S_Add") + "...");
		_buttonAdd.setToolTipText(UpdateManagerStrings.getString("S_Search_for_new_components"));
		_buttonAdd.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		_buttonAdd.addSelectionListener(this);

		// Button: Remove
		//---------------	
		_buttonRemove = new Button(compositeGroup, SWT.PUSH);
		_buttonRemove.setText(UpdateManagerStrings.getString("S_Remove"));
		_buttonRemove.setToolTipText(UpdateManagerStrings.getString("S_Remove_selected_components"));
		_buttonRemove.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		_buttonRemove.addSelectionListener(this);

		// Button: Update
		//---------------	
		_buttonUpdateSelected = new Button(compositeGroup, SWT.PUSH);
		_buttonUpdateSelected.setText(UpdateManagerStrings.getString("S_Update") + "...");
		_buttonUpdateSelected.setToolTipText(UpdateManagerStrings.getString("S_Search_for_updates_for_selected_components"));
		_buttonUpdateSelected.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		_buttonUpdateSelected.addSelectionListener(this);

		// Button: Update All
		//-------------------
		_buttonUpdateAll = new Button(compositeGroup, SWT.PUSH);
		_buttonUpdateAll.setText(UpdateManagerStrings.getString("S_Update_All") + "...");
		_buttonUpdateAll.setToolTipText(UpdateManagerStrings.getString("S_Search_for_updates_for_all_components"));
		_buttonUpdateAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		_buttonUpdateAll.addSelectionListener(this);

		Label labelSeparator = new Label(compositeGroup, SWT.NULL);
		labelSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Button: Revert
		//---------------
		_buttonRevert = new Button(compositeGroup, SWT.PUSH);
		_buttonRevert.setText(UpdateManagerStrings.getString("S_Revert") + "...");
		_buttonRevert.setToolTipText(UpdateManagerStrings.getString("S_Revert_to_another_installation"));
		_buttonRevert.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		_buttonRevert.addSelectionListener(this);

		// Separator line
		//---------------
		labelSeparator = new Label(compositeContent, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		labelSeparator.setLayoutData(gridData);

		updateEnabledState();

		// Initialization
		//---------------
		_bRestartMessageRequired = false;
		_sashFormTopBottom.layout();
		initializeContent();

		return compositeContent;
	}
	/**
	 * 
	 */
	public void doDisplayNewWizard() {

		UMWizardProductComponentNew wizard = new UMWizardProductComponentNew(this);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);

		// Display the dialog as modal / wait
		//-----------------------------------
		dialog.open();
	}
	/**
	 * 
	 */
	public void doDisplayRevertWizard() {

		UMWizardProductComponentRevert wizard = new UMWizardProductComponentRevert(this);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);

		// Display the dialog as modal / wait
		//-----------------------------------
		dialog.open();

	}
	/**
	 * 
	 */
	public void doDisplayUpdateWizard(boolean bSelectedOnly) {

		IManifestDescriptor[] manifestDescriptors = null;

		// Collect selected, or all manifest descriptors
		//----------------------------------------------
		TableTreeItem[] items = bSelectedOnly == true ? _tableTreeItems.getSelection() : _tableTreeItems.getItems();

		Vector vectorItems = new Vector();

		if (items.length > 0) {

			for (int i = 0; i < items.length; ++i) {

				UMWizardTreeItem umTreeItem = (UMWizardTreeItem) items[i].getData();
				if (umTreeItem != null && umTreeItem._descriptorCurrent != null) {
					vectorItems.add(umTreeItem._descriptorCurrent);
				}
			}

			manifestDescriptors = new IManifestDescriptor[vectorItems.size()];

			vectorItems.copyInto(manifestDescriptors);
		}

		UMWizardProductComponentUpdate wizard = new UMWizardProductComponentUpdate(this, manifestDescriptors);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);

		// Display the dialog as modal / wait
		//-----------------------------------
		dialog.open();
	}
	/**
	 * 
	 */
	public void doRemove() {

		// Collect selected manifest descriptors
		//--------------------------------------
		if (_tableTreeItems.getSelectionCount() > 0) {

			TableTreeItem[] itemsSelected = _tableTreeItems.getSelection();

			Vector vectorItemsToRemove = new Vector();

			int[] iSelected = _tableTreeItems.getTable().getSelectionIndices();

			UMWizardTreeItem[] umTreeItems = new UMWizardTreeItem[itemsSelected.length];

			for (int i = 0; i < itemsSelected.length; ++i) {
				umTreeItems[i] = (UMWizardTreeItem) itemsSelected[i].getData();
			}

			// Do the remove
			//--------------
			//		_tableTreeItems.getTable().remove(iSelected);

			IManifestDescriptor manifest = null;

			for (int i = 0; i < umTreeItems.length; ++i) {

				if (umTreeItems[i] != null) {
					manifest = umTreeItems[i]._descriptorCurrent;

					if (manifest instanceof IProductDescriptor) {
						_updateManager.removeProduct((IProductDescriptor) manifest);
						_umTreeItemProductCategory._vectorChildren.remove(umTreeItems[i]);
					}

					else if (manifest instanceof IComponentDescriptor) {
						_updateManager.removeComponent((IComponentDescriptor) manifest);
						_umTreeItemComponentCategory._vectorChildren.remove(umTreeItems[i]);
					}
				}
			}

			// Update the displayed items
			//---------------------------
			initializeContent();

			// Notify user that restart is necessary
			//--------------------------------------
			setRestartMessageRequired(true);
		}
	}
	/**
	 *
	 */
	protected void finish() {
		if (_bRestartMessageRequired == true) {
			MessageDialog.openInformation(getShell(), UpdateManagerStrings.getString("S_Software_Updates"), UpdateManagerStrings.getString("S_You_must_restart_the_workbench_to_activate_any_changes"));
			LaunchInfo.getCurrent().flush();
		}
	}
	/**
	 */
	public UpdateManager getUpdateManager() {
		return _updateManager;
	}
	/**
	 * 
	 */
	public void initializeContent() {

		// Update Manager
		//---------------
		if (_updateManager == null) {
			_updateManager = new UpdateManager(getShell());

			try {
				_updateManager.initialize();
			}

			catch (UpdateManagerException ex) {
			}
		}

		// Obtain local registry
		//----------------------
		final UMRegistryManager registryManager = _updateManager.getRegistryManager();

		// Update tree with location information
		//--------------------------------------
		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			public void run() {
				_registry = registryManager.getCurrentRegistry();
			}
		});

		UMWizardTreeItem itemProduct = null;
		UMWizardTreeItem itemProductCategory = null;
		UMWizardTreeItem itemComponent = null;
		UMWizardTreeItem itemComponentCategory = null;
		UMWizardTreeItem itemLocation = null;

		// Initialize with products
		//-------------------------
		IProductDescriptor[] descriptorsProduct = _registry.getProductDescriptors();

		if (descriptorsProduct.length > 0) {

			_umTreeItemProductCategory = new UMWizardTreeItem();
			_umTreeItemProductCategory._iType = UpdateManagerConstants.TYPE_PRODUCT_CATEGORY;
			_umTreeItemProductCategory._strName = "products";

			for (int i = 0; i < descriptorsProduct.length; ++i) {

				itemProduct = new UMWizardTreeItem();
				itemProduct._iType = UpdateManagerConstants.TYPE_PRODUCT;
				itemProduct._strDescription = descriptorsProduct[i].getDescription();
				itemProduct._strName = descriptorsProduct[i].getLabel();
				itemProduct._strId = descriptorsProduct[i].getUniqueIdentifier();
				itemProduct._strVendorName = descriptorsProduct[i].getProviderName();
				itemProduct._strVersionCurrent = descriptorsProduct[i].getVersionStr();
				itemProduct._descriptorCurrent = descriptorsProduct[i];

				_umTreeItemProductCategory.addChildItem(itemProduct);
			}
		}

		// Initialize with components not associated with products
		//--------------------------------------------------------
		IComponentDescriptor[] descriptorsComponent = _registry.getDanglingComponents();

		if (descriptorsComponent.length > 0) {

			_umTreeItemComponentCategory = new UMWizardTreeItem();
			_umTreeItemComponentCategory._iType = UpdateManagerConstants.TYPE_COMPONENT_CATEGORY;
			_umTreeItemComponentCategory._strName = "components";

			for (int i = 0; i < descriptorsComponent.length; ++i) {

				itemComponent = new UMWizardTreeItem();

				itemComponent._iType = UpdateManagerConstants.TYPE_COMPONENT;
				itemComponent._strDescription = descriptorsComponent[i].getDescription();
				itemComponent._strName = descriptorsComponent[i].getLabel();
				itemComponent._strId = descriptorsComponent[i].getUniqueIdentifier();
				itemComponent._strVendorName = descriptorsComponent[i].getProviderName();
				itemComponent._strVersionCurrent = descriptorsComponent[i].getVersionStr();
				itemComponent._descriptorCurrent = descriptorsComponent[i];

				_umTreeItemComponentCategory.addChildItem(itemComponent);
			}
		}

		// Create tree widget items
		//-------------------------
		connectToTree();

		updateEnabledState();
	}
	/**
	 * Sent when the mouse pointer passes into the area of
	 * the screen covered by a control.
	 */
	public void mouseEnter(org.eclipse.swt.events.MouseEvent e) {
	}
	/**
	 * Sent when the mouse pointer passes out of the area of
	 * the screen covered by a control.
	 */
	public void mouseExit(org.eclipse.swt.events.MouseEvent e) {
		if (_strDescription != _strEmpty) {
			_textDescription.setText(_strEmpty);
			_strDescription = _strEmpty;
		}
	}
	/**
	 * Sent when the mouse pointer hovers (that is, stops moving
	 * for an (operating system specified) period of time) over
	 * a control.
	 */
	public void mouseHover(org.eclipse.swt.events.MouseEvent e) {
	}
	/**
	 * Sent when the mouse moves.
	 */
	public void mouseMove(org.eclipse.swt.events.MouseEvent e) {

		// Table item
		//-----------
		TableItem tableItem = _tableTreeItems.getTable().getItem(new Point(e.x, e.y));

		// TableTree item
		//---------------
		if (tableItem != null) {

			TableTreeItem tableTreeItem = (TableTreeItem) tableItem.getData();

			// UMWizardTreeItem
			//---------------------- 
			if (tableTreeItem != null) {

				UMWizardTreeItem item = (UMWizardTreeItem) tableTreeItem.getData();

				// Description string
				//-------------------
				if (item != null) {
					if (item._strDescription != null) {
						if (_strDescription != item._strDescription) {
							_textDescription.setText(item._strDescription);
							_strDescription = item._strDescription;
						}
					}
					else {
						if (_strDescription != _strEmpty) {
							_textDescription.setText(_strEmpty);
							_strDescription = _strEmpty;
						}
					}
				}
				else if (_strDescription != _strEmpty) {
					_textDescription.setText(_strEmpty);
					_strDescription = _strEmpty;
				}

			}
		}
		else if (_strDescription != _strEmpty) {
			_textDescription.setText(_strEmpty);
			_strDescription = _strEmpty;
		}
	}
	/**
	 * Notifies that the ok button of this dialog has been pressed.
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method sets
	 * this dialog's return code to <code>Window.OK</code>
	 * and closes the dialog. Subclasses may override.
	 * </p>
	 */
	protected void okPressed() {

		finish();
		super.okPressed();
	}
	/**
	 */
	public void setRestartMessageRequired(boolean bRequired) {
		_bRestartMessageRequired = bRequired;
	}
	/**
	 * Enables or disables the edit and remove buttons.
	 */
	protected void updateEnabledState() {

		if (LaunchInfo.getCurrent().isUpdateEnabled() == false) {
			_buttonAdd.setEnabled(false);
			_buttonUpdateAll.setEnabled(false);
			_buttonUpdateSelected.setEnabled(false);
			_buttonRemove.setEnabled(false);
			_buttonRevert.setEnabled(false);
		}

		else {

			boolean bScriptSelected = _tableTreeItems.getSelectionCount() > 0;

			// Update button
			//--------------
			_buttonUpdateSelected.setEnabled(bScriptSelected);

			// Update All button
			//------------------
			if (_tableTreeItems.getItemCount() > 0) {
				_buttonUpdateAll.setEnabled(true);
			}
			else {
				_buttonUpdateAll.setEnabled(false);
			}

			// Remove button
			// Disable the button if any one of the selected items
			// cannot be removed
			//----------------------------------------------------
			boolean bEnableRemoveButton = false;

			if (bScriptSelected == true) {

				bEnableRemoveButton = true;

				TableTreeItem[] items = _tableTreeItems.getSelection();

				for (int i = 0; i < items.length; ++i) {

					UMWizardTreeItem umTreeItem = (UMWizardTreeItem) items[i].getData();

					if (umTreeItem != null) {
						if (umTreeItem._descriptorCurrent instanceof ProductDescriptor) {
							if (((ProductDescriptor) umTreeItem._descriptorCurrent).isRemovable() == false) {
								bEnableRemoveButton = false;
								break;
							}
						}
						else if (umTreeItem._descriptorCurrent instanceof ComponentDescriptor) {
							if (((ComponentDescriptor) umTreeItem._descriptorCurrent).isRemovable() == false) {
								bEnableRemoveButton = false;
								break;
							}
						}
					}
				}
			}

			_buttonRemove.setEnabled(bEnableRemoveButton);
		}
	}
	/**
	 * Sent when default selection occurs in the control.
	 */
	public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
	}
	/**
	 * Sent when selection occurs in the control.
	 */
	public void widgetSelected(SelectionEvent event) {

		// Add button pressed
		//-------------------
		if (event.widget == _buttonAdd) {
			doDisplayNewWizard();
		}

		// Update selected button pressed
		//-------------------------------
		else if (event.widget == _buttonUpdateSelected) {
			doDisplayUpdateWizard(true);
		}

		// Update all button pressed
		//--------------------------
		else if (event.widget == _buttonUpdateAll) {
			doDisplayUpdateWizard(false);
		}

		// Remove button pressed
		//----------------------
		else if (event.widget == _buttonRemove) {
			doRemove();
		}

		// Revert button pressed
		//----------------------
		else if (event.widget == _buttonRevert) {
			doDisplayRevertWizard();
		}

		// Table: Installed items
		//-----------------------
		else if (event.widget == _tableTreeItems) {

			if (_tableTreeItems.getSelectionCount() > 0) {

				boolean bSelectionUpdateRequired = false;

				TableTreeItem[] itemsSelected = _tableTreeItems.getSelection();

				Vector vectorItemsToSelect = new Vector();

				for (int i = 0; i < itemsSelected.length; ++i) {

					UMWizardTreeItem item = (UMWizardTreeItem) itemsSelected[i].getData();

					if (item._iType == UpdateManagerConstants.TYPE_PRODUCT || item._iType == UpdateManagerConstants.TYPE_COMPONENT) {
						vectorItemsToSelect.add(itemsSelected[i]);
					}

					else {
						// Need to deselect
						//-----------------
						bSelectionUpdateRequired = true;
					}
				}

				if (bSelectionUpdateRequired == true) {

					TableTreeItem[] itemsToSelect = new TableTreeItem[vectorItemsToSelect.size()];

					Object[] objArray = vectorItemsToSelect.toArray();
					System.arraycopy(objArray, 0, itemsToSelect, 0, vectorItemsToSelect.size());
					_tableTreeItems.setSelection(itemsToSelect);
				}
			}
		}

		updateEnabledState();
	}
}