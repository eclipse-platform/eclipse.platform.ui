package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 *
 */
public class AcceleratorConfiguration {
	private String id;
	private String name;
	private String description;
	
	public AcceleratorConfiguration(String id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
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
}
