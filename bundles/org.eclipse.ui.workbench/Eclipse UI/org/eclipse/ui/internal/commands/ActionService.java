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
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.ui.commands.IAction;
import org.eclipse.ui.commands.IActionService;
import org.eclipse.ui.commands.IActionServiceEvent;
import org.eclipse.ui.commands.IActionServiceListener;
import org.eclipse.ui.internal.util.Util;

public final class ActionService implements IActionService {

	private SortedMap actionsById;
	private IActionServiceEvent actionServiceEvent;
	private List actionServiceListeners;

	public ActionService() {
	}

	public void addAction(String commandId, IAction action) {
		if (commandId == null)
			throw new NullPointerException();

		if (actionsById == null)
			actionsById = new TreeMap();
			
		if (actionsById.get(commandId) != action) {
			actionsById.put(commandId, action);
			fireActionServiceChanged();
		}
	}

	public void addActionServiceListener(IActionServiceListener actionServiceListener) {
		if (actionServiceListener == null)
			throw new NullPointerException();
			
		if (actionServiceListeners == null)
			actionServiceListeners = new ArrayList();
		
		if (!actionServiceListeners.contains(actionServiceListener))
			actionServiceListeners.add(actionServiceListener);
	}

	public SortedMap getActionsById() {
		return actionsById != null ? Collections.unmodifiableSortedMap(actionsById) : Util.EMPTY_SORTED_MAP;
	}

	public void removeAction(String commandId) {
		if (commandId == null)
			throw new NullPointerException();

		if (actionsById != null && actionsById.containsKey(commandId)) {
			actionsById.remove(commandId);
						
			if (actionsById.isEmpty())
				actionsById = null;

			fireActionServiceChanged();
		}			
	}
	
	public void removeActionServiceListener(IActionServiceListener actionServiceListener) {
		if (actionServiceListener == null)
			throw new NullPointerException();
			
		if (actionServiceListeners != null) {
			actionServiceListeners.remove(actionServiceListener);
			
			if (actionServiceListeners.isEmpty())
				actionServiceListeners = null;
		}
	}
	
	private void fireActionServiceChanged() {
		if (actionServiceListeners != null) {
			for (int i = 0; i < actionServiceListeners.size(); i++) {
				if (actionServiceEvent == null)
					actionServiceEvent = new ActionServiceEvent(this);
							
				((IActionServiceListener) actionServiceListeners.get(i)).actionServiceChanged(actionServiceEvent);
			}				
		}
	}	
}
