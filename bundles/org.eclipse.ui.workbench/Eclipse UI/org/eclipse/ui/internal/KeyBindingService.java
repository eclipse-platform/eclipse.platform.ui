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

import java.util.HashMap;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.internal.misc.Assert;

/** 
 * Implementation of an IKeyBindingService.
 * Notes:
 * <ul>
 * <li>One instance is created for each editor site</li>
 * <li>Each editor has to register all its actions by calling registerAction()</li>
 * <li>The editor should call setActiveAcceleratorScopeId() once</li>
 * </ul>
 */
public class KeyBindingService implements IKeyBindingService {
	
	/* Maps action definition id to action. */
	private HashMap defIdToAction = new HashMap();
	
	/* The active accelerator scope which is set by the editor */
	private String[] scopeIds = new String[] { IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID };
	
	/* The Workbench window key binding service which manages the 
	 * global actions and the action sets 
	 */
	private WWinKeyBindingService parent;
	
	/**
	 * Create an instance of KeyBindingService and initializes 
	 * it with its parent.
	 */		
	public KeyBindingService(WWinKeyBindingService service, PartSite site) {
		parent = service;
		
		if (site instanceof EditorSite) {
			EditorActionBuilder.ExternalContributor contributor = (EditorActionBuilder.ExternalContributor) ((EditorSite) site).getExtensionActionBarContributor();
			
			if (contributor != null)
				registerExtendedActions(contributor.getExtendedActions());
		}
	}
	
	public void registerExtendedActions(ActionDescriptor[] actionDescriptors) {
		for (int i = 0; i < actionDescriptors.length; i++) {
			IAction action = actionDescriptors[i].getAction();
			
			if (action.getActionDefinitionId() != null)
				registerAction(action);
		}		
	}

	/*
	 * @see IKeyBindingService#getScopeIds()
	 */
	public String[] getScopeIds() {
    	return (String[]) scopeIds.clone();
    }

	/*
	 * @see IKeyBindingService#setScopeIds(String[] scopeIds)
	 */
	public void setScopeIds(String[] scopeIds)
		throws IllegalArgumentException {
		if (scopeIds == null || scopeIds.length < 1)
			throw new IllegalArgumentException();
			
    	this.scopeIds = (String[]) scopeIds.clone();
    	
    	for (int i = 0; i < scopeIds.length; i++)
			if (scopeIds[i] == null)
				throw new IllegalArgumentException();    	
    }

	/*
	 * @see IKeyBindingService#registerAction(IAction)
	 */
	public void registerAction(IAction action) {
    	String defId = action.getActionDefinitionId();
    	Assert.isNotNull(defId, "All registered action must have a definition id"); //$NON-NLS-1$
		defIdToAction.put(defId,action);
    }
    
   	/*
	 * @see IKeyBindingService#unregisterAction(IAction)
	 */
	public void unregisterAction(IAction action) {   		
    	String defId = action.getActionDefinitionId();
    	Assert.isNotNull(defId, "All registered action must have a definition id"); //$NON-NLS-1$
		defIdToAction.remove(defId);
    }
		
    /**
     * Returns the action mapped with the specified <code>definitionId</code>
     */
    public IAction getAction(String definitionId) {
    	IAction action = (IAction) defIdToAction.get(definitionId);
    	
    	if (action == null)
    		action = (IAction) parent.getMapping().get(definitionId);
    	    		
    	return action;
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
   		return getScopeIds()[0];
    }

	/*
	 * @see IKeyBindingService#setActiveAcceleratorScopeId(String)
	 */ 
    public void setActiveAcceleratorScopeId(String scopeId)
    	throws IllegalArgumentException {
   		setScopeIds(new String[] { scopeId });
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
