/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.actions.keybindings;

import java.text.Collator;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class PreferencePage extends org.eclipse.jface.preference.PreferencePage
	implements IWorkbenchPreferencePage {

	private Button buttonCustomize;
	private DialogCustomize dialogCustomize;
	private HashMap nameToConfigurationMap;
	private Combo comboConfiguration;
	private String activeConfigurationName;

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		//composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		Label label = new Label(composite, SWT.LEFT);
		label.setFont(parent.getFont());
		label.setText("Configuration:");
		
		comboConfiguration = new Combo(composite, SWT.READ_ONLY);				
		GridData gridData = new GridData();
		gridData.widthHint = 150;
		comboConfiguration.setLayoutData(gridData);

		if (nameToConfigurationMap.size() > 0) { 
			String[] listItems = (String[]) nameToConfigurationMap.keySet().toArray(new String[nameToConfigurationMap.size()]);
			Arrays.sort(listItems, Collator.getInstance());
			comboConfiguration.setItems(listItems);
		
			if (activeConfigurationName != null)
				comboConfiguration.select(comboConfiguration.indexOf(activeConfigurationName));
		} else
			comboConfiguration.setEnabled(false);		
		
		Composite compositeCustomize = new Composite(composite, SWT.NONE);		
		GridLayout gridLayoutCompositeCustomize = new GridLayout();
		gridLayoutCompositeCustomize.marginWidth = 0;
		compositeCustomize.setLayout(gridLayoutCompositeCustomize);

		buttonCustomize = new Button(compositeCustomize, SWT.LEFT | SWT.PUSH);
		buttonCustomize.setText("Customize Key Bindings...");
		setButtonLayoutData(buttonCustomize);

		buttonCustomize.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (dialogCustomize == null)
					dialogCustomize = new DialogCustomize(getShell());
					
				dialogCustomize.open();
			}	
		});

		//WorkbenchHelp.setHelp(parent, IHelpContextIds.WORKBENCH_KEYBINDINGS_PREFERENCE_PAGE);
		return composite;	
	}
	
	public void init(IWorkbench workbench) {
		nameToConfigurationMap = new HashMap();
		Registry registry = Registry.getInstance();
		Collection configurations = registry.getConfigurationMap().values();
		Iterator iterator = configurations.iterator();

		while (iterator.hasNext()) {
			Configuration configuration = (Configuration) iterator.next();
			nameToConfigurationMap.put(configuration.getName(), configuration);	
		}
		
		Configuration configuration = ((Workbench) workbench).getActiveAcceleratorConfiguration();
		
		if (configuration != null)
			activeConfigurationName = configuration.getName();
	}

	protected void performDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();	
		String id = preferenceStore.getDefaultString(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID);

		Registry registry = Registry.getInstance();
		Map configurations = registry.getConfigurationMap();
		Configuration configuration = (Configuration) configurations.get(id);
		
		if (configuration == null)
			configuration = (Configuration) configurations.get(IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID); 

		if (configuration != null) { 
			String name = configuration.getName();

			if (name != null && comboConfiguration != null)
				comboConfiguration.select(comboConfiguration.indexOf(name));
		}
	}	

	public boolean performOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();		

		if (comboConfiguration != null) {
			String configNames = comboConfiguration.getItem(comboConfiguration.getSelectionIndex());
			
			if (configNames != null) {				
				Configuration config = (Configuration) nameToConfigurationMap.get(configNames);
				
				if (config != null) {
					Workbench workbench = (Workbench)PlatformUI.getWorkbench();
					workbench.setActiveAcceleratorConfiguration(config);
					preferenceStore.setValue(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID, config.getId());
				}
			}
		}
		
		return super.performOk();
	}
	
	protected IPreferenceStore doGetPreferenceStore() {
		return WorkbenchPlugin.getDefault().getPreferenceStore();
	}
}
