package org.eclipse.ui.internal.registry;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * This class captures the attributes of a category of project capabilities. Each
 * capability belongs to a category, and stores that category's id.
 */
public class CapabilityCategory {
	private String id;
	private String name;
	
	public CapabilityCategory(String id, String name) {
		this.id = id;
		this.name = name;	
	}
	
	public String getId() {
		return id;	
	}
	
	public String getName() {
		return name;	
	}
}
