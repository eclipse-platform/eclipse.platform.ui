package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import org.eclipse.core.internal.boot.update.IComponentDescriptor;
import org.eclipse.core.internal.boot.update.IComponentEntryDescriptor;
import org.eclipse.core.internal.boot.update.IManifestDescriptor;
import org.eclipse.core.internal.boot.update.IProductDescriptor;
import org.eclipse.core.internal.boot.update.IUMRegistry;
import org.eclipse.core.internal.boot.update.UMEclipseTree;
import org.eclipse.core.internal.boot.update.URLNamePair;
import org.eclipse.core.internal.boot.update.UpdateManagerConstants;
import org.eclipse.core.internal.boot.update.BaseURLHandler.Response;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.update.internal.core.URLHandler;
import org.eclipse.update.internal.core.UpdateManagerStrings;
import org.eclipse.webdav.http.client.IStatusCodes;

/**
 * Presents products and components that may be selected for installation.
 */
public class UMWizardPageInstallable extends WizardPage implements MouseMoveListener, MouseTrackListener, SelectionListener {
	protected boolean _bRefreshRequired = true;
	protected UMWizard _wizard = null;
	protected TableTree _tableTreeItems = null;
	protected Hashtable _hashRegistryCache = new Hashtable();
	protected TreeSet _treeSetItems = null;
	protected TreeSet _treeSetLocations = null;
	protected Text _textDescription = null;
	protected String _strDescription = null;
	protected boolean _bUpdateMode = false;
	protected SashForm _sashFormTopBottom = null;
	protected IProgressMonitor _progressMonitor = null;
	protected URLNamePair[] _urlNamePairs = null;
	protected Button _buttonShowLatestOnly = null;
	protected boolean _bShowLatestOnly = true;
	/**
	 *
	 */
	public UMWizardPageInstallable(UMWizard wizard, String strName, boolean bUpdateMode) {
		super(strName);
		_wizard = wizard;
		_bUpdateMode = bUpdateMode;

		if (bUpdateMode == true) {
			this.setTitle(UpdateManagerStrings.getString("S_New_Component_Updates"));
			this.setDescription(UpdateManagerStrings.getString("S_Select_available_versions_to_install"));
		}
		else {
			this.setTitle(UpdateManagerStrings.getString("S_New_Components"));
			this.setDescription(UpdateManagerStrings.getString("S_Select_components_to_install"));
		}
	}
	/**
	 * 
	 */
	public void addLocationToTree(IUMRegistry registryRemote, IUMRegistry registryLocal, IProgressMonitor progressMonitor) {

		UMWizardTreeItem itemProduct = null;
		UMWizardTreeItem itemComponent = null;
		UMWizardTreeItem itemComponentEntry = null;
		UMWizardTreeItem itemLocation = null;
		UMWizardTreeItem itemComponentCategory = null;
		UMWizardTreeItem itemProductCategory = null;

		IProductDescriptor descriptorLocalProduct = null;
		IProductDescriptor[] descriptorsProduct = null;
		IComponentDescriptor descriptorLocalComponent = null;
		IComponentDescriptor[] descriptorsComponent = null;
		IComponentEntryDescriptor[] descriptorsEntry = null;

		// Obtain descriptors
		//-------------------
		if (_bShowLatestOnly == true) {
			descriptorsProduct = registryRemote.getProductDescriptors();
			descriptorsComponent = registryRemote.getComponentDescriptors();
		}
		else {
			descriptorsProduct = registryRemote.getAllProductDescriptors();
			descriptorsComponent = registryRemote.getAllComponentDescriptors();
		}

		if (descriptorsProduct.length + descriptorsComponent.length > 0) {

			itemLocation = new UMWizardTreeItem();
			itemLocation._iType = UpdateManagerConstants.TYPE_URL;
			itemLocation._strDescription = registryRemote.getRegistryBaseURL().toExternalForm();
			itemLocation._strName = getURLDescription(itemLocation._strDescription);
			itemLocation._strURLUpdate = itemLocation._strName;

			progressMonitor.beginTask(UpdateManagerStrings.getString("S_Examining") + ":", descriptorsProduct.length + descriptorsComponent.length);
			progressMonitor.subTask(itemLocation._strName);

			IManifestDescriptor[] descriptorsSelected = _wizard._manifestDescriptors;

			boolean bIncludeThisOne = false;

			// Products
			//---------
			if (descriptorsProduct.length > 0) {

				itemProductCategory = new UMWizardTreeItem();
				itemProductCategory._iType = UpdateManagerConstants.TYPE_PRODUCT_CATEGORY;
				itemProductCategory._strName = "products";

				// Add product category to location tree later
				//--------------------------------------------
				IProductDescriptor descriptorProductSelected = null;

				for (int i = 0; i < descriptorsProduct.length; ++i) {

					// Include all if selected descriptors is null
					// Otherwise, include only those selected
					//--------------------------------------------
					bIncludeThisOne = true;

					if (descriptorsSelected != null) {
						bIncludeThisOne = false;

						for (int j = 0; j < descriptorsSelected.length; ++j) {
							if (descriptorsSelected[j] instanceof IProductDescriptor) {
								descriptorProductSelected = (IProductDescriptor) descriptorsSelected[j];
								if (descriptorProductSelected.getUniqueIdentifier() != null) {
									if (descriptorProductSelected.getUniqueIdentifier().equals(descriptorsProduct[i].getUniqueIdentifier()) == true) {
										bIncludeThisOne = true;
										break;
									}
								}
							}
						}
					}

					// Display only non-installed items
					//---------------------------------
					if (_bUpdateMode == false) {
						if (registryLocal.getProductDescriptor(descriptorsProduct[i].getUniqueIdentifier()) != null) {
							bIncludeThisOne = false;
						}
					}

					if (bIncludeThisOne == true) {
						itemProduct = new UMWizardTreeItem();
						itemProduct._iType = UpdateManagerConstants.TYPE_PRODUCT;
						itemProduct._strDescription = descriptorsProduct[i].getDescription();
						itemProduct._strName = descriptorsProduct[i].getLabel();
						itemProduct._strId = descriptorsProduct[i].getUniqueIdentifier();
						itemProduct._strVendorName = descriptorsProduct[i].getProviderName();
						itemProduct._strVersionAvailable = descriptorsProduct[i].getVersionStr();
						itemProduct._descriptorAvailable = descriptorsProduct[i];

						descriptorLocalProduct = registryLocal.getProductDescriptor(itemProduct._strId);

						if (descriptorLocalProduct != null) {
							itemProduct._descriptorCurrent = descriptorLocalProduct;
							itemProduct._strVersionCurrent = descriptorLocalProduct.getVersionStr();
						}

						itemProductCategory.addChildItem(itemProduct);

						// Component entries of the product
						//---------------------------------
						descriptorsEntry = descriptorsProduct[i].getComponentEntries();

						for (int j = 0; j < descriptorsEntry.length; ++j) {

							// Turn off selected flag
							//-----------------------
							descriptorsEntry[j].isSelected(false);
							itemComponentEntry = new UMWizardTreeItem();
							itemComponentEntry._iType = UpdateManagerConstants.TYPE_COMPONENT_ENTRY;
							itemComponentEntry._strName = descriptorsEntry[j].getLabel();
							itemComponentEntry._strId = descriptorsEntry[j].getUniqueIdentifier();
							itemComponentEntry._strVersionAvailable = descriptorsEntry[j].getVersionStr();
							itemComponentEntry._descriptorEntry = descriptorsEntry[j];

							descriptorLocalComponent = registryLocal.getComponentDescriptor(itemComponentEntry._strId);

							if (descriptorLocalComponent != null) {
								itemComponentEntry._strVersionCurrent = descriptorLocalComponent.getVersionStr();
								itemComponentEntry._strVendorName = descriptorLocalComponent.getProviderName();
								itemComponentEntry._descriptorCurrent = descriptorLocalComponent;
							}

							itemProduct.addChildItem(itemComponentEntry);
						}
					}

					progressMonitor.worked(1);
				}

				// Add component category if there are any components
				//---------------------------------------------------
				if (itemProductCategory._vectorChildren != null && itemProductCategory._vectorChildren.size() > 0) {
					itemLocation.addChildItem(itemProductCategory);
				}
			}

			// Components
			//-----------
			if (descriptorsComponent.length > 0) {

				itemComponentCategory = new UMWizardTreeItem();
				itemComponentCategory._iType = UpdateManagerConstants.TYPE_COMPONENT_CATEGORY;
				itemComponentCategory._strName = "components";

				// Add product category to location tree later
				//--------------------------------------------
				IComponentDescriptor descriptorComponentSelected = null;
				for (int i = 0; i < descriptorsComponent.length; ++i) {

					// Include all if selected descriptors is null
					// Otherwise, include only those selected
					//--------------------------------------------
					bIncludeThisOne = true;
					if (descriptorsSelected != null) {
						bIncludeThisOne = false;
						for (int j = 0; j < descriptorsSelected.length; ++j) {
							if (descriptorsSelected[j] instanceof IComponentDescriptor) {
								descriptorComponentSelected = (IComponentDescriptor) descriptorsSelected[j];
								if (descriptorComponentSelected.getUniqueIdentifier() != null) {
									if (descriptorComponentSelected.getUniqueIdentifier().equals(descriptorsComponent[i].getUniqueIdentifier()) == true) {
										bIncludeThisOne = true;
										break;
									}
								}
							}
						}
					}

					// Display only non-installed items
					//---------------------------------
					if (_bUpdateMode == false) {
						if (registryLocal.getComponentDescriptor(descriptorsComponent[i].getUniqueIdentifier()) != null) {
							bIncludeThisOne = false;
						}
					}

					if (bIncludeThisOne == true) {
						itemComponent = new UMWizardTreeItem();
						itemComponent._iType = UpdateManagerConstants.TYPE_COMPONENT;
						itemComponent._strDescription = descriptorsComponent[i].getDescription();
						itemComponent._strName = descriptorsComponent[i].getLabel();
						itemComponent._strId = descriptorsComponent[i].getUniqueIdentifier();
						itemComponent._strVendorName = descriptorsComponent[i].getProviderName();
						itemComponent._strVersionAvailable = descriptorsComponent[i].getVersionStr();
						itemComponent._descriptorAvailable = descriptorsComponent[i];

						descriptorLocalComponent = registryLocal.getComponentDescriptor(itemComponent._strId);

						if (descriptorLocalComponent != null) {
							itemComponent._strVersionCurrent = descriptorLocalComponent.getVersionStr();
							itemComponent._descriptorCurrent = descriptorLocalComponent;
						}

						itemComponentCategory.addChildItem(itemComponent);
					}
					progressMonitor.worked(1);
				}

				// Add component category if there are any components
				//---------------------------------------------------
				if (itemComponentCategory._vectorChildren != null && itemComponentCategory._vectorChildren.size() > 0) {
					itemLocation.addChildItem(itemComponentCategory);
				}
			}
			progressMonitor.done();

			// Add location if there are any products/components
			//---------------------------------------------------       
			if (itemLocation._vectorChildren != null && itemLocation._vectorChildren.size() > 0) {
				_treeSetItems.add(itemLocation);
			}
		}
	}
	/**
	 *
	 */
	public void connectToTree() {

		// Remove all existing tree items
		//-------------------------------
		_tableTreeItems.removeAll();

		UMWizardTreeItem item = null;

		Iterator iter = _treeSetItems.iterator();

		while (iter.hasNext() == true) {

			item = (UMWizardTreeItem) iter.next();

			// Create a new tree item for the top level (URL)
			//-----------------------------------------------
			TableTreeItem treeItem = new TableTreeItem(_tableTreeItems, SWT.NULL);

			if (item._strName != null)
				treeItem.setText(0, item._strName);

			if (item._strVendorName != null)
				treeItem.setText(4, item._strVendorName);

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
	 */
	public void connectToTree(UMWizardTreeItem item, TableTreeItem treeItemParent) {

		// Ignore product category and component category items
		//-----------------------------------------------------
		TableTreeItem treeItem = treeItemParent;

		// Create a new tree item
		//-----------------------
		if (item._iType == UpdateManagerConstants.TYPE_PRODUCT || item._iType == UpdateManagerConstants.TYPE_COMPONENT || item._iType == UpdateManagerConstants.TYPE_COMPONENT_ENTRY) {

			treeItem = new TableTreeItem(treeItemParent, SWT.NULL);
			treeItem.setText(0, item._strName);
			treeItem.setData(item);

			// Table text
			//-----------
			if (item._strVersionCurrent != null)
				treeItem.setText(1, item._strVersionCurrent);

			if (item._strVersionAvailable != null)
				treeItem.setText(2, item._strVersionAvailable);

			if (item._strVendorName != null)
				treeItem.setText(4, item._strVendorName);

			// Status text
			//------------
			boolean bInstallable = false;

			if (item._iType == UpdateManagerConstants.TYPE_PRODUCT) {
				int iInstallable = isInstallableProduct(treeItem);
				treeItem.setText(3, convertInstallableToString(iInstallable));
			}

			else if (item._iType == UpdateManagerConstants.TYPE_COMPONENT) {
				int iInstallable = isInstallableComponent(treeItem);
				treeItem.setText(3, convertInstallableToString(iInstallable));
			}

			else if (item._iType == UpdateManagerConstants.TYPE_COMPONENT_ENTRY) {

				int iInstallable = isInstallableComponentEntry(treeItem);
				treeItem.setText(3, convertInstallableToString(iInstallable));

				if (iInstallable == UpdateManagerConstants.OK_TO_INSTALL) {
					// Optional installation
					//---------------------- 
					if (item._descriptorEntry.isOptionalForInstall() == true) {
						treeItem.setText(3, UpdateManagerStrings.getString("S_optional"));
					}
				}
				else {
					treeItem.setText(3, convertInstallableToString(iInstallable));
				}
			}
		}

		// Create child tree items
		//------------------------
		if (item._vectorChildren != null) {
			for (int i = 0; i < item._vectorChildren.size(); ++i) {
				connectToTree((UMWizardTreeItem) item._vectorChildren.elementAt(i), treeItem);
			}
		}

		// Expand only after children have been created
		//---------------------------------------------
		if (item._iType != UpdateManagerConstants.TYPE_PRODUCT)
			treeItem.setExpanded(true);

		return;
	}
	/**
	 * Converts installable constant to a displayable string
	 */
	public String convertInstallableToString(int iInstallable) {

		switch (iInstallable) {
			case UpdateManagerConstants.NOT_COMPATIBLE :
				return UpdateManagerStrings.getString("S_not_compatible");
			case UpdateManagerConstants.NOT_NEWER :
				return UpdateManagerStrings.getString("S_not_newer");
			case UpdateManagerConstants.NOT_UPDATABLE :
				return UpdateManagerStrings.getString("S_not_updatable");
			case UpdateManagerConstants.NOT_AVAILABLE :
				return UpdateManagerStrings.getString("S_not_available");
			default :
				return "";
		}
	}
	/**
	 * createContents method comment.
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

		// Checkbox: Show most recent versions only
		//-----------------------------------------
		_buttonShowLatestOnly = new Button(compositeContent, SWT.CHECK);
		_buttonShowLatestOnly.setText(UpdateManagerStrings.getString("S_Show_only_most_recent_versions"));
		_buttonShowLatestOnly.setSelection(_bShowLatestOnly);
		_buttonShowLatestOnly.addSelectionListener(this);

		// SashPane Top/Bottom
		//--------------------
		_sashFormTopBottom = new SashForm(compositeContent, SWT.VERTICAL);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		_sashFormTopBottom.setLayoutData(gridData);

		// Tree: Installable
		//------------------
		_tableTreeItems = new TableTree(_sashFormTopBottom, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.CHECK);

		String[] columnTitles = { UpdateManagerStrings.getString("S_Component"), UpdateManagerStrings.getString("S_Installed"), UpdateManagerStrings.getString("S_Available"), UpdateManagerStrings.getString("S_Status"), UpdateManagerStrings.getString("S_Provider")};
		int[] iColumnWeight = { 40, 15, 15, 15, 15 };
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
		gridData.heightHint = 50;
		_textDescription.setLayoutData(gridData);

		_sashFormTopBottom.setWeights(new int[] { 80, 20 });

		setControl(compositeContent);

		setPageComplete(false);
	}
	/**
	 * 
	 */
	public IUMRegistry getRegistryAt(String strLocation) {

		IUMRegistry registry = (IUMRegistry) _hashRegistryCache.get(strLocation);

		// Create new entry if not found
		//------------------------------
		if (registry == null) {
			URL url = null;

			try {
				url = new URL(strLocation);
			}
			catch (MalformedURLException ex) {
				return null;
			}

			registry = _wizard._updateManager.getRegistryAt(url);

			if (registry != null) {
				_hashRegistryCache.put(strLocation, registry);
			}
		}

		return registry;
	}
	/**
	 * 
	 */
	public UMWizardTreeItem[] getSelectedItems() {

		UMWizardTreeItem[] items = null;

		// Obtain all selected items
		//--------------------------
		TableTreeItem[] treeItemChildren = _tableTreeItems.getItems();

		// Place them into an array
		//-------------------------
		Hashtable hashProducts = new Hashtable();
		Hashtable hashComponents = new Hashtable();
		Hashtable hashComponentEntries = new Hashtable();

		for (int i = 0; i < treeItemChildren.length; ++i) {
			getSelectedItemsRecursive(treeItemChildren[i], hashProducts, hashComponents, hashComponentEntries);
		}

		// Copy items to an array
		//-----------------------
		items = new UMWizardTreeItem[hashProducts.size() + hashComponents.size() + hashComponentEntries.size()];

		// Products
		//---------
		Object[] objArray = hashProducts.values().toArray();
		System.arraycopy(objArray, 0, items, 0, objArray.length);

		// Components
		//-----------
		objArray = hashComponents.values().toArray();
		System.arraycopy(objArray, 0, items, hashProducts.size(), objArray.length);

		// Component Entries
		//------------------
		objArray = hashComponentEntries.values().toArray();
		System.arraycopy(objArray, 0, items, hashProducts.size() + hashComponents.size(), objArray.length);

		return items;
	}
	/**
	 * 
	 */
	public void getSelectedItemsRecursive(TableTreeItem treeItem, Hashtable hashProducts, Hashtable hashComponents, Hashtable hashComponentEntries) {

		// Obtain all selected items
		//--------------------------
		UMWizardTreeItem umTreeItem = null;

		if (treeItem.getChecked() == true) {
			umTreeItem = (UMWizardTreeItem) treeItem.getData();

			if (umTreeItem != null) {
				if (umTreeItem._iType == UpdateManagerConstants.TYPE_PRODUCT) {

					// Remove duplicates by storing newer ones only
					//---------------------------------------------
					if (hashProducts.containsKey(umTreeItem._strId) == true) {
						UMWizardTreeItem umTreeItemPrevious = (UMWizardTreeItem) hashProducts.get(umTreeItem._strId);
						if (((IProductDescriptor) umTreeItem._descriptorAvailable).compare((IProductDescriptor) umTreeItemPrevious._descriptorAvailable) > 0) {
							hashProducts.remove(umTreeItem._strId);
							hashProducts.put(umTreeItem._strId, umTreeItem);
						}
					}
					else {
						hashProducts.put(umTreeItem._strId, umTreeItem);
					}
				}

				else if (umTreeItem._iType == UpdateManagerConstants.TYPE_COMPONENT) {

					// Remove duplicates by storing newer ones only
					//---------------------------------------------
					if (hashComponents.containsKey(umTreeItem._strId) == true) {
						UMWizardTreeItem umTreeItemPrevious = (UMWizardTreeItem) hashComponents.get(umTreeItem._strId);
						if (((IComponentDescriptor) umTreeItem._descriptorAvailable).compare((IComponentDescriptor) umTreeItemPrevious._descriptorAvailable) > 0) {
							hashComponents.remove(umTreeItem._strId);
							hashComponents.put(umTreeItem._strId, umTreeItem);
						}
					}
					else {
						hashComponents.put(umTreeItem._strId, umTreeItem);
					}
				}

				else if (umTreeItem._iType == UpdateManagerConstants.TYPE_COMPONENT_ENTRY) {

					// Add only if the parent product has not been selected
					//-----------------------------------------------------
					TableTreeItem treeItemParent = treeItem.getParentItem();
					if (treeItemParent.getChecked() == false) {

						// Remove duplicates by storing newer ones only
						//---------------------------------------------
						if (hashComponentEntries.containsKey(umTreeItem._strId) == true) {
							UMWizardTreeItem umTreeItemPrevious = (UMWizardTreeItem) hashComponentEntries.get(umTreeItem._strId);
							if (((IComponentEntryDescriptor) umTreeItem._descriptorEntry).compare((IComponentEntryDescriptor) umTreeItemPrevious._descriptorEntry) > 0) {
								hashComponentEntries.remove(umTreeItem._strId);
								hashComponentEntries.put(umTreeItem._strId, umTreeItem);
							}
						}
						else {
							hashComponentEntries.put(umTreeItem._strId, umTreeItem);
						}
					}
				}
			}
		}

		// Do child items
		//---------------
		TableTreeItem[] treeItemChildren = treeItem.getItems();

		for (int i = 0; i < treeItemChildren.length; ++i) {
			getSelectedItemsRecursive(treeItemChildren[i], hashProducts, hashComponents, hashComponentEntries);
		}

		return;
	}
	/**
	 */
	public String getURLDescription(String strURL) {

		if (_treeSetLocations != null && _treeSetLocations.size() > 0) {
			Iterator iter = _treeSetLocations.iterator();
			URLNamePair pair = null;
			while (iter.hasNext() == true) {
				pair = (URLNamePair) iter.next();
				if (pair._getURL().equals(strURL) == true) {
					if (pair._getName() != null && pair._getName().length() > 0) {
						return pair._getName();
					}
				}
			}
		}

		return strURL;
	}
	/**
	 * Obtains a list of registered component URLs from the local update registry.
	 * Obtains a list of bookmarked URLs from the persistent data.
	 * Creates a tree for all of the URLs.
	 */
	protected void initializeContent() {

		if (_bRefreshRequired == false) {
			return;
		}

		// Add all selected locations to a set
		//------------------------------------
		_treeSetLocations = new TreeSet(new UMWizardURLNamePairComparator());

		// Obtain predefined locations strings from location page
		//-------------------------------------------------------
		UMWizardPageLocations pageLocations = (UMWizardPageLocations) _wizard.getPage("locations");

		URLNamePair[] pairs = pageLocations.getSelectedPredefinedLocations();

		for (int i = 0; i < pairs.length; ++i) {
			_treeSetLocations.add(pairs[i]);
		}

		// Obtain additional locations strings from location page
		//-------------------------------------------------------
		pairs = pageLocations.getSelectedAdditionalLocations();

		for (int i = 0; i < pairs.length; ++i) {
			_treeSetLocations.add(pairs[i]);
		}

		// Obtain local registry in case there are installed versions
		//-----------------------------------------------------------
		final IUMRegistry registryLocal = _wizard._updateManager.getRegistryManager().getCurrentRegistry();

		final Vector vectorRegistries = new Vector();
		final Vector vectorURLsFailed = new Vector();

		_tableTreeItems.setRedraw(false);

		// Create an operation that read and create a registry for each location
		//----------------------------------------------------------------------
		IRunnableWithProgress operation = new IRunnableWithProgress() {
			public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {

				_progressMonitor = progressMonitor;

				progressMonitor.beginTask(UpdateManagerStrings.getString("S_Reading") + ":", _treeSetLocations.size());

				URLNamePair pair = null;
				IUMRegistry registry = null;

				Iterator iter = _treeSetLocations.iterator();

				boolean bUrlOk = false;

				while (iter.hasNext() == true) {

					bUrlOk = false;

					// Obtain the registry for the URL
					// This will download the information
					//-----------------------------------
					pair = (URLNamePair) iter.next();

					progressMonitor.subTask(pair._getName());

					// Attempt to open the stream
					//---------------------------
					try {
						URL url = new URL(pair._getURL());
						url = UMEclipseTree.appendTrailingSlash(url);
						Response response = URLHandler.open(url);
						// we open the url to get authenticated.  response code not
						// important at this point
					/*	if (response.getResponseCode() == IStatusCodes.HTTP_OK) {
							InputStream stream = response.getInputStream();

							if (stream != null) {
								stream.close();*/
								bUrlOk = true;
					//		}
					//	}
					}
					catch (Exception ex) {
					}

					if (bUrlOk == true) { 
						registry = getRegistryAt(pair._getURL());

						// Add it to the list
						//-------------------
						if (registry != null)
							vectorRegistries.add(registry);
					}
					else {
						vectorURLsFailed.add(pair);
					}

					progressMonitor.worked(1);
				}
				progressMonitor.done();

				_progressMonitor = null;
			}
		};

		// Run the operation
		//------------------
		try {
			_wizard.getContainer().run(true, true, operation);
		}
		catch (InterruptedException e) {
		}
		catch (InvocationTargetException e) {
		}

		// Create a new tree
		//------------------
		_treeSetItems = new TreeSet(new UMWizardTreeItemComparator());

		// Create an operation that will fill the tree with information
		// Location URL A
		//   products
		//     Product A
		//     Product B
		//   components
		//     Component A
		//     Component B
		// Location URL B
		// ...
		//-------------------------------------------------------------
		operation = new IRunnableWithProgress() {
			public void run(IProgressMonitor progressMonitor) throws InvocationTargetException {

				IUMRegistry registryRemote = null;

				for (int i = 0; i < vectorRegistries.size(); ++i) {
					registryRemote = (IUMRegistry) vectorRegistries.elementAt(i);
					addLocationToTree(registryRemote, registryLocal, progressMonitor);
				}
			}
		};

		// Run the operation
		//------------------
		try {
			_wizard.getContainer().run(true, true, operation);
		}
		catch (InterruptedException e) {
			return;
			//		System.out.println( "Interrupted" );
		}
		catch (InvocationTargetException e) {
			//		System.out.println( "Invocation" );
		}

		_tableTreeItems.setRedraw(true);

		// Create tree widget items for each update manager tree item
		//-----------------------------------------------------------
		connectToTree();

		StringBuffer strbMessage = new StringBuffer();

		// Determine whether there are any updatable items
		//------------------------------------------------
		if (_tableTreeItems.getItemCount() == 0)
			strbMessage.append(UpdateManagerStrings.getString("S_No_installable_items_were_found") + "\n\n");

		// Determine if any locations could not be accessed
		//-------------------------------------------------
		if (vectorURLsFailed.size() > 0) {
			strbMessage.append(UpdateManagerStrings.getString("S_Unable_to_access_the_following_locations") + ":");
			for (int i = 0; i < vectorURLsFailed.size(); ++i) {
				strbMessage.append("\n   ");
				strbMessage.append(((URLNamePair) vectorURLsFailed.elementAt(i))._getName());
				strbMessage.append(" (");
				strbMessage.append(((URLNamePair) vectorURLsFailed.elementAt(i))._getURL());
				strbMessage.append(")");
			}
		}

		if (strbMessage.length() > 0) {
			MessageDialog.openInformation(getControl().getShell(), UpdateManagerStrings.getString("S_Information"), strbMessage.toString());
		}

		// Reset refresh flag
		//-------------------
		_bRefreshRequired = false;

		return;
	}
	/**
	 */
	public int isInstallableComponent(TableTreeItem treeItemComponent) {

		UMWizardTreeItem umTreeItem = (UMWizardTreeItem) treeItemComponent.getData();

		// False if none available to install
		//-----------------------------------
		if (umTreeItem._descriptorAvailable == null) {
			return -1;
		}

		// True if not yet installed
		//--------------------------
		if (umTreeItem._descriptorCurrent == null) {
			return UpdateManagerConstants.OK_TO_INSTALL;
		}

		// True if installable
		//--------------------
		return ((IComponentDescriptor) umTreeItem._descriptorAvailable).isInstallable((IComponentDescriptor) umTreeItem._descriptorCurrent);
	}
	/**
	 * @return int
	 * @param treeItemComponent org.eclipse.swt.custom.TableTreeItem
	 */
	public int isInstallableComponentEntry(TableTreeItem treeItemComponentEntry) {

		UMWizardTreeItem umTreeItem = (UMWizardTreeItem) treeItemComponentEntry.getData();

		// False if none available to install
		//-----------------------------------
		if (umTreeItem._descriptorEntry == null) {
			return -1;
		}

		// Is the product installable?
		//----------------------------
		TableTreeItem treeItemProduct = treeItemComponentEntry.getParentItem();

		int iProductInstallable = isInstallableProduct(treeItemProduct);

		// If the product is not installable, disable if mandatory component
		//------------------------------------------------------------------
		if (iProductInstallable != UpdateManagerConstants.OK_TO_INSTALL) {
			if (umTreeItem._descriptorEntry.isOptionalForInstall() == false) {
				return iProductInstallable;
			}
		}

		return umTreeItem._descriptorEntry.isInstallable((IComponentDescriptor) umTreeItem._descriptorCurrent);
	}
	/**
	 * @return int
	 * @param treeItemComponent org.eclipse.swt.custom.TableTreeItem
	 */
	public int isInstallableProduct(TableTreeItem treeItemProduct) {

		UMWizardTreeItem umTreeItem = (UMWizardTreeItem) treeItemProduct.getData();

		// False if none available to install
		//-----------------------------------
		if (umTreeItem._descriptorAvailable == null) {
			return -1;
		}

		// True if not yet installed
		//--------------------------
		else if (umTreeItem._descriptorCurrent == null) {
			return UpdateManagerConstants.OK_TO_INSTALL;
		}

		// True if installable
		//--------------------
		return ((IProductDescriptor) umTreeItem._descriptorAvailable).isInstallable((IProductDescriptor) umTreeItem._descriptorCurrent);
	}
	/**
	 * 
	 */
	public boolean isInstallableRecursive(TableTreeItem treeItem) {

		boolean bInstallable = false;

		UMWizardTreeItem umTreeItem = (UMWizardTreeItem) treeItem.getData();

		// True if product or component is checked
		//----------------------------------------
		if (treeItem.getChecked() == true) {
			if (umTreeItem._iType == UpdateManagerConstants.TYPE_COMPONENT || umTreeItem._iType == UpdateManagerConstants.TYPE_PRODUCT || umTreeItem._iType == UpdateManagerConstants.TYPE_COMPONENT_ENTRY) {
				return true;
			}
		}

		// Check all children
		//-------------------
		TableTreeItem[] treeItemChildren = treeItem.getItems();

		for (int i = 0; i < treeItemChildren.length; ++i) {
			if (isInstallableRecursive(treeItemChildren[i]) == true) {
				return true;
			}
		}

		return false;
	}
	/**
	 * Sent when the mouse pointer passes into the area of
	 * the screen covered by a control.
	 */
	public void mouseEnter(org.eclipse.swt.events.MouseEvent e) {
	}
	/**
	 * Sent when the mouse pointer passes out of the area of
	 * the screen covered by a control.  Erases the description text area.
	 */
	public void mouseExit(MouseEvent e) {
		if (_strDescription != _wizard._strEmpty) {
			_textDescription.setText(_wizard._strEmpty);
			_strDescription = _wizard._strEmpty;
		}
	}
	/**
	 * Sent when the mouse pointer hovers (that is, stops moving
	 * for an (operating system specified) period of time) over
	 * a control.
	 */
	public void mouseHover(MouseEvent e) {
	}
	/**
	 * Determines which item the mouse is over.  Sets the text in the description text area to the description of the item.
	 */
	public void mouseMove(MouseEvent e) {

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
						if (_strDescription != _wizard._strEmpty) {
							_textDescription.setText(_wizard._strEmpty);
							_strDescription = _wizard._strEmpty;
						}
					}
				}
				else if (_strDescription != _wizard._strEmpty) {
					_textDescription.setText(_wizard._strEmpty);
					_strDescription = _wizard._strEmpty;
				}

			}
		}
		else if (_strDescription != _wizard._strEmpty) {
			_textDescription.setText(_wizard._strEmpty);
			_strDescription = _wizard._strEmpty;
		}
	}
	/**
	 * @return boolean
	 */
	public boolean performCancel() {

		if (_progressMonitor == null)
			return true;

		_progressMonitor.setCanceled(true);
		return false;
	}
	/**
	 */
	protected boolean setCheckedComponent(TableTreeItem treeItemComponent) {

		if (treeItemComponent.getChecked() == false) {
			int iInstallable = isInstallableComponent(treeItemComponent);

			setTreeItemChecked(treeItemComponent, iInstallable == 0);
			return iInstallable == 0;
		}

		return true;
	}
	/**
	 */
	protected boolean setCheckedComponentEntry(TableTreeItem treeItemComponentEntry) {

		if (treeItemComponentEntry.getChecked() == false) {

			UMWizardTreeItem umTreeItem = (UMWizardTreeItem) treeItemComponentEntry.getData();

			int iInstallable = isInstallableComponentEntry(treeItemComponentEntry);

			if (iInstallable == 0) {

				// Set checked the product and all of its mandatory components
				//------------------------------------------------------------
				TableTreeItem treeItemProduct = treeItemComponentEntry.getParentItem();

				boolean bInstallableProduct = setCheckedProduct(treeItemProduct);

				// If the product is not installable, disable if mandatory component
				//------------------------------------------------------------------
				if (bInstallableProduct == false) {

					if (umTreeItem._descriptorEntry.isOptionalForInstall() == false) {
						if (treeItemComponentEntry.getChecked() == true)
							treeItemComponentEntry.setChecked(false);
						return false;
					}
				}
			}

			setTreeItemChecked(treeItemComponentEntry, iInstallable == 0);
			umTreeItem._descriptorEntry.isSelected(iInstallable == 0);

			return iInstallable == 0;
		}

		return true;
	}
	/**
	 * This routine assumes that the checkbox was unchecked and is currently checked
	 */
	protected boolean setCheckedProduct(TableTreeItem treeItemProduct) {

		if (treeItemProduct.getChecked() == false) {

			int iInstallable = isInstallableProduct(treeItemProduct);

			setTreeItemChecked(treeItemProduct, iInstallable == 0);

			if (iInstallable == 0) {

				TableTreeItem[] treeItemChildren = treeItemProduct.getItems();

				UMWizardTreeItem umTreeItem = null;

				for (int i = 0; i < treeItemChildren.length; ++i) {
					setCheckedComponentEntry(treeItemChildren[i]);
				}
			}

			return iInstallable == 0;
		}

		return true;
	}
	/**
	 * This routine assumes that the checkbox was unchecked and is currently checked.
	 */
	protected boolean setCheckedURL(TableTreeItem treeItemURL) {

		TableTreeItem[] treeItemChildren = treeItemURL.getItems();
		UMWizardTreeItem umTreeItem = null;

		for (int i = 0; i < treeItemChildren.length; ++i) {
			umTreeItem = (UMWizardTreeItem) treeItemChildren[i].getData();

			if (umTreeItem._iType == UpdateManagerConstants.TYPE_PRODUCT) {
				setCheckedProduct(treeItemChildren[i]);
			}
			else if (umTreeItem._iType == UpdateManagerConstants.TYPE_COMPONENT) {
				setCheckedComponent(treeItemChildren[i]);
			}
		}

		// Always checked
		//---------------
		if (treeItemURL.getChecked() == false)
			treeItemURL.setChecked(true);

		return true;
	}
	/**
	 */
	protected void setTreeItemChecked(TableTreeItem treeItem, boolean bChecked) {

		if (bChecked == true) {
			if (treeItem.getChecked() == false)
				treeItem.setChecked(true);
		}
		else {
			if (treeItem.getChecked() == true)
				treeItem.setChecked(false);
		}
	}
	/**
	 */
	public void setTreeItemChecked(boolean bChecked) {
	}
	/**
	 */
	public void setTreeItemUncheckedRecursiveBackward(TableTreeItem treeItem) {

		if (treeItem.getChecked() != false) {
			treeItem.setChecked(false);
		}

		TableTreeItem treeItemParent = treeItem.getParentItem();

		if (treeItemParent != null) {
			setTreeItemUncheckedRecursiveBackward(treeItemParent);
		}
	}
	/**
	 */
	public void setTreeItemUncheckedRecursiveForward(TableTreeItem treeItem) {

		if (treeItem.getChecked() != false) {

			treeItem.setChecked(false);
			/*
					UMWizardTreeItem umTreeItem = (UMWizardTreeItem) treeItem.getData();
			
					if (umTreeItem._iType == UpdateManagerConstants.TYPE_COMPONENT_ENTRY) {
			
						if (umTreeItem._descriptorEntry.isOptionalForInstall() == true) {
							umTreeItem._descriptorEntry.isSelected(false);
						}
					}
			*/
		}

		TableTreeItem[] treeItemChildren = treeItem.getItems();

		for (int i = 0; i < treeItemChildren.length; ++i) {
			setTreeItemUncheckedRecursiveForward(treeItemChildren[i]);
		}
	}
	/**
	 * 
	 */
	public void setVisible(boolean bVisible) {

		super.setVisible(bVisible);

		if (bVisible == true) {
			_sashFormTopBottom.layout();
			initializeContent();

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
	public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {

		// Checkbox: Show latest versions only
		//------------------------------------
		if (e.widget == _buttonShowLatestOnly) {
			_bShowLatestOnly = _buttonShowLatestOnly.getSelection();
			_bRefreshRequired = true;
			initializeContent();
		}

		// TableTreeItem: Checkbox
		//------------------------
		else if (e.widget == _tableTreeItems && e.detail == SWT.CHECK) {

			// Set page complete to false in case there are
			// no selected items, or until selection is determined to be valid
			//----------------------------------------------------------------
			setPageComplete(false);

			TableTreeItem treeItem = (TableTreeItem) e.item;

			UMWizardTreeItem umTreeItem = (UMWizardTreeItem) treeItem.getData();

			boolean bChecked = treeItem.getChecked();

			// Check
			//------
			if (bChecked == true) {
				// Temporarily uncheck
				//--------------------
				treeItem.setChecked(false);

				if (umTreeItem._iType == UpdateManagerConstants.TYPE_URL)
					setCheckedURL(treeItem);

				else if (umTreeItem._iType == UpdateManagerConstants.TYPE_PRODUCT)
					setCheckedProduct(treeItem);

				else if (umTreeItem._iType == UpdateManagerConstants.TYPE_COMPONENT)
					setCheckedComponent(treeItem);

				else if (umTreeItem._iType == UpdateManagerConstants.TYPE_COMPONENT_ENTRY)
					setCheckedComponentEntry(treeItem);
			}

			// Uncheck
			//--------
			else {
				switch (umTreeItem._iType) {
					case UpdateManagerConstants.TYPE_COMPONENT_ENTRY :
						umTreeItem._descriptorEntry.isSelected(false);
						if (umTreeItem._descriptorEntry.isOptionalForInstall() == false)
							setTreeItemUncheckedRecursiveBackward(treeItem);
						break;

					default :
						setTreeItemUncheckedRecursiveBackward(treeItem);
						setTreeItemUncheckedRecursiveForward(treeItem);
						break;
				}
			}

			// Determine if page is complete
			//------------------------------
			boolean bPageComplete = false;

			TableTreeItem[] treeItems = _tableTreeItems.getItems();

			for (int i = 0; i < treeItems.length; ++i) {
				if (isInstallableRecursive(treeItems[i]) == true) {
					setPageComplete(true);
					break;
				}
			}
		}

		return;
	}
}