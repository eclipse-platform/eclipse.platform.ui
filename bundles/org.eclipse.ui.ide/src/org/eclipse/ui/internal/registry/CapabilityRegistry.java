/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;
 
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
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
	 * Finds the capability for the given identifier, or
	 * <code>null</code> if none.
	 */
	public Capability findCapability(String id) {
		Iterator enum = capabilities.iterator();
		while (enum.hasNext()) {
			Capability cap = (Capability) enum.next();
			if (id.equals(cap.getId())) {
				return cap;
			}
		}
		return null;
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
	
	/**
	 * Finds the category for each specified identifier.
	 * Any <code>null</code> entries in the resulting array
	 * are for identifiers to which no category exist.
	 * 
	 * @return an array of <code>ICategory</code>
	 */
	public ICategory[] findCategories(String[] ids) {
		int count = categories.size();
		ICategory[] results = new Category[ids.length];
		
		for (int i = 0; i < ids.length; i++) {
			String id = ids[i];
			for (int j = 0; j < count; j++) {
				Category cat = (Category) categories.get(j);
				if (cat.getId().equals(id)) {
					results[i] = cat;
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
	 * Returns the list of categories in the registry
	 * which contain at least one capability. Does not
	 * include the misc and unknown categories.
	 */
	public ArrayList getUsedCategories() {
		ArrayList results = new ArrayList(categories.size());
		Iterator enum = categories.iterator();
		while (enum.hasNext()) {
			ICategory cat = (ICategory) enum.next();
			if (cat.hasElements())
				results.add(cat);
		}
		return results;
	}

	/**
	 * Returns the capability for the nature id
	 */
	public Capability getCapabilityForNature(String natureId) {
		return (Capability)natureToCapability.get(natureId);
	}
	
	/**
	 * Returns the list of capabilities in the registry
	 */
	public ArrayList getCapabilities() {
		return capabilities;
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchAdapter.
	 */
	public Object[] getChildren(Object o) {
		return capabilities.toArray();
	}
	
	/**
	 * Returns the membership set ids that the specified
	 * capability belongs to.
	 */
	public String[] getMembershipSetIds(Capability capability) {
		IProjectNatureDescriptor desc = capability.getNatureDescriptor();
		if (desc == null)
			return EMPTY_ID_LIST;
			
		return desc.getNatureSetIds();
	}

	/**
	 * Returns the miscellaneous category, or <code>null</code>
	 * if none.
	 */
	public ICategory getMiscCategory() {
		return miscCategory;
	}
	
	/**
	 * Returns the capability ids that are prerequisites
	 * of the specified capability.
	 */
	public String[] getPrerequisiteIds(Capability capability) {
		IProjectNatureDescriptor desc = capability.getNatureDescriptor();
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
				if (cap == null) {
					cap = new Capability(natureIds[i]);
					mapCapability(cap);
				}
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
	 * Returns the capabilities assigned to the specified project
	 * that are consideed disabled by core.
	 */
	public Capability[] getProjectDisabledCapabilities(IProject project) {
		try {
			String[] natureIds = project.getDescription().getNatureIds();
			ArrayList results = new ArrayList(natureIds.length);
			for (int i = 0; i < natureIds.length; i++) {
				if (!project.isNatureEnabled(natureIds[i])) {
					Capability cap = (Capability)natureToCapability.get(natureIds[i]);
					if (cap == null) {
						cap = new Capability(natureIds[i]);
						mapCapability(cap);
					}
					results.add(cap);
				}
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
	 * Returns whether the registry contains any capabilities.
	 */
	public boolean hasCapabilities() {
		return !capabilities.isEmpty();
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
		mapCapabilities();
	}
	
	/**
	 * Maps each capability in the registry to a particular category.
	 * The category is defined in xml. If the capability's category is
	 * not found, then the capability is added to the "misc" category.
	 * <p>
	 * Maps each capability in the registry to a particular nature
	 * id.
	 */
	/* package */ void mapCapabilities() {
		natureToCapability = new HashMap();
		
		Iterator enum = capabilities.iterator();
		while (enum.hasNext())
			mapCapability((Capability) enum.next());
	}

	private void mapCapability(Capability cap) {
		// Map to category
		if (!cap.isValid()) {
			if (miscCategory == null)
				miscCategory = new Category();
			miscCategory.addElement(cap);
		} else {
			Category cat = null;
			String catPath = cap.getCategoryPath();
			if (catPath != null)
				cat = (Category)findCategory(catPath);
			if (cat != null) {
				cat.addElement(cap);
			} else {
				if (miscCategory == null)
					miscCategory = new Category();
				miscCategory.addElement(cap);
			}
		}
		
		// Map to nature id
		natureToCapability.put(cap.getNatureId(), cap);
	}

	/**
	 * Removes from the capability collection all capabilities
	 * whose UI is handle by another capability in the collection.
	 * The provided collection must be in proper prerequisite order.
	 * 
	 * @param capabilities the capabilities to be pruned
	 * @return a collection of capabilities pruned
	 */
	public Capability[] pruneCapabilities(Capability[] capabilities) {
		ArrayList ids = new ArrayList(capabilities.length);
		for (int i = 0; i < capabilities.length; i++)
			ids.add(capabilities[i].getId());
		
		for (int i = 0; i < capabilities.length; i++) {
			ArrayList handleIds = capabilities[i].getHandleUIs();
			if (handleIds != null)
				ids.removeAll(handleIds);
		}

		String[] results = new String[ids.size()];
		ids.toArray(results);
		return findCapabilities(results);
	}
	
	/**
	 * Checks that the collection is valid. If so, the collection is
	 * ordered based on prerequisite.
	 * 
	 * @param capabilities the capabilities to be checked and ordered
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given set of natures is valid, otherwise a status 
	 *		object indicating what is wrong with the set. Also, the
	 * 		collection of capabilities specified is ordered based on
	 * 		prerequisite.
	 */
	public IStatus validateCapabilities(Capability[] capabilities) {
		String natures[] = new String[capabilities.length];
		for (int i = 0; i < capabilities.length; i++)
			natures[i] = capabilities[i].getNatureId();
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IStatus status = workspace.validateNatureSet(natures);
		if (status.isOK()) {
			natures = workspace.sortNatureSet(natures);
			for (int i = 0; i < natures.length; i++)
				capabilities[i] = (Capability)natureToCapability.get(natures[i]);
		}

		return status;
	}
}
