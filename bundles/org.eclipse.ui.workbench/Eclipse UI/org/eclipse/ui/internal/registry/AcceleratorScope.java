package org.eclipse.ui.internal.registry;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;
import java.util.HashMap;

import org.eclipse.jface.action.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.internal.*;

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
}
