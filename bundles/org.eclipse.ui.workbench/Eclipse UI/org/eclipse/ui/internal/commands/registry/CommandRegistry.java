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

package org.eclipse.ui.internal.commands.registry;

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
import org.eclipse.ui.commands.ICategoryDefinition;
import org.eclipse.ui.commands.ICategoryDefinitionHandle;
import org.eclipse.ui.commands.ICommandDefinition;
import org.eclipse.ui.commands.ICommandDefinitionHandle;
import org.eclipse.ui.commands.ICommandRegistry;
import org.eclipse.ui.commands.ICommandRegistryEvent;
import org.eclipse.ui.commands.ICommandRegistryListener;
import org.eclipse.ui.commands.IKeyConfigurationDefinition;
import org.eclipse.ui.commands.IKeyConfigurationDefinitionHandle;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;

public final class CommandRegistry implements ICommandRegistry {

	private static CommandRegistry instance;

	public static CommandRegistry getInstance() {
		if (instance == null)
			instance = new CommandRegistry();
			
		return instance;
	}

	private SortedMap categoryDefinitionHandlesById = new TreeMap();
	private SortedMap categoryDefinitionsById = new TreeMap();
	private SortedMap commandDefinitionHandlesById = new TreeMap();
	private SortedMap commandDefinitionsById = new TreeMap();
	private ICommandRegistryEvent commandRegistryEvent;
	private List commandRegistryListeners;
	private SortedMap keyConfigurationDefinitionHandlesById = new TreeMap();
	private SortedMap keyConfigurationDefinitionsById = new TreeMap();	
	private IRegistry pluginRegistry;
	private IMutableRegistry preferenceRegistry;

	private CommandRegistry() {
		super();
		loadPluginRegistry();
		loadPreferenceRegistry();
		update();
	}

	public void addCommandRegistryListener(ICommandRegistryListener commandRegistryListener) {
		if (commandRegistryListener == null)
			throw new NullPointerException();
			
		if (commandRegistryListeners == null)
			commandRegistryListeners = new ArrayList();
		
		if (!commandRegistryListeners.contains(commandRegistryListener))
			commandRegistryListeners.add(commandRegistryListener);
	}

	public ICategoryDefinitionHandle getCategoryDefinitionHandle(String categoryDefinitionId) {
		if (categoryDefinitionId == null)
			throw new NullPointerException();
			
		ICategoryDefinitionHandle categoryDefinitionHandle = (ICategoryDefinitionHandle) categoryDefinitionHandlesById.get(categoryDefinitionId);
		
		if (categoryDefinitionHandle == null) {
			categoryDefinitionHandle = new CategoryDefinitionHandle(categoryDefinitionId);
			categoryDefinitionHandlesById.put(categoryDefinitionId, categoryDefinitionHandle);
		}
		
		return categoryDefinitionHandle;
	}

	public SortedMap getCategoryDefinitionsById() {
		return Collections.unmodifiableSortedMap(categoryDefinitionsById);
	}

	public ICommandDefinitionHandle getCommandDefinitionHandle(String commandDefinitionId) {
		if (commandDefinitionId == null)
			throw new NullPointerException();
			
		ICommandDefinitionHandle commandDefinitionHandle = (ICommandDefinitionHandle) commandDefinitionHandlesById.get(commandDefinitionId);
		
		if (commandDefinitionHandle == null) {
			commandDefinitionHandle = new CommandDefinitionHandle(commandDefinitionId);
			commandDefinitionHandlesById.put(commandDefinitionId, commandDefinitionHandle);
		}
		
		return commandDefinitionHandle;
	}
	
	public SortedMap getCommandDefinitionsById() {
		return Collections.unmodifiableSortedMap(commandDefinitionsById);
	}	

	public IKeyConfigurationDefinitionHandle getKeyConfigurationDefinitionHandle(String keyConfigurationDefinitionId) {
		if (keyConfigurationDefinitionId == null)
			throw new NullPointerException();
			
		IKeyConfigurationDefinitionHandle keyConfigurationDefinitionHandle = (IKeyConfigurationDefinitionHandle) keyConfigurationDefinitionHandlesById.get(keyConfigurationDefinitionId);
		
		if (keyConfigurationDefinitionHandle == null) {
			keyConfigurationDefinitionHandle = new KeyConfigurationDefinitionHandle(keyConfigurationDefinitionId);
			keyConfigurationDefinitionHandlesById.put(keyConfigurationDefinitionId, keyConfigurationDefinitionHandle);
		}
		
		return keyConfigurationDefinitionHandle;
	}

	public SortedMap getKeyConfigurationDefinitionsById() {
		return Collections.unmodifiableSortedMap(keyConfigurationDefinitionsById);
	}

	public void removeCommandRegistryListener(ICommandRegistryListener commandRegistryListener) {
		if (commandRegistryListener == null)
			throw new NullPointerException();
			
		if (commandRegistryListeners != null) {
			commandRegistryListeners.remove(commandRegistryListener);
			
			if (commandRegistryListeners.isEmpty())
				commandRegistryListeners = null;
		}
	}

	private void fireCommandRegistryChanged() {
		if (commandRegistryListeners != null) {
			// TODO copying to avoid ConcurrentModificationException
			Iterator iterator = new ArrayList(commandRegistryListeners).iterator();	
			
			if (iterator.hasNext()) {
				if (commandRegistryEvent == null)
					commandRegistryEvent = new CommandRegistryEvent(this);
				
				while (iterator.hasNext())	
					((ICommandRegistryListener) iterator.next()).commandRegistryChanged(commandRegistryEvent);
			}							
		}			
	}

	private void loadPluginRegistry() {
		if (pluginRegistry == null)
			pluginRegistry = new PluginRegistry(Platform.getPluginRegistry());
		
		try {
			pluginRegistry.load();
		} catch (IOException eIO) {
			// TODO proper catch
		}
	}
	
	private void loadPreferenceRegistry() {
		if (preferenceRegistry == null)
			preferenceRegistry = new PreferenceRegistry(WorkbenchPlugin.getDefault().getPreferenceStore());
		
		try {
			preferenceRegistry.load();
		} catch (IOException eIO) {
			// TODO proper catch
		}
	}

	private void update() {	
		List categoryDefinitions = new ArrayList();
		categoryDefinitions.addAll(pluginRegistry.getCategoryDefinitions());
		categoryDefinitions.addAll(preferenceRegistry.getCategoryDefinitions());
		SortedMap categoryDefinitionsById = CategoryDefinition.sortedMapById(categoryDefinitions);			
		SortedSet categoryDefinitionChanges = new TreeSet();
		Util.diff(categoryDefinitionsById, this.categoryDefinitionsById, categoryDefinitionChanges, categoryDefinitionChanges, categoryDefinitionChanges);
		List commandDefinitions = new ArrayList();
		commandDefinitions.addAll(pluginRegistry.getCommandDefinitions());
		commandDefinitions.addAll(preferenceRegistry.getCommandDefinitions());
		SortedMap commandDefinitionsById = CommandDefinition.sortedMapById(commandDefinitions);			
		SortedSet commandDefinitionChanges = new TreeSet();
		Util.diff(commandDefinitionsById, this.commandDefinitionsById, commandDefinitionChanges, commandDefinitionChanges, commandDefinitionChanges);
		List keyConfigurationDefinitions = new ArrayList();
		keyConfigurationDefinitions.addAll(pluginRegistry.getKeyConfigurationDefinitions());
		keyConfigurationDefinitions.addAll(preferenceRegistry.getKeyConfigurationDefinitions());
		SortedMap keyConfigurationDefinitionsById = KeyConfigurationDefinition.sortedMapById(keyConfigurationDefinitions);			
		SortedSet keyConfigurationDefinitionChanges = new TreeSet();
		Util.diff(keyConfigurationDefinitionsById, this.keyConfigurationDefinitionsById, keyConfigurationDefinitionChanges, keyConfigurationDefinitionChanges, keyConfigurationDefinitionChanges);
		boolean commandRegistryChanged = false;
				
		if (!categoryDefinitionChanges.isEmpty()) {
			this.categoryDefinitionsById = categoryDefinitionsById;
			commandRegistryChanged = true;			
		}

		if (!commandDefinitionChanges.isEmpty()) {
			this.commandDefinitionsById = commandDefinitionsById;		
			commandRegistryChanged = true;			
		}

		if (!keyConfigurationDefinitionChanges.isEmpty()) {
			this.keyConfigurationDefinitionsById = keyConfigurationDefinitionsById;		
			commandRegistryChanged = true;
		}

		if (commandRegistryChanged)
			fireCommandRegistryChanged();

		if (!categoryDefinitionChanges.isEmpty()) {
			Iterator iterator = categoryDefinitionChanges.iterator();
		
			while (iterator.hasNext()) {
				String categoryDefinitionId = (String) iterator.next();					
				CategoryDefinitionHandle categoryDefinitionHandle = (CategoryDefinitionHandle) categoryDefinitionHandlesById.get(categoryDefinitionId);
			
				if (categoryDefinitionHandle != null) {			
					if (categoryDefinitionsById.containsKey(categoryDefinitionId))
						categoryDefinitionHandle.define((ICategoryDefinition) categoryDefinitionsById.get(categoryDefinitionId));
					else
						categoryDefinitionHandle.undefine();
				}
			}			
		}

		if (!commandDefinitionChanges.isEmpty()) {
			Iterator iterator = commandDefinitionChanges.iterator();
		
			while (iterator.hasNext()) {
				String commandDefinitionId = (String) iterator.next();					
				CommandDefinitionHandle commandDefinitionHandle = (CommandDefinitionHandle) commandDefinitionHandlesById.get(commandDefinitionId);
			
				if (commandDefinitionHandle != null) {			
					if (commandDefinitionsById.containsKey(commandDefinitionId))
						commandDefinitionHandle.define((ICommandDefinition) commandDefinitionsById.get(commandDefinitionId));
					else
						commandDefinitionHandle.undefine();
				}
			}			
		}
		
		if (!keyConfigurationDefinitionChanges.isEmpty()) {
			Iterator iterator = keyConfigurationDefinitionChanges.iterator();
		
			while (iterator.hasNext()) {
				String keyConfigurationDefinitionId = (String) iterator.next();					
				KeyConfigurationDefinitionHandle keyConfigurationDefinitionHandle = (KeyConfigurationDefinitionHandle) keyConfigurationDefinitionHandlesById.get(keyConfigurationDefinitionId);
			
				if (keyConfigurationDefinitionHandle != null) {			
					if (keyConfigurationDefinitionsById.containsKey(keyConfigurationDefinitionId))
						keyConfigurationDefinitionHandle.define((IKeyConfigurationDefinition) keyConfigurationDefinitionsById.get(keyConfigurationDefinitionId));
					else
						keyConfigurationDefinitionHandle.undefine();
				}
			}			
		}		
	}
}
