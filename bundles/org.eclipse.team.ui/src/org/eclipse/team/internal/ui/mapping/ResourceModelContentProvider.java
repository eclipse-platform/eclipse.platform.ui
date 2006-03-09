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
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IDiffTree;
import org.eclipse.team.core.mapping.*;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.mapping.SynchronizationContentProvider;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;

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
			if (!resource.getProject().isAccessible())
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
		if (!resource.isAccessible())
			return false;
		IResource[] roots = scope.getRoots();
		for (int i = 0; i < roots.length; i++) {
			IResource root = roots[i];
			if (resource.getFullPath().isPrefixOf(root.getFullPath()))
				return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
	 */
	public void init(ICommonContentExtensionSite site) {
		super.init(site);
		TeamUIPlugin.getPlugin().getPreferenceStore().addPropertyChangeListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.AbstractTeamAwareContentProvider#dispose()
	 */
	public void dispose() {
		provider.dispose();
		super.dispose();
		TeamUIPlugin.getPlugin().getPreferenceStore().removePropertyChangeListener(this);
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IPreferenceIds.SYNCVIEW_DEFAULT_LAYOUT)) {
			refresh();
		}
		super.propertyChange(event);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider#getChildrenInContext(org.eclipse.team.core.mapping.ISynchronizationContext, java.lang.Object, java.lang.Object[])
	 */
	protected Object[] getChildrenInContext(ISynchronizationContext context, Object parent, Object[] children) {
		if (parent instanceof IResource) {
			IResource resource = (IResource) parent;
			if (!resource.getProject().isAccessible())
				return new Object[0];
			IResourceDiffTree diffTree = context.getDiffTree();
			Object[] allChildren = filterChildren(diffTree, resource, children);
			return super.getChildrenInContext(context, parent, allChildren);
		}
		return super.getChildrenInContext(context, parent, children);
	}

	protected Object[] filterChildren(IResourceDiffTree diffTree, IResource resource, Object[] children) {
		Object[] allChildren;
		if (getLayout().equals(IPreferenceIds.FLAT_LAYOUT) && resource.getType() == IResource.PROJECT) {
			allChildren = getFlatChildren(diffTree, resource);
		} else if (getLayout().equals(IPreferenceIds.COMPRESSED_LAYOUT) && resource.getType() == IResource.PROJECT) {
			allChildren = getCompressedChildren(diffTree, (IProject)resource, children);
		} else if (getLayout().equals(IPreferenceIds.COMPRESSED_LAYOUT) && resource.getType() == IResource.FOLDER) {
			allChildren = getCompressedChildren(diffTree, (IFolder)resource, children);
		} else {
			allChildren = getTreeChildren(diffTree, resource, children);
		}
		return allChildren;
	}

	private Object[] getCompressedChildren(IResourceDiffTree diffTree, IProject project, Object[] children) {
		Set result = new HashSet();
		IDiff[] diffs = diffTree.getDiffs(project, IResource.DEPTH_INFINITE);
		for (int i = 0; i < diffs.length; i++) {
			IDiff diff = diffs[i];
			IResource resource = diffTree.getResource(diff);
			if (resource.getType() == IResource.FILE) {
				IContainer parent = resource.getParent();
				if (parent.getType() == IResource.FOLDER)
					result.add(parent);
				else 
					result.add(resource);
			} else if (resource.getType() == IResource.FOLDER)
				result.add(resource);
		}
		return result.toArray();
	}

	/*
	 * Only return the files that are direct children of the folder
	 */
	private Object[] getCompressedChildren(IResourceDiffTree diffTree, IFolder folder, Object[] children) {
		Set result = new HashSet();
		for (int i = 0; i < children.length; i++) {
			Object object = children[i];
			if (object instanceof IResource) {
				IResource resource = (IResource) object;
				if (resource.getType() == IResource.FILE)
					result.add(resource);
			}
		}
		IDiff[] diffs = diffTree.getDiffs(folder, IResource.DEPTH_ONE);
		for (int i = 0; i < diffs.length; i++) {
			IDiff diff = diffs[i];
			IResource resource = diffTree.getResource(diff);
			if (resource.getType() == IResource.FILE)
				result.add(resource);
		}
		return result.toArray();
	}

	private Object[] getFlatChildren(IResourceDiffTree diffTree, IResource resource) {
		Object[] allChildren;
		IDiff[] diffs = diffTree.getDiffs(resource, IResource.DEPTH_INFINITE);
		ArrayList result = new ArrayList();
		for (int i = 0; i < diffs.length; i++) {
			IDiff diff = diffs[i];
			result.add(diffTree.getResource(diff));
		}
		allChildren = result.toArray();
		return allChildren;
	}

	private Object[] getTreeChildren(IResourceDiffTree diffTree, IResource resource, Object[] children) {
		Set result = new HashSet();
		for (int i = 0; i < children.length; i++) {
			Object object = children[i];
			result.add(object);
		}
		IPath[] childPaths = diffTree.getChildren(resource.getFullPath());
		for (int i = 0; i < childPaths.length; i++) {
			IPath path = childPaths[i];
			IDiff delta = diffTree.getDiff(path);
			IResource child;
			if (delta == null) {
				// the path has descendent deltas so it must be a folder
				if (path.segmentCount() == 1) {
					child = ((IWorkspaceRoot)resource).getProject(path.lastSegment());
				} else {
					child = ((IContainer)resource).getFolder(new Path(path.lastSegment()));
				}
			} else {
				child = diffTree.getResource(delta);
			}
			result.add(child);
		}
		Object[] allChildren = result.toArray(new Object[result.size()]);
		return allChildren;
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
	public void propertyChanged(IDiffTree tree, final int property, final IPath[] paths) {
		Utils.syncExec(new Runnable() {
			public void run() {
				ISynchronizationContext context = getContext();
				if (context != null) {
					IResource[] resources = getResources(context, paths);
					if (resources.length > 0)
						((AbstractTreeViewer)getViewer()).update(resources, null);
				}
			}
		}, (StructuredViewer)getViewer());
	}

	private IResource[] getResources(ISynchronizationContext context, IPath[] paths) {
		List resources = new ArrayList();
		for (int i = 0; i < paths.length; i++) {
			IPath path = paths[i];
			IResource resource = getResource(context, path);
			if (resource != null)
				resources.add(resource);
		}
		return (IResource[]) resources.toArray(new IResource[resources.size()]);
	}

	private IResource getResource(ISynchronizationContext context, IPath path) {
		// Does the resource exist locally
		IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		if (resource != null) {
			return resource;
		}
		// Look in the diff tree for a phantom
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
	
	protected String getLayout() {
		return TeamUIPlugin.getPlugin().getPreferenceStore().getString(IPreferenceIds.SYNCVIEW_DEFAULT_LAYOUT);
	}
	
	public Object[] getChildren(Object parent) {
		if (parent instanceof ISynchronizationContext) {
			// Put the resource projects directly under the context
			parent = getModelRoot();
		}
		return super.getChildren(parent);
	}
	
	public boolean hasChildren(Object element) {
		if (element instanceof ISynchronizationContext) {
			// Put the resource projects directly under the context
			element = getModelRoot();
		}
		return super.hasChildren(element);
	}
	
	public Object[] getElements(Object parent) {
		if (parent instanceof ISynchronizationContext) {
			// Put the resource projects directly under the context
			parent = getModelRoot();
		}
		return super.getElements(parent);
	}
	
	public Object getParent(Object element) {
		if (element instanceof IProject) {
			ISynchronizationContext context = getContext();
			if (context != null)
				return context;
		}
		return super.getParent(element);
	}
	
	protected void refresh() {
		Utils.syncExec(new Runnable() {
			public void run() {
				TreeViewer treeViewer = ((TreeViewer)getViewer());
				treeViewer.refresh();
			}
		
		}, getViewer().getControl());
	}
}