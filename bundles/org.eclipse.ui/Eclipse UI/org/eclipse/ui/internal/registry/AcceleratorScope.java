package org.eclipse.ui.internal.registry;

import java.util.HashMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.KeyBindingService;
import org.eclipse.ui.internal.WorkbenchPlugin;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * An accelerator scope is a range in which a given accelerator (a mapping
 * between an accelerator key and an action id) is available.
 * A scope may represent a view, editor, a page of a multi-page editor, etc.
 * An accelerator is available when the part represented by its scope is active.
 */
public class AcceleratorScope {
	private String id;
	private String name;
	private String description;
	private String parentScopeString;
	private AcceleratorScope parentScope;
	private AcceleratorConfiguration configuration;
	private HashMap defaultAcceleratorToAction = new HashMap();
	
	private static AcceleratorMode currentMode;
	private static AcceleratorMode defaultMode;
	private static KeyBindingService currentService;
	
	public AcceleratorScope(String id, String name, String description, String parentScope) {
		this.id = id;
		this.name = name;
		this.description = description;
		if(parentScope==null) {
			this.parentScopeString = IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID;	
		} else {
			this.parentScopeString = parentScope;
		}
	}
	
	public String getId() {
		return id;	
	}
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;	
	}
	public void registerAction(int accelerator,IAction action) {
		defaultAcceleratorToAction.put(new Integer(accelerator),new DefaultAction(action));
	}
	/**
	 * Returns the parent scope of the current scope. For example, if the current
	 * scope is that of a page of a multi-page editor, the parent scope would be
	 * the scope of the editor.
	 */
	public AcceleratorScope getParentScope() {
		if(id.equals(IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID))
			return null;
		AcceleratorRegistry registry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();
		if(parentScope ==  null) {
			parentScope = registry.getScope(parentScopeString);
			if(parentScope ==  null) 
				parentScope = registry.getScope(IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID);
		}
		return parentScope;
	}
	
	public void initializeAccelerators(AcceleratorConfiguration configuration) {
		this.configuration = configuration;
		AcceleratorRegistry registry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();
		defaultMode = new AcceleratorMode();
		resetMode();
		initializeAccelerators(IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID,defaultMode,registry);
		if(!IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID.equals(configuration.getId()))
			initializeAccelerators(configuration.getId(),defaultMode,registry);
	}
	
	private static void resetMode() {
		currentMode = defaultMode;
	}
	private static void setCurrentMode(KeyBindingService service,AcceleratorMode mode) {
		if(service == currentService)
			currentMode = mode;
		else
			currentMode = mode;
	}
	private static void verifyService(KeyBindingService service) {
		if(service == currentService)
			return;
		currentService = service;
		resetMode();
	}
	
	private void initializeAccelerators(String configId,AcceleratorMode mode,AcceleratorRegistry registry) {
		//ISSUE: Must resolve conflicts
		AcceleratorScope parent = getParentScope();
		if(parent != null)
			parent.initializeAccelerators(configId,mode,registry);
		Accelerator accelerators[] = registry.getAccelerators(configId,getId());
		for (int i = 0; i < accelerators.length; i++) {
			Integer accKeys[][] = accelerators[i].getAccelerators();
			for (int j = 0; j < accKeys.length; j++) {
				AcceleratorMode childMode = mode;
				for (int k = 0; k < accKeys[j].length - 1; k++) {
					AcceleratorAction a = mode.getAction(accKeys[j][k]);
					if ((a == null) || (!a.isMode())) {
						AcceleratorMode newMode = new AcceleratorMode();
						childMode.addAction(accKeys[j][k], newMode);
						childMode = newMode;
					} else {
						//if a is not instance of AcceleratorMode, there is a conflict.
						childMode = (AcceleratorMode) a;
					}
				}
				childMode.addAction(accKeys[j][accKeys[j].length - 1], new AcceleratorAction(accelerators[i].getId()));
			}
		}
	}
	
	private boolean isModifierOnly(KeyEvent event) {
    	if (event.character != 0)
    		return false;
    		
    	switch (event.keyCode) {
			case SWT.CONTROL:
			case SWT.ALT:
			case SWT.SHIFT:
				return true;
    	}
    	return false;
    }

	private Integer convertEvent(KeyEvent event) {
		//ISSUE: Must fix the number 64.
    	if(event.stateMask == SWT.CONTROL) {
			char upper = Character.toUpperCase(event.character);
			if (0 <= upper && upper <= 64) 
				return new Integer(event.stateMask | upper + 64);
    	}
    	return new Integer(event.stateMask | event.character);
    }
    
	public boolean processKey(KeyBindingService service, KeyEvent e) {
		if(isModifierOnly(e))
			return false;
		verifyService(service);
		Integer event = convertEvent(e);
		AcceleratorAction a = currentMode.getAction(event);
		if(a == null)
			a = (AcceleratorAction)defaultAcceleratorToAction.get(event);
		if(a == null) {
			if(currentMode == defaultMode)
				return false;
			resetMode();
			return true;
		}
		a.run(service,e);
		return true;
	}
	
	public static class AcceleratorAction {
		String id;
		AcceleratorAction(String defId) {
			id = defId;
		}
		public boolean isMode() {
			return false;
		}
		public void run(KeyBindingService service,KeyEvent e) {
			IAction a = service.getAction(id);
			if((a != null) && (a.isEnabled()))
				a.run();
				//a.runWithEvent(e);
			resetMode();
		}
	}
	
	public static class DefaultAction extends AcceleratorAction {
		IAction action;
		DefaultAction(IAction action) {
			super(null);
			this.action = action;
		}
		public boolean isMode() {
			return false;
		}
		public void run(KeyBindingService service,KeyEvent e) {
			action.run();
		}
	}
	public static class AcceleratorMode extends AcceleratorAction {
		private HashMap acceleratorToAction = new HashMap();
		
		AcceleratorMode() {
			super(null);
		}
		public boolean isMode() {
			return true;
		}	
		public void run(KeyBindingService service,KeyEvent e) {
			setCurrentMode(service,this);
		}
		public AcceleratorAction getAction(Integer keyCode) {
			return (AcceleratorAction)acceleratorToAction.get(keyCode);	
		}
		public void addAction(Integer keyCode,AcceleratorAction acc) {
			acceleratorToAction.put(keyCode,acc);
		}
	}
}
