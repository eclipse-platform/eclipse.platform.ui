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
	
	/**
	 *	@see IWorkbenchPreferencePage
	 */
	public void init(IWorkbench aWorkbench) {
		super.init(aWorkbench);
		
		//Call commented out on WorkbenchPreferencePage. 
		acceleratorInit(aWorkbench);
	}
	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		//Call commented out on WorkbenchPreferencePage. 
		createAcceleratorConfigurationGroup(composite, WorkbenchMessages.getString("WorkbenchPreference.acceleratorConfiguration"));

		// Temporary option to enable wizard for project capability work
		createSpace(composite);
		capabilityButton = new Button(composite, SWT.CHECK);
		capabilityButton.setText("Enable new configurable project wizard (work in progress of project capabilities)"); //$NON-NLS-1$
		Label label = new Label(composite, SWT.NONE);
		label.setText("Note: The new configurable project option will only take effect after restarting.");

		createSpace(composite);
		workingSetsButton = new Button(composite, SWT.CHECK);
		workingSetsButton.setText("Enable workbench working sets"); //$NON-NLS-1$

		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
				
		// Temporary option to enable wizard for project capability
		capabilityButton.setSelection(store.getBoolean("ENABLE_CONFIGURABLE_PROJECT_WIZARD")); //$NON-NLS-1$
		workingSetsButton.setSelection(store.getBoolean("ENABLE_WORKING_SETS")); //$NON-NLS-1$
		return composite;
	}
	/**
	 * The default button has been pressed. 
	 */
	protected void performDefaults() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();

		// Temporary option to enable wizard for project capability
		capabilityButton.setSelection(store.getDefaultBoolean("ENABLE_CONFIGURABLE_PROJECT_WIZARD")); //$NON-NLS-1$
		
		workingSetsButton.setSelection(store.getDefaultBoolean("ENABLE_WORKING_SETS")); //$NON-NLS-1$				
		//Call commented out on WorkbenchPreferencePage. 
		acceleratorPerformDefaults(store);
	}
	
	/**
	 *	The user has pressed Ok.  Store/apply this page's values appropriately.
	 */
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();

		// Temporary option to enable wizard for project capability
		store.setValue("ENABLE_CONFIGURABLE_PROJECT_WIZARD", capabilityButton.getSelection()); //$NON-NLS-1$
		store.setValue("ENABLE_WORKING_SETS", workingSetsButton.getSelection()); //$NON-NLS-1$
		
		//Call commented out on WorkbenchPreferencePage. 
		acceleratorPerformOk(store);
		return true;
	}	
}
