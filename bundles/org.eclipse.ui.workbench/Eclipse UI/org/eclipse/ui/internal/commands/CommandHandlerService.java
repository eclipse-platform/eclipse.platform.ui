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
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.ui.commands.ICommandHandler;
import org.eclipse.ui.commands.ICommandHandlerService;
import org.eclipse.ui.commands.ICommandHandlerServiceEvent;
import org.eclipse.ui.commands.ICommandHandlerServiceListener;
import org.eclipse.ui.internal.util.Util;

public final class CommandHandlerService implements ICommandHandlerService {

	private SortedMap commandHandlersById;
	private ICommandHandlerServiceEvent commandHandlerServiceEvent;
	private List commandHandlerServiceListeners;

	public CommandHandlerService() {
		super();
	}

	public void addCommandHandler(String commandId, ICommandHandler commandHandler)
		throws IllegalArgumentException {
		if (commandId == null)
			throw new IllegalArgumentException();

		if (commandHandlersById == null)
			commandHandlersById = new TreeMap();
			
		if (commandHandlersById.get(commandId) != commandHandler) {
			commandHandlersById.put(commandId, commandHandler);
			fireCommandHandlerServiceChanged();
		}
	}

	public void addCommandHandlerServiceListener(ICommandHandlerServiceListener commandHandlerServiceListener)
		throws IllegalArgumentException {
		if (commandHandlerServiceListener == null)
			throw new IllegalArgumentException();
			
		if (commandHandlerServiceListeners == null)
			commandHandlerServiceListeners = new ArrayList();
		
		if (!commandHandlerServiceListeners.contains(commandHandlerServiceListener))
			commandHandlerServiceListeners.add(commandHandlerServiceListener);
	}

	public SortedMap getCommandHandlersById() {
		return commandHandlersById != null ? Collections.unmodifiableSortedMap(commandHandlersById) : Util.EMPTY_SORTED_MAP;
	}

	public void removeCommandHandler(String commandId)
		throws IllegalArgumentException {
		if (commandId == null)
			throw new IllegalArgumentException();

		if (commandHandlersById != null && commandHandlersById.containsKey(commandId)) {
			commandHandlersById.remove(commandId);
						
			if (commandHandlersById.isEmpty())
				commandHandlersById = null;

			fireCommandHandlerServiceChanged();
		}			
	}
	
	public void removeCommandHandlerServiceListener(ICommandHandlerServiceListener commandHandlerServiceListener)
		throws IllegalArgumentException {
		if (commandHandlerServiceListener == null)
			throw new IllegalArgumentException();
			
		if (commandHandlerServiceListeners != null) {
			commandHandlerServiceListeners.remove(commandHandlerServiceListener);
			
			if (commandHandlerServiceListeners.isEmpty())
				commandHandlerServiceListeners = null;
		}
	}
	
	private void fireCommandHandlerServiceChanged() {
		if (commandHandlerServiceListeners != null) {
			Iterator iterator = commandHandlerServiceListeners.iterator();
			
			if (iterator.hasNext()) {
				if (commandHandlerServiceEvent == null)
					commandHandlerServiceEvent = new CommandHandlerServiceEvent(this);
				
				while (iterator.hasNext())	
					((ICommandHandlerServiceListener) iterator.next()).commandHandlerServiceChanged(commandHandlerServiceEvent);
			}							
		}			
	}	
}
