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

package org.eclipse.ui.internal.csm.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.csm.commands.api.ICategory;
import org.eclipse.ui.internal.csm.commands.api.ICategoryEvent;
import org.eclipse.ui.internal.csm.commands.api.ICommand;
import org.eclipse.ui.internal.csm.commands.api.ICommandEvent;
import org.eclipse.ui.internal.csm.commands.api.ICommandManager;
import org.eclipse.ui.internal.csm.commands.api.ICommandManagerEvent;
import org.eclipse.ui.internal.csm.commands.api.ICommandManagerListener;
import org.eclipse.ui.internal.csm.commands.api.IKeyConfiguration;
import org.eclipse.ui.internal.csm.commands.api.IKeyConfigurationEvent;
import org.eclipse.ui.internal.csm.commands.api.IKeySequenceBinding;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;

public final class CommandManager implements ICommandManager {

	static boolean isKeyConfigurationDefinitionChildOf(String ancestor, String id, Map keyConfigurationDefinitionsById) {
		Collection visited = new HashSet();

		while (id != null && !visited.contains(id)) {
			IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) keyConfigurationDefinitionsById.get(id);				
			visited.add(id);

			if (keyConfigurationDefinition != null && Util.equals(id = keyConfigurationDefinition.getParentId(), ancestor))
				return true;
		}

		return false;
	}		
	
	private Set activeCommandIds = new HashSet();
	private Set activeKeyConfigurationIds = new HashSet();
	
	private Map activityBindingsByCommandId = new HashMap();		

	private Map categoriesById = new WeakHashMap();
	private Set categoriesWithListeners = new HashSet();
	private Map categoryDefinitionsById = new HashMap();
	
	private Map commandDefinitionsById = new HashMap();
	
	private List commandManagerListeners;
	private ICommandRegistry commandRegistry;		

	private Map commandsById = new WeakHashMap();
	private Set commandsWithListeners = new HashSet();

	private Set definedCategoryIds = new HashSet();
	private Set definedCommandIds = new HashSet();
	private Set definedKeyConfigurationIds = new HashSet();

	private Set enabledCommandIds = new HashSet();	

	private Map imageBindingsByCommandId = new HashMap();	
	
	private Map keyConfigurationsById = new WeakHashMap();
	private Set keyConfigurationsWithListeners = new HashSet();
	private Map keyConfigurationDefinitionsById = new HashMap();	
	
	private Map keySequenceBindingsByCommandId = new HashMap();

	private IMutableCommandRegistry mutableCommandRegistry;		
	
	public CommandManager() {
		this(new ExtensionCommandRegistry(Platform.getExtensionRegistry()), new PreferenceCommandRegistry(WorkbenchPlugin.getDefault().getPreferenceStore()));
	}

	public CommandManager(ICommandRegistry commandRegistry, IMutableCommandRegistry mutableCommandRegistry) {
		if (commandRegistry == null || mutableCommandRegistry == null)
			throw new NullPointerException();

		this.commandRegistry = commandRegistry;
		this.mutableCommandRegistry = mutableCommandRegistry;
		
		this.commandRegistry.addCommandRegistryListener(new ICommandRegistryListener() {
			public void commandRegistryChanged(ICommandRegistryEvent commandRegistryEvent) {
				readRegistry();
			}
		});

		this.mutableCommandRegistry.addCommandRegistryListener(new ICommandRegistryListener() {
			public void commandRegistryChanged(ICommandRegistryEvent commandRegistryEvent) {
				readRegistry();
			}
		});
		
		readRegistry();
	}	
	
	public void addCommandManagerListener(ICommandManagerListener commandManagerListener) {
		if (commandManagerListener == null)
			throw new NullPointerException();
			
		if (commandManagerListeners == null)
			commandManagerListeners = new ArrayList();
		
		if (!commandManagerListeners.contains(commandManagerListener))
			commandManagerListeners.add(commandManagerListener);
	}

	public Set getActiveCommandIds() {
		return Collections.unmodifiableSet(activeCommandIds);
	}

	public Set getActiveKeyConfigurationIds() {
		return Collections.unmodifiableSet(activeKeyConfigurationIds);
	}	

	public ICategory getCategory(String categoryId) {
		if (categoryId == null)
			throw new NullPointerException();
			
		Category category = (Category) categoriesById.get(categoryId);
		
		if (category == null) {
			category = new Category(categoriesWithListeners, categoryId);
			updateCategory(category);
			categoriesById.put(categoryId, category);
		}
		
		return category;
	}	
	
	public ICommand getCommand(String commandId) {
		if (commandId == null)
			throw new NullPointerException();
			
		Command command = (Command) commandsById.get(commandId);
		
		if (command == null) {
			command = new Command(commandsWithListeners, commandId);
			updateCommand(command);
			commandsById.put(commandId, command);
		}
		
		return command;
	}

	public Set getDefinedCategoryIds() {
		return Collections.unmodifiableSet(definedCategoryIds);
	}
	
	public Set getDefinedCommandIds() {
		return Collections.unmodifiableSet(definedCommandIds);
	}

	public Set getDefinedKeyConfigurationIds() {
		return Collections.unmodifiableSet(definedKeyConfigurationIds);
	}
	
	public Set getEnabledCommandIds() {
		return Collections.unmodifiableSet(enabledCommandIds);
	}	

	public IKeyConfiguration getKeyConfiguration(String keyConfigurationId) {
		if (keyConfigurationId == null)
			throw new NullPointerException();
			
		KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurationsById.get(keyConfigurationId);
		
		if (keyConfiguration == null) {
			keyConfiguration = new KeyConfiguration(keyConfigurationsWithListeners, keyConfigurationId);
			updateKeyConfiguration(keyConfiguration);
			keyConfigurationsById.put(keyConfigurationId, keyConfiguration);
		}
		
		return keyConfiguration;
	}	
	
	public void removeCommandManagerListener(ICommandManagerListener commandManagerListener) {
		if (commandManagerListener == null)
			throw new NullPointerException();
			
		if (commandManagerListeners != null)
			commandManagerListeners.remove(commandManagerListener);
	}

	public void setActiveCommandIds(Set activeCommandIds) {
		activeCommandIds = Util.safeCopy(activeCommandIds, String.class);
		boolean commandManagerChanged = false;
		Map commandEventsByCommandId = null;

		if (!this.activeCommandIds.equals(activeCommandIds)) {
			this.activeCommandIds = activeCommandIds;
			commandManagerChanged = true;	
			commandEventsByCommandId = updateCommands(commandsById.keySet());	
		}
		
		if (commandManagerChanged)
			fireCommandManagerChanged(new CommandManagerEvent(this, true, false, false));

		if (commandEventsByCommandId != null)
			notifyCommands(commandEventsByCommandId);	
	}
	
	public void setActiveKeyConfigurationIds(Set activeKeyConfigurationIds) {
		activeKeyConfigurationIds = Util.safeCopy(activeKeyConfigurationIds, String.class);
		boolean commandManagerChanged = false;
		Map keyConfigurationEventsByKeyConfigurationId = null;

		if (!this.activeKeyConfigurationIds.equals(activeKeyConfigurationIds)) {
			this.activeKeyConfigurationIds = activeKeyConfigurationIds;
			commandManagerChanged = true;	
			keyConfigurationEventsByKeyConfigurationId = updateKeyConfigurations(keyConfigurationsById.keySet());	
		}
		
		if (commandManagerChanged)
			fireCommandManagerChanged(new CommandManagerEvent(this, true, false, false));

		if (keyConfigurationEventsByKeyConfigurationId != null)
			notifyKeyConfigurations(keyConfigurationEventsByKeyConfigurationId);	
	}	
	
	public void setEnabledCommandIds(Set enabledCommandIds) {	
		enabledCommandIds = Util.safeCopy(enabledCommandIds, String.class);
		boolean commandManagerChanged = false;
		Map commandEventsByCommandId = null;

		if (!this.enabledCommandIds.equals(enabledCommandIds)) {
			this.enabledCommandIds = enabledCommandIds;
			commandManagerChanged = true;	
			commandEventsByCommandId = updateCommands(this.definedCommandIds);	
		}
		
		if (commandManagerChanged)
			fireCommandManagerChanged(new CommandManagerEvent(this, false, false, true));

		if (commandEventsByCommandId != null)
			notifyCommands(commandEventsByCommandId);	
	}	

	private void fireCommandManagerChanged(ICommandManagerEvent commandManagerEvent) {
		if (commandManagerEvent == null)
			throw new NullPointerException();
		
		if (commandManagerListeners != null)
			for (int i = 0; i < commandManagerListeners.size(); i++)
				((ICommandManagerListener) commandManagerListeners.get(i)).commandManagerChanged(commandManagerEvent);
	}

	private void notifyCategories(Map categoryEventsByCategoryId) {	
		for (Iterator iterator = categoryEventsByCategoryId.entrySet().iterator(); iterator.hasNext();) {	
			Map.Entry entry = (Map.Entry) iterator.next();			
			String categoryId = (String) entry.getKey();
			ICategoryEvent categoryEvent = (ICategoryEvent) entry.getValue();
			Category category = (Category) categoriesById.get(categoryId);
			
			if (category != null)
				category.fireCategoryChanged(categoryEvent);
		}
	}	
	
	private void notifyCommands(Map commandEventsByCommandId) {	
		for (Iterator iterator = commandEventsByCommandId.entrySet().iterator(); iterator.hasNext();) {	
			Map.Entry entry = (Map.Entry) iterator.next();			
			String commandId = (String) entry.getKey();
			ICommandEvent commandEvent = (ICommandEvent) entry.getValue();
			Command command = (Command) commandsById.get(commandId);
			
			if (command != null)
				command.fireCommandChanged(commandEvent);
		}
	}

	private void notifyKeyConfigurations(Map keyConfigurationEventsByKeyConfigurationId) {	
		for (Iterator iterator = keyConfigurationEventsByKeyConfigurationId.entrySet().iterator(); iterator.hasNext();) {	
			Map.Entry entry = (Map.Entry) iterator.next();			
			String keyConfigurationId = (String) entry.getKey();
			IKeyConfigurationEvent keyConfigurationEvent = (IKeyConfigurationEvent) entry.getValue();
			KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurationsById.get(keyConfigurationId);
			
			if (keyConfiguration != null)
				keyConfiguration.fireKeyConfigurationChanged(keyConfigurationEvent);
		}
	}	
	
	private void readRegistry() {
		Collection categoryDefinitions = new ArrayList();
		categoryDefinitions.addAll(commandRegistry.getCategoryDefinitions());				
		Map categoryDefinitionsById = new HashMap(CategoryDefinition.categoryDefinitionsById(categoryDefinitions, false));

		for (Iterator iterator = categoryDefinitionsById.values().iterator(); iterator.hasNext();) {
			ICategoryDefinition categoryDefinition = (ICategoryDefinition) iterator.next();
			String name = categoryDefinition.getName();
				
			if (name == null || name.length() == 0)
				iterator.remove();
		}
		
		Collection commandDefinitions = new ArrayList();
		commandDefinitions.addAll(commandRegistry.getCommandDefinitions());				
		Map commandDefinitionsById = new HashMap(CommandDefinition.commandDefinitionsById(commandDefinitions, false));

		for (Iterator iterator = commandDefinitionsById.values().iterator(); iterator.hasNext();) {
			ICommandDefinition commandDefinition = (ICommandDefinition) iterator.next();
			String name = commandDefinition.getName();
				
			if (name == null || name.length() == 0)
				iterator.remove();
		}

		Collection keyConfigurationDefinitions = new ArrayList();
		keyConfigurationDefinitions.addAll(commandRegistry.getKeyConfigurationDefinitions());				
		Map keyConfigurationDefinitionsById = new HashMap(KeyConfigurationDefinition.keyConfigurationDefinitionsById(keyConfigurationDefinitions, false));

		for (Iterator iterator = keyConfigurationDefinitionsById.values().iterator(); iterator.hasNext();) {
			IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) iterator.next();
			String name = keyConfigurationDefinition.getName();
				
			if (name == null || name.length() == 0)
				iterator.remove();
		}		

		for (Iterator iterator = commandDefinitionsById.values().iterator(); iterator.hasNext();) {
			ICommandDefinition commandDefinition = (ICommandDefinition) iterator.next();
			String categoryId = commandDefinition.getCategoryId();
				
			if (categoryId != null && !categoryDefinitionsById.containsKey(categoryId))
				iterator.remove();
		}

		for (Iterator iterator = keyConfigurationDefinitionsById.keySet().iterator(); iterator.hasNext();)
			if (!isKeyConfigurationDefinitionChildOf(null, (String) iterator.next(), keyConfigurationDefinitionsById))
				iterator.remove();
			
		// TODO begin - check this.
			
		Map keySequenceBindingDefinitionsByCommandId = KeySequenceBindingDefinition.keySequenceBindingDefinitionsByCommandId(commandRegistry.getKeySequenceBindingDefinitions());
		Map keySequenceBindingsByCommandId = new HashMap();		

		for (Iterator iterator = keySequenceBindingDefinitionsByCommandId.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String commandId = (String) entry.getKey();
			
			if (commandDefinitionsById.containsKey(commandId)) {			
				Collection keySequenceBindingDefinitions = (Collection) entry.getValue();
				
				if (keySequenceBindingDefinitions != null)
					for (Iterator iterator2 = keySequenceBindingDefinitions.iterator(); iterator2.hasNext();) {
						IKeySequenceBindingDefinition keySequenceBindingDefinition = (IKeySequenceBindingDefinition) iterator2.next();
						KeySequence keySequence = keySequenceBindingDefinition.getKeySequence();
					
						if (keySequence != null && keySequence.isComplete()) {
							// TODO match value
							IKeySequenceBinding keySequenceBinding = new KeySequenceBinding(keySequence, 0);	
							List keySequenceBindings = (List) keySequenceBindingsByCommandId.get(commandId);
							
							if (keySequenceBindings == null) {
								keySequenceBindings = new ArrayList();
								keySequenceBindingsByCommandId.put(commandId, keySequenceBindings);
							}
							
							keySequenceBindings.add(keySequenceBinding);
						}
					}
			}
		}	
		
		// TODO end - check this.
		
		this.categoryDefinitionsById = categoryDefinitionsById;
		this.commandDefinitionsById = commandDefinitionsById;
		this.keyConfigurationDefinitionsById = keyConfigurationDefinitionsById;		
		
		// TODO begin - check this.
		this.keySequenceBindingsByCommandId = keySequenceBindingsByCommandId;
		// TODO end - check this.
		
		boolean commandManagerChanged = false;			
		Set definedCategoryIds = new HashSet(categoryDefinitionsById.keySet());		

		if (!definedCategoryIds.equals(this.definedCategoryIds)) {
			this.definedCategoryIds = definedCategoryIds;
			commandManagerChanged = true;	
		}
		
		Set definedCommandIds = new HashSet(commandDefinitionsById.keySet());		

		if (!definedCommandIds.equals(this.definedCommandIds)) {
			this.definedCommandIds = definedCommandIds;
			commandManagerChanged = true;	
		}

		Set definedKeyConfigurationIds = new HashSet(keyConfigurationDefinitionsById.keySet());		

		if (!definedKeyConfigurationIds.equals(this.definedKeyConfigurationIds)) {
			this.definedKeyConfigurationIds = definedKeyConfigurationIds;
			commandManagerChanged = true;	
		}
		
		Map categoryEventsByCategoryId = updateCategories(categoriesById.keySet());	
		Map commandEventsByCommandId = updateCommands(commandsById.keySet());	
		Map keyConfigurationEventsByKeyConfigurationId = updateKeyConfigurations(keyConfigurationsById.keySet());	
		
		if (commandManagerChanged)
			// TODO...
			fireCommandManagerChanged(new CommandManagerEvent(this, false, true, false));

		if (categoryEventsByCategoryId != null)
			notifyCategories(categoryEventsByCategoryId);		

		if (commandEventsByCommandId != null)
			notifyCommands(commandEventsByCommandId);
		
		if (keyConfigurationEventsByKeyConfigurationId != null)
			notifyKeyConfigurations(keyConfigurationEventsByKeyConfigurationId);
	}

	private Map updateCategories(Collection categoryIds) {
		Map categoryEventsByCategoryId = new TreeMap();
		
		for (Iterator iterator = categoryIds.iterator(); iterator.hasNext();) {		
			String categoryId = (String) iterator.next();					
			Category category = (Category) categoriesById.get(categoryId);
			
			if (category != null) {
				ICategoryEvent categoryEvent = updateCategory(category);
				
				if (categoryEvent != null)
					categoryEventsByCategoryId.put(categoryId, categoryEvent);
			}
		}
		
		return categoryEventsByCategoryId;			
	}	
	
	private ICategoryEvent updateCategory(Category category) {
		ICategoryDefinition categoryDefinition = (ICategoryDefinition) categoryDefinitionsById.get(category.getId());
		boolean definedChanged = category.setDefined(categoryDefinition != null);
		boolean descriptionChanged = category.setDescription(categoryDefinition != null ? categoryDefinition.getDescription() : null);		
		boolean nameChanged = category.setName(categoryDefinition != null ? categoryDefinition.getName() : null);
		
		if (definedChanged || descriptionChanged || nameChanged )
			return new CategoryEvent(category, definedChanged, descriptionChanged, nameChanged); 
		else 
			return null;
	}
	
	private ICommandEvent updateCommand(Command command) {
		boolean activeChanged = command.setActive(activeCommandIds.contains(command.getId()));		
		ICommandDefinition commandDefinition = (ICommandDefinition) commandDefinitionsById.get(command.getId());
		boolean categoryIdChanged = command.setCategoryId(commandDefinition != null ? commandDefinition.getCategoryId() : null);				
		boolean definedChanged = command.setDefined(commandDefinition != null);
		boolean descriptionChanged = command.setDescription(commandDefinition != null ? commandDefinition.getDescription() : null);		
		boolean enabledChanged = command.setEnabled(enabledCommandIds.contains(command.getId()));
		List keySequenceBindings = (List) keySequenceBindingsByCommandId.get(command.getId());
		boolean keySequenceBindingsChanged = command.setKeySequenceBindings(keySequenceBindings != null ? keySequenceBindings : Collections.EMPTY_LIST);		
		boolean nameChanged = command.setName(commandDefinition != null ? commandDefinition.getName() : null);

		if (activeChanged || categoryIdChanged || definedChanged || descriptionChanged || enabledChanged || keySequenceBindingsChanged || nameChanged)
			return new CommandEvent(command, activeChanged, categoryIdChanged, definedChanged, descriptionChanged, enabledChanged, keySequenceBindingsChanged, nameChanged); 
		else 
			return null;
	}

	private Map updateCommands(Collection commandIds) {
		Map commandEventsByCommandId = new TreeMap();
		
		for (Iterator iterator = commandIds.iterator(); iterator.hasNext();) {		
			String commandId = (String) iterator.next();					
			Command command = (Command) commandsById.get(commandId);
			
			if (command != null) {
				ICommandEvent commandEvent = updateCommand(command);
				
				if (commandEvent != null)
					commandEventsByCommandId.put(commandId, commandEvent);
			}
		}
		
		return commandEventsByCommandId;			
	}	
	
	private IKeyConfigurationEvent updateKeyConfiguration(KeyConfiguration keyConfiguration) {
		boolean activeChanged = keyConfiguration.setActive(activeKeyConfigurationIds.contains(keyConfiguration.getId()));		
		IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) keyConfigurationDefinitionsById.get(keyConfiguration.getId());
		boolean definedChanged = keyConfiguration.setDefined(keyConfigurationDefinition != null);
		boolean descriptionChanged = keyConfiguration.setDescription(keyConfigurationDefinition != null ? keyConfigurationDefinition.getDescription() : null);		
		boolean nameChanged = keyConfiguration.setName(keyConfigurationDefinition != null ? keyConfigurationDefinition.getName() : null);
		boolean parentIdChanged = keyConfiguration.setParentId(keyConfigurationDefinition != null ? keyConfigurationDefinition.getParentId() : null);				
		
		if (activeChanged || definedChanged || descriptionChanged || nameChanged || parentIdChanged)
			return new KeyConfigurationEvent(keyConfiguration, activeChanged, definedChanged, descriptionChanged, nameChanged, parentIdChanged); 
		else 
			return null;
	}

	private Map updateKeyConfigurations(Collection keyConfigurationIds) {
		Map keyConfigurationEventsByKeyConfigurationId = new TreeMap();
		
		for (Iterator iterator = keyConfigurationIds.iterator(); iterator.hasNext();) {		
			String keyConfigurationId = (String) iterator.next();					
			KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurationsById.get(keyConfigurationId);
			
			if (keyConfiguration != null) {
				IKeyConfigurationEvent keyConfigurationEvent = updateKeyConfiguration(keyConfiguration);
				
				if (keyConfigurationEvent != null)
					keyConfigurationEventsByKeyConfigurationId.put(keyConfigurationId, keyConfigurationEvent);
			}
		}
		
		return keyConfigurationEventsByKeyConfigurationId;			
	}
}
