package org.eclipse.ui.internal.registry;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNatureDescriptor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.model.WorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This class represents a registry of project capabilities and categories of 
 * capabilities.
 */
public class CapabilityRegistry extends WorkbenchAdapter implements IAdaptable {
	private static final String[] EMPTY_ID_LIST = new String[0];
	private static final Capability[] EMPTY_CAP_LIST = new Capability[0];
	
	private HashMap natureToCapability;
	private ArrayList capabilities;
	private ArrayList categories;
	private Category miscCategory;
	
	/**
	 * Creates a new instance of <code>CapabilityRegistry</code>
	 */
	public CapabilityRegistry() {
		capabilities = new ArrayList(30);
		categories = new ArrayList(15);		
	}
	
	/**
	 * Adds the given capability to the registry. Called by
	 * the CapabilityRegistryReader.
	 */
	/* package */ boolean addCapability(Capability capability) {
		return capabilities.add(capability);	
	}
	
	/**
	 * Adds the given capability category to the registry. Called
	 * by the CapabilityRegistryReader.
	 */
	/* package */ boolean addCategory(ICategory category) {
		return categories.add(category);
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
	 * Finds the capability for each specified identifier.
	 * Any <code>null</code> entries in the resulting array
	 * are for identifiers to which no capability exist.
	 */
	public Capability[] findCapabilities(String[] ids) {
		int count = capabilities.size();
		Capability[] results = new Capability[ids.length];
		
		for (int i = 0; i < ids.length; i++) {
			String id = ids[i];
			for (int j = 0; j < count; j++) {
				Capability cap = (Capability) capabilities.get(j);
				if (cap.getId().equals(id)) {
					results[i] = cap;
					break;
				}
			}
		}
		
		return results;
	}
	
	/* (non-Javadoc)
	 * Method declared on IAdaptable.
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) 
			return this;
		else
			return null;
	}
	
	/**
	 * Returns the list of capabilities in the registry
	 */
	public List getCapabilities() {
		return capabilities;
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchAdapter.
	 */
	public Object[] getChildren(Object o) {
		return capabilities.toArray();
	}
	
	/**
	 * Returns the capability ids that are prerequisites
	 * of the specified capability.
	 */
	public String[] getPrerequisiteIds(Capability capability) {
		IProjectNatureDescriptor desc;
		desc = ResourcesPlugin.getWorkspace().getNatureDescriptor(capability.getNatureId());
		if (desc == null)
			return EMPTY_ID_LIST;
			
		String[] natureIds = desc.getRequiredNatureIds();
		if (natureIds.length == 0)
			return EMPTY_ID_LIST;
			
		ArrayList results = new ArrayList(natureIds.length);
		for (int i = 0; i < natureIds.length; i++) {
			Capability cap = (Capability)natureToCapability.get(natureIds[i]);
			if (cap != null)
				results.add(cap.getId());
		}
		
		if (results.size() == 0) {
			return EMPTY_ID_LIST;
		} else {
			String[] ids = new String[results.size()];
			results.toArray(ids);
			return ids;
		}
	}

	/**
	 * Returns the capabilities assigned to the specified project
	 */
	public Capability[] getProjectCapabilities(IProject project) {
		try {
			String[] natureIds = project.getDescription().getNatureIds();
			ArrayList results = new ArrayList(natureIds.length);
			for (int i = 0; i < natureIds.length; i++) {
				Capability cap = (Capability)natureToCapability.get(natureIds[i]);
				if (cap != null)
					results.add(cap);
			}
			
			if (results.size() == 0) {
				return EMPTY_CAP_LIST;
			} else {
				Capability[] caps = new Capability[results.size()];
				results.toArray(caps);
				return caps;
			}
		}
		catch (CoreException e) {
			return EMPTY_CAP_LIST;
		}
	}
	
	/**
	 * Returns whether the specified capability has any prerequisites.
	 */
	public boolean hasPrerequisites(Capability capability) {
		return getPrerequisiteIds(capability).length > 0;
	}

	/**
	 * Loads capabilities and capability categories from the platform's plugin
	 * registry.
	 */
	public void load() {
		CapabilityRegistryReader reader = new CapabilityRegistryReader();
		reader.read(Platform.getPluginRegistry(), this);
		mapCapabilitiesToCategories();
	}
	
	/**
	 * Adds each capability in the registry to a particular category.
	 * The category is defined in xml. If the capability's category is
	 * not found, then the capability is added to the "misc" category.
	 */
	/* package */ void mapCapabilitiesToCategories() {
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
