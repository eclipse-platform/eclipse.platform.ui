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
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.ICommandManagerEvent;
import org.eclipse.ui.commands.ICommandManagerListener;
import org.eclipse.ui.handles.IHandle;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.commands.registry.ICommandDefinition;
import org.eclipse.ui.internal.commands.registry.ICommandRegistry;
import org.eclipse.ui.internal.commands.registry.ICommandRegistryEvent;
import org.eclipse.ui.internal.commands.registry.ICommandRegistryListener;
import org.eclipse.ui.internal.commands.registry.PluginCommandRegistry;
import org.eclipse.ui.internal.commands.registry.PreferenceCommandRegistry;
import org.eclipse.ui.internal.handles.Handle;
import org.eclipse.ui.internal.util.Util;

public final class CommandManager implements ICommandManager {

	private static CommandManager instance;

	public static CommandManager getInstance() {
		if (instance == null)
			instance = new CommandManager();
			
		return instance;
	}

	private SortedSet activeCommandIds = new TreeSet();
	private SortedSet activeContextIds = new TreeSet();
	private String activeKeyConfigurationId = Util.ZERO_LENGTH_STRING;
	private String activeLocale = Util.ZERO_LENGTH_STRING;
	private String activePlatform = Util.ZERO_LENGTH_STRING;	
	private ICommandManagerEvent commandManagerEvent;
	private SortedMap actionsById = new TreeMap();	
	private List commandManagerListeners;
	private SortedMap commandHandlesById = new TreeMap();
	private SortedMap commandsById = new TreeMap();
	private PluginCommandRegistry pluginCommandRegistry;
	private PreferenceCommandRegistry preferenceCommandRegistry;

	private CommandManager() {
		if (pluginCommandRegistry == null)
			pluginCommandRegistry = new PluginCommandRegistry(Platform.getPluginRegistry());
			
		loadPluginCommandRegistry();		

		pluginCommandRegistry.addCommandRegistryListener(new ICommandRegistryListener() {
			public void commandRegistryChanged(ICommandRegistryEvent commandRegistryEvent) {
				update();
			}
		});

		if (preferenceCommandRegistry == null)
			preferenceCommandRegistry = new PreferenceCommandRegistry(WorkbenchPlugin.getDefault().getPreferenceStore());	

		loadPreferenceCommandRegistry();

		preferenceCommandRegistry.addCommandRegistryListener(new ICommandRegistryListener() {
			public void commandRegistryChanged(ICommandRegistryEvent commandRegistryEvent) {
				update();
			}
		});
		
		update();
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

	public SortedMap getCategoriesById() {
		// TODO 
		return null;
	}
	
	public IHandle getCategoryHandle(String categoryId) {
		// TODO
		return null;
	}

	public IHandle getCommandHandle(String commandId) {
		if (commandId == null)
			throw new NullPointerException();
			
		IHandle handle = (IHandle) commandHandlesById.get(commandId);
		
		if (handle == null) {
			handle = new Handle(commandId);
			commandHandlesById.put(commandId, handle);
		}
		
		return handle;
	}

	public SortedMap getCommandsById() {
		return Collections.unmodifiableSortedMap(commandsById);
	}

	public IHandle getKeyConfigurationHandle(String keyConfigurationId) {
		// TODO
		return null;
	}

	public SortedMap getKeyConfigurationsById() {
		// TODO
		return null;
	}
	
	public ICommandRegistry getPluginCommandRegistry() {
		return pluginCommandRegistry;
	}

	public ICommandRegistry getPreferenceCommandRegistry() {
		return preferenceCommandRegistry;
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
		
		if (!activeCommandIds.equals(this.activeCommandIds)) {
			this.activeCommandIds = activeCommandIds;	
			update();
		}
	}

	public void setActiveContextIds(SortedSet activeContextIds) {
		activeContextIds = Util.safeCopy(activeContextIds, String.class);
		
		if (!activeContextIds.equals(this.activeContextIds)) {
			this.activeContextIds = activeContextIds;	
			update();
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

	private void loadPluginCommandRegistry() {
		try {
			pluginCommandRegistry.load();
		} catch (IOException eIO) {
			// TODO proper catch
		}
	}
	
	private void loadPreferenceCommandRegistry() {
		try {
			preferenceCommandRegistry.load();
		} catch (IOException eIO) {
			// TODO proper catch
		}		
	}

	private void update() {
		List commandDefinitions = new ArrayList();
		commandDefinitions.addAll(pluginCommandRegistry.getCommandDefinitions());
		commandDefinitions.addAll(preferenceCommandRegistry.getCommandDefinitions());
		SortedMap commandsById = new TreeMap();
		Iterator iterator = commandDefinitions.iterator();
		
		while (iterator.hasNext()) {
			ICommandDefinition commandDefinition = (ICommandDefinition) iterator.next();
			ICommand command = new Command(activeCommandIds.contains(commandDefinition.getId()), commandDefinition.getCategoryId(), Collections.EMPTY_LIST, commandDefinition.getDescription(), commandDefinition.getHelpId(), commandDefinition.getId(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, commandDefinition.getName());
			commandsById.put(command.getId(), command);
		}

		SortedSet commandChanges = new TreeSet();
		Util.diff(commandsById, this.commandsById, commandChanges, commandChanges, commandChanges);
		boolean commandManagerChanged = false;
				
		if (!commandChanges.isEmpty()) {
			this.commandsById = commandsById;		
			commandManagerChanged = true;
		}

		if (commandManagerChanged)
			fireCommandManagerChanged();

		if (!commandChanges.isEmpty()) {
			iterator = commandChanges.iterator();
		
			while (iterator.hasNext()) {
				String commandId = (String) iterator.next();					
				Handle handle = (Handle) commandHandlesById.get(commandId);
			
				if (handle != null) {			
					if (commandsById.containsKey(commandId))
						handle.define((ICommand) commandsById.get(commandId));
					else
						handle.undefine();
				}
			}			
		}
	}			
}
