/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.examples.model.ui.mapping;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.examples.model.ModelObject;
import org.eclipse.team.examples.model.ModelObjectElementFile;
import org.eclipse.team.examples.model.mapping.ExampleModelProvider;
import org.eclipse.team.ui.mapping.SynchronizationCompareAdapter;
import org.eclipse.ui.IMemento;

/**
 * Compare adapter for use with our example model.
 */
public class CompareAdapter extends SynchronizationCompareAdapter {

	private static final String CTX_MODEL_MAPPINGS = "org.eclipse.team.examples.filesystem.modelMappings";
	
	private final ExampleModelProvider provider;

	public CompareAdapter(ExampleModelProvider provider) {
		this.provider = provider;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationCompareAdapter#getName(org.eclipse.core.resources.mapping.ResourceMapping)
	 */
	public String getName(ResourceMapping mapping) {
		Object o = mapping.getModelObject();
		if (o instanceof ModelObject) {
			return ((ModelObject) o).getName();
		}
		return super.getName(mapping);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationCompareAdapter#getPathString(org.eclipse.core.resources.mapping.ResourceMapping)
	 */
	public String getPathString(ResourceMapping mapping) {
		Object o = mapping.getModelObject();
		if (o instanceof ModelObject) {
			return ((ModelObject) o).getPath();
		}
		return super.getPathString(mapping);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationCompareAdapter#asCompareInput(org.eclipse.team.core.mapping.ISynchronizationContext, java.lang.Object)
	 */
	public ICompareInput asCompareInput(ISynchronizationContext context, Object o) {
		if (o instanceof ModelObjectElementFile) {
			ModelObjectElementFile moeFile = (ModelObjectElementFile) o;
			// Use a file compare input for the model element file
			return super.asCompareInput(context, moeFile.getResource());
		}
		return super.asCompareInput(context, o);
	}
	
	public ResourceMapping[] restore(IMemento memento) {
		List result = new ArrayList();
		IMemento[] children = memento.getChildren(CTX_MODEL_MAPPINGS);
		for (int i = 0; i < children.length; i++) {
			IMemento child = children[i];
			ResourceMapping mapping = restoreMapping(child);
			if (mapping != null)
				result.add(mapping);
		}
		return (ResourceMapping[]) result.toArray(new ResourceMapping[result.size()]);
	}

	private ResourceMapping restoreMapping(IMemento child) {
		// TODO Auto-generated method stub
		return null;
	}

	public void save(ResourceMapping[] mappings, IMemento memento) {
		
	}

	public ModelProvider getProvider() {
		return provider;
	}

}
