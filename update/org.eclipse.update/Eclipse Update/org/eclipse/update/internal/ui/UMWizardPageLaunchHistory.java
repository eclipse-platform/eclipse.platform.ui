package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
/**
 * Presents previous levels of launch configurations
 */
import java.util.Date;
import org.eclipse.core.internal.boot.LaunchInfo;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.core.internal.boot.update.*;
import java.net.*;
import java.util.Vector;
import org.eclipse.swt.graphics.*;
import java.util.Iterator;
import java.util.TreeSet;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.operation.*;
import org.eclipse.swt.custom.*;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.IProgressMonitor;

public class UMWizardPageLaunchHistory extends WizardPage implements SelectionListener
{
	protected UMWizard  _wizard            = null;
	protected TableTree _tableTreeHistory  = null;
	protected boolean   _bInitialized      = false;
/**
 *
 */
public UMWizardPageLaunchHistory( UMWizard wizard, String strName )
{
	super( strName );
	_wizard = wizard;

	this.setTitle(UpdateManagerStrings.getString("S_Revert_to_a_Previous_Installation"));
	this.setDescription(UpdateManagerStrings.getString("S_Select_a_previous_installation_to_revert_to"));

	setPageComplete(false);
}
/**
 *
 */
public void createControl( Composite compositeParent )
{
	// Content
	//--------
	Composite compositeContent = new Composite( compositeParent, SWT.NULL );

	GridLayout layout= new GridLayout();
	compositeContent.setLayout( layout );
	
	GridData gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.verticalAlignment   = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.grabExcessVerticalSpace   = true;
	compositeContent.setLayoutData( gridData );

	// Tree: History
	//--------------
	_tableTreeHistory = new TableTree(compositeContent, SWT.FULL_SELECTION | SWT.BORDER);

	String[] columnTitles = { UpdateManagerStrings.getString("S_Installation"), UpdateManagerStrings.getString("S_Identifier"), UpdateManagerStrings.getString("Version"), UpdateManagerStrings.getString("S_Provider") };
	int[] iColumnWeight = { 40, 25, 15, 20 };
	TableLayout layoutTable = new TableLayout();

	for (int i = 0; i < columnTitles.length; i++) {
		TableColumn tableColumn = new TableColumn(_tableTreeHistory.getTable(), SWT.NONE);
		tableColumn.setText(columnTitles[i]);
		ColumnLayoutData cLayout = new ColumnWeightData(iColumnWeight[i], true);
		layoutTable.addColumnData(cLayout);
	}

	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.verticalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.grabExcessVerticalSpace   = true;
	gridData.heightHint = 300;
	gridData.widthHint = 500;
	_tableTreeHistory.setLayoutData(gridData);
	
	_tableTreeHistory.getTable().setLinesVisible(true);
	_tableTreeHistory.getTable().setHeaderVisible(true);
	_tableTreeHistory.getTable().setLayout(layout);
	_tableTreeHistory.getTable().setLayout(layoutTable);
	_tableTreeHistory.addSelectionListener(this);
	
	setControl( compositeContent );
}
/**
 * 
 */
public void doRevert() {
	
	if (_tableTreeHistory.getSelectionCount() > 0) {

		TableTreeItem[] itemsSelected = _tableTreeHistory.getSelection();
		TableTreeItem itemConfiguration = itemsSelected[0];

		LaunchInfo.History history = (LaunchInfo.History) itemConfiguration.getData();

		if (history != null) {

			// Revert
			//-------
			if (history.getLaunchInfoDate() != null) {
				LaunchInfo.getCurrent().revertTo(history);
			}
		}
	}
}
/**
 * Return the string label of the selected installation state.
 * @return java.lang.String
 */
public String getSelectedInstallation() {
	
	if (_tableTreeHistory.getSelectionCount() > 0) {

		TableTreeItem[] itemsSelected = _tableTreeHistory.getSelection();
		return itemsSelected[0].getText(0);
	}

	return null;
}
/**
 *
 */
public void initializeContent() {

	if (_bInitialized == true)
		return;

	IUMRegistry registry = _wizard._updateManager.getRegistryManager().getLocalRegistry();

	LaunchInfo launchInfoCurrent = LaunchInfo.getCurrent();
	LaunchInfo launchInfoFormer = null;

	LaunchInfo.History[] histories = launchInfoCurrent.getLaunchInfoHistory();

	// Display only the number of histories that 
	// we are supposed to keep plus the current one
	//---------------------------------------------
	int iNumberOfHistoriesToDisplay = Math.min(histories.length, launchInfoCurrent.getHistoryCount() + 1);
	int iLastHistory = histories.length - iNumberOfHistoriesToDisplay;

	URL urlProfile = null;

	// Most recent to oldest order
	//----------------------------
	for (int i = histories.length - 1; i >= iLastHistory; --i) {
		TableTreeItem treeItemProfile = new TableTreeItem(_tableTreeHistory, SWT.NULL);

		treeItemProfile.setData(histories[i]);

		Date date = histories[i].getLaunchInfoDate();

		// Current profile
		//----------------
		if (date == null) {
			treeItemProfile.setText(0, UpdateManagerStrings.getString("S_Current_installation"));
			initializeLaunchInfoTree(treeItemProfile, launchInfoCurrent, registry);
		}

		// Older profile
		//--------------
		else {
			treeItemProfile.setText(0, histories[i].getLaunchInfoDate().toString());
			try {
				launchInfoFormer = new LaunchInfo(histories[i]);
				initializeLaunchInfoTree(treeItemProfile, launchInfoFormer, registry);
			}
			catch (IOException ex) {
			}
		}
	}

	_bInitialized = true;
}
/**
 *
 */
protected void initializeLaunchInfoTree(TableTreeItem treeItemParent, LaunchInfo launchInfo, IUMRegistry registry) {

	TableTreeItem treeItem = null;
	
	// Configurations
	//---------------
	String[] straText = null;
	LaunchInfo.VersionedIdentifier[] configurations = launchInfo.getConfigurations();

	for (int i = 0; i < configurations.length; ++i) {
		treeItem = new TableTreeItem(treeItemParent, SWT.NULL);

		treeItem.setText(1, configurations[i].getIdentifier());
		treeItem.setText(2, configurations[i].getVersion());

		IProductDescriptor descriptor = registry.getProductDescriptor(configurations[i].getIdentifier());

		if (descriptor != null) {
			treeItem.setText(0, descriptor.getLabel());
			treeItem.setText(3, descriptor.getProviderName());
		}
	}

	// Components
	//-----------
	LaunchInfo.VersionedIdentifier[] components = launchInfo.getComponents();

	for (int i = 0; i < components.length; ++i) {
		treeItem = new TableTreeItem(treeItemParent, SWT.NULL);

		treeItem.setText(1, components[i].getIdentifier());
		treeItem.setText(2, components[i].getVersion());
		
		IComponentDescriptor descriptor = registry.getComponentDescriptor(components[i].getIdentifier());
		
		if (descriptor != null) {
			treeItem.setText(0, descriptor.getLabel());
			treeItem.setText(3, descriptor.getProviderName());
		}
	}
}
/**
 * 
 */
public void setVisible(boolean bVisible) {

	// Do installations before setting this page visible
	//--------------------------------------------------
	if (bVisible == true) {
		initializeContent();
	}
	
	super.setVisible(bVisible);
}
/**
 * Sent when default selection occurs in the control.
 * <p>
 * For example, on some platforms default selection occurs
 * in a List when the user double-clicks an item or types
 * return in a Text.
 * </p>
 *
 * @param e an event containing information about the default selection
 */
public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {}
/**
 *
 */
public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {

	if (_tableTreeHistory.getSelectionCount() > 0) {

		TableTreeItem[] itemsSelected = _tableTreeHistory.getSelection();
		TableTreeItem   itemConfiguration = itemsSelected[0];

		LaunchInfo.History history = (LaunchInfo.History) itemConfiguration.getData();

		if (history != null) {

			// Page complete
			//--------------
			if (history.getLaunchInfoDate() != null) {
				setPageComplete(true);
				return;
			}
		}
	}

	setPageComplete(false);
}
}
