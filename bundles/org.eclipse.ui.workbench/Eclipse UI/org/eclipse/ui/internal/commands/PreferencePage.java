/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.SortedSet;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.keybindings.KeyMachine;
import org.eclipse.ui.internal.keybindings.KeyManager;

public class PreferencePage extends org.eclipse.jface.preference.PreferencePage
	implements IWorkbenchPreferencePage {

	private final static ResourceBundle resourceBundle = ResourceBundle.getBundle(PreferencePage.class.getName());

	private final static String ACTION_CONFLICT = Util.getString(resourceBundle, "ActionConflict"); //$NON-NLS-1$
	private final static String ACTION_UNDEFINED = Util.getString(resourceBundle, "ActionUndefined"); //$NON-NLS-1$
	private final static int DIFFERENCE_ADD = 0;	
	private final static int DIFFERENCE_CHANGE = 1;	
	private final static int DIFFERENCE_MINUS = 2;	
	private final static int DIFFERENCE_NONE = 3;	
	private final static Image IMAGE_CHANGE = ImageFactory.getImage("change"); //$NON-NLS-1$
	private final static Image IMAGE_MINUS = ImageFactory.getImage("minus"); //$NON-NLS-1$
	private final static Image IMAGE_PLUS = ImageFactory.getImage("plus"); //$NON-NLS-1$
	private final static RGB RGB_CONFLICT = new RGB(255, 0, 0);
	private final static RGB RGB_CONFLICT_MINUS = new RGB(255, 192, 192);
	private final static RGB RGB_MINUS =	new RGB(192, 192, 192);
	private final static int SPACE = 8;	
	private final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$
	
	private String defaultConfigurationId;
	private String defaultScopeId;
	private SortedSet preferenceBindingSet;	
	
	private KeyManager keyManager;
	private KeyMachine keyMachine;
	private SortedMap registryActionMap;
	private SortedSet registryBindingSet;
	private SortedMap registryConfigurationMap;
	private SortedMap registryScopeMap;
	
	private List actions;
	private List configurations;
	private List scopes;

	private String[] actionNames;
	private String[] configurationNames;	
	private String[] scopeNames;
	
	private SortedMap tree;
	private Map nameToKeySequenceMap;
	private List actionRecords = new ArrayList();	
	private List keySequenceRecords = new ArrayList();	
	
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
		composite.setFont(parent.getFont());
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.numColumns = 2;
		composite.setLayout(gridLayout);

		org.eclipse.swt.widgets.List listCommands = 
			new org.eclipse.swt.widgets.List(composite, SWT.BORDER | SWT.H_SCROLL | SWT.SINGLE | SWT.V_SCROLL);
		listCommands.setFont(composite.getFont());
		GridData gridData = new GridData(GridData.VERTICAL_ALIGN_FILL);
		gridData.widthHint = 200;
		listCommands.setLayoutData(gridData);

		TabFolder tabFolder = new TabFolder(composite, SWT.NULL);
		tabFolder.setFont(composite.getFont());
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
	
		TabItem tabItemProperties = new TabItem(tabFolder, SWT.NULL);
		tabItemProperties.setText("Properties");

		Composite compositeProperties = new Composite(tabFolder, SWT.NULL);
		compositeProperties.setFont(tabFolder.getFont());
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		compositeProperties.setLayout(gridLayout);
		tabItemProperties.setControl(compositeProperties);
		
		TabItem tabItemGestures = new TabItem(tabFolder, SWT.NULL);
		tabItemGestures.setText("Gestures");

		Composite compositeGestures = new Composite(tabFolder, SWT.NULL);
		compositeGestures.setFont(tabFolder.getFont());
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		compositeGestures.setLayout(gridLayout);
		tabItemGestures.setControl(compositeGestures);

		TabItem tabItemKeys = new TabItem(tabFolder, SWT.NULL);
		tabItemKeys.setText("Keys");

		Composite compositeKeys = new Composite(tabFolder, SWT.NULL);
		compositeKeys.setFont(tabFolder.getFont());
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		compositeKeys.setLayout(gridLayout);
		tabItemKeys.setControl(compositeKeys);

		/*
		org.eclipse.swt.widgets.Label labelName = new org.eclipse.swt.widgets.Label(compositeProperties, SWT.LEFT);
		labelName.setText("Name:");
		labelName.setLayoutData(new GridData(GridData.FILL_BOTH));
		*/

		org.eclipse.swt.widgets.Label labelDescription = new org.eclipse.swt.widgets.Label(compositeProperties, SWT.LEFT);
		labelDescription.setText("Description:");
		labelDescription.setLayoutData(new GridData(GridData.FILL_BOTH));

		org.eclipse.swt.widgets.Label labelImage = new org.eclipse.swt.widgets.Label(compositeProperties, SWT.LEFT);
		labelImage.setText("Image:");
		labelImage.setLayoutData(new GridData(GridData.FILL_BOTH));

		org.eclipse.swt.widgets.Label labelId= new org.eclipse.swt.widgets.Label(compositeProperties, SWT.LEFT);
		labelId.setText("Id:");
		labelId.setLayoutData(new GridData(GridData.FILL_BOTH));

		org.eclipse.swt.widgets.Label labelPlugin= new org.eclipse.swt.widgets.Label(compositeProperties, SWT.LEFT);
		labelPlugin.setText("Plugin:");
		labelPlugin.setLayoutData(new GridData(GridData.FILL_BOTH));

		Table tableKeys = new Table(compositeKeys, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tableKeys.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 75;		
		gridData.horizontalSpan = 3;		
		tableKeys.setLayoutData(gridData);
		tableKeys.setFont(tabFolder.getFont());

		TableColumn tableColumn = new TableColumn(tableKeys, SWT.NULL, 0);
		tableColumn.setResizable(false);
		tableColumn.setText(ZERO_LENGTH_STRING);
		tableColumn.setWidth(20);

		tableColumn = new TableColumn(tableKeys, SWT.NULL, 1);
		tableColumn.setResizable(true);
		//tableColumn.setText(Util.getString(resourceBundle, "HeaderKeySequence")); //$NON-NLS-1$
		tableColumn.setWidth(250);	

		tableColumn = new TableColumn(tableKeys, SWT.NULL, 2);
		tableColumn.setResizable(true);
		//tableColumn.setText(Util.getString(resourceBundle, "HeaderScope")); //$NON-NLS-1$
		tableColumn.setWidth(100);

		tableColumn = new TableColumn(tableKeys, SWT.NULL, 3);
		tableColumn.setResizable(true);
		//tableColumn.setText(Util.getString(resourceBundle, "HeaderConfiguration")); //$NON-NLS-1$
		tableColumn.setWidth(100);

		/*
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

		/*
		this.defaultConfigurationId = defaultConfigurationId;
		this.defaultScopeId = defaultScopeId;
		preferenceBindingSet = new TreeSet(preferenceBindingSet);
		Iterator iterator = preferenceBindingSet.iterator();
		
		while (iterator.hasNext())
			if (!(iterator.next() instanceof Binding))
				throw new IllegalArgumentException();
	
		this.preferenceBindingSet = preferenceBindingSet;
		*/

		keyManager = KeyManager.getInstance();
		keyMachine = keyManager.getKeyMachine();

		registryActionMap = org.eclipse.ui.internal.commands.Registry.getInstance().getCommandMap();
		actions = new ArrayList();
		actions.addAll(registryActionMap.values());
		Collections.sort(actions, Item.nameComparator());				
	
		registryBindingSet = keyManager.getRegistryBindingSet();
		
		registryConfigurationMap = keyManager.getRegistryConfigurationMap();
		configurations = new ArrayList();
		configurations.addAll(registryConfigurationMap.values());	
		Collections.sort(configurations, Item.nameComparator());				
		
		registryScopeMap = keyManager.getRegistryScopeMap();	
		scopes = new ArrayList();
		scopes.addAll(registryScopeMap.values());	
		Collections.sort(scopes, Item.nameComparator());				

		actionNames = new String[1 + actions.size()];
		actionNames[0] = ACTION_UNDEFINED;
		
		for (int i = 0; i < actions.size(); i++)
			actionNames[i + 1] = ((Item) actions.get(i)).getName();

		configurationNames = new String[configurations.size()];
		
		for (int i = 0; i < configurations.size(); i++)
			configurationNames[i] = ((Item) configurations.get(i)).getName();

		scopeNames = new String[scopes.size()];
		
		for (int i = 0; i < scopes.size(); i++)
			scopeNames[i] = ((Item) scopes.get(i)).getName();
		
		/*
		tree = new TreeMap();
		SortedSet bindingSet = new TreeSet();
		bindingSet.addAll(preferenceBindingSet);
		bindingSet.addAll(registryBindingSet);
		iterator = bindingSet.iterator();
		
		while (iterator.hasNext()) {
			Binding binding = (Binding) iterator.next();				
			//set(tree, binding, false);			
		}
		*/

		/*
		nameToKeySequenceMap = new HashMap();	
		Collection keySequences = tree.keySet();
		iterator = keySequences.iterator();

		while (iterator.hasNext()) {
			KeySequence keySequence = (KeySequence) iterator.next();
			String name = keyManager.getTextForKeySequence(keySequence);
			
			if (!nameToKeySequenceMap.containsKey(name))
				nameToKeySequenceMap.put(name, keySequence);
		}
		*/

		listCommands.setItems(actionNames);

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
