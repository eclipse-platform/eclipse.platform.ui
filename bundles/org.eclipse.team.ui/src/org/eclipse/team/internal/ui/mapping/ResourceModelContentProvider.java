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

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.*;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IDiffTree;
import org.eclipse.team.core.mapping.*;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.mapping.SynchronizationContentProvider;
import org.eclipse.ui.model.WorkbenchContentProvider;

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
public class ResourceModelContentProvider extends SynchronizationContentProvider {

	private WorkbenchContentProvider provider;

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.AbstractTeamAwareContentProvider#getDelegateContentProvider()
	 */
	protected ITreeContentProvider getDelegateContentProvider() {
		if (provider == null)
			provider = new WorkbenchContentProvider();
		return provider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.AbstractTeamAwareContentProvider#getModelProviderId()
	 */
	protected String getModelProviderId() {
		return ModelProvider.RESOURCE_MODEL_PROVIDER_ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.AbstractTeamAwareContentProvider#getModelRoot()
	 */
	protected Object getModelRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider#isInScope(org.eclipse.team.core.mapping.IResourceMappingScope, java.lang.Object, java.lang.Object)
	 */
	protected boolean isInScope(ISynchronizationScope scope, Object parent, Object object) {
		if (object instanceof IResource) {
			IResource resource = (IResource) object;
			if (resource == null)
				return false;
			if (scope.contains(resource))
				return true;
			if (hasChildrenInScope(scope, object, resource)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean hasChildrenInScope(ISynchronizationScope scope, Object object, IResource resource) {
		IResource[] roots = scope.getRoots();
		for (int i = 0; i < roots.length; i++) {
			IResource root = roots[i];
			if (resource.getFullPath().isPrefixOf(root.getFullPath()))
				return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.AbstractTeamAwareContentProvider#dispose()
	 */
	public void dispose() {
		provider.dispose();
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider#getChildrenInContext(org.eclipse.team.core.mapping.ISynchronizationContext, java.lang.Object, java.lang.Object[])
	 */
	protected Object[] getChildrenInContext(ISynchronizationContext context, Object parent, Object[] children) {
		if (parent instanceof IResource) {
			IResource resource = (IResource) parent;
			
			Set result = new HashSet();
			for (int i = 0; i < children.length; i++) {
				Object object = children[i];
				result.add(object);
			}
			IPath[] childPaths = context.getDiffTree().getChildren(resource.getFullPath());
			for (int i = 0; i < childPaths.length; i++) {
				IPath path = childPaths[i];
				IDiff delta = context.getDiffTree().getDiff(path);
				IResource child;
				if (delta == null) {
					// the path has descendent deltas so it must be a folder
					if (path.segmentCount() == 1) {
						child = ((IWorkspaceRoot)resource).getProject(path.lastSegment());
					} else {
						child = ((IContainer)resource).getFolder(new Path(path.lastSegment()));
					}
				} else {
					child = context.getDiffTree().getResource(delta);
				}
				if (isInScope(context.getScope(), parent, child)) {
					result.add(child);
				}
			}
			return super.getChildrenInContext(context, parent, result.toArray(new Object[result.size()]));
		}
		return super.getChildrenInContext(context, parent, children);
	}

	protected ResourceTraversal[] getTraversals(ISynchronizationContext context, Object object) {
		ISynchronizationScope scope = context.getScope();
		// First see if the object is a root of the scope
		ResourceMapping mapping = scope.getMapping(object);
		if (mapping != null)
			return scope.getTraversals(mapping);
		// Next, check if the object is within the scope
		if (object instanceof IResource) {
			IResource resource = (IResource) object;
			if (scope.contains(resource)) {
				List result = new ArrayList();
				ResourceTraversal[] traversals = scope.getTraversals();
				for (int i = 0; i < traversals.length; i++) {
					ResourceTraversal traversal = traversals[i];
					if (traversal.contains(resource)) {
						boolean include = false;
						int depth = traversal.getDepth();
						if (depth == IResource.DEPTH_INFINITE) {
							include = true;
						} else {
							IResource[] roots = traversal.getResources();
							for (int j = 0; j < roots.length; j++) {
								IResource root = roots[j];
								if (root.equals(resource)) {
									include = true;
									break;
								}
								if (root.getFullPath().equals(resource.getFullPath().removeLastSegments(1)) && depth == IResource.DEPTH_ONE) {
									include = true;
									depth = IResource.DEPTH_ZERO;
									break;
								}
							}
						}
						if (include)
							result.add(new ResourceTraversal(new IResource[] { resource}, depth, IResource.NONE));
					}
				}
				return (ResourceTraversal[]) result.toArray(new ResourceTraversal[result.size()]);
			} else {
				// The resource is a parent of an in-scope resource
				// TODO: fails due to se of roots
				ResourceMapping[] mappings = scope.getMappings(ModelProvider.RESOURCE_MODEL_PROVIDER_ID);
				List result = new ArrayList();
				for (int i = 0; i < mappings.length; i++) {
					ResourceMapping resourceMapping = mappings[i];
					if (resourceMapping.getModelObject() instanceof IResource) {
						IResource root = (IResource) resourceMapping.getModelObject();
						if (resource.getFullPath().isPrefixOf(root.getFullPath())) {
							mapping = scope.getMapping(root);
							if (mapping != null) {
								ResourceTraversal[] traversals = scope.getTraversals(mapping);
								result.addAll(Arrays.asList(traversals));
							}
						}
					}
				}
				return (ResourceTraversal[]) result.toArray(new ResourceTraversal[result.size()]);
			}
		}
		return new ResourceTraversal[0];
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider#hasChildrenInContext(org.eclipse.team.core.mapping.ISynchronizationContext, java.lang.Object)
	 */
	protected boolean hasChildrenInContext(ISynchronizationContext context, Object element) {
		if (element instanceof IContainer) {
			IContainer container = (IContainer) element;
			// For containers check to see if the delta contains any children
			if (context != null) {
				IDiffTree tree = context.getDiffTree();
				if (tree.getChildren(container.getFullPath()).length > 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider#propertyChanged(int, org.eclipse.core.runtime.IPath[])
	 */
	public void propertyChanged(final int property, final IPath[] paths) {
		Utils.syncExec(new Runnable() {
			public void run() {
				ISynchronizationContext context = getContext();
				if (context != null) {
					IResource[] resources = getResources(paths);
					if (resources.length > 0)
						((AbstractTreeViewer)getViewer()).update(resources, null);
				}
			}
		}, (StructuredViewer)getViewer());
	}

	private IResource[] getResources(IPath[] paths) {
		List resources = new ArrayList();
		for (int i = 0; i < paths.length; i++) {
			IPath path = paths[i];
			IResource resource = getResource(path);
			if (resource != null)
				resources.add(resource);
		}
		return (IResource[]) resources.toArray(new IResource[resources.size()]);
	}

	private IResource getResource(IPath path) {
		// Does the resource exist locally
		IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		if (resource != null) {
			return resource;
		}
		// Look in the diff tree for a phantom
		ISynchronizationContext context = getContext();
		if (context != null) {
			IResourceDiffTree diffTree = context.getDiffTree();
			// Is there a diff for the path
			IDiff node = diffTree.getDiff(path);
			if (node != null) {
				return diffTree.getResource(node);
			}
			// Is there any descendants of the path
			if (diffTree.getChildren(path).length > 0) {
				if (path.segmentCount() == 1) {
					return ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0));
				} else if (path.segmentCount() > 1) {
					return ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
				}
			}
		}
		return null;
	}
	
	protected StructuredViewer getStructuredViewer() {
		return (StructuredViewer)getViewer();
	}
}