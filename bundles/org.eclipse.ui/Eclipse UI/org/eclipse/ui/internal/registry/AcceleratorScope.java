package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * 
 */
public class AcceleratorScope {
	private static final String DEFAULT_PARENT_SCOPE = "org.eclipse.ui.globalScope";
	private String id;
	private String name;
	private String description;
	private String parentScope;
	
	public AcceleratorScope(String id, String name, String description, String parentScope) {
		this.id = id;
		this.name = name;
		this.description = description;
		if(parentScope==null) {
			this.parentScope = DEFAULT_PARENT_SCOPE;	
		} else {
			this.parentScope = parentScope;
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
	public String getParentScope() {
		return parentScope;
	}
}
