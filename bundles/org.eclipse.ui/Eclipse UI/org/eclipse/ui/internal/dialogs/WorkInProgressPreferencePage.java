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

	// Temporary option to enable cool bars
	private Button coolBarsButton;
	// Temporary option to enable new menu structure
	private Button newMenusButton;
	
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
		button.setSelection(store.getBoolean(prefId)); //$NON-NLS-1$
		
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
		coolBarsButton =
			createTempOption(
				composite,
				WorkbenchMessages.getString("WorkInProgressPreference.enableCoolbars"), //$NON-NLS-1$
				"ENABLE_COOL_BARS", //$NON-NLS-1$
				true,
				WorkbenchMessages.getString("WorkInProgressPreference.onlyAffectsNewWindows")); //$NON-NLS-1$
							
/*		// Temporary option to enable the new menu organization
		newMenusButton = 
			createTempOption(
				composite,
				"Enable new menu organization",
				"ENABLE_NEW_MENUS",
				true);
*/
		return composite;
	}
	
	/**
	 * The default button has been pressed. 
	 */
	protected void performDefaults() {
		IPreferenceStore store = getPreferenceStore();
		
		coolBarsButton.setSelection(store.getDefaultBoolean("ENABLE_COOL_BARS")); //$NON-NLS-1$				
//		newMenusButton.setSelection(store.getDefaultBoolean("ENABLE_NEW_MENUS")); //$NON-NLS-1$
	}
	
	/**
	 *	The user has pressed Ok.  Store/apply this page's values appropriately.
	 */
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();

//		store.setValue("ENABLE_NEW_MENUS", newMenusButton.getSelection()); //$NON-NLS-1$
		store.setValue("ENABLE_COOL_BARS", coolBarsButton.getSelection()); //$NON-NLS-1$
		
		ResourcesPlugin.getPlugin().savePluginPreferences();
		WorkbenchPlugin.getDefault().savePluginPreferences();
		return true;
	}	
}
