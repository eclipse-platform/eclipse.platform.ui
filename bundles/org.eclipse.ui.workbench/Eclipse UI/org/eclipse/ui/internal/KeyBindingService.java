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

package org.eclipse.ui.internal;

import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.commands.IActionService;
import org.eclipse.ui.commands.IContextService;

final class KeyBindingService implements IKeyBindingService {
	
	private SortedMap actionMap = new TreeMap();
	private IActionService actionService;
	private IContextService contextService;
	
	KeyBindingService(IActionService actionService, IContextService contextService) {
		super();
		this.actionService = actionService;
		this.contextService = contextService;
	}

	public String[] getScopes() {
    	List contexts = contextService.getContexts();
    	return (String[]) contexts.toArray(new String[contexts.size()]);
    }

	public void setScopes(String[] scopes) {
		List contexts = Arrays.asList(scopes);
		contextService.setContexts(contexts);		 	
    }

	public void registerAction(IAction action) {
    	String command = action.getActionDefinitionId();

		if (command != null) {
			actionMap.put(command, action);		
			actionService.setActionMap(actionMap);
		}
    }
    
	public void unregisterAction(IAction action) {   		
    	String command = action.getActionDefinitionId();

		if (command != null) {
			actionMap.remove(command);
			actionService.setActionMap(actionMap);
		}
    }	
}
