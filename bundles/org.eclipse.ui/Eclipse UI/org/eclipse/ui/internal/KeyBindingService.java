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

/* (non-Javadoc)
 * @see IKeyBindingService
 */
public class KeyBindingService implements IKeyBindingService {

	private HashMap defIdToAction = new HashMap();
	private HashMap allDefIdToAction = new HashMap();
	private AcceleratorScope scope;
	private WWinKeyBindingService parent;
	private long parentUpdateNumber;
			
	public KeyBindingService(WWinKeyBindingService service) {
		parent = service;
		parentUpdateNumber = parent.getUpdateNumber() - 1;
	}
	public void initializeMapping() {
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
           
 	public boolean isParticipating() {
 		return defIdToAction.size() != 0;
 	}
}
