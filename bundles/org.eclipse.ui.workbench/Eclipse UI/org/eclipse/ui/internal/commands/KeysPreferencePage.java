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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.commands.api.IActiveKeyConfigurationDefinition;
import org.eclipse.ui.internal.commands.api.ICategoryDefinition;
import org.eclipse.ui.internal.commands.api.ICommandDefinition;
import org.eclipse.ui.internal.commands.api.ICommandRegistry;
import org.eclipse.ui.internal.commands.api.IContextBindingDefinition;
import org.eclipse.ui.internal.commands.api.IImageBindingDefinition;
import org.eclipse.ui.internal.commands.api.IKeyBindingDefinition;
import org.eclipse.ui.internal.commands.api.IKeyConfigurationDefinition;
import org.eclipse.ui.internal.contexts.ContextDefinition;
import org.eclipse.ui.internal.contexts.ContextManager;
import org.eclipse.ui.internal.contexts.api.IContextDefinition;
import org.eclipse.ui.internal.contexts.api.IContextRegistry;
import org.eclipse.ui.internal.keys.KeySequenceText;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;

public class KeysPreferencePage extends org.eclipse.jface.preference.PreferencePage
	implements IWorkbenchPreferencePage {

	private final static class CommandAssignment implements Comparable {

		private KeyBindingNode.Assignment assignment;		
		private String contextId;
		private KeySequence keySequence;		
		
		public int compareTo(Object object) {
			CommandAssignment commandAssignment = (CommandAssignment) object;
			int compareTo = Util.compare(contextId, commandAssignment.contextId);
	
			if (compareTo == 0) {
				compareTo = Util.compare(keySequence, commandAssignment.keySequence);	
			
				if (compareTo == 0)
					compareTo = Util.compare(assignment, commandAssignment.assignment);
			}
		
			return compareTo;	
		}
		
		public boolean equals(Object object) {
			if (!(object instanceof CommandAssignment))
				return false;

			CommandAssignment commandAssignment = (CommandAssignment) object;	
			boolean equals = true;
			equals &= Util.equals(assignment, commandAssignment.assignment);
			equals &= Util.equals(contextId, commandAssignment.contextId);
			equals &= Util.equals(keySequence, commandAssignment.keySequence);
			return equals;
		}
	}
		
	private final static class KeySequenceAssignment implements Comparable {
		
		private KeyBindingNode.Assignment assignment;		
		private String contextId;
	
		public int compareTo(Object object) {
			KeySequenceAssignment keySequenceAssignment = (KeySequenceAssignment) object;
			int compareTo = Util.compare(contextId, keySequenceAssignment.contextId);
	
			if (compareTo == 0)
				compareTo = Util.compare(assignment, keySequenceAssignment.assignment);
		
			return compareTo;	
		}
		
		public boolean equals(Object object) {
			if (!(object instanceof CommandAssignment))
				return false;

			KeySequenceAssignment keySequenceAssignment = (KeySequenceAssignment) object;	
			boolean equals = true;
			equals &= Util.equals(assignment, keySequenceAssignment.assignment);
			equals &= Util.equals(contextId, keySequenceAssignment.contextId);
			return equals;
		}	
	}

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

	private Map assignmentsByContextIdByKeySequence;
	private Button buttonAdd;
	private Button buttonRemove;
	private Button buttonRestore;
	private Map categoryDefinitionsById;
	private Map categoryIdsByUniqueName;
	private Map categoryUniqueNamesById;
	private Combo comboCategory;
	private Combo comboCommand;	
	private Combo comboContext;
	private Combo comboKeyConfiguration;
	private Set commandAssignments;	
	private Map commandDefinitionsById;
	private Map commandIdsByCategoryId;
	private Map commandIdsByUniqueName;
	private Map commandUniqueNamesById;
	private CommandManager commandManager;
	private Map contextDefinitionsById;
	private Map contextIdsByCommandId;
	private Map contextIdsByUniqueName;
	private Map contextUniqueNamesById;
	private ContextManager contextManager;
	private Group groupCommand; 
	private Group groupKeySequence; 
	private Map keyConfigurationDefinitionsById;
	private Map keyConfigurationIdsByUniqueName;
	private Map keyConfigurationUniqueNamesById;
	private Set keySequenceAssignments;
	private Label labelAssignmentsForCommand;
	private Label labelAssignmentsForKeySequence;
	private Label labelCategory; 	
	private Label labelCommand;
	private Label labelContext; 
	private Label labelContextExtends;
	private Label labelKeyConfiguration;
	private Label labelKeyConfigurationExtends;
	private Label labelKeySequence;	
	private Table tableAssignmentsForCommand;
	private Table tableAssignmentsForKeySequence;
	private KeySequenceText textKeySequence;
	private SortedMap tree;
	private IWorkbench workbench;	
		
	public void init(IWorkbench workbench) {
		this.workbench = workbench;
		commandManager = CommandManager.getInstance();
		contextManager = ContextManager.getInstance();
		commandAssignments = new TreeSet();
		keySequenceAssignments = new TreeSet();
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

		// TODO remove the dependancy on Workbench. have Workbench rely on events from CommandManager.
		if (workbench instanceof Workbench) {
			((Workbench) workbench).updateActiveContextIds();
			((Workbench) workbench).updateActiveWorkbenchWindowMenuManager();
		}

		return super.performOk();
	}

	public void setVisible(boolean visible) {
		if (visible == true) {
			ICommandRegistry pluginCommandRegistry = commandManager.getPluginCommandRegistry();				
			ICommandRegistry preferenceCommandRegistry = commandManager.getPreferenceCommandRegistry();
			List categoryDefinitions = new ArrayList();
			categoryDefinitions.addAll(pluginCommandRegistry.getCategoryDefinitions());
			categoryDefinitions.addAll(preferenceCommandRegistry.getCategoryDefinitions());
			categoryDefinitionsById = CategoryDefinition.categoryDefinitionsById(categoryDefinitions, false);			
			
			for (Iterator iterator = categoryDefinitionsById.values().iterator(); iterator.hasNext();) {
				ICategoryDefinition categoryDefinition = (ICategoryDefinition) iterator.next();
				String name = categoryDefinition.getName();
				
				if (name == null || name.length() == 0)
					iterator.remove();
			}			
			
			List commandDefinitions = new ArrayList();
			commandDefinitions.addAll(pluginCommandRegistry.getCommandDefinitions());
			commandDefinitions.addAll(preferenceCommandRegistry.getCommandDefinitions());
			commandDefinitionsById = CommandDefinition.commandDefinitionsById(commandDefinitions, false);
			commandIdsByCategoryId = new HashMap();
			HashSet categoryIdsReferencedByCommandDefinitions = new HashSet();

			for (Iterator iterator = commandDefinitionsById.values().iterator(); iterator.hasNext();) {
				ICommandDefinition commandDefinition = (ICommandDefinition) iterator.next();
				String categoryId = commandDefinition.getCategoryId();
				String name = commandDefinition.getName();

				if (name == null || name.length() == 0 || categoryId != null && !categoryDefinitionsById.containsKey(categoryId))
					iterator.remove();
				else {
					String commandId = commandDefinition.getId();
					Set commandIds = (Set) commandIdsByCategoryId.get(categoryId);
				
					if (commandIds == null) {
						commandIds = new HashSet();
						commandIdsByCategoryId.put(categoryId, commandIds);
					}
				
					commandIds.add(commandId);
					categoryIdsReferencedByCommandDefinitions.add(categoryId);				
				}
			}
			
			categoryDefinitionsById.keySet().retainAll(categoryIdsReferencedByCommandDefinitions);			
			List keyConfigurationDefinitions = new ArrayList();
			keyConfigurationDefinitions.addAll(pluginCommandRegistry.getKeyConfigurationDefinitions());
			keyConfigurationDefinitions.addAll(preferenceCommandRegistry.getKeyConfigurationDefinitions());			
			keyConfigurationDefinitionsById = KeyConfigurationDefinition.keyConfigurationDefinitionsById(keyConfigurationDefinitions, false);

			for (Iterator iterator = keyConfigurationDefinitionsById.values().iterator(); iterator.hasNext();) {
				IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) iterator.next();		
				String name = keyConfigurationDefinition.getName();
				
				if (name == null || name.length() == 0)
					iterator.remove();
			}

			for (Iterator iterator = keyConfigurationDefinitionsById.values().iterator(); iterator.hasNext();) {
				IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) iterator.next();				
				String parentId = keyConfigurationDefinition.getParentId();
				
				// TODO this does not prevent circular graphs higher up the hierarchy
				while (parentId != null) {
					keyConfigurationDefinition = (IKeyConfigurationDefinition) keyConfigurationDefinitionsById.get(parentId);
				
					if (keyConfigurationDefinition == null) {
						iterator.remove();
						break;
					}
					
					parentId = keyConfigurationDefinition.getParentId();
				} 
			}

			List activeKeyConfigurationDefinitions = new ArrayList();
			activeKeyConfigurationDefinitions.addAll(pluginCommandRegistry.getActiveKeyConfigurationDefinitions());
			activeKeyConfigurationDefinitions.addAll(preferenceCommandRegistry.getActiveKeyConfigurationDefinitions());
			String activeKeyConfigurationId = null;
			
			if (!activeKeyConfigurationDefinitions.isEmpty()) {
				IActiveKeyConfigurationDefinition activeKeyConfigurationDefinition = (IActiveKeyConfigurationDefinition) activeKeyConfigurationDefinitions.get(activeKeyConfigurationDefinitions.size() - 1);
				activeKeyConfigurationId = activeKeyConfigurationDefinition.getKeyConfigurationId();
				
				if (!keyConfigurationDefinitionsById.containsKey(activeKeyConfigurationId))
					activeKeyConfigurationId = null;
			}

			IContextRegistry pluginContextRegistry = contextManager.getPluginContextRegistry();			
			IContextRegistry preferenceContextRegistry = contextManager.getPreferenceContextRegistry();	
			List contextDefinitions = new ArrayList();
			contextDefinitions.addAll(pluginContextRegistry.getContextDefinitions());
			contextDefinitions.addAll(preferenceContextRegistry.getContextDefinitions());
			contextDefinitionsById = ContextDefinition.contextDefinitionsById(contextDefinitions, false);

			for (Iterator iterator = contextDefinitionsById.values().iterator(); iterator.hasNext();) {
				IContextDefinition contextDefinition = (IContextDefinition) iterator.next();
				String name = contextDefinition.getName();
				
				if (name == null || name.length() == 0)
					iterator.remove();
			}

			for (Iterator iterator = contextDefinitionsById.values().iterator(); iterator.hasNext();) {
				IContextDefinition contextDefinition = (IContextDefinition) iterator.next();
				String parentId = contextDefinition.getParentId();
					
				// TODO this does not prevent circular graphs higher up the hierarchy
				while (parentId != null) {
					contextDefinition = (IContextDefinition) contextDefinitionsById.get(parentId);
					
					if (contextDefinition == null) {
						iterator.remove();
						break;
					}
						
					parentId = contextDefinition.getParentId();
				} 
			}

			List contextBindingDefinitions = new ArrayList();
			contextBindingDefinitions.addAll(pluginCommandRegistry.getContextBindingDefinitions());
			contextBindingDefinitions.addAll(preferenceCommandRegistry.getContextBindingDefinitions());
			contextIdsByCommandId = new HashMap();
			
			for (Iterator iterator = contextBindingDefinitions.iterator(); iterator.hasNext();) {
				IContextBindingDefinition contextBindingDefinition = (IContextBindingDefinition) iterator.next();
				String commandId = contextBindingDefinition.getCommandId();
				String contextId = contextBindingDefinition.getContextId();
				boolean validCommandId = commandDefinitionsById.containsKey(commandId);
				boolean validContextId = contextDefinitionsById.containsKey(contextId);
				
				if (!validCommandId || !validContextId)
					iterator.remove();
				else {					
					Set contextIds = (Set) contextIdsByCommandId.get(commandId);
				
					if (contextIds == null) {
						contextIds = new HashSet();
						contextIdsByCommandId.put(commandId, contextIds);
					}

					contextIds.add(contextId);					
				}			
			}

			List imageBindingDefinitions = new ArrayList();
			imageBindingDefinitions.addAll(pluginCommandRegistry.getImageBindingDefinitions());
			imageBindingDefinitions.addAll(preferenceCommandRegistry.getImageBindingDefinitions());

			for (Iterator iterator = imageBindingDefinitions.iterator(); iterator.hasNext();) {
				IImageBindingDefinition imageBindingDefinition = (IImageBindingDefinition) iterator.next();
				String commandId = imageBindingDefinition.getCommandId();
				boolean validCommandId = commandId == null || commandDefinitionsById.containsKey(commandId);
				
				if (!validCommandId)
					iterator.remove();
			}

			List pluginKeyBindingDefinitions = new ArrayList(pluginCommandRegistry.getKeyBindingDefinitions());

			for (Iterator iterator = pluginKeyBindingDefinitions.iterator(); iterator.hasNext();) {
				IKeyBindingDefinition keyBindingDefinition = (IKeyBindingDefinition) iterator.next();				
				KeySequence keySequence = keyBindingDefinition.getKeySequence();
				String commandId = keyBindingDefinition.getCommandId();
				String contextId = keyBindingDefinition.getContextId();
				String keyConfigurationId = keyBindingDefinition.getKeyConfigurationId();
				Set contextIds = (Set) contextIdsByCommandId.get(commandId);
				boolean validKeySequence = keySequence != null && CommandManager.validateKeySequence(keySequence);
				boolean validCommandId = commandId == null || commandDefinitionsById.containsKey(commandId);							
				boolean validContextId = contextId == null || contextDefinitionsById.containsKey(contextId);
				boolean validKeyConfigurationId = keyConfigurationId == null || keyConfigurationDefinitionsById.containsKey(keyConfigurationId);
				boolean validContextIdForCommandId = contextIds == null || contextIds.contains(contextId);
			
				if (!validKeySequence || !validCommandId || !validContextId || !validKeyConfigurationId || !validContextIdForCommandId)
					iterator.remove();
			}

			List preferenceKeyBindingDefinitions = new ArrayList(preferenceCommandRegistry.getKeyBindingDefinitions());

			for (Iterator iterator = preferenceKeyBindingDefinitions.iterator(); iterator.hasNext();) {
				IKeyBindingDefinition keyBindingDefinition = (IKeyBindingDefinition) iterator.next();				
				KeySequence keySequence = keyBindingDefinition.getKeySequence();
				String commandId = keyBindingDefinition.getCommandId();
				String contextId = keyBindingDefinition.getContextId();
				String keyConfigurationId = keyBindingDefinition.getKeyConfigurationId();
				Set contextIds = (Set) contextIdsByCommandId.get(commandId);
				boolean validKeySequence = keySequence != null && CommandManager.validateKeySequence(keySequence);
				boolean validCommandId = commandId == null || commandDefinitionsById.containsKey(commandId);							
				boolean validContextId = contextId == null || contextDefinitionsById.containsKey(contextId);
				boolean validKeyConfigurationId = keyConfigurationId == null || keyConfigurationDefinitionsById.containsKey(keyConfigurationId);
				boolean validContextIdForCommandId = contextIds == null || contextIds.contains(contextId);
			
				if (!validKeySequence || !validCommandId || !validContextId || !validKeyConfigurationId || !validContextIdForCommandId)
					iterator.remove();
			}

			tree = new TreeMap();
			
			for (Iterator iterator = pluginKeyBindingDefinitions.iterator(); iterator.hasNext();) {
				IKeyBindingDefinition keyBindingDefinition = (IKeyBindingDefinition) iterator.next();				
				KeyBindingNode.add(tree, keyBindingDefinition.getKeySequence(), keyBindingDefinition.getContextId(), keyBindingDefinition.getKeyConfigurationId(), 1, keyBindingDefinition.getPlatform(), keyBindingDefinition.getLocale(), keyBindingDefinition.getCommandId());
			}

			for (Iterator iterator = preferenceKeyBindingDefinitions.iterator(); iterator.hasNext();) {
				IKeyBindingDefinition keyBindingDefinition = (IKeyBindingDefinition) iterator.next();				
				KeyBindingNode.add(tree, keyBindingDefinition.getKeySequence(), keyBindingDefinition.getContextId(), keyBindingDefinition.getKeyConfigurationId(), 0, keyBindingDefinition.getPlatform(), keyBindingDefinition.getLocale(), keyBindingDefinition.getCommandId());
			}			
		
			Map categoryDefinitionsByName = CategoryDefinition.categoryDefinitionsByName(categoryDefinitionsById.values(), false);
			categoryIdsByUniqueName = new HashMap();
			categoryUniqueNamesById = new HashMap();

			for (Iterator iterator = categoryDefinitionsByName.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String name = (String) entry.getKey();
				Set categoryDefinitions2 = (Set) entry.getValue();
				Iterator iterator2 = categoryDefinitions2.iterator();				
				
				if (categoryDefinitions2.size() == 1) {					
					ICategoryDefinition categoryDefinition = (ICategoryDefinition) iterator2.next(); 
					categoryIdsByUniqueName.put(name, categoryDefinition.getId());
					categoryUniqueNamesById.put(categoryDefinition.getId(), name);
				} else while (iterator2.hasNext()) {
					ICategoryDefinition categoryDefinition = (ICategoryDefinition) iterator2.next(); 
					String uniqueName = MessageFormat.format(Util.translateString(resourceBundle, "uniqueName"), new Object[] { name, categoryDefinition.getId() }); //$NON-NLS-1$
					categoryIdsByUniqueName.put(uniqueName, categoryDefinition.getId());							
					categoryUniqueNamesById.put(categoryDefinition.getId(), uniqueName);
				}
			}	

			Map commandDefinitionsByName = CommandDefinition.commandDefinitionsByName(commandDefinitionsById.values(), false);
			commandIdsByUniqueName = new HashMap();
			commandUniqueNamesById = new HashMap();

			for (Iterator iterator = commandDefinitionsByName.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String name = (String) entry.getKey();
				Set commandDefinitions2 = (Set) entry.getValue();
				Iterator iterator2 = commandDefinitions2.iterator();				
				
				if (commandDefinitions2.size() == 1) {					
					ICommandDefinition commandDefinition = (ICommandDefinition) iterator2.next(); 
					commandIdsByUniqueName.put(name, commandDefinition.getId());
					commandUniqueNamesById.put(commandDefinition.getId(), name);
				} else while (iterator2.hasNext()) {
					ICommandDefinition commandDefinition = (ICommandDefinition) iterator2.next(); 
					String uniqueName = MessageFormat.format(Util.translateString(resourceBundle, "uniqueName"), new Object[] { name, commandDefinition.getId() }); //$NON-NLS-1$
					commandIdsByUniqueName.put(uniqueName, commandDefinition.getId());							
					commandUniqueNamesById.put(commandDefinition.getId(), uniqueName);
				}
			}	

			Map keyConfigurationDefinitionsByName = KeyConfigurationDefinition.keyConfigurationDefinitionsByName(keyConfigurationDefinitionsById.values(), false);
			keyConfigurationIdsByUniqueName = new HashMap();
			keyConfigurationUniqueNamesById = new HashMap();

			for (Iterator iterator = keyConfigurationDefinitionsByName.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String name = (String) entry.getKey();
				Set keyConfigurationDefinitions2 = (Set) entry.getValue();
				Iterator iterator2 = keyConfigurationDefinitions2.iterator();				
				
				if (keyConfigurationDefinitions2.size() == 1) {					
					IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) iterator2.next(); 
					keyConfigurationIdsByUniqueName.put(name, keyConfigurationDefinition.getId());
					keyConfigurationUniqueNamesById.put(keyConfigurationDefinition.getId(), name);
				} else while (iterator2.hasNext()) {
					IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) iterator2.next(); 
					String uniqueName = MessageFormat.format(Util.translateString(resourceBundle, "uniqueName"), new Object[] { name, keyConfigurationDefinition.getId() }); //$NON-NLS-1$
					keyConfigurationIdsByUniqueName.put(uniqueName, keyConfigurationDefinition.getId());							
					keyConfigurationUniqueNamesById.put(keyConfigurationDefinition.getId(), uniqueName);
				}
			}	

			Map contextDefinitionsByName = ContextDefinition.contextDefinitionsByName(contextDefinitionsById.values(), false);
			contextIdsByUniqueName = new HashMap();
			contextUniqueNamesById = new HashMap();

			for (Iterator iterator = contextDefinitionsByName.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String name = (String) entry.getKey();
				Set contextDefinitions2 = (Set) entry.getValue();
				Iterator iterator2 = contextDefinitions2.iterator();				
				
				if (contextDefinitions2.size() == 1) {					
					IContextDefinition contextDefinition = (IContextDefinition) iterator2.next(); 
					contextIdsByUniqueName.put(name, contextDefinition.getId());
					contextUniqueNamesById.put(contextDefinition.getId(), name);
				} else while (iterator2.hasNext()) {
					IContextDefinition contextDefinition = (IContextDefinition) iterator2.next(); 
					String uniqueName = MessageFormat.format(Util.translateString(resourceBundle, "uniqueName"), new Object[] { name, contextDefinition.getId() }); //$NON-NLS-1$
					contextIdsByUniqueName.put(uniqueName, contextDefinition.getId());							
					contextUniqueNamesById.put(contextDefinition.getId(), uniqueName);
				}
			}	

			/* TODO rich client platform. simplify UI if possible
			boolean showCategory = !categoryIdsByUniqueName.isEmpty();
			labelCategory.setVisible(showCategory);
			comboCategory.setVisible(showCategory);					
			boolean showContext = !contextIdsByUniqueName.isEmpty();
			labelContext.setVisible(showContext);
			comboContext.setVisible(showContext);					
			labelContextExtends.setVisible(showContext);
			boolean showKeyConfiguration = !keyConfigurationIdsByUniqueName.isEmpty();		
			labelKeyConfiguration.setVisible(showKeyConfiguration);
			comboKeyConfiguration.setVisible(showKeyConfiguration);
			labelKeyConfigurationExtends.setVisible(showKeyConfiguration);
			*/

			List categoryNames = new ArrayList(categoryIdsByUniqueName.keySet());
			Collections.sort(categoryNames, Collator.getInstance());						

			if (commandIdsByCategoryId.containsKey(null))						
				categoryNames.add(0, Util.translateString(resourceBundle, "other")); //$NON-NLS-1$
			
			comboCategory.setItems((String[]) categoryNames.toArray(new String[categoryNames.size()]));
			comboCategory.clearSelection();
			comboCategory.deselectAll();
			
			if (commandIdsByCategoryId.containsKey(null) || !categoryNames.isEmpty())
				comboCategory.select(0);			

			List keyConfigurationNames = new ArrayList(keyConfigurationIdsByUniqueName.keySet());
			Collections.sort(keyConfigurationNames, Collator.getInstance());						
			keyConfigurationNames.add(0, Util.translateString(resourceBundle, "standard")); //$NON-NLS-1$
			comboKeyConfiguration.setItems((String[]) keyConfigurationNames.toArray(new String[keyConfigurationNames.size()]));
			setKeyConfigurationId(activeKeyConfigurationId);		
			update();
		}

		super.setVisible(visible);
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		composite.setLayout(gridLayout);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gridData);
		Composite compositeKeyConfiguration = new Composite(composite, SWT.NULL);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		compositeKeyConfiguration.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		compositeKeyConfiguration.setLayoutData(gridData);
		labelKeyConfiguration = new Label(compositeKeyConfiguration, SWT.LEFT);
		labelKeyConfiguration.setText(Util.translateString(resourceBundle, "labelKeyConfiguration")); //$NON-NLS-1$
		comboKeyConfiguration = new Combo(compositeKeyConfiguration, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.widthHint = 200;
		comboKeyConfiguration.setLayoutData(gridData);

		comboKeyConfiguration.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboKeyConfiguration();
			}	
		});

		labelKeyConfigurationExtends = new Label(compositeKeyConfiguration, SWT.LEFT);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		labelKeyConfigurationExtends.setLayoutData(gridData);
		Control spacer = new Composite(composite, SWT.NULL);
		gridData = new GridData();
		gridData.heightHint = 10;
		gridData.widthHint = 10;
		spacer.setLayoutData(gridData);					
		groupCommand = new Group(composite, SWT.SHADOW_NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		groupCommand.setLayout(gridLayout);	
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		groupCommand.setLayoutData(gridData);
		groupCommand.setText(Util.translateString(resourceBundle, "groupCommand")); //$NON-NLS-1$	
		labelCategory = new Label(groupCommand, SWT.LEFT);
		gridData = new GridData();
		labelCategory.setLayoutData(gridData);
		labelCategory.setText(Util.translateString(resourceBundle, "labelCategory")); //$NON-NLS-1$
		comboCategory = new Combo(groupCommand, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.widthHint = 200;
		comboCategory.setLayoutData(gridData);

		comboCategory.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboCategory();
			}	
		});

		labelCommand = new Label(groupCommand, SWT.LEFT);
		gridData = new GridData();
		labelCommand.setLayoutData(gridData);
		labelCommand.setText(Util.translateString(resourceBundle, "labelCommand")); //$NON-NLS-1$
		comboCommand = new Combo(groupCommand, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.widthHint = 300;
		comboCommand.setLayoutData(gridData);

		comboCommand.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboCommand();
			}	
		});

		labelAssignmentsForCommand = new Label(groupCommand, SWT.LEFT);
		gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		labelAssignmentsForCommand.setLayoutData(gridData);
		labelAssignmentsForCommand.setText(Util.translateString(resourceBundle, "labelAssignmentsForCommand")); //$NON-NLS-1$
		tableAssignmentsForCommand = new Table(groupCommand, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tableAssignmentsForCommand.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 60;
		gridData.horizontalSpan = 2;
		gridData.widthHint = "carbon".equals(SWT.getPlatform()) ? 520 : 420;
		tableAssignmentsForCommand.setLayoutData(gridData);		
		TableColumn tableColumnDelta = new TableColumn(tableAssignmentsForCommand, SWT.NULL, 0);
		tableColumnDelta.setResizable(false);
		tableColumnDelta.setText(Util.ZERO_LENGTH_STRING);
		tableColumnDelta.setWidth(20);				
		TableColumn tableColumnContext = new TableColumn(tableAssignmentsForCommand, SWT.NULL, 1);		
		tableColumnContext.setResizable(true);
		tableColumnContext.setText(Util.translateString(resourceBundle, "tableColumnContext")); //$NON-NLS-1$
		tableColumnContext.pack();
		tableColumnContext.setWidth("carbon".equals(SWT.getPlatform()) ? 110 : 100);
		TableColumn tableColumnKeySequence = new TableColumn(tableAssignmentsForCommand, SWT.NULL, 2);
		tableColumnKeySequence.setResizable(true);
		tableColumnKeySequence.setText(Util.translateString(resourceBundle, "tableColumnKeySequence")); //$NON-NLS-1$
		tableColumnKeySequence.pack();
		tableColumnKeySequence.setWidth(300);						

		tableAssignmentsForCommand.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent mouseEvent) {
				doubleClickedAssignmentsForCommand();	
			}			
		});		

		tableAssignmentsForCommand.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {			
				selectedTableAssignmentsForCommand();
			}	
		});

		groupKeySequence = new Group(composite, SWT.SHADOW_NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		groupKeySequence.setLayout(gridLayout);	
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		groupKeySequence.setLayoutData(gridData);
		groupKeySequence.setText(Util.translateString(resourceBundle, "groupKeySequence")); //$NON-NLS-1$	
		labelKeySequence = new Label(groupKeySequence, SWT.LEFT);
		gridData = new GridData();
		labelKeySequence.setLayoutData(gridData);
		labelKeySequence.setText(Util.translateString(resourceBundle, "labelKeySequence")); //$NON-NLS-1$
		textKeySequence = new KeySequenceText(groupKeySequence);
		gridData = new GridData();
		gridData.horizontalSpan = 2;		
		gridData.widthHint = 300;
		textKeySequence.setLayoutData(gridData);
		textKeySequence.setMaxStrokes(4);
		
		textKeySequence.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				modifiedTextKeySequence();
			}
		});

		labelAssignmentsForKeySequence = new Label(groupKeySequence, SWT.LEFT);
		gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		labelAssignmentsForKeySequence.setLayoutData(gridData);
		labelAssignmentsForKeySequence.setText(Util.translateString(resourceBundle, "labelAssignmentsForKeySequence")); //$NON-NLS-1$
		tableAssignmentsForKeySequence = new Table(groupKeySequence, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tableAssignmentsForKeySequence.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 60;
		gridData.horizontalSpan = 2;
		gridData.widthHint = "carbon".equals(SWT.getPlatform()) ? 520 : 420;
		tableAssignmentsForKeySequence.setLayoutData(gridData);		
		tableColumnDelta = new TableColumn(tableAssignmentsForKeySequence, SWT.NULL, 0);
		tableColumnDelta.setResizable(false);
		tableColumnDelta.setText(Util.ZERO_LENGTH_STRING);
		tableColumnDelta.setWidth(20);		
		tableColumnContext = new TableColumn(tableAssignmentsForKeySequence, SWT.NULL, 1);		
		tableColumnContext.setResizable(true);
		tableColumnContext.setText(Util.translateString(resourceBundle, "tableColumnContext")); //$NON-NLS-1$
		tableColumnContext.pack();
		tableColumnContext.setWidth("carbon".equals(SWT.getPlatform()) ? 110 : 100);
		TableColumn tableColumnCommand = new TableColumn(tableAssignmentsForKeySequence, SWT.NULL, 2);
		tableColumnCommand.setResizable(true);
		tableColumnCommand.setText(Util.translateString(resourceBundle, "tableColumnCommand")); //$NON-NLS-1$
		tableColumnCommand.pack();
		tableColumnCommand.setWidth(300);				

		tableAssignmentsForKeySequence.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent mouseEvent) {
				doubleClickedTableAssignmentsForKeySequence();	
			}			
		});		

		tableAssignmentsForKeySequence.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {			
				selectedTableAssignmentsForKeySequence();
			}	
		});

		Composite compositeContext = new Composite(composite, SWT.NULL);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		compositeContext.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		compositeContext.setLayoutData(gridData);
		labelContext = new Label(compositeContext, SWT.LEFT);
		labelContext.setText(Util.translateString(resourceBundle, "labelContext")); //$NON-NLS-1$
		comboContext = new Combo(compositeContext, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.widthHint = 200;
		comboContext.setLayoutData(gridData);

		comboContext.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboContext();
			}	
		});

		labelContextExtends = new Label(compositeContext, SWT.LEFT);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		labelContextExtends.setLayoutData(gridData);
		Composite compositeButton = new Composite(composite, SWT.NULL);
		gridLayout = new GridLayout();
		gridLayout.marginHeight = 20;
		gridLayout.marginWidth = 0;		
		gridLayout.numColumns = 3;
		compositeButton.setLayout(gridLayout);
		gridData = new GridData();
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
		
		update();
	}

	private void buildCommandAssignmentsTable() {		
		tableAssignmentsForCommand.removeAll();
		
		for (Iterator iterator = commandAssignments.iterator(); iterator.hasNext();) {
			CommandAssignment commandAssignment = (CommandAssignment) iterator.next();
			KeyBindingNode.Assignment assignment = commandAssignment.assignment;
			KeySequence keySequence = commandAssignment.keySequence;
			String commandString = null;
			int difference = DIFFERENCE_NONE;

			if (assignment.hasPreferenceCommandIdInFirstKeyConfiguration || assignment.hasPreferenceCommandIdInInheritedKeyConfiguration) {
				String preferenceCommandId;
						
				if (assignment.hasPreferenceCommandIdInFirstKeyConfiguration)
					preferenceCommandId = assignment.preferenceCommandIdInFirstKeyConfiguration;
				else
					preferenceCommandId = assignment.preferenceCommandIdInInheritedKeyConfiguration;
						
				if (assignment.hasPluginCommandIdInFirstKeyConfiguration || assignment.hasPluginCommandIdInInheritedKeyConfiguration) {
					String pluginCommandId;
							
					if (assignment.hasPluginCommandIdInFirstKeyConfiguration)
						pluginCommandId = assignment.pluginCommandIdInFirstKeyConfiguration;
					else
						pluginCommandId = assignment.pluginCommandIdInInheritedKeyConfiguration;
		
					if (preferenceCommandId != null) {
						difference = DIFFERENCE_CHANGE;						
						commandString = /*commandUniqueNamesById.get(preferenceCommandId)*/ keySequence.format() + "";
					} else {
						difference = DIFFERENCE_MINUS;						
						commandString = /*"Unassigned"*/ keySequence.format();							
					}							
		
					if (pluginCommandId != null)
						commandString += " (was: " + commandUniqueNamesById.get(pluginCommandId) + ")";
					else
						commandString += " (was: " + "Unassigned" + ");";						
				} else {
					if (preferenceCommandId != null) {
						difference = DIFFERENCE_ADD;						
						commandString = /*commandUniqueNamesById.get(preferenceCommandId)*/ keySequence.format() + "";
					} else {
						difference = DIFFERENCE_MINUS;						
						commandString = /*"Unassigned"*/ keySequence.format();							
					}							
				}
			} else {
				String pluginCommandId;
					
				if (assignment.hasPluginCommandIdInFirstKeyConfiguration)
					pluginCommandId = assignment.pluginCommandIdInFirstKeyConfiguration;
				else
					pluginCommandId = assignment.pluginCommandIdInInheritedKeyConfiguration;					
		
				if (pluginCommandId != null) {
					difference = DIFFERENCE_NONE;						
					commandString = /*commandUniqueNamesById.get(preferenceCommandId)*/ keySequence.format() + "";
				} else {
					difference = DIFFERENCE_MINUS;						
					commandString = /*"Unassigned"*/ keySequence.format();							
				}		
			}

			TableItem tableItem = new TableItem(tableAssignmentsForCommand, SWT.NULL);
				
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
	
			String contextId = commandAssignment.contextId;
	
			if (contextId == null)
				tableItem.setText(1, Util.translateString(resourceBundle, "general")); //$NON-NLS-1$
			else 
				tableItem.setText(1, (String) contextUniqueNamesById.get(contextId)); //$NON-NLS-1$	
			
			tableItem.setText(2, commandString);
				
			if (difference == DIFFERENCE_MINUS)
				tableItem.setForeground(new Color(getShell().getDisplay(), RGB_MINUS));	
		}	
	}

	private void buildKeySequenceAssignmentsTable() {	
		tableAssignmentsForKeySequence.removeAll();
		
		for (Iterator iterator = keySequenceAssignments.iterator(); iterator.hasNext();) {
			KeySequenceAssignment keySequenceAssignment = (KeySequenceAssignment) iterator.next();
			KeyBindingNode.Assignment assignment = keySequenceAssignment.assignment;
			String commandString = null;
			int difference = DIFFERENCE_NONE;
			
			if (assignment.hasPreferenceCommandIdInFirstKeyConfiguration || assignment.hasPreferenceCommandIdInInheritedKeyConfiguration) {
				String preferenceCommandId;
					
				if (assignment.hasPreferenceCommandIdInFirstKeyConfiguration)
					preferenceCommandId = assignment.preferenceCommandIdInFirstKeyConfiguration;
				else
					preferenceCommandId = assignment.preferenceCommandIdInInheritedKeyConfiguration;
					
				if (assignment.hasPluginCommandIdInFirstKeyConfiguration || assignment.hasPluginCommandIdInInheritedKeyConfiguration) {
					String pluginCommandId;
						
					if (assignment.hasPluginCommandIdInFirstKeyConfiguration)
						pluginCommandId = assignment.pluginCommandIdInFirstKeyConfiguration;
					else
						pluginCommandId = assignment.pluginCommandIdInInheritedKeyConfiguration;
	
					if (preferenceCommandId != null) {
						difference = DIFFERENCE_CHANGE;						
						commandString = commandUniqueNamesById.get(preferenceCommandId) + "";
					} else {
						difference = DIFFERENCE_MINUS;						
						commandString = "Unassigned";							
					}							
	
					if (pluginCommandId != null)
						commandString += " (was: " + commandUniqueNamesById.get(pluginCommandId) + ")";
					else
						commandString += " (was: " + "Unassigned" + ");";						
				} else {
					if (preferenceCommandId != null) {
						difference = DIFFERENCE_ADD;						
						commandString = commandUniqueNamesById.get(preferenceCommandId) + "";
					} else {
						difference = DIFFERENCE_MINUS;						
						commandString = "Unassigned";							
					}							
				}
			} else {
				String pluginCommandId;
				
				if (assignment.hasPluginCommandIdInFirstKeyConfiguration)
					pluginCommandId = assignment.pluginCommandIdInFirstKeyConfiguration;
				else
					pluginCommandId = assignment.pluginCommandIdInInheritedKeyConfiguration;					
	
				if (pluginCommandId != null) {
					difference = DIFFERENCE_NONE;						
					commandString = commandUniqueNamesById.get(pluginCommandId) + "";
				} else {
					difference = DIFFERENCE_MINUS;						
					commandString = "Unassigned";							
				}		
			}
	
			TableItem tableItem = new TableItem(tableAssignmentsForKeySequence, SWT.NULL);
				
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
	
			String contextId = keySequenceAssignment.contextId;
	
			if (contextId == null)
				tableItem.setText(1, Util.translateString(resourceBundle, "general")); //$NON-NLS-1$
			else 
				tableItem.setText(1, (String) contextUniqueNamesById.get(contextId)); //$NON-NLS-1$	
			
			tableItem.setText(2, commandString);
				
			if (difference == DIFFERENCE_MINUS)
				tableItem.setForeground(new Color(getShell().getDisplay(), RGB_MINUS));	
		}
	}

	private void doubleClickedTableAssignmentsForKeySequence() {	
		update();
	}
	
	private void doubleClickedAssignmentsForCommand() {
		update();
	}

	private String getCategoryId() {
		return !commandIdsByCategoryId.containsKey(null) || comboCategory.getSelectionIndex() > 0 ? (String) categoryIdsByUniqueName.get(comboCategory.getText()) : null;
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
		update();
	}

	private void selectAssignmentForCommand(String contextId) {	
		if (tableAssignmentsForCommand.getSelectionCount() > 1)
			tableAssignmentsForCommand.deselectAll();
	
		int i = 0;
		int selection = -1;
		KeySequence keySequence = getKeySequence();

		for (Iterator iterator = commandAssignments.iterator(); iterator.hasNext(); i++) {
			CommandAssignment commandAssignment = (CommandAssignment) iterator.next();

			if (Util.equals(contextId, commandAssignment.contextId) && Util.equals(keySequence, commandAssignment.keySequence)) {
				selection = i;
				break;
			}
		}

		if (selection != tableAssignmentsForCommand.getSelectionIndex()) {
			if (selection == -1 || selection >= tableAssignmentsForCommand.getItemCount())
				tableAssignmentsForCommand.deselectAll();
			else
				tableAssignmentsForCommand.select(selection);
		}
	}

	private void selectAssignmentForKeySequence(String contextId) {	
		if (tableAssignmentsForKeySequence.getSelectionCount() > 1)
			tableAssignmentsForKeySequence.deselectAll();
	
		int i = 0;
		int selection = -1;

		for (Iterator iterator = keySequenceAssignments.iterator(); iterator.hasNext(); i++) {
			KeySequenceAssignment keySequenceAssignment = (KeySequenceAssignment) iterator.next();

			if (Util.equals(contextId, keySequenceAssignment.contextId)) {
				selection = i;
				break;
			}
		}

		if (selection != tableAssignmentsForKeySequence.getSelectionIndex()) {
			if (selection == -1 || selection >= tableAssignmentsForKeySequence.getItemCount())
				tableAssignmentsForKeySequence.deselectAll();
			else
				tableAssignmentsForKeySequence.select(selection);
		}
	}

	private void selectedButtonAdd() {
		String commandId = getCommandId();
		String contextId = getContextId();
		String keyConfigurationId = getKeyConfigurationId();
		KeySequence keySequence = getKeySequence();
		KeyBindingNode.remove(tree, keySequence, contextId, keyConfigurationId, 0, null, null);
		KeyBindingNode.add(tree, keySequence, contextId, keyConfigurationId, 0, null, null, commandId);			
		List preferenceKeyBindingDefinitions = new ArrayList();
		KeyBindingNode.getKeyBindingDefinitions(tree, KeySequence.getInstance(), 0, preferenceKeyBindingDefinitions);		
		update();
	}

	private void selectedButtonRemove() {
		String contextId = getContextId();
		String keyConfigurationId = getKeyConfigurationId();
		KeySequence keySequence = getKeySequence();		
		KeyBindingNode.remove(tree, keySequence, contextId, keyConfigurationId, 0, null, null);
		KeyBindingNode.add(tree, keySequence, contextId, keyConfigurationId, 0, null, null, null);
		List preferenceKeyBindingDefinitions = new ArrayList();
		KeyBindingNode.getKeyBindingDefinitions(tree, KeySequence.getInstance(), 0, preferenceKeyBindingDefinitions);		
		update();		
	}
	
	private void selectedButtonRestore() {
		String contextId = getContextId();
		String keyConfigurationId = getKeyConfigurationId();
		KeySequence keySequence = getKeySequence();
		KeyBindingNode.remove(tree, keySequence, contextId, keyConfigurationId, 0, null, null);
		List preferenceKeyBindingDefinitions = new ArrayList();
		KeyBindingNode.getKeyBindingDefinitions(tree, KeySequence.getInstance(), 0, preferenceKeyBindingDefinitions);		
		update();		
	}

	private void selectedComboCategory() {
		update();		
	}	

	private void selectedComboCommand() {			
		update();		
	}

	private void selectedComboContext() {		
		update();		
	}	

	private void selectedComboKeyConfiguration() {		
		update();		
	}	

	private void selectedTableAssignmentsForCommand() {
		int selection = tableAssignmentsForCommand.getSelectionIndex();
		List commandAssignmentsAsList = new ArrayList(commandAssignments);
			
		if (selection >= 0 && selection < commandAssignmentsAsList.size() && tableAssignmentsForCommand.getSelectionCount() == 1) {
			CommandAssignment commandAssignment = (CommandAssignment) commandAssignmentsAsList.get(selection);
			KeyBindingNode.Assignment assignment = commandAssignment.assignment;
			String contextId = commandAssignment.contextId;
			KeySequence keySequence = commandAssignment.keySequence;
			setContextId(contextId);			
			setKeySequence(keySequence);
		}		
		
		update();		
	}
	
	private void selectedTableAssignmentsForKeySequence() {
		int selection = tableAssignmentsForKeySequence.getSelectionIndex();
		List keySequenceAssignmentsAsList = new ArrayList(keySequenceAssignments);
			
		if (selection >= 0 && selection < keySequenceAssignmentsAsList.size() && tableAssignmentsForKeySequence.getSelectionCount() == 1) {
			KeySequenceAssignment keySequenceAssignment = (KeySequenceAssignment) keySequenceAssignmentsAsList.get(selection);
			KeyBindingNode.Assignment assignment = keySequenceAssignment.assignment;
			String contextId = keySequenceAssignment.contextId;				
			setContextId(contextId);			
		}

		update();		
	}

	private void setAssignmentsForCommand() {		
		commandAssignments.clear();
		String commandId = getCommandId();
		
		for (Iterator iterator = assignmentsByContextIdByKeySequence.entrySet()	.iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeySequence keySequence = (KeySequence) entry.getKey();
			Map assignmentsByContextId = (Map) entry.getValue();

			if (assignmentsByContextId != null)
				for (Iterator iterator2 = assignmentsByContextId.entrySet().iterator(); iterator2.hasNext();) {
					Map.Entry entry2 = (Map.Entry) iterator2.next();		
					CommandAssignment commandAssignment = new CommandAssignment();
					commandAssignment.assignment = (KeyBindingNode.Assignment) entry2.getValue();
					commandAssignment.contextId	= (String) entry2.getKey();
					commandAssignment.keySequence = keySequence;

					if (commandAssignment.assignment.contains(commandId))
						commandAssignments.add(commandAssignment);
				}
		}		
		
		buildCommandAssignmentsTable();
	}	

	private void setAssignmentsForKeySequence() {		
		keySequenceAssignments.clear();
		KeySequence keySequence = getKeySequence();			
		Map assignmentsByContextId = (Map) assignmentsByContextIdByKeySequence.get(keySequence);

		if (assignmentsByContextId != null)
			for (Iterator iterator = assignmentsByContextId.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry entry = (Map.Entry) iterator.next();		
				KeySequenceAssignment keySequenceAssignment = new KeySequenceAssignment();
				keySequenceAssignment.assignment = (KeyBindingNode.Assignment) entry.getValue();
				keySequenceAssignment.contextId	= (String) entry.getKey();
				keySequenceAssignments.add(keySequenceAssignment);
			}

		buildKeySequenceAssignmentsTable();
	}

	private void setCategoryId(String categoryId) {				
		comboCategory.clearSelection();
		comboCategory.deselectAll();
		String categoryUniqueName = (String) categoryUniqueNamesById.get(categoryId);
		
		if (categoryUniqueName != null) {
			String items[] = comboCategory.getItems();
			
			for (int i = commandIdsByCategoryId.containsKey(null) ? 1 : 0; i < items.length; i++)
				if (categoryUniqueName.equals(items[i])) {
					comboCategory.select(i);
					break;		
				}
		} else if (commandIdsByCategoryId.containsKey(null))
			comboCategory.select(0);
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

	private void setCommandsForCategory() {
		String categoryId = getCategoryId();
		String commandId = getCommandId();
		Set commandIds = (Set) commandIdsByCategoryId.get(categoryId);
		Map commandIdsByUniqueName = new HashMap(this.commandIdsByUniqueName);
		commandIdsByUniqueName.values().retainAll(commandIds);
		List commandNames = new ArrayList(commandIdsByUniqueName.keySet());			
		Collections.sort(commandNames, Collator.getInstance());						
		comboCommand.setItems((String[]) commandNames.toArray(new String[commandNames.size()]));
		setCommandId(commandId);
		
		if (comboCommand.getSelectionIndex() == -1 && !commandNames.isEmpty())
			comboCommand.select(0);		
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

	private void setContextsForCommand() {
		String commandId = getCommandId();
		String contextId = getContextId();
		Set contextIds = (Set) contextIdsByCommandId.get(commandId);
		Map contextIdsByUniqueName = new HashMap(this.contextIdsByUniqueName);			
		
		// TODO for context bound commands, this code retains only those context explictly bound. what about assigning key bindings to implicit descendant contexts? 
		if (contextIds != null)
			contextIdsByUniqueName.values().retainAll(contextIds);

		List contextNames = new ArrayList(contextIdsByUniqueName.keySet());
		Collections.sort(contextNames, Collator.getInstance());						
		
		if (contextIds == null)
			contextNames.add(0, Util.translateString(resourceBundle, "general")); //$NON-NLS-1$
		
		comboContext.setItems((String[]) contextNames.toArray(new String[contextNames.size()]));				
		setContextId(contextId);

		if (comboContext.getSelectionIndex() == -1 && !contextNames.isEmpty())
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

	private void update() {
		setCommandsForCategory();
		setContextsForCommand();
		String keyConfigurationId = getKeyConfigurationId();
		KeySequence keySequence = getKeySequence();
		String[] activeKeyConfigurationIds = CommandManager.extend(CommandManager.getKeyConfigurationIds(keyConfigurationId, keyConfigurationDefinitionsById));
		String[] activeLocales = CommandManager.extend(CommandManager.getPath(CommandManager.getInstance().getActiveLocale(), CommandManager.SEPARATOR));
		String[] activePlatforms = CommandManager.extend(CommandManager.getPath(CommandManager.getInstance().getActivePlatform(), CommandManager.SEPARATOR));
		KeyBindingNode.solve(tree, activeKeyConfigurationIds, activePlatforms, activeLocales);		
		assignmentsByContextIdByKeySequence = KeyBindingNode.getAssignmentsByContextIdKeySequence(tree, KeySequence.getInstance());
		setAssignmentsForKeySequence();
		setAssignmentsForCommand();		
		String categoryId = getCategoryId();
		String commandId = getCommandId();
		String contextId = getContextId();
		selectAssignmentForKeySequence(contextId);
		selectAssignmentForCommand(contextId);
		updateLabelKeyConfigurationExtends();		
		updateLabelContextExtends();
		labelAssignmentsForKeySequence.setEnabled(keySequence != null && !keySequence.getKeyStrokes().isEmpty());
		tableAssignmentsForKeySequence.setEnabled(keySequence != null && !keySequence.getKeyStrokes().isEmpty());
		labelAssignmentsForCommand.setEnabled(commandId != null);
		tableAssignmentsForCommand.setEnabled(commandId != null);		
		boolean buttonsEnabled = commandId != null && keySequence != null && !keySequence.getKeyStrokes().isEmpty();		
		boolean buttonAddEnabled = buttonsEnabled; 
		boolean buttonRemoveEnabled = buttonsEnabled;
		boolean buttonRestoreEnabled = buttonsEnabled;	
		// TODO better button enablement
		buttonAdd.setEnabled(buttonAddEnabled);
		buttonRemove.setEnabled(buttonRemoveEnabled);
		buttonRestore.setEnabled(buttonRestoreEnabled);		
	}

	private void updateLabelContextExtends() {
		IContextDefinition contextDefinition = (IContextDefinition) contextDefinitionsById.get(getContextId());
		
		if (contextDefinition != null) {
			String name = (String) contextUniqueNamesById.get(contextDefinition.getParentId());
			
			if (name != null)
				labelContextExtends.setText(MessageFormat.format(Util.translateString(resourceBundle, "extends"), new Object[] { name })); //$NON-NLS-1$
			else
				labelContextExtends.setText(Util.translateString(resourceBundle, "extendsGeneral")); //$NON-NLS-1$
		} else
			labelContextExtends.setText(Util.ZERO_LENGTH_STRING);
	}

	private void updateLabelKeyConfigurationExtends() {
		IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) keyConfigurationDefinitionsById.get(getKeyConfigurationId());

		if (keyConfigurationDefinition != null) {
			String name = (String) keyConfigurationUniqueNamesById.get(keyConfigurationDefinition.getParentId());
			
			if (name != null)
				labelKeyConfigurationExtends.setText(MessageFormat.format(Util.translateString(resourceBundle, "extends"), new Object[] { name })); //$NON-NLS-1$
			else
				labelKeyConfigurationExtends.setText(Util.translateString(resourceBundle, "extendsStandard")); //$NON-NLS-1$
		} else
			labelKeyConfigurationExtends.setText(Util.ZERO_LENGTH_STRING);
	}

	/*
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
	*/
}
