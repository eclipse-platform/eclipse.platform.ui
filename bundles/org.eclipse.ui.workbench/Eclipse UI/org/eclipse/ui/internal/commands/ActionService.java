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

import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.commands.IActionService;
import org.eclipse.ui.internal.ActionDescriptor;

public final class ActionService implements IActionService {
	
	private SortedMap commandIdToActionMap = new TreeMap();
	
	public ActionService() {
		super();
	}

	IAction getAction(String command) {
		return (IAction) commandIdToActionMap.get(command);
	}
	
	void registerExtendedActions(ActionDescriptor[] actionDescriptors) {
		if (actionDescriptors != null) {
			for (int i = 0; i < actionDescriptors.length; i++) {
				ActionDescriptor actionDescriptor = actionDescriptors[i];
				
				if (actionDescriptor != null) {
					IAction action = actionDescriptors[i].getAction();
			
					if (action != null && action.getActionDefinitionId() != null)
						registerAction(action);
				}
			}
		}		
	}

	/*
	 * @see IActionService#registerAction(IAction)
	 */
	public void registerAction(IAction action) {
    	String command = action.getActionDefinitionId();

		if (command != null)
			commandIdToActionMap.put(command, action);
    }
    
   	/*
	 * @see IActionService#unregisterAction(IAction)
	 */
	public void unregisterAction(IAction action) {   		
    	String command = action.getActionDefinitionId();

		if (command != null)
			commandIdToActionMap.remove(command);
    }
}
