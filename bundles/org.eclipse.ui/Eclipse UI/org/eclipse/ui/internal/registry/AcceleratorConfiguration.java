package org.eclipse.ui.internal.registry;

import java.util.HashMap;
import org.eclipse.ui.internal.WorkbenchPlugin;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * An accelerator configuration represents a collection of accelerator key to 
 * action id mappings. Accelerators belong to accelerator sets. Each
 * accelerator set is assigned to a certain accelerator configurations.
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
	public void initializeScopes() {
		AcceleratorRegistry registry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();
		AcceleratorScope scopes[] = registry.getScopes();
		for (int i = 0; i < scopes.length; i++) {
			scopes[i].initializeAccelerators(this);
		}
	}
}
