/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

	// Temporary option to enable working sets
	private Button workingSetsButton;
	// Temporary option to enable cool bars
	private Button coolBarsButton;
	// Temporary option to enable new menu structure
	private Button newMenusButton;
	
	private Button singleClickButtons[] = new Button[4];
	private int singleClickMethod;
	
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
	private Button createTempOption(Composite parent, String text, String prefId, boolean restartNeeded,String labelText) {
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
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		// Call commented out on WorkbenchPreferencePage. 
		createAcceleratorConfigurationGroup(composite, WorkbenchMessages.getString("WorkbenchPreference.acceleratorConfiguration")); //$NON-NLS-1$

		// Temporary option to enable workbench working sets
		workingSetsButton =
			createTempOption(
				composite,
				WorkbenchMessages.getString("WorkInProgressPreference.enableWorkbenchWorkingSets"), //$NON-NLS-1$
				"ENABLE_WORKING_SETS", //$NON-NLS-1$
				true,
				null);

		// Temporary option to enable cool bars
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
		createSpace(composite);
		createSingleClickGroup(composite);

		return composite;
	}
	
	private void createSingleClickGroup(Composite composite) {
		Group buttonComposite = new Group(composite, SWT.LEFT);
		GridLayout layout = new GridLayout();
		buttonComposite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		buttonComposite.setLayoutData(data);
		buttonComposite.setText(WorkbenchMessages.getString("WorkInProgressPreference.singleClick")); //$NON-NLS-1$
		
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		singleClickMethod = store.getInt("SINGLE_CLICK_METHOD"); //$NON-NLS-1$
		createSingleClickButton(buttonComposite,0,WorkbenchMessages.getString("WorkInProgressPreference.singleClickNoTimer"),OpenStrategy.NO_TIMER); //$NON-NLS-1$
		createSingleClickButton(buttonComposite,1,WorkbenchMessages.getString("WorkInProgressPreference.fileExplorer"),OpenStrategy.FILE_EXPLORER); //$NON-NLS-1$
		createSingleClickButton(buttonComposite,2,WorkbenchMessages.getString("WorkInProgressPreference.activeDesktop"),OpenStrategy.ACTIVE_DESKTOP); //$NON-NLS-1$
		createSingleClickButton(buttonComposite,3,WorkbenchMessages.getString("WorkInProgressPreference.doubleClick"),OpenStrategy.DOUBLE_CLICK); //$NON-NLS-1$
	}
	
	private void createSingleClickButton(Composite parent,int index,String label,final int method) {
		singleClickButtons[index] = createRadioButton(parent,label);
		singleClickButtons[index].addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				singleClickMethod = method;
			}
		});
		singleClickButtons[index].setSelection(singleClickMethod == method);
	}
	/**
	 * The default button has been pressed. 
	 */
	protected void performDefaults() {
		IPreferenceStore store = getPreferenceStore();

		//Call commented out on WorkbenchPreferencePage. 
		acceleratorPerformDefaults(store);
		singleClickButtons[3].setSelection(true);

		workingSetsButton.setSelection(store.getDefaultBoolean("ENABLE_WORKING_SETS")); //$NON-NLS-1$				
		coolBarsButton.setSelection(store.getDefaultBoolean("ENABLE_COOL_BARS")); //$NON-NLS-1$				
//		newMenusButton.setSelection(store.getDefaultBoolean("ENABLE_NEW_MENUS")); //$NON-NLS-1$
		
	}
	
	/**
	 *	The user has pressed Ok.  Store/apply this page's values appropriately.
	 */
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();

		store.setValue("ENABLE_WORKING_SETS", workingSetsButton.getSelection()); //$NON-NLS-1$
//		store.setValue("ENABLE_NEW_MENUS", newMenusButton.getSelection()); //$NON-NLS-1$
		store.setValue("ENABLE_COOL_BARS", coolBarsButton.getSelection()); //$NON-NLS-1$
		store.setValue("SINGLE_CLICK_METHOD",singleClickMethod); //$NON-NLS-1$
		OpenStrategy.setOpenMethod(singleClickMethod);
		//Call commented out on WorkbenchPreferencePage. 
		acceleratorPerformOk(store);
		return true;
	}	
}
