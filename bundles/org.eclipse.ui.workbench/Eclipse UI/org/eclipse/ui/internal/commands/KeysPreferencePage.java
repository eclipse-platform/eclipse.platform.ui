/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import java.io.IOException;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.commands.registry.ActiveKeyConfigurationDefinition;
import org.eclipse.ui.internal.commands.registry.CategoryDefinition;
import org.eclipse.ui.internal.commands.registry.CommandDefinition;
import org.eclipse.ui.internal.commands.registry.IActiveKeyConfigurationDefinition;
import org.eclipse.ui.internal.commands.registry.ICategoryDefinition;
import org.eclipse.ui.internal.commands.registry.ICommandDefinition;
import org.eclipse.ui.internal.commands.registry.ICommandRegistry;
import org.eclipse.ui.internal.commands.registry.IKeyBindingDefinition;
import org.eclipse.ui.internal.commands.registry.IKeyConfigurationDefinition;
import org.eclipse.ui.internal.commands.registry.KeyConfigurationDefinition;
import org.eclipse.ui.internal.commands.registry.PreferenceCommandRegistry;
import org.eclipse.ui.internal.contexts.ContextManager;
import org.eclipse.ui.internal.contexts.registry.ContextDefinition;
import org.eclipse.ui.internal.contexts.registry.IContextDefinition;
import org.eclipse.ui.internal.contexts.registry.IContextRegistry;
import org.eclipse.ui.internal.keys.KeySequenceText;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;

public class KeysPreferencePage extends org.eclipse.jface.preference.PreferencePage
	implements IWorkbenchPreferencePage {

	private final static ResourceBundle resourceBundle = ResourceBundle.getBundle(KeysPreferencePage.class.getName());

	private final static String COMMAND_CONFLICT = Util.translateString(resourceBundle, "commandConflict"); //$NON-NLS-1$
	private final static String COMMAND_NOTHING = Util.translateString(resourceBundle, "commandNothing"); //$NON-NLS-1$
	private final static String COMMAND_UNDEFINED = Util.translateString(resourceBundle, "commandUndefined"); //$NON-NLS-1$
	private final static int DIFFERENCE_ADD = 0;	
	private final static int DIFFERENCE_CHANGE = 1;	
	private final static int DIFFERENCE_MINUS = 2;	
	private final static int DIFFERENCE_NONE = 3;	
	private final static Image IMAGE_BLANK = ImageFactory.getImage("blank"); //$NON-NLS-1$
	private final static Image IMAGE_CHANGE = ImageFactory.getImage("change"); //$NON-NLS-1$
	private final static Image IMAGE_CLEAR = ImageFactory.getImage("clear"); //$NON-NLS-1$
	private final static Image IMAGE_EXCLAMATION = ImageFactory.getImage("exclamation"); //$NON-NLS-1$
	private final static Image IMAGE_MINUS = ImageFactory.getImage("minus"); //$NON-NLS-1$
	private final static Image IMAGE_PLUS = ImageFactory.getImage("plus"); //$NON-NLS-1$
	private final static RGB RGB_CONFLICT = new RGB(255, 0, 0);
	private final static RGB RGB_CONFLICT_MINUS = new RGB(255, 160, 160);
	private final static RGB RGB_MINUS =	new RGB(160, 160, 160);
	private final static char SPACE = ' ';

	private final class CommandSetPair {
		
		Set customSet;
		Set defaultSet;		
	}

	private final class CommandRecord {

		String commandId;
		KeySequence keySequence;
		String contextId;
		String keyConfigurationId;
		Set customSet;
		Set defaultSet;
		
		boolean customConflict = false;
		String customCommand = null;
		boolean defaultConflict = false;
		String defaultCommand = null;	

		void calculate() {
			if (customSet.size() > 1)
				customConflict = true;
			else if (!customSet.isEmpty())				
				customCommand = (String) customSet.iterator().next();
	
			if (defaultSet.size() > 1)
				defaultConflict = true;
			else if (!defaultSet.isEmpty())				
				defaultCommand = (String) defaultSet.iterator().next();
		}
	}

	private final class KeySequenceRecord {

		String scope;
		String configuration;
		Set customSet;
		Set defaultSet;

		boolean customConflict = false;
		String customCommand = null;
		boolean defaultConflict = false;
		String defaultCommand = null;	

		void calculate() {
			if (customSet.size() > 1)
				customConflict = true;
			else if (!customSet.isEmpty())				
				customCommand = (String) customSet.iterator().next();
	
			if (defaultSet.size() > 1)
				defaultConflict = true;
			else if (!defaultSet.isEmpty())				
				defaultCommand = (String) defaultSet.iterator().next();
		}
	}

	private Button buttonAdd;
	private Button buttonClear;
	private Button buttonRemove;
	private Button buttonRestore;
	private Map categoryDefinitionsById;
	private Map categoryIdsByUniqueName;
	private Map categoryUniqueNamesById;
	private Combo comboCategory;
	private Combo comboCommand;	
	private Combo comboContext;
	private Combo comboKeyConfiguration;	
	private Map commandDefinitionsById;
	private Map commandIdsByUniqueName;
	private Map commandUniqueNamesById;
	private CommandManager commandManager;
	private Map contextDefinitionsById;
	private Map contextIdsByUniqueName;
	private Map contextUniqueNamesById;
	private ContextManager contextManager;
	// TODO private Font fontLabelContextExtends;
	// TODO private Font fontLabelKeyConfigurationExtends;	
	private Map keyConfigurationDefinitionsById;
	private Map keyConfigurationIdsByUniqueName;
	private Map keyConfigurationUniqueNamesById;
	private Label labelCategory; 	
	private Label labelCommand;
	private Label labelCommandGroup; 
	private Label labelCommandsForKeySequence;
	private Label labelContext; 
	private Label labelContextExtends;
	private Label labelDescription;
	private Label labelKeyConfiguration;
	private Label labelKeyConfigurationExtends;
	private Label labelKeySequence;
	private Label labelKeySequencesForCommand;			
	private Table tableCommandsForKeySequence;
	private Table tableKeySequencesForCommand;
	private Text textDescription; 
	private KeySequenceText textKeySequence;
	private SortedMap tree;
	private IWorkbench workbench;	

	private List commandRecords = new ArrayList();	
	private List keySequenceRecords = new ArrayList();
		
	public void init(IWorkbench workbench) {
		this.workbench = workbench;
		commandManager = CommandManager.getInstance();
		contextManager = ContextManager.getInstance();
		tree = new TreeMap();
	}

	public boolean performOk() {
		List preferenceActiveKeyConfigurationDefinitions = new ArrayList();
		preferenceActiveKeyConfigurationDefinitions.add(new ActiveKeyConfigurationDefinition(getKeyConfigurationId(), null));		
		PreferenceCommandRegistry preferenceCommandRegistry = (PreferenceCommandRegistry) commandManager.getPreferenceCommandRegistry();	
		preferenceCommandRegistry.setActiveKeyConfigurationDefinitions(preferenceActiveKeyConfigurationDefinitions);
		List preferenceKeyBindingDefinitions = new ArrayList();
		KeyBindingNode.getKeyBindingDefinitions(tree, KeySequence.getInstance(), 0, preferenceKeyBindingDefinitions);		
		preferenceCommandRegistry.setKeyBindingDefinitions(preferenceKeyBindingDefinitions);
		
		try {
			preferenceCommandRegistry.save();
		} catch (IOException eIO) {
		}

		// TODO ok to remove?
		if (workbench instanceof Workbench)
			((Workbench) workbench).updateCommandAndContextController();

		return super.performOk();
	}

	public void setVisible(boolean visible) {
		if (visible == true) {
			ICommandRegistry pluginCommandRegistry = commandManager.getPluginCommandRegistry();	
			IContextRegistry pluginContextRegistry = contextManager.getPluginContextRegistry();			
			ICommandRegistry preferenceCommandRegistry = commandManager.getPreferenceCommandRegistry();
			IContextRegistry preferenceContextRegistry = contextManager.getPreferenceContextRegistry();	
			List categoryDefinitions = new ArrayList();
			categoryDefinitions.addAll(pluginCommandRegistry.getCategoryDefinitions());
			categoryDefinitions.addAll(preferenceCommandRegistry.getCategoryDefinitions());
			CommandManager.validateCategoryDefinitions(categoryDefinitions);
			categoryDefinitionsById = Collections.unmodifiableMap(CategoryDefinition.categoryDefinitionsById(categoryDefinitions, false));
			Map categoryDefinitionsByName = Collections.unmodifiableMap(CategoryDefinition.categoryDefinitionsByName(categoryDefinitionsById.values(), false));
			categoryIdsByUniqueName = new HashMap();
			Iterator iterator = categoryDefinitionsByName.entrySet().iterator();				

			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String name = (String) entry.getKey();
				Set categoryDefinitions2 = (Set) entry.getValue();
				Iterator iterator2 = categoryDefinitions2.iterator();
				
				if (categoryDefinitions2.size() == 1) {					
					ICategoryDefinition categoryDefinition = (ICategoryDefinition) iterator2.next(); 
					categoryIdsByUniqueName.put(name, categoryDefinition.getId());
				} else while (iterator2.hasNext()) {
					ICategoryDefinition categoryDefinition = (ICategoryDefinition) iterator2.next(); 
					String uniqueName = MessageFormat.format(Util.translateString(resourceBundle, "uniqueName"), new Object[] { name, categoryDefinition.getId() }); //$NON-NLS-1$
					categoryIdsByUniqueName.put(uniqueName, categoryDefinition.getId());							
				}
			}	

			categoryUniqueNamesById = new HashMap();
			iterator = categoryIdsByUniqueName.entrySet().iterator();				

			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				categoryUniqueNamesById.put(entry.getValue(), entry.getKey());
			}

			List commandDefinitions = new ArrayList();
			commandDefinitions.addAll(pluginCommandRegistry.getCommandDefinitions());
			commandDefinitions.addAll(preferenceCommandRegistry.getCommandDefinitions());
			CommandManager.validateCommandDefinitions(commandDefinitions);
			commandDefinitionsById = Collections.unmodifiableMap(CommandDefinition.commandDefinitionsById(commandDefinitions, false));
			Map commandDefinitionsByName = Collections.unmodifiableMap(CommandDefinition.commandDefinitionsByName(commandDefinitionsById.values(), false));
			commandIdsByUniqueName = new HashMap();
			iterator = commandDefinitionsByName.entrySet().iterator();				

			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String name = (String) entry.getKey();
				Set commandDefinitions2 = (Set) entry.getValue();
				Iterator iterator2 = commandDefinitions2.iterator();
				
				if (commandDefinitions2.size() == 1) {					
					ICommandDefinition commandDefinition = (ICommandDefinition) iterator2.next(); 
					commandIdsByUniqueName.put(name, commandDefinition.getId());
				} else while (iterator2.hasNext()) {
					ICommandDefinition commandDefinition = (ICommandDefinition) iterator2.next(); 
					String uniqueName = MessageFormat.format(Util.translateString(resourceBundle, "uniqueName"), new Object[] { name, commandDefinition.getId() }); //$NON-NLS-1$
					commandIdsByUniqueName.put(uniqueName, commandDefinition.getId());							
				}
			}	

			commandUniqueNamesById = new HashMap();
			iterator = commandIdsByUniqueName.entrySet().iterator();				

			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				commandUniqueNamesById.put(entry.getValue(), entry.getKey());
			}

			List contextDefinitions = new ArrayList();
			contextDefinitions.addAll(pluginContextRegistry.getContextDefinitions());
			contextDefinitions.addAll(preferenceContextRegistry.getContextDefinitions());
			ContextManager.validateContextDefinitions(contextDefinitions);
			contextDefinitionsById = Collections.unmodifiableMap(ContextDefinition.contextDefinitionsById(contextDefinitions, false));
			Map contextDefinitionsByName = Collections.unmodifiableMap(ContextDefinition.contextDefinitionsByName(contextDefinitionsById.values(), false));
			contextIdsByUniqueName = new HashMap();
			iterator = contextDefinitionsByName.entrySet().iterator();				

			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String name = (String) entry.getKey();
				Set contextDefinitions2 = (Set) entry.getValue();
				Iterator iterator2 = contextDefinitions2.iterator();
				
				if (contextDefinitions2.size() == 1) {					
					IContextDefinition contextDefinition = (IContextDefinition) iterator2.next(); 
					contextIdsByUniqueName.put(name, contextDefinition.getId());
				} else while (iterator2.hasNext()) {
					IContextDefinition contextDefinition = (IContextDefinition) iterator2.next(); 
					String uniqueName = MessageFormat.format(Util.translateString(resourceBundle, "uniqueName"), new Object[] { name, contextDefinition.getId() }); //$NON-NLS-1$
					contextIdsByUniqueName.put(uniqueName, contextDefinition.getId());												
				}
			}	

			contextUniqueNamesById = new HashMap();
			iterator = contextIdsByUniqueName.entrySet().iterator();				

			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				contextUniqueNamesById.put(entry.getValue(), entry.getKey());
			}

			List keyConfigurationDefinitions = new ArrayList();
			keyConfigurationDefinitions.addAll(pluginCommandRegistry.getKeyConfigurationDefinitions());
			keyConfigurationDefinitions.addAll(preferenceCommandRegistry.getKeyConfigurationDefinitions());
			CommandManager.validateKeyConfigurationDefinitions(keyConfigurationDefinitions);
			keyConfigurationDefinitionsById = Collections.unmodifiableMap(KeyConfigurationDefinition.keyConfigurationDefinitionsById(keyConfigurationDefinitions, false));
			Map keyConfigurationDefinitionsByName = Collections.unmodifiableMap(KeyConfigurationDefinition.keyConfigurationDefinitionsByName(keyConfigurationDefinitionsById.values(), false));
			keyConfigurationIdsByUniqueName = new HashMap();
			iterator = keyConfigurationDefinitionsByName.entrySet().iterator();				

			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String name = (String) entry.getKey();
				Set keyConfigurationDefinitions2 = (Set) entry.getValue();
				Iterator iterator2 = keyConfigurationDefinitions2.iterator();
				
				if (keyConfigurationDefinitions2.size() == 1) {					
					IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) iterator2.next(); 
					keyConfigurationIdsByUniqueName.put(name, keyConfigurationDefinition.getId());
				} else while (iterator2.hasNext()) {
					IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) iterator2.next(); 
					String uniqueName = MessageFormat.format(Util.translateString(resourceBundle, "uniqueName"), new Object[] { name, keyConfigurationDefinition.getId() }); //$NON-NLS-1$
					keyConfigurationIdsByUniqueName.put(uniqueName, keyConfigurationDefinition.getId());		
				}
			}	

			keyConfigurationUniqueNamesById = new HashMap();
			iterator = keyConfigurationIdsByUniqueName.entrySet().iterator();				

			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				keyConfigurationUniqueNamesById.put(entry.getValue(), entry.getKey());
			}
		
			List categoryNames = new ArrayList(categoryIdsByUniqueName.keySet());
			Collections.sort(categoryNames, Collator.getInstance());						
			comboCategory.setItems((String[]) categoryNames.toArray(new String[categoryNames.size()]));
			List commandNames = new ArrayList(commandIdsByUniqueName.keySet());
			Collections.sort(commandNames, Collator.getInstance());						
			comboCommand.setItems((String[]) commandNames.toArray(new String[commandNames.size()]));
			List contextNames = new ArrayList(contextIdsByUniqueName.keySet());
			Collections.sort(contextNames, Collator.getInstance());						
			contextNames.add(0, Util.translateString(resourceBundle, "general")); //$NON-NLS-1$
			comboContext.setItems((String[]) contextNames.toArray(new String[contextNames.size()]));
			List keyConfigurationNames = new ArrayList(keyConfigurationIdsByUniqueName.keySet());
			Collections.sort(keyConfigurationNames, Collator.getInstance());						
			keyConfigurationNames.add(0, Util.translateString(resourceBundle, "standard")); //$NON-NLS-1$
			comboKeyConfiguration.setItems((String[]) keyConfigurationNames.toArray(new String[keyConfigurationNames.size()]));

			/*
			while (iterator.hasNext()) {
				IContextDefinition contextDefinition = (IContextDefinition) iterator.next();
				
				if (contextDefinition != null) {
					String name = contextDefinition.getName();
					String parentId = contextDefinition.getParentId();
				
					if (parentId != null) {
						contextDefinition = (IContextDefinition) contextDefinitionsById.get(parentId);
					
						if (contextDefinition != null)
							name = MessageFormat.format(Util.getString(resourceBundle, "extends"), new Object[] { name, contextDefinition.getName() }); //$NON-NLS-1$
						else 
							name = MessageFormat.format(Util.getString(resourceBundle, "extends"), new Object[] { name, parentId }); //$NON-NLS-1$
					} else
						name = MessageFormat.format(Util.getString(resourceBundle, "extendsGeneral"), new Object[] { name }); //$NON-NLS-1$

					contextNames.add(name);
				}
			}
			
			while (iterator.hasNext()) {
				IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) iterator.next();
				
				if (keyConfigurationDefinition != null) {
					String name = keyConfigurationDefinition.getName();
					String parentId = keyConfigurationDefinition.getParentId();
				
					if (parentId != null) {
						keyConfigurationDefinition = (IKeyConfigurationDefinition) keyConfigurationDefinitionsById.get(parentId);
					
						if (keyConfigurationDefinition != null)
							name = MessageFormat.format(Util.getString(resourceBundle, "extends"), new Object[] { name, keyConfigurationDefinition.getName() }); //$NON-NLS-1$
						else 
							name = MessageFormat.format(Util.getString(resourceBundle, "extends"), new Object[] { name, parentId }); //$NON-NLS-1$
					} else
						name = MessageFormat.format(Util.getString(resourceBundle, "extendsStandard"), new Object[] { name }); //$NON-NLS-1$

					keyConfigurationNames.add(name);
				}
			}	
			
			labelContext.setVisible(..);
			comboContext.setVisible(..);					
			
			labelKeyConfiguration.setVisible(..);
			comboKeyConfiguration.setVisible(..);
			*/

			List activeKeyConfigurationDefinitions = new ArrayList();
			activeKeyConfigurationDefinitions.addAll(pluginCommandRegistry.getActiveKeyConfigurationDefinitions());
			activeKeyConfigurationDefinitions.addAll(preferenceCommandRegistry.getActiveKeyConfigurationDefinitions());
			String activeKeyConfigurationId = null;
		
			if (activeKeyConfigurationDefinitions.size() >= 1) {
				IActiveKeyConfigurationDefinition activeKeyConfigurationDefinition = (IActiveKeyConfigurationDefinition) activeKeyConfigurationDefinitions.get(activeKeyConfigurationDefinitions.size() - 1);
				activeKeyConfigurationId = activeKeyConfigurationDefinition.getKeyConfigurationId();
			
				if (activeKeyConfigurationId != null && !keyConfigurationDefinitionsById.containsKey(activeKeyConfigurationId))
					activeKeyConfigurationId = null;
			}
			
			setContextId(null);
			setKeyConfigurationId(activeKeyConfigurationId);
			List pluginKeyBindingDefinitions = new ArrayList(pluginCommandRegistry.getKeyBindingDefinitions());
			CommandManager.validateKeyBindingDefinitions(pluginKeyBindingDefinitions);
			List preferenceKeyBindingDefinitions = new ArrayList(preferenceCommandRegistry.getKeyBindingDefinitions());
			CommandManager.validateKeyBindingDefinitions(preferenceKeyBindingDefinitions);
			tree.clear();
			iterator = preferenceKeyBindingDefinitions.iterator();
			
			while (iterator.hasNext()) {
				IKeyBindingDefinition keyBindingDefinition = (IKeyBindingDefinition) iterator.next();
				KeyBindingNode.add(tree, keyBindingDefinition.getKeySequence(), keyBindingDefinition.getContextId(), keyBindingDefinition.getKeyConfigurationId(), 0, keyBindingDefinition.getPlatform(), keyBindingDefinition.getLocale(), keyBindingDefinition.getCommandId());
			}

			iterator = pluginKeyBindingDefinitions.iterator();
			
			while (iterator.hasNext()) {
				IKeyBindingDefinition keyBindingDefinition = (IKeyBindingDefinition) iterator.next();
				KeyBindingNode.add(tree, keyBindingDefinition.getKeySequence(), keyBindingDefinition.getContextId(), keyBindingDefinition.getKeyConfigurationId(), 1, keyBindingDefinition.getPlatform(), keyBindingDefinition.getLocale(), keyBindingDefinition.getCommandId());
			}

			/*
			keySequencesByName = new TreeMap();
			Iterator iterator = tree.keySet().iterator();

			while (iterator.hasNext()) {
				Object object = iterator.next();
			
				if (object instanceof KeySequence) {
					KeySequence keySequence = (KeySequence) object;
					String name = KeySupport.formatSequence(keySequence, true);
					keySequencesByName.put(name, keySequence);
				}
			}		

			Set keySequenceNameSet = keySequencesByName.keySet();
			comboSequence.setItems((String[]) keySequenceNameSet.toArray(new String[keySequenceNameSet.size()]));
			selectedTreeViewerCommands();
			*/
		}

		super.setVisible(visible);
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.numColumns = 3;
		composite.setLayout(gridLayout);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gridData);
		labelKeyConfiguration = new Label(composite, SWT.LEFT);
		labelKeyConfiguration.setText(Util.translateString(resourceBundle, "labelKeyConfiguration")); //$NON-NLS-1$
		comboKeyConfiguration = new Combo(composite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.widthHint = 200;
		comboKeyConfiguration.setLayoutData(gridData);

		comboKeyConfiguration.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboKeyConfiguration();
			}	
		});

		labelKeyConfigurationExtends = new Label(composite, SWT.LEFT);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		labelKeyConfigurationExtends.setLayoutData(gridData);
		Label labelSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		labelSeparator.setLayoutData(gridData);	
		labelCommandGroup = new Label(composite, SWT.LEFT);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		labelCommandGroup.setLayoutData(gridData);
		labelCommandGroup.setText(Util.translateString(resourceBundle, "labelCommandGroup")); //$NON-NLS-1$
		labelCategory = new Label(composite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalIndent = 50;
		labelCategory.setLayoutData(gridData);
		labelCategory.setText(Util.translateString(resourceBundle, "labelCategory")); //$NON-NLS-1$
		comboCategory = new Combo(composite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.widthHint = 200;
		comboCategory.setLayoutData(gridData);

		comboCategory.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboCategory();
			}	
		});

		labelCommand = new Label(composite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalIndent = 50;
		labelCommand.setLayoutData(gridData);
		labelCommand.setText(Util.translateString(resourceBundle, "labelCommand")); //$NON-NLS-1$
		comboCommand = new Combo(composite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.widthHint = 300;
		comboCommand.setLayoutData(gridData);

		comboCommand.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboCommand();
			}	
		});

		labelDescription = new Label(composite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalIndent = 50;
		labelDescription.setLayoutData(gridData);
		labelDescription.setText(Util.translateString(resourceBundle, "labelDescription")); //$NON-NLS-1$
		textDescription = new Text(composite, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		textDescription.setLayoutData(gridData);
		labelKeySequencesForCommand = new Label(composite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalIndent = 50;
		gridData.horizontalSpan = 3;
		labelKeySequencesForCommand.setLayoutData(gridData);
		labelKeySequencesForCommand.setText(Util.translateString(resourceBundle, "labelKeySequencesForCommand")); //$NON-NLS-1$
		tableKeySequencesForCommand = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tableKeySequencesForCommand.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 50;
		gridData.horizontalIndent = 50;
		gridData.horizontalSpan = 3;
		gridData.widthHint = 520;
		tableKeySequencesForCommand.setLayoutData(gridData);
		int width = 0;
		TableColumn tableColumn = new TableColumn(tableKeySequencesForCommand, SWT.NULL, 0);
		tableColumn.setResizable(false);
		tableColumn.setText(Util.ZERO_LENGTH_STRING);
		tableColumn.setWidth(20);
		width += tableColumn.getWidth();
		tableColumn = new TableColumn(tableKeySequencesForCommand, SWT.NULL, 1);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.translateString(resourceBundle, "tableColumnContext")); //$NON-NLS-1$
		tableColumn.pack();
		tableColumn.setWidth(100); // TODO tableColumn.getWidth() + constant
		width += tableColumn.getWidth();
		tableColumn = new TableColumn(tableKeySequencesForCommand, SWT.NULL, 2);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.translateString(resourceBundle, "tableColumnKeySequence")); //$NON-NLS-1$
		tableColumn.pack();
		tableColumn.setWidth(Math.max(220, Math.max(440 - width, tableColumn.getWidth() + 20)));	

		tableKeySequencesForCommand.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent mouseEvent) {
				doubleClickedTableKeySequencesForCommand();	
			}			
		});		

		tableKeySequencesForCommand.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {			
				selectedTableKeySequencesForCommand();
			}	
		});

		labelContext = new Label(composite, SWT.LEFT);
		labelContext.setText(Util.translateString(resourceBundle, "labelContext")); //$NON-NLS-1$
		comboContext = new Combo(composite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.widthHint = 200;
		comboContext.setLayoutData(gridData);

		comboContext.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboContext();
			}	
		});

		labelContextExtends = new Label(composite, SWT.LEFT);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		labelContextExtends.setLayoutData(gridData);
		labelKeySequence = new Label(composite, SWT.LEFT);
		labelKeySequence.setText(Util.translateString(resourceBundle, "labelKeySequence")); //$NON-NLS-1$
		Composite compositeKeySequence = new Composite(composite, SWT.NULL);
		gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;		
		gridLayout.numColumns = 2;
		compositeKeySequence.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		compositeKeySequence.setLayoutData(gridData);
		textKeySequence = new KeySequenceText(compositeKeySequence);
		gridData = new GridData();
		gridData.widthHint = 300;
		textKeySequence.setLayoutData(gridData);

		textKeySequence.addModifyListener(new ModifyListener() {			
			public void modifyText(ModifyEvent modifyEvent) {
				modifiedTextKeySequence();
			}	
		});

		buttonClear = new Button(compositeKeySequence, SWT.FLAT);
		buttonClear.setImage(IMAGE_CLEAR);
		/* TODO 
		gridData = new GridData();
		gridData.heightHint = 20;
		gridData.widthHint = 20;
		buttonClear.setLayoutData(gridData);
		*/

		buttonClear.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedButtonClear();
			}	
		});

		labelCommandsForKeySequence = new Label(composite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalIndent = 50;
		gridData.horizontalSpan = 3;
		labelCommandsForKeySequence.setLayoutData(gridData);
		labelCommandsForKeySequence.setText(Util.translateString(resourceBundle, "labelCommandsForKeySequence")); //$NON-NLS-1$
		tableCommandsForKeySequence = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tableCommandsForKeySequence.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 50;
		gridData.horizontalIndent = 50;
		gridData.horizontalSpan = 3;
		gridData.widthHint = 520;
		tableCommandsForKeySequence.setLayoutData(gridData);
		width = 0;
		tableColumn = new TableColumn(tableCommandsForKeySequence, SWT.NULL, 0);
		tableColumn.setResizable(false);
		tableColumn.setText(Util.ZERO_LENGTH_STRING);
		tableColumn.setWidth(20);
		width += tableColumn.getWidth();
		tableColumn = new TableColumn(tableCommandsForKeySequence, SWT.NULL, 1);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.translateString(resourceBundle, "tableColumnContext")); //$NON-NLS-1$
		tableColumn.pack();		
		tableColumn.setWidth(100); // TODO tableColumn.getWidth() + constant
		width += tableColumn.getWidth();
		tableColumn = new TableColumn(tableCommandsForKeySequence, SWT.NULL, 2);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.translateString(resourceBundle, "tableColumnCommand")); //$NON-NLS-1$
		tableColumn.pack();
		tableColumn.setWidth(Math.max(220, Math.max(440 - width, tableColumn.getWidth() + 20)));		

		tableCommandsForKeySequence.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent mouseEvent) {
				doubleClickedTableCommandsForKeySequence();	
			}			
		});		

		tableCommandsForKeySequence.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {			
				selectedTableCommandsForKeySequence();
			}	
		});

		Composite compositeButton = new Composite(composite, SWT.NULL);
		gridLayout = new GridLayout();
		gridLayout.marginHeight = 20;
		gridLayout.marginWidth = 0;		
		gridLayout.numColumns = 3;
		compositeButton.setLayout(gridLayout);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		compositeButton.setLayoutData(gridData);
		buttonAdd = new Button(compositeButton, SWT.CENTER | SWT.PUSH);
		gridData = new GridData();
		gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		buttonAdd.setText(Util.translateString(resourceBundle, "buttonAdd")); //$NON-NLS-1$
		gridData.widthHint = Math.max(widthHint, buttonAdd.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		buttonAdd.setLayoutData(gridData);		

		buttonAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedButtonAdd();
			}	
		});

		buttonRemove = new Button(compositeButton, SWT.CENTER | SWT.PUSH);
		gridData = new GridData();
		gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		buttonRemove.setText(Util.translateString(resourceBundle, "buttonRemove")); //$NON-NLS-1$
		gridData.widthHint = Math.max(widthHint, buttonRemove.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		buttonRemove.setLayoutData(gridData);		

		buttonRemove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedButtonRemove();
			}	
		});

		buttonRestore = new Button(compositeButton, SWT.CENTER | SWT.PUSH);
		gridData = new GridData();
		gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		buttonRestore.setText(Util.translateString(resourceBundle, "buttonRestore")); //$NON-NLS-1$
		gridData.widthHint = Math.max(widthHint, buttonRestore.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		buttonRestore.setLayoutData(gridData);		
		
		buttonRestore.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedButtonRestore();
			}	
		});
				
		// TODO WorkbenchHelp.setHelp(parent, IHelpContextIds.WORKBENCH_KEY_PREFERENCE_PAGE);
		applyDialogFont(composite);
		return composite;	
	}

	protected IPreferenceStore doGetPreferenceStore() {
		return WorkbenchPlugin.getDefault().getPreferenceStore();
	}

	protected void performDefaults() {
		String activeKeyConfigurationId = getKeyConfigurationId();
		List preferenceKeyBindingDefinitions = new ArrayList();
		KeyBindingNode.getKeyBindingDefinitions(tree, KeySequence.getInstance(), 0, preferenceKeyBindingDefinitions);	

		if (activeKeyConfigurationId != null || !preferenceKeyBindingDefinitions.isEmpty()) {
			MessageBox restoreDefaultsMessageBox = new MessageBox(getShell(), SWT.YES | SWT.NO | SWT.ICON_WARNING | SWT.APPLICATION_MODAL);
			restoreDefaultsMessageBox.setText(Util.translateString(resourceBundle, "restoreDefaultsMessageBoxText")); //$NON-NLS-1$
			restoreDefaultsMessageBox.setMessage(Util.translateString(resourceBundle, "restoreDefaultsMessageBoxMessage")); //$NON-NLS-1$
		
			if (restoreDefaultsMessageBox.open() == SWT.YES) {
				setKeyConfigurationId(null);			
				Iterator iterator = preferenceKeyBindingDefinitions.iterator();
				
				while (iterator.hasNext()) {
					IKeyBindingDefinition keyBindingDefinition = (IKeyBindingDefinition) iterator.next();
					KeyBindingNode.remove(tree, keyBindingDefinition.getKeySequence(), keyBindingDefinition.getContextId(), keyBindingDefinition.getKeyConfigurationId(), 0, keyBindingDefinition.getPlatform(), keyBindingDefinition.getLocale(), keyBindingDefinition.getCommandId());
				}
			}
		}
	}

	private void doubleClickedTableCommandsForKeySequence() {	
	}
	
	private void doubleClickedTableKeySequencesForCommand() {
	}

	private String getCategoryId() {
		return (String) categoryIdsByUniqueName.get(comboCategory.getText());
	}
	
	private String getCommandId() {
		return (String) commandIdsByUniqueName.get(comboCommand.getText());
	}
	
	private String getContextId() {
		return comboContext.getSelectionIndex() > 0 ? (String) contextIdsByUniqueName.get(comboContext.getText()) : null;
	}

	private String getKeyConfigurationId() {
		return comboKeyConfiguration.getSelectionIndex() > 0 ? (String) keyConfigurationIdsByUniqueName.get(comboKeyConfiguration.getText()) : null;
	}

	private KeySequence getKeySequence() {
        return textKeySequence.getKeySequence();
	}

	private void modifiedTextKeySequence() {
	}

	private void selectedButtonAdd() {
		String commandId = getCommandId();
		String contextId = getContextId();
		String keyConfigurationId = getKeyConfigurationId();
		KeySequence keySequence = getKeySequence();
		KeyBindingNode.add(tree, keySequence, contextId, keyConfigurationId, 0, null, null, commandId);			
		List preferenceKeyBindingDefinitions = new ArrayList();
		KeyBindingNode.getKeyBindingDefinitions(tree, KeySequence.getInstance(), 0, preferenceKeyBindingDefinitions);		
		System.out.println("current: " + preferenceKeyBindingDefinitions);		
	}
		
	private void selectedButtonClear() {
		textKeySequence.clear();
	}

	private void selectedButtonRemove() {
		String contextId = getContextId();
		String keyConfigurationId = getKeyConfigurationId();
		KeySequence keySequence = getKeySequence();		
		KeyBindingNode.add(tree, keySequence, null, null, 0, null, null, null);
		List preferenceKeyBindingDefinitions = new ArrayList();
		KeyBindingNode.getKeyBindingDefinitions(tree, KeySequence.getInstance(), 0, preferenceKeyBindingDefinitions);		
		System.out.println("current: " + preferenceKeyBindingDefinitions);			
	}
	
	private void selectedButtonRestore() {
		String contextId = getContextId();
		String keyConfigurationId = getKeyConfigurationId();
		KeySequence keySequence = getKeySequence();
		KeyBindingNode.remove(tree, keySequence, null, null, 0, null, null);
		List preferenceKeyBindingDefinitions = new ArrayList();
		KeyBindingNode.getKeyBindingDefinitions(tree, KeySequence.getInstance(), 0, preferenceKeyBindingDefinitions);		
		System.out.println("current: " + preferenceKeyBindingDefinitions);			
	}

	private void selectedComboCategory() {		
	}	

	private void selectedComboCommand() {			
	}

	private void selectedComboContext() {		
	}	

	private void selectedComboKeyConfiguration() {		
	}	

	private void selectedTableCommandsForKeySequence() {
	}

	private void selectedTableKeySequencesForCommand() {
	}

	private void setCategoryId(String categoryId) {				
		comboCategory.clearSelection();
		comboCategory.deselectAll();
		String categoryUniqueName = (String) categoryUniqueNamesById.get(categoryId);
		
		if (categoryUniqueName != null) {
			String items[] = comboCategory.getItems();
			
			for (int i = 0; i < items.length; i++)
				if (categoryUniqueName.equals(items[i])) {
					comboCategory.select(i);
					break;		
				}
		}
	}
	
	private void setCommandId(String commandId) {				
		comboCommand.clearSelection();
		comboCommand.deselectAll();
		String commandUniqueName = (String) commandUniqueNamesById.get(commandId);
		
		if (commandUniqueName != null) {
			String items[] = comboCommand.getItems();
			
			for (int i = 0; i < items.length; i++)
				if (commandUniqueName.equals(items[i])) {
					comboCommand.select(i);
					break;		
				}
		}
	}

	private void setContextId(String contextId) {				
		comboContext.clearSelection();
		comboContext.deselectAll();
		String contextUniqueName = (String) contextUniqueNamesById.get(contextId);
		
		if (contextUniqueName != null) {
			String items[] = comboContext.getItems();
			
			for (int i = 1; i < items.length; i++)
				if (contextUniqueName.equals(items[i])) {
					comboContext.select(i);
					break;		
				}
		} else 
			comboContext.select(0);
	}

	private void setKeyConfigurationId(String keyConfigurationId) {				
		comboKeyConfiguration.clearSelection();
		comboKeyConfiguration.deselectAll();
		String keyConfigurationUniqueName = (String) keyConfigurationUniqueNamesById.get(keyConfigurationId);
		
		if (keyConfigurationUniqueName != null) {
			String items[] = comboKeyConfiguration.getItems();
			
			for (int i = 1; i < items.length; i++)
				if (keyConfigurationUniqueName.equals(items[i])) {
					comboKeyConfiguration.select(i);
					break;		
				}
		} else
			comboKeyConfiguration.select(0);
	}

	private void setKeySequence(KeySequence keySequence) {
        textKeySequence.setKeySequence(keySequence, null);
	}

	/*
	private String bracket(String string) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append('[');
		stringBuffer.append(string);
		stringBuffer.append(']');
		return stringBuffer.toString();
	}

	private void update() {
		ICommandDefinition command = null;
		ISelection selection = treeViewerCommands.getSelection();
			
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Object object = ((IStructuredSelection) selection).getFirstElement();
							
			if (object instanceof ICommandDefinition)
				command = (ICommandDefinition) object;
		}
	
		boolean commandSelected = command != null;
	
		KeySequence keySequence = getKeySequence();
		boolean validKeySequence = keySequence != null && validateSequence(keySequence);
		String scopeId = getScopeId();
		boolean validScopeId = scopeId != null && contextsById.get(scopeId) != null;	
		String keyConfigurationId = getKeyConfigurationId();
		boolean validKeyConfigurationId = keyConfigurationId != null && keyConfigurationsById.get(keyConfigurationId) != null;
	
		labelName.setEnabled(commandSelected);
		textName.setEnabled(commandSelected);
		labelDescription.setEnabled(commandSelected);
		textDescription.setEnabled(commandSelected);
		labelSequencesForCommand.setEnabled(commandSelected);
		tableSequencesForCommand.setEnabled(commandSelected);
		labelSequence.setEnabled(commandSelected);		
		comboSequence.setEnabled(commandSelected);
		labelScope.setEnabled(commandSelected);		
		comboScope.setEnabled(commandSelected);
		labelConfiguration.setEnabled(commandSelected);	
		comboConfiguration.setEnabled(commandSelected);	
		buttonAdd.setEnabled(false);
		buttonRemove.setEnabled(false);
		buttonRestore.setEnabled(false);
		labelCommandsForSequence.setEnabled(validKeySequence);		
		tableCommandsForSequence.setEnabled(validKeySequence);		
		textName.setText(commandSelected ? command.getName() : Util.ZERO_LENGTH_STRING);		
		String description = commandSelected ? command.getDescription() : null;
		textDescription.setText(description != null ? description : Util.ZERO_LENGTH_STRING);		
		CommandRecord commandRecord = getSelectedCommandRecord();
			
		if (commandRecord == null)
			buttonAdd.setEnabled(commandSelected && validKeySequence && validScopeId && validKeyConfigurationId);
		else {
			if (!commandRecord.customSet.isEmpty() && !commandRecord.defaultSet.isEmpty()) {
				buttonRestore.setEnabled(commandSelected && validKeySequence && validScopeId && validKeyConfigurationId);
			} else
				buttonRemove.setEnabled(commandSelected && validKeySequence && validScopeId && validKeyConfigurationId);
		}
	
		if (validKeySequence) {
			String text = MessageFormat.format(Util.getString(resourceBundle, "labelCommandsForSequence.selection"), new Object[] { '\''+ KeySupport.formatSequence(keySequence, true) + '\''}); //$NON-NLS-1$
			labelCommandsForSequence.setText(text);
		} else 
			labelCommandsForSequence.setText(Util.getString(resourceBundle, "labelCommandsForSequence.noSelection")); //$NON-NLS-1$
	}

	private void selectedComboContext() {
		KeySequence keySequence = getKeySequence();
		String scopeId = getScopeId();
		String keyConfigurationId = getKeyConfigurationId();
		selectTableCommand(scopeId, keyConfigurationId, keySequence);
		selectTableKeySequence(scopeId, keyConfigurationId);
		update();
	}

	private void selectedComboName() {		
			from selectedTreeViewerCommands...
			commandRecords.clear();
			ISelection selection = treeViewerCommands.getSelection();
		
			if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
				Object object = ((IStructuredSelection) selection).getFirstElement();
						
				if (object instanceof ICommandDefinition)
					buildCommandRecords(tree, ((ICommandDefinition) object).getId(), commandRecords);
			}

			buildTableCommand();
			setKeySequence(null);
		
			// TODO: add 'globalScope' element to commands extension point to remove this.
			setScopeId("org.eclipse.ui.globalScope"); //$NON-NLS-1$
			setKeyConfigurationId(getActiveKeyConfigurationId());				
		
			KeySequence keySequence = getKeySequence();
			String scopeId = getScopeId();
			String keyConfigurationId = getKeyConfigurationId();
			selectTableCommand(scopeId, keyConfigurationId, keySequence);				
			update();
		}
	}

	private void selectedTableKeySequencesForCommand() {
		CommandRecord commandRecord = (CommandRecord) getSelectedCommandRecord();
		
		if (commandRecord != null) {
			setScopeId(commandRecord.scope);
			setKeyConfigurationId(commandRecord.configuration);				
			setKeySequence(commandRecord.sequence);
		}
		
		update();
	}

	private void modifiedComboKeySequence() {
		//selectedComboKeySequence();
	}

	private void selectedComboKeySequence() {
		KeySequence keySequence = getKeySequence();
		String scopeId = getScopeId();
		String keyConfigurationId = getKeyConfigurationId();
		selectTableCommand(scopeId, keyConfigurationId, keySequence);						
		keySequenceRecords.clear();
		buildSequenceRecords(tree, keySequence, keySequenceRecords);
		buildTableKeySequence();	
		selectTableKeySequence(scopeId, keyConfigurationId);		
		update();
	}

	private void selectedButtonChange() {
		KeySequence keySequence = getKeySequence();
		boolean validKeySequence = keySequence != null && validateSequence(keySequence);
		String scopeId = getScopeId();
		boolean validScopeId = scopeId != null && contextsDefinitionsById.get(scopeId) != null;	
		String keyConfigurationId = getKeyConfigurationId();
		boolean validKeyConfigurationId = keyConfigurationId != null && keyConfigurationsById.get(keyConfigurationId) != null;
	
		if (validKeySequence && validScopeId && validKeyConfigurationId) {	
			String commandId = null;
			ISelection selection = treeViewerCommands.getSelection();
		
			if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
				Object object = ((IStructuredSelection) selection).getFirstElement();
						
				if (object instanceof ICommandDefinition)
					commandId = ((ICommandDefinition) object).getId();
			}

			CommandRecord commandRecord = getSelectedCommandRecord();
		
			if (commandRecord == null)
				set(tree, keySequence, scopeId, keyConfigurationId, commandId);			 
			else {
				if (!commandRecord.customSet.isEmpty())
					clear(tree, keySequence, scopeId, keyConfigurationId);
				else
					set(tree, keySequence, scopeId, keyConfigurationId, null);
			}

			commandRecords.clear();
			buildCommandRecords(tree, commandId, commandRecords);
			buildTableCommand();
			selectTableCommand(scopeId, keyConfigurationId, keySequence);							
			keySequenceRecords.clear();
			buildSequenceRecords(tree, keySequence, keySequenceRecords);
			buildTableKeySequence();	
			selectTableKeySequence(scopeId, keyConfigurationId);
			update();
		}
	}

	private class TreeViewerCommandsContentProvider implements ITreeContentProvider {
		
		public void dispose() {
		}
		
		public Object[] getChildren(Object parentElement) {
			List children = new ArrayList();
			List commandDefinitions = new ArrayList(KeyPreferencePage.this.commandDefinitions);
			Collections.sort(commandDefinitions, CommandDefinition.nameComparator());

			if (parentElement instanceof ICategoryDefinition) {
				ICategoryDefinition categoryDefinition = (ICategoryDefinition) parentElement;

				for (int i = 0; i < commandDefinitions.size(); i++) {
					ICommandDefinition commandDefinition = (ICommandDefinition) commandDefinitions.get(i);
							
					if (categoryDefinition.getId().equals(commandDefinition.getCategoryId()))
						children.add(commandDefinition);											
				}
			} else if (parentElement == null) {
				List categoryDefinitions = new ArrayList(KeyPreferencePage.this.categoryDefinitions);
				Collections.sort(categoryDefinitions, CategoryDefinition.nameComparator());
				children.addAll(categoryDefinitions);
	
				for (int i = 0; i < commandDefinitions.size(); i++) {
					ICommandDefinition commandDefinition = (ICommandDefinition) commandDefinitions.get(i);
							
					if (commandDefinition.getCategoryId() == null)
						children.add(commandDefinition);										
				}									
			}

			return children.toArray();
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(null);
		}

		public Object getParent(Object element) {
			if (element instanceof ICommandDefinition && categoryDefinitionsById != null) {
				String categoryId = ((ICommandDefinition) element).getCategoryId();
				
				if (categoryId != null)
					return categoryDefinitionsById.get(categoryId);
			}

			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length >= 1;
		}			

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
		
	private class TreeViewerCommandsLabelProvider extends LabelProvider {			

		public String getText(Object element) {
			if (element instanceof ICategoryDefinition)
				return ((ICategoryDefinition) element).getName();
			else if (element instanceof ICommandDefinition)
				return ((ICommandDefinition) element).getName();
			else 				
				return super.getText(element);
		}						
	};

	private void buildCommandRecords(SortedMap tree, String command, List commandRecords) {
		if (commandRecords != null) {
			commandRecords.clear();
					
			if (tree != null) {
				Iterator iterator = tree.entrySet().iterator();
						
				while (iterator.hasNext()) {
					Map.Entry entry = (Map.Entry) iterator.next();
					KeySequence sequence = (KeySequence) entry.getKey();					
					Map scopeMap = (Map) entry.getValue();						
			
					if (scopeMap != null) {
						Iterator iterator2 = scopeMap.entrySet().iterator();
							
						while (iterator2.hasNext()) {
							Map.Entry entry2 = (Map.Entry) iterator2.next();
							String scope = (String) entry2.getKey();										
							Map configurationMap = (Map) entry2.getValue();						
							Iterator iterator3 = configurationMap.entrySet().iterator();
											
							while (iterator3.hasNext()) {
								Map.Entry entry3 = (Map.Entry) iterator3.next();
								String configuration = (String) entry3.getKey();					
								CommandSetPair commandSetPair = (CommandSetPair) entry3.getValue();													
								Set customSet = new HashSet();
								Set defaultSet = new HashSet();						
		
								if (commandSetPair.customSet != null)
									customSet.addAll(commandSetPair.customSet);
	
								if (commandSetPair.defaultSet != null)
									defaultSet.addAll(commandSetPair.defaultSet);
										
								if (customSet.contains(command) || defaultSet.contains(command)) {
									CommandRecord commandRecord = new CommandRecord();
									commandRecord.command = command;
									commandRecord.sequence = sequence;
									commandRecord.scope = scope;
									commandRecord.configuration = configuration;
									commandRecord.customSet = customSet;
									commandRecord.defaultSet = defaultSet;
									commandRecord.calculate();	
									commandRecords.add(commandRecord);									
								}
							}
						}
					}
				}												
			}	
		}
	}
	
	private void buildSequenceRecords(SortedMap tree, KeySequence sequence, List sequenceRecords) {
		if (sequenceRecords != null) {
			sequenceRecords.clear();
				
			if (tree != null && sequence != null) {
				Map scopeMap = (Map) tree.get(sequence);
				
				if (scopeMap != null) {
					Iterator iterator = scopeMap.entrySet().iterator();
				
					while (iterator.hasNext()) {
						Map.Entry entry = (Map.Entry) iterator.next();
						String scope = (String) entry.getKey();					
						Map configurationMap = (Map) entry.getValue();						
						Iterator iterator2 = configurationMap.entrySet().iterator();
								
						while (iterator2.hasNext()) {
							Map.Entry entry2 = (Map.Entry) iterator2.next();
							String configuration = (String) entry2.getKey();					
							CommandSetPair commandSetPair = (CommandSetPair) entry2.getValue();													
							Set customSet = new HashSet();
							Set defaultSet = new HashSet();						
		
							if (commandSetPair.customSet != null)
								customSet.addAll(commandSetPair.customSet);
	
							if (commandSetPair.defaultSet != null)
								defaultSet.addAll(commandSetPair.defaultSet);							
	
							KeySequenceRecord sequenceRecord = new KeySequenceRecord();
							sequenceRecord.scope = scope;
							sequenceRecord.configuration = configuration;							
							sequenceRecord.customSet = customSet;
							sequenceRecord.defaultSet = defaultSet;		
							sequenceRecord.calculate();
							sequenceRecords.add(sequenceRecord);
						}												
					}	
				}								
			}			
		}
	}
	
	private void buildTableCommand() {
		tableSequencesForCommand.removeAll();
	
		for (int i = 0; i < commandRecords.size(); i++) {
			CommandRecord commandRecord = (CommandRecord) commandRecords.get(i);
			Set customSet = commandRecord.customSet;
			Set defaultSet = commandRecord.defaultSet;
			int difference = DIFFERENCE_NONE;
			//String commandId = null;
			boolean commandConflict = false;
			String alternateCommandId = null;
			boolean alternateCommandConflict = false;
		
			if (customSet.isEmpty()) {
				if (defaultSet.contains(commandRecord.command)) {												
					//commandId = commandRecord.commandId;
					commandConflict = commandRecord.defaultConflict;					
				}
			} else {
				if (defaultSet.isEmpty()) {									
					if (customSet.contains(commandRecord.command)) {													
						difference = DIFFERENCE_ADD;
						//commandId = commandRecord.commandId;
						commandConflict = commandRecord.customConflict;
					}
				} else {
					if (customSet.contains(commandRecord.command)) {
						difference = DIFFERENCE_CHANGE;
						//commandId = commandRecord.commandId;
						commandConflict = commandRecord.customConflict;		
						alternateCommandId = commandRecord.defaultCommand;
						alternateCommandConflict = commandRecord.defaultConflict;
					} else {
						if (defaultSet.contains(commandRecord.command)) {	
							difference = DIFFERENCE_MINUS;
							//commandId = commandRecord.commandId;
							commandConflict = commandRecord.defaultConflict;		
							alternateCommandId = commandRecord.customCommand;
							alternateCommandConflict = commandRecord.customConflict;
						}
					}
				}								
			}
	
			TableItem tableItem = new TableItem(tableSequencesForCommand, SWT.NULL);					
	
			switch (difference) {
				case DIFFERENCE_ADD:
					tableItem.setImage(0, IMAGE_PLUS);
					break;
	
				case DIFFERENCE_CHANGE:
					tableItem.setImage(0, IMAGE_CHANGE);
					break;
	
				case DIFFERENCE_MINUS:
					tableItem.setImage(0, IMAGE_MINUS);
					break;
	
				case DIFFERENCE_NONE:
					tableItem.setImage(0, IMAGE_BLANK);
					break;				
			}
	
			IContextDefinition scope = (IContextDefinition) contextsById.get(commandRecord.scope);
			tableItem.setText(1, scope != null ? scope.getName() : bracket(commandRecord.scope));
			Configuration keyConfiguration = (Configuration) keyConfigurationsById.get(commandRecord.configuration);			
			tableItem.setText(2, keyConfiguration != null ? keyConfiguration.getName() : bracket(commandRecord.configuration));
			boolean conflict = commandConflict || alternateCommandConflict;
			StringBuffer stringBuffer = new StringBuffer();
	
			if (commandRecord.sequence != null)
				stringBuffer.append(KeySupport.formatSequence(commandRecord.sequence, true));
	
			if (commandConflict)
				stringBuffer.append(SPACE + COMMAND_CONFLICT);
	
			String alternateCommandName = null;
					
			if (alternateCommandId == null) 
				alternateCommandName = COMMAND_UNDEFINED;
			else if (alternateCommandId.length() == 0)
				alternateCommandName = COMMAND_NOTHING;				
			else {
				ICommandDefinition command = (ICommandDefinition) commandsById.get(alternateCommandId);
						
				if (command != null)
					alternateCommandName = command.getName();
				else
					alternateCommandName = bracket(alternateCommandId);
			}
	
			if (alternateCommandConflict)
				alternateCommandName += SPACE + COMMAND_CONFLICT;
	
			stringBuffer.append(SPACE);
	
			if (difference == DIFFERENCE_CHANGE)
				stringBuffer.append(MessageFormat.format(Util.getString(resourceBundle, "was"), new Object[] { alternateCommandName })); //$NON-NLS-1$
			else if (difference == DIFFERENCE_MINUS)
				stringBuffer.append(MessageFormat.format(Util.getString(resourceBundle, "now"), new Object[] { alternateCommandName })); //$NON-NLS-1$
	
			tableItem.setText(3, stringBuffer.toString());				
	
			if (difference == DIFFERENCE_MINUS) {
				if (conflict)
					tableItem.setForeground(new Color(getShell().getDisplay(), RGB_CONFLICT_MINUS));	
				else 
					tableItem.setForeground(new Color(getShell().getDisplay(), RGB_MINUS));	
			} else if (conflict)
				tableItem.setForeground(new Color(getShell().getDisplay(), RGB_CONFLICT));	
		}			
	}
		
	private void buildTableKeySequence() {
		tableCommandsForSequence.removeAll();
		
		for (int i = 0; i < keySequenceRecords.size(); i++) {
			KeySequenceRecord keySequenceRecord = (KeySequenceRecord) keySequenceRecords.get(i);
			int difference = DIFFERENCE_NONE;
			String commandId = null;
			boolean commandConflict = false;
			String alternateCommandId = null;
			boolean alternateCommandConflict = false;
	
			if (keySequenceRecord.customSet.isEmpty()) {
				commandId = keySequenceRecord.defaultCommand;															
				commandConflict = keySequenceRecord.defaultConflict;
			} else {
				commandId = keySequenceRecord.customCommand;															
				commandConflict = keySequenceRecord.customConflict;						
	
				if (keySequenceRecord.defaultSet.isEmpty())
					difference = DIFFERENCE_ADD;
				else {
					difference = DIFFERENCE_CHANGE;									
					alternateCommandId = keySequenceRecord.defaultCommand;
					alternateCommandConflict = keySequenceRecord.defaultConflict;																		
				}
			}
	
			TableItem tableItem = new TableItem(tableCommandsForSequence, SWT.NULL);					
	
			switch (difference) {
				case DIFFERENCE_ADD:
					tableItem.setImage(0, IMAGE_PLUS);
					break;
		
				case DIFFERENCE_CHANGE:
					tableItem.setImage(0, IMAGE_CHANGE);
					break;
		
				case DIFFERENCE_MINUS:
					tableItem.setImage(0, IMAGE_MINUS);
					break;
		
				case DIFFERENCE_NONE:
					tableItem.setImage(0, IMAGE_BLANK);
					break;				
			}
	
			IContextDefinition scope = (IContextDefinition) contextsById.get(keySequenceRecord.scope);
			tableItem.setText(1, scope != null ? scope.getName() : bracket(keySequenceRecord.scope));
			Configuration keyConfiguration = (Configuration) keyConfigurationsById.get(keySequenceRecord.configuration);			
			tableItem.setText(2, keyConfiguration != null ? keyConfiguration.getName() : bracket(keySequenceRecord.configuration));
			boolean conflict = commandConflict || alternateCommandConflict;
			StringBuffer stringBuffer = new StringBuffer();
			String commandName = null;
						
			if (commandId == null) 
				commandName = COMMAND_UNDEFINED;
			else if (commandId.length() == 0)
				commandName = COMMAND_NOTHING;				
			else {
				ICommandDefinition command = (ICommandDefinition) commandsById.get(commandId);
							
				if (command != null)
					commandName = command.getName();
				else
					commandName = bracket(commandId);
			}
				
			stringBuffer.append(commandName);
	
			if (commandConflict)
				stringBuffer.append(SPACE + COMMAND_CONFLICT);
	
			String alternateCommandName = null;
					
			if (alternateCommandId == null) 
				alternateCommandName = COMMAND_UNDEFINED;
			else if (alternateCommandId.length() == 0)
				alternateCommandName = COMMAND_NOTHING;				
			else {
				ICommandDefinition command = (ICommandDefinition) commandsById.get(alternateCommandId);
						
				if (command != null)
					alternateCommandName = command.getName();
				else
					alternateCommandName = bracket(alternateCommandId);
			}
	
			if (alternateCommandConflict)
				alternateCommandName += SPACE + COMMAND_CONFLICT;
	
			stringBuffer.append(SPACE);
				
			if (difference == DIFFERENCE_CHANGE)
				stringBuffer.append(MessageFormat.format(Util.getString(resourceBundle, "was"), new Object[] { alternateCommandName })); //$NON-NLS-1$
	
			tableItem.setText(3, stringBuffer.toString());
	
			if (difference == DIFFERENCE_MINUS) {
				if (conflict)
					tableItem.setForeground(new Color(getShell().getDisplay(), RGB_CONFLICT_MINUS));	
				else 
					tableItem.setForeground(new Color(getShell().getDisplay(), RGB_MINUS));	
			} else if (conflict)
				tableItem.setForeground(new Color(getShell().getDisplay(), RGB_CONFLICT));	
		}
	}

	private void selectTableCommand(String scopeId, String keyConfigurationId, KeySequence keySequence) {	
		int selection = -1;
			
		for (int i = 0; i < commandRecords.size(); i++) {
			CommandRecord commandRecord = (CommandRecord) commandRecords.get(i);			
				
			if (Util.equals(scopeId, commandRecord.scope) && Util.equals(keyConfigurationId, commandRecord.configuration) && Util.equals(keySequence, commandRecord.sequence)) {
				selection = i;
				break;			
			}			
		}
	
		if (tableSequencesForCommand.getSelectionCount() > 1)
			tableSequencesForCommand.deselectAll();
	
		if (selection != tableSequencesForCommand.getSelectionIndex()) {
			if (selection == -1 || selection >= tableSequencesForCommand.getItemCount())
				tableSequencesForCommand.deselectAll();
			else
				tableSequencesForCommand.select(selection);
		}
	}
	
	private void selectTableKeySequence(String scopeId, String keyConfigurationId) {		
		int selection = -1;
			
		for (int i = 0; i < keySequenceRecords.size(); i++) {
			KeySequenceRecord keySequenceRecord = (KeySequenceRecord) keySequenceRecords.get(i);			
				
			if (Util.equals(scopeId, keySequenceRecord.scope) && Util.equals(keyConfigurationId, keySequenceRecord.configuration)) {
				selection = i;
				break;			
			}			
		}
	
		if (tableCommandsForSequence.getSelectionCount() > 1)
			tableCommandsForSequence.deselectAll();
	
		if (selection != tableCommandsForSequence.getSelectionIndex()) {
			if (selection == -1 || selection >= tableCommandsForSequence.getItemCount())
				tableCommandsForSequence.deselectAll();
			else
				tableCommandsForSequence.select(selection);
		}
	}
	
	private CommandRecord getSelectedCommandRecord() {		
		int selection = tableSequencesForCommand.getSelectionIndex();
			
		if (selection >= 0 && selection < commandRecords.size() && tableSequencesForCommand.getSelectionCount() == 1)
			return (CommandRecord) commandRecords.get(selection);
		else
			return null;
	}
	
	private KeySequenceRecord getSelectedKeySequenceRecord() {		
		int selection = tableCommandsForSequence.getSelectionIndex();
			
		if (selection >= 0 && selection < keySequenceRecords.size() && tableCommandsForSequence.getSelectionCount() == 1)
			return (KeySequenceRecord) keySequenceRecords.get(selection);
		else
			return null;
	}
	*/
}
