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

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.ICommandManagerEvent;
import org.eclipse.ui.commands.ICommandManagerListener;
import org.eclipse.ui.internal.util.Util;

public final class CommandManager implements ICommandManager {

	private SortedSet activeCommandIds = new TreeSet();
	private SortedMap commandElementsById = new TreeMap();
	private ICommandManagerEvent commandManagerEvent;
	private List commandManagerListeners;
	private SortedMap commandsById = new TreeMap();
	private SortedSet definedCommandIds = new TreeSet();
	private RegistryReader registryReader;

	public CommandManager() {
		super();
		updateDefinedCommandIds();
	}

	public void addCommandManagerListener(ICommandManagerListener commandManagerListener)
		throws IllegalArgumentException {
		if (commandManagerListener == null)
			throw new IllegalArgumentException();
			
		if (commandManagerListeners == null)
			commandManagerListeners = new ArrayList();
		
		if (!commandManagerListeners.contains(commandManagerListener))
			commandManagerListeners.add(commandManagerListener);
	}

	public SortedSet getActiveCommandIds() {
		return Collections.unmodifiableSortedSet(activeCommandIds);
	}

	public ICommand getCommand(String commandId)
		throws IllegalArgumentException {
		if (commandId == null)
			throw new IllegalArgumentException();
			
		ICommand command = (ICommand) commandsById.get(commandId);
		
		if (command == null) {
			command = new Command(this, commandId);
			commandsById.put(commandId, command);
		}
		
		return command;
	}

	public SortedSet getDefinedCommandIds() {
		return Collections.unmodifiableSortedSet(activeCommandIds);
	}

	public void removeCommandManagerListener(ICommandManagerListener commandManagerListener)
		throws IllegalArgumentException {
		if (commandManagerListener == null)
			throw new IllegalArgumentException();
			
		if (commandManagerListeners != null) {
			commandManagerListeners.remove(commandManagerListener);
			
			if (commandManagerListeners.isEmpty())
				commandManagerListeners = null;
		}
	}

	public void setActiveCommandIds(SortedSet activeCommandIds)
		throws IllegalArgumentException {
		activeCommandIds = Util.safeCopy(activeCommandIds, String.class);
		SortedSet activatingCommandIds = new TreeSet();
		SortedSet deactivatingCommandIds = new TreeSet();
		Iterator iterator = activeCommandIds.iterator();

		while (iterator.hasNext()) {
			String id = (String) iterator.next();
		
			if (!this.activeCommandIds.contains(id))
				activatingCommandIds.add(id);
		}

		iterator = this.activeCommandIds.iterator();
		
		while (iterator.hasNext()) {
			String id = (String) iterator.next();
		
			if (!activeCommandIds.contains(id))
				deactivatingCommandIds.add(id);					
		}		

		SortedSet commandChanges = new TreeSet();
		commandChanges.addAll(activatingCommandIds);		
		commandChanges.addAll(deactivatingCommandIds);			

		if (!commandChanges.isEmpty()) {
			this.activeCommandIds = activeCommandIds;	
			fireCommandManagerChanged();

			iterator = commandChanges.iterator();
		
			while (iterator.hasNext())
				fireCommandChanged((String) iterator.next());
		}
	}

	public void updateDefinedCommandIds() {
		if (registryReader == null)
			registryReader = new RegistryReader(Platform.getPluginRegistry());
		
		registryReader.load();
		SortedMap commandElementsById = CommandElement.sortedMapById(registryReader.getCommandElements());		
		SortedSet commandElementAdditions = new TreeSet();		
		SortedSet commandElementChanges = new TreeSet();
		SortedSet commandElementRemovals = new TreeSet();		
		Iterator iterator = commandElementsById.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String id = (String) entry.getKey();
			CommandElement commandElement = (CommandElement) entry.getValue();
			
			if (!this.commandElementsById.containsKey(id))
				commandElementAdditions.add(id);
			else if (!Util.equals(commandElement, this.commandElementsById.get(id)))
				commandElementChanges.add(id);								
		}

		iterator = this.commandElementsById.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String id = (String) entry.getKey();
			CommandElement commandElement = (CommandElement) entry.getValue();
			
			if (!commandElementsById.containsKey(id))
				commandElementRemovals.add(id);						
		}

		SortedSet commandChanges = new TreeSet();
		commandChanges.addAll(commandElementAdditions);		
		commandChanges.addAll(commandElementChanges);		
		commandChanges.addAll(commandElementRemovals);
		
		if (!commandChanges.isEmpty()) {
			this.commandElementsById = commandElementsById;		
			SortedSet definedCommandIds = new TreeSet(commandElementsById.keySet());

			if (!Util.equals(definedCommandIds, this.definedCommandIds)) {	
				this.definedCommandIds = definedCommandIds;
				fireCommandManagerChanged();
			}

			iterator = commandChanges.iterator();
		
			while (iterator.hasNext())
				fireCommandChanged((String) iterator.next());
		}
	}

	CommandElement getCommandElement(String commandId) {
		return (CommandElement) commandElementsById.get(commandId);
	}
	
	private void fireCommandChanged(String commandId) {
		Command command = (Command) commandsById.get(commandId);
		
		if (command != null) 
			command.fireCommandChanged();		
	}

	private void fireCommandManagerChanged() {
		if (commandManagerListeners != null) {
			Iterator iterator = commandManagerListeners.iterator();
			
			if (iterator.hasNext()) {
				if (commandManagerEvent == null)
					commandManagerEvent = new CommandManagerEvent(this);
				
				while (iterator.hasNext())	
					((ICommandManagerListener) iterator.next()).commandManagerChanged(commandManagerEvent);
			}							
		}			
	}
}
