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
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;
import org.eclipse.ui.keys.ParseException;

public class KeysPreferencePage extends org.eclipse.jface.preference.PreferencePage
	implements IWorkbenchPreferencePage {

	private final static ResourceBundle resourceBundle = ResourceBundle.getBundle(KeysPreferencePage.class.getName());

	private final static String COMMAND_CONFLICT = Util.getString(resourceBundle, "commandConflict"); //$NON-NLS-1$
	private final static String COMMAND_NOTHING = Util.getString(resourceBundle, "commandNothing"); //$NON-NLS-1$
	private final static String COMMAND_UNDEFINED = Util.getString(resourceBundle, "commandUndefined"); //$NON-NLS-1$
	private final static int DIFFERENCE_ADD = 0;	
	private final static int DIFFERENCE_CHANGE = 1;	
	private final static int DIFFERENCE_MINUS = 2;	
	private final static int DIFFERENCE_NONE = 3;	
	private final static Image IMAGE_BLANK = ImageFactory.getImage("blank"); //$NON-NLS-1$
	private final static Image IMAGE_CHANGE = ImageFactory.getImage("change"); //$NON-NLS-1$
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

	private static boolean validateKeySequence(KeySequence keySequence) {
		List keyStrokes = keySequence.getKeyStrokes();
		int size = keyStrokes.size();
			
		if (size == 0)
			return false;
		else 
			for (int i = 0; i < size; i++) {
				KeyStroke keyStroke = (KeyStroke) keyStrokes.get(i);	
	
				if (!validateKeyStroke(keyStroke))
					return false;
			}
			
		return true;
	}

	private static boolean validateKeyStroke(KeyStroke keyStroke) {
		return keyStroke.getNaturalKey().equals("\u0000");
	}

	private Button buttonAdd;
	private Button buttonRemove;
	private Button buttonRestore;
	private List categoryDefinitions;
	private Map categoryDefinitionsById;
	private Map categoryDefinitionsByName;
	private Combo comboCategory;
	private Combo comboCommand;	
	private Combo comboContext;
	private Combo comboKeyConfiguration;	
	private Combo comboKeySequence;
	private List commandDefinitions;
	private SortedMap commandDefinitionsById;
	private SortedMap commandDefinitionsByName;
	private CommandManager commandManager;
	private List contextDefinitions;
	private Map contextDefinitionsById;
	private Map contextDefinitionsByName;
	private ContextManager contextManager;
	private List keyConfigurationDefinitions;
	private Map keyConfigurationDefinitionsById;
	private Map keyConfigurationDefinitionsByName;
	private Label labelCategory; 	
	private Label labelCommand;
	private Label labelCommandGroup; 
	private Label labelCommandsForKeySequence;
	private Label labelContext; 
	private Label labelDescription;
	private Label labelKeyConfiguration;
	private Label labelKeySequence;
	private Label labelKeySequencesForCommand;			
	private Table tableCommandsForKeySequence;
	private Table tableKeySequencesForCommand;
	private Text textDescription; 
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

		// TODO remove
		//System.out.println("outgoing: " + preferenceKeyBindingDefinitions);
		
		preferenceCommandRegistry.setKeyBindingDefinitions(preferenceKeyBindingDefinitions);
		
		try {
			preferenceCommandRegistry.save();
		} catch (IOException eIO) {
		}

		// TODO ok to remove?
		if (workbench instanceof Workbench)
			((Workbench) workbench).updateActiveKeyBindingService();

		return super.performOk();
	}

	public void setVisible(boolean visible) {
		if (visible == true) {
			ICommandRegistry pluginCommandRegistry = commandManager.getPluginCommandRegistry();	
			IContextRegistry pluginContextRegistry = contextManager.getPluginContextRegistry();			
			ICommandRegistry preferenceCommandRegistry = commandManager.getPreferenceCommandRegistry();
			IContextRegistry preferenceContextRegistry = contextManager.getPreferenceContextRegistry();	
			boolean categoryDefinitionsChanged = false;
			List categoryDefinitions = new ArrayList();
			categoryDefinitions.addAll(pluginCommandRegistry.getCategoryDefinitions());
			categoryDefinitions.addAll(preferenceCommandRegistry.getCategoryDefinitions());
			CommandManager.validateCategoryDefinitions(categoryDefinitions);
	
			if (!Util.equals(categoryDefinitions, this.categoryDefinitions)) {
				this.categoryDefinitions = Collections.unmodifiableList(categoryDefinitions);
				categoryDefinitionsById = Collections.unmodifiableSortedMap(new TreeMap(CategoryDefinition.categoryDefinitionsById(this.categoryDefinitions, false)));
				categoryDefinitionsByName = Collections.unmodifiableSortedMap(new TreeMap(CategoryDefinition.categoryDefinitionsByName(this.categoryDefinitions, false)));
				categoryDefinitionsChanged = true;
			}
	
			if (categoryDefinitionsChanged) {
				List names = new ArrayList();
				Iterator iterator = this.categoryDefinitions.iterator();
				
				while (iterator.hasNext()) {
					ICategoryDefinition categoryDefinition = (ICategoryDefinition) iterator.next();
					
					if (categoryDefinition != null) {
						String name = categoryDefinition.getName();
						names.add(name);
					}
				}				

				Collections.sort(names, Collator.getInstance());						
				comboCategory.setItems((String[]) names.toArray(new String[names.size()]));
			}
	
			boolean commandDefinitionsChanged = false;
			List commandDefinitions = new ArrayList();
			commandDefinitions.addAll(pluginCommandRegistry.getCommandDefinitions());
			commandDefinitions.addAll(preferenceCommandRegistry.getCommandDefinitions());
			CommandManager.validateCommandDefinitions(commandDefinitions);
	
			if (!Util.equals(commandDefinitions, this.commandDefinitions)) {
				this.commandDefinitions = Collections.unmodifiableList(commandDefinitions);
				commandDefinitionsById = Collections.unmodifiableSortedMap(new TreeMap(CommandDefinition.commandDefinitionsById(this.commandDefinitions, false)));
				commandDefinitionsByName = Collections.unmodifiableSortedMap(new TreeMap(CommandDefinition.commandDefinitionsByName(this.commandDefinitions, false)));
				commandDefinitionsChanged = true;
			}

			if (commandDefinitionsChanged) {
				List names = new ArrayList();
				Iterator iterator = this.commandDefinitions.iterator();
				
				while (iterator.hasNext()) {
					ICommandDefinition commandDefinition = (ICommandDefinition) iterator.next();
					
					if (commandDefinition != null) {
						String name = commandDefinition.getName();
						names.add(name);
					}
				}				

				Collections.sort(names, Collator.getInstance());						
				comboCommand.setItems((String[]) names.toArray(new String[names.size()]));
			}	

			boolean contextDefinitionsChanged = false;
			List contextDefinitions = new ArrayList();
			contextDefinitions.addAll(pluginContextRegistry.getContextDefinitions());
			contextDefinitions.addAll(preferenceContextRegistry.getContextDefinitions());
			ContextManager.validateContextDefinitions(contextDefinitions);
	
			if (!Util.equals(contextDefinitions, this.contextDefinitions)) {
				this.contextDefinitions = Collections.unmodifiableList(contextDefinitions);
				contextDefinitionsById = Collections.unmodifiableSortedMap(new TreeMap(ContextDefinition.contextDefinitionsById(this.contextDefinitions, false)));
				contextDefinitionsByName = Collections.unmodifiableSortedMap(new TreeMap(ContextDefinition.contextDefinitionsByName(this.contextDefinitions, false)));
				contextDefinitionsChanged = true;
			}

			if (contextDefinitionsChanged) {
				List names = new ArrayList();
				Iterator iterator = this.contextDefinitions.iterator();
				
				while (iterator.hasNext()) {
					IContextDefinition contextDefinition = (IContextDefinition) iterator.next();
					
					if (contextDefinition != null) {
						String name = contextDefinition.getName();
						String parentId = contextDefinition.getParentId();
					
						if (parentId != null) {
							contextDefinition = (IContextDefinition) contextDefinitionsById.get(parentId);
						
							if (contextDefinition != null)
								name = MessageFormat.format(Util.getString(resourceBundle, "extends"), new Object[] { name, contextDefinition.getName() }); //$NON-NLS-1$
						}

						names.add(name);
					}
				}				

				Collections.sort(names, Collator.getInstance());						
				comboContext.setItems((String[]) names.toArray(new String[names.size()]));
			}		

			boolean keyConfigurationDefinitionsChanged = false;
			List keyConfigurationDefinitions = new ArrayList();
			keyConfigurationDefinitions.addAll(pluginCommandRegistry.getKeyConfigurationDefinitions());
			keyConfigurationDefinitions.addAll(preferenceCommandRegistry.getKeyConfigurationDefinitions());
			CommandManager.validateKeyConfigurationDefinitions(keyConfigurationDefinitions);
	
			if (!Util.equals(keyConfigurationDefinitions, this.keyConfigurationDefinitions)) {
				this.keyConfigurationDefinitions = Collections.unmodifiableList(keyConfigurationDefinitions);
				keyConfigurationDefinitionsById = Collections.unmodifiableSortedMap(new TreeMap(KeyConfigurationDefinition.keyConfigurationDefinitionsById(this.keyConfigurationDefinitions, false)));
				keyConfigurationDefinitionsByName = Collections.unmodifiableSortedMap(new TreeMap(KeyConfigurationDefinition.keyConfigurationDefinitionsByName(this.keyConfigurationDefinitions, false)));
				keyConfigurationDefinitionsChanged = true;
			}

			if (keyConfigurationDefinitionsChanged) {
				List names = new ArrayList();
				Iterator iterator = this.keyConfigurationDefinitions.iterator();
				
				while (iterator.hasNext()) {
					IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) iterator.next();
					
					if (keyConfigurationDefinition != null) {
						String name = keyConfigurationDefinition.getName();
						String parentId = keyConfigurationDefinition.getParentId();
					
						if (parentId != null) {
							keyConfigurationDefinition = (IKeyConfigurationDefinition) keyConfigurationDefinitionsById.get(parentId);
						
							if (keyConfigurationDefinition != null)
								name = MessageFormat.format(Util.getString(resourceBundle, "extends"), new Object[] { name, keyConfigurationDefinition.getName() }); //$NON-NLS-1$
						}

						names.add(name);
					}
				}				

				Collections.sort(names, Collator.getInstance());								
				comboKeyConfiguration.setItems((String[]) names.toArray(new String[names.size()]));
			}	

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
			
			setKeyConfigurationId(activeKeyConfigurationId);
			List pluginKeyBindingDefinitions = new ArrayList(pluginCommandRegistry.getKeyBindingDefinitions());
			CommandManager.validateKeyBindingDefinitions(pluginKeyBindingDefinitions);
			List preferenceKeyBindingDefinitions = new ArrayList(preferenceCommandRegistry.getKeyBindingDefinitions());
			
			// TODO remove
			//System.out.println("incoming: " + preferenceKeyBindingDefinitions);
			
			CommandManager.validateKeyBindingDefinitions(preferenceKeyBindingDefinitions);
			tree.clear();
			Iterator iterator = preferenceKeyBindingDefinitions.iterator();
			
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
		composite.setFont(parent.getFont());
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.numColumns = 2;
		composite.setLayout(gridLayout);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gridData);

		labelKeyConfiguration = new Label(composite, SWT.LEFT);
		labelKeyConfiguration.setFont(composite.getFont());
		labelKeyConfiguration.setText(Util.getString(resourceBundle, "labelKeyConfiguration")); //$NON-NLS-1$

		comboKeyConfiguration = new Combo(composite, SWT.READ_ONLY);
		comboKeyConfiguration.setFont(composite.getFont());
		gridData = new GridData();
		gridData.widthHint = 250;
		comboKeyConfiguration.setLayoutData(gridData);

		comboKeyConfiguration.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboKeyConfiguration();
			}	
		});

		Label labelSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		labelSeparator.setLayoutData(gridData);	

		labelCommandGroup = new Label(composite, SWT.LEFT);
		labelCommandGroup.setFont(composite.getFont());
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		labelCommandGroup.setLayoutData(gridData);
		labelCommandGroup.setText(Util.getString(resourceBundle, "labelCommandGroup")); //$NON-NLS-1$

		labelCategory = new Label(composite, SWT.LEFT);
		labelCategory.setFont(composite.getFont());
		gridData = new GridData();
		gridData.horizontalIndent = 50;
		labelCategory.setLayoutData(gridData);
		labelCategory.setText(Util.getString(resourceBundle, "labelCategory")); //$NON-NLS-1$

		comboCategory = new Combo(composite, SWT.READ_ONLY);
		comboCategory.setFont(composite.getFont());
		gridData = new GridData();
		gridData.widthHint = 200;
		comboCategory.setLayoutData(gridData);

		comboCategory.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboCategory();
			}	
		});

		labelCommand = new Label(composite, SWT.LEFT);
		labelCommand.setFont(composite.getFont());
		gridData = new GridData();
		gridData.horizontalIndent = 50;
		labelCommand.setLayoutData(gridData);
		labelCommand.setText(Util.getString(resourceBundle, "labelCommand")); //$NON-NLS-1$

		comboCommand = new Combo(composite, SWT.READ_ONLY);
		comboCommand.setFont(composite.getFont());
		gridData = new GridData();
		gridData.widthHint = 300;
		comboCommand.setLayoutData(gridData);

		comboCommand.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboCommand();
			}	
		});

		labelDescription = new Label(composite, SWT.LEFT);
		labelDescription.setFont(composite.getFont());
		gridData = new GridData();
		gridData.horizontalIndent = 50;
		labelDescription.setLayoutData(gridData);
		labelDescription.setText(Util.getString(resourceBundle, "labelDescription")); //$NON-NLS-1$

		textDescription = new Text(composite, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		textDescription.setFont(composite.getFont());
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		textDescription.setLayoutData(gridData);

		labelKeySequencesForCommand = new Label(composite, SWT.LEFT);
		labelKeySequencesForCommand.setFont(composite.getFont());
		gridData = new GridData();
		gridData.horizontalIndent = 50;
		gridData.horizontalSpan = 2;
		labelKeySequencesForCommand.setLayoutData(gridData);
		labelKeySequencesForCommand.setText(Util.getString(resourceBundle, "labelKeySequencesForCommand")); //$NON-NLS-1$

		tableKeySequencesForCommand = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tableKeySequencesForCommand.setFont(composite.getFont());
		tableKeySequencesForCommand.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 60;
		gridData.horizontalIndent = 50;
		gridData.horizontalSpan = 2;
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
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnContext")); //$NON-NLS-1$
		tableColumn.pack();
		tableColumn.setWidth("carbon".equals(SWT.getPlatform()) ? 100 : tableColumn.getWidth() + 40); // TODO remove carbon reference
		width += tableColumn.getWidth();

		tableColumn = new TableColumn(tableKeySequencesForCommand, SWT.NULL, 2);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnKeySequence")); //$NON-NLS-1$
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
		labelContext.setFont(composite.getFont());
		labelContext.setText(Util.getString(resourceBundle, "labelContext")); //$NON-NLS-1$

		comboContext = new Combo(composite, SWT.READ_ONLY);
		comboContext.setFont(composite.getFont());
		gridData = new GridData();
		gridData.widthHint = 250;
		comboContext.setLayoutData(gridData);

		comboContext.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboContext();
			}	
		});

		labelKeySequence = new Label(composite, SWT.LEFT);
		labelKeySequence.setFont(composite.getFont());
		labelKeySequence.setText(Util.getString(resourceBundle, "labelKeySequence")); //$NON-NLS-1$

		comboKeySequence = new Combo(composite, SWT.NULL);
		comboKeySequence.setFont(composite.getFont());
		gridData = new GridData();
		gridData.widthHint = 250;
		comboKeySequence.setLayoutData(gridData);

		comboKeySequence.addModifyListener(new ModifyListener() {			
			public void modifyText(ModifyEvent modifyEvent) {
				modifiedComboKeySequence();
			}	
		});

		comboKeySequence.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboKeySequence();
			}	
		});

		labelCommandsForKeySequence = new Label(composite, SWT.LEFT);
		labelCommandsForKeySequence.setFont(composite.getFont());
		gridData = new GridData();
		gridData.horizontalIndent = 50;
		gridData.horizontalSpan = 2;
		labelCommandsForKeySequence.setLayoutData(gridData);
		labelCommandsForKeySequence.setText(Util.getString(resourceBundle, "labelCommandsForKeySequence")); //$NON-NLS-1$

		tableCommandsForKeySequence = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tableCommandsForKeySequence.setFont(composite.getFont());
		tableCommandsForKeySequence.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 60;
		gridData.horizontalIndent = 50;
		gridData.horizontalSpan = 2;
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
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnContext")); //$NON-NLS-1$
		tableColumn.pack();
		tableColumn.setWidth("carbon".equals(SWT.getPlatform()) ? 100 : tableColumn.getWidth() + 40); // TODO remove carbon reference
		width += tableColumn.getWidth();

		tableColumn = new TableColumn(tableCommandsForKeySequence, SWT.NULL, 2);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnCommand")); //$NON-NLS-1$
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
		compositeButton.setFont(composite.getFont());
		gridLayout = new GridLayout();
		gridLayout.marginHeight = 20;
		gridLayout.marginWidth = 0;		
		gridLayout.numColumns = 3;
		compositeButton.setLayout(gridLayout);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		compositeButton.setLayoutData(gridData);
				
		buttonAdd = new Button(compositeButton, SWT.CENTER | SWT.PUSH);
		buttonAdd.setFont(compositeButton.getFont());
		gridData = new GridData();
		gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		buttonAdd.setText(Util.getString(resourceBundle, "buttonAdd")); //$NON-NLS-1$
		gridData.widthHint = Math.max(widthHint, buttonAdd.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		buttonAdd.setLayoutData(gridData);		

		buttonAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedButtonAdd();
			}	
		});

		buttonRemove = new Button(compositeButton, SWT.CENTER | SWT.PUSH);
		buttonRemove.setFont(compositeButton.getFont());
		gridData = new GridData();
		gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		buttonRemove.setText(Util.getString(resourceBundle, "buttonRemove")); //$NON-NLS-1$
		gridData.widthHint = Math.max(widthHint, buttonRemove.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		buttonRemove.setLayoutData(gridData);		

		buttonRemove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedButtonRemove();
			}	
		});

		buttonRestore = new Button(compositeButton, SWT.CENTER | SWT.PUSH);
		buttonRestore.setFont(compositeButton.getFont());
		gridData = new GridData();
		gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		buttonRestore.setText(Util.getString(resourceBundle, "buttonRestore")); //$NON-NLS-1$
		gridData.widthHint = Math.max(widthHint, buttonRestore.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		buttonRestore.setLayoutData(gridData);		
		
		buttonRestore.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedButtonRestore();
			}	
		});
				
		// TODO WorkbenchHelp.setHelp(parent, IHelpContextIds.WORKBENCH_KEY_PREFERENCE_PAGE);
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
			restoreDefaultsMessageBox.setText(Util.getString(resourceBundle, "restoreDefaultsMessageBoxText")); //$NON-NLS-1$
			restoreDefaultsMessageBox.setMessage(Util.getString(resourceBundle, "restoreDefaultsMessageBoxMessage")); //$NON-NLS-1$
		
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
		int selection = comboCategory.getSelectionIndex();
		List categoryDefinitions = new ArrayList(categoryDefinitionsByName.values());			
			
		if (selection >= 0 && selection < categoryDefinitions.size()) {
			ICategoryDefinition categoryDefinition = (ICategoryDefinition) categoryDefinitions.get(selection);
			return categoryDefinition.getId();				
		}
			
		return null;
	}
	
	private String getCommandId() {
		int selection = comboCommand.getSelectionIndex();
		List commandDefinitions = new ArrayList(commandDefinitionsByName.values());			
			
		if (selection >= 0 && selection < commandDefinitions.size()) {
			ICommandDefinition commandDefinition = (ICommandDefinition) commandDefinitions.get(selection);
			return commandDefinition.getId();				
		}
			
		return null;
	}
	
	private String getContextId() {
		int selection = comboContext.getSelectionIndex();
		List contextDefinitions = new ArrayList(contextDefinitionsByName.values());			
		
		if (selection >= 0 && selection < contextDefinitions.size()) {
			IContextDefinition contextDefinition = (IContextDefinition) contextDefinitions.get(selection);
			return contextDefinition.getId();				
		}
		
		return null;
	}

	private String getKeyConfigurationId() {
		int selection = comboKeyConfiguration.getSelectionIndex();
		List keyConfigurationDefinitions = new ArrayList(keyConfigurationDefinitionsByName.values());
		
		if (selection >= 0 && selection < keyConfigurationDefinitions.size()) {
			IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) keyConfigurationDefinitions.get(selection);
			return keyConfigurationDefinition.getId();				
		}
		
		return null;
	}

	private KeySequence getKeySequence() {
		try {
			return KeySequence.getInstance(comboKeySequence.getText());
		} catch (ParseException eParse) {
			return KeySequence.getInstance();		
		}
	}

	private void modifiedComboKeySequence() {
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

	private void selectedComboKeySequence() {
	}

	private void selectedTableCommandsForKeySequence() {
	}

	private void selectedTableKeySequencesForCommand() {
	}

	private void setCategoryId(String categoryId) {				
		comboCategory.clearSelection();
		comboCategory.deselectAll();
			
		if (categoryId != null) {
			List categoryDefinitions = new ArrayList(categoryDefinitionsByName.values());			
	
			for (int i = 0; i < categoryDefinitions.size(); i++) {
				ICategoryDefinition categoryDefinition = (ICategoryDefinition) categoryDefinitions.get(i);		
					
				if (categoryDefinition.getId().equals(categoryId)) {
					comboCategory.select(i);
					break;		
				}
			}
		}
	}
	
	private void setCommandId(String commandId) {				
		comboCommand.clearSelection();
		comboCommand.deselectAll();
			
		if (commandId != null) {
			List commandDefinitions = new ArrayList(commandDefinitionsByName.values());			
	
			for (int i = 0; i < commandDefinitions.size(); i++) {
				ICommandDefinition commandDefinition = (ICommandDefinition) commandDefinitions.get(i);		
					
				if (commandDefinition.getId().equals(commandId)) {
					comboCommand.select(i);
					break;		
				}
			}
		}
	}

	private void setContextId(String contextId) {				
		comboContext.clearSelection();
		comboContext.deselectAll();
		
		if (contextId != null) {
			List contextDefinitions = new ArrayList(contextDefinitionsByName.values());			

			for (int i = 0; i < contextDefinitions.size(); i++) {
				IContextDefinition contextDefinition = (IContextDefinition) contextDefinitions.get(i);		
				
				if (contextDefinition.getId().equals(contextId)) {
					comboContext.select(i);
					break;		
				}
			}
		}
	}

	private void setKeyConfigurationId(String keyConfigurationId) {				
		comboKeyConfiguration.clearSelection();
		comboKeyConfiguration.deselectAll();
		
		if (keyConfigurationId != null) {
			List keyConfigurationDefinitions = new ArrayList(keyConfigurationDefinitionsByName.values());
				
			for (int i = 0; i < keyConfigurationDefinitions.size(); i++) {
				IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) keyConfigurationDefinitions.get(i);		
				
				if (keyConfigurationDefinition.getId().equals(keyConfigurationId)) {
					comboKeyConfiguration.select(i);
					break;		
				}
			}
		}
	}

	private void setKeySequence(KeySequence keySequence) {
		comboKeySequence.setText(keySequence.toString());
	}

	/*
	private String bracket(String string) {
		return string != null ? '[' + string + ']' : "[]"; //$NON-NLS-1$	
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

	private SortedMap build(SortedSet sequenceBindingSet) {
		SortedMap tree = new TreeMap();
		Iterator iterator = sequenceBindingSet.iterator();
		
		while (iterator.hasNext()) {
			SequenceBinding sequenceBinding = (SequenceBinding) iterator.next();
			KeySequence sequence = sequenceBinding.getSequence();
			String configuration = sequenceBinding.getConfiguration();			
			String command = sequenceBinding.getCommand();			
			Path locale = SequenceMachine.getPathForLocale(sequenceBinding.getLocale());
			Path platform = SequenceMachine.getPathForPlatform(sequenceBinding.getPlatform());
			List paths = new ArrayList();
			paths.add(platform);
			paths.add(locale);
			State platformLocale = State.create(paths);
			Integer rank = new Integer(sequenceBinding.getRank());
			String scope = sequenceBinding.getContext();			
			SortedMap scopeMap = (SortedMap) tree.get(sequence);
			
			if (scopeMap == null) {
				scopeMap = new TreeMap();
				tree.put(sequence, scopeMap);
			}

			SortedMap configurationMap = (SortedMap) scopeMap.get(scope);
			
			if (configurationMap == null) {
				configurationMap = new TreeMap();
				scopeMap.put(scope, configurationMap);
			}

			SortedMap rankMap = (SortedMap) configurationMap.get(configuration);
		
			if (rankMap == null) {
				rankMap = new TreeMap();	
				configurationMap.put(configuration, rankMap);
			}

			SortedMap platformLocaleMap = (SortedMap) rankMap.get(rank);

			if (platformLocaleMap == null) {
				platformLocaleMap = new TreeMap();	
				rankMap.put(rank, platformLocaleMap);
			}

			Set commandSet = (Set) platformLocaleMap.get(platformLocale);

			if (commandSet == null) {
				commandSet = new HashSet();	
				platformLocaleMap.put(platformLocale, commandSet);
			}

			commandSet.add(command);										
		}

		List paths = new ArrayList();
		paths.add(SequenceMachine.getSystemPlatform());
		paths.add(SequenceMachine.getSystemLocale());
		State platformLocale = State.create(paths);
		iterator = tree.values().iterator();
		
		while (iterator.hasNext()) {
			SortedMap scopeMap = (SortedMap) iterator.next();			
			Iterator iterator2 = scopeMap.values().iterator();
			
			while (iterator2.hasNext()) {
				SortedMap configurationMap = (SortedMap) iterator2.next();			
				Iterator iterator3 = configurationMap.entrySet().iterator();
				
				while (iterator3.hasNext()) {
					Map.Entry entry = (Map.Entry) iterator3.next();
					entry.setValue(solveRankMap((SortedMap) entry.getValue(), platformLocale));
				}
			}
		}
		
		return tree;
	}

	private CommandSetPair solveRankMap(SortedMap rankMap, State platformLocale) {
		CommandSetPair commandSetPair = new CommandSetPair();		
		Iterator iterator = rankMap.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			Integer rank = (Integer) entry.getKey();
			SortedMap platformLocaleMap = (SortedMap) entry.getValue();			
			Set commandSet = solvePlatformLocaleMap(platformLocaleMap, platformLocale);

			if (rank.intValue() == 0)
				commandSetPair.customSet = commandSet;
			else if (commandSetPair.defaultSet == null)
				commandSetPair.defaultSet = commandSet;
		}

		return commandSetPair;
	}

	private Set solvePlatformLocaleMap(SortedMap platformLocaleMap, State platformLocale) {
		int bestDefinedMatch = -1;
		Set bestDefinedCommandSet = null;		
		int bestUndefinedMatch = -1;
		Set bestUndefinedCommandSet = null;		
		Iterator iterator = platformLocaleMap.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			State testPlatformLocale = (State) entry.getKey();
			Set testCommandSet = (Set) entry.getValue();
			int testMatch = testPlatformLocale.match(platformLocale);

			if (testMatch >= 0) {
				String testCommand = SequenceNode.solveCommandSet(testCommandSet);

				if (testCommand != null) {
					if (bestDefinedMatch == -1 || testMatch < bestDefinedMatch) {
						bestDefinedMatch = testMatch;
						bestDefinedCommandSet = testCommandSet;
					}	
				} else {
					if (bestUndefinedMatch == -1 || testMatch < bestUndefinedMatch) {
						bestUndefinedMatch = testMatch;
						bestUndefinedCommandSet = testCommandSet;
					}					
				}
			}	
		}

		return bestDefinedMatch >= 0 ? bestDefinedCommandSet : bestUndefinedCommandSet; 
	}

	private SortedSet solve(SortedMap tree) {
		SortedSet sequenceBindingSet = new TreeSet();		
		Iterator iterator = tree.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();	
			KeySequence sequence = (KeySequence) entry.getKey();
			SortedMap scopeMap = (SortedMap) entry.getValue();			
			Iterator iterator2 = scopeMap.entrySet().iterator();
			
			while (iterator2.hasNext()) {
				Map.Entry entry2 = (Map.Entry) iterator2.next();	
				String scope = (String) entry2.getKey();
				SortedMap configurationMap = (SortedMap) entry2.getValue();
				Iterator iterator3 = configurationMap.entrySet().iterator();
				
				while (iterator3.hasNext()) {
					Map.Entry entry3 = (Map.Entry) iterator3.next();					
					String configuration = (String) entry3.getKey();
					CommandSetPair commandSetPair = (CommandSetPair) entry3.getValue();
					Set customSet = commandSetPair.customSet;
					
					if (customSet != null) {
						Iterator iterator4 = customSet.iterator();
						
						while (iterator4.hasNext()) {
							String command = (String) iterator4.next();
							sequenceBindingSet.add(SequenceBinding.create(command, configuration, scope, Util.ZERO_LENGTH_STRING, Util.ZERO_LENGTH_STRING, null, 0, sequence));									
						}
					}
				}
			}
		}
		
		return sequenceBindingSet;		
	}
	*/
}
