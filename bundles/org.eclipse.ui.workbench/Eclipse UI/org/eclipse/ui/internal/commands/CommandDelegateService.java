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

import org.eclipse.ui.commands.ICommandDelegate;
import org.eclipse.ui.commands.ICommandDelegateService;
import org.eclipse.ui.commands.ICommandDelegateServiceEvent;
import org.eclipse.ui.commands.ICommandDelegateServiceListener;
import org.eclipse.ui.internal.util.Util;

public final class CommandDelegateService implements ICommandDelegateService {

	private SortedMap commandDelegatesById;
	private ICommandDelegateServiceEvent commandDelegateServiceEvent;
	private List commandDelegateServiceListeners;

	public CommandDelegateService() {
		super();
	}

	public void addCommandDelegate(String commandId, ICommandDelegate commandDelegate) {
		if (commandId == null)
			throw new NullPointerException();

		if (commandDelegatesById == null)
			commandDelegatesById = new TreeMap();
			
		if (commandDelegatesById.get(commandId) != commandDelegate) {
			commandDelegatesById.put(commandId, commandDelegate);
			fireCommandDelegateServiceChanged();
		}
	}

	public void addCommandDelegateServiceListener(ICommandDelegateServiceListener commandDelegateServiceListener) {
		if (commandDelegateServiceListener == null)
			throw new NullPointerException();
			
		if (commandDelegateServiceListeners == null)
			commandDelegateServiceListeners = new ArrayList();
		
		if (!commandDelegateServiceListeners.contains(commandDelegateServiceListener))
			commandDelegateServiceListeners.add(commandDelegateServiceListener);
	}

	public SortedMap getCommandDelegatesById() {
		return commandDelegatesById != null ? Collections.unmodifiableSortedMap(commandDelegatesById) : Util.EMPTY_SORTED_MAP;
	}

	public void removeCommandDelegate(String commandId) {
		if (commandId == null)
			throw new NullPointerException();

		if (commandDelegatesById != null && commandDelegatesById.containsKey(commandId)) {
			commandDelegatesById.remove(commandId);
						
			if (commandDelegatesById.isEmpty())
				commandDelegatesById = null;

			fireCommandDelegateServiceChanged();
		}			
	}
	
	public void removeCommandDelegateServiceListener(ICommandDelegateServiceListener commandDelegateServiceListener) {
		if (commandDelegateServiceListener == null)
			throw new NullPointerException();
			
		if (commandDelegateServiceListeners != null) {
			commandDelegateServiceListeners.remove(commandDelegateServiceListener);
			
			if (commandDelegateServiceListeners.isEmpty())
				commandDelegateServiceListeners = null;
		}
	}
	
	private void fireCommandDelegateServiceChanged() {
		if (commandDelegateServiceListeners != null) {
			// TODO copying to avoid ConcurrentModificationException
			Iterator iterator = new ArrayList(commandDelegateServiceListeners).iterator();
			
			if (iterator.hasNext()) {
				if (commandDelegateServiceEvent == null)
					commandDelegateServiceEvent = new CommandDelegateServiceEvent(this);
				
				while (iterator.hasNext())	
					((ICommandDelegateServiceListener) iterator.next()).commandDelegateServiceChanged(commandDelegateServiceEvent);
			}							
		}			
	}	
}
