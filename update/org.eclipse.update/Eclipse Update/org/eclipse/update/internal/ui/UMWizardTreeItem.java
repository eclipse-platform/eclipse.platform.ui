package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 *
 */
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import java.util.Vector;
import org.eclipse.update.internal.core.*;
import org.eclipse.core.internal.boot.update.*;

public class UMWizardTreeItem {
	protected Vector _vectorChildren      = null;
	protected String _strVersionAvailable = null;
	protected String _strVersionCurrent   = null;
	protected String _strName             = null;
	protected String _strId               = null;
	protected String _strDescription      = null;
	protected String _strVendorName       = null;
	protected String _strURLUpdate        = null;
	protected String _strURLInformation   = null;
	protected int    _iType               = 0;
	protected IManifestDescriptor _descriptorCurrent          = null;
	protected IManifestDescriptor _descriptorAvailable        = null;
	protected IComponentEntryDescriptor _descriptorEntry      = null;
	protected URLNamePair _urlNamePair = null;
/**
 * UpdateManagerItem constructor comment.
 */
public UMWizardTreeItem() {
	super();
	_iType = UpdateManagerConstants.TYPE_UNKNOWN;
}
/**
 * Adds a new tree item to the update tree.
 * @param item org.eclipse.update.internal.ui.UpdateManagerItem
 */
public void addChildItem(UMWizardTreeItem item) {

	if( _vectorChildren == null ) {
		_vectorChildren = new Vector();
	}

	_vectorChildren.add( item );
}
/**
 * @return org.eclipse.update.internal.ui.UpdateManagerTreeItem
 */
public Object clone() {
	UMWizardTreeItem item = new UMWizardTreeItem();

	item._descriptorAvailable = _descriptorAvailable;
	item._descriptorCurrent   = _descriptorCurrent;
	item._iType               = _iType;
	item._strDescription      = _strDescription;
	item._strId               = _strId;
	item._strName             = _strName;
	item._strURLInformation   = _strURLInformation;
	item._strURLUpdate        = _strURLUpdate;
	item._strVendorName       = _strVendorName;
	item._strVersionAvailable = _strVersionAvailable;
	item._strVersionCurrent   = _strVersionCurrent;

	return item;
}
/**
 * Creates a tree item widget with a tree items of the URL tree.
 * This is called recursively for child tree items.
 * @param treeItemParent org.eclipse.swt.widgets.TreeItem
 */
public void connectToTree(TreeItem treeItemParent) {

	TreeItem treeItem = new TreeItem(treeItemParent, SWT.NULL);
	treeItem.setText(_strName);

	treeItem.setData(this);

	// Create child tree items
	//------------------------
	if (_vectorChildren != null) {

		for (int i = 0; i < _vectorChildren.size(); ++i) {
			((UMWizardTreeItem) _vectorChildren.elementAt(i)).connectToTree(treeItem);
		}
	}

	// Associate an appropriate image
	//-------------------------------
	if (_iType == UpdateManagerConstants.TYPE_COMPONENT) {

		// Server is better?
		//------------------
/*
		if (isUpdatable() == true) {
			treeItem.setImage(UpdateManagerPlugin.getImageDescriptor("icons/basic/ovr16/sbet_stat.gif").createImage());
		}
		else {
			treeItem.setImage(UpdateManagerPlugin.getImageDescriptor("icons/basic/ovr16/wbet_stat.gif").createImage());
		}
:*/
	}

	else if (_iType == UpdateManagerConstants.TYPE_COMPONENT_CATEGORY) {
	    
	}
	else if (_iType == UpdateManagerConstants.TYPE_URL) {
	    
	}

	treeItem.setExpanded(true);
}
}
