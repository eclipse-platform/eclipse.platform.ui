package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.Accelerator;
import org.eclipse.ui.internal.registry.AcceleratorRegistry;
import org.eclipse.ui.internal.registry.AcceleratorScope;
import org.eclipse.ui.internal.registry.AcceleratorSet;

/** 
 * Implementation of an IKeyBindingService.
 * Notes:
 * <ul>
 * <li>One instance is created for each editor site</li>
 * <li>Each editor has to register all its actions by calling registerAction()</li>
 * <li>The editor must call process key passing the key event</li>
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
	private AcceleratorScope scope;
	/* The Workbench window key binding service which manages the 
	 * global actions and the action sets 
	 */
	private WWinKeyBindingService parent;
	/* A number increased by the parent whenever a new action 
	 * is registered so that this instance can update its mapping
	 * when it is out of sync.
	 */
	private long parentUpdateNumber;
	/* */
	private boolean enabled = false;
	/**
	 * Create an instance of KeyBindingService and initializes 
	 * it with its parent.
	 */		
	public KeyBindingService(WWinKeyBindingService service) {
		parent = service;
		parentUpdateNumber = parent.getUpdateNumber() - 1;
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
	/*
	 * @see IKeyBindingService#getActiveAcceleratorConfigurationId()
	 */
    public String getActiveAcceleratorConfigurationId() {
    	return ((Workbench)PlatformUI.getWorkbench()).getActiveAcceleratorConfiguration().getId();
    }
	/*
	 * @see IKeyBindingService#processKey(Event)
	 */
	public boolean processKey(KeyEvent event) {
		if(scope == null)
			return false;
    	return scope.processKey(this,event);
    }
	/*
	 * @see IKeyBindingService#registerAction(IAction)
	 */
	public void registerAction(IAction action) {
    	if(parentUpdateNumber != parent.getUpdateNumber())
    		initializeMapping();
		defIdToAction.put(action.getActionDefinitionId(),action);
		allDefIdToAction.put(action.getActionDefinitionId(),action);
		if(scope != null)
			scope.registerAction(action.getAccelerator(),action);
    }
    /*
	 * @see IKeyBindingService#registerAction(IAction)
	 */
	public void enable(boolean enable) {
		enabled = enable;
		parent.update(this);
	}
    /**
     * Returns the action mapped with the specified <code>definitionId</code>
     */
    public IAction getAction(String definitionId) {
    	//Chech if parent has changed. E.g. added action sets.
    	if(parentUpdateNumber != parent.getUpdateNumber())
    		initializeMapping();
    	return (IAction)allDefIdToAction.get(definitionId);
    }
	/*
	 * @see IKeyBindingService#getActiveAcceleratorScopeId()
	 */
	public String getActiveAcceleratorScopeId() {
    	return scope.getId();
    }
	/*
	 * @see IKeyBindingService#setActiveAcceleratorScopeId(String)
	 */ 
    public void setActiveAcceleratorScopeId(String scopeId) {
    	AcceleratorRegistry registry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();
    	scope = registry.getScope(scopeId);
    }
    /*
     * Return if this service is participating on the key bindings
     */       
 	public boolean isParticipating() {
 		return (defIdToAction.size() != 0) && enabled;
 	}
}
