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

import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.commands.IHandlerService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.commands.ActionHandler;

final class KeyBindingService implements IKeyBindingService {
	
	private SortedMap handlerMap = new TreeMap();
	private IContextService contextService;
	private IHandlerService handlerService;
		
	KeyBindingService(IContextService contextService, IHandlerService handlerService) {
		super();
		this.contextService = contextService;
		this.handlerService = handlerService;	
	}

	public String[] getScopes() {
    	return contextService.getActiveContextIds();
    }

	public void setScopes(String[] scopes) {
		contextService.setActiveContextIds(scopes);		 	
    }

	public void registerAction(IAction action) {
    	String command = action.getActionDefinitionId();

		if (command != null) {
			handlerMap.put(command, new ActionHandler(action));		
			handlerService.setHandlerMap(handlerMap);
		}
    }
    
	public void unregisterAction(IAction action) {   		
    	String command = action.getActionDefinitionId();

		if (command != null) {
			handlerMap.remove(command);
			handlerService.setHandlerMap(handlerMap);
		}
    }	
}
