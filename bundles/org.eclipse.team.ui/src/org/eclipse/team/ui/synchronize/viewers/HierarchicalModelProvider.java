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
package org.eclipse.team.ui.synchronize.viewers;

import java.util.*;

import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.core.ITeamStatus;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.team.internal.ui.Policy;

/**
 * An input that can be used with both {@link } and 
 * {@link }. The
 * job of this input is to create the logical model of the contents of the
 * sync set for displaying to the user. The created logical model must diff
 * nodes.
 * <p>
 * 1. First, prepareInput is called to initialize the model with the given sync
 * set. Building the model occurs in the ui thread.
 * 2. The input must react to changes in the sync set and adjust its diff node
 * model then update the viewer. In effect mediating between the sync set
 * changes and the model shown to the user. This happens in the ui thread.
 * </p>
 * NOT ON DEMAND - model is created then maintained!
 * @since 3.0
 */
public class HierarchicalModelProvider extends SynchronizeModelProvider implements ISyncInfoSetChangeListener {

	// Map from resources to model objects. This allows effecient lookup
	// of model objects based on changes occuring to resources.
	private Map resourceMap = Collections.synchronizedMap(new HashMap());
	
	// The viewer this input is being displayed in
	private AbstractTreeViewer viewer;
	
	// Flasg to indicate if tree control should be updated while
	// building the model.
	private boolean refreshViewer;
	
	private RootDiffNode root;
	
	private SyncInfoTree set;
	
	private Set pendingLabelUpdates = new HashSet();
	
	private LabelUpdateJob labelUpdater = new LabelUpdateJob();
	
	class LabelUpdateJob extends UIJob {
		public static final int BATCH_WAIT_INCREMENT = 100;
		Set nodes = new HashSet();
		public LabelUpdateJob() {
			super(Policy.bind("HierarchicalModelProvider.0")); //$NON-NLS-1$
			setSystem(true);
		}
		public IStatus runInUIThread(IProgressMonitor monitor) {
			Object[] updates;
			synchronized(nodes) {
				updates = nodes.toArray(new Object[nodes.size()]);
				nodes.clear();
			}
			if (canUpdateViewer()) {
				AbstractTreeViewer tree = getTreeViewer();
				tree.update(updates, null);
			}
			schedule(BATCH_WAIT_INCREMENT);
			return Status.OK_STATUS;
		}
		public void add(Object node, boolean isBusy) {
			synchronized(nodes) {
				nodes.add(node);
			}
			if (isBusy) {
				schedule(BATCH_WAIT_INCREMENT);
			} else {
				// Wait when unbusying to give the events a chance to propogate through
				// the collector
				schedule(BATCH_WAIT_INCREMENT * 10);
			}
		}
		public boolean shouldRun() {
			return !nodes.isEmpty();
		}
	}
	
	private IPropertyChangeListener listener = new IPropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent event) {
			if (event.getProperty() == SynchronizeModelElement.BUSY_PROPERTY) {
				labelUpdater.add(event.getSource(), ((Boolean)event.getNewValue()).booleanValue());
			}
		}
	};
	
	/**
	 * Create an input based on the provide sync set. The input is not initialized
	 * until <code>prepareInput</code> is called. 
	 * 
	 * @param set the sync set used as the basis for the model created by this input.
	 */
	public HierarchicalModelProvider(SyncInfoTree set) {
		Assert.isNotNull(set);
		this.root = new RootDiffNode();
		this.set = set;
	}

	/**
	 * Return the model object (i.e. an instance of <code>SyncInfoModelElement</code>
	 * or one of its subclasses) for the given IResource.
	 * @param resource
	 *            the resource
	 * @return the <code>SyncInfoModelElement</code> for the given resource
	 */
	protected SynchronizeModelElement getModelObject(IResource resource) {
		return (SynchronizeModelElement) resourceMap.get(resource);
	}

	/**
	 * Return the <code>AbstractTreeViewer</code> asociated with this content
	 * provider or <code>null</code> if the viewer is not of the proper type.
	 * @return
	 */
	public AbstractTreeViewer getTreeViewer() {
		return viewer;
	}

	public void setViewer(StructuredViewer viewer) {
		Assert.isTrue(viewer instanceof AbstractTreeViewer);
		this.viewer = (AbstractTreeViewer)viewer;
	}

	public ViewerSorter getViewerSorter() {
		return new SynchronizeModelElementSorter();
	}

	/**
	 * Builds the viewer model based on the contents of the sync set.
	 */
	public SynchronizeModelElement prepareInput(IProgressMonitor monitor) {
		// Connect to the sync set which will register us as a listener and give us a reset event
		// in a background thread
		getSyncInfoTree().connect(this, monitor);
		return getRoot();
	}
	
	/**
	 * Dispose of the builder
	 */
	public void dispose() {
		resourceMap.clear();
		getSyncInfoTree().removeSyncSetChangedListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.ccvs.syncviews.views.ISyncSetChangedListener#syncSetChanged()
	 */
	public void syncInfoChanged(final ISyncInfoSetChangeEvent event, IProgressMonitor monitor) {
		if (! (event instanceof ISyncInfoTreeChangeEvent)) {
			reset();
		} else {
			final Control ctrl = viewer.getControl();
			if (ctrl != null && !ctrl.isDisposed()) {
				ctrl.getDisplay().syncExec(new Runnable() {
					public void run() {
						if (!ctrl.isDisposed()) {
							BusyIndicator.showWhile(ctrl.getDisplay(), new Runnable() {
								public void run() {
									handleChanges((ISyncInfoTreeChangeEvent)event);
									getRoot().fireChanges();
								}
							});
						}
					}
				});
			}
		}
	}

	/**
	 * For each node create children based on the contents of
	 * @param node
	 * @return
	 */
	protected IDiffElement[] buildModelObjects(SynchronizeModelElement node) {
		IDiffElement[] children = createModelObjects(node);
		for (int i = 0; i < children.length; i++) {
			IDiffElement element = children[i];
			if (element instanceof SynchronizeModelElement) {
				buildModelObjects((SynchronizeModelElement) element);
			}
		}
		return children;
	}

	/**
	 * Invoked by the <code>buildModelObject</code> method to create
	 * the childen of the given node. This method can be overriden
	 * by subclasses but subclasses should inv
	 * @param container
	 * @return
	 */
	protected IDiffElement[] createModelObjects(SynchronizeModelElement container) {
		IResource resource = null;
		if (container == getRoot()) {
			resource = ResourcesPlugin.getWorkspace().getRoot();
		} else {
			resource = container.getResource();
		}
		if(resource != null) {
			SyncInfoTree infoTree = getSyncInfoTree();
			IResource[] children = infoTree.members(resource);
			SynchronizeModelElement[] nodes = new SynchronizeModelElement[children.length];
			for (int i = 0; i < children.length; i++) {
				nodes[i] = createModelObject(container, children[i]);
			}
			return nodes;	
		}
		return new IDiffElement[0];
	}

	protected SynchronizeModelElement createModelObject(SynchronizeModelElement parent, IResource resource) {
		SyncInfo info = getSyncInfoTree().getSyncInfo(resource);
		SynchronizeModelElement newNode;
		if(info != null) {
			newNode = new SyncInfoModelElement(parent, info);
		} else {
			newNode = new UnchangedResourceModelElement(parent, resource);
		}
		addToViewer(newNode);
		return newNode;
	}

	/**
	 * Clear the model objects from the diff tree, cleaning up any cached state
	 * (such as resource to model object map). This method recurses deeply on
	 * the tree to allow the cleanup of any cached state for the children as
	 * well.
	 * @param node
	 *            the root node
	 */
	protected void clearModelObjects(SynchronizeModelElement node) {
		IDiffElement[] children = node.getChildren();
		for (int i = 0; i < children.length; i++) {
			IDiffElement element = children[i];
			if (element instanceof SynchronizeModelElement) {
				clearModelObjects((SynchronizeModelElement) element);
			}
		}
		IResource resource = node.getResource();
		if (resource != null) {
			unassociateDiffNode(resource);
		}
		IDiffContainer parent = node.getParent();
		if (parent != null) {
			parent.removeToRoot(node);
		}
	}

	/**
	 * Invokes <code>getModelObject(Object)</code> on an array of resources.
	 * @param resources
	 *            the resources
	 * @return the model objects for the resources
	 */
	protected Object[] getModelObjects(IResource[] resources) {
		Object[] result = new Object[resources.length];
		for (int i = 0; i < resources.length; i++) {
			result[i] = getModelObject(resources[i]);
		}
		return result;
	}

	protected void associateDiffNode(SynchronizeModelElement node) {
		IResource resource = node.getResource();
		if(resource != null) {
			resourceMap.put(resource, node);
		}
	}

	protected void unassociateDiffNode(IResource resource) {
		resourceMap.remove(resource);
	}

	/**
	 * Handle the changes made to the viewer's <code>SyncInfoSet</code>.
	 * This method delegates the changes to the three methods <code>handleResourceChanges(ISyncInfoSetChangeEvent)</code>,
	 * <code>handleResourceRemovals(ISyncInfoSetChangeEvent)</code> and
	 * <code>handleResourceAdditions(ISyncInfoSetChangeEvent)</code>.
	 * @param event
	 *            the event containing the changed resourcses.
	 */
	protected void handleChanges(ISyncInfoTreeChangeEvent event) {
		try {
			viewer.getControl().setRedraw(false);
			handleResourceChanges(event);
			handleResourceRemovals(event);
			handleResourceAdditions(event);
			firePendingLabelUpdates();
		} finally {
			viewer.getControl().setRedraw(true);
		}
	}

	/**
	 * Update the viewer for the sync set additions in the provided event. This
	 * method is invoked by <code>handleChanges(ISyncInfoSetChangeEvent)</code>.
	 * Subclasses may override.
	 * @param event
	 */
	protected void handleResourceAdditions(ISyncInfoTreeChangeEvent event) {
		IResource[] added = event.getAddedSubtreeRoots();
		addResources(added);
	}

	/**
	 * Update the viewer for the sync set changes in the provided event. This
	 * method is invoked by <code>handleChanges(ISyncInfoSetChangeEvent)</code>.
	 * Subclasses may override.
	 * @param event
	 */
	protected void handleResourceChanges(ISyncInfoTreeChangeEvent event) {
		// Refresh the viewer for each changed resource
		SyncInfo[] infos = event.getChangedResources();
		for (int i = 0; i < infos.length; i++) {
			SyncInfo info = infos[i];
			IResource local = info.getLocal();
			SynchronizeModelElement diffNode = getModelObject(local);
			// If a sync info diff node already exists then just update
			// it, otherwise remove the old diff node and create a new
			// sub-tree.
			if (diffNode != null) {
				handleChange(diffNode, info);
			}
		}
	}
	
	/**
	 * Handle the change for the existing diff node. The diff node
	 * should be changed to have the given sync info
	 * @param diffNode the diff node to be changed
	 * @param info the new sync info for the diff node
	 */
	protected void handleChange(SynchronizeModelElement diffNode, SyncInfo info) {
		IResource local = info.getLocal();
		// TODO: Get any additional sync bits
		if(diffNode instanceof SyncInfoModelElement) {
			boolean wasConflict = isConflicting(diffNode);
			// The update preserves any of the additional sync info bits
			((SyncInfoModelElement)diffNode).update(info);
			boolean isConflict = isConflicting(diffNode);
			updateLabel(diffNode);
			if (wasConflict && !isConflict) {
				setParentConflict(diffNode, false);
			} else if (!wasConflict && isConflict) {
				setParentConflict(diffNode, true);
			}
		} else {
			removeFromViewer(local);
			addResources(new IResource[] {local});
		}
		// TODO: set any additional sync info bits
	}

	protected boolean isConflicting(SynchronizeModelElement diffNode) {
		return (diffNode.getKind() & SyncInfo.DIRECTION_MASK) == SyncInfo.CONFLICTING;
	}

	/**
	 * Update the viewer for the sync set removals in the provided event. This
	 * method is invoked by <code>handleChanges(ISyncInfoSetChangeEvent)</code>.
	 * Subclasses may override.
	 * @param event
	 */
	protected void handleResourceRemovals(ISyncInfoTreeChangeEvent event) {
		// Remove the removed subtrees
		IResource[] removedRoots = event.getRemovedSubtreeRoots();
		for (int i = 0; i < removedRoots.length; i++) {
			removeFromViewer(removedRoots[i]);
		}
		// We have to look for folders that may no longer be in the set
		// (i.e. are in-sync) but still have descendants in the set
		IResource[] removedResources = event.getRemovedResources();
		for (int i = 0; i < removedResources.length; i++) {
			IResource resource = removedResources[i];
			if (resource.getType() != IResource.FILE) {
				SynchronizeModelElement node = getModelObject(resource);
				if (node != null) {
					removeFromViewer(resource);
					addResources(new IResource[] {resource});
				}
			}
		}
	}

	protected void reset() {
		try {
			refreshViewer = false;
			
			// Clear existing model, but keep the root node
			resourceMap.clear();
			clearModelObjects(getRoot());
			// remove all from tree viewer
			IDiffElement[] elements = getRoot().getChildren();
			for (int i = 0; i < elements.length; i++) {
				viewer.remove(elements[i]);
			}
			
			// Rebuild the model
			associateDiffNode(getRoot());
			buildModelObjects(getRoot());
			
			// Notify listeners that model has changed
			getRoot().fireChanges();
		} finally {
			refreshViewer = true;
		}
		TeamUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				if (viewer != null && !viewer.getControl().isDisposed()) {
					viewer.refresh();
				}
			}
		});
	}

	protected RootDiffNode getRoot() {
		return root;
	}
	
	protected SyncInfoTree getSyncInfoTree() {
		return set;
	}

	/**
	 * Remove any traces of the resource and any of it's descendants in the
	 * hiearchy defined by the content provider from the content provider and
	 * the viewer it is associated with.
	 * @param resource
	 */
	protected void removeFromViewer(IResource resource) {
		SynchronizeModelElement node = getModelObject(resource);
		if (node == null) return;
		if (isConflicting(node)) {
			setParentConflict(node, false);
		}
		clearModelObjects(node);
		if (canUpdateViewer()) {
			AbstractTreeViewer tree = getTreeViewer();
			tree.remove(node);
		}
	}

	protected void addToViewer(SynchronizeModelElement node) {
		associateDiffNode(node);
		node.addPropertyChangeListener(listener);
		if (isConflicting(node)) {
			setParentConflict(node, true);
		}
		if (canUpdateViewer()) {
			AbstractTreeViewer tree = getTreeViewer();
			tree.add(node.getParent(), node);
		}
	}

	protected void addResources(IResource[] added) {
		for (int i = 0; i < added.length; i++) {
			IResource resource = added[i];
			SynchronizeModelElement node = getModelObject(resource);
			if (node != null) {
				// Somehow the node exists. Remove it and read it to ensure
				// what is shown matches the contents of the sync set
				removeFromViewer(resource);
			}
			// Build the sub-tree rooted at this node
			SynchronizeModelElement parent = getModelObject(resource.getParent());
			if (parent != null) {
				node = createModelObject(parent, resource);
				buildModelObjects(node);
			}
		}
	}
	
	/**
	 * @param tree
	 * @return
	 */
	private boolean canUpdateViewer() {
		return refreshViewer && getTreeViewer() != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.ISyncInfoSetChangeListener#syncInfoSetReset(org.eclipse.team.core.subscribers.SyncInfoSet, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void syncInfoSetReset(SyncInfoSet set, IProgressMonitor monitor) {
		reset();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.ISyncInfoSetChangeListener#syncInfoSetError(org.eclipse.team.core.subscribers.SyncInfoSet, org.eclipse.team.core.ITeamStatus[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void syncInfoSetErrors(SyncInfoSet set, ITeamStatus[] errors, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.SynchronizeModelProvider#getInput()
	 */
	public SynchronizeModelElement getInput() {
		return getRoot();
	}
	
		/**
	 * Update the label of the given diff node. Diff nodes
	 * are accumulated and updated in a single call.
	 * @param diffNode the diff node to be updated
	 */
	protected void updateLabel(SynchronizeModelElement diffNode) {
		pendingLabelUpdates.add(diffNode);
	}

	/**
	 * Forces the viewer to update the labels for parents whose children have
	 * changed during this round of sync set changes.
	 */
	protected void firePendingLabelUpdates() {
		try {
			if (canUpdateViewer()) {
				AbstractTreeViewer tree = getTreeViewer();
				tree.update(pendingLabelUpdates.toArray(new Object[pendingLabelUpdates.size()]), null);
			}
		} finally {
			pendingLabelUpdates.clear();
		}
	}

	protected void setParentConflict(SynchronizeModelElement diffNode, boolean value) {
		diffNode.setPropertyToRoot(SynchronizeModelElement.PROPAGATED_CONFLICT_PROPERTY, value);
		updateParentLabels(diffNode);
	}

	private void updateParentLabels(SynchronizeModelElement diffNode) {
		updateLabel(diffNode);
		while (diffNode.getParent() != null) {
			diffNode = (SynchronizeModelElement)diffNode.getParent();
			updateLabel(diffNode);
		}
	}
}
