package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.HashMap;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.events.*;
import org.eclipse.ui.*;
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
	
	/* Maps action definition id to action. Includes the actions 
	 * registered in this service and its parent so that only one 
	 * lookup is needed.
	 */
	private HashMap allDefIdToAction = new HashMap();

	/* The active accelerator scope which is set by the editor */
	private String[] scopeIds = new String[] { IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID };
	
	/* The Workbench window key binding service which manages the 
	 * global actions and the action sets 
	 */
	private WWinKeyBindingService parent;
	
	/* A number increased by the parent whenever a new action 
	 * is registered so that this instance can update its mapping
	 * when it is out of sync.
	 */
	private long parentUpdateNumber;
	
	/**
	 * Create an instance of KeyBindingService and initializes 
	 * it with its parent.
	 */		
	public KeyBindingService(WWinKeyBindingService service,PartSite site) {
		parent = service;
		parentUpdateNumber = parent.getUpdateNumber() - 1;
		
		if(site instanceof EditorSite) {
			EditorActionBuilder.ExternalContributor contributor = (EditorActionBuilder.ExternalContributor)((EditorSite)site).getExtensionActionBarContributor();
			if(contributor != null)
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
    	if(parentUpdateNumber != parent.getUpdateNumber())
    		initializeMapping();
    	String defId = action.getActionDefinitionId();
    	Assert.isNotNull(defId,"All registered action must have a definition id"); //$NON-NLS-1$
		defIdToAction.put(defId,action);
		allDefIdToAction.put(defId,action);
    }
    
   	/*
	 * @see IKeyBindingService#unregisterAction(IAction)
	 */
	public void unregisterAction(IAction action) {   		
    	String defId = action.getActionDefinitionId();
    	Assert.isNotNull(defId,"All registered action must have a definition id"); //$NON-NLS-1$
		defIdToAction.remove(defId);
		allDefIdToAction.remove(defId);
    }
	
	/*
	 * Merge the actions from its parents with its registered actions
	 * in one HashMap
	 */
	private void initializeMapping() {
		parentUpdateNumber = parent.getUpdateNumber();
		allDefIdToAction = parent.getMapping();
		allDefIdToAction.putAll(defIdToAction);
	}
	
    /**
     * Returns the action mapped with the specified <code>definitionId</code>
     */
    public IAction getAction(String definitionId) {
    	//Chech if parent has changed. E.g. added action sets.
    	if (parentUpdateNumber != parent.getUpdateNumber())
    		initializeMapping();
    		
    	return (IAction) allDefIdToAction.get(definitionId);
    }




	/*
	 * @see IKeyBindingService#getActiveAcceleratorConfigurationId()
	 */
    public String getActiveAcceleratorConfigurationId() {
    	return ((Workbench) PlatformUI.getWorkbench()).getActiveAcceleratorConfiguration().getLabel().getId();
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
