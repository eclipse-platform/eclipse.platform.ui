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
import org.eclipse.ui.commands.ICategory;
import org.eclipse.ui.commands.ICategoryHandle;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandDelegate;
import org.eclipse.ui.commands.ICommandHandle;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.ICommandManagerEvent;
import org.eclipse.ui.commands.ICommandManagerListener;
import org.eclipse.ui.commands.IKeyConfiguration;
import org.eclipse.ui.commands.IKeyConfigurationHandle;
import org.eclipse.ui.internal.util.Util;

public final class CommandManager implements ICommandManager {

	private SortedMap categoriesById = new TreeMap();
	private SortedMap categoryHandlesById = new TreeMap();
	private SortedMap commandDelegatesById = new TreeMap();	
	private SortedMap commandHandlesById = new TreeMap();
	private ICommandManagerEvent commandManagerEvent;
	private List commandManagerListeners;
	private SortedMap commandsById = new TreeMap();
	private SortedSet definedCategoryIds = new TreeSet();
	private SortedSet definedCommandIds = new TreeSet();
	private SortedSet definedKeyConfigurationIds = new TreeSet();
	private SortedMap keyConfigurationHandlesById = new TreeMap();
	private SortedMap keyConfigurationsById = new TreeMap();	
	private PluginRegistry registryReader;

	public CommandManager() {
		super();
		updateFromRegistry();
	}

	public void addCommandManagerListener(ICommandManagerListener commandManagerListener) {
		if (commandManagerListener == null)
			throw new NullPointerException();
			
		if (commandManagerListeners == null)
			commandManagerListeners = new ArrayList();
		
		if (!commandManagerListeners.contains(commandManagerListener))
			commandManagerListeners.add(commandManagerListener);
	}

	public ICategoryHandle getCategoryHandle(String categoryId) {
		if (categoryId == null)
			throw new NullPointerException();
			
		ICategoryHandle categoryHandle = (ICategoryHandle) categoryHandlesById.get(categoryId);
		
		if (categoryHandle == null) {
			categoryHandle = new CategoryHandle(categoryId);
			categoryHandlesById.put(categoryId, categoryHandle);
		}
		
		return categoryHandle;
	}

	public SortedMap getCommandDelegatesById() {
		return Collections.unmodifiableSortedMap(commandDelegatesById);
	}

	public ICommandHandle getCommandHandle(String commandId) {
		if (commandId == null)
			throw new NullPointerException();
			
		ICommandHandle commandHandle = (ICommandHandle) commandHandlesById.get(commandId);
		
		if (commandHandle == null) {
			commandHandle = new CommandHandle(commandId);
			commandHandlesById.put(commandId, commandHandle);
		}
		
		return commandHandle;
	}

	public SortedSet getDefinedCategoryIds() {
		return Collections.unmodifiableSortedSet(definedCategoryIds);
	}

	public SortedSet getDefinedCommandIds() {
		return Collections.unmodifiableSortedSet(definedCommandIds);	
	}

	public SortedSet getDefinedKeyConfigurationIds() {
		return Collections.unmodifiableSortedSet(definedKeyConfigurationIds);
	}

	public IKeyConfigurationHandle getKeyConfigurationHandle(String keyConfigurationId) {
		if (keyConfigurationId == null)
			throw new NullPointerException();
			
		IKeyConfigurationHandle keyConfigurationHandle = (IKeyConfigurationHandle) keyConfigurationHandlesById.get(keyConfigurationId);
		
		if (keyConfigurationHandle == null) {
			keyConfigurationHandle = new KeyConfigurationHandle(keyConfigurationId);
			keyConfigurationHandlesById.put(keyConfigurationId, keyConfigurationHandle);
		}
		
		return keyConfigurationHandle;
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

	public void setCommandDelegatesById(SortedMap commandDelegatesById)
		throws IllegalArgumentException {
		commandDelegatesById = Util.safeCopy(commandDelegatesById, String.class, ICommandDelegate.class);	
	
		if (!Util.equals(commandDelegatesById, this.commandDelegatesById)) {	
			this.commandDelegatesById = commandDelegatesById;	
			fireCommandManagerChanged();
		}
	}

	public void updateFromRegistry() {
		if (registryReader == null)
			registryReader = new PluginRegistry(Platform.getPluginRegistry());
		
		try {
			registryReader.load();
		} catch (IOException eIO) {
			// TODO proper catch
		}
			
		List keyConfigurations = registryReader.getKeyConfigurations();
		SortedMap keyConfigurationsById = KeyConfiguration.sortedMapById(keyConfigurations);			
		SortedSet keyConfigurationChanges = new TreeSet();
		Util.diff(keyConfigurationsById, this.keyConfigurationsById, keyConfigurationChanges, keyConfigurationChanges, keyConfigurationChanges);
		List commands = registryReader.getCommands();
		SortedMap commandsById = Command.sortedMapById(commands);			
		SortedSet commandChanges = new TreeSet();
		Util.diff(commandsById, this.commandsById, commandChanges, commandChanges, commandChanges);
		List categories = registryReader.getCategories();
		SortedMap categoriesById = Category.sortedMapById(categories);			
		SortedSet categoryChanges = new TreeSet();
		Util.diff(categoriesById, this.categoriesById, categoryChanges, categoryChanges, categoryChanges);
		boolean commandManagerChanged = false;
				
		if (!categoryChanges.isEmpty()) {
			this.categoriesById = categoriesById;
			SortedSet definedCategoryIds = new TreeSet(categoriesById.keySet());

			if (!Util.equals(definedCategoryIds, this.definedCategoryIds)) {	
				this.definedCategoryIds = definedCategoryIds;
				commandManagerChanged = true;
			}
		}

		if (!commandChanges.isEmpty()) {
			this.commandsById = commandsById;		
			SortedSet definedCommandIds = new TreeSet(commandsById.keySet());
	
			if (!Util.equals(definedCommandIds, this.definedCommandIds)) {	
				this.definedCommandIds = definedCommandIds;
				commandManagerChanged = true;
			}
		}

		if (!keyConfigurationChanges.isEmpty()) {
			this.keyConfigurationsById = keyConfigurationsById;		
			SortedSet definedKeyConfigurationIds = new TreeSet(keyConfigurationsById.keySet());
	
			if (!Util.equals(definedKeyConfigurationIds, this.definedKeyConfigurationIds)) {	
				this.definedKeyConfigurationIds = definedKeyConfigurationIds;
				commandManagerChanged = true;
			}
		}

		if (commandManagerChanged)
			fireCommandManagerChanged();

		if (!categoryChanges.isEmpty()) {
			Iterator iterator = categoryChanges.iterator();
		
			while (iterator.hasNext()) {
				String categoryId = (String) iterator.next();					
				CategoryHandle categoryHandle = (CategoryHandle) categoryHandlesById.get(categoryId);
			
				if (categoryHandle != null) {			
					if (categoriesById.containsKey(categoryId))
						categoryHandle.define((ICategory) categoriesById.get(categoryId));
					else
						categoryHandle.undefine();
				}
			}			
		}

		if (!commandChanges.isEmpty()) {
			Iterator iterator = commandChanges.iterator();
		
			while (iterator.hasNext()) {
				String commandId = (String) iterator.next();					
				CommandHandle commandHandle = (CommandHandle) commandHandlesById.get(commandId);
			
				if (commandHandle != null) {			
					if (commandsById.containsKey(commandId))
						commandHandle.define((ICommand) commandsById.get(commandId));
					else
						commandHandle.undefine();
				}
			}			
		}
		
		if (!keyConfigurationChanges.isEmpty()) {
			Iterator iterator = keyConfigurationChanges.iterator();
		
			while (iterator.hasNext()) {
				String keyConfigurationId = (String) iterator.next();					
				KeyConfigurationHandle keyConfigurationHandle = (KeyConfigurationHandle) keyConfigurationHandlesById.get(keyConfigurationId);
			
				if (keyConfigurationHandle != null) {			
					if (keyConfigurationsById.containsKey(keyConfigurationId))
						keyConfigurationHandle.define((IKeyConfiguration) keyConfigurationsById.get(keyConfigurationId));
					else
						keyConfigurationHandle.undefine();
				}
			}			
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
}
