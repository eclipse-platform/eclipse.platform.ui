package org.eclipse.ui.internal.keybindings;

/**
Copyright (c) 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

import java.text.Collator;
import java.util.Arrays;
import java.util.Hashtable;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.AcceleratorConfiguration;
import org.eclipse.ui.internal.registry.AcceleratorRegistry;

public class KeyBindingPreferencePage extends PreferencePage
	implements IWorkbenchPreferencePage {

	private Combo accelConfigCombo;
	private Hashtable namesToConfiguration;
	private String activeAcceleratorConfigurationName;

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(parent.getFont());

		Font font = composite.getFont();			
		Composite groupComposite = new Composite(composite, SWT.LEFT);
		GridLayout groupLayout = new GridLayout();
		groupLayout.numColumns = 2;
		groupLayout.marginWidth = 0;
		groupLayout.marginHeight = 0;
		groupComposite.setLayout(groupLayout);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		groupComposite.setLayoutData(gd);
		groupComposite.setFont(font);
		
		createLabel(groupComposite, WorkbenchMessages.getString("WorkbenchPreference.acceleratorConfiguration"));
		accelConfigCombo = createCombo(groupComposite);

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

		//Tree tree = new Tree(composite, SWT.NULL);


		/*
		validCheck();		
		WorkbenchHelp.setHelp(parent, IHelpContextIds.WORKBENCH_EDITOR_PREFERENCE_PAGE);
		*/

		return composite;	
	}
	
	public void init(IWorkbench workbench) {
		namesToConfiguration = new Hashtable();
		WorkbenchPlugin plugin = WorkbenchPlugin.getDefault();
		AcceleratorRegistry registry = plugin.getAcceleratorRegistry();
		AcceleratorConfiguration configs[] = registry.getConfigsWithSets();
		for (int i = 0; i < configs.length; i++)
			namesToConfiguration.put(configs[i].getName(), configs[i]);	
		
		AcceleratorConfiguration config = ((Workbench)workbench).getActiveAcceleratorConfiguration();
		if(config != null)
			activeAcceleratorConfigurationName = config.getName();
	}

	protected void performDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		// Sets the accelerator configuration selection to the default configuration
		String id = preferenceStore.getDefaultString(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID);
		AcceleratorRegistry registry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();
		AcceleratorConfiguration config = registry.getConfiguration(id);
		String name = null;
		if(config != null) 
			name = config.getName();
		if((name != null) && (accelConfigCombo != null))
			accelConfigCombo.select(accelConfigCombo.indexOf(name));
	}	
		
	public boolean performOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();		
			// store the active accelerator configuration id
		if(accelConfigCombo != null) {
			String configName = accelConfigCombo.getText();
			AcceleratorConfiguration config = (AcceleratorConfiguration)namesToConfiguration.get(configName);
			if(config != null) {
				Workbench workbench = (Workbench)PlatformUI.getWorkbench();
				workbench.setActiveAcceleratorConfiguration(config);
				preferenceStore.setValue(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID, config.getId());
			}
		}
		
		return super.performOk();
	}
	
	protected IPreferenceStore doGetPreferenceStore() {
		return WorkbenchPlugin.getDefault().getPreferenceStore();
	}
	
	private static Combo createCombo(Composite parent) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		combo.setFont(parent.getFont());
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		combo.setLayoutData(gridData);
		return combo;
	}

	private static Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.LEFT);
		label.setFont(parent.getFont());
		label.setText(text);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		label.setLayoutData(gridData);
		return label;
	}
	
	/*
	private static Button createRadioButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.RADIO | SWT.LEFT);
		button.setFont(parent.getFont());
		button.setText(label);
		return button;
	}

	private static void createSpace(Composite parent) {
		Label space = new Label(parent, SWT.LEFT);
		GridData gridData = new GridData();
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = false;
		gridData.grabExcessVerticalSpace = false;
		gridData.horizontalAlignment = GridData.BEGINNING;
		gridData.verticalAlignment = GridData.CENTER;
		space.setLayoutData(gridData);
	}
	*/
}
