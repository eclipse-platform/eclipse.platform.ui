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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.mapping.*;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * This content provider displays the mappings as a flat list 
 * of elements.
 * <p>
 * There are three use-cases we need to consider. The first is when there
 * are resource level mappings to be displayed. The second is when there
 * are mappings from a model provider that does not have a content provider
 * registered. The third is for the case where a resource mapping does not
 * have a model provider registered (this may be considered an error case).
 *
 */
public class ResourceMappingContentProvider implements IResourceMappingContentProvider {

	private ISynchronizationContext context;
	private final ModelProvider provider;
	private IResourceMappingScope input;
	
	public class ResourceAndDepth extends WorkbenchAdapter implements IAdaptable {
		Object parent;
		IResource resource;
		int depth;
		
		public ResourceAndDepth(Object parent, IResource member, int depth) {
			this.parent = parent;
			this.resource = member;
			this.depth = depth;
		}

		public Object getAdapter(Class adapter) {
			if (adapter == IWorkbenchAdapter.class)
				return this;
			return null;
		}
		
		public Object getParent(Object object) {
			return parent;
		}
		
		public Object[] getChildren(Object object) {
			if (resource.getType() == IResource.FILE || depth == IResource.DEPTH_ZERO) {
				return new Object[0];
			}
			List children = new ArrayList();
			try {
				IResource[] members = ((IContainer)resource).members();
				for (int i = 0; i < members.length; i++) {
					IResource member = members[i];
					if (depth == IResource.DEPTH_INFINITE) {
						children.add(new ResourceAndDepth(this, member, IResource.DEPTH_INFINITE));
					} else if (depth == IResource.DEPTH_ONE && member.getType() == IResource.FILE) {
						children.add(new ResourceAndDepth(this, member, IResource.DEPTH_ZERO));
					}
				}
				return children.toArray(new Object[children.size()]);
			} catch (CoreException e) {
				TeamUIPlugin.log(e);
				return new Object[0];
			}
		}

		public int getDepth() {
			return depth;
		}

		public Object getParent() {
			return parent;
		}

		public IResource getResource() {
			return resource;
		}
	}

    public ResourceMappingContentProvider(ModelProvider provider) {
		this.provider = provider;
    }

    public Object getRoot() {
        return provider;
    }

    public Object[] getChildren(Object parentElement) {
        if (parentElement == provider) {
        	List children = new ArrayList();
        	ResourceTraversal[] traversals = getTraversals();
        	for (int i = 0; i < traversals.length; i++) {
				ResourceTraversal traversal = traversals[i];
				IResource[] resources = traversal.getResources();
				for (int j = 0; j < resources.length; j++) {
					IResource resource = resources[j];
					children.add(new ResourceAndDepth(provider, resource, traversal.getDepth()));
				}
			}
            return children.toArray(new Object[children.size()]);
        }
        return new Object[0];
    }

	private ResourceTraversal[] getTraversals() {
		List result = new ArrayList();
		ResourceMapping[] mappings = input.getMappings(provider.getDescriptor().getId());
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			ResourceTraversal[] traversals = input.getTraversals(mapping);
			for (int j = 0; j < traversals.length; j++) {
				ResourceTraversal traversal = traversals[j];
				result.add(traversal);
			}
		}
		return ResourceMappingScope.combineTraversals((ResourceTraversal[]) result.toArray(new ResourceTraversal[result.size()]));
	}

    public Object getParent(Object element) {
        if (element instanceof ResourceAndDepth) {
			ResourceAndDepth rad = (ResourceAndDepth) element;
			return rad.getParent();
		}
        return null;
    }

    public boolean hasChildren(Object element) {
        if (element == provider)
            return true;
        if (element instanceof ResourceAndDepth) {
			ResourceAndDepth rad = (ResourceAndDepth) element;
			return rad.getResource().getType() != IResource.FILE && rad.getDepth() != IResource.DEPTH_ZERO;
		}
        return false;
    }

    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    public void dispose() {
        // Nothing to do
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Nothing to do
    }

	public void init(IResourceMappingScope input, ISynchronizationContext context) {
		this.input = input;
		this.context = context;
	}
}