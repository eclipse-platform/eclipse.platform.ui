/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class KeyPreferencePage extends org.eclipse.jface.preference.PreferencePage
	implements IWorkbenchPreferencePage {
	
	private final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$
	
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
	private IWorkbench workbench;

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
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
			KeyConfiguration configuration = (KeyConfiguration) registryConfigurationMap.get(configurationId);

			if (configuration != null)
				comboConfiguration.select(comboConfiguration.indexOf(configuration.getName()));
		}

		buttonCustomize = new Button(composite, SWT.CENTER | SWT.PUSH);
		buttonCustomize.setFont(composite.getFont());
		buttonCustomize.setText("Customize Key Bindings...");
		gridData = setButtonLayoutData(buttonCustomize);
		gridData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
		gridData.widthHint += 8;

		buttonCustomize.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				DialogCustomize dialogCustomize = new DialogCustomize(getShell(), IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID, 
					IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID, preferenceBindingSet);
				
				if (dialogCustomize.open() == DialogCustomize.OK) {
					preferenceBindingSet = dialogCustomize.getPreferenceBindingSet();	
				}
				
				// TODO: doesn't this have to be disposed?
			}	
		});

		// TODO: WorkbenchHelp.setHelp(parent, IHelpContextIds.WORKBENCH_KEYBINDINGS_PREFERENCE_PAGE);

		return composite;	
	}

	public void init(IWorkbench workbench) {
		this.workbench = workbench;
		keyManager = KeyManager.getInstance();
		preferenceStore = getPreferenceStore();
		configurationId = loadConfiguration();		

		List pathItems = new ArrayList();
		pathItems.add(KeyManager.systemPlatform());
		pathItems.add(KeyManager.systemLocale());
		State[] states = new State[] { State.create(pathItems) };	

		CoreRegistry coreRegistry = CoreRegistry.getInstance();
		LocalRegistry localRegistry = LocalRegistry.getInstance();
		PreferenceRegistry preferenceRegistry = PreferenceRegistry.getInstance();

		SortedSet coreRegistryKeyBindingSet = new TreeSet();
		coreRegistryKeyBindingSet.addAll(coreRegistry.getKeyBindings());	
		SortedSet coreRegistryRegionalKeyBindingSet = new TreeSet();
		coreRegistryRegionalKeyBindingSet.addAll(coreRegistry.getRegionalKeyBindings());
		coreRegistryKeyBindingSet.addAll(KeyManager.solveRegionalKeyBindingSet(coreRegistryRegionalKeyBindingSet, states));

		SortedSet localRegistryKeyBindingSet = new TreeSet();
		localRegistryKeyBindingSet.addAll(localRegistry.getKeyBindings());	
		SortedSet localRegistryRegionalKeyBindingSet = new TreeSet();
		localRegistryRegionalKeyBindingSet.addAll(localRegistry.getRegionalKeyBindings());
		localRegistryKeyBindingSet.addAll(KeyManager.solveRegionalKeyBindingSet(localRegistryRegionalKeyBindingSet, states));

		SortedSet preferenceRegistryKeyBindingSet = new TreeSet();
		preferenceRegistryKeyBindingSet.addAll(preferenceRegistry.getKeyBindings());	
	
		List registryKeyConfigurations = new ArrayList();
		registryKeyConfigurations.addAll(coreRegistry.getKeyConfigurations());
		registryKeyConfigurations.addAll(localRegistry.getKeyConfigurations());
		registryKeyConfigurations.addAll(preferenceRegistry.getKeyConfigurations());
		registryConfigurationMap = KeyConfiguration.sortedMap(registryKeyConfigurations);
		
		List registryScopes = new ArrayList();
		registryScopes.addAll(coreRegistry.getScopes());
		registryScopes.addAll(localRegistry.getScopes());
		registryScopes.addAll(preferenceRegistry.getScopes());
		registryScopeMap = Scope.sortedMap(registryScopes);

		registryBindingSet = new TreeSet();		
		registryBindingSet.addAll(coreRegistryKeyBindingSet);
		registryBindingSet.addAll(localRegistryKeyBindingSet);

		preferenceBindingSet = new TreeSet();
		preferenceBindingSet.addAll(preferenceRegistryKeyBindingSet);

		nameToConfigurationMap = new HashMap();	
		Collection configurations = registryConfigurationMap.values();
		Iterator iterator = configurations.iterator();

		while (iterator.hasNext()) {
			KeyConfiguration configuration = (KeyConfiguration) iterator.next();
			String name = configuration.getName();
			
			if (!nameToConfigurationMap.containsKey(name))
				nameToConfigurationMap.put(name, configuration);
		}	
	}
	
	protected void performDefaults() {
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
				KeyConfiguration configuration = (KeyConfiguration) registryConfigurationMap.get(configurationId);

				if (configuration != null)
					comboConfiguration.select(comboConfiguration.indexOf(configuration.getName()));
			}

			preferenceBindingSet = new TreeSet();
		}
	}	
	
	public boolean performOk() {
		if (comboConfiguration != null && comboConfiguration.isEnabled()) {
			int i = comboConfiguration.getSelectionIndex();
			
			if (i >= 0 && i < comboConfiguration.getItemCount()) {			
				String configurationName = comboConfiguration.getItem(i);
				
				if (configurationName != null) {				
					KeyConfiguration configuration = (KeyConfiguration) nameToConfigurationMap.get(configurationName);
					
					if (configuration != null) {
						configurationId = configuration.getId();
						saveConfiguration(configurationId);					
	
						List preferenceKeyBindings = new ArrayList();
						preferenceKeyBindings.addAll(preferenceBindingSet);

						PreferenceRegistry preferenceRegistry = PreferenceRegistry.getInstance();
						preferenceRegistry.setKeyBindings(preferenceKeyBindings);

						try {
							preferenceRegistry.save();
						} catch (IOException eIO) {
						}
							
						keyManager.update();
	
						if (workbench instanceof Workbench) {
							Workbench workbench = (Workbench) this.workbench;
							workbench.setActiveAcceleratorConfiguration(configuration);
						}
					}
				}
			}
		}
		
		return super.performOk();
	}
	
	protected IPreferenceStore doGetPreferenceStore() {
		return WorkbenchPlugin.getDefault().getPreferenceStore();
	}
	
	private String loadConfiguration() {
		String configuration = preferenceStore.getString(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID);

		if (configuration == null || configuration.length() == 0)
			configuration = preferenceStore.getDefaultString(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID);

		if (configuration == null)
			configuration = ZERO_LENGTH_STRING;

		return configuration;
	}
	
	private void saveConfiguration(String configuration)
		throws IllegalArgumentException {
		if (configuration == null)
			throw new IllegalArgumentException();

		preferenceStore.setValue(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID, configuration);
	}
}
