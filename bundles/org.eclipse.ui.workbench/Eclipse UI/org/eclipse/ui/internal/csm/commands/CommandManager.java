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
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.csm.commands.api.ICommand;
import org.eclipse.ui.internal.csm.commands.api.ICommandEvent;
import org.eclipse.ui.internal.csm.commands.api.ICommandManager;
import org.eclipse.ui.internal.csm.commands.api.ICommandManagerEvent;
import org.eclipse.ui.internal.csm.commands.api.ICommandManagerListener;
import org.eclipse.ui.internal.csm.commands.api.IPatternBinding;
import org.eclipse.ui.internal.util.Util;

public final class CommandManager implements ICommandManager {

	static boolean isCommandDefinitionChildOf(String ancestor, String id, Map commandDefinitionsById) {
		Collection visited = new HashSet();

		while (id != null && !visited.contains(id)) {
			ICommandDefinition commandDefinition = (ICommandDefinition) commandDefinitionsById.get(id);				
			visited.add(id);

			if (commandDefinition != null && Util.equals(id = commandDefinition.getParentId(), ancestor))
				return true;
		}

		return false;
	}	

	private Set activeCommandIds = new HashSet();
	private Map commandsById = new WeakHashMap();
	private Set commandsWithListeners = new HashSet();
	private Map commandDefinitionsById = new HashMap();
	private List commandManagerListeners;
	private ICommandRegistry commandRegistry;	
	private Set definedCommandIds = new HashSet();
	private Set enabledCommandIds = new HashSet();	
	private Map patternBindingsByCommandId = new HashMap();

	public CommandManager() {
		this(new ExtensionCommandRegistry(Platform.getExtensionRegistry()));
	}

	public CommandManager(ICommandRegistry commandRegistry) {
		if (commandRegistry == null)
			throw new NullPointerException();

		this.commandRegistry = commandRegistry;
		
		this.commandRegistry.addCommandRegistryListener(new ICommandRegistryListener() {
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

	public ICommand getCommand(String commandId) {
		if (commandId == null)
			throw new NullPointerException();
			
		Command command = (Command) commandsById.get(commandId);
		
		if (command == null) {
			command = new Command(this, commandId);
			updateCommand(command);
			commandsById.put(commandId, command);
		}
		
		return command;
	}
	
	public Set getDefinedCommandIds() {
		return Collections.unmodifiableSet(definedCommandIds);
	}

	public Set getEnabledCommandIds() {
		return Collections.unmodifiableSet(enabledCommandIds);
	}	

	public boolean match(String string, Set commandIds) {
		commandIds = Util.safeCopy(commandIds, String.class);
		
		for (Iterator iterator = commandIds.iterator(); iterator.hasNext();) {			
			ICommand command = getCommand((String) iterator.next());
						
			if (command.match(string))
				return true;
		}
			
		return false;
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
	
	Set getCommandsWithListeners() {
		return commandsWithListeners;
	}

	private void fireCommandManagerChanged(ICommandManagerEvent commandManagerEvent) {
		if (commandManagerEvent == null)
			throw new NullPointerException();
		
		if (commandManagerListeners != null)
			for (int i = 0; i < commandManagerListeners.size(); i++)
				((ICommandManagerListener) commandManagerListeners.get(i)).commandManagerChanged(commandManagerEvent);
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

	private void readRegistry() {
		Collection commandDefinitions = new ArrayList();
		commandDefinitions.addAll(commandRegistry.getCommandDefinitions());				
		Map commandDefinitionsById = new HashMap(CommandDefinition.commandDefinitionsById(commandDefinitions, false));

		for (Iterator iterator = commandDefinitionsById.values().iterator(); iterator.hasNext();) {
			ICommandDefinition commandDefinition = (ICommandDefinition) iterator.next();
			String name = commandDefinition.getName();
				
			if (name == null || name.length() == 0)
				iterator.remove();
		}

		for (Iterator iterator = commandDefinitionsById.keySet().iterator(); iterator.hasNext();)
			if (!isCommandDefinitionChildOf(null, (String) iterator.next(), commandDefinitionsById))
				iterator.remove();

		Map commandPatternBindingDefinitionsByCommandId = CommandPatternBindingDefinition.commandPatternBindingDefinitionsByCommandId(commandRegistry.getCommandPatternBindingDefinitions());
		Map patternBindingsByCommandId = new HashMap();		

		for (Iterator iterator = commandPatternBindingDefinitionsByCommandId.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String commandId = (String) entry.getKey();
			
			if (commandDefinitionsById.containsKey(commandId)) {			
				Collection commandPatternBindingDefinitions = (Collection) entry.getValue();
				
				if (commandPatternBindingDefinitions != null)
					for (Iterator iterator2 = commandPatternBindingDefinitions.iterator(); iterator2.hasNext();) {
						ICommandPatternBindingDefinition commandPatternBindingDefinition = (ICommandPatternBindingDefinition) iterator2.next();
						String pattern = commandPatternBindingDefinition.getPattern();
					
						if (pattern != null && pattern.length() != 0) {
							IPatternBinding patternBinding = new PatternBinding(commandPatternBindingDefinition.isInclusive(), Pattern.compile(pattern));	
							List patternBindings = (List) patternBindingsByCommandId.get(commandId);
							
							if (patternBindings == null) {
								patternBindings = new ArrayList();
								patternBindingsByCommandId.put(commandId, patternBindings);
							}
							
							patternBindings.add(patternBinding);
						}
					}
			}
		}		
		
		this.commandDefinitionsById = commandDefinitionsById;
		this.patternBindingsByCommandId = patternBindingsByCommandId;			
		boolean commandManagerChanged = false;			
		Set definedCommandIds = new HashSet(commandDefinitionsById.keySet());		

		if (!definedCommandIds.equals(this.definedCommandIds)) {
			this.definedCommandIds = definedCommandIds;
			commandManagerChanged = true;	
		}

		Map commandEventsByCommandId = updateCommands(commandsById.keySet());	
		
		if (commandManagerChanged)
			fireCommandManagerChanged(new CommandManagerEvent(this, false, true, false));

		if (commandEventsByCommandId != null)
			notifyCommands(commandEventsByCommandId);		
	}

	private ICommandEvent updateCommand(Command command) {
		boolean activeChanged = command.setActive(activeCommandIds.contains(command.getId()));		
		ICommandDefinition commandDefinition = (ICommandDefinition) commandDefinitionsById.get(command.getId());
		boolean definedChanged = command.setDefined(commandDefinition != null);
		boolean descriptionChanged = command.setDescription(commandDefinition != null ? commandDefinition.getDescription() : null);		
		boolean enabledChanged = command.setEnabled(enabledCommandIds.contains(command.getId()));
		boolean nameChanged = command.setName(commandDefinition != null ? commandDefinition.getName() : null);
		boolean parentIdChanged = command.setParentId(commandDefinition != null ? commandDefinition.getParentId() : null);				
		List patternBindings = (List) patternBindingsByCommandId.get(command.getId());
		boolean patternBindingsChanged = command.setPatternBindings(patternBindings != null ? patternBindings : Collections.EMPTY_LIST);

		if (activeChanged || definedChanged || descriptionChanged || enabledChanged || nameChanged || parentIdChanged || patternBindingsChanged)
			return new CommandEvent(command, activeChanged, definedChanged, descriptionChanged, enabledChanged, nameChanged, parentIdChanged, patternBindingsChanged); 
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
}
