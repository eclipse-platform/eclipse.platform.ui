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

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.mapping.ISynchronizationScope;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.examples.filesystem.FileSystemPlugin;
import org.eclipse.team.examples.model.*;
import org.eclipse.team.examples.model.mapping.ExampleModelProvider;
import org.eclipse.team.examples.model.ui.ModelNavigatorContentProvider;
import org.eclipse.team.internal.ui.mapping.SynchronizationResourceMappingContext;
import org.eclipse.team.ui.mapping.SynchronizationContentProvider;
import org.eclipse.ui.navigator.*;

/**
 * The content provider that is used for synchronizations.
 * It also makes use of the Common Navigator pipeline 
 * to override the resource content extension so that model projects will
 * replace the corresponding resource project in the Synchronize view.
 */
public class ModelSyncContentProvider extends SynchronizationContentProvider implements IPipelinedTreeContentProvider {

	private ModelNavigatorContentProvider delegate;
	
	public ModelSyncContentProvider() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
	 */
	public void init(ICommonContentExtensionSite site) {
		super.init(site);
		delegate = new ModelNavigatorContentProvider();
		delegate.init(site);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (delegate != null)
			delegate.dispose();
	}
	
	protected ITreeContentProvider getDelegateContentProvider() {
		return delegate;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider#getModelProviderId()
	 */
	protected String getModelProviderId() {
		return ExampleModelProvider.ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider#getModelRoot()
	 */
	protected Object getModelRoot() {
		return ModelWorkspace.getRoot();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider#getTraversals(org.eclipse.team.core.mapping.ISynchronizationContext, java.lang.Object)
	 */
	protected ResourceTraversal[] getTraversals(
			ISynchronizationContext context, Object object) {
		if (object instanceof ModelObject) {
			ModelObject mo = (ModelObject) object;
			ResourceMapping mapping = (ResourceMapping)mo.getAdapter(ResourceMapping.class);
			ResourceMappingContext rmc = new SynchronizationResourceMappingContext(context);
			try {
				// Technically speaking, this may end up being too long running for this
				// but it will do for illustration purposes
				return mapping.getTraversals(rmc, new NullProgressMonitor());
			} catch (CoreException e) {
				FileSystemPlugin.log(e.getStatus());
			}
		}
		return new ResourceTraversal[0];
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider#getChildrenInContext(org.eclipse.team.core.mapping.ISynchronizationContext, java.lang.Object, java.lang.Object[])
	 */
	protected Object[] getChildrenInContext(ISynchronizationContext context, Object parent, Object[] children) {
		Set allChildren = new HashSet();
		allChildren.addAll(Arrays.asList(super.getChildrenInContext(context, parent, children)));
		// We need to override this method in order to ensure that any elements
		// that exist in the context but do not exist locally are included
		if (parent instanceof ModelContainer) {
			ModelContainer mc = (ModelContainer) parent;
			IDiff[] diffs = context.getDiffTree().getDiffs(mc.getResource(), IResource.DEPTH_ONE);
			for (int i = 0; i < diffs.length; i++) {
				IDiff diff = diffs[i];
				IResource resource = ResourceDiffTree.getResourceFor(diff);
				if (!resource.exists() && ModelObjectDefinitionFile.isModFile(resource)) {
					allChildren.add(ModelObject.create(resource));
				}
			}
		}
		if (parent instanceof ModelObjectDefinitionFile) {
			ResourceTraversal[] traversals = getTraversals(context, parent);
			IDiff[] diffs = context.getDiffTree().getDiffs(traversals);
			for (int i = 0; i < diffs.length; i++) {
				IDiff diff = diffs[i];
				IResource resource = ResourceDiffTree.getResourceFor(diff);
				if (!resource.exists() && ModelObjectElementFile.isMoeFile(resource)) {
					allChildren.add(ModelObject.create(resource));
				}
			}
		}
		return allChildren.toArray(new Object[allChildren.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedChildren(java.lang.Object, java.util.Set)
	 */
	public void getPipelinedChildren(Object aParent, Set theCurrentChildren) {
		// Nothing to do
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedElements(java.lang.Object, java.util.Set)
	 */
	public void getPipelinedElements(Object anInput, Set theCurrentElements) {
		// Replace any model projects with a ModelProject if the input
		// is a synchronization context
		if (anInput instanceof ISynchronizationContext) {
			List newProjects = new ArrayList();
			for (Iterator iter = theCurrentElements.iterator(); iter.hasNext();) {
				Object element = iter.next();
				if (element instanceof IProject) {
					IProject project = (IProject) element;
					try {
						if (ModelProject.isModProject(project)) {
							iter.remove();
							newProjects.add(ModelObject.create(project));
						}
					} catch (CoreException e) {
						FileSystemPlugin.log(e.getStatus());
					}
				}
			}
			theCurrentElements.addAll(newProjects);
		} else if (anInput instanceof ISynchronizationScope) {
			// When the root is a scope, we should return
			// our model provider so all model providers appear
			// at the root of the viewer.
			theCurrentElements.add(getModelProvider());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedParent(java.lang.Object, java.lang.Object)
	 */
	public Object getPipelinedParent(Object anObject, Object aSuggestedParent) {
		// We're not changing the parenting of any resources
		return aSuggestedParent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptAdd(org.eclipse.ui.navigator.PipelinedShapeModification)
	 */
	public PipelinedShapeModification interceptAdd(PipelinedShapeModification anAddModification) {
		if (anAddModification.getParent() instanceof ISynchronizationContext) {
			for (Iterator iter = anAddModification.getChildren().iterator(); iter.hasNext();) {
				Object element = iter.next();
				if (element instanceof IProject) {
					IProject project = (IProject) element;
					try {
						if (ModelProject.isModProject(project)) {
							iter.remove();
						}
					} catch (CoreException e) {
						FileSystemPlugin.log(e.getStatus());
					}
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptRefresh(org.eclipse.ui.navigator.PipelinedViewerUpdate)
	 */
	public boolean interceptRefresh(PipelinedViewerUpdate aRefreshSynchronization) {
		// No need to intercept the refresh
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptRemove(org.eclipse.ui.navigator.PipelinedShapeModification)
	 */
	public PipelinedShapeModification interceptRemove(PipelinedShapeModification aRemoveModification) {
		// No need to intercept the remove
		return aRemoveModification;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptUpdate(org.eclipse.ui.navigator.PipelinedViewerUpdate)
	 */
	public boolean interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization) {
		// No need to intercept the update
		return false;
	}

}
