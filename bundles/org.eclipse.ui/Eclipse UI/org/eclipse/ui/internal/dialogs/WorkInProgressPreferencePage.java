/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * @version 	1.0
 * @author
 */
public class WorkInProgressPreferencePage extends WorkbenchPreferencePage {

	// Temporary option to enable wizard for project capability
	private Button capabilityButton;
	// Temporary option to enable working sets
	private Button workingSetsButton;
	// Temporary option to enable new menu structure
	private Button newMenusButton;
	
	/**
	 *	@see IWorkbenchPreferencePage
	 */
	public void init(IWorkbench aWorkbench) {
		super.init(aWorkbench);
		
		//Call commented out on WorkbenchPreferencePage. 
		acceleratorInit(aWorkbench);
	}
	
	/**
	 * Creates a temporary option checkbox.
	 */
	private Button createTempOption(Composite parent, String text, String prefId, boolean restartNeeded) {
		// create composite needed to get tab order right
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		
		Button button = new Button(composite, SWT.CHECK);
		button.setText(text);
		IPreferenceStore store = getPreferenceStore();
		button.setSelection(store.getBoolean(prefId)); //$NON-NLS-1$
		
		if (restartNeeded) {
			Label label = new Label(composite, SWT.NONE);
			label.setText("Note: This preference will only take effect after restarting.");
		}
		
		return button;
	}
	
	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		// Call commented out on WorkbenchPreferencePage. 
		createAcceleratorConfigurationGroup(composite, WorkbenchMessages.getString("WorkbenchPreference.acceleratorConfiguration")); //$NON-NLS-1$

		// Temporary option to enable wizard for project capability work
		capabilityButton = 
			createTempOption(
				composite,
				"Enable new configurable project wizard (work in progress of project capabilities)", 
				"ENABLE_CONFIGURABLE_PROJECT_WIZARD",
				true);

		// Temporary option to enable workbench working sets
		workingSetsButton =
			createTempOption(
				composite,
				"Enable workbench working sets (adds two items to Window menu)",
				"ENABLE_WORKING_SETS",
				true);
			
		// Temporary option to enable the new menu organization
		newMenusButton = 
			createTempOption(
				composite,
				"Enable new menu organization",
				"ENABLE_NEW_MENUS",
				true);

		return composite;
	}
	/**
	 * The default button has been pressed. 
	 */
	protected void performDefaults() {
		IPreferenceStore store = getPreferenceStore();

		//Call commented out on WorkbenchPreferencePage. 
		acceleratorPerformDefaults(store);

		capabilityButton.setSelection(store.getDefaultBoolean("ENABLE_CONFIGURABLE_PROJECT_WIZARD")); //$NON-NLS-1$	
		workingSetsButton.setSelection(store.getDefaultBoolean("ENABLE_WORKING_SETS")); //$NON-NLS-1$				
		newMenusButton.setSelection(store.getDefaultBoolean("ENABLE_NEW_MENUS")); //$NON-NLS-1$
	}
	
	/**
	 *	The user has pressed Ok.  Store/apply this page's values appropriately.
	 */
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();

		store.setValue("ENABLE_CONFIGURABLE_PROJECT_WIZARD", capabilityButton.getSelection()); //$NON-NLS-1$
		store.setValue("ENABLE_WORKING_SETS", workingSetsButton.getSelection()); //$NON-NLS-1$
		store.setValue("ENABLE_NEW_MENUS", newMenusButton.getSelection()); //$NON-NLS-1$
		
		//Call commented out on WorkbenchPreferencePage. 
		acceleratorPerformOk(store);
		return true;
	}	
}
