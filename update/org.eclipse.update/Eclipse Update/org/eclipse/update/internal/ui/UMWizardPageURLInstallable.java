package org.eclipse.update.internal.ui;

/**
 * Presents update URL locations for selection.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import java.net.*;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.core.internal.boot.update.*;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Iterator;

public class UMWizardPageURLInstallable extends WizardPage implements SelectionListener
{
	protected boolean   _bRefreshRequired = true;
	protected UMWizard  _wizard          = null;
	protected Label     _labelLocation   = null;
	protected TableTree _tableTreeItems  = null;
	protected IUMRegistry _registry = null;
	protected IManifestDescriptor _descriptor = null;
/**
 * ScriptNewScriptWizardPage1 constructor comment.
 * @param name java.lang.String
 */
public UMWizardPageURLInstallable( UMWizard wizard, String strName )
{
	super( strName );
	_wizard = wizard;
	
	this.setTitle(UpdateManagerStrings.getString("S_Install_Components"));
	this.setDescription(UpdateManagerStrings.getString("S_Select_any_optional_components_to_install"));
}
/**
 * 
 */
public void connectToTree(UMWizardTreeItem item) {

	// Remove all existing tree items
	//-------------------------------
	_tableTreeItems.removeAll();

	TableTreeItem treeItem = new TableTreeItem(_tableTreeItems, SWT.NULL);

	if (item._strName != null)
		treeItem.setText(0, item._strName);

	if (item._strVersionCurrent != null)
		treeItem.setText(2, item._strVersionCurrent);

	if (item._strVendorName != null)
		treeItem.setText(3, item._strVendorName);

	treeItem.setChecked(true);

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
 * Connects items to a new tree widget.
 * @param tree org.eclipse.swt.widgets.Tree
 */
public void connectToTree(UMWizardTreeItem item, TableTreeItem treeItemParent) {

	// All of these should be component entries
	//-----------------------------------------
	TableTreeItem treeItem = new TableTreeItem(treeItemParent, SWT.NULL);
	treeItem.setText(0, item._strName);

	if (item._iType == UpdateManagerConstants.TYPE_COMPONENT_ENTRY) {

		if (item._descriptorEntry.isOptionalForInstall() == true) {
			treeItem.setText(1, UpdateManagerStrings.getString("S_optional"));
		}

		if (item._strVendorName != null)
			treeItem.setText(3, item._strVendorName);
	}

	treeItem.setChecked(true);
	treeItem.setData(item);
	treeItem.setExpanded(true);
}
/**
 * createContents method comment.
 */
public void createControl( Composite compositeParent )
{
	// Content
	//--------
	Composite compositeContent = new Composite( compositeParent, SWT.NULL );

	GridLayout layout= new GridLayout();
	layout.numColumns = 2;
	compositeContent.setLayout( layout );
	
	GridData gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.verticalAlignment   = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.grabExcessVerticalSpace   = true;
	compositeContent.setLayoutData( gridData );

	// Label: Location
	//----------------
	Label label = new Label( compositeContent, SWT.NULL );
	label.setText( UpdateManagerStrings.getString("S_Source_location") + ": ");

	_labelLocation = new Label( compositeContent, SWT.NULL );
	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	_labelLocation.setLayoutData( gridData );

	// Tree: Installable
	//------------------
	_tableTreeItems = new TableTree(compositeContent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER  | SWT.CHECK );

	String[] columnTitles = { UpdateManagerStrings.getString("S_Component"), UpdateManagerStrings.getString("S_Status"), UpdateManagerStrings.getString("S_Version"), UpdateManagerStrings.getString("S_Provider")};
	int[] iColumnWeight = { 40, 20, 20, 20 };
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
	gridData.horizontalSpan = 2;
	gridData.grabExcessHorizontalSpace = true;
	gridData.grabExcessVerticalSpace   = true;
	_tableTreeItems.setLayoutData(gridData);
	
	_tableTreeItems.getTable().setLinesVisible(true);
	_tableTreeItems.getTable().setHeaderVisible(true);
	_tableTreeItems.getTable().setLayout(layout);
	_tableTreeItems.getTable().setLayout(layoutTable);
	_tableTreeItems.addSelectionListener(this);
		
	setControl( compositeContent );

	setPageComplete(false);
}
/**
 * 
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

	if (_bRefreshRequired == false)
		return;

	// Obtain the descriptor to be installed from the install page
	//------------------------------------------------------------
	UMWizardPageURLInstall pageInstall = (UMWizardPageURLInstall) _wizard.getPage("install");

	_descriptor = pageInstall.getDescriptor();

	// Determine whether we need to look for a newer version of the descriptor
	//------------------------------------------------------------------------
	if (pageInstall.isLookForLaterVersionsChecked() == true) {

		final IURLNamePair[] urlNamePairs = _descriptor.getUpdateURLs();

		// Search for a remote registry with the latest version
		//-----------------------------------------------------
		if (urlNamePairs != null) {

			// Obtain a registry for each update URL
			//--------------------------------------
			final Vector vectorRegistries = new Vector();
			final UMRegistryManager registryManager = _wizard._updateManager.getRegistryManager();

			IRunnableWithProgress operation = new IRunnableWithProgress() {
				public void run(IProgressMonitor progressMonitor) throws InvocationTargetException {

					progressMonitor.beginTask(UpdateManagerStrings.getString("S_Obtaining_information"), urlNamePairs.length);
					URL url = null;

					for (int i = 0; i < urlNamePairs.length; ++i) {

						try {
							url = urlNamePairs[i].getURL();
							progressMonitor.subTask(url.toExternalForm());
							IUMRegistry registry = registryManager.getRegistryAt(url);
							if (registry != null) {
								vectorRegistries.add(registry);
							}

							progressMonitor.worked(1);
						}

						catch (Exception ex) {
						}
					}

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

			// Look for a new version at each update site
			//-------------------------------------------
			if (vectorRegistries.size() > 0) {
				IUMRegistry registry = null;

				// Product
				//--------
				if (_descriptor instanceof IProductDescriptor) {
					IProductDescriptor descriptorPotential = null;

					for (int i = 0; i < vectorRegistries.size(); ++i) {
						registry = (IUMRegistry) vectorRegistries.elementAt(i);

						descriptorPotential = registry.getProductDescriptor(_wizard._strInstallId);

						if (descriptorPotential.isInstallable((IProductDescriptor) _descriptor) == UpdateManagerConstants.OK_TO_INSTALL) {
							_descriptor = descriptorPotential;
						}
					}
				}

				// Component
				//----------
				else if (_descriptor instanceof IComponentDescriptor) {
					IComponentDescriptor descriptorPotential = null;

					for (int i = 0; i < vectorRegistries.size(); ++i) {
						registry = (IUMRegistry) vectorRegistries.elementAt(i);

						descriptorPotential = registry.getComponentDescriptor(_wizard._strInstallId);

						if (descriptorPotential.isInstallable((IComponentDescriptor) _descriptor) == UpdateManagerConstants.OK_TO_INSTALL) {
							_descriptor = descriptorPotential;
						}
					}
				}
			}
		}
	}

	// Create a tree
	//--------------
	UMWizardTreeItem umTreeItemRoot = null;

	// Initialize with product
	//------------------------
	if (_descriptor instanceof IProductDescriptor) {

		UMWizardTreeItem itemProduct = umTreeItemRoot = new UMWizardTreeItem();
		itemProduct._iType = UpdateManagerConstants.TYPE_PRODUCT;
		itemProduct._strName = ((IProductDescriptor) _descriptor).getLabel();
		itemProduct._strVendorName = ((IProductDescriptor) _descriptor).getProviderName();
		itemProduct._strVersionCurrent = ((IProductDescriptor) _descriptor).getVersionStr();
		itemProduct._descriptorCurrent = _descriptor;

		// Component entries of the product
		//---------------------------------
		IComponentEntryDescriptor[] descriptorsEntry = ((IProductDescriptor) _descriptor).getComponentEntries();
		UMWizardTreeItem itemComponentEntry = null;

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

	// Initialize with component
	//--------------------------
	else if (_descriptor instanceof IComponentDescriptor) {

		UMWizardTreeItem itemComponent = umTreeItemRoot = new UMWizardTreeItem();

		itemComponent._iType = UpdateManagerConstants.TYPE_COMPONENT;
		itemComponent._strDescription = ((IComponentDescriptor) _descriptor).getDescription();
		itemComponent._strName = ((IComponentDescriptor) _descriptor).getLabel();
		itemComponent._strId = ((IComponentDescriptor) _descriptor).getUniqueIdentifier();
		itemComponent._strVendorName = ((IComponentDescriptor) _descriptor).getProviderName();
		itemComponent._strVersionCurrent = ((IComponentDescriptor) _descriptor).getVersionStr();
		itemComponent._descriptorCurrent = _descriptor;
	}

	// Create tree widget items
	//-------------------------
	connectToTree(umTreeItemRoot);

	if (_descriptor != null) {
		_labelLocation.setText(_descriptor.getUMRegistry().getRegistryBaseURL().toExternalForm());
		setPageComplete(true);
	}

	_bRefreshRequired = false;

	return;
}
/**
 * 
 */
public void setVisible(boolean bVisible) {
	
	super.setVisible(bVisible);
	
	if (bVisible == true) {
//		_sashFormTopBottom.layout();
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
 * <p>
 * For example, on some platforms default selection occurs
 * in a List when the user double-clicks an item.
 * </p>
 *
 * @param e an event containing information about the default selection
 */
public void widgetDefaultSelected(SelectionEvent e) {}
/**
 * widgetSelected method comment.
 */
public void widgetSelected(SelectionEvent e) {

	if (e.widget == _tableTreeItems && e.detail == SWT.CHECK) {

		TableTreeItem treeItem = (TableTreeItem) e.item;

		UMWizardTreeItem umTreeItem = (UMWizardTreeItem) treeItem.getData();

		// Unchecked
		//----------
		if (treeItem.getChecked() == false) {
			
			// Disallow unchecking of products/components
			//-------------------------------------------
			if (umTreeItem._iType == UpdateManagerConstants.TYPE_PRODUCT || umTreeItem._iType == UpdateManagerConstants.TYPE_COMPONENT) {
				treeItem.setChecked(true);
			}

			// Allow unchecking of optional components
			//----------------------------------------
			else if (umTreeItem._iType == UpdateManagerConstants.TYPE_COMPONENT_ENTRY) {
				
				// Disallow unchecking of optional component
				//------------------------------------------
				if (umTreeItem._descriptorEntry.isOptionalForInstall() == false) {
					treeItem.setChecked(true);
				}

				// Indicate selection state within component entry
				//------------------------------------------------
				else {
					umTreeItem._descriptorEntry.isSelected(false);
				}
			}
		}

		// Checked
		//--------
		else if (umTreeItem._iType == UpdateManagerConstants.TYPE_COMPONENT_ENTRY) {
			
			// Indicate selection state within component entry
			//------------------------------------------------
			umTreeItem._descriptorEntry.isSelected(true);
		}
	}

	return;
}
}
