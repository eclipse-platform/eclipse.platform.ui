/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.gesturebindings;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class PreferencePage extends org.eclipse.jface.preference.PreferencePage
	implements IWorkbenchPreferencePage {
	
	private final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$
	
	/*
	private Button buttonCustomize;
	private Combo comboConfiguration;
	private String configurationId;
	private HashMap nameToConfigurationMap;
	private KeyManager keyManager;
	private SortedSet preferenceBindingSet;
	private IPreferenceStore preferenceStore;
	private SortedSet registryBindingSet;
	private SortedMap registryConfigurationMap;
	private SortedMap registryScopeMap;
	*/
	private IWorkbench workbench;

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		/*
		composite.setFont(parent.getFont());
		GridLayout gridLayoutComposite = new GridLayout();
		gridLayoutComposite.marginWidth = 0;
		gridLayoutComposite.marginHeight = 0;
		composite.setLayout(gridLayoutComposite);

		Label label = new Label(composite, SWT.LEFT);
		label.setFont(composite.getFont());
		label.setText("Active Configuration:");

		comboConfiguration = new Combo(composite, SWT.READ_ONLY);
		comboConfiguration.setFont(composite.getFont());
		GridData gridData = new GridData();
		gridData.widthHint = 200;
		comboConfiguration.setLayoutData(gridData);

		if (nameToConfigurationMap.isEmpty())
			comboConfiguration.setEnabled(false);
		else {
			String[] items = (String[]) nameToConfigurationMap.keySet().toArray(new String[nameToConfigurationMap.size()]);
			Arrays.sort(items, Collator.getInstance());
			comboConfiguration.setItems(items);
			Configuration configuration = (Configuration) registryConfigurationMap.get(configurationId);

			if (configuration != null)
				comboConfiguration.select(comboConfiguration.indexOf(configuration.getLabel().getName()));
		}

		buttonCustomize = new Button(composite, SWT.CENTER | SWT.PUSH);
		buttonCustomize.setFont(composite.getFont());
		buttonCustomize.setText("Customize Key Bindings...");
		gridData = setButtonLayoutData(buttonCustomize);
		gridData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
		gridData.widthHint += 8;

		buttonCustomize.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DialogCustomize dialogCustomize = new DialogCustomize(getShell(), IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID, 
					IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID, preferenceBindingSet);
				
				if (dialogCustomize.open() == DialogCustomize.OK) {
					preferenceBindingSet = dialogCustomize.getPreferenceBindingSet();	
				}
				
				//TBD: doesn't this have to be disposed somehow?
			}	
		});

		//TBD: WorkbenchHelp.setHelp(parent, IHelpContextIds.
		WORKBENCH_KEYBINDINGS_PREFERENCE_PAGE);
		 */

		return composite;	
	}

	public void init(IWorkbench workbench) {
		this.workbench = workbench;
		/*
		preferenceStore = getPreferenceStore();
		configurationId = loadConfiguration();		
		keyManager = KeyManager.getInstance();
		preferenceBindingSet = keyManager.getPreferenceBindingSet();
		registryBindingSet = keyManager.getRegistryBindingSet();
		registryConfigurationMap = keyManager.getRegistryConfigurationMap();
		registryScopeMap = keyManager.getRegistryScopeMap();	
		nameToConfigurationMap = new HashMap();	
		Collection configurations = registryConfigurationMap.values();
		Iterator iterator = configurations.iterator();

		while (iterator.hasNext()) {
			Configuration configuration = (Configuration) iterator.next();
			String name = configuration.getLabel().getName();
			
			if (!nameToConfigurationMap.containsKey(name))
				nameToConfigurationMap.put(name, configuration);
		}
		*/	
	}
	
	protected void performDefaults() {
		/*
		int result = SWT.YES;
		
		if (!preferenceBindingSet.isEmpty()) {		
			MessageBox messageBox = new MessageBox(getShell(), SWT.YES | SWT.NO | SWT.ICON_WARNING | SWT.APPLICATION_MODAL);
			messageBox.setText("Restore Defaults");
			messageBox.setMessage("This will clear all of your customized key bindings.\r\nAre you sure you want to do this?");
			result = messageBox.open();
		}
		
		if (result == SWT.YES) {			
			if (comboConfiguration != null && comboConfiguration.isEnabled()) {
				comboConfiguration.clearSelection();
				comboConfiguration.deselectAll();
				configurationId = preferenceStore.getDefaultString(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID);
				Configuration configuration = (Configuration) registryConfigurationMap.get(configurationId);

				if (configuration != null)
					comboConfiguration.select(comboConfiguration.indexOf(configuration.getLabel().getName()));
			}

			preferenceBindingSet = new TreeSet();
		}
		*/
	}	
	
	public boolean performOk() {
		/*
		if (comboConfiguration != null && comboConfiguration.isEnabled()) {
			int i = comboConfiguration.getSelectionIndex();
			
			if (i >= 0 && i < comboConfiguration.getItemCount()) {			
				String configurationName = comboConfiguration.getItem(i);
				
				if (configurationName != null) {				
					Configuration configuration = (Configuration) nameToConfigurationMap.get(configurationName);
					
					if (configuration != null) {
						configurationId = configuration.getLabel().getId();
						saveConfiguration(configurationId);					
	
						keyManager.setPreferenceBindingSet(preferenceBindingSet);
						keyManager.savePreference();					
						keyManager.update();
	
						if (workbench instanceof Workbench) {
							Workbench workbench = (Workbench) this.workbench;
							workbench.setActiveAcceleratorConfiguration(configuration);
						}
					}
				}
			}
		}
		*/
		return super.performOk();
	}
	
	protected IPreferenceStore doGetPreferenceStore() {
		return WorkbenchPlugin.getDefault().getPreferenceStore();
	}
	
	private String loadConfiguration() {
		/*
		String configuration = preferenceStore.getString(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID);

		if (configuration == null || configuration.length() == 0)
			configuration = preferenceStore.getDefaultString(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID);

		if (configuration == null)
			configuration = ZERO_LENGTH_STRING;

		return configuration;
		*/
		return null;
	}
	
	private void saveConfiguration(String configuration)
		throws IllegalArgumentException {
		/*
		if (configuration == null)
			throw new IllegalArgumentException();

		preferenceStore.setValue(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID, configuration);
		*/
	}
}
