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
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.ui.commands.ICommandActivationService;
import org.eclipse.ui.commands.ICommandActivationServiceEvent;
import org.eclipse.ui.commands.ICommandActivationServiceListener;

public final class CommandActivationService implements ICommandActivationService {

	private final static SortedSet EMPTY_SORTED_SET = Collections.unmodifiableSortedSet(new TreeSet());

	private SortedSet activeCommandIds;
	private ICommandActivationServiceEvent commandActivationServiceEvent;
	private List commandActivationServiceListeners;

	public CommandActivationService() {
		super();
	}

	public void activateCommand(String commandId)
		throws IllegalArgumentException {
		if (commandId == null)
			throw new IllegalArgumentException();

		if (activeCommandIds == null)
			activeCommandIds = new TreeSet();
			
		if (activeCommandIds.add(commandId))
			fireCommandActivationServiceChanged();
	}

	public void addCommandActivationServiceListener(ICommandActivationServiceListener commandActivationServiceListener)
		throws IllegalArgumentException {
		if (commandActivationServiceListener == null)
			throw new IllegalArgumentException();
			
		if (commandActivationServiceListeners == null)
			commandActivationServiceListeners = new ArrayList();
		
		if (!commandActivationServiceListeners.contains(commandActivationServiceListener))
			commandActivationServiceListeners.add(commandActivationServiceListener);
	}

	public void deactivateCommand(String commandId)
		throws IllegalArgumentException {
		if (commandId == null)
			throw new IllegalArgumentException();

		if (activeCommandIds != null && activeCommandIds.remove(commandId)) {			
			if (activeCommandIds.isEmpty())
				activeCommandIds = null;

			fireCommandActivationServiceChanged();
		}			
	}

	public SortedSet getActiveCommandIds() {
		return activeCommandIds != null ? Collections.unmodifiableSortedSet(activeCommandIds) : EMPTY_SORTED_SET;
	}
	
	public void removeCommandActivationServiceListener(ICommandActivationServiceListener commandActivationServiceListener)
		throws IllegalArgumentException {
		if (commandActivationServiceListener == null)
			throw new IllegalArgumentException();
			
		if (commandActivationServiceListeners != null) {
			commandActivationServiceListeners.remove(commandActivationServiceListener);
			
			if (commandActivationServiceListeners.isEmpty())
				commandActivationServiceListeners = null;
		}
	}
	
	private void fireCommandActivationServiceChanged() {
		if (commandActivationServiceListeners != null) {
			Iterator iterator = commandActivationServiceListeners.iterator();
			
			if (iterator.hasNext()) {
				if (commandActivationServiceEvent == null)
					commandActivationServiceEvent = new CommandActivationServiceEvent(this);
				
				while (iterator.hasNext())	
					((ICommandActivationServiceListener) iterator.next()).commandActivationServiceChanged(commandActivationServiceEvent);
			}							
		}			
	}	
}
