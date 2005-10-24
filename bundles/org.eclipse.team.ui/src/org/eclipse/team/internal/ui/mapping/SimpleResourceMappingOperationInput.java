/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.ui.mapping.IResourceMappingOperationInput;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;

/**
 * A simple implementation of an operation input that
 * does not transform the input.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public class SimpleResourceMappingOperationInput implements
		IResourceMappingOperationInput {

	private ResourceMapping[] mappings;
	private ResourceMappingContext context;
	private Map mappingToTraversalsMap = new HashMap();
	
	public SimpleResourceMappingOperationInput(ResourceMapping[] mappings, ResourceMappingContext context) {
		this.mappings = mappings;
		this.context = context;
	}

	public ResourceMapping[] getSeedMappings() {
		return mappings;
	}

	public void buildInput(IProgressMonitor monitor) throws CoreException {
		buildInputMappingToTraversalsMap(monitor);
	}

	public ResourceMapping[] getInputMappings() {
		return mappings;
	}

	public ResourceTraversal[] getInputTraversals() {
		Collection values = mappingToTraversalsMap.values();
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

	public ResourceTraversal[] getTraversals(ResourceMapping mapping) {
		return (ResourceTraversal[])mappingToTraversalsMap.get(mapping);
	}

	public boolean hasAdditionalMappings() {
		return false;
	}

	private void buildInputMappingToTraversalsMap(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(null,	mappings.length * 100);
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			ResourceTraversal[] traversals = mapping.getTraversals(context, Policy.subMonitorFor(monitor, 100));
			mappingToTraversalsMap.put(mapping, traversals);
		}
		monitor.done();
	}
	
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

	public ModelProvider[] getModelProviders() {
		Set result = new HashSet();
		ResourceMapping[] mappings = getInputMappings();
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			result.add(mapping.getModelProvider());
		}
		return (ModelProvider[]) result.toArray(new ModelProvider[result.size()]);
	}

	public ResourceMappingContext getContext() {
		return context;
	}

	public ResourceMapping[] getResourceMappings(String id) {
		Set result = new HashSet();
		ResourceMapping[] mappings = getInputMappings();
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			if (mapping.getModelProviderId().equals(id)) {
				result.add(mapping);
			}
		}
		return (ResourceMapping[]) result.toArray(new ResourceMapping[result.size()]);

	}
	
	public ISynchronizeScope asSynchronizationScope() {
		// TODO Temporary implementation
		return new ResourceMappingScope("TODO: Need appropriate labels", getInputMappings(), getInputTraversals());
	}
}
