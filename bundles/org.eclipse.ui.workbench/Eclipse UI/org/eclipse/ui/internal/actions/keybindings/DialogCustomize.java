/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.actions.keybindings;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.actions.Action;
import org.eclipse.ui.internal.actions.Util;

final class DialogCustomize extends Dialog {

	private final static int DOUBLE_SPACE = 16;
	private final static int HALF_SPACE = 4;
	private final static int SPACE = 8;	
	private final static String ACTION_CONFLICT = Messages.getString("DialogCustomize.ActionConflict"); //$NON-NLS-1$
	private final static String ACTION_UNDEFINED = Messages.getString("DialogCustomize.ActionUndefined"); //$NON-NLS-1$
	private final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

	private final class Record {

		String scopeId;
		String configurationId;
		Map pluginMap;
	}

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

	private Button buttonCustom; 
	private Button buttonDefault;
	private Combo comboConfiguration;
	private Combo comboCustom;
	private Combo comboKeySequence;
	private Combo comboScope;
	private Label labelConfiguration; 
	private Label labelKeySequence;
	private Label labelPromptImage;
	private Label labelPromptText;
	private Label labelScope; 
	private Table table;
	private Text textDefault;

	private SortedMap tree;
	private Map nameToKeySequenceMap;
	private List records = new ArrayList();

	public DialogCustomize(Shell parentShell, String defaultConfigurationId, String defaultScopeId, SortedSet preferenceBindingSet)
		throws IllegalArgumentException {
		super(parentShell);
		if (defaultConfigurationId == null || defaultScopeId == null || preferenceBindingSet == null)
			throw new IllegalArgumentException();
			
		this.defaultConfigurationId = defaultConfigurationId;
		this.defaultScopeId = defaultScopeId;
		preferenceBindingSet = new TreeSet(preferenceBindingSet);
		Iterator iterator = preferenceBindingSet.iterator();
		
		while (iterator.hasNext())
			if (!(iterator.next() instanceof Binding))
				throw new IllegalArgumentException();
	
		this.preferenceBindingSet = preferenceBindingSet;

		keyManager = KeyManager.getInstance();
		keyMachine = keyManager.getKeyMachine();

		registryActionMap = org.eclipse.ui.internal.actions.Registry.getInstance().getActionMap();
		actions = new ArrayList();
		actions.addAll(registryActionMap.values());
		Collections.sort(actions, Action.nameComparator());				
	
		registryBindingSet = keyManager.getRegistryBindingSet();
		
		registryConfigurationMap = keyManager.getRegistryConfigurationMap();
		configurations = new ArrayList();
		configurations.addAll(registryConfigurationMap.values());	
		Collections.sort(configurations, Configuration.nameComparator());				
		
		registryScopeMap = keyManager.getRegistryScopeMap();	
		scopes = new ArrayList();
		scopes.addAll(registryScopeMap.values());	
		Collections.sort(scopes, Scope.nameComparator());				

		actionNames = new String[1 + actions.size()];
		actionNames[0] = ACTION_UNDEFINED;
		
		for (int i = 0; i < actions.size(); i++)
			actionNames[i + 1] = ((Action) actions.get(i)).getLabel().getName();

		configurationNames = new String[configurations.size()];
		
		for (int i = 0; i < configurations.size(); i++)
			configurationNames[i] = ((Configuration) configurations.get(i)).getLabel().getName();

		scopeNames = new String[scopes.size()];
		
		for (int i = 0; i < scopes.size(); i++)
			scopeNames[i] = ((Scope) scopes.get(i)).getLabel().getName();
		
		tree = new TreeMap();
		SortedSet bindingSet = new TreeSet();
		bindingSet.addAll(preferenceBindingSet);
		bindingSet.addAll(registryBindingSet);
		iterator = bindingSet.iterator();
		
		while (iterator.hasNext()) {
			Binding binding = (Binding) iterator.next();				
			set(tree, binding, false);			
		}

		nameToKeySequenceMap = new HashMap();	
		Collection keySequences = tree.keySet();
		iterator = keySequences.iterator();

		while (iterator.hasNext()) {
			KeySequence keySequence = (KeySequence) iterator.next();
			String name = keyManager.getTextForKeySequence(keySequence);
			
			if (!nameToKeySequenceMap.containsKey(name))
				nameToKeySequenceMap.put(name, keySequence);
		}
	}

	public SortedSet getPreferenceBindingSet() {
		return Collections.unmodifiableSortedSet(preferenceBindingSet);	
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.getString("DialogCustomize.Title"));
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		createUI(composite);
		return composite;		
	}	

	protected void okPressed() {
		preferenceBindingSet = solve(tree);
		super.okPressed();
	}

	private void clear(SortedMap tree, KeySequence keySequence, String scope, String configuration) {			
		Map scopeMap = (Map) tree.get(keySequence);
		
		if (scopeMap != null) {
			Map configurationMap = (Map) scopeMap.get(scope);
		
			if (configurationMap != null) {
				Map pluginMap = (Map) configurationMap.get(configuration);
	
				if (pluginMap != null) {
					pluginMap.remove(null);
					
					if (pluginMap.isEmpty()) {
						configurationMap.remove(configuration);
						
						if (configurationMap.isEmpty()) {
							scopeMap.remove(scope);	

							if (scopeMap.isEmpty()) {
								tree.remove(keySequence);	
							}							
						}	
					}	
				}	
			}
		}
	}

	private void set(SortedMap tree, Binding binding, boolean consolidate) {			
		Map scopeMap = (Map) tree.get(binding.getKeySequence());
		
		if (scopeMap == null) {
			scopeMap = new TreeMap();	
			tree.put(binding.getKeySequence(), scopeMap);
		}

		Map configurationMap = (Map) scopeMap.get(binding.getScope());
		
		if (configurationMap == null) {
			configurationMap = new TreeMap();	
			scopeMap.put(binding.getScope(), configurationMap);
		}
		
		Map pluginMap = (Map) configurationMap.get(binding.getConfiguration());
		
		if (pluginMap == null) {
			pluginMap = new HashMap();	
			configurationMap.put(binding.getConfiguration(), pluginMap);
		}

		Map actionMap = consolidate ? null : (Map) pluginMap.get(binding.getPlugin());
		
		if (actionMap == null) {
			actionMap = new HashMap();	
			pluginMap.put(binding.getPlugin(), actionMap);
		}

		Set bindingSet = (Set) actionMap.get(binding.getAction());
		
		if (bindingSet == null) {
			bindingSet = new TreeSet();
			actionMap.put(binding.getAction(), bindingSet);	
		}

		if (consolidate)
			bindingSet.clear();
		
		bindingSet.add(binding);
	}

	private SortedSet solve(SortedMap tree) {
		SortedSet bindingSet = new TreeSet();
		Iterator iterator = tree.values().iterator();
		
		while (iterator.hasNext()) {
			Map scopeMap = (Map) iterator.next();
			Iterator iterator2 = scopeMap.values().iterator();
			
			while (iterator2.hasNext()) {
				Map configurationMap = (Map) iterator2.next();
				Iterator iterator3 = configurationMap.values().iterator();
				
				while (iterator3.hasNext()) {
					Map pluginMap = (Map) iterator3.next();
					Map actionMap = (Map) pluginMap.get(null);
					
					if (actionMap != null) {
						Iterator iterator4 = actionMap.values().iterator();
						
						while (iterator4.hasNext())
							bindingSet.addAll((Set) iterator4.next());
					}
				}
			}		
		}
		
		return bindingSet;
	}


	private String getActionId() {
		int selection = comboCustom.getSelectionIndex();
		
		if (selection == 0)
			return null;
		else {
			selection--;
			
			if (selection >= 0 && selection < actions.size()) {
				Action action = (Action) actions.get(selection);
				return action.getLabel().getId();
			}				
		}
	
		return null;
	}

	private String getConfigurationId() {
		int selection = comboConfiguration.getSelectionIndex();
		
		if (selection >= 0 && selection < configurations.size()) {
			Configuration configuration = (Configuration) configurations.get(selection);
			return configuration.getLabel().getId();				
		}
		
		return null;
	}

	private String[] getKeySequences() {
		String[] items = (String[]) nameToKeySequenceMap.keySet().toArray(new String[nameToKeySequenceMap.size()]);
		Arrays.sort(items, Collator.getInstance());
		return items;
	}

	private String getScopeId() {
		int selection = comboScope.getSelectionIndex();
		
		if (selection >= 0 && selection < scopes.size()) {
			Scope scope = (Scope) scopes.get(selection);
			return scope.getLabel().getId();				
		}
		
		return null;
	}

	private void change(boolean custom) {
		KeySequence keySequence = null;
		String name = comboKeySequence.getText();
		
		if (name != null || name.length() > 0) {
			keySequence = (KeySequence) nameToKeySequenceMap.get(name);
			
			if (keySequence == null)
				keySequence = KeyManager.parseKeySequenceStrict(name);
		}				

		if (keySequence != null) {
			String scopeId = getScopeId();
			String configurationId = getConfigurationId();

			if (!custom)
				clear(tree, keySequence, scopeId, configurationId);						
			else { 
				set(tree, Binding.create(getActionId(), configurationId, keySequence, null, 0, scopeId), true);				
				/*
				name = keyManager.getTextForKeySequence(keySequence);			
				
				if (!nameToKeySequenceMap.containsKey(name))
					nameToKeySequenceMap.put(name, keySequence);
	
				comboKeySequence.setItems(getKeySequences());
				*/					
			}
		}
				
		update();
	}

	private void modifiedComboKeySequence() {
		update();
	}

	private void selectedButtonCustom() {
		change(true);
	}

	private void selectedButtonDefault() {
		change(false);
	}

	private void selectedComboConfiguration() {
		update();				
	}

	private void selectedComboCustom() {
		change(true);
	}

	private void selectedComboKeySequence() {			
		update();
	}
	
	private void selectedComboScope() {
		update();	
	}

	private void selectedTable() {
		update();	
	}

	private void setPrompt(Image image, String string)
		throws IllegalArgumentException {
		labelPromptImage.setImage(image);
		labelPromptText.setText(string);	
	}
	
	private void update() {
		KeySequence keySequence = null;
		String name = comboKeySequence.getText();
		
		if (name == null || name.length() == 0)
			setPrompt(getImage(DLG_IMG_MESSAGE_INFO), "Select or Type a Key Sequence");	
		else {		
			setPrompt(null, ZERO_LENGTH_STRING);
			keySequence = (KeySequence) nameToKeySequenceMap.get(name);
			
			if (keySequence == null) {
				// TBD: review! still not strict enough! convertAccelerator says 'Ctrl+Ax' is valid!				
				keySequence = KeyManager.parseKeySequenceStrict(name);
				
				if (keySequence == null)
					setPrompt(getImage(DLG_IMG_MESSAGE_ERROR), "Invalid Key Sequence");
			}
		}		

		String scopeId = getScopeId();
		String configurationId = getConfigurationId();

		boolean setPluginMap = false;
		records.clear();
	
		if (tree != null && keySequence != null) {
			Map scopeMap = (Map) tree.get(keySequence);
			
			if (scopeMap != null) {
				Iterator iterator = scopeMap.entrySet().iterator();
			
				while (iterator.hasNext()) {
					Map.Entry entry = (Map.Entry) iterator.next();
					String scopeId2 = (String) entry.getKey();					
					Map configurationMap = (Map) entry.getValue();						
					Iterator iterator2 = configurationMap.entrySet().iterator();
							
					while (iterator2.hasNext()) {
						Map.Entry entry2 = (Map.Entry) iterator2.next();
						String configurationId2 = (String) entry2.getKey();					
						Map pluginMap = (Map) entry2.getValue();			
						Record record = new Record();
						record.scopeId = scopeId2;
						record.configurationId = configurationId2;
						record.pluginMap = pluginMap;
						
						if (Util.equals(scopeId, record.scopeId) && Util.equals(configurationId, record.configurationId)) {							
							setPluginMap(record.pluginMap);
							setPluginMap = true;	
						}
						
						records.add(record);
					}												
				}	
			}
		}

		if (!setPluginMap)
			setPluginMap(Collections.EMPTY_MAP);

		table.removeAll();
		int select = -1;

		for (int i = 0; i < records.size(); i++) {
			Record record = (Record) records.get(i);

			if (Util.equals(scopeId, record.scopeId) && Util.equals(configurationId, record.configurationId))
				select = i;				

			Scope scope = (Scope) registryScopeMap.get(record.scopeId);
			Configuration configuration = (Configuration) registryConfigurationMap.get(record.configurationId);			
			TableItem tableItem = new TableItem(table, SWT.NULL);					
			tableItem.setText(1, scope != null ? scope.getLabel().getName() : "(" + record.scopeId + ")");
			tableItem.setText(2, configuration != null ? configuration.getLabel().getName() : "(" + record.configurationId + ")");
			Map pluginMap = record.pluginMap;

			boolean customConflict = false;
			String customAction = null;
			
			Map actionMap = (Map) pluginMap.get(null);							

			if (actionMap != null) {
				Set bindingSet = (Set) actionMap.values().iterator().next();
				
				if (actionMap.size() > 1 || bindingSet.size() > 1) {
					customConflict = true;
					customAction = ACTION_CONFLICT;
				} else {
					Binding binding = (Binding) bindingSet.iterator().next();	
					String actionId = binding.getAction();
				
					if (actionId == null)
						customAction = ACTION_UNDEFINED;
					else
						for (int j = 0; j < actions.size(); j++) {
							Action action = (Action) actions.get(j);		
							
							if (action.getLabel().getId().equals(actionId)) {
								customAction = action.getLabel().getName();
								break;		
							}
						}		
				}
			}

			boolean defaultConflict = false;
			String defaultAction = null;
			
			pluginMap = new HashMap(pluginMap);
			pluginMap.remove(null);
	
			if (pluginMap.size() > 0) {
				actionMap = (Map) pluginMap.values().iterator().next();	
				Set bindingSet = (Set) actionMap.values().iterator().next();
						
				if (pluginMap.size() > 1 || actionMap.size() > 1 || bindingSet.size() > 1) {
					defaultConflict = true;
					defaultAction = ACTION_CONFLICT;				
				} else {
					Binding binding = (Binding) bindingSet.iterator().next();	
					String actionId = binding.getAction();
				
					if (actionId == null)
						defaultAction = ACTION_UNDEFINED;
					else
						for (int j = 0; j < actions.size(); j++) {
							Action action = (Action) actions.get(j);		
							
							if (action.getLabel().getId().equals(actionId)) {
								defaultAction = action.getLabel().getName();
								break;		
							}
						}		
				}
			}
				
			if (defaultAction == null && customAction != null)
				tableItem.setImage(0, ImageFactory.getImage("plus"));
			else if (defaultAction != null && customAction != null)
				tableItem.setImage(0, ImageFactory.getImage("change"));
				
			if (customAction != null) {				
				tableItem.setText(3, customAction);
			
				if (customConflict)
					tableItem.setImage(3, ImageFactory.getImage("exclamation"));			
			} else if (defaultAction != null) {
				tableItem.setText(3, defaultAction);
			
				if (defaultConflict)
					tableItem.setImage(3, ImageFactory.getImage("exclamation"));			
			} 
		}
		
		if (select >= 0)
			table.select(select);
	}
		
	private void createUI(Composite composite) {
		Font font = composite.getFont();
		GridLayout gridLayoutComposite = new GridLayout();
		gridLayoutComposite.marginHeight = SPACE;
		composite.setLayout(gridLayoutComposite);

		Composite compositeKeySequence = new Composite(composite, SWT.NULL);		
		GridLayout gridLayoutCompositeKeySequence = new GridLayout();
		gridLayoutCompositeKeySequence.numColumns = 3;
		gridLayoutCompositeKeySequence.marginWidth = SPACE;	
		gridLayoutCompositeKeySequence.horizontalSpacing = SPACE;
		compositeKeySequence.setLayout(gridLayoutCompositeKeySequence);
		
		labelKeySequence = new Label(compositeKeySequence, SWT.LEFT);
		labelKeySequence.setFont(font);
		labelKeySequence.setText(Messages.getString("DialogCustomize.LabelKeySequence"));

		comboKeySequence = new Combo(compositeKeySequence, SWT.NULL);
		comboKeySequence.setFont(font);
		GridData gridDataComboKeySequence = new GridData();
		gridDataComboKeySequence.widthHint = 200;
		comboKeySequence.setLayoutData(gridDataComboKeySequence);		

		Composite compositePrompt = new Composite(compositeKeySequence, SWT.NULL);		
		GridLayout gridLayoutCompositePrompt = new GridLayout();
		gridLayoutCompositePrompt.numColumns = 2;
		gridLayoutCompositePrompt.marginWidth = 0;
		gridLayoutCompositePrompt.marginHeight = 0;
		gridLayoutCompositePrompt.horizontalSpacing = HALF_SPACE;
		gridLayoutCompositePrompt.verticalSpacing = HALF_SPACE;
		compositePrompt.setLayout(gridLayoutCompositePrompt);

		labelPromptImage = new Label(compositePrompt, SWT.LEFT);
		labelPromptImage.setFont(font);
		GridData gridDataPromptImage = new GridData();
		gridDataPromptImage.widthHint = 16;
		labelPromptImage.setLayoutData(gridDataPromptImage);
			
		labelPromptText = new Label(compositePrompt, SWT.LEFT);		
		labelPromptText.setFont(font);

		Composite compositeStateAndAction = new Composite(composite, SWT.NULL);
		GridLayout gridLayoutCompositeStateAndAction = new GridLayout();
		gridLayoutCompositeStateAndAction.numColumns = 2;
		gridLayoutCompositeStateAndAction.horizontalSpacing = SPACE;
		compositeStateAndAction.setLayout(gridLayoutCompositeStateAndAction);

		Group groupState = new Group(compositeStateAndAction, SWT.NULL);	
		groupState.setFont(font);
		GridLayout gridLayoutGroupState = new GridLayout();
		gridLayoutGroupState.numColumns = 2;
		gridLayoutGroupState.marginWidth = SPACE;
		gridLayoutGroupState.marginHeight = SPACE;
		gridLayoutGroupState.horizontalSpacing = SPACE;
		gridLayoutGroupState.verticalSpacing = SPACE;
		groupState.setLayout(gridLayoutGroupState);
		groupState.setLayoutData(new GridData(GridData.FILL_BOTH));
		groupState.setText(Messages.getString("DialogCustomize.GroupState"));	

		labelScope = new Label(groupState, SWT.LEFT);
		labelScope.setFont(font);
		labelScope.setText(Messages.getString("DialogCustomize.LabelScope"));

		comboScope = new Combo(groupState, SWT.READ_ONLY);
		comboScope.setFont(font);
		GridData gridDataComboScope = new GridData();
		gridDataComboScope.widthHint = 100;
		comboScope.setLayoutData(gridDataComboScope);

		labelConfiguration = new Label(groupState, SWT.LEFT);
		labelConfiguration.setFont(font);
		labelConfiguration.setText(Messages.getString("DialogCustomize.LabelConfiguration"));

		comboConfiguration = new Combo(groupState, SWT.READ_ONLY);
		comboConfiguration.setFont(font);
		GridData gridDataComboConfiguration = new GridData(GridData.FILL_HORIZONTAL);
		gridDataComboConfiguration.widthHint = 100;
		comboConfiguration.setLayoutData(gridDataComboConfiguration);

		Group groupAction = new Group(compositeStateAndAction, SWT.NULL);	
		groupAction.setFont(font);
		GridLayout gridLayoutGroupAction = new GridLayout();
		gridLayoutGroupAction.numColumns = 2;
		gridLayoutGroupAction.marginWidth = SPACE;
		gridLayoutGroupAction.marginHeight = SPACE;
		gridLayoutGroupAction.horizontalSpacing = SPACE;
		gridLayoutGroupAction.verticalSpacing = SPACE;
		groupAction.setLayout(gridLayoutGroupAction);
		groupAction.setLayoutData(new GridData(GridData.FILL_BOTH));		
		groupAction.setText(Messages.getString("DialogCustomize.GroupAction"));			

		buttonDefault = new Button(groupAction, SWT.LEFT | SWT.RADIO);
		buttonDefault.setFont(font);
		buttonDefault.setText(Messages.getString("DialogCustomize.ButtonDefault"));

		textDefault = new Text(groupAction, SWT.BORDER | SWT.READ_ONLY);
		textDefault.setFont(font);
		GridData gridDataTextDefault = new GridData(GridData.FILL_HORIZONTAL);
		gridDataTextDefault.widthHint = 150;
		textDefault.setLayoutData(gridDataTextDefault);

		buttonCustom = new Button(groupAction, SWT.LEFT | SWT.RADIO);
		buttonCustom.setFont(font);
		buttonCustom.setText(Messages.getString("DialogCustomize.ButtonCustom"));

		comboCustom = new Combo(groupAction, SWT.READ_ONLY);
		comboCustom.setFont(font);
		GridData gridDataComboCustom = new GridData(GridData.FILL_HORIZONTAL);
		gridDataComboCustom.widthHint = 150;
		comboCustom.setLayoutData(gridDataComboCustom);

		Composite compositeTable = new Composite(composite, SWT.NULL);		
		GridLayout gridLayoutCompositeTable = new GridLayout();
		compositeTable.setLayout(gridLayoutCompositeTable);
		compositeTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		table = new Table(compositeTable, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		table.setHeaderVisible(true);
		GridData gridDataTable = new GridData(GridData.FILL_BOTH);
		gridDataTable.heightHint = 100;		
		table.setLayoutData(gridDataTable);

		TableColumn tableColumn = new TableColumn(table, SWT.NULL, 0);
		tableColumn.setResizable(false);
		tableColumn.setText(ZERO_LENGTH_STRING);
		tableColumn.setWidth(20);

		tableColumn = new TableColumn(table, SWT.NULL, 1);
		tableColumn.setResizable(true);
		tableColumn.setText(Messages.getString("DialogCustomize.HeaderScope"));
		tableColumn.setWidth(100);

		tableColumn = new TableColumn(table, SWT.NULL, 2);
		tableColumn.setResizable(true);
		tableColumn.setText(Messages.getString("DialogCustomize.HeaderConfiguration"));
		tableColumn.setWidth(100);

		tableColumn = new TableColumn(table, SWT.NULL, 3);
		tableColumn.setResizable(true);
		tableColumn.setText(Messages.getString("DialogCustomize.HeaderAction"));
		tableColumn.setWidth(250);	

		comboKeySequence.setItems(getKeySequences());			
		comboScope.setItems(scopeNames);
		comboConfiguration.setItems(configurationNames);
		comboCustom.setItems(actionNames);
		
		setConfigurationId(defaultConfigurationId);
		setScopeId(defaultScopeId);

		comboKeySequence.addModifyListener(new ModifyListener() {			
			public void modifyText(ModifyEvent e) {
				modifiedComboKeySequence();
			}	
		});

		comboKeySequence.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectedComboKeySequence();
			}	
		});

		comboScope.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectedComboScope();
			}	
		});

		comboConfiguration.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectedComboConfiguration();
			}	
		});

		buttonDefault.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectedButtonDefault();
			}	
		});

		buttonCustom.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectedButtonCustom();
			}	
		});
		
		comboCustom.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectedComboCustom();
			}	
		});

		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int i = table.getSelectionIndex();
				
				if (i >= 0) {
					Record record = (Record) records.get(i);						
					
					if (record != null) {
						setScopeId(record.scopeId);
						setConfigurationId(record.configurationId);	
					}
				}
				
				selectedTable();
			}	
		});

		update();
	}

	
	private void setConfigurationId(String configurationId) {				
		comboConfiguration.clearSelection();
		comboConfiguration.deselectAll();
		
		if (configurationId != null)	
			for (int i = 0; i < configurations.size(); i++) {
				Configuration configuration = (Configuration) configurations.get(i);		
				
				if (configuration.getLabel().getId().equals(configurationId)) {
					comboConfiguration.select(i);
					break;		
				}
			}
	}	
	
	private void setPluginMap(Map pluginMap) {		
		//textDefault.clearSelection();
		//comboCustom.clearSelection();
		comboCustom.deselectAll();	

		Map actionMap = (Map) pluginMap.get(null);
		buttonDefault.setSelection(actionMap == null);
		buttonCustom.setSelection(!buttonDefault.getSelection());									

		if (actionMap != null) {
			Set bindingSet = (Set) actionMap.values().iterator().next();
			
			if (actionMap.size() > 1 || bindingSet.size() > 1)
				comboCustom.setText(ACTION_CONFLICT);
			else {
				Binding binding = (Binding) bindingSet.iterator().next();	
				String actionId = binding.getAction();
			
				if (actionId == null)
					comboCustom.select(0);
				else
					for (int i = 0; i < actions.size(); i++) {
						Action action = (Action) actions.get(i);		
						
						if (action.getLabel().getId().equals(actionId)) {
							comboCustom.select(i + 1);
							break;		
						}
					}		
			}
		} else
			comboCustom.select(0);

		pluginMap = new HashMap(pluginMap);
		pluginMap.remove(null);

		if (pluginMap.size() > 0) {
			actionMap = (Map) pluginMap.values().iterator().next();	
			Set bindingSet = (Set) actionMap.values().iterator().next();
					
			if (pluginMap.size() > 1 || actionMap.size() > 1 || bindingSet.size() > 1)
				textDefault.setText(ACTION_CONFLICT);
			else {
				Binding binding = (Binding) bindingSet.iterator().next();	
				String actionId = binding.getAction();
			
				if (actionId == null)
					textDefault.setText(ACTION_UNDEFINED);
				else
					for (int i = 0; i < actions.size(); i++) {
						Action action = (Action) actions.get(i);		
						
						if (action.getLabel().getId().equals(actionId)) {
							textDefault.setText(action.getLabel().getName());
							break;		
						}
					}		
			}
		} else
			textDefault.setText(ACTION_UNDEFINED);	
	}

	private void setScopeId(String scopeId) {				
		comboScope.clearSelection();
		comboScope.deselectAll();
		
		if (scopeId != null)	
			for (int i = 0; i < scopes.size(); i++) {
				Scope scope = (Scope) scopes.get(i);		
				
				if (scope.getLabel().getId().equals(scopeId)) {
					comboScope.select(i);
					break;		
				}
			}
	}
}
