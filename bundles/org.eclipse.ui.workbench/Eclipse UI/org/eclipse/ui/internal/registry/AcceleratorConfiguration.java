package org.eclipse.ui.internal.registry;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
/**
 * An accelerator configuration represents a collection of accelerator key to 
 * action id mappings. Accelerators belong to accelerator sets. Each
 * accelerator set is assigned to a certain accelerator configurations.
 */
public class AcceleratorConfiguration {
	private String id;
	private String name;
	private String description;
	private String parentConfigurationString;
	private AcceleratorConfiguration parentConfiguration;
	/**
	 * Create an instance of AcceleratorConfiguration and initializes 
	 * it with its id, name and description.
	 */	
	public AcceleratorConfiguration(String id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}
	/**
	 * Return this configuration's id.
	 */
	public String getId() {
		return id;	
	}
	/**
	 * Return this configuration's name.
	 */
	public String getName() {
		return name;
	}		

	public AcceleratorConfiguration getParentConfiguration() {
		if(id.equals(IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID))
			return null;
		AcceleratorRegistry registry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();
		if(parentConfiguration ==  null) {
			parentConfiguration = registry.getConfiguration(parentConfigurationString);
			if(parentConfiguration ==  null) 
				parentConfiguration = registry.getConfiguration(IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID);
		}
		return parentConfiguration;
	}
	
	/**
	 * Return this configuration's description.
	 */
	public String getDescription() {
		return description;	
	}
}
