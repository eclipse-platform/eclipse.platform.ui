/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import java.text.Collator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class PreferencePage extends org.eclipse.jface.preference.PreferencePage
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
		Registry registry = Registry.getInstance();
		Collection configurations = registry.getConfigurationMap().values();
		Iterator iterator = configurations.iterator();

		while (iterator.hasNext()) {
			Configuration acceleratorConfiguration = (Configuration) iterator.next();
			namesToConfiguration.put(acceleratorConfiguration.getName(), acceleratorConfiguration);	
		}
		
		Configuration acceleratorConfiguration = ((Workbench)workbench).getActiveAcceleratorConfiguration();
		
		if (acceleratorConfiguration != null)
			activeAcceleratorConfigurationName = acceleratorConfiguration.getName();
	}

	protected void performDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();	
		String id = preferenceStore.getDefaultString(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID);

		Registry acceleratorRegistry = Registry.getInstance();
		Map acceleratorConfigurations = acceleratorRegistry.getConfigurationMap();
		Configuration acceleratorConfiguration = (Configuration) acceleratorConfigurations.get(id);
		
		if (acceleratorConfiguration == null)
			acceleratorConfiguration = 
				(Configuration) acceleratorConfigurations.get(IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID); 

		if (acceleratorConfiguration != null) { 
			String name = acceleratorConfiguration.getName();

			if (name != null && accelConfigCombo != null)
				accelConfigCombo.select(accelConfigCombo.indexOf(name));
		}
	}	

	public boolean performOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();		
			// store the active accelerator configuration id
		if(accelConfigCombo != null) {
			String configName = accelConfigCombo.getText();
			Configuration config = (Configuration)namesToConfiguration.get(configName);
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
