/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.ui.internal.dialogs;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

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
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * @version 	1.0
 * @author
 */
public class WorkInProgressPreferencePage extends WorkbenchPreferencePage {

	// Temporary option to enable cool bars
	private Button coolBarsButton;
	// Temporary option to enable new menu structure
	private Button newMenusButton;
	
	private Button doubleClickButton;
	private Button singleClickButton;
	private Button selectOnHoverButton;
	private Button openAfterDelayButton;
	
	private boolean openOnSingleClick;
	private boolean selectOnHover;
	private boolean openAfterDelay;

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
		
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		openOnSingleClick = store.getBoolean(IPreferenceConstants.OPEN_ON_SINGLE_CLICK); //$NON-NLS-1$
		selectOnHover = store.getBoolean(IPreferenceConstants.SELECT_ON_HOVER); //$NON-NLS-1$
		openAfterDelay = store.getBoolean(IPreferenceConstants.OPEN_AFTER_DELAY); //$NON-NLS-1$
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
		createSingleClickGroup(composite);

//		createEncodingGroup(composite);
		
		return composite;
	}
	
	private void createSingleClickGroup(Composite composite) {
		
		Group buttonComposite = new Group(composite, SWT.LEFT);
		GridLayout layout = new GridLayout();
		buttonComposite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		buttonComposite.setLayoutData(data);
		buttonComposite.setText(WorkbenchMessages.getString("WorkInProgressPreference.openMode")); //$NON-NLS-1$
		

		String label = WorkbenchMessages.getString("WorkInProgressPreference.doubleClick"); //$NON-NLS-1$	
		doubleClickButton = createRadioButton(buttonComposite,label);
		doubleClickButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectClickMode(singleClickButton.getSelection());
			}
		});
		doubleClickButton.setSelection(!openOnSingleClick);

		label = WorkbenchMessages.getString("WorkInProgressPreference.singleClick"); //$NON-NLS-1$
		singleClickButton = createRadioButton(buttonComposite,label);
		singleClickButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectClickMode(singleClickButton.getSelection());
			}
		});
		singleClickButton.setSelection(openOnSingleClick);
		
		label = WorkbenchMessages.getString("WorkInProgressPreference.singleClick_SelectOnHover"); //$NON-NLS-1$				
		selectOnHoverButton = new Button(buttonComposite, SWT.CHECK | SWT.LEFT);
		selectOnHoverButton.setText(label);
		selectOnHoverButton.setEnabled(openOnSingleClick);
		selectOnHoverButton.setSelection(selectOnHover);
		selectOnHoverButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectOnHover = selectOnHoverButton.getSelection();
			}
		});
		data = new GridData();
		data.horizontalIndent = 20;
		selectOnHoverButton.setLayoutData(data);
		
		
		label = WorkbenchMessages.getString("WorkInProgressPreference.singleClick_OpenAfterDelay"); //$NON-NLS-1$				
		openAfterDelayButton = new Button(buttonComposite, SWT.CHECK | SWT.LEFT);
		openAfterDelayButton.setText(label);
		openAfterDelayButton.setEnabled(openOnSingleClick);
		openAfterDelayButton.setSelection(openAfterDelay);
		openAfterDelayButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openAfterDelay = openAfterDelayButton.getSelection();
			}
		});		
		data = new GridData();
		data.horizontalIndent = 20;
		openAfterDelayButton.setLayoutData(data);
		
		Label note = new Label(buttonComposite, SWT.NONE);
		note.setText(WorkbenchMessages.getString("WorkInProgressPreference.noEffectOnAllViews")); //$NON-NLS-1$
	}
	
	private void selectClickMode(boolean singleClick) {
		openOnSingleClick = singleClick;
		selectOnHoverButton.setEnabled(openOnSingleClick);
		openAfterDelayButton.setEnabled(openOnSingleClick);
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
				boolean useDefault = e.widget == defaultEncodingButton;
				updateEncodingState(useDefault);
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
		
		IPreferenceStore store = getPreferenceStore();
		boolean useDefault = !store.getBoolean(IPreferenceConstants.IS_ENCODING_SET);
		updateEncodingState(useDefault);
		
		if (!encodings.contains(defaultEnc)) {
			encodings.add(defaultEnc);
		}

		String enc = store.getString(IPreferenceConstants.ENCODING);
		if (enc.length() > 0) {
			encodingCombo.setText(enc);
		 	if (!encodings.contains(enc)) {
				encodings.add(enc);
		 	}
		}
		else {
			encodingCombo.setText(defaultEnc);
		}
		
		Collections.sort(encodings);
		for (int i = 0; i < encodings.size(); ++i) {
			encodingCombo.add((String) encodings.get(i));
		}
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
		
		boolean openOnSingleClick = store.getDefaultBoolean(IPreferenceConstants.OPEN_ON_SINGLE_CLICK); //$NON-NLS-1$
		boolean selectOnHover = store.getDefaultBoolean(IPreferenceConstants.SELECT_ON_HOVER); //$NON-NLS-1$
		boolean openAfterDelay = store.getDefaultBoolean(IPreferenceConstants.OPEN_AFTER_DELAY); //$NON-NLS-1$
		singleClickButton.setSelection(openOnSingleClick);
		doubleClickButton.setSelection(!openOnSingleClick);
		selectOnHoverButton.setSelection(selectOnHover);
		openAfterDelayButton.setSelection(openAfterDelay);
		selectOnHoverButton.setEnabled(openOnSingleClick);
		openAfterDelayButton.setEnabled(openOnSingleClick);

		coolBarsButton.setSelection(store.getDefaultBoolean("ENABLE_COOL_BARS")); //$NON-NLS-1$				
//		newMenusButton.setSelection(store.getDefaultBoolean("ENABLE_NEW_MENUS")); //$NON-NLS-1$
		
//		updateEncodingState(true);
	}
	
	/**
	 *	The user has pressed Ok.  Store/apply this page's values appropriately.
	 */
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();

//		store.setValue("ENABLE_NEW_MENUS", newMenusButton.getSelection()); //$NON-NLS-1$
		store.setValue("ENABLE_COOL_BARS", coolBarsButton.getSelection()); //$NON-NLS-1$

		store.setValue(IPreferenceConstants.OPEN_ON_SINGLE_CLICK,openOnSingleClick); //$NON-NLS-1$
		store.setValue(IPreferenceConstants.SELECT_ON_HOVER,selectOnHover); //$NON-NLS-1$
		store.setValue(IPreferenceConstants.OPEN_AFTER_DELAY,openAfterDelay); //$NON-NLS-1$
		int singleClickMethod = openOnSingleClick ? OpenStrategy.SINGLE_CLICK : OpenStrategy.DOUBLE_CLICK;
		if(openOnSingleClick) {
			if(selectOnHover)
				singleClickMethod |= OpenStrategy.SELECT_ON_HOVER;
			if(openAfterDelay)
				singleClickMethod |= OpenStrategy.ARROW_KEYS_OPEN;
		}
		OpenStrategy.setOpenMethod(singleClickMethod);

		//Call commented out on WorkbenchPreferencePage. 
		acceleratorPerformOk(store);
		
		//store.setValue(IPreferenceConstants.IS_ENCODING_SET, otherEncodingButton.getSelection());
		//store.setValue(IPreferenceConstants.ENCODING, encodingCombo.getText());
		
		return true;
	}	
}
