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
	
	// State for encoding group
	private String defaultEnc;
	private Button defaultEncodingButton;
	private Button otherEncodingButton;
	private Combo encodingCombo;
	
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

		// Call commented out on WorkbenchPreferencePage. 
		createAcceleratorConfigurationGroup(composite, WorkbenchMessages.getString("WorkbenchPreference.acceleratorConfiguration")); //$NON-NLS-1$
		
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
		createSpace(composite);
		createEncodingGroup(composite);
		
		validCheck();
		
		return composite;
	}

	private void createEncodingGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setText(WorkbenchMessages.getString("WorkbenchPreference.encoding")); //$NON-NLS-1$
		
		SelectionAdapter buttonListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEncodingState(defaultEncodingButton.getSelection());
				validCheck();
			}
		};
		
		defaultEncodingButton = new Button(group, SWT.RADIO);
		defaultEnc = System.getProperty("file.encoding", "UTF-8");  //$NON-NLS-1$  //$NON-NLS-2$
		defaultEncodingButton.setText(WorkbenchMessages.format("WorkbenchPreference.defaultEncoding", new String[] { defaultEnc })); //$NON-NLS-1$
		data = new GridData();
		data.horizontalSpan = 2;
		defaultEncodingButton.setLayoutData(data);
		defaultEncodingButton.addSelectionListener(buttonListener);
		
		otherEncodingButton = new Button(group, SWT.RADIO);
		otherEncodingButton.setText(WorkbenchMessages.getString("WorkbenchPreference.otherEncoding")); //$NON-NLS-1$
		otherEncodingButton.addSelectionListener(buttonListener);
		
		encodingCombo = new Combo(group, SWT.NONE);
		data = new GridData();
		data.widthHint = convertWidthInCharsToPixels(15);
		encodingCombo.setLayoutData(data);
		encodingCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validCheck();
			}
		});

		ArrayList encodings = new ArrayList();
		int n = 0;
		try {
			n = Integer.parseInt(WorkbenchMessages.getString("WorkbenchPreference.numDefaultEncodings")); //$NON-NLS-1$
		}
		catch (NumberFormatException e) {
			// Ignore;
		}
		for (int i = 0; i < n; ++i) {
			String enc = WorkbenchMessages.getString("WorkbenchPreference.defaultEncoding" + (i+1), null); //$NON-NLS-1$
			if (enc != null) {
				encodings.add(enc);
			}
		}
		
		if (!encodings.contains(defaultEnc)) {
			encodings.add(defaultEnc);
		}

		String enc = ResourcesPlugin.getPlugin().getPluginPreferences().getString(ResourcesPlugin.PREF_ENCODING);
		boolean isDefault = enc == null || enc.length() == 0;

	 	if (!isDefault && !encodings.contains(enc)) {
			encodings.add(enc);
		}
		Collections.sort(encodings);
		for (int i = 0; i < encodings.size(); ++i) {
			encodingCombo.add((String) encodings.get(i));
		}

		encodingCombo.setText(isDefault ? defaultEnc : enc);
		
		updateEncodingState(isDefault);
	}

	/**
	 * Updates the valid state of the page, and any error message.
	 */
	private void validCheck() {
		if (!isEncodingValid()) {
			setErrorMessage(WorkbenchMessages.getString("WorkbenchPreference.unsupportedEncoding")); //$NON-NLS-1$
			setValid(false);
		}
		else {
			setErrorMessage(null);
			setValid(true);
		}
	}
	
	private boolean isEncodingValid() {
		return defaultEncodingButton.getSelection() ||
			isValidEncoding(encodingCombo.getText());
	}
	
	private boolean isValidEncoding(String enc) {
		try {
			new String(new byte[0], enc);
			return true;
		}
		catch (UnsupportedEncodingException e) {
			return false;
		}
	}
	
	private void updateEncodingState(boolean useDefault) {
		defaultEncodingButton.setSelection(useDefault);
		otherEncodingButton.setSelection(!useDefault);
		encodingCombo.setEnabled(!useDefault);
	}
	
	/**
	 * The default button has been pressed. 
	 */
	protected void performDefaults() {
		IPreferenceStore store = getPreferenceStore();

		//Call commented out on WorkbenchPreferencePage. 
		acceleratorPerformDefaults(store);
		
		coolBarsButton.setSelection(store.getDefaultBoolean("ENABLE_COOL_BARS")); //$NON-NLS-1$				
//		newMenusButton.setSelection(store.getDefaultBoolean("ENABLE_NEW_MENUS")); //$NON-NLS-1$
		
		updateEncodingState(true);
	}
	
	/**
	 *	The user has pressed Ok.  Store/apply this page's values appropriately.
	 */
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();

//		store.setValue("ENABLE_NEW_MENUS", newMenusButton.getSelection()); //$NON-NLS-1$
		store.setValue("ENABLE_COOL_BARS", coolBarsButton.getSelection()); //$NON-NLS-1$

		//Call commented out on WorkbenchPreferencePage. 
		acceleratorPerformOk(store);
		
		Preferences resourcePrefs = ResourcesPlugin.getPlugin().getPluginPreferences();
		if (defaultEncodingButton.getSelection()) {
			resourcePrefs.setToDefault(ResourcesPlugin.PREF_ENCODING);
		}
		else {
			String enc = encodingCombo.getText();
			resourcePrefs.setValue(ResourcesPlugin.PREF_ENCODING, enc);
		}
		ResourcesPlugin.getPlugin().savePluginPreferences();
		WorkbenchPlugin.getDefault().savePluginPreferences();
		return true;
	}	
}
