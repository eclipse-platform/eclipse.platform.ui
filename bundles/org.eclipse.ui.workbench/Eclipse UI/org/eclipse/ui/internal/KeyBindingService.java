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

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.commands.IActionService;
import org.eclipse.ui.contexts.IContextActivationService;
import org.eclipse.ui.internal.commands.ActionHandler;

final class KeyBindingService implements IKeyBindingService {
	
	private IActionService commandDelegateService;
	private IContextActivationService contextActivationService;
	private List scopes = new ArrayList();
		
	KeyBindingService(IActionService commandDelegateService, IContextActivationService contextActivationService) {
		super();
		this.contextActivationService = contextActivationService;
		this.commandDelegateService = commandDelegateService;	
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

		if (command != null)
			commandDelegateService.addAction(command, new ActionHandler(action));		
    }
    
	public void unregisterAction(IAction action) {   		
    	String command = action.getActionDefinitionId();

		if (command != null)
			commandDelegateService.removeAction(command);
    }	
}
