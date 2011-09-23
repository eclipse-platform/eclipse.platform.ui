/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.IHelpBaseConstants;
import org.eclipse.help.internal.base.remote.DefaultPreferenceFileHandler;
import org.eclipse.help.internal.base.remote.PreferenceFileHandler;
import org.eclipse.help.internal.base.remote.RemoteHelp;
import org.eclipse.help.internal.base.remote.RemoteIC;
import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * Preference page to set remote infocenters
 */
public class HelpContentPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private InfocenterDisplay remoteICPage;

	private Button searchLocalHelpOnly;
	private Button searchLocalHelpFirst;
	private Button searchLocalHelpLast;
	private Label descLabel;

	/**
	 * Creates the preference page
	 */
	public HelpContentPreferencePage() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IHelpUIConstants.PREF_PAGE_HELP_CONTENT);

		initializeDialogUnits(parent);
		
		descLabel = new Label(parent, SWT.NONE);
		descLabel.setText(Messages.HelpContentPage_title);
		Dialog.applyDialogFont(descLabel);
		
		createSearchLocalHelpOnly(parent);
		createSearchLocalHelpFirst(parent);
		createSearchLocalHelpLast(parent);
		
		remoteICPage = new InfocenterDisplay(this);
		remoteICPage.createContents(parent);

		initializeTableEnablement(searchLocalHelpOnly.getSelection());
		
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();

		// Restore Defaults functionality here		
		HelpContentBlock currentBlock=remoteICPage.getHelpContentBlock();
		currentBlock.getRemoteICviewer().getRemoteICList().removeAllRemoteICs(currentBlock.getRemoteICList());
		currentBlock.getRemoteICviewer().getRemoteICList().loadDefaultPreferences();
		currentBlock.restoreDefaultButtons();
		boolean remoteHelpOn = new DefaultPreferenceFileHandler().isRemoteHelpOn();
		boolean remoteHelpPreferred = new DefaultPreferenceFileHandler().isRemoteHelpPreferred();
		searchLocalHelpOnly.setSelection(!remoteHelpOn);
		searchLocalHelpFirst.setSelection(remoteHelpOn && !remoteHelpPreferred);
		searchLocalHelpLast.setSelection(remoteHelpOn && remoteHelpPreferred);
		changeListener.handleEvent(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {

		HelpContentBlock currentBlock;
		RemoteIC[] currentRemoteICArray;
		
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(HelpBasePlugin.PLUGIN_ID);
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.preference.PreferencePage#performOk()
		 */
		prefs.putBoolean(IHelpBaseConstants.P_KEY_REMOTE_HELP_ON, !(searchLocalHelpOnly.getSelection()));
		prefs.putBoolean(IHelpBaseConstants.P_KEY_REMOTE_HELP_PREFERRED, searchLocalHelpLast.getSelection());
	
		currentBlock=remoteICPage.getHelpContentBlock();
		currentRemoteICArray=currentBlock.getRemoteICList();
     	PreferenceFileHandler.commitRemoteICs(currentRemoteICArray);
		
    	RemoteHelp.notifyPreferenceChange();
		return super.performOk();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.DialogPage#setButtonLayoutData(org.eclipse.swt.widgets.Button)
	 */
	protected GridData setButtonLayoutData(Button button) {
		return super.setButtonLayoutData(button);
	}

	private void createSearchLocalHelpOnly(Composite parent) {
		searchLocalHelpOnly = new Button(parent, SWT.RADIO);
		searchLocalHelpOnly.setText(Messages.SearchEmbeddedHelpOnly);
		searchLocalHelpOnly.addListener(SWT.Selection, changeListener);

		boolean isRemoteOn = Platform.getPreferencesService().getBoolean
		    (HelpBasePlugin.PLUGIN_ID, IHelpBaseConstants.P_KEY_REMOTE_HELP_ON, false, null);
		
		searchLocalHelpOnly.setSelection(!isRemoteOn);
		Dialog.applyDialogFont(searchLocalHelpOnly);	
	}
	
	private void createSearchLocalHelpFirst(Composite parent) {
		searchLocalHelpFirst = new Button(parent, SWT.RADIO);
		searchLocalHelpFirst.setText(Messages.SearchEmbeddedHelpFirst);
		searchLocalHelpFirst.addListener(SWT.Selection, changeListener);
		
		boolean isRemoteOn = Platform.getPreferencesService().getBoolean
	    	(HelpBasePlugin.PLUGIN_ID, IHelpBaseConstants.P_KEY_REMOTE_HELP_ON, false, null);
		boolean isRemotePreferred = Platform.getPreferencesService().getBoolean
	    	(HelpBasePlugin.PLUGIN_ID, IHelpBaseConstants.P_KEY_REMOTE_HELP_PREFERRED, false, null);
		
		searchLocalHelpFirst.setSelection(isRemoteOn && !isRemotePreferred);
		Dialog.applyDialogFont(searchLocalHelpFirst);
	}
	
	private void createSearchLocalHelpLast(Composite parent) {
		searchLocalHelpLast = new Button(parent, SWT.RADIO);
		searchLocalHelpLast.setText(Messages.SearchEmbeddedHelpLast);
		searchLocalHelpLast.addListener(SWT.Selection, changeListener);
		
		boolean isRemoteOn = Platform.getPreferencesService().getBoolean
			(HelpBasePlugin.PLUGIN_ID, IHelpBaseConstants.P_KEY_REMOTE_HELP_ON, false, null);
		boolean isRemotePreferred = Platform.getPreferencesService().getBoolean
			(HelpBasePlugin.PLUGIN_ID, IHelpBaseConstants.P_KEY_REMOTE_HELP_PREFERRED, false, null);
		
		searchLocalHelpLast.setSelection(isRemoteOn && isRemotePreferred);
		Dialog.applyDialogFont(searchLocalHelpLast);
	}
	
	/*
	 * Initialize the table enablement with the current checkbox selection 
	 */
	
	private void initializeTableEnablement(boolean isRemoteHelpDisabled)
	{
		
		HelpContentBlock currentBlock=remoteICPage.getHelpContentBlock();
		
		if(isRemoteHelpDisabled)
			currentBlock.disableAllButtons();
		else
			currentBlock.restoreDefaultButtons();
			
		// Disable/Enable table
		currentBlock.getRemoteICviewer().getTable().setEnabled(!isRemoteHelpDisabled);
	}

	/*
	 * Listens for any change in the UI and checks for valid input and correct
	 * enablement.
	 */
	private Listener changeListener = new Listener() {
		public void handleEvent(Event event) {

			HelpContentBlock currentBlock=remoteICPage.getHelpContentBlock();			
			
			boolean isRemoteHelpEnabled = !(searchLocalHelpOnly.getSelection());
			//  Disable/Enable buttons
			if(isRemoteHelpEnabled)
				currentBlock.restoreDefaultButtons();
			else
				currentBlock.disableAllButtons();
			
			// Disable/Enable table
			currentBlock.getRemoteICviewer().getTable().setEnabled(isRemoteHelpEnabled);
		}
		
	};
	
}
