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
package org.eclipse.team.internal.ui.mapping;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.mapping.IResourceMappingPersistenceAdapter;
import org.eclipse.ui.IMemento;

public class ResourceModelPersistenceAdapter implements
		IResourceMappingPersistenceAdapter {

	private static final String RESOURCES = "resources"; //$NON-NLS-1$
	private static final String RESOURCE_PATH = "resourcePath"; //$NON-NLS-1$
	private static final String RESOURCE_TYPE = "resourceType"; //$NON-NLS-1$
	
	private final ModelProvider provider;

	public ResourceModelPersistenceAdapter(ModelProvider provider) {
		this.provider = provider;
	}

	public void save(ResourceMapping[] mappings, IMemento memento) {
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			Object object = mapping.getModelObject();
			if (object instanceof IResource) {
				IResource resource = (IResource) object;
				IMemento child = memento.createChild(RESOURCES);
				child.putInteger(RESOURCE_TYPE, resource.getType());
				child.putString(RESOURCE_PATH, resource.getFullPath().toString());
			} else {
				// TODO: Should we try and persist non-resource objects like working sets
			}
		}
	}

	public ResourceMapping[] restore(IMemento memento) {
		IMemento[] children = memento.getChildren(RESOURCES);
		List result = new ArrayList();
		for (int i = 0; i < children.length; i++) {
			IMemento child = children[i];
			Integer typeInt = child.getInteger(RESOURCE_TYPE);
			if (typeInt == null) 
				break;
			int type = typeInt.intValue();
			String pathString = child.getString(RESOURCE_PATH);
			if (pathString == null)
				break;
			IPath path = new Path(pathString);
			IResource resource;
			switch (type) {
			case IResource.ROOT:
				resource = ResourcesPlugin.getWorkspace().getRoot();
				break;
			case IResource.PROJECT:
				resource = ResourcesPlugin.getWorkspace().getRoot().getProject(path.lastSegment());
				break;
			case IResource.FILE:
				resource = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
				break;
			case IResource.FOLDER:
				resource = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
				break;
			default:
				resource = null;
				break;
			}
			if (resource != null) {
				ResourceMapping mapping = Utils.getResourceMapping(resource);
				if (mapping != null) {
					result.add(mapping);
				}
			}
		}
		return (ResourceMapping[]) result.toArray(new ResourceMapping[result.size()]);
	}

}
