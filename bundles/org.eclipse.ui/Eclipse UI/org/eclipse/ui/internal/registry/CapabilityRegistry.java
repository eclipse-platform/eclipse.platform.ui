package org.eclipse.ui.internal.registry;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Platform;

/**
 * This class represents a registry of project capabilities and categories of 
 * capabilities.
 */
public class CapabilityRegistry {
	private ArrayList capabilities;
	private ArrayList categories;
	private Category miscCategory;
	
	public CapabilityRegistry() {
		capabilities = new ArrayList(30);
		categories = new ArrayList(15);		
	}
	
	/**
	 * Finds the category for the given identifier, or
	 * <code>null</code> if none.
	 */
	public ICategory findCategory(String id) {
		Iterator enum = categories.iterator();
		while (enum.hasNext()) {
			Category cat = (Category) enum.next();
			if (id.equals(cat.getRootPath())) {
				return cat;
			}
		}
		return null;
	}

	/**
	 * Returns the list of capabilities in the registry
	 */
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
	public boolean addCategory(ICategory category) {
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
	
	/**
	 * Adds each capability in the registry to a particular category.
	 * The category is defined in xml. If the capability's category is
	 * not found, then the capability is added to the "misc" category.
	 */
	public void mapCapabilitiesToCategories() {
		Iterator enum = capabilities.iterator();
		while (enum.hasNext()) {
			Capability cap = (Capability) enum.next();
			Category cat = null;
			String catPath = cap.getCategoryPath();
			if (catPath != null)
				cat = (Category)findCategory(catPath);
			if (cat != null) {
				cat.addElement(cap);
			} else {
				if (miscCategory == null) {
					miscCategory = new Category();
					categories.add(miscCategory);
				}
				miscCategory.addElement(cap);
			}
		}
	}
}
