/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.ui.internal.dialogs;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.OpenStrategy;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.*;

/**
 * @version 	1.0
 * @author
 */
public class WorkInProgressPreferencePage extends WorkbenchPreferencePage {

	// Temporary option to enable navigation items
	private Button navigationHistoryButton;
	private static String ENABLE_NAVIGATION_HISTORY = "ENABLE_NAVIGATION_HISTORY"; //$NON-NLS-1$
	private static boolean useNavigationHistory = true;	

	
	static {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();

		//Set the default values.
		store.setDefault(ENABLE_NAVIGATION_HISTORY, true);
		useNavigationHistory = store.getBoolean(ENABLE_NAVIGATION_HISTORY);	
	}
	
	public static boolean useNavigationHistory() {
		return useNavigationHistory;
	}
	
	/**
	 *	@see IWorkbenchPreferencePage
	 */
	public void init(IWorkbench aWorkbench) {
		super.init(aWorkbench);
	}	
	/**
	 * Creates a temporary option checkbox.
	 */
	private Button createTempOption(Composite parent, String text, String prefId, boolean restartNeeded,String labelText) {
		// create composite needed to get tab order right
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		
		Button button = new Button(composite, SWT.CHECK);
		button.setText(text);
		IPreferenceStore store = getPreferenceStore();
		button.setSelection(store.getBoolean(prefId));
		
		if (restartNeeded) {
			Label label = new Label(composite, SWT.NONE);
			if(labelText == null)
				label.setText(WorkbenchMessages.getString("WorkInProgressPreference.noEffectUntilRestarted")); //$NON-NLS-1$
			else
				label.setText(labelText);
		}
		
		return button;
	}
	
	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		
		// Temporary option to enable cool bars
		createSpace(composite);
		navigationHistoryButton =
			createTempOption(
				composite,
				WorkbenchMessages.getString("WorkInProgressPreference.enableNavigationHistory"), //$NON-NLS-1$
				ENABLE_NAVIGATION_HISTORY, //$NON-NLS-1$
				true,
				null); //$NON-NLS-1$

		return composite;
	}
	
	/**
	 * The default button has been pressed. 
	 */
	protected void performDefaults() {
		IPreferenceStore store = getPreferenceStore();
		
		navigationHistoryButton.setSelection(store.getDefaultBoolean(ENABLE_NAVIGATION_HISTORY)); //$NON-NLS-1$				
	}
	
	/**
	 *	The user has pressed Ok.  Store/apply this page's values appropriately.
	 */
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();

		store.setValue(ENABLE_NAVIGATION_HISTORY, navigationHistoryButton.getSelection()); 
		
		ResourcesPlugin.getPlugin().savePluginPreferences();
		WorkbenchPlugin.getDefault().savePluginPreferences();
		return true;
	}	
}
