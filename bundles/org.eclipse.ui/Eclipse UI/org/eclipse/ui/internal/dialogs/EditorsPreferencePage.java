package org.eclipse.ui.internal.dialogs;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.UnsupportedEncodingException;
import java.text.Collator;
import java.util.*;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.EncodingDefinition;
import org.eclipse.ui.editors.text.EncodingDefinitionManager;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.registry.AcceleratorConfiguration;
import org.eclipse.ui.internal.registry.AcceleratorRegistry;

public class EditorsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private IWorkbench workbench;
	
	// State for encoding group
	private String defaultEnc;
	private Button defaultEncodingButton;
	private Button otherEncodingButton;
	private Combo encodingCombo;

	private Combo accelConfigCombo;

	private Button reuseEditors;
	private IntegerFieldEditor reuseEditorsThreshold;
	private Composite editorReuseGroup;
	
	private IntegerFieldEditor recentFilesEditor;

	// hashtable mapping accelerator configuration names to accelerator configuration
	private Hashtable namesToConfiguration;
	// the name of the active accelerator configuration
	private String activeAcceleratorConfigurationName;
			
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(parent.getFont());

		WorkbenchPreferencePage.createSpace(composite);
		createEditorHistoryGroup(composite);
		
		WorkbenchPreferencePage.createSpace(composite);
		createEditorReuseGroup(composite);
				
		WorkbenchPreferencePage.createSpace(composite);
		createAcceleratorConfigurationGroup(composite, WorkbenchMessages.getString("WorkbenchPreference.acceleratorConfiguration"));

		WorkbenchPreferencePage.createSpace(composite);
		createEncodingGroup(composite);
		validCheck();

		return composite;
	}
	
	public void init(IWorkbench aWorkbench) {
		workbench = aWorkbench;
		acceleratorInit(workbench);
	}
	
	protected void performDefaults() {
		IPreferenceStore store = getPreferenceStore();
		updateEncodingState(true);
		acceleratorPerformDefaults(store);
		reuseEditors.setSelection(store.getDefaultBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN));
		reuseEditorsThreshold.loadDefault();
		reuseEditorsThreshold.getLabelControl(editorReuseGroup).setEnabled(reuseEditors.getSelection());
		reuseEditorsThreshold.getTextControl(editorReuseGroup).setEnabled(reuseEditors.getSelection());
		recentFilesEditor.loadDefault();
	}
	
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();	
		Preferences resourcePrefs = ResourcesPlugin.getPlugin().getPluginPreferences();
		if (defaultEncodingButton.getSelection()) {
			resourcePrefs.setToDefault(ResourcesPlugin.PREF_ENCODING);
		}
		else {
			String enc = encodingCombo.getText();
			Iterator encodings = EncodingDefinitionManager.getEncodings().iterator();
			boolean found = false;
			while(encodings.hasNext()){
				EncodingDefinition definition = (EncodingDefinition) encodings.next();
				if(definition.getLabel().equals(enc)){
					resourcePrefs.setValue(ResourcesPlugin.PREF_ENCODING, definition.getValue());
					found = true;
					break;
				}
			}
			//If there is a new one then make sure it is added to the encodings
			if(!found){
				resourcePrefs.setValue(ResourcesPlugin.PREF_ENCODING, enc);
				EncodingDefinitionManager.addEncoding(enc);
			}
		}
		
		ResourcesPlugin.getPlugin().savePluginPreferences();

		acceleratorPerformOk(store);		
		
		// store the reuse editors setting
		store.setValue(IPreferenceConstants.REUSE_EDITORS_BOOLEAN,reuseEditors.getSelection());
		reuseEditorsThreshold.store();

		// store the recent files setting
		recentFilesEditor.store();
						
		return super.performOk();
	}
	/**
	 * Returns preference store that belongs to the our plugin.
	 *
	 * @return the preference store for this plugin
	 */
	protected IPreferenceStore doGetPreferenceStore() {
		return WorkbenchPlugin.getDefault().getPreferenceStore();
	}
		
	private void createEncodingGroup(Composite parent) {
		
		Font font = parent.getFont();
		Group group = new Group(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setText(WorkbenchMessages.getString("WorkbenchPreference.encoding")); //$NON-NLS-1$
		group.setFont(font);
		
		SelectionAdapter buttonListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEncodingState(defaultEncodingButton.getSelection());
				validCheck();
			}
		};
		
		defaultEnc = EncodingDefinitionManager.getDefaultEncoding();
		defaultEncodingButton = new Button(group, SWT.RADIO);
		defaultEncodingButton.setText(
			WorkbenchMessages.format(
				"WorkbenchPreference.defaultEncoding", //$NON-NLS-1$
				new String[] { defaultEnc })); 
		data = new GridData();
		data.horizontalSpan = 2;
		defaultEncodingButton.setLayoutData(data);
		defaultEncodingButton.addSelectionListener(buttonListener);
		defaultEncodingButton.setFont(font);
		
		otherEncodingButton = new Button(group, SWT.RADIO);
		otherEncodingButton.setText(WorkbenchMessages.getString("WorkbenchPreference.otherEncoding")); //$NON-NLS-1$
		otherEncodingButton.addSelectionListener(buttonListener);
		otherEncodingButton.setFont(font);
		
		encodingCombo = new Combo(group, SWT.NONE);
		data = new GridData();
		data.widthHint = convertWidthInCharsToPixels(15);
		encodingCombo.setFont(font);
		encodingCombo.setLayoutData(data);
		encodingCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validCheck();
			}
		});

		java.util.Collection encodings = EncodingDefinitionManager.getLabelSortedEncodings();
		Iterator iterator = encodings.iterator();
		
		String encodingValue = ResourcesPlugin.getPlugin().getPluginPreferences().getString(ResourcesPlugin.PREF_ENCODING);
		String selectedLabel = null;
		
		while (iterator.hasNext()) {
			EncodingDefinition next = (EncodingDefinition) iterator.next();
			encodingCombo.add(next.getLabel());
			if(next.getValue().equals(encodingValue))
				selectedLabel = next.getLabel();
		}

		boolean isDefault = Preferences.STRING_DEFAULT_DEFAULT.equals(encodingValue);
		
		encodingCombo.setText(isDefault ? defaultEnc : selectedLabel);
		
		updateEncodingState(isDefault);
	}
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
		} catch (UnsupportedEncodingException e) {
			return false;
		}
	}
	
	private void updateEncodingState(boolean useDefault) {
		defaultEncodingButton.setSelection(useDefault);
		otherEncodingButton.setSelection(!useDefault);
		encodingCombo.setEnabled(!useDefault);
		setErrorMessage(null);
		setValid(true);
	}
	
		/**
	 * Creates a composite that contains a label and combo box specifying the active
	 * accelerator configuration.
	 */
	protected void createAcceleratorConfigurationGroup(Composite composite, String label) {
		
		Font font = composite.getFont();
		
		Composite groupComposite = new Composite(composite, SWT.LEFT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		groupComposite.setLayout(layout);
		GridData gd = new GridData();
		gd.horizontalAlignment = gd.FILL;
		gd.grabExcessHorizontalSpace = true;
		groupComposite.setLayoutData(gd);
		groupComposite.setFont(font);
		
		Label configLabel = WorkbenchPreferencePage.createLabel(groupComposite, label);
		accelConfigCombo = WorkbenchPreferencePage.createCombo(groupComposite);

		if(namesToConfiguration.size() > 0) { 
			String[] comboItems = new String[namesToConfiguration.size()];
			namesToConfiguration.keySet().toArray(comboItems);
			Arrays.sort(comboItems,Collator.getInstance());
			accelConfigCombo.setItems(comboItems);
		
			if(activeAcceleratorConfigurationName != null)
				accelConfigCombo.select(accelConfigCombo.indexOf(activeAcceleratorConfigurationName));
		} else {
			accelConfigCombo.setEnabled(false);
		}	
	}
	protected void acceleratorInit(IWorkbench aWorkbench) {
		namesToConfiguration = new Hashtable();
		WorkbenchPlugin plugin = WorkbenchPlugin.getDefault();
		AcceleratorRegistry registry = plugin.getAcceleratorRegistry();
		AcceleratorConfiguration configs[] = registry.getConfigsWithSets();
		for (int i = 0; i < configs.length; i++)
			namesToConfiguration.put(configs[i].getName(), configs[i]);	
		
		AcceleratorConfiguration config = ((Workbench)aWorkbench).getActiveAcceleratorConfiguration();
		if(config != null)
			activeAcceleratorConfigurationName = config.getName();
	}	
	protected void acceleratorPerformDefaults(IPreferenceStore store) {
		// Sets the accelerator configuration selection to the default configuration
		String id = store.getDefaultString(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID);
		AcceleratorRegistry registry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();
		AcceleratorConfiguration config = registry.getConfiguration(id);
		String name = null;
		if(config != null) 
			name = config.getName();
		if((name != null) && (accelConfigCombo != null))
			accelConfigCombo.select(accelConfigCombo.indexOf(name));
	}	
	protected void acceleratorPerformOk(IPreferenceStore store) {
		// store the active accelerator configuration id
		if(accelConfigCombo != null) {
			String configName = accelConfigCombo.getText();
			AcceleratorConfiguration config = (AcceleratorConfiguration)namesToConfiguration.get(configName);
			if(config != null) {
				Workbench workbench = (Workbench)PlatformUI.getWorkbench();
				workbench.setActiveAcceleratorConfiguration(config);
				store.setValue(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID, config.getId());
			}
		}
	}	
	/**
	 * Create a composite that contains entry fields specifying editor reuse preferences.
	 */
	private void createEditorReuseGroup(Composite composite) {
		
		Font font = composite.getFont();
		
		editorReuseGroup = new Composite(composite, SWT.LEFT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		editorReuseGroup.setLayout(layout);
		GridData gd = new GridData();
		gd.horizontalAlignment = gd.FILL;
		gd.grabExcessHorizontalSpace = true;
		editorReuseGroup.setLayoutData(gd);	
		editorReuseGroup.setFont(font);	
		
		reuseEditors = new Button(editorReuseGroup, SWT.CHECK);
		reuseEditors.setText(WorkbenchMessages.getString("WorkbenchPreference.reuseEditors")); //$NON-NLS-1$
		GridData reuseEditorsData = new GridData();
		reuseEditorsData.horizontalSpan = layout.numColumns;
		reuseEditors.setLayoutData(reuseEditorsData);
		reuseEditors.setFont(font);
		
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		reuseEditors.setSelection(store.getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN));
		reuseEditors.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				reuseEditorsThreshold.getLabelControl(editorReuseGroup).setEnabled(reuseEditors.getSelection());
				reuseEditorsThreshold.getTextControl(editorReuseGroup).setEnabled(reuseEditors.getSelection());
			}
		});
		
		reuseEditorsThreshold = new IntegerFieldEditor(IPreferenceConstants.REUSE_EDITORS, WorkbenchMessages.getString("WorkbenchPreference.reuseEditorsThreshold"), editorReuseGroup); //$NON-NLS-1$
		
		reuseEditorsThreshold.setPreferenceStore(WorkbenchPlugin.getDefault().getPreferenceStore());
		reuseEditorsThreshold.setPreferencePage(this);
		reuseEditorsThreshold.setTextLimit(2);
		reuseEditorsThreshold.setErrorMessage(WorkbenchMessages.getString("WorkbenchPreference.reuseEditorsThresholdError")); //$NON-NLS-1$
		reuseEditorsThreshold.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		reuseEditorsThreshold.setValidRange(1, 99);
		reuseEditorsThreshold.load();
		reuseEditorsThreshold.getLabelControl(editorReuseGroup).setEnabled(reuseEditors.getSelection());
		reuseEditorsThreshold.getTextControl(editorReuseGroup).setEnabled(reuseEditors.getSelection());
		reuseEditorsThreshold.setPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FieldEditor.IS_VALID)) 
					setValid(reuseEditorsThreshold.isValid());
			}
		});
	}
	/**
	 * Create a composite that contains entry fields specifying editor history preferences.
	 */
	private void createEditorHistoryGroup(Composite composite) {
		Composite groupComposite = new Composite(composite, SWT.LEFT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		groupComposite.setLayout(layout);
		GridData gd = new GridData();
		gd.horizontalAlignment = gd.FILL;
		gd.grabExcessHorizontalSpace = true;
		groupComposite.setLayoutData(gd);	
		groupComposite.setFont(composite.getFont());
		
		recentFilesEditor = new IntegerFieldEditor(IPreferenceConstants.RECENT_FILES, WorkbenchMessages.getString("WorkbenchPreference.recentFiles"), groupComposite); //$NON-NLS-1$

		int recentFilesMax = IPreferenceConstants.MAX_RECENT_FILES_SIZE;
		recentFilesEditor.setPreferenceStore(WorkbenchPlugin.getDefault().getPreferenceStore());
		recentFilesEditor.setPreferencePage(this);
		recentFilesEditor.setTextLimit(Integer.toString(recentFilesMax).length());
		recentFilesEditor.setErrorMessage(WorkbenchMessages.format("WorkbenchPreference.recentFilesError", new Object[] { new Integer(recentFilesMax)})); //$NON-NLS-1$
		recentFilesEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		recentFilesEditor.setValidRange(0, recentFilesMax);
		recentFilesEditor.load();
		recentFilesEditor.setPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FieldEditor.IS_VALID)) 
					setValid(recentFilesEditor.isValid());
			}
		});
		
	}
}

