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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandDefinition;
import org.eclipse.ui.commands.ICommandDelegate;
import org.eclipse.ui.commands.ICommandHandle;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.ICommandManagerEvent;
import org.eclipse.ui.commands.ICommandManagerListener;
import org.eclipse.ui.commands.ICommandRegistry;
import org.eclipse.ui.commands.ICommandRegistryEvent;
import org.eclipse.ui.commands.ICommandRegistryListener;
import org.eclipse.ui.internal.commands.registry.CommandRegistry;
import org.eclipse.ui.internal.util.Util;

public final class CommandManager implements ICommandManager {

	private static CommandManager instance;

	public static CommandManager getInstance() {
		if (instance == null)
			instance = new CommandManager();
			
		return instance;
	}

	private SortedSet activeCommandIds = new TreeSet();
	private ICommandManagerEvent commandManagerEvent;
	private SortedMap commandDelegatesById = new TreeMap();	
	private List commandManagerListeners;
	private SortedMap commandHandlesById = new TreeMap();
	private ICommandRegistry commandRegistry;
	private SortedMap commandsById = new TreeMap();

	private CommandManager() {
		super();
		commandRegistry = CommandRegistry.getInstance();		
		commandRegistry.addCommandRegistryListener(new ICommandRegistryListener() {
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

	public SortedSet getActiveCommandIds() {
		return Collections.unmodifiableSortedSet(activeCommandIds);
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

	public ICommandRegistry getCommandRegistry() {
		return commandRegistry;
	}

	public SortedMap getCommandsById() {
		return Collections.unmodifiableSortedMap(commandsById);
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

	public void setActiveCommandIds(SortedSet activeCommandIds) {
		activeCommandIds = Util.safeCopy(activeCommandIds, String.class);
		
		if (!activeCommandIds.equals(this.activeCommandIds)) {
			this.activeCommandIds = activeCommandIds;	
			update();
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

	private void update() {
		SortedMap commandDefinitionsById = commandRegistry.getCommandDefinitionsById();
		SortedMap commandsById = new TreeMap();
		Iterator iterator = commandDefinitionsById.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			Object key = entry.getKey();
			ICommandDefinition commandDefinition = (ICommandDefinition) entry.getValue();
			ICommand command = new Command(activeCommandIds.contains(commandDefinition.getId()), commandDefinition.getCategoryId(), commandDefinition.getDescription(), commandDefinition.getId(), commandDefinition.getName(), commandDefinition.getPluginId());		
			commandsById.put(key, command);
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
				CommandHandle commandHandle = (CommandHandle) commandHandlesById.get(commandId);
			
				if (commandHandle != null) {			
					if (commandsById.containsKey(commandId))
						commandHandle.define((ICommand) commandsById.get(commandId));
					else
						commandHandle.undefine();
				}
			}			
		}
	}			
}
