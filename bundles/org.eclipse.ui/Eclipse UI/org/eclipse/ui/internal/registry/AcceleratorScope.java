package org.eclipse.ui.internal.registry;

import java.util.HashMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
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
	private static final String DEFAULT_PARENT_SCOPE = "org.eclipse.ui.globalScope";
	private String id;
	private String name;
	private String description;
	private String parentScopeString;
	private AcceleratorScope parentScope;
	private AcceleratorConfiguration configuration;
	private AcceleratorMode defaultMode;
	private AcceleratorMode currentMode;
	
	public AcceleratorScope(String id, String name, String description, String parentScope) {
		this.id = id;
		this.name = name;
		this.description = description;
		if(parentScope==null) {
			this.parentScopeString = DEFAULT_PARENT_SCOPE;	
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
	/**
	 * Returns the parent scope of the current scope. For example, if the current
	 * scope is that of a page of a multi-page editor, the parent scope would be
	 * the scope of the editor.
	 */
	public AcceleratorScope getParentScope() {
		if(id.equals(DEFAULT_PARENT_SCOPE))
			return null;
		AcceleratorRegistry registry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();
		if(parentScope ==  null) {
			parentScope = registry.getScope(parentScopeString);
			if(parentScope ==  null) 
				parentScope = registry.getScope(DEFAULT_PARENT_SCOPE);
		}
		return parentScope;
	}
	
	public void initializeAccelerators(AcceleratorConfiguration configuration) {
		this.configuration = configuration;
		AcceleratorRegistry registry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();
		defaultMode = new AcceleratorMode();
		currentMode = defaultMode;
		initializeAccelerators(defaultMode,registry);
	}
	
	private void initializeAccelerators(AcceleratorMode mode,AcceleratorRegistry registry) {
		//ISSUE: Must resolve conflicts
		AcceleratorScope parent = getParentScope();
		if(parent != null)
			parent.initializeAccelerators(mode,registry);
		Accelerator accelerator[] = registry.getAccelerators(configuration.getId(),getId());
		for (int i = 0; i < accelerator.length; i++) {
			Integer accKeys[][] = accelerator[i].getAccelerators();
			for (int j = 0; j < accKeys.length; j++) {
				AcceleratorMode childMode = mode;
				for (int k = 0; k < accKeys[j].length - 1; k++) {
					AcceleratorAction a = mode.getAction(accKeys[j][k]);
					if((a == null) || (!a.isMode())){
						AcceleratorMode newMode = new AcceleratorMode();
						childMode.addAction(accKeys[j][k],newMode);
						childMode = newMode;
					} else {
						//if a is not instance of AcceleratorMode, there is a conflict.
						childMode = (AcceleratorMode)a;
					}
				}
				childMode.addAction(accKeys[j][accKeys[j].length - 1],new AcceleratorAction(accelerator[i].getId()));
			}
		}
	}
	
	private Integer convertEvent(Event event) {
		//ISSUE: Must fix the number 64.
    	if(event.stateMask == SWT.CONTROL) {
			char upper = Character.toUpperCase(event.character);
			if (0 <= upper && upper <= 64) 
				return new Integer(event.stateMask | upper + 64);
    	}
    	return new Integer(event.stateMask | event.character);
    }
    
	public boolean processKey(KeyBindingService service, Event e) {
		AcceleratorAction a = currentMode.getAction(convertEvent(e));
		if(a == null) {
			if(currentMode == defaultMode)
				return false;
			currentMode = defaultMode;
			return true;
		}
		a.run(service,e);
		return true;
	}
	
	class AcceleratorAction {
		String id;
		AcceleratorAction(String defId) {
			id = defId;
		}
		public boolean isMode() {
			return false;
		}
		public void run(KeyBindingService service,Event e) {
			IAction a = service.getAction(id);
			if((a != null) && (a.isEnabled()))
				a.runWithEvent(e);
			currentMode = defaultMode;
		}
	}
	
	class AcceleratorMode extends AcceleratorAction {
		private HashMap acceleratorToAction = new HashMap();
		
		AcceleratorMode() {
			super(null);
		}
		public boolean isMode() {
			return false;
		}	
		public void run(KeyBindingService service,Event e) {
			currentMode = this;
		}
		public AcceleratorAction getAction(Integer keyCode) {
			return (AcceleratorAction)acceleratorToAction.get(keyCode);	
		}
		public void addAction(Integer keyCode,AcceleratorAction acc) {
			acceleratorToAction.put(keyCode,acc);
		}
	}
}
