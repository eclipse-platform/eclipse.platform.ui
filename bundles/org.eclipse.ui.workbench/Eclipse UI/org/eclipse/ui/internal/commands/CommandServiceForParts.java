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
import org.eclipse.ui.IActionService;
import org.eclipse.ui.IActiveContextService;
import org.eclipse.ui.internal.ActionDescriptor;
import org.eclipse.ui.internal.EditorActionBuilder;
import org.eclipse.ui.internal.EditorSite;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.PartSite;

final class CommandServiceForParts implements IActionService, IActiveContextService {
	
	private SortedMap commandIdToActionMap = new TreeMap();
	private String[] activeContexts = new String[] { IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID };
	
	CommandServiceForParts(PartSite partSite) {
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
    
	/*
	 * @see IActiveContextService#getActiveContexts()
	 */
	public String[] getActiveContexts() {
		return (String[]) activeContexts.clone();	
	}	
	
	/*
	 * @see IActiveContextService#setActiveContexts(String[] activeContexts)
	 */
	public void setActiveContexts(String[] activeContexts)
		throws IllegalArgumentException {
		if (activeContexts == null || activeContexts.length < 1)
			throw new IllegalArgumentException();
			
		activeContexts = (String[]) activeContexts.clone();
    	
		for (int i = 0; i < activeContexts.length; i++)
			if (activeContexts[i] == null)
				throw new IllegalArgumentException(); 
				
		this.activeContexts = activeContexts;   	
	}    
}
