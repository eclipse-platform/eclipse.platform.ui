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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.actions.Action;
import org.eclipse.ui.internal.actions.Util;

final class DialogCustomize extends Dialog {

	private final static int BUTTON_SET_ID = IDialogConstants.CLIENT_ID + 0;
	private final static int DOUBLE_SPACE = 16;
	private final static int HALF_SPACE = 4;
	private final static int SPACE = 8;	
	private final static String ACTION_DO_NOTHING = "(do nothing)";
	private final static String ACTION_UNDEFINED = "(undefined)";

	private final static ColumnLayoutData columnLayouts[] = {
		new ColumnPixelData(20, false),
		new ColumnPixelData(100, false),
		new ColumnPixelData(100, false),
		new ColumnPixelData(250, false) 
	};	

	private final static String columnHeaders[] = {
		"",
		"Scope",
		"Configuration",
		"Action"
	};

	private final class Record {

		String scope;
		String configuration;

		boolean preferenceAction;
		//boolean preferenceActionConflict;
		String preferenceActionString;

		boolean registryAction;
		//boolean registryActionConflict;
		String registryActionString;
	}

	private final class ViewContentProvider implements IStructuredContentProvider {
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
		public void dispose() {
		}
		
		public Object[] getElements(Object parent) {
			List records = new ArrayList();
			
			if (tree != null) {
				Iterator iterator = tree.entrySet().iterator();
				
				while (iterator.hasNext()) {
					Map.Entry entry = (Map.Entry) iterator.next();
					KeySequence keySequence = (KeySequence) entry.getKey();
					
					if (!keySequence.equals(DialogCustomize.this.keySequence))
						continue;
					
					Map scopeMap = (Map) entry.getValue();
					Iterator iterator2 = scopeMap.entrySet().iterator();
					
					while (iterator2.hasNext()) {
						Map.Entry entry2 = (Map.Entry) iterator2.next();
						String scope = (String) entry2.getKey();					
						Map configurationMap = (Map) entry2.getValue();						
						Iterator iterator3 = configurationMap.entrySet().iterator();
						
						while (iterator3.hasNext()) {
							Map.Entry entry3 = (Map.Entry) iterator3.next();
							String configuration = (String) entry3.getKey();					
							Map pluginMap = (Map) entry3.getValue();			
							Record record = new Record();
							record.scope = scope;
							record.configuration = configuration;
							solve(pluginMap, record);							
							Scope scope2 = (Scope) registryScopeMap.get(record.scope);

							if (scope2 != null)
								record.scope = scope2.getName();

							Configuration configuration2 = (Configuration) registryConfigurationMap.get(record.configuration);

							if (configuration2 != null)
								record.configuration = configuration2.getName();

							if (record.preferenceActionString != null) {	
								Action action = (Action) registryActionMap.get(record.preferenceActionString);
	
								if (action != null)
									record.preferenceActionString = action.getName();
							}

							if (record.registryActionString != null) {							
								Action action = (Action) registryActionMap.get(record.registryActionString);
	
								if (action != null)
									record.registryActionString = action.getName();
							}
							
							records.add(record);
						}
					}		
				}
			}

			return records.toArray();
		}

		private void solve(Map pluginMap, Record record) {		
			Set preferenceBindingSet = new TreeSet();
			Set registryBindingSet = new TreeSet();
			Iterator iterator = pluginMap.entrySet().iterator();
			
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String plugin = (String) entry.getKey();
				Map actionMap = (Map) entry.getValue();				
			
				if (plugin == null) {
					record.preferenceAction = true;
					Binding binding = Node.solveActionMap(actionMap);
					
					if (binding != null)	
						preferenceBindingSet.add(binding);
				} else {
					record.registryAction = true;					
					Binding binding = Node.solveActionMap(actionMap);
					
					if (binding != null)	
						registryBindingSet.add(binding);
				}
			}
			
			Binding preferenceBinding = preferenceBindingSet.size() == 1 ? (Binding) preferenceBindingSet.iterator().next() : null;				
			record.preferenceActionString = preferenceBinding != null ? preferenceBinding.getAction() : null;						
			Binding registryBinding = registryBindingSet.size() == 1 ? (Binding) registryBindingSet.iterator().next() : null;	
			record.registryActionString = registryBinding != null ? registryBinding.getAction() : null;					
		}
	}
	
	private final class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		
		public String getColumnText(Object obj, int index) {
			if (obj instanceof Record) {
				Record record = (Record) obj;			
				
				switch (index) {
					case 0:	
						return "";
					case 1:
						return record.scope != null && record.scope.length() != 0 ? record.scope : "-";
					case 2:
						return record.configuration != null && record.configuration.length() != 0 ? record.configuration : "-";
					case 3:
						if (record.preferenceAction) {
							if (record.preferenceActionString == null)
								return ACTION_UNDEFINED;
							else if (record.preferenceActionString.length() == 0)
								return ACTION_DO_NOTHING;
							else
								return record.preferenceActionString;							
						} else if (record.registryAction) {
							if (record.registryActionString == null)
								return ACTION_UNDEFINED;
							else if (record.registryActionString.length() == 0)
								return ACTION_DO_NOTHING;
							else
								return record.registryActionString;							
						} else
							return "?";	
				}					
			}

			return getText(obj);
		}
		
		public Image getColumnImage(Object obj, int index) {
			if (obj instanceof Record) {
				Record record = (Record) obj;			
				
				switch (index) {
					case 0:	
						if (record.preferenceAction && !record.registryAction) {
							return ImageFactory.getImage("plus");	
						} else if (record.preferenceAction && record.registryAction) {
							return ImageFactory.getImage("change");								
						} else
							return null;
					case 1:
						return null;
					case 2:
						return null;
					case 3:
						/*
						if (record.preferenceAction && record.preferenceActionConflict) {
							return ImageFactory.getImage("attention");	
						} else if (record.registryAction && record.registryActionConflict) {
							return ImageFactory.getImage("attention");								
						} else
						*/
							return null;
				}					
			}

			return getImage(obj);
		}
	}
	
	private final class NameSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			Record left = (Record) e1;
			Record right = (Record) e2;			
			int compare = Util.compare(left.scope, right.scope);
			
			if (compare == 0) {
				compare = Util.compare(left.configuration, right.configuration);
				
				if (compare == 0) {
					compare = Util.compare(left.preferenceActionString, right.preferenceActionString);

					if (compare == 0)
						compare = Util.compare(left.registryActionString, right.registryActionString);
				}
			}
			
			return compare;
		}
	}

	private String defaultConfiguration;
	private String defaultScope;
	private SortedSet preferenceBindingSet;
	
	private KeyManager keyManager;
	private KeyMachine keyMachine;

	private SortedMap registryActionMap;
	private SortedSet registryBindingSet;
	private SortedMap registryConfigurationMap;
	private SortedMap registryScopeMap;

	private SortedMap tree;
	private KeySequence keySequence;

	private HashMap nameToActionMap;
	private HashMap nameToKeySequenceMap;
	private HashMap nameToConfigurationMap;
	private HashMap nameToScopeMap;

	private Image imageError;
	private Image imageInfo;
	private Image imageWarning;	
	private Label labelKeySequence;
	private Combo comboKeySequence;
	private Label labelPromptImage;
	private Label labelPromptText;
	private Button buttonDefault;
	private Text textDefault;
	private Button buttonCustom; 
	private Combo comboCustom;
	private Label labelScope; 
	private Combo comboScope;
	private Label labelConfiguration; 
	private Combo comboConfiguration;
	private Button buttonSet;
	private Table table;
	private TableViewer viewer;
	private Button buttonOptions;	

	public DialogCustomize(Shell parentShell, String defaultConfiguration, String defaultScope, SortedSet preferenceBindingSet)
		throws IllegalArgumentException {
		super(parentShell);
		if (defaultConfiguration == null || defaultScope == null || preferenceBindingSet == null)
			throw new IllegalArgumentException();
			
		this.defaultConfiguration = defaultConfiguration;
		this.defaultScope = defaultScope;
		preferenceBindingSet = new TreeSet(preferenceBindingSet);
		Iterator iterator = preferenceBindingSet.iterator();
		
		while (iterator.hasNext())
			if (!(iterator.next() instanceof Binding))
				throw new IllegalArgumentException();
	
		this.preferenceBindingSet = preferenceBindingSet;
		keyManager = KeyManager.getInstance();
		keyMachine = keyManager.getKeyMachine();

		registryActionMap = org.eclipse.ui.internal.actions.Registry.getInstance().getActionMap();
		registryBindingSet = keyManager.getRegistryBindingSet();
		registryConfigurationMap = keyManager.getRegistryConfigurationMap();
		registryScopeMap = keyManager.getRegistryScopeMap();	

		tree = new TreeMap();
		SortedSet bindingSet = new TreeSet();
		bindingSet.addAll(preferenceBindingSet);
		bindingSet.addAll(registryBindingSet);
		iterator = bindingSet.iterator();
		
		while (iterator.hasNext()) {
			Binding binding = (Binding) iterator.next();				
			set(tree, binding, false);			
		}

		nameToActionMap = new HashMap();	
		Collection actions = registryActionMap.values();
		iterator = actions.iterator();

		while (iterator.hasNext()) {
			Action action = (Action) iterator.next();
			String name = action.getName();
			
			if (!nameToActionMap.containsKey(name))
				nameToActionMap.put(name, action);
		}	

		nameToConfigurationMap = new HashMap();	
		Collection configurations = registryConfigurationMap.values();
		iterator = configurations.iterator();

		while (iterator.hasNext()) {
			Configuration configuration = (Configuration) iterator.next();
			String name = configuration.getName();
			
			if (!nameToConfigurationMap.containsKey(name))
				nameToConfigurationMap.put(name, configuration);
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

		nameToScopeMap = new HashMap();	
		Collection scopes = registryScopeMap.values();
		iterator = scopes.iterator();

		while (iterator.hasNext()) {
			Scope scope = (Scope) iterator.next();
			String name = scope.getName();
			
			if (!nameToScopeMap.containsKey(name))
				nameToScopeMap.put(name, scope);
		}	
	}

	public SortedSet getPreferenceBindingSet() {
		return Collections.unmodifiableSortedSet(preferenceBindingSet);	
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == BUTTON_SET_ID) {
			if (keySequence != null) {
				Scope scope = 
					(Scope) nameToScopeMap.get(comboScope.getItem(comboScope.getSelectionIndex()));
				String scopeName = scope.getId();
				Configuration configuration = 
					(Configuration) nameToConfigurationMap.get(comboConfiguration.getItem(comboConfiguration.getSelectionIndex()));
				String configurationName = configuration.getId();

				if (buttonDefault.getSelection()) {
					clear(tree, keySequence, scopeName, configurationName);						
					viewer.refresh();
					update();
				} else if (buttonCustom.getSelection()) {
					int i = comboCustom.getSelectionIndex();
					String actionName = i != -1 ? comboCustom.getItem(i) : null;					
					
					if (ACTION_UNDEFINED.equals(actionName))
						actionName = null;
					else if (ACTION_DO_NOTHING.equals(actionName))
						actionName = "";
					else {
						Action action = (Action) nameToActionMap.get(actionName);
						
						if (action == null)
							actionName = null;
						else
							actionName = action.getId(); 	
					} 
									
					set(tree, Binding.create(actionName, configurationName, keySequence, null, 0, scopeName), true);				
					viewer.refresh();
					update();
				}
			}
		}
		else
			super.buttonPressed(buttonId);
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.getString("DialogCustomize.Title"));
	}

	protected Control createDialogArea(Composite parent) {
		imageError = getImage(DLG_IMG_MESSAGE_ERROR);
		imageInfo = getImage(DLG_IMG_MESSAGE_INFO);
		imageWarning = getImage(DLG_IMG_MESSAGE_WARNING);
		
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout gridLayoutComposite = new GridLayout();
		gridLayoutComposite.marginHeight = SPACE;
		composite.setLayout(gridLayoutComposite);

		Composite compositeKeySequence = new Composite(composite, SWT.NONE);		
		GridLayout gridLayoutCompositeKeySequence = new GridLayout();
		gridLayoutCompositeKeySequence.numColumns = 3;
		gridLayoutCompositeKeySequence.marginWidth = SPACE;	
		gridLayoutCompositeKeySequence.horizontalSpacing = SPACE;
		compositeKeySequence.setLayout(gridLayoutCompositeKeySequence);
		
		labelKeySequence = new Label(compositeKeySequence, SWT.LEFT);
		labelKeySequence.setFont(parent.getFont());
		labelKeySequence.setText(Messages.getString("&Key Sequence:"));

		comboKeySequence = new Combo(compositeKeySequence, /*SWT.NONE*/ SWT.READ_ONLY);
		GridData gridDataComboKeySequence = new GridData();
		gridDataComboKeySequence.widthHint = 200;
		comboKeySequence.setLayoutData(gridDataComboKeySequence);		
		comboKeySequence.setFont(parent.getFont());
		comboKeySequence.setItems(getKeySequences());

		comboKeySequence.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				buttonSet.setEnabled(true);
				String name = comboKeySequence.getItem(comboKeySequence.getSelectionIndex());
				keySequence = (KeySequence) nameToKeySequenceMap.get(name);
				viewer.refresh();
				DialogCustomize.this.update();
			}	
		});

		Composite compositePrompt = new Composite(compositeKeySequence, SWT.NONE);		
		GridLayout gridLayoutCompositePrompt = new GridLayout();
		gridLayoutCompositePrompt.numColumns = 2;
		gridLayoutCompositePrompt.marginWidth = 0;
		gridLayoutCompositePrompt.marginHeight = 0;
		gridLayoutCompositePrompt.horizontalSpacing = HALF_SPACE;
		gridLayoutCompositePrompt.verticalSpacing = HALF_SPACE;
		compositePrompt.setLayout(gridLayoutCompositePrompt);
		
		//compositePrompt.setBackground(new Color(parent.getDisplay(), new RGB(230, 226, 221)));

		labelPromptImage = new Label(compositePrompt, SWT.LEFT);	
		labelPromptImage.setFont(parent.getFont());
		//labelPromptImage.setImage(imageInfo);
		//labelPromptImage.setImage(imageError);

		labelPromptText = new Label(compositePrompt, SWT.LEFT);		
		labelPromptText.setFont(parent.getFont());
		//labelPromptText.setText("Select or Type a Key Sequence");
		//labelPromptText.setText("Invalid Key Sequence");

		Composite compositeStateAndAction = new Composite(composite, SWT.NONE);
		GridLayout gridLayoutCompositeStateAndAction = new GridLayout();
		gridLayoutCompositeStateAndAction.numColumns = 2;
		//gridLayoutCompositeStateAndAction.makeColumnsEqualWidth = true;
		gridLayoutCompositeStateAndAction.horizontalSpacing = SPACE;
		compositeStateAndAction.setLayout(gridLayoutCompositeStateAndAction);

		Group groupState = new Group(compositeStateAndAction, SWT.NONE);	
		groupState.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout gridLayoutGroupState = new GridLayout();
		gridLayoutGroupState.numColumns = 2;
		gridLayoutGroupState.marginWidth = SPACE;
		gridLayoutGroupState.marginHeight = SPACE;
		gridLayoutGroupState.horizontalSpacing = SPACE;
		gridLayoutGroupState.verticalSpacing = SPACE;
		groupState.setLayout(gridLayoutGroupState);
		groupState.setFont(parent.getFont());
		groupState.setText(Messages.getString("When"));	

		labelScope = new Label(groupState, SWT.LEFT);
		labelScope.setFont(parent.getFont());
		labelScope.setText(Messages.getString("Sco&pe:"));

		comboScope = new Combo(groupState, SWT.READ_ONLY);
		GridData gridDataComboScope = new GridData();
		gridDataComboScope.widthHint = 100;
		comboScope.setLayoutData(gridDataComboScope);
		comboScope.setFont(parent.getFont());
		comboScope.setItems(getScopes());

		Scope scope = (Scope) registryScopeMap.get(defaultScope);

		if (scope != null)
			comboScope.select(comboScope.indexOf(scope.getName()));

		comboScope.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DialogCustomize.this.update();
			}	
		});

		labelConfiguration = new Label(groupState, SWT.LEFT);
		labelConfiguration.setFont(parent.getFont());
		labelConfiguration.setText(Messages.getString("Co&nfiguration:"));

		comboConfiguration = new Combo(groupState, SWT.READ_ONLY);
		GridData gridDataComboConfiguration = new GridData(GridData.FILL_HORIZONTAL);
		gridDataComboConfiguration.widthHint = 100;
		comboConfiguration.setLayoutData(gridDataComboConfiguration);
		comboConfiguration.setFont(parent.getFont());
		comboConfiguration.setItems(getConfigurations());

		Configuration configuration = (Configuration) registryConfigurationMap.get(defaultConfiguration);

		if (configuration != null)
			comboConfiguration.select(comboConfiguration.indexOf(configuration.getName()));

		comboConfiguration.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DialogCustomize.this.update();
			}	
		});

		Group groupAction = new Group(compositeStateAndAction, SWT.NONE);	
		groupAction.setLayoutData(new GridData(GridData.FILL_BOTH));		
		GridLayout gridLayoutGroupAction = new GridLayout();
		gridLayoutGroupAction.numColumns = 2;
		gridLayoutGroupAction.marginWidth = SPACE;
		gridLayoutGroupAction.marginHeight = SPACE;
		gridLayoutGroupAction.horizontalSpacing = SPACE;
		gridLayoutGroupAction.verticalSpacing = SPACE;
		groupAction.setLayout(gridLayoutGroupAction);
		groupAction.setFont(parent.getFont());
		groupAction.setText(Messages.getString("Action"));			

		buttonDefault = new Button(groupAction, SWT.LEFT | SWT.RADIO);
		buttonDefault.setFont(parent.getFont());
		buttonDefault.setText(Messages.getString("&Default:"));
		buttonDefault.setSelection(true);

		textDefault = new Text(groupAction, SWT.BORDER | SWT.READ_ONLY);
		GridData gridDataTextDefault = new GridData(GridData.FILL_HORIZONTAL);
		gridDataTextDefault.widthHint = 150;
		textDefault.setLayoutData(gridDataTextDefault);
		textDefault.setFont(parent.getFont());

		buttonCustom = new Button(groupAction, SWT.LEFT | SWT.RADIO);
		buttonCustom.setFont(parent.getFont());
		buttonCustom.setText(Messages.getString("&Custom:"));

		buttonDefault.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				buttonCustom.setSelection(false);;
			}	
		});

		buttonCustom.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				buttonDefault.setSelection(false);;
			}	
		});

		comboCustom = new Combo(groupAction, SWT.READ_ONLY);
		GridData gridDataComboCustom = new GridData(GridData.FILL_HORIZONTAL);
		gridDataComboCustom.widthHint = 150;
		comboCustom.setLayoutData(gridDataComboCustom);
		comboCustom.setFont(parent.getFont());
		comboCustom.setItems(getActions());
		comboCustom.select(1);

		comboCustom.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				buttonDefault.setSelection(false);
				buttonCustom.setSelection(true);
			}	
		});

		Composite compositeSet = new Composite(composite, SWT.NONE);		
		GridLayout gridLayoutCompositeSet = new GridLayout();
		gridLayoutCompositeSet.marginWidth = DOUBLE_SPACE;
		compositeSet.setLayout(gridLayoutCompositeSet);

		buttonSet = createButton(compositeSet, BUTTON_SET_ID, "&Set", false); 
		buttonSet.setEnabled(false);

		Composite compositeTable = new Composite(composite, SWT.NONE);		
		compositeTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout gridLayoutCompositeTable = new GridLayout();
		compositeTable.setLayout(gridLayoutCompositeTable);

		table = new Table(compositeTable, SWT.BORDER | SWT.HIDE_SELECTION/*SWT.FULL_SELECTION*/ | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gridDataTable = new GridData(GridData.FILL_BOTH);
		gridDataTable.heightHint = 100;		
		table.setLayoutData(gridDataTable);

		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		table.setHeaderVisible(true);
		
		for (int i = 0; i < columnHeaders.length; i++) {
			layout.addColumnData(columnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE, i);
			tc.setResizable(columnLayouts[i].resizable);
			tc.setText(columnHeaders[i]);
			//tc.addSelectionListener(headerListener);
		}

		viewer = new TableViewer(table);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(new Object());
		update();		
		
		//TableItem[] tableItems = table.getItems();
		//Color background = tableItems[0].getBackground();
		//tableItems[0].setBackground(new Color(parent.getDisplay(), new RGB(background.getRed() - 15, background.getGreen(), background.getBlue() - 15)));

		return composite;		
	}	

	protected void okPressed() {
		preferenceBindingSet = solve(tree);
		super.okPressed();
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

	private String[] getActions() {
		SortedSet actionSet = new TreeSet(nameToActionMap.keySet());
		actionSet.add(ACTION_UNDEFINED);
		actionSet.add(ACTION_DO_NOTHING);
		String[] items = (String[]) actionSet.toArray(new String[actionSet.size()]);
		Arrays.sort(items, Collator.getInstance());
		return items;
	}

	private String[] getConfigurations() {
		String[] items = (String[]) nameToConfigurationMap.keySet().toArray(new String[nameToConfigurationMap.size()]);
		Arrays.sort(items, Collator.getInstance());
		return items;
	}

	private String[] getKeySequences() {
		String[] items = (String[]) nameToKeySequenceMap.keySet().toArray(new String[nameToKeySequenceMap.size()]);
		Arrays.sort(items, Collator.getInstance());
		return items;
	}

	private String[] getScopes() {
		String[] items = (String[]) nameToScopeMap.keySet().toArray(new String[nameToScopeMap.size()]);
		Arrays.sort(items, Collator.getInstance());
		return items;
	}	
	
	private void update() {
		buttonDefault.setSelection(true);
		textDefault.setText(ACTION_UNDEFINED);
		buttonCustom.setSelection(false);	
		comboCustom.select(1);	
		Scope scope = 
			(Scope) nameToScopeMap.get(comboScope.getItem(comboScope.getSelectionIndex()));
		String scopeName = scope.getId();
		// TBD ^?
		
		scopeName = comboScope.getItem(comboScope.getSelectionIndex());
		
		Configuration configuration = 
			(Configuration) nameToConfigurationMap.get(comboConfiguration.getItem(comboConfiguration.getSelectionIndex()));
		String configurationName = configuration.getId();
		// TBD ^?
		
		configurationName = comboConfiguration.getItem(comboConfiguration.getSelectionIndex());

		Object[] elements = ((IStructuredContentProvider) viewer.getContentProvider()).getElements(new Object());
		
		for (int i = 0; i < elements.length; i++) {
			Record record = (Record) elements[i];
			
			if (Util.equals(scopeName, record.scope) && Util.equals(configurationName, record.configuration)) {
				if (record.preferenceAction) {
					buttonDefault.setSelection(false);
					buttonCustom.setSelection(true);
					
					if (record.preferenceActionString == null)					
						comboCustom.select(1);
					else if (record.preferenceActionString.equals(""))
						comboCustom.select(0);
					else
						comboCustom.select(comboCustom.indexOf(record.preferenceActionString));										
				} else if (record.registryAction) {	
					textDefault.setText(record.registryActionString);
				}
				
				return;				
			}
		}
	}
}
