/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.IKeyBindingService;

final class KeyBindingService implements IKeyBindingService {
	
	private SortedMap commandIdToActionMap = new TreeMap();
	private String[] scopes = new String[] { IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID };
	
	KeyBindingService(PartSite partSite) {
		if (partSite instanceof EditorSite) {
			EditorActionBuilder.ExternalContributor contributor = (EditorActionBuilder.ExternalContributor) ((EditorSite) partSite).getExtensionActionBarContributor();
			
			if (contributor != null)
				registerExtendedActions(contributor.getExtendedActions());
		}
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
	 * @see IKeyBindingService#getScopes()
	 */
	public String[] getScopes() {
    	return (String[]) scopes.clone();
    }

	/*
	 * @see IKeyBindingService#setScopes(String[] scopes)
	 */
	public void setScopes(String[] scopes)
		throws IllegalArgumentException {
		if (scopes == null || scopes.length < 1)
			throw new IllegalArgumentException();
			
    	this.scopes = (String[]) scopes.clone();
    	
    	for (int i = 0; i < scopes.length; i++)
			if (scopes[i] == null)
				throw new IllegalArgumentException();    	
    }

	/*
	 * @see IKeyBindingService#registerAction(IAction)
	 */
	public void registerAction(IAction action) {
    	String command = action.getActionDefinitionId();

		if (command != null)
			commandIdToActionMap.put(command, action);
    }
    
   	/*
	 * @see IKeyBindingService#unregisterAction(IAction)
	 */
	public void unregisterAction(IAction action) {   		
    	String command = action.getActionDefinitionId();

		if (command != null)
			commandIdToActionMap.remove(command);
    }

	/*
	 * @see IKeyBindingService#getActiveAcceleratorConfigurationId()
	 */
    public String getActiveAcceleratorConfigurationId() {
    	return org.eclipse.ui.internal.commands.Manager.getInstance().getKeyMachine().getConfiguration();
    }

	/*
	 * @see IKeyBindingService#getActiveAcceleratorScopeId()
	 */
	public String getActiveAcceleratorScopeId() {
   		return getScopes()[0];
    }

	/*
	 * @see IKeyBindingService#setActiveAcceleratorScopeId(String)
	 */ 
    public void setActiveAcceleratorScopeId(String scopeId)
    	throws IllegalArgumentException {
   		setScopes(new String[] { scopeId });
    }
    
   	/*
	 * @see IKeyBindingService#processKey(Event)
	 */
	public boolean processKey(KeyEvent event) {
		return false;
    }

    /*
	 * @see IKeyBindingService#registerAction(IAction)
	 */
	public void enable(boolean enable) {
	}
}
