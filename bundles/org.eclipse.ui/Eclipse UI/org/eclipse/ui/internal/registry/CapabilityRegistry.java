package org.eclipse.ui.internal.registry;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;

/**
 * This class represents a registry of project capabilities and categories of 
 * capabilities.
 */
public class CapabilityRegistry {
	private List capabilities;
	private List categories;
	
	public CapabilityRegistry() {
		capabilities = new ArrayList();
		categories = new ArrayList();		
	}
	
	public List getCapabilities() {
		return capabilities;
	}
	
	/**
	 * Adds the given capability to the registry.
	 */
	public boolean addCapability(Capability capability) {
		return capabilities.add(capability);	
	}
	
	/**
	 * Adds the given capability category to the registry.
	 */
	public boolean addCategory(CapabilityCategory category) {
		return categories.add(category);
	}
	
	/**
	 * Loads capabilities and capability categories from the platform's plugin
	 * registry.
	 */
	public void load() {
		CapabilityRegistryReader reader = 
			new CapabilityRegistryReader();
		reader.read(Platform.getPluginRegistry(), this);
	}
}
