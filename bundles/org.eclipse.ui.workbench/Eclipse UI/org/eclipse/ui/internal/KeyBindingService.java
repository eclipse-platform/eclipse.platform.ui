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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.commands.old.ICommandHandlerService;
import org.eclipse.ui.contexts.IContextActivationService;
import org.eclipse.ui.internal.commands.old.ActionHandler;

final class KeyBindingService implements IKeyBindingService {
	
	private SortedMap handlerMap = new TreeMap();
	private IContextActivationService contextActivationService;
	private ICommandHandlerService handlerService;
	private List scopes = new ArrayList();
		
	KeyBindingService(IContextActivationService contextService, ICommandHandlerService handlerService) {
		super();
		this.contextActivationService = contextService;
		this.handlerService = handlerService;	
	}

	public String[] getScopes() {
    	return (String[]) scopes.toArray(new String[scopes.size()]);
    }

	public void setScopes(String[] scopes) {
		for (int i = 0; i < this.scopes.size(); i++)
			contextActivationService.deactivateContext((String) this.scopes.get(i));
			
		this.scopes.clear();		

		for (int i = 0; i < scopes.length; i++) {
			contextActivationService.activateContext(scopes[i]);
			this.scopes.add(scopes[i]);
		}
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
