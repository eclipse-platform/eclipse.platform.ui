/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ILazyTreePathContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.progress.UIJob;

/**
 * Content provider for a virtual tree.
 * 
 * @since 3.3
 */
class TreeModelContentProvider extends ModelContentProvider implements ILazyTreePathContentProvider {
	
	protected static final String[] STATE_PROPERTIES = new String[]{IBasicPropertyConstants.P_TEXT, IBasicPropertyConstants.P_IMAGE};
	
	/**
	 * Map of parent paths to requests
	 */
	private Map fPendingChildRequests = new HashMap();
	
	/**
	 * Map of content adapters to requests
	 */
	private Map fPendingCountRequests = new HashMap();
	
	/**
	 * Map of content adapters to requests
	 */
	private Map fPendingHasChildrenRequests = new HashMap();	
	
	private Timer fTimer = new Timer();
	
	/**
	 * Re-filters any filtered children of the given parent element.
	 * 
	 * @param path parent element
	 */
	protected void refilterChildren(TreePath path) {
		if (getViewer() != null) {
			int[] filteredChildren = getFilteredChildren(path);
			if (filteredChildren != null) {
				for (int i = 0; i < filteredChildren.length; i++) {
					doUpdateElement(path, filteredChildren[i]);
				}
			}
		}
	}
	
	protected synchronized void doUpdateChildCount(TreePath path) {
		Object element = getElement(path);
		IElementContentProvider contentAdapter = getContentAdapter(element);
		if (contentAdapter != null) {
			ChildrenCountUpdate request = (ChildrenCountUpdate) fPendingCountRequests.get(contentAdapter);
			if (request != null) {
				if (request.coalesce(path)) {
					return;
				} else {
					request.start();
				}
			}
			final ChildrenCountUpdate newRequest = new ChildrenCountUpdate(this, contentAdapter);
			newRequest.coalesce(path);
			fPendingCountRequests.put(contentAdapter, newRequest);
			fTimer.schedule(new TimerTask() {
				public void run() {
					newRequest.start();
				}
			}, 10L);
		}
	}	
	
	protected synchronized void doUpdateElement(TreePath parentPath, int modelIndex) {
		ChildrenUpdate request = (ChildrenUpdate) fPendingChildRequests.get(parentPath);
		if (request != null) {
			if (request.coalesce(modelIndex)) {
				return;
			} else {
				request.start();
			}
		} 
		Object parent = getElement(parentPath);
		IElementContentProvider contentAdapter = getContentAdapter(parent);
		if (contentAdapter != null) {
			final ChildrenUpdate newRequest = new ChildrenUpdate(this, parentPath, modelIndex, contentAdapter);
			fPendingChildRequests.put(parentPath, newRequest);
			fTimer.schedule(new TimerTask() {
				public void run() {
					newRequest.start();
				}
			}, 10L);
		}			
	}	
	
	protected synchronized void doUpdateHasChildren(TreePath path) {
		Object element = getElement(path);
		IElementContentProvider contentAdapter = getContentAdapter(element);
		if (contentAdapter != null) {
			HasChildrenUpdate request = (HasChildrenUpdate) fPendingHasChildrenRequests.get(contentAdapter);
			if (request != null) {
				if (request.coalesce(path)) {
					return;
				} else {
					request.start();
				}
			}
			final HasChildrenUpdate newRequest = new HasChildrenUpdate(this, contentAdapter);
			newRequest.coalesce(path);
			fPendingHasChildrenRequests.put(contentAdapter, newRequest);
			fTimer.schedule(new TimerTask() {
				public void run() {
					newRequest.start();
				}
			}, 10L);
		}
	}		
	
	protected synchronized void childRequestStarted(IChildrenUpdate update) {
		fPendingChildRequests.remove(update.getParent());
	}
	
	protected synchronized void countRequestStarted(Object key) {
		fPendingCountRequests.remove(key);
	}
	
	protected synchronized void hasChildrenRequestStarted(Object key) {
		fPendingHasChildrenRequests.remove(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#getPresentationContext()
	 */
	protected IPresentationContext getPresentationContext() {
		return ((TreeModelViewer)getViewer()).getPresentationContext();
	}
	
	/**
	 * Returns the tree viewer this content provider is working for
	 * 
	 * @return tree viewer
	 */
	protected TreeViewer getTreeViewer() {
		return (TreeViewer)getViewer();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleAdd(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleAdd(IModelDelta delta) {
		doUpdateChildCount(getViewerTreePath(delta.getParentDelta()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleContent(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleContent(IModelDelta delta) {
		if (delta.getChildCount() == 0) {
			// if the delta is for the root, ensure the root still matches viewer input
			if (!delta.getElement().equals(getViewer().getInput())) {
				return;
			}
		}
		TreePath treePath = getViewerTreePath(delta);
		cancelSubtreeUpdates(treePath);
		getTreeViewer().refresh(getElement(treePath));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleExpand(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleExpand(IModelDelta delta) {
		// expand each parent, then this node
		IModelDelta parentDelta = delta.getParentDelta();
		if (parentDelta != null) {
			handleExpand(parentDelta);
			expand(delta);
		}
	}
	
	protected void expand(IModelDelta delta) {
		int childCount = delta.getChildCount();
		int modelIndex = delta.getIndex();
		TreeViewer treeViewer = getTreeViewer();
		if (modelIndex >= 0) {
			int viewIndex = modelToViewIndex(getViewerTreePath(delta.getParentDelta()), modelIndex);
			if (DEBUG_CONTENT_PROVIDER) {
				System.out.println("[expand] replace(" + delta.getParentDelta().getElement() + ", (model) " + modelIndex + " (view) " + viewIndex + ", " + delta.getElement()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			treeViewer.replace(delta.getParentDelta().getElement(), viewIndex, delta.getElement());
		}
		if (childCount > 0) {
			TreePath elementPath = getViewerTreePath(delta);
			int viewCount = modelToViewChildCount(elementPath, childCount);
			if (DEBUG_CONTENT_PROVIDER) {
				System.out.println("[expand] setChildCount(" + delta.getElement() + ", (model) " + childCount + " (view) " + viewCount); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			treeViewer.setChildCount(elementPath, viewCount);
			if (!treeViewer.getExpandedState(elementPath)) {
				treeViewer.expandToLevel(elementPath, 1);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleInsert(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleInsert(IModelDelta delta) {
		// TODO: filters
		getTreeViewer().insert(getViewerTreePath(delta.getParentDelta()), delta.getElement(), delta.getIndex());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleRemove(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleRemove(IModelDelta delta) {
		getTreeViewer().remove(getViewerTreePath(delta));
		// refresh the parent to properly update for non-visible/unmapped children
		getTreeViewer().refresh(delta.getParentDelta().getElement());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleReplace(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleReplace(IModelDelta delta) {
		getTreeViewer().replace(delta.getParentDelta().getElement(), delta.getIndex(), delta.getElement());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleSelect(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleSelect(IModelDelta delta) {
		int modelIndex = delta.getIndex();
		TreeViewer treeViewer = getTreeViewer();
		if (modelIndex >= 0) {
			IModelDelta parentDelta = delta.getParentDelta();
			TreePath parentPath = getViewerTreePath(parentDelta);
			int viewIndex = modelToViewIndex(parentPath, modelIndex);
			int modelCount = parentDelta.getChildCount();
			if (modelCount > 0) {
				int viewCount = modelToViewChildCount(parentPath, modelCount);
				if (DEBUG_CONTENT_PROVIDER) {
					System.out.println("[select] setChildCount(" + parentDelta.getElement() + ", (model) " + parentDelta.getChildCount() + " (view) " + viewCount ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				treeViewer.setChildCount(parentPath, viewCount);
			}
			if (DEBUG_CONTENT_PROVIDER) {
				System.out.println("[select] replace(" + parentDelta.getElement() + ", (model) " + modelIndex + " (view) " + viewIndex + ", " + delta.getElement()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			treeViewer.replace(parentDelta.getElement(), viewIndex, delta.getElement());
		}
		treeViewer.setSelection(new TreeSelection(getViewerTreePath(delta)));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleState(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleState(IModelDelta delta) {
		getTreeViewer().update(delta.getElement(), STATE_PROPERTIES);
	}

	public synchronized void dispose() {
		fTimer.cancel();
		fPendingChildRequests.clear();
		fPendingCountRequests.clear();
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#buildViewerState(org.eclipse.debug.internal.ui.viewers.provisional.ModelDelta)
	 */
	protected void buildViewerState(ModelDelta delta) {
		Tree tree = (Tree) getViewer().getControl();
		TreeItem[] selection = tree.getSelection();
		Set set = new HashSet();
		for (int i = 0; i < selection.length; i++) {
			set.add(selection[i]);
		}
		TreeItem[] items = tree.getItems();
		for (int i = 0; i < items.length; i++) {
			buildViewerState(delta, items[i], set);
		}
	}

	/**
	 * @param delta parent delta to build on
	 * @param item item
	 * @param set set of selected tree items
	 */
	private void buildViewerState(ModelDelta delta, TreeItem item, Set set) {
		Object element = item.getData();
		if (element != null) {
			boolean expanded = item.getExpanded();
			boolean selected = set.contains(item);
			if (expanded || selected) {
				int flags = IModelDelta.NO_CHANGE;
				if (expanded) {
					flags = flags | IModelDelta.EXPAND;
				}
				if (selected) {
					flags = flags | IModelDelta.SELECT;
				}
				ModelDelta childDelta = delta.addNode(element, flags);
				if (expanded) {
					TreeItem[] items = item.getItems();
					for (int i = 0; i < items.length; i++) {
						buildViewerState(childDelta, items[i], set);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#doInitialRestore()
	 */
	protected void doInitialRestore() {
		Tree tree = (Tree) getViewer().getControl();
		TreeItem[] items = tree.getItems();
		for (int i = 0; i < items.length; i++) {
			TreeItem item = items[i];
			Object data = item.getData();
			if (data != null) {
				doRestore(new TreePath(new Object[]{data}));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILazyTreePathContentProvider#getParents(java.lang.Object)
	 */
	public TreePath[] getParents(Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILazyTreePathContentProvider#updateChildCount(org.eclipse.jface.viewers.TreePath, int)
	 */
	public synchronized void updateChildCount(TreePath treePath, int currentChildCount) {
		if (DEBUG_CONTENT_PROVIDER) {
			System.out.println("updateChildCount(" + getElement(treePath) + ", " + currentChildCount + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		refilterChildren(treePath);
		//re-filter children when asked to update the child count for an element (i.e.
		// when refreshing, see if filtered children are still filtered)
		doUpdateChildCount(treePath);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILazyTreePathContentProvider#updateElement(org.eclipse.jface.viewers.TreePath, int)
	 */
	public synchronized void updateElement(TreePath parentPath, int viewIndex) {
		int modelIndex = viewToModelIndex(parentPath, viewIndex);
		if (DEBUG_CONTENT_PROVIDER) {
			System.out.println("updateElement("+ getElement(parentPath) + ", " + viewIndex + ") > modelIndex = " + modelIndex); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		doUpdateElement(parentPath, modelIndex);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILazyTreePathContentProvider#updateHasChildren(org.eclipse.jface.viewers.TreePath)
	 */
	public synchronized void updateHasChildren(TreePath path) {
		if (DEBUG_CONTENT_PROVIDER) {
			System.out.println("updateHasChildren(" + getElement(path)); //$NON-NLS-1$
		}
		doUpdateHasChildren(path);
	}

	/**
	 * @param delta
	 */
	void doRestore(final ModelDelta delta) {
		if (delta.getFlags() != IModelDelta.NO_CHANGE) {
			UIJob job = new UIJob("restore delta") { //$NON-NLS-1$
				public IStatus runInUIThread(IProgressMonitor monitor) {
					TreePath treePath = getViewerTreePath(delta);
					AbstractTreeViewer viewer = (AbstractTreeViewer)getViewer();
					if ((delta.getFlags() & IModelDelta.EXPAND) != 0) {
						viewer.expandToLevel(treePath, 1);
					}
					if ((delta.getFlags() & IModelDelta.SELECT) != 0) {
						viewer.setSelection(new TreeSelection(treePath));
					}
					delta.setFlags(IModelDelta.NO_CHANGE);
					checkIfRestoreComplete();
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.schedule();
		}
	}
}
