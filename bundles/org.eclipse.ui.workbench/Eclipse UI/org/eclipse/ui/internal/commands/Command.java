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
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandEvent;
import org.eclipse.ui.commands.ICommandListener;
import org.eclipse.ui.commands.NotDefinedException;
import org.eclipse.ui.commands.NotHandledException;
import org.eclipse.ui.commands.old.ICommandHandler;

final class Command implements ICommand {

	private ICommandEvent commandEvent;
	private List commandListeners;
	private CommandManager commandManager;
	private String id;

	Command(CommandManager commandManager, String id) {
		super();
		this.commandManager = commandManager;
		this.id = id;
	}

	public void addCommandListener(ICommandListener commandListener)
		throws IllegalArgumentException {
		if (commandListener == null)
			throw new IllegalArgumentException();
		
		if (commandListeners == null)
			commandListeners = new ArrayList();
		
		if (!commandListeners.contains(commandListener))
			commandListeners.add(commandListener);
	}

	public ICommandHandler getCommandHandler()
		throws NotHandledException {
		SortedMap commandHandlersById = commandManager.getCommandHandlersById();
		ICommandHandler commandHandler = (ICommandHandler) commandHandlersById.get(id);
		
		if (commandHandlersById.containsKey(id))
			return commandHandler;
		else 		
			throw new NotHandledException();
	}

	public String getDescription() 
		throws NotDefinedException {
		CommandElement commandElement = (CommandElement) commandManager.getCommandElement(id);

		if (commandElement != null && commandManager.getDefinedCommandIds().contains(id))
			return commandElement.getDescription();
		else 
			throw new NotDefinedException();
	}

	public String getId() {
		return id;
	}

	public String getName() 
		throws NotDefinedException {
		CommandElement commandElement = (CommandElement) commandManager.getCommandElement(id);

		if (commandElement != null && commandManager.getDefinedCommandIds().contains(id))
			return commandElement.getName();
		else 
			throw new NotDefinedException();
	}

	public String getParentId() 
		throws NotDefinedException {
		CommandElement commandElement = (CommandElement) commandManager.getCommandElement(id);

		if (commandElement != null && commandManager.getDefinedCommandIds().contains(id))
			return commandElement.getParentId();
		else 
			throw new NotDefinedException();
	}

	public String getPluginId() 
		throws NotDefinedException {
		CommandElement commandElement = (CommandElement) commandManager.getCommandElement(id);

		if (commandElement != null && commandManager.getDefinedCommandIds().contains(id))
			return commandElement.getPluginId();
		else 
			throw new NotDefinedException();
	}

	public boolean isDefined() {
		return commandManager.getCommandElement(id) != null && commandManager.getDefinedCommandIds().contains(id);
	}

	public boolean isHandled() {
		return commandManager.getCommandHandlersById().containsKey(id);
	}

	public void removeCommandListener(ICommandListener commandListener)
		throws IllegalArgumentException {
		if (commandListener == null)
			throw new IllegalArgumentException();

		if (commandListeners != null) {
			commandListeners.remove(commandListener);
			
			if (commandListeners.isEmpty())
				commandListeners = null;
		}
	}
	
	void fireCommandChanged() {
		if (commandListeners != null) {
			Iterator iterator = commandListeners.iterator();
			
			if (iterator.hasNext()) {
				if (commandEvent == null)
					commandEvent = new CommandEvent(this);
				
				while (iterator.hasNext())	
					((ICommandListener) iterator.next()).commandChanged(commandEvent);
			}							
		}			
	}		
}
