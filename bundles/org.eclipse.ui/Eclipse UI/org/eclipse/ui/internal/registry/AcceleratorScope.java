package org.eclipse.ui.internal.registry;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.HashMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.KeyBindingService;
import org.eclipse.ui.internal.WorkbenchPlugin;

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
	/**
	 * Create an instance of AcceleratorScope and initializes 
	 * it with its id, name, description and parent scope.
	 */			
	public AcceleratorScope(String id, String name, String description, String parentScope) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.parentScopeString = parentScope;
		if(parentScope==null)
			this.parentScopeString = IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID;	
	}
	/**
	 * Return this scope's id
	 */
	public String getId() {
		return id;	
	}
	/**
	 * Return this scope's name
	 */
	public String getName() {
		return name;
	}
	/**
	 * Return this scope's description
	 */
	public String getDescription() {
		return description;	
	}
	/**
	 * Register and action with its accelerator. This action is
	 * used only if there is no other action using this accelerator
	 * defined in XML.
	 */
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
	/**
	 * Reset the current mode
	 */
	private static void resetMode() {
		currentMode = defaultMode;
	}
	/**
	 * Set the current mode and service
	 */
	private static void setCurrentMode(KeyBindingService service,AcceleratorMode mode) {
		if(service == currentService)
			currentMode = mode;
		else
			currentMode = mode;
	}
	/**
	 * Verify if the current mode was set with this service. Reset the mode
	 * if not.
	 */
	private static void verifyService(KeyBindingService service) {
		if(service == currentService)
			return;
		currentService = service;
		resetMode();
	}
	/**
	 * Initialize this scope with all accelerators defined in the default 
	 * configuration and then override with the ones defined in the active
	 * configuration. Fists initialize with the accelerators defined in the
	 * parent scope and then override with the ones defined in this scope.
	 */
	public void initializeAccelerators(AcceleratorConfiguration configuration) {
		this.configuration = configuration;
		AcceleratorRegistry registry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();
		defaultMode = new AcceleratorMode();
		resetMode();
		initializeAccelerators(IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID,defaultMode,registry);
		if(!IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID.equals(configuration.getId()))
			initializeAccelerators(configuration.getId(),defaultMode,registry);
	}
	/**
	 * Recurcive initialize this scope. First initialize its parents
	 * and then override with the accelerators defined in this scope.
	 */	
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
	/*
	 * Returns true if the event has only modifier otherwise return
	 * false
	 */
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
	/*
	 * Convert and event to an Integer.
	 */
	private Integer convertEvent(KeyEvent event) {
		//ISSUE: Must fix the number 64.
		char upper = Character.toUpperCase(event.character);
    	if(event.stateMask == SWT.CONTROL)
			if (0 <= upper && upper <= 64) 
				return new Integer(event.stateMask | upper + 64);
    	return new Integer(event.stateMask | upper);
    }
    /**
     * Process a key event. Find a action associated with the event
     * and run the action. Returns true if an action was found otherwise
     * returns false.
     */ 
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
	/**
	 * Adapter for an IAction with a definition in XML.
	 */
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
	/**
	 * Adapter for an IAction without a definition in XML.
	 */
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
	/**
	 * Adapter for a Mode.
	 * 
	 * A mode represents a particular state of use in which the user has already
 	 * pressed a sequence (possibly of length one) of accelerator keys which matches
	 * the beginning of a valid sequence of accelerator keys which will cause an
	 * action to occur.
	 * <p>
	 * When a mode is active, the next accelerator key to be pressed is processed
	 * in the context of the active mode. If it matches the next key in any valid
	 * sequence, a new mode will become the active mode. If the next key presses
	 * is invalid, the active mode becomes inactive and the user is returned to a 
	 * modeless (defaultMode) state.
	 * </p>
	 */
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
