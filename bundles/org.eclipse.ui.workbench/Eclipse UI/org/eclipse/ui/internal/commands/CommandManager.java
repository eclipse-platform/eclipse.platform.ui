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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.commands.IAction;
import org.eclipse.ui.commands.ICategory;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.ICommandManagerEvent;
import org.eclipse.ui.commands.ICommandManagerListener;
import org.eclipse.ui.commands.IKeyConfiguration;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.commands.registry.CategoryDefinition;
import org.eclipse.ui.internal.commands.registry.CommandDefinition;
import org.eclipse.ui.internal.commands.registry.ICategoryDefinition;
import org.eclipse.ui.internal.commands.registry.ICommandDefinition;
import org.eclipse.ui.internal.commands.registry.ICommandRegistry;
import org.eclipse.ui.internal.commands.registry.ICommandRegistryEvent;
import org.eclipse.ui.internal.commands.registry.ICommandRegistryListener;
import org.eclipse.ui.internal.commands.registry.IKeyConfigurationDefinition;
import org.eclipse.ui.internal.commands.registry.KeyConfigurationDefinition;
import org.eclipse.ui.internal.commands.registry.PluginCommandRegistry;
import org.eclipse.ui.internal.commands.registry.PreferenceCommandRegistry;
import org.eclipse.ui.internal.util.Util;

public final class CommandManager implements ICommandManager {

	private static CommandManager instance;

	public static CommandManager getInstance() {
		if (instance == null)
			instance = new CommandManager();
			
		return instance;
	}

	private SortedMap actionsById = new TreeMap();	
	private SortedSet activeCommandIds = new TreeSet();
	private SortedSet activeContextIds = new TreeSet();
	private String activeKeyConfigurationId = Util.ZERO_LENGTH_STRING;
	private String activeLocale = Util.ZERO_LENGTH_STRING;
	private String activePlatform = Util.ZERO_LENGTH_STRING;	
	private SortedMap categoriesById = new TreeMap();	
	private SortedMap categoryDefinitionsById = new TreeMap();
	private SortedMap commandDefinitionsById = new TreeMap();
	private ICommandManagerEvent commandManagerEvent;
	private List commandManagerListeners;
	private SortedMap commandsById = new TreeMap();	
	private SortedMap contextBindingsByCommandId = new TreeMap();
	private SortedMap imageBindingsByCommandId = new TreeMap();
	private SortedMap keyBindingsByCommandId = new TreeMap();
	private SortedMap keyConfigurationDefinitionsById = new TreeMap();
	private SortedMap keyConfigurationsById = new TreeMap();	
	private PluginCommandRegistry pluginCommandRegistry;
	private PreferenceCommandRegistry preferenceCommandRegistry;

	private CommandManager() {
		if (pluginCommandRegistry == null)
			pluginCommandRegistry = new PluginCommandRegistry(Platform.getPluginRegistry());
			
		loadPluginCommandRegistry();		

		pluginCommandRegistry.addCommandRegistryListener(new ICommandRegistryListener() {
			public void commandRegistryChanged(ICommandRegistryEvent commandRegistryEvent) {
				readRegistry();
			}
		});

		if (preferenceCommandRegistry == null)
			preferenceCommandRegistry = new PreferenceCommandRegistry(WorkbenchPlugin.getDefault().getPreferenceStore());	

		loadPreferenceCommandRegistry();

		preferenceCommandRegistry.addCommandRegistryListener(new ICommandRegistryListener() {
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

	public SortedMap getActionsById() {
		return Collections.unmodifiableSortedMap(actionsById);
	}

	public SortedSet getActiveCommandIds() {
		return Collections.unmodifiableSortedSet(activeCommandIds);
	}

	public SortedSet getActiveContextIds() {
		return Collections.unmodifiableSortedSet(activeContextIds);
	}

	public String getActiveKeyConfigurationId() {		
		return activeKeyConfigurationId;
	}
	
	public String getActiveLocale() {
		return activeLocale;
	}
	
	public String getActivePlatform() {
		return activePlatform;
	}

	public ICategory getCategory(String categoryId) {
		if (categoryId == null)
			throw new NullPointerException();
			
		Category category = (Category) categoriesById.get(categoryId);
		
		if (category == null) {
			category = new Category(categoryId);
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
			command = new Command(commandId);
			updateCommand(command);
			commandsById.put(commandId, command);
		}
		
		return command;
	}

	public SortedSet getDefinedCategoryIds() {
		return Collections.unmodifiableSortedSet(new TreeSet(categoryDefinitionsById.keySet()));
	}
	
	public SortedSet getDefinedCommandIds() {
		return Collections.unmodifiableSortedSet(new TreeSet(commandDefinitionsById.keySet()));
	}
	
	public SortedSet getDefinedKeyConfigurationIds() {
		return Collections.unmodifiableSortedSet(new TreeSet(keyConfigurationDefinitionsById.keySet()));
	}

	public IKeyConfiguration getKeyConfiguration(String keyConfigurationId) {
		if (keyConfigurationId == null)
			throw new NullPointerException();
			
		KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurationsById.get(keyConfigurationId);
		
		if (keyConfiguration == null) {
			keyConfiguration = new KeyConfiguration(keyConfigurationId);
			updateKeyConfiguration(keyConfiguration);
			keyConfigurationsById.put(keyConfigurationId, keyConfiguration);
		}
		
		return keyConfiguration;
	}
	
	public void removeCommandManagerListener(ICommandManagerListener commandManagerListener) {
		if (commandManagerListener == null)
			throw new NullPointerException();
			
		if (commandManagerListeners != null) {
			commandManagerListeners.remove(commandManagerListener);
			
			if (commandManagerListeners.isEmpty())
				commandManagerListeners = null;
		}
	}

	public void setActionsById(SortedMap actionsById)
		throws IllegalArgumentException {
		actionsById = Util.safeCopy(actionsById, String.class, IAction.class);	
	
		if (!Util.equals(actionsById, this.actionsById)) {	
			this.actionsById = actionsById;	
			fireCommandManagerChanged();
		}
	}

	public void setActiveCommandIds(SortedSet activeCommandIds) {
		activeCommandIds = Util.safeCopy(activeCommandIds, String.class);
		SortedSet commandChanges = new TreeSet();
		Util.diff(activeCommandIds, this.activeCommandIds, commandChanges, commandChanges);
		
		if (!commandChanges.isEmpty()) {
			this.activeCommandIds = activeCommandIds;	
			// TODO: this changes keyBindingsByCommandId
			updateCommands(commandChanges);			
			fireCommandManagerChanged();
			notifyCommands(commandChanges);
		}
	}

	public void setActiveContextIds(SortedSet activeContextIds) {
		activeContextIds = Util.safeCopy(activeContextIds, String.class);
		
		if (!activeContextIds.equals(this.activeContextIds)) {
			this.activeContextIds = activeContextIds;	
			// TODO: this changes Command.inContext and keyBindingsByCommandId
		}
	}

	public void setActiveLocale(String locale) {
		// TODO: this changes imageBindingsByCommandId and keyBindingsByCommandId 
	}
	
	public void setActivePlatform(String platform) {
		// TODO: this changes imageBindingsByCommandId and keyBindingsByCommandId
	}

	ICommandRegistry getPluginCommandRegistry() {
		return pluginCommandRegistry;
	}

	ICommandRegistry getPreferenceCommandRegistry() {
		return preferenceCommandRegistry;
	}

	void loadPluginCommandRegistry() {
		try {
			pluginCommandRegistry.load();
		} catch (IOException eIO) {
			// TODO proper catch
		}
	}
	
	void loadPreferenceCommandRegistry() {
		try {
			preferenceCommandRegistry.load();
		} catch (IOException eIO) {
			// TODO proper catch
		}		
	}

	private void fireCommandManagerChanged() {
		if (commandManagerListeners != null) {
			// TODO copying to avoid ConcurrentModificationException
			Iterator iterator = new ArrayList(commandManagerListeners).iterator();	
			
			if (iterator.hasNext()) {
				if (commandManagerEvent == null)
					commandManagerEvent = new CommandManagerEvent(this);
				
				while (iterator.hasNext())	
					((ICommandManagerListener) iterator.next()).commandManagerChanged(commandManagerEvent);
			}							
		}			
	}

	private void notifyCategories(SortedSet categoryChanges) {	
		Iterator iterator = categoryChanges.iterator();
		
		while (iterator.hasNext()) {
			String categoryId = (String) iterator.next();					
			Category category = (Category) categoriesById.get(categoryId);
			
			if (category != null)
				category.fireCategoryChanged();
		}
	}

	private void notifyCommands(SortedSet commandChanges) {	
		Iterator iterator = commandChanges.iterator();
		
		while (iterator.hasNext()) {
			String commandId = (String) iterator.next();					
			Command command = (Command) commandsById.get(commandId);
			
			if (command != null)
				command.fireCommandChanged();
		}
	}

	private void notifyKeyConfigurations(SortedSet keyConfigurationChanges) {	
		Iterator iterator = keyConfigurationChanges.iterator();
		
		while (iterator.hasNext()) {
			String keyConfigurationId = (String) iterator.next();					
			KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurationsById.get(keyConfigurationId);
			
			if (keyConfiguration != null)
				keyConfiguration.fireKeyConfigurationChanged();
		}
	}

	private void readRegistry() {
		// TODO: activeConfigurationId + the bindings need to be read		
		List categoryDefinitions = new ArrayList();
		categoryDefinitions.addAll(pluginCommandRegistry.getCategoryDefinitions());
		categoryDefinitions.addAll(preferenceCommandRegistry.getCategoryDefinitions());
		SortedMap categoryDefinitionsById = CategoryDefinition.sortedMapById(categoryDefinitions);
		SortedSet categoryChanges = new TreeSet();
		Util.diff(categoryDefinitionsById, this.categoryDefinitionsById, categoryChanges, categoryChanges, categoryChanges);
		List commandDefinitions = new ArrayList();
		commandDefinitions.addAll(pluginCommandRegistry.getCommandDefinitions());
		commandDefinitions.addAll(preferenceCommandRegistry.getCommandDefinitions());
		SortedMap commandDefinitionsById = CommandDefinition.sortedMapById(commandDefinitions);
		SortedSet commandChanges = new TreeSet();
		Util.diff(commandDefinitionsById, this.commandDefinitionsById, commandChanges, commandChanges, commandChanges);
		List keyConfigurationDefinitions = new ArrayList();
		keyConfigurationDefinitions.addAll(pluginCommandRegistry.getKeyConfigurationDefinitions());
		keyConfigurationDefinitions.addAll(preferenceCommandRegistry.getKeyConfigurationDefinitions());
		SortedMap keyConfigurationDefinitionsById = KeyConfigurationDefinition.sortedMapById(keyConfigurationDefinitions);
		SortedSet keyConfigurationChanges = new TreeSet();
		Util.diff(keyConfigurationDefinitionsById, this.keyConfigurationDefinitionsById, keyConfigurationChanges, keyConfigurationChanges, keyConfigurationChanges);
		boolean commandManagerChanged = false;
	
		if (!categoryChanges.isEmpty()) {
			this.categoryDefinitionsById = categoryDefinitionsById;	
			updateCategories(categoryChanges);			
			commandManagerChanged = true;		
		}

		if (!commandChanges.isEmpty()) {
			this.commandDefinitionsById = commandDefinitionsById;	
			updateCommands(commandChanges);			
			commandManagerChanged = true;
		}

		if (!keyConfigurationChanges.isEmpty()) {
			this.keyConfigurationDefinitionsById = keyConfigurationDefinitionsById;	
			updateKeyConfigurations(keyConfigurationChanges);			
			commandManagerChanged = true;
		}

		if (commandManagerChanged)
			fireCommandManagerChanged();

		if (!categoryChanges.isEmpty())
			notifyCategories(categoryChanges);
		
		if (!commandChanges.isEmpty())
			notifyCommands(commandChanges);

		if (!keyConfigurationChanges.isEmpty())
			notifyKeyConfigurations(keyConfigurationChanges);
	}

	private void updateCategories(SortedSet categoryChanges) {
		Iterator iterator = categoryChanges.iterator();
		
		while (iterator.hasNext()) {
			String categoryId = (String) iterator.next();					
			Category category = (Category) categoriesById.get(categoryId);
			
			if (category != null)
				updateCategory(category);			
		}			
	}

	private void updateCategory(Category category) {
		ICategoryDefinition categoryDefinition = (ICategoryDefinition) categoryDefinitionsById.get(category.getId());
		category.setDefined(categoryDefinition != null);
		category.setDescription(categoryDefinition != null ? categoryDefinition.getDescription() : null);
		category.setName(categoryDefinition != null ? categoryDefinition.getName() : Util.ZERO_LENGTH_STRING);
	}

	private void updateCommand(Command command) {
		command.setActive(activeCommandIds.contains(command.getId()));
		ICommandDefinition commandDefinition = (ICommandDefinition) commandDefinitionsById.get(command.getId());
		command.setCategoryId(commandDefinition != null ? commandDefinition.getCategoryId() : null);
		SortedSet contextBindings = (SortedSet) contextBindingsByCommandId.get(command.getId());
		command.setContextBindingSet(contextBindings != null ? contextBindings : Util.EMPTY_SORTED_SET);
		command.setDefined(commandDefinition != null);
		command.setDescription(commandDefinition != null ? commandDefinition.getDescription() : null);
		command.setHelpId(commandDefinition != null ? commandDefinition.getHelpId() : null);
		SortedSet imageBindings = (SortedSet) imageBindingsByCommandId.get(command.getId());
		command.setImageBindingSet(imageBindings != null ? imageBindings : Util.EMPTY_SORTED_SET);
		// TODO: command.setInContext(false);
		SortedSet keyBindings = (SortedSet) keyBindingsByCommandId.get(command.getId());
		command.setKeyBindingSet(keyBindings != null ? keyBindings : Util.EMPTY_SORTED_SET);
		command.setName(commandDefinition != null ? commandDefinition.getName() : Util.ZERO_LENGTH_STRING);
	}

	private void updateCommands(SortedSet commandChanges) {
		Iterator iterator = commandChanges.iterator();
		
		while (iterator.hasNext()) {
			String commandId = (String) iterator.next();					
			Command command = (Command) commandsById.get(commandId);
			
			if (command != null)
				updateCommand(command);			
		}			
	}
	
	private void updateKeyConfiguration(KeyConfiguration keyConfiguration) {
		keyConfiguration.setActive(keyConfiguration.getId().equals(activeKeyConfigurationId));
		IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) keyConfigurationDefinitionsById.get(keyConfiguration.getId());
		keyConfiguration.setDefined(keyConfigurationDefinition != null);
		keyConfiguration.setDescription(keyConfigurationDefinition != null ? keyConfigurationDefinition.getDescription() : null);
		keyConfiguration.setName(keyConfigurationDefinition != null ? keyConfigurationDefinition.getName() : Util.ZERO_LENGTH_STRING);
		keyConfiguration.setParentId(keyConfigurationDefinition != null ? keyConfigurationDefinition.getParentId() : null);
	}

	private void updateKeyConfigurations(SortedSet keyConfigurationChanges) {
		Iterator iterator = keyConfigurationChanges.iterator();
		
		while (iterator.hasNext()) {
			String keyConfigurationId = (String) iterator.next();					
			KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurationsById.get(keyConfigurationId);
			
			if (keyConfiguration != null)
				updateKeyConfiguration(keyConfiguration);			
		}			
	}		
}
