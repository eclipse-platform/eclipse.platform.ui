package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;
import java.net.*;
import java.awt.Toolkit;
import java.awt.Dimension;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
/**
 *
 */
import org.eclipse.jface.window.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.core.*;
import java.util.Vector;
import org.eclipse.update.internal.core.*;
import org.eclipse.jface.dialogs.*;

public class UMApplicationWindow extends Window {
	protected UpdateManager _updateManager = null;
	protected UMApplicationUserInterface _userInterface = null;
	protected ToolItem _toolItemBookmarkAdd = null;
	protected ToolItem _toolItemBookmarkRemove = null;
	protected ToolItem _toolItemRefresh = null;
	protected ToolItem _toolItemViewVendorWebSite = null;
	protected ToolItem _toolItemUpdate = null;
	protected UMApplicationUserInterfaceProperties _properties = null;
/**
 * UpdateManagerWindow constructor comment.
 */
public UMApplicationWindow( UMApplicationUserInterface userInterface ) {
	super(null);

	_userInterface = userInterface;

	// Update Manager
	//---------------
	_updateManager = new UpdateManager( getShell() );

	try
	{
		_updateManager.initialize();
	}

	catch( UpdateManagerException ex )
	{
		// Unable to find logs
		//--------------------
		displayMessage( "error", UpdateManagerStrings.getString("S_Update_Manager"), UpdateManagerStrings.getString("S_Unable_to_open_error_logs"), null );
	}

	// Persistent Properties
	// The properties are loaded by the constructor
	// and saved by this object's close function
	//---------------------------------------------
	_properties = new UMApplicationUserInterfaceProperties();
}
/**
 * 
 * @return boolean
 */
public boolean close() {


	// Save persistent properties
	//---------------------------
	_properties.save();

	// Get the window to do its thing
	//-------------------------------
	super.close();

	// Set the flag to stop the application event loop
	//------------------------------------------------
	_userInterface.stopEventLoop();

	// Return ok to close
	//-------------------
	return true;
}
/**
 * Creates and returns this window's contents.
 * <p>
 * The default implementation of this framework method
 * creates an instance of <code>Composite</code>.
 * Subclasses may override.
 * </p>
 * 
 * @return the control
 */
protected Control createContents(Composite compositeParent) {

	// Title bar
	//----------
	getShell().setText(UpdateManagerStrings.getString("S_Software_Updates"));
	// getShell().setImage(...);
/*
	// Menu bar
	//---------
	Menu bar = new Menu(getShell(), SWT.BAR);
	getShell().setMenuBar(bar);

	// File
	//-----
	MenuItem fileItem = new MenuItem(bar, SWT.CASCADE);
	fileItem.setText("File");

	Menu menu = new Menu(bar);
	MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
	menuItem.setText("Exit");
//	menuItem.addSelectionListener(this);

	fileItem.setMenu(menu);

	// Selected
	//---------
	MenuItem selectedItem = new MenuItem(bar, SWT.CASCADE);
	selectedItem.setText("Selected");

	menu = new Menu(bar);
	menuItem = new MenuItem(menu, SWT.PUSH);
	menuItem.setText("Update");
//	menuItem.addSelectionListener(this);

	menuItem = new MenuItem(menu, SWT.PUSH);
	menuItem.setText("View information");
//	menuItem.addSelectionListener(this);
	
	selectedItem.setMenu(menu);

	// Toolbar
	//--------
	ToolBar toolBar = new ToolBar(getShell(), SWT.NULL);
	
	new ToolItem(toolBar, SWT.SEPARATOR);
	
	_toolItemUpdate = new ToolItem(toolBar, SWT.PUSH);
	_toolItemUpdate.setImage(UpdateManagerPlugin.getImageDescriptor("icons/basic/ovr16/updatecomponents.gif").createImage());
	_toolItemUpdate.setToolTipText("Update selected components");
//	_toolItemUpdate.addSelectionListener(this);
	
	new ToolItem(toolBar, SWT.SEPARATOR);
		
	_toolItemViewVendorWebSite = new ToolItem(toolBar, SWT.PUSH);
	_toolItemViewVendorWebSite.setImage(UpdateManagerPlugin.getImageDescriptor("icons/basic/ovr16/openbrwsr.gif").createImage());
	_toolItemViewVendorWebSite.setToolTipText("View vendor information page");
//	_toolItemViewVendorWebSite.addSelectionListener(this);

	new ToolItem(toolBar, SWT.SEPARATOR);

	_toolItemBookmarkAdd = new ToolItem(toolBar, SWT.PUSH);
	_toolItemBookmarkAdd.setImage(UpdateManagerPlugin.getImageDescriptor("icons/basic/ovr16/bkmrk.gif").createImage());
	_toolItemBookmarkAdd.setToolTipText("Add to list");
	_toolItemBookmarkAdd.setEnabled(false);
//	_toolItemBookmarkAdd.addSelectionListener(this);

	_toolItemBookmarkRemove = new ToolItem(toolBar, SWT.PUSH);
	_toolItemBookmarkRemove.setImage(UpdateManagerPlugin.getImageDescriptor("icons/basic/ovr16/rembkmrk.gif").createImage());
	_toolItemBookmarkRemove.setToolTipText("Remove from list");
//	_toolItemBookmarkRemove.addSelectionListener(this);

	new ToolItem(toolBar, SWT.SEPARATOR);
	
	_toolItemRefresh = new ToolItem(toolBar, SWT.PUSH);
	_toolItemRefresh.setImage(UpdateManagerPlugin.getImageDescriptor("icons/basic/ovr16/refresh.gif").createImage());
	_toolItemRefresh.setToolTipText("Refresh");
//	_toolItemRefresh.addSelectionListener(this);
*/
	// Client
	//-------
	Composite compositeClient = new Composite(compositeParent, SWT.NULL);

	GridLayout layout = new GridLayout();
	layout.numColumns = 1;
	compositeClient.setLayout(layout);

	GridData gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.verticalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.grabExcessVerticalSpace = true;
	compositeClient.setLayoutData(gridData);
/*
	// Splitter Top/Bottom
	//--------------------
	Splitter splitterTopBottom = new Splitter(compositeClient, SWT.VERTICAL, true);
	splitterTopBottom.setLayoutData(new GridData(GridData.FILL_BOTH));

	// Top composite
	//--------------
	Composite compositeTop = new Composite(splitterTopBottom, SWT.NULL);

	layout = new GridLayout();
	layout.numColumns = 2;
	layout.marginWidth = 0;
	layout.marginHeight = 0;
	compositeTop.setLayout(layout);

	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.verticalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.grabExcessVerticalSpace = true;
	compositeTop.setLayoutData(gridData);

	// Separator
	//----------
	Label labelSeparator = new Label(compositeTop, SWT.SEPARATOR | SWT.HORIZONTAL);
	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.horizontalSpan = 2;
	labelSeparator.setLayoutData(gridData);

	// Label: Location
	//----------------
	Label labelLocation = new Label(compositeTop, SWT.NULL);
	labelLocation.setText("Location:");

	// Combo: Locations
	//-----------------
	_comboLocations = new Combo(compositeTop, SWT.NULL);
	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;

	String[] straBookmarks = _properties.getBookmarkStrings();

	for (int i = 0; i < straBookmarks.length; ++i) {
		_comboLocations.add(straBookmarks[i]);
	}

	_comboLocations.setLayoutData(gridData);
	_comboLocations.addModifyListener(this);

	// Separator
	//----------
	labelSeparator = new Label(compositeTop, SWT.SEPARATOR | SWT.HORIZONTAL);
	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.horizontalSpan = 2;
	labelSeparator.setLayoutData(gridData);

	// Table: URLs
	//------------
	_tableURLs = new Table(compositeTop, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.verticalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.grabExcessVerticalSpace = true;
	gridData.horizontalSpan = 2;
	_tableURLs.setLayoutData(gridData);

	String[] titles = new String[] { "Location", "Vendor" };
	int[] widths = new int[] { 200, 200 };
	for (int i = 0; i < titles.length; i++) {
		TableColumn column = new TableColumn(_tableURLs, SWT.NONE);
		column.setText(titles[i]);
		column.setWidth(widths[i]);
		column.addSelectionListener(this);
	}
	_tableURLs.setHeaderVisible(true);
	_tableURLs.setLinesVisible(true);
	//	_tableURLs.setMenu (createTableMenu ());
	_tableURLs.addSelectionListener(this);

	// Sash
	//-----
	splitterTopBottom.addSash();

	// Splitter Left/Right
	//--------------------
	Splitter splitterLeftRight = new Splitter(splitterTopBottom, SWT.HORIZONTAL, true);
	splitterLeftRight.setLayoutData(new GridData(GridData.FILL_BOTH));

	// Tree container component
	//-------------------------
	_compositeTreeContainer = new Composite(splitterLeftRight, SWT.NULL);

	_layoutCard = new UpdateManagerCardLayout(this);
	_compositeTreeContainer.setLayout(_layoutCard);

	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.verticalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.grabExcessVerticalSpace = true;
	_compositeTreeContainer.setLayoutData(gridData);

	// Sash Left/Right
	//----------------
	splitterLeftRight.addSash();

	// Information Page
	//-----------------
	_infoPage = new UpdateManagerWindowInfoPage(splitterLeftRight, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

	// Add content
	//------------
	doRefresh();
*/	
	return compositeClient;
}
/**
 * Displays error message of type:
 * <ul>
 * <li>error</li>
 * <li>warning</li>
 * </ul>
 * The title and message1 are translated before displaying
 * @param strMessageType java.lang.String
 * @param strTitle java.lang.String
 * @param strMessage1 java.lang.String
 * @param strMessage2 java.lang.String
 */
private void displayMessage(String strMessageType, String strTitle, String strMessage1, String strMessage2) {
	MessageDialog.openError( getShell(), strTitle, strMessage1 + "\n" + strMessage2 );
}
/**
 * Initializes the location and size of this window's SWT shell 
 * after it has been created.
 * <p>
 * This framework method is called by the <code>create</code>
 * framework method. The default implementation takes into account the
 * shell's properties, which will have been set by <code>configureShell</code>,
 * and also the minimum size recommended by <code>getMinimumSize</code>.
 * Subclasses may extend or reimplement. 
 * </p>
 */
protected void initializeBounds() {

	// Ensure that the width of the shell fits the display.
	Rectangle rectScreen = getShell().getDisplay().getBounds();

	// Shell bounds
	//-------------
	getShell().setBounds(rectScreen.width / 4, rectScreen.height / 8, rectScreen.width / 2, rectScreen.height * 6 / 8);
}
}
