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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.ITeamStatus;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.progress.WorkbenchJob;

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
public abstract class SynchronizeModelProvider implements ISyncInfoSetChangeListener, ISynchronizeModelProvider, IResourceChangeListener {

	private static boolean DEBUG = false;
	
	protected Map resourceMap = Collections.synchronizedMap(new HashMap());
	
	protected SynchronizeModelElement root;
	
	// The viewer this input is being displayed in
	private StructuredViewer viewer;
	
	private Set pendingLabelUpdates = Collections.synchronizedSet(new HashSet());
	
	private LabelUpdateJob labelUpdater = new LabelUpdateJob();
	
	private ISynchronizePageConfiguration configuration;
	
	private boolean disposed = false;
	
	private IPropertyChangeListener listener = new IPropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent event) {
				if (event.getProperty() == SynchronizeModelElement.BUSY_PROPERTY) {
					labelUpdater.add(event.getSource(), ((Boolean)event.getNewValue()).booleanValue());
				}
			}
		};
	
	class LabelUpdateJob extends WorkbenchJob {
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
				StructuredViewer tree = getViewer();
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
	
	private SyncInfoSet set;
	
	/**
	 * Create an input based on the provide sync set. The input is not
	 * initialized until <code>prepareInput</code> is called.
	 * @param set
	 *            the sync set used as the basis for the model created by this
	 *            input.
	 */
	public SynchronizeModelProvider(ISynchronizePageConfiguration configuration, SyncInfoSet set) {
		this(new UnchangedResourceModelElement(null, ResourcesPlugin.getWorkspace().getRoot()) {
			/* 
			 * Override to ensure that the diff viewer will appear in CompareEditorInputs
			 */
			public boolean hasChildren() {
				return true;
			}
		}, configuration, set);
	}

	public SynchronizeModelProvider(SynchronizeModelElement parent, ISynchronizePageConfiguration configuration, SyncInfoSet set) {
		Assert.isNotNull(set);
		Assert.isNotNull(parent);
		this.root = parent;
		this.set = set;
		this.configuration = configuration;
	}
	
	/**
	 * Return the set that contains the elements this provider is using as
	 * a basis for creating a presentation model. This cannot be null.
	 * 
	 * @return the set that contains the elements this provider is
	 * using as a basis for creating a presentation model.
	 */
	public SyncInfoSet getSyncInfoSet() {
		return set;
	}
	
	/**
	 * Return the page configuration for this provider.
	 * 
	 * @return the page configuration for this provider.
	 */
	public ISynchronizePageConfiguration getConfiguration() {
		return configuration;
	}
	
	/**
	 * Return the <code>AbstractTreeViewer</code> asociated with this content
	 * provider or <code>null</code> if the viewer is not of the proper type.
	 * @return
	 */
	public StructuredViewer getViewer() {
		return viewer;
	}

	public void setViewer(StructuredViewer viewer) {
		Assert.isTrue(viewer instanceof AbstractTreeViewer);
		this.viewer = (AbstractTreeViewer) viewer;
	}

		/**
	 * Builds the viewer model based on the contents of the sync set.
	 */
	public ISynchronizeModelElement prepareInput(IProgressMonitor monitor) {
		// Connect to the sync set which will register us as a listener and give us a reset event
		// in a background thread
		getSyncInfoSet().connect(this, monitor);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		return getModelRoot();
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
		getSyncInfoSet().removeSyncSetChangedListener(this);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		this.disposed = true;
	}
	
	/**
	 * Returns the input created by this provider or <code>null</code> if 
	 * {@link #prepareInput(IProgressMonitor)} hasn't been called on this object yet.
	 * 
	 * @return the input created by this provider.
	 */
	public ISynchronizeModelElement getModelRoot() {
		return root;
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

	public void syncInfoChanged(final ISyncInfoSetChangeEvent event, IProgressMonitor monitor) {
		synchronized(this) {
			if (! (event instanceof ISyncInfoTreeChangeEvent)) {
				reset();
			} else {
				final Control ctrl = getViewer().getControl();
				if (ctrl != null && !ctrl.isDisposed()) {
					ctrl.getDisplay().syncExec(new Runnable() {
						public void run() {
							if (!ctrl.isDisposed()) {
								BusyIndicator.showWhile(ctrl.getDisplay(), new Runnable() {
									public void run() {
										handleChanges((ISyncInfoTreeChangeEvent)event);
										ISynchronizeModelElement root = getModelRoot();
										if(root instanceof SynchronizeModelElement)
											((SynchronizeModelElement)root).fireChanges();
									}
								});
							}
						}
					});
				}
			}
		}
	}

	/**
	 * For each node create children based on the contents of
	 * @param node
	 * @return
	 */
	protected abstract IDiffElement[] buildModelObjects(ISynchronizeModelElement node);

	protected abstract void doAdd(ISynchronizeModelElement parent, ISynchronizeModelElement element);
	
	protected abstract void doRemove(ISynchronizeModelElement element);
	
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
	 * Handle the changes made to the viewer's <code>SyncInfoSet</code>.
	 * This method delegates the changes to the three methods <code>handleResourceChanges(ISyncInfoSetChangeEvent)</code>,
	 * <code>handleResourceRemovals(ISyncInfoSetChangeEvent)</code> and
	 * <code>handleResourceAdditions(ISyncInfoSetChangeEvent)</code>.
	 * @param event
	 *            the event containing the changed resourcses.
	 */
	protected final void handleChanges(ISyncInfoTreeChangeEvent event) {
		StructuredViewer viewer = getViewer();
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
	protected abstract void handleResourceAdditions(ISyncInfoTreeChangeEvent event);

	/**
	 * Update the viewer for the sync set changes in the provided event. This
	 * method is invoked by <code>handleChanges(ISyncInfoSetChangeEvent)</code>.
	 * Subclasses may override.
	 * @param event
	 */
	protected abstract void handleResourceChanges(ISyncInfoTreeChangeEvent event);

	protected boolean isConflicting(ISynchronizeModelElement diffNode) {
		return (diffNode.getKind() & SyncInfo.DIRECTION_MASK) == SyncInfo.CONFLICTING;
	}

	/**
	 * Update the viewer for the sync set removals in the provided event. This
	 * method is invoked by <code>handleChanges(ISyncInfoSetChangeEvent)</code>.
	 * Subclasses may override.
	 * @param event
	 */
	protected abstract void handleResourceRemovals(ISyncInfoTreeChangeEvent event);

	protected void reset() {
		// save expansion state
		if(! resourceMap.isEmpty()) {
			saveViewerState();
		}
		
		// Clear existing model, but keep the root node
		resourceMap.clear();
		pendingLabelUpdates.clear();
		clearModelObjects(getModelRoot());
		// remove all from tree viewer
		IDiffElement[] elements = getModelRoot().getChildren();
		for (int i = 0; i < elements.length; i++) {
			doRemove((ISynchronizeModelElement)elements[i]);
		}
		
		// Rebuild the model
		associateDiffNode(getModelRoot());
		buildModelObjects(getModelRoot());
		
		// Notify listeners that model has changed
		ISynchronizeModelElement root = getModelRoot();
		if(root instanceof SynchronizeModelElement) {
			((SynchronizeModelElement)root).fireChanges();
		}
		TeamUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				StructuredViewer viewer = getViewer();
				if (viewer != null && !viewer.getControl().isDisposed()) {
					viewer.refresh();
					//	restore expansion state
					restoreViewerState();
				}
			}
		});
	}

	/**
	 * Remove any traces of the model element and any of it's descendants in the
	 * hiearchy defined by the content provider from the content provider and
	 * the viewer it is associated with.
	 * @param node the model element to remove
	 */
	protected void removeFromViewer(ISynchronizeModelElement node) {
		propogateConflictState(node, true /* clear the conflict */);
		clearModelObjects(node);
		if (canUpdateViewer()) {
			doRemove(node);
		}
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

	/**
	 * Clear the model objects from the diff tree, cleaning up any cached state
	 * (such as resource to model object map). This method recurses deeply on
	 * the tree to allow the cleanup of any cached state for the children as
	 * well.
	 * @param node
	 *            the root node
	 */
	protected void clearModelObjects(ISynchronizeModelElement node) {
		IDiffElement[] children = node.getChildren();
		for (int i = 0; i < children.length; i++) {
			IDiffElement element = children[i];
			if (element instanceof ISynchronizeModelElement) {
				clearModelObjects((ISynchronizeModelElement) element);
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
	
	protected void addToViewer(ISynchronizeModelElement node) {
		associateDiffNode(node);
		node.addPropertyChangeListener(listener);
		propogateConflictState(node, false);
		// Set the marker property on this node.
		// There is no need to propogate this to the parents 
		// as they will be displaying the proper marker already
		String property = calculateProblemMarker(node);
		if (property != null) {
			node.setProperty(property, true);
		}
		if (canUpdateViewer()) {
			doAdd((SynchronizeModelElement)node.getParent(), node);
		}
	}

	/**
	 * Calculate the problem marker that should be shown on the given 
	 * element.
	 * @param element a synchronize model element
	 * @return the marker property that should be displayed on the element
	 * or <code>null</code> if no marker should be displayed
	 */
	private String calculateProblemMarker(ISynchronizeModelElement element) {
		IResource resource = element.getResource();
		String property = null;
		if (resource != null && resource.exists()) {
			try {
				IMarker[] markers = resource.findMarkers(IMarker.PROBLEM, true, getLogicalModelDepth(resource));
				for (int i = 0; i < markers.length; i++) {
					IMarker marker = markers[i];
					try {
						Integer severity = (Integer) marker.getAttribute(IMarker.SEVERITY);
						if (severity != null) {
							if (severity.intValue() == IMarker.SEVERITY_ERROR) {
								property = ISynchronizeModelElement.PROPAGATED_ERROR_MARKER_PROPERTY;
								break;
							} else if (severity.intValue() == IMarker.SEVERITY_WARNING) {
								property = ISynchronizeModelElement.PROPAGATED_WARNING_MARKER_PROPERTY;
								// Keep going because there may be errors on other resources
							}
						}
					} catch (CoreException e) {
						if (!resource.exists()) {
							// The resource was deleted concurrently. Forget any previously found property
							property = null;
							break;
						}
						// If the marker exists, log the exception and continue.
						// Otherwise, just ignore the exception and keep going
						if (marker.exists()) {
							TeamPlugin.log(e);
						}
					}
				}
			} catch (CoreException e) {
				// If the resource exists (is accessible), log the exception and continue.
				// Otherwise, just ignore the exception
				if (resource.isAccessible() 
						&& e.getStatus().getCode() != IResourceStatus.RESOURCE_NOT_FOUND
						&& e.getStatus().getCode() != IResourceStatus.PROJECT_NOT_OPEN) {
					TeamPlugin.log(e);
				}
			}
		}
		return property;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener#syncInfoSetReset(org.eclipse.team.core.synchronize.SyncInfoSet, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void syncInfoSetReset(SyncInfoSet set, IProgressMonitor monitor) {
		if(disposed) {
			set.removeSyncSetChangedListener(this);
		} else {
			reset();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener#syncInfoSetErrors(org.eclipse.team.core.synchronize.SyncInfoSet, org.eclipse.team.core.ITeamStatus[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void syncInfoSetErrors(SyncInfoSet set, ITeamStatus[] errors, IProgressMonitor monitor) {
		// When errors occur we currently don't process them. It may be possible to decorate
		// elements in the model with errors, but currently we prefer to let ignore and except
		// another listener to display them.
	}

	protected void saveViewerState() {
		//	save visible expanded elements and selection
		if (viewer != null && !viewer.getControl().isDisposed() && viewer instanceof AbstractTreeViewer) {
			final Object[][] expandedElements = new Object[1][1];
			final Object[][] selectedElements = new Object[1][1];
			viewer.getControl().getDisplay().syncExec(new Runnable() {
				public void run() {
					if (viewer != null && !viewer.getControl().isDisposed()) {
						expandedElements[0] = ((AbstractTreeViewer) viewer).getVisibleExpandedElements();
						selectedElements[0] = ((IStructuredSelection) viewer.getSelection()).toArray();
					}
				}
			});
			//
			// Save expansion
			//
			if (expandedElements[0].length > 0) {
				ISynchronizePageConfiguration config = getConfiguration();
				ArrayList savedExpansionState = new ArrayList();
				for (int i = 0; i < expandedElements[0].length; i++) {
					if (expandedElements[0][i] instanceof ISynchronizeModelElement) {
						IResource resource = ((ISynchronizeModelElement) expandedElements[0][i]).getResource();
						if(resource != null)
							savedExpansionState.add(resource.getFullPath().toString());
					}
				}
				config.setProperty(P_VIEWER_EXPANSION_STATE, savedExpansionState);
			}
			//
			// Save selection
			//
			if (selectedElements[0].length > 0) {
				ISynchronizePageConfiguration config = getConfiguration();
				ArrayList savedSelectedState = new ArrayList();
				for (int i = 0; i < selectedElements[0].length; i++) {
					if (selectedElements[0][i] instanceof ISynchronizeModelElement) {
						IResource resource = ((ISynchronizeModelElement) selectedElements[0][i]).getResource();
						if(resource != null)
							savedSelectedState.add(resource.getFullPath().toString());
					}
				}
				config.setProperty(P_VIEWER_SELECTION_STATE, savedSelectedState);
			}
		}
	}
	
	protected void restoreViewerState() {
		// restore expansion state and selection state
		if (viewer != null && !viewer.getControl().isDisposed() && viewer instanceof AbstractTreeViewer) {
			List savedExpansionState = (List)configuration.getProperty(P_VIEWER_EXPANSION_STATE);
			List savedSelectionState = (List)configuration.getProperty(P_VIEWER_SELECTION_STATE);
			IContainer container = ResourcesPlugin.getWorkspace().getRoot();
			final ArrayList expandedElements = new ArrayList();
			if (savedExpansionState != null) {
				for (Iterator it = savedExpansionState.iterator(); it.hasNext();) {
					String path = (String) it.next();
					IResource resource = container.findMember(path, true /* include phantoms */);
					ISynchronizeModelElement element = getModelObject(resource);
					if (element != null) {
						expandedElements.add(element);
					}
				}
			}
			final ArrayList selectedElements = new ArrayList();
			if (savedSelectionState != null) {
				for (Iterator it = savedSelectionState.iterator(); it.hasNext();) {
					String path = (String) it.next();
					IResource resource = container.findMember(path, true /* include phantoms */);
					ISynchronizeModelElement element = getModelObject(resource);
					if (element != null) {
						selectedElements.add(element);
					}
				}
			}
			asyncExec(new Runnable() {
				public void run() {
					((AbstractTreeViewer) viewer).setExpandedElements(expandedElements.toArray());
					viewer.setSelection(new StructuredSelection(selectedElements));
				}
			});
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.ISynchronizeModelProvider#saveState()
	 */
	public void saveState() {
		saveViewerState();
	}
	
	/**
	 * Update the label of the given diff node. Diff nodes
	 * are accumulated and updated in a single call.
	 * @param diffNode the diff node to be updated
	 */
	protected void queueForLabelUpdate(ISynchronizeModelElement diffNode) {
		pendingLabelUpdates.add(diffNode);
	}
	
	/*
	 * The viewer will only be updated if the viewer is not null, the control is not disposed, and
	 * this code is being run from the UI thread.
	 */
	private boolean canUpdateViewer() {
		StructuredViewer viewer = getViewer();
		if(viewer == null || viewer.getControl().isDisposed()) return false;
		Display display = viewer.getControl().getDisplay();
		if (display == null) return false;
		if (display.getThread() != Thread.currentThread ()) return false;
		return true;
	}
	
	/**
	 * Forces the viewer to update the labels for queued elemens
	 * whose label has changed during this round of changes. This method
	 * should only be invoked in the UI thread.
	 */
	protected void firePendingLabelUpdates() {
		if (!canUpdateViewer()) return;
		try {
			Object[] updates = pendingLabelUpdates.toArray(new Object[pendingLabelUpdates.size()]);
			updateLabels(updates);
		} finally {
			pendingLabelUpdates.clear();
		}
	}
	
	/*
	 * Forces the viewer to update the labels for the given elements
	 */
	private void updateLabels(Object[] elements) {
		if (canUpdateViewer()) {
			StructuredViewer tree = getViewer();	
			tree.update(elements, null);
		}
	}

	/**
	 * Method invoked when a sync element is added or removed or its state changes.
	 * This method can be invoked from the UI thread or a background thread.
	 * @param element synchronize element
	 * @param clear <code>true</code> if the conflict bit of the element was cleared 
	 * (i.e. the element has been deleted)
	 */
	protected void propogateConflictState(ISynchronizeModelElement element, boolean clear) {
		boolean isConflict = clear ? false : isConflicting(element);
		boolean wasConflict = element.getProperty(ISynchronizeModelElement.PROPAGATED_CONFLICT_PROPERTY);
		// Only propogate and update parent labels if the state of the element has changed
		if (isConflict != wasConflict) {
			element.setPropertyToRoot(ISynchronizeModelElement.PROPAGATED_CONFLICT_PROPERTY, isConflict);
			updateParentLabels(element);
		}
	}
	
	/**
	 * Calculate and propagate problem markers in the element model
	 * @param element the ssynchronize element
	 */
	private void propagateProblemMarkers(ISynchronizeModelElement element) {
		IResource resource = element.getResource();
		if (resource != null) {
			String property = calculateProblemMarker(element);
			// If it doesn't have a direct change, a parent might
			boolean recalculateParentDecorations = hadProblemProperty(element, property);
			if (recalculateParentDecorations) {
				ISynchronizeModelElement parent = (ISynchronizeModelElement) element.getParent();
				if (parent != null) {
					propagateProblemMarkers(parent);
				}
			}
		}
	}

	// none -> error
	// error -> none
	// none -> warning
	// warning -> none
	// warning -> error
	// error -> warning
	private boolean hadProblemProperty(ISynchronizeModelElement element, String property) {
		boolean hadError = element.getProperty(ISynchronizeModelElement.PROPAGATED_ERROR_MARKER_PROPERTY);
		boolean hadWarning = element.getProperty(ISynchronizeModelElement.PROPAGATED_WARNING_MARKER_PROPERTY);
		
		// Force recalculation of parents of phantom resources
		IResource resource = element.getResource();
		if(resource != null && resource.isPhantom()) {
			return true;
		}
		
		if(hadError) {
			if(! (property == ISynchronizeModelElement.PROPAGATED_ERROR_MARKER_PROPERTY)) {
				element.setPropertyToRoot(ISynchronizeModelElement.PROPAGATED_ERROR_MARKER_PROPERTY, false);
				if(property != null) {
					// error -> warning
					element.setPropertyToRoot(property, true);
				}
				// error -> none
				// recalculate parents
				return true;
			}	
			return false;
		} else if(hadWarning) {
			if(! (property == ISynchronizeModelElement.PROPAGATED_WARNING_MARKER_PROPERTY)) {
				element.setPropertyToRoot(ISynchronizeModelElement.PROPAGATED_WARNING_MARKER_PROPERTY, false);
				if(property != null) {
					// warning -> error
					element.setPropertyToRoot(property, true);
					return false;
				}
				// warning ->  none
				return true;
			}	
			return false;		
		} else {
			if(property == ISynchronizeModelElement.PROPAGATED_ERROR_MARKER_PROPERTY) {
				// none -> error
				element.setPropertyToRoot(property, true);
				return false;
			} else if(property == ISynchronizeModelElement.PROPAGATED_WARNING_MARKER_PROPERTY) {
				// none -> warning
				element.setPropertyToRoot(property, true);
				return true;
			}	
			return false;
		}
	}

	private void updateParentLabels(ISynchronizeModelElement diffNode) {
		queueForLabelUpdate(diffNode);
		while (diffNode.getParent() != null) {
			diffNode = (ISynchronizeModelElement)diffNode.getParent();
			queueForLabelUpdate(diffNode);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(final IResourceChangeEvent event) {
			String[] markerTypes = getMarkerTypes();
			Map changes = new HashMap();
			
			// Accumulate all distinct resources that have had problem marker
			// changes
			for (int idx = 0; idx < markerTypes.length; idx++) {
				IMarkerDelta[] markerDeltas = event.findMarkerDeltas(markerTypes[idx], true);
					for (int i = 0; i < markerDeltas.length; i++) {
						IMarkerDelta delta = markerDeltas[i];
						IResource resource = delta.getResource();
						if(! changes.containsKey(resource)) {								
							ISynchronizeModelElement element = getClosestExistingParent(delta.getResource());
							if(element != null) {
								changes.put(resource, element);
							}
						}
					}
				}
			
			if (!changes.isEmpty()) {
				startMarkerUpdateJob((ISynchronizeModelElement[]) changes.values().toArray(new ISynchronizeModelElement[changes.size()]));
		}
	}
	
	/**
	 * Calculate the properties for affected resources in our model and fire
	 * label changes for changed elements. This will happen in a background
	 * job.
	 * 
	 * @param changes the model elements that have changed and need
	 * their labels updated. Note that this will update the annotations on the
	 * label because the element will already have the correct image and text.
	 */
	private void startMarkerUpdateJob(final ISynchronizeModelElement[] changes) {
		Job job = new Job(Policy.bind("SynchronizeModelProvider.0")) { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				long start = System.currentTimeMillis();
				synchronized (this) {
					// Changes contains all elements that need their labels updated
					for (int i = 0; i < changes.length; i++) {
						ISynchronizeModelElement element = changes[i];
						propagateProblemMarkers(element);
						updateParentLabels(element);
					}
				}
				if (DEBUG) {
					long time = System.currentTimeMillis() - start;
					DateFormat TIME_FORMAT = new SimpleDateFormat("m:ss.SSS"); //$NON-NLS-1$
					String took = TIME_FORMAT.format(new Date(time));
					System.out.println(took + " for " + changes.length + " files"); //$NON-NLS-1$//$NON-NLS-2$
				}
				// Fire label changed
				asyncExec(new Runnable() {
					public void run() {
						firePendingLabelUpdates();
					}
				});
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	protected ISynchronizeModelElement getClosestExistingParent(IResource resource) {
		ISynchronizeModelElement element = getModelObject(resource);
		if(element == null) {
			do {
				resource = resource.getParent();
				element = getModelObject(resource);
			} while(element == null && resource != null);
		}
		return element;
	}
	
	protected int getLogicalModelDepth(IResource resource) {
		return IResource.DEPTH_INFINITE;
	}
	
	protected String[] getMarkerTypes() {
		return new String[] {IMarker.PROBLEM};
	}
	
	private void asyncExec(final Runnable r) {
		StructuredViewer v = getViewer();
		if(v == null) return;
		final Control ctrl = v.getControl();
		if (ctrl != null && !ctrl.isDisposed()) {
			ctrl.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (!ctrl.isDisposed()) {
						BusyIndicator.showWhile(ctrl.getDisplay(), r);
					}
				}
			});
		}
	}
}
