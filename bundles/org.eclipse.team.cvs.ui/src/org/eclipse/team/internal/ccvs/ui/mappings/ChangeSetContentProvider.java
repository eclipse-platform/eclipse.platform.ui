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
package org.eclipse.team.internal.ccvs.ui.mappings;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.*;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.core.mapping.IResourceDiffTree;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.internal.ccvs.core.mapping.ChangeSetModelProvider;
import org.eclipse.team.internal.core.subscribers.*;
import org.eclipse.team.internal.core.subscribers.BatchedChangeSetCollector.CollectorChangeEvent;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.mapping.ResourceModelContentProvider;
import org.eclipse.team.internal.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.internal.ui.synchronize.IChangeSetProvider;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;

public class ChangeSetContentProvider extends ResourceModelContentProvider implements ITreePathContentProvider {

	private final class CollectorListener implements IChangeSetChangeListener, BatchedChangeSetCollector.IChangeSetCollectorChangeListener {
		/* (non-Javadoc)
		 * @see org.eclipse.team.internal.core.subscribers.IChangeSetChangeListener#setAdded(org.eclipse.team.internal.core.subscribers.ChangeSet)
		 */
		public void setAdded(final ChangeSet set) {
			// We only react here for active change sets.
			// Checked-in change set changes are batched
			if (set instanceof ActiveChangeSet) {
				if (isVisible(set)) {
					Utils.syncExec(new Runnable() {
						public void run() {
							Object input = getViewer().getInput();
							((AbstractTreeViewer)getViewer()).add(input, set);
						}
					}, (StructuredViewer)getViewer());
				}
				handleSetAddition(set);
			}
		}

		private void handleSetAddition(final ChangeSet set) {
			IResource[] resources = set.getResources();
			try {
				getTheRest().beginInput();
				for (int i = 0; i < resources.length; i++) {
					IResource resource = resources[i];
					getTheRest().remove(resource);
				}
			} finally {
				getTheRest().endInput(null);
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.team.internal.core.subscribers.IChangeSetChangeListener#defaultSetChanged(org.eclipse.team.internal.core.subscribers.ChangeSet, org.eclipse.team.internal.core.subscribers.ChangeSet)
		 */
		public void defaultSetChanged(final ChangeSet previousDefault, final ChangeSet set) {
			if (isVisible(set) || isVisible(previousDefault)) {
				Utils.asyncExec(new Runnable() {
					public void run() {
						((AbstractTreeViewer)getViewer()).update(new Object[] {previousDefault, set}, null);
					}
				}, (StructuredViewer)getViewer());
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.team.internal.core.subscribers.IChangeSetChangeListener#setRemoved(org.eclipse.team.internal.core.subscribers.ChangeSet)
		 */
		public void setRemoved(final ChangeSet set) {
			// We only react here for active change sets.
			// Checked-in change set changes are batched
			if (set instanceof ActiveChangeSet) {
				if (isVisible(set)) {
					Utils.syncExec(new Runnable() {
						public void run() {
							((AbstractTreeViewer)getViewer()).remove(TreePath.EMPTY.createChildPath(set));
						}
					}, (StructuredViewer)getViewer());
				}
				handleSetRemoval(set);
			}
		}

		private void handleSetRemoval(final ChangeSet set) {
			IResource[] resources = set.getResources();
			try {
				getTheRest().beginInput();
				for (int i = 0; i < resources.length; i++) {
					IResource resource = resources[i];
					IDiff diff = getContext().getDiffTree().getDiff(resource);
					if (diff != null && !isContainedInSet(diff))
						getTheRest().add(diff);
				}
			} finally {
				getTheRest().endInput(null);
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.team.internal.core.subscribers.IChangeSetChangeListener#nameChanged(org.eclipse.team.internal.core.subscribers.ChangeSet)
		 */
		public void nameChanged(final ChangeSet set) {
			if (isVisible(set)) {
				Utils.asyncExec(new Runnable() {
					public void run() {
						((AbstractTreeViewer)getViewer()).update(set, null);
					}
				}, (StructuredViewer)getViewer());
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.team.internal.core.subscribers.IChangeSetChangeListener#resourcesChanged(org.eclipse.team.internal.core.subscribers.ChangeSet, org.eclipse.core.runtime.IPath[])
		 */
		public void resourcesChanged(final ChangeSet set, final IPath[] paths) {
			// We only react here for active change sets.
			// Checked-in change set changes are batched
			if (set instanceof ActiveChangeSet) {
				if (isVisible(set)) {
					Utils.syncExec(new Runnable() {
						public void run() {
							((AbstractTreeViewer)getViewer()).refresh(set, true);
						}
					}, (StructuredViewer)getViewer());
				}
				handleSetChange(set, paths);
			}
		}

		private void handleSetChange(final ChangeSet set, final IPath[] paths) {
			try {
				getTheRest().beginInput();
			    for (int i = 0; i < paths.length; i++) {
					IPath path = paths[i];
			        boolean isContained = ((DiffChangeSet)set).contains(path);
					if (isContained) {
						IDiff diff = ((DiffChangeSet)set).getDiffTree().getDiff(path);
						if (diff != null) {
							getTheRest().remove(ResourceDiffTree.getResourceFor(diff));
						}
					} else {
			            IDiff diff = getContext().getDiffTree().getDiff(path);
			            if (diff != null && !isContainedInSet(diff)) {
			                getTheRest().add(diff);
			            }
			        }   
			    }
			} finally {
				getTheRest().endInput(null);
			}
		}

		public void changeSetChanges(final CollectorChangeEvent event, IProgressMonitor monitor) {
			ChangeSet[] addedSets = event.getAddedSets();
			final ChangeSet[] visibleAddedSets = getVisibleSets(addedSets);
			ChangeSet[] removedSets = event.getRemovedSets();
			final ChangeSet[] visibleRemovedSets = getVisibleSets(removedSets);
			ChangeSet[] changedSets = event.getChangedSets();
			final ChangeSet[] visibleChangedSets = getVisibleSets(changedSets);
			if (visibleAddedSets.length > 0 || visibleRemovedSets.length > 0 || visibleChangedSets.length > 0) {
				Utils.syncExec(new Runnable() {
					public void run() {
						if (visibleAddedSets.length > 0) {
							Object input = getViewer().getInput();
							((AbstractTreeViewer)getViewer()).add(input, visibleAddedSets);
						}
						if (visibleRemovedSets.length > 0)
							((AbstractTreeViewer)getViewer()).remove(visibleRemovedSets);
						if (visibleChangedSets.length > 0) {
							((AbstractTreeViewer)getViewer()).refresh(visibleChangedSets, true);
						}
					}
				}, (StructuredViewer)getViewer());
			}
			try {
				getTheRest().beginInput();
				for (int i = 0; i < addedSets.length; i++) {
					ChangeSet set = addedSets[i];
					handleSetAddition(set);
				}
				for (int i = 0; i < removedSets.length; i++) {
					ChangeSet set = removedSets[i];
					handleSetRemoval(set);
				}
				for (int i = 0; i < visibleChangedSets.length; i++) {
					ChangeSet set = visibleChangedSets[i];
					IPath[] paths = event.getChangesFor(set);
					if (event.getSource().contains(set)) {
						handleSetChange(set, paths);
					} else {
						try {
							getTheRest().beginInput();
							for (int j = 0; j < paths.length; j++) {
								IPath path = paths[j];
								IDiff diff = getContext().getDiffTree().getDiff(path);
								if (diff != null && !isContainedInSet(diff))
									getTheRest().add(diff);
							}
						} finally {
							getTheRest().endInput(null);
						}
					}
				}
			} finally {
				getTheRest().endInput(monitor);
			}
		}

		private ChangeSet[] getVisibleSets(ChangeSet[] sets) {
			List result = new ArrayList(sets.length);
			for (int i = 0; i < sets.length; i++) {
				ChangeSet set = sets[i];
				if (isVisible(set)) {
					result.add(set);
				}
			}
			return (ChangeSet[]) result.toArray(new ChangeSet[result.size()]);
		}
	}

	private ResourceDiffTree theRest;
	
	/*
	 * Listener that reacts to changes made to the active change set collector
	 */
	private IChangeSetChangeListener collectorListener = new CollectorListener();
	
	private IDiffChangeListener diffTreeListener = new IDiffChangeListener() {
	
		/* (non-Javadoc)
		 * @see org.eclipse.team.core.diff.IDiffChangeListener#propertyChanged(org.eclipse.team.core.diff.IDiffTree, int, org.eclipse.core.runtime.IPath[])
		 */
		public void propertyChanged(IDiffTree tree, int property, IPath[] paths) {
			// Ignore
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.team.core.diff.IDiffChangeListener#diffsChanged(org.eclipse.team.core.diff.IDiffChangeEvent, org.eclipse.core.runtime.IProgressMonitor)
		 */
		public void diffsChanged(IDiffChangeEvent event, IProgressMonitor monitor) {
			Object input = getViewer().getInput();
			if (input instanceof ChangeSetModelProvider && event.getTree() == theRest) {
				Utils.asyncExec(new Runnable() {
					public void run() {
						// TODO: Need to be a bit more precise
						((AbstractTreeViewer)getViewer()).refresh();
					}
				}, (StructuredViewer)getViewer());
			}
		}
	
	};
	private CheckedInChangeSetCollector checkedInCollector;
	private boolean collectorInitialized;
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.ResourceModelContentProvider#getModelProviderId()
	 */
	protected String getModelProviderId() {
		return ChangeSetModelProvider.ID;
	}
	
	protected boolean isVisible(ChangeSet set) {
		final Object input = getViewer().getInput();
		if (input instanceof ChangeSetModelProvider) {
			if (set instanceof ActiveChangeSet) {
				// TODO: may need to be more precise that this
				return getConfiguration().getMode() != ISynchronizePageConfiguration.INCOMING_MODE;
			}
			if (set instanceof DiffChangeSet) {
				// TODO: may need to be more precise that this
				return getConfiguration().getMode() != ISynchronizePageConfiguration.OUTGOING_MODE;
			}
		}
		return false;
	}

	private boolean isEnabled() {
		final Object input = getViewer().getInput();
		return (input instanceof ChangeSetModelProvider);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.ResourceModelContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {
		if (parent instanceof ISynchronizationContext) {
			// Do not show change sets when all models are visible because
			// model providers that override the resource content may cause
			// problems for the change set content provider
			return new Object[0];
		}
		if (parent == getModelProvider()) {
			return getRootElements();
		}
		return super.getElements(parent);
	}

	private Object[] getRootElements() {
		if (!collectorInitialized) {
			initializeCheckedInChangeSetCollector(getChangeSetCapability());
			collectorInitialized = true;
		}
		List result = new ArrayList();
		ChangeSet[] sets = getAllSets();
		for (int i = 0; i < sets.length; i++) {
			ChangeSet set = sets[i];
			if (hasChildren(TreePath.EMPTY.createChildPath(set)))
				result.add(set);
		}
		// Include resources that are not in a set
		ResourceDiffTree tree = getTheRest();
		IPath[] otherRoots = tree.getChildren(ResourcesPlugin.getWorkspace().getRoot().getFullPath());
		for (int i = 0; i < otherRoots.length; i++) {
			IPath path = otherRoots[i];
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.lastSegment());
			if (project.isAccessible() && hasChildren(TreePath.EMPTY.createChildPath(project)))
				result.add(project);
		}
		return result.toArray();
	}

	private synchronized ResourceDiffTree getTheRest() {
		if (theRest == null) {
			theRest = new ResourceDiffTree();
			theRest.addDiffChangeListener(diffTreeListener);
			IResourceDiffTree allChanges = getContext().getDiffTree();
			try {
				theRest.beginInput();
				allChanges.accept(ResourcesPlugin.getWorkspace().getRoot().getFullPath(), new IDiffVisitor() {
					public boolean visit(IDiff diff) {
						if (!isContainedInSet(diff))
							theRest.add(diff);
						return true;
					}
				}, IResource.DEPTH_INFINITE);
			} finally {
				theRest.endInput(null);
			}
		}
		return theRest;
	}

	protected boolean isContainedInSet(IDiff diff) {
		ChangeSet[] sets = getAllSets();
		for (int i = 0; i < sets.length; i++) {
			ChangeSet set = sets[i];
			if (set.contains(ResourceDiffTree.getResourceFor(diff))) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.ResourceModelContentProvider#getTraversals(org.eclipse.team.core.mapping.ISynchronizationContext, java.lang.Object)
	 */
	protected ResourceTraversal[] getTraversals(
			ISynchronizationContext context, Object object) {
		if (object instanceof ChangeSet) {
			ChangeSet set = (ChangeSet) object;
			IResource[] resources = set.getResources();
			return new ResourceTraversal[] { new ResourceTraversal(resources, IResource.DEPTH_ZERO, IResource.NONE) };
		}
		return super.getTraversals(context, object);
	}

	public Object[] getChildren(TreePath parentPath) {
		if (!isEnabled())
			return new Object[0];
		if (parentPath.getSegmentCount() == 0)
			return getRootElements();
		Object first = parentPath.getFirstSegment();
		IResourceDiffTree diffTree;
		Object parent = parentPath.getLastSegment();
		if (first instanceof DiffChangeSet) {
			DiffChangeSet set = (DiffChangeSet) first;
			diffTree = set.getDiffTree();
			if (parent instanceof DiffChangeSet) {
				parent = getModelRoot();
			}
		} else {
			diffTree = getTheRest();
			if (parent instanceof ModelProvider) {
				parent = getModelRoot();
			}
		}
		Object[] children = getChildren(parent);
		Set result = new HashSet();
		for (int i = 0; i < children.length; i++) {
			Object child = children[i];
			if (isVisible(child, diffTree)) {
				result.add(child);
			}
		}
		return result.toArray();
	}

	private boolean isVisible(Object object, IResourceDiffTree tree) {
		//TODO: need to match diff direction with mode
		if (object instanceof IResource) {
			IResource resource = (IResource) object;
			if (tree.getDiff(resource) != null)
				return true;
			int depth = getTraversalCalculator().getLayoutDepth(resource);
			return tree.getDiffs(resource, depth).length > 0;
		}
		return false;
	}

	public boolean hasChildren(TreePath path) {
		return getChildren(path).length > 0;
	}

	public TreePath[] getParents(Object element) {
		if (element instanceof ChangeSet) {
			return new TreePath[] { TreePath.EMPTY };
		}
		if (element instanceof IResource) {
			IResource resource = (IResource) element;
			DiffChangeSet[] sets = getSetsContaining(resource);
			if (sets.length > 0) {
				List result = new ArrayList();
				for (int i = 0; i < sets.length; i++) {
					DiffChangeSet set = sets[i];
					TreePath path = getPathForElement(set.getDiffTree(), resource);
					if (path != null)
						result.add(path);
				}
				return (TreePath[]) result.toArray(new TreePath[result.size()]);
			} else {
				TreePath path = getPathForElement(getTheRest(), resource);
				if (path != null)
					return new TreePath[] { path };
			}
		}
		
		return new TreePath[0];
	}

	private DiffChangeSet[] getSetsContaining(IResource resource) {
		List result = new ArrayList();
		DiffChangeSet[] allSets = getAllSets();
		for (int i = 0; i < allSets.length; i++) {
			DiffChangeSet set = allSets[i];
			if (isVisible(resource, set.getDiffTree())) {
				result.add(set);
			}
		}
		return (DiffChangeSet[]) result.toArray(new DiffChangeSet[result.size()]);
	}

	private DiffChangeSet[] getAllSets() {
		List result = new ArrayList();
		ChangeSetCapability csc = getChangeSetCapability();
		if (csc.supportsActiveChangeSets()) {
			SubscriberChangeSetCollector collector = csc.getActiveChangeSetManager();
			ChangeSet[] sets = collector.getSets();	
			for (int i = 0; i < sets.length; i++) {
				ChangeSet set = sets[i];
				result.add(set);
			}
		}
		if (checkedInCollector != null) {
			ChangeSet[] sets = checkedInCollector.getSets();	
			for (int i = 0; i < sets.length; i++) {
				ChangeSet set = sets[i];
				result.add(set);
			}
		}
		return (DiffChangeSet[]) result.toArray(new DiffChangeSet[result.size()]);
	}

	private TreePath getPathForElement(IResourceDiffTree tree, IResource resource) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void init(ICommonContentExtensionSite site) {
		super.init(site);
		ChangeSetCapability csc = getChangeSetCapability();
		if (csc.supportsActiveChangeSets()) {
			SubscriberChangeSetCollector collector = csc.getActiveChangeSetManager();
			collector.addListener(collectorListener);
		}
	}

	private void initializeCheckedInChangeSetCollector(ChangeSetCapability csc) {
		if (csc.supportsCheckedInChangeSets()) {
			checkedInCollector = ((ModelParticipantChangeSetCapability)csc).createCheckedInChangeSetCollector(getConfiguration());
			checkedInCollector.addListener(collectorListener);
			checkedInCollector.add(((ResourceDiffTree)getContext().getDiffTree()).getDiffs());
		}
	}
	
	public void dispose() {
		ChangeSetCapability csc = getChangeSetCapability();
		if (csc.supportsActiveChangeSets()) {
			csc.getActiveChangeSetManager().removeListener(collectorListener);
		}
		if (checkedInCollector != null) {
			checkedInCollector.removeListener(collectorListener);
			checkedInCollector.dispose();
		}
		if (theRest != null) {
			theRest.removeDiffChangeListener(diffTreeListener);
		}
		super.dispose();
	}
	
	public boolean isVisible(IDiff diff) {
		return super.isVisible(diff);
	}

	public IResourceDiffTree getDiffTree(TreePath path) {
		if (path.getSegmentCount() > 0) {
			Object first = path.getFirstSegment();
			if (first instanceof DiffChangeSet) {
				DiffChangeSet set = (DiffChangeSet) first;
				return set.getDiffTree();
			}
		}
		return getTheRest();
	}
	
	public void diffsChanged(IDiffChangeEvent event, IProgressMonitor monitor) {
		// Override inherited method to reconcile sub-trees
		IPath[] removed = event.getRemovals();
		IDiff[] added = event.getAdditions();
		IDiff[] changed = event.getChanges();
		// Only adjust the set of the rest. The others will be handled by the collectors
		try {
			getTheRest().beginInput();
			for (int i = 0; i < removed.length; i++) {
				IPath path = removed[i];
				getTheRest().remove(path);
			}
			for (int i = 0; i < added.length; i++) {
				IDiff diff = added[i];
				// Only add the diff if it is not already in another set
				if (!isContainedInSet(diff)) {
					getTheRest().add(diff);
				}
			}
			for (int i = 0; i < changed.length; i++) {
				IDiff diff = changed[i];
				// Only add the diff if it is already contained in the free set
				if (getTheRest().getDiff(diff.getPath()) != null) {
					getTheRest().add(diff);
				}
			}
		} finally {
			getTheRest().endInput(monitor);
		}
		if (checkedInCollector != null)
			checkedInCollector.handleChange(event);
	}
	
	protected void updateLabels(ISynchronizationContext context, IPath[] paths) {
		super.updateLabels(context, paths);
		ChangeSet[] sets = getSetsShowingPropogatedStateFrom(paths);
		if (sets.length > 0)
			((AbstractTreeViewer)getViewer()).update(sets, null);
	}
	
	
    private ChangeSet[] getSetsShowingPropogatedStateFrom(IPath[] paths) {
		Set result = new HashSet();
		for (int i = 0; i < paths.length; i++) {
			IPath path = paths[i];
			ChangeSet[] sets = getSetsShowingPropogatedStateFrom(path);
			for (int j = 0; j < sets.length; j++) {
				ChangeSet set = sets[j];
				result.add(set);
			}
		}
		return (ChangeSet[]) result.toArray(new ChangeSet[result.size()]);
	}
    
	protected DiffChangeSet[] getSetsShowingPropogatedStateFrom(IPath path) {
		List result = new ArrayList();
		DiffChangeSet[] allSets = getAllSets();
		for (int i = 0; i < allSets.length; i++) {
			DiffChangeSet set = allSets[i];
			if (set.getDiffTree().getDiff(path) != null || set.getDiffTree().getChildren(path).length > 0) {
				result.add(set);
			}
		}
		return (DiffChangeSet[]) result.toArray(new DiffChangeSet[result.size()]);
	}

	public ChangeSetCapability getChangeSetCapability() {
        ISynchronizeParticipant participant = getConfiguration().getParticipant();
        if (participant instanceof IChangeSetProvider) {
            IChangeSetProvider provider = (IChangeSetProvider) participant;
            return provider.getChangeSetCapability();
        }
        return null;
    }

}
