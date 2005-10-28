/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.team.ui.mapping.IResourceMappingOperationScope;

/**
 * Concrete implementation of the {@link IResourceMappingOperationScope}
 * interface for use by clients.
 * <p>
 * This class is not intended to be subclasses by clients
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see org.eclipse.core.resources.mapping.ResourceMapping
 * 
 * @since 3.2
 */
public class ResourceMappingOperationScope implements
		IResourceMappingOperationScope {
	
	private ResourceMapping[] inputMappings;
	private final Map mappingsToTraversals = new HashMap();
	private boolean hasAdditionalMappings;

	public static ResourceTraversal[] combineTraversals(ResourceTraversal[] allTraversals) {
		Set zero = new HashSet();
		Set shallow = new HashSet();
		Set deep = new HashSet();
		for (int i = 0; i < allTraversals.length; i++) {
			ResourceTraversal traversal = allTraversals[i];
			switch (traversal.getDepth()) {
			case IResource.DEPTH_ZERO:
				zero.addAll(Arrays.asList(traversal.getResources()));
				break;
			case IResource.DEPTH_ONE:
				shallow.addAll(Arrays.asList(traversal.getResources()));
				break;
			case IResource.DEPTH_INFINITE:
				deep.addAll(Arrays.asList(traversal.getResources()));
				break;
			}
		}
		List result = new ArrayList();
		if (!zero.isEmpty()) {
			result.add(new ResourceTraversal((IResource[]) zero.toArray(new IResource[zero.size()]), IResource.DEPTH_ZERO, IResource.NONE));
		}
		if (!shallow.isEmpty()) {
			result.add(new ResourceTraversal((IResource[]) shallow.toArray(new IResource[shallow.size()]), IResource.DEPTH_ONE, IResource.NONE));
		}
		if (!deep.isEmpty()) {
			result.add(new ResourceTraversal((IResource[]) deep.toArray(new IResource[deep.size()]), IResource.DEPTH_INFINITE, IResource.NONE));
		}
		return (ResourceTraversal[]) result.toArray(new ResourceTraversal[result.size()]);
	}
	
	/**
	 * @param selectedMappings
	 */
	public ResourceMappingOperationScope(ResourceMapping[] selectedMappings) {
		inputMappings = selectedMappings;
	}
	
	/**
	 * Add the mapping and its traversals to the scope. This method should
	 * only be invoked during the scope building process.
	 * @param mapping the mapping being added to the scope
	 * @param traversals the traversals for that mapping
	 */
	public void addMapping(ResourceMapping mapping, ResourceTraversal[] traversals) {
		mappingsToTraversals.put(mapping, traversals);
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IResourceMappingOperationScope#getInputMappings()
	 */
	public ResourceMapping[] getInputMappings() {
		return inputMappings;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IResourceMappingOperationScope#getMappings()
	 */
	public ResourceMapping[] getMappings() {
		return (ResourceMapping[]) mappingsToTraversals.keySet().toArray(new ResourceMapping[mappingsToTraversals.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IResourceMappingOperationScope#getTraversals()
	 */
	public ResourceTraversal[] getTraversals() {
		Collection values = mappingsToTraversals.values();
		List result = new ArrayList();
		for (Iterator iter = values.iterator(); iter.hasNext();) {
			ResourceTraversal[] traversals = (ResourceTraversal[]) iter.next();
			for (int i = 0; i < traversals.length; i++) {
				ResourceTraversal traversal = traversals[i];
				result.add(traversal);
			}
		}
		return combineTraversals((ResourceTraversal[]) result.toArray(new ResourceTraversal[result.size()]));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IResourceMappingOperationScope#getTraversals(org.eclipse.core.resources.mapping.ResourceMapping)
	 */
	public ResourceTraversal[] getTraversals(ResourceMapping mapping) {
		return (ResourceTraversal[])mappingsToTraversals.get(mapping);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IResourceMappingOperationScope#hasAdditionalMappings()
	 */
	public boolean hasAdditionalMappings() {
		return hasAdditionalMappings;
	}
	
	/**
	 * Set whether the scope has additional mappings to the input mappings.
	 * This method should only be invoked during the scope building process.
	 * @param hasAdditionalMappings whether the scope has additional mappings
	 */
	public void setHasAdditionalMappings(boolean hasAdditionalMappings) {
		this.hasAdditionalMappings = hasAdditionalMappings;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IResourceMappingOperationScope#getModelProviders()
	 */
	public ModelProvider[] getModelProviders() {
		Set result = new HashSet();
		ResourceMapping[] mappings = getMappings();
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			result.add(mapping.getModelProvider());
		}
		return (ModelProvider[]) result.toArray(new ModelProvider[result.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IResourceMappingOperationScope#getMappings(java.lang.String)
	 */
	public ResourceMapping[] getMappings(String id) {
		Set result = new HashSet();
		ResourceMapping[] mappings = getMappings();
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			if (mapping.getModelProviderId().equals(id)) {
				result.add(mapping);
			}
		}
		return (ResourceMapping[]) result.toArray(new ResourceMapping[result.size()]);

	}

}
