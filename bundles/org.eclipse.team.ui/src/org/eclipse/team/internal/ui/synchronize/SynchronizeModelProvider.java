/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * This class is reponsible for creating and maintaining a presentation model of 
 * {@link SynchronizeModelElement} elements that can be shown in a viewer. The model
 * is based on the synchronization information contained in the provided {@link SyncInfoSet}.
 * <p>
 * label updates (property propagation to parent nodes)
 * sync change listener (changes, additions, removals, reset)
 * batching busy updates
 * </p>
 * 
 * @see HierarchicalModelProvider
 * @see CompressedFoldersModelProvider
 * @since 3.0
 */
public abstract class SynchronizeModelProvider extends AbstractSynchronizeModelProvider implements ISyncInfoSetChangeListener {
	
	protected Map resourceMap = Collections.synchronizedMap(new HashMap());

    protected static final boolean DEBUG = false;
	
	public SynchronizeModelProvider(ISynchronizePageConfiguration configuration, SyncInfoSet set) {
		super(configuration, set);
	}

	public SynchronizeModelProvider(AbstractSynchronizeModelProvider parentProvider, ISynchronizeModelElement modelRoot, ISynchronizePageConfiguration configuration, SyncInfoSet set) {
		super(parentProvider, modelRoot, configuration, set);
		associateRoot(modelRoot);
	}
	
    private void associateRoot(ISynchronizeModelElement modelRoot) {
        // associate the root resource with the provider's root element
		resourceMap.put(ResourcesPlugin.getWorkspace().getRoot(), modelRoot);
    }

    /**
	 * The provider can try and return a mapping for the provided object. Providers often use mappings
	 * to store the source of a logical element they have created. For example, when displaying resource
	 * based logical elements, a provider will cache the resource -> element mapping for quick retrieval
	 * of the element when resource based changes are made.
	 * 
	 * @param object the object to query for a mapping
	 * @return an object created by this provider that would be shown in a viewer, or <code>null</code>
	 * if the provided object is not mapped by this provider.
	 */
	public Object getMapping(Object object) {
		return resourceMap.get(object);
	}
	
	/**
	 * Dispose of the builder
	 */
	public void dispose() {
		resourceMap.clear();
		super.dispose();
	}

	/**
	 * Returns the sorter for this model provider.
	 * 
	 * @return the sorter for this model provider. 
	 */
	public abstract ViewerSorter getViewerSorter();

	/**
	 * Return the model object (i.e. an instance of <code>SyncInfoModelElement</code>
	 * or one of its subclasses) for the given IResource.
	 * @param resource
	 *            the resource
	 * @return the <code>SyncInfoModelElement</code> for the given resource
	 */
	protected ISynchronizeModelElement getModelObject(IResource resource) {
		return (ISynchronizeModelElement) resourceMap.get(resource);
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizeModelProvider#getModelObjects(org.eclipse.core.resources.IResource)
     */
    protected ISynchronizeModelElement[] getModelObjects(IResource resource) {
        ISynchronizeModelElement element = getModelObject(resource);
        if (element == null) {
            return new ISynchronizeModelElement[0];
        }
        return new ISynchronizeModelElement[] { element };
    }
    
	protected void associateDiffNode(ISynchronizeModelElement node) {
		IResource resource = node.getResource();
		if(resource != null) {
			resourceMap.put(resource, node);
		}
	}

	protected void unassociateDiffNode(IResource resource) {
		resourceMap.remove(resource);
	}
	
	/**
	 * Helper method to remove a resource from the viewer. If the resource
	 * is not mapped to a model element, this is a no-op.
	 * @param resource the resource to remove
	 */
	protected void removeFromViewer(IResource resource) {
		ISynchronizeModelElement element = getModelObject(resource);
		if(element != null) {
			removeFromViewer(element);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizeModelProvider#clearModelObjects(org.eclipse.team.ui.synchronize.ISynchronizeModelElement)
	 */
	protected void clearModelObjects(ISynchronizeModelElement node) {
		super.clearModelObjects(node);
		if (node == getModelRoot()) {
	        // If we are clearing everything under the root
	        // than just purge the resource map
	        resourceMap.clear();
	        // Reassociate the root node to allow the children to be readded
	        associateRoot(getModelRoot());
		} else {
			IResource resource = node.getResource();
			if (resource != null) {
				unassociateDiffNode(resource);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizeModelProvider#addToViewer(org.eclipse.team.ui.synchronize.ISynchronizeModelElement)
	 */
	protected void addToViewer(ISynchronizeModelElement node) {
		associateDiffNode(node);
		super.addToViewer(node);
	}

	/* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizeModelProvider#hasViewerState()
     */
    protected boolean hasViewerState() {
        return ! resourceMap.isEmpty();
    }

	public ISynchronizeModelElement[] getClosestExistingParents(IResource resource) {
		ISynchronizeModelElement element = getModelObject(resource);
		if(element == null) {
			do {
				resource = resource.getParent();
				element = getModelObject(resource);
			} while(element == null && resource != null);
		}
		if (element == null) {
		    return new ISynchronizeModelElement[0];
		}
		return new ISynchronizeModelElement[] { element };
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizeModelProvider#handleChanges(org.eclipse.team.core.synchronize.ISyncInfoTreeChangeEvent)
     */
    protected final void handleChanges(ISyncInfoTreeChangeEvent event, IProgressMonitor monitor) {
        super.handleChanges(event, monitor);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizeModelProvider#handleResourceChanges(org.eclipse.team.core.synchronize.ISyncInfoTreeChangeEvent)
     */
	protected void handleResourceChanges(ISyncInfoTreeChangeEvent event) {
		// Refresh the viewer for each changed resource
		SyncInfo[] infos = event.getChangedResources();
		for (int i = 0; i < infos.length; i++) {
			SyncInfo info = infos[i];
			IResource local = info.getLocal();
			ISynchronizeModelElement diffNode = getModelObject(local);
			if (diffNode != null) {
				handleChange(diffNode, info);
			}
		}	
	}

    /**
     * The sync state for the existing diff node has changed and the new state
     * is provided by the given sync info.
     * @param diffNode the changed diff node
     * @param info the new sync state
     */
	protected void handleChange(ISynchronizeModelElement diffNode, SyncInfo info) {
		IResource local = info.getLocal();

		if(diffNode instanceof SyncInfoModelElement) {
			((SyncInfoModelElement)diffNode).update(info);
			propogateConflictState(diffNode, false);
			queueForLabelUpdate(diffNode);
		} else {
			removeFromViewer(local);
			addResources(new SyncInfo[] {info});
		}
	}

    /**
     * Add the give sync infos to the provider
     * @param infos the added infos
     */
	protected void addResources(SyncInfo[] added) {
		for (int i = 0; i < added.length; i++) {
			SyncInfo info = added[i];
			ISynchronizeModelElement node = getModelObject(info.getLocal());
			if (node != null) {
				// Somehow the node exists. Remove it and read it to ensure
				// what is shown matches the contents of the sync set
				removeFromViewer(info.getLocal());
			}
			// Add the node to the root
			node = createModelObject(getModelRoot(), info);
			buildModelObjects(node);
		}
	}

	/**
	 * Create the model object for the given sync info as a child of the given parent node.
	 * @param parent the parent
	 * @param info the info to be used for the new node
	 * @return the created node
	 */
    protected abstract ISynchronizeModelElement createModelObject(ISynchronizeModelElement parent, SyncInfo info);
}
