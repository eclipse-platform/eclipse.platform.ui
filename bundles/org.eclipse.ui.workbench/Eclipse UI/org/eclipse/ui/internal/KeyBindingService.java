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
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.IKeyBindingService;

final class KeyBindingService implements IKeyBindingService {
	
	private SortedMap commandIdToActionMap = new TreeMap();
	private String[] scopes = new String[] { IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID };
	
	KeyBindingService() {
		super();
	}

	IAction getAction(String command) {
		return (IAction) commandIdToActionMap.get(command);
	}
	
	public String[] getScopes() {
    	return (String[]) scopes.clone();
    }

	public void setScopes(String[] scopes)
		throws IllegalArgumentException {
		if (scopes == null || scopes.length < 1)
			throw new IllegalArgumentException();
			
    	this.scopes = (String[]) scopes.clone();
    	
    	for (int i = 0; i < scopes.length; i++)
			if (scopes[i] == null)
				throw new IllegalArgumentException();    	
    }

	public void registerAction(IAction action) {
    	String command = action.getActionDefinitionId();

		if (command != null)
			commandIdToActionMap.put(command, action);
    }
    
	public void unregisterAction(IAction action) {   		
    	String command = action.getActionDefinitionId();

		if (command != null)
			commandIdToActionMap.remove(command);
    }

    public String getActiveAcceleratorConfigurationId() {
    	return org.eclipse.ui.internal.commands.Manager.getInstance().getKeyMachine().getConfiguration();
    }

	public String getActiveAcceleratorScopeId() {
   		return getScopes()[0];
    }

    public void setActiveAcceleratorScopeId(String scopeId)
    	throws IllegalArgumentException {
   		setScopes(new String[] { scopeId });
    }
    
	public boolean processKey(KeyEvent event) {
		return false;
    }

	public void enable(boolean enable) {
	}
}
