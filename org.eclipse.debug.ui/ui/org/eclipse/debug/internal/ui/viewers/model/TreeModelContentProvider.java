/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ILazyTreePathContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.progress.UIJob;

/**
 * Content provider for a virtual tree.
 * 
 * @since 3.3
 */
public class TreeModelContentProvider extends ModelContentProvider implements ILazyTreePathContentProvider {
	
	protected static final String[] STATE_PROPERTIES = new String[]{IBasicPropertyConstants.P_TEXT, IBasicPropertyConstants.P_IMAGE};
	
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
			ChildrenCountUpdate request = new ChildrenCountUpdate(this, path, element, contentAdapter);
			schedule(request);
		}
	}	
	
	protected synchronized void doUpdateElement(TreePath parentPath, int modelIndex) {
		Object parent = getElement(parentPath);
		IElementContentProvider contentAdapter = getContentAdapter(parent);
		if (contentAdapter != null) {
			ChildrenUpdate request = new ChildrenUpdate(this, parentPath, parent, modelIndex, contentAdapter);
			schedule(request);
		}			
	}	
	
	protected synchronized void doUpdateHasChildren(TreePath path) {
		Object element = getElement(path);
		IElementContentProvider contentAdapter = getContentAdapter(element);
		if (contentAdapter != null) {
			HasChildrenUpdate request = new HasChildrenUpdate(this, path, element, contentAdapter);
			schedule(request);
		}
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
	 * @see org.eclipse.debug.internal.ui.viewers.model.ModelContentProvider#handleCollapse(org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta)
	 */
	protected void handleCollapse(IModelDelta delta) {
		TreePath elementPath = getViewerTreePath(delta);
		getTreeViewer().setExpandedState(elementPath, false);
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
		TreePath elementPath = getViewerTreePath(delta);
		if (modelIndex >= 0) {
			int viewIndex = modelToViewIndex(getViewerTreePath(delta.getParentDelta()), modelIndex);
			if (DEBUG_CONTENT_PROVIDER) {
				System.out.println("[expand] replace(" + delta.getParentDelta().getElement() + ", (model) " + modelIndex + " (view) " + viewIndex + ", " + delta.getElement()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			TreePath parentPath = elementPath.getParentPath();
			if (parentPath == null) {
				parentPath = TreePath.EMPTY;
			}
			treeViewer.replace(parentPath, viewIndex, delta.getElement());
		}
		if (childCount > 0) {
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
		IModelDelta parentDelta = delta.getParentDelta();
		InternalTreeModelViewer treeViewer = (InternalTreeModelViewer)getViewer();
		TreePath parentPath = getViewerTreePath(parentDelta);
		Widget parentItem = treeViewer.findItem(parentPath);
		if (parentItem == null) {
			// if we can't see the parent, nothing to worry about (but clear the filters, if any)
			clearFilters(parentPath);
			return;
		}
		Object element = delta.getElement();
		if (removeElementFromFilters(parentPath, element)) {
			// element was filtered - done
			return;
		}
		int viewIndex = -1;
		int modelIndex = delta.getIndex();
		int unmappedIndex = -1;
		if (modelIndex < 0) {
			// index not provided by delta
			Item[] children = treeViewer.getChildren(parentItem);
			for (int i = 0; i < children.length; i++) {
				Item item = children[i];
				Object data = item.getData();
				if (element.equals(data)) {
					viewIndex = i;
					modelIndex = viewToModelIndex(parentPath, i);
					break;
				} else if (data == null) {
					unmappedIndex = i;
				}
			}
		} else {
			viewIndex = modelToViewIndex(parentPath, modelIndex);
		}
		if (modelIndex >= 0) {
			// found the element
			getTreeViewer().remove(parentPath, viewIndex);
			removeElementFromFilters(parentPath, modelIndex);
			return;
		}
		if (unmappedIndex >= 0) {
			// did not find the element, but found an unmapped item.
			// remove the unmapped item in it's place and update filters
			getTreeViewer().remove(parentPath, unmappedIndex);
			removeElementFromFilters(parentPath, viewToModelIndex(parentPath, unmappedIndex));
			return;
		}
		// failing that, refresh the parent to properly update for non-visible/unmapped children
		// and update filtered indexes
		getTreeViewer().remove(getViewerTreePath(delta));
		clearFilters(parentPath);
		getTreeViewer().refresh(parentDelta.getElement());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleReplace(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleReplace(IModelDelta delta) {
		TreePath parentPath = getViewerTreePath(delta.getParentDelta());
		getTreeViewer().replace(parentPath, delta.getIndex(), delta.getElement());
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
			treeViewer.replace(parentPath, viewIndex, delta.getElement());
		}
		treeViewer.setSelection(new TreeSelection(getViewerTreePath(delta)));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleState(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleState(IModelDelta delta) {
		getTreeViewer().update(delta.getElement(), STATE_PROPERTIES);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ModelContentProvider#handleReveal(org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta)
	 */
	protected void handleReveal(IModelDelta delta) {
		IModelDelta parentDelta = delta.getParentDelta();
		if (parentDelta != null) {
			handleExpand(parentDelta);
			reveal(delta);
		}
	}
	
	protected void reveal(IModelDelta delta) {
		int modelIndex = delta.getIndex();
		InternalTreeModelViewer treeViewer = (InternalTreeModelViewer) getTreeViewer();
		TreePath elementPath = getViewerTreePath(delta);
		if (modelIndex >= 0) {
			int viewIndex = modelToViewIndex(getViewerTreePath(delta.getParentDelta()), modelIndex);
			if (DEBUG_CONTENT_PROVIDER) {
				System.out.println("[reveal] replace(" + delta.getParentDelta().getElement() + ", (model) " + modelIndex + " (view) " + viewIndex + ", " + delta.getElement()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			TreePath parentPath = elementPath.getParentPath();
			if (parentPath == null) {
				parentPath = TreePath.EMPTY;
			}
			treeViewer.replace(parentPath, viewIndex, delta.getElement());
			Widget item = treeViewer.findItem(elementPath);
			if (item instanceof TreeItem) {
				treeViewer.getTree().setTopItem((TreeItem) item);
			}
		}
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
			buildViewerState(EMPTY_TREE_PATH, delta, items[i], set, i);
		}
		// add memento for top item
		TreeItem topItem = tree.getTopItem();
		if (topItem != null) {
			TreePath path = ((InternalTreeModelViewer)getTreeViewer()).getTreePathFromItem(topItem);
			ModelDelta parentDelta = delta;
			for (int i = 0; i < path.getSegmentCount(); i++) {
				Object element = path.getSegment(i);
				ModelDelta childDelta = parentDelta.getChildDelta(element);
				if (childDelta == null) {
					parentDelta = parentDelta.addNode(element, IModelDelta.NO_CHANGE);
				} else {
					parentDelta = childDelta;
				}
			}
			parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.REVEAL);
		}
	}

	/**
	 * @param delta parent delta to build on
	 * @param item item
	 * @param set set of selected tree items
	 * @param index the item's index relative to it's parent
	 */
	private void buildViewerState(TreePath parentPath, ModelDelta delta, TreeItem item, Set set, int index) {
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
				int modelIndex = viewToModelIndex(parentPath, index);
				TreePath elementPath = parentPath.createChildPath(element);
				int numChildren = viewToModelCount(elementPath, item.getItemCount());
				ModelDelta childDelta = delta.addNode(element, modelIndex, flags, numChildren);
				if (expanded) {
					TreeItem[] items = item.getItems();
					for (int i = 0; i < items.length; i++) {
						buildViewerState(elementPath, childDelta, items[i], set, i);
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
					InternalTreeModelViewer viewer = (InternalTreeModelViewer)getViewer();
					if ((delta.getFlags() & IModelDelta.EXPAND) != 0) {
						viewer.expandToLevel(treePath, 1);
					}
					if ((delta.getFlags() & IModelDelta.SELECT) != 0) {
						viewer.setSelection(new TreeSelection(treePath));
					}
					int flag = IModelDelta.NO_CHANGE;
					if ((delta.getFlags() & IModelDelta.REVEAL) != 0) {
						flag = IModelDelta.REVEAL;
					}
					delta.setFlags(flag);
					IModelDelta topItemDelta = checkIfRestoreComplete();
					// force child deltas to update, so viewer is populated
					IModelDelta[] childDeltas = delta.getChildDeltas();
					for (int i = 0; i < childDeltas.length; i++) {
						IModelDelta childDelta = childDeltas[i];
						int modelIndex = childDelta.getIndex();
						if (modelIndex >= 0) {
							doUpdateElement(treePath, modelIndex);
						}
					}
					if (topItemDelta != null) {
						TreePath itemPath = getViewerTreePath(topItemDelta);
						Widget topItem = viewer.findItem(itemPath);
						if (topItem instanceof TreeItem) {
							viewer.getTree().setTopItem((TreeItem) topItem);
						}
					}
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.schedule();
		}
	}
}
