/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - Fix for viewer state save/restore [188704] 
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDeltaVisitor;
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
		IElementContentProvider contentAdapter = ViewerAdapterService.getContentProvider(element);
		if (contentAdapter != null) {
			ChildrenCountUpdate request = new ChildrenCountUpdate(this, getTreeViewer().getInput(), path, element, contentAdapter, getPresentationContext());
			schedule(request);
		}
	}	
	
	protected synchronized void doUpdateElement(TreePath parentPath, int modelIndex) {
		Object parent = getElement(parentPath);
		IElementContentProvider contentAdapter = ViewerAdapterService.getContentProvider(parent);
		if (contentAdapter != null) {
			ChildrenUpdate request = new ChildrenUpdate(this, getTreeViewer().getInput(), parentPath, parent, modelIndex, contentAdapter, getPresentationContext());
			schedule(request);
		}			
	}	
	
	protected synchronized void doUpdateHasChildren(TreePath path) {
		Object element = getElement(path);
		IElementContentProvider contentAdapter = ViewerAdapterService.getContentProvider(element);
		if (contentAdapter != null) {
			HasChildrenUpdate request = new HasChildrenUpdate(this, getTreeViewer().getInput(), path, element, contentAdapter, getPresentationContext());
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
		if (DEBUG_CONTENT_PROVIDER) {
			System.out.println("handleAdd(" + delta.getElement() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		doUpdateChildCount(getViewerTreePath(delta.getParentDelta()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleContent(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleContent(IModelDelta delta) {
		if (delta.getParentDelta() == null && delta.getChildCount() == 0) {
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
			TreePath parentPath = elementPath.getParentPath();
			if (parentPath == null) {
				parentPath = TreePath.EMPTY;
			}
			int viewIndex = modelToViewIndex(parentPath, modelIndex);
			if (viewIndex >= 0) {
				if (DEBUG_CONTENT_PROVIDER) {
					System.out.println("[expand] replace(" + delta.getParentDelta().getElement() + ", (model) " + modelIndex + " (view) " + viewIndex + ", " + delta.getElement()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				treeViewer.replace(parentPath, viewIndex, delta.getElement());
			} else {
				// Element is filtered - if no longer filtered, insert the element
				viewIndex = unfilterElement(parentPath, delta.getElement(), modelIndex);
				if (viewIndex < 0) {
					// insert did not complete
					return;
				}
			}
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
	
	/**
	 * Inserts the given child element of the specified parent into the tree if the element
	 * should *no* longer be filtered. Returns the view index of the newly inserted element
	 * or -1 if not inserted.
	 * 
	 * @param parentPath viewer tree path to parent element
	 * @param element element to insert
	 * @param modelIndex index of the element in the model
	 * @return
	 */
	protected int unfilterElement(TreePath parentPath, Object element, int modelIndex) {
		// Element is filtered - if no longer filtered, insert the element
		if (shouldFilter(parentPath, element)) {
			if (DEBUG_CONTENT_PROVIDER) {
				System.out.println("[unfilter] abort unfilter element: " + element + ", (model) " + modelIndex);  //$NON-NLS-1$ //$NON-NLS-2$
			}
			// still filtered, stop
			return -1;
		}
		// clear the filter an insert the element
		clearFilteredChild(parentPath, modelIndex);
		int viewIndex = modelToViewIndex(parentPath, modelIndex);
		if (viewIndex >= 0) {
			if (DEBUG_CONTENT_PROVIDER) {
				System.out.println("[unfilter] insert(" + parentPath.getLastSegment() + ", (model) " + modelIndex + " (view) " + viewIndex + ", " + element); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			getTreeViewer().insert(parentPath, element, viewIndex);
			return viewIndex;
		} else {
			// still filtered - should not happen
			return -1;
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
		if (DEBUG_CONTENT_PROVIDER) {
			System.out.println("handleRemove(" + delta.getElement() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		}
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
		int itemCount = -1;
		if (modelIndex < 0) {
			// index not provided by delta
			Item[] children = treeViewer.getChildren(parentItem);
			itemCount = children.length;
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
			if (DEBUG_CONTENT_PROVIDER) {
				System.out.println(" - (found) remove(" + parentPath.getLastSegment() + ", viewIndex: " + viewIndex + " modelIndex: " + modelIndex); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			rescheduleUpdates(parentPath, modelIndex);
			getTreeViewer().remove(parentPath, viewIndex);
			removeElementFromFilters(parentPath, modelIndex);
			return;
		}
		if (unmappedIndex >= 0) {
			// did not find the element, but found an unmapped item.
			// remove the unmapped item in it's place and update filters
			if (DEBUG_CONTENT_PROVIDER) {
				System.out.println(" - (not found) remove(" + parentPath.getLastSegment() + ", viewIndex: " + viewIndex + " modelIndex: " + modelIndex); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			modelIndex = viewToModelIndex(parentPath, unmappedIndex);
			rescheduleUpdates(parentPath, modelIndex);
			getTreeViewer().remove(parentPath, unmappedIndex);
			removeElementFromFilters(parentPath, modelIndex);
			return;
		}
		int modelCount = parentDelta.getChildCount();
		if (itemCount >= 0 && modelCount >= 0) {
			if (modelToViewChildCount(parentPath, modelCount) == itemCount) {
				// item count matches the parent's child count, don't do anything
				return;
			}
		}
		// failing that, refresh the parent to properly update for non-visible/unmapped children
		// and update filtered indexes
		if (DEBUG_CONTENT_PROVIDER) {
			System.out.println(" - (not found) remove/refresh(" + delta.getElement()); //$NON-NLS-1$
		}
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
			if (viewIndex >= 0) {
				// when viewIndex < 0, the element has been filtered - so we should not try to replace
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
			TreePath parentPath = elementPath.getParentPath();
			if (parentPath == null) {
				parentPath = TreePath.EMPTY;
			}
			int viewIndex = modelToViewIndex(parentPath, modelIndex);
			if (viewIndex >= 0) {
				if (DEBUG_CONTENT_PROVIDER) {
					System.out.println("[reveal] replace(" + delta.getParentDelta().getElement() + ", (model) " + modelIndex + " (view) " + viewIndex + ", " + delta.getElement()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				treeViewer.replace(parentPath, viewIndex, delta.getElement());
			} else {
				// Element is filtered - insert if filter state changed
				viewIndex = unfilterElement(parentPath, delta.getElement(), modelIndex);
				if (viewIndex < 0) {
					// insert did not complete
					return;
				}
			}
			// only move tree based on selection policy
			if (treeViewer.overrideSelection(treeViewer.getSelection(), new TreeSelection(elementPath))) {
				Widget item = treeViewer.findItem(elementPath);			
				if (item instanceof TreeItem) {
					treeViewer.getTree().setTopItem((TreeItem) item);
				}
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
		// Add memento for top item if it is mapped to an element.  The reveal memento
		// is in its own path to avoid requesting unnecessary data when restoring it.
		TreeItem topItem = tree.getTopItem();
		if (topItem != null && topItem.getData() != null) {
            LinkedList itemsInPath = new LinkedList();
            TreeItem item = topItem;
            while (item != null) {
                itemsInPath.addFirst(item);
                item = item.getParentItem();
            }
			ModelDelta parentDelta = delta;
			for (Iterator itr = itemsInPath.iterator(); itr.hasNext();) {
			    TreeItem next = (TreeItem)itr.next();
			    Object element = next.getData();
	            int index = next.getParentItem() == null ? tree.indexOf(next) : next.getParentItem().indexOf(next);
                ModelDelta childDelta = parentDelta.getChildDelta(element);
                if (childDelta == null) {
                    parentDelta = parentDelta.addNode(element, index, IModelDelta.NO_CHANGE);
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
    protected void doInitialRestore(ModelDelta delta) {
        // Find the reveal delta and mark nodes on its path 
        // to reveal as elements are updated.
        markRevealDelta(delta);
        
        // Restore visible items.  
        // Note (Pawel Piech): the initial list of items is normally 
        // empty, so in most cases the code below does not do anything.
        // Instead doRestore() is called when various updates complete.
        Tree tree = (Tree) getViewer().getControl();
        TreeItem[] items = tree.getItems();
        for (int i = 0; i < items.length; i++) {
            TreeItem item = items[i];
            Object data = item.getData();
            if (data != null) {
                doRestore(new TreePath(new Object[]{data}), i, false, false);
            }
        }
        
    }

    /**
     * Finds the delta with the reveal flag, then it walks up this 
     * delta and marks all the parents of it with the reveal flag.
     * These flags are then used by the restore logic to restore
     * and reveal all the nodes leading up to the element that should
     * be ultimately at the top.
     * @return The node just under the rootDelta which contains
     * the reveal flag.  <code>null</code> if no reveal flag was found.
     */
    private ModelDelta markRevealDelta(ModelDelta rootDelta) {
        final ModelDelta[] revealDelta = new ModelDelta[1];
        IModelDeltaVisitor visitor = new IModelDeltaVisitor() {
            public boolean visit(IModelDelta delta, int depth) {
                if ( (delta.getFlags() & IModelDelta.REVEAL) != 0) {
                    revealDelta[0] = (ModelDelta)delta;
                }
                // Keep recursing only if we haven't found our delta yet.
                return revealDelta[0] == null;
            }
        };
        
        rootDelta.accept(visitor);
        if (revealDelta[0] != null) {
            ModelDelta parentDelta = (ModelDelta)revealDelta[0].getParentDelta(); 
            while(parentDelta.getParentDelta() != null) {
                revealDelta[0] = parentDelta;
                revealDelta[0].setFlags(revealDelta[0].getFlags() | IModelDelta.REVEAL);
                parentDelta = (ModelDelta)parentDelta.getParentDelta();
            }
        }
        return revealDelta[0];
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
	void doRestore(ModelDelta delta, boolean knowsHasChildren, boolean knowsChildCount) {
		TreePath treePath = getViewerTreePath(delta);
		InternalTreeModelViewer viewer = (InternalTreeModelViewer)getViewer();
		// Attempt to expand the node only if the children are known.
		if (knowsHasChildren && (delta.getFlags() & IModelDelta.EXPAND) != 0) {
			viewer.expandToLevel(treePath, 1);
            delta.setFlags(delta.getFlags() & ~IModelDelta.EXPAND);
		}
		if ((delta.getFlags() & IModelDelta.SELECT) != 0) {
			viewer.setSelection(new TreeSelection(treePath), false);
            delta.setFlags(delta.getFlags() & ~IModelDelta.SELECT);
		}
        if ((delta.getFlags() & IModelDelta.REVEAL) != 0) {
            delta.setFlags(delta.getFlags() & ~IModelDelta.REVEAL);
            // Look for the reveal flag in the child deltas.  If 
            // A child delta has the reveal flag, do not set the 
            // top element yet.
            boolean setTopItem = true;
            IModelDelta[] childDeltas = delta.getChildDeltas();
            for (int i = 0; i < childDeltas.length; i++) {
                IModelDelta childDelta = childDeltas[i];
                int modelIndex = childDelta.getIndex();
                if (modelIndex >= 0 && (childDelta.getFlags() & IModelDelta.REVEAL) != 0) {
                    setTopItem = false;
                }
            }
            
            if (setTopItem) { 
                TreePath itemPath = getViewerTreePath(delta);
                Widget topItem = viewer.findItem(itemPath);
                if (topItem instanceof TreeItem) {
                    viewer.getTree().setTopItem((TreeItem) topItem);
                }
            }
		}

        // If we know the children, look for the reveal delta in 
        // the child deltas.  For the children with reveal 
        // flag start a new update.
        if (knowsChildCount) {
	        IModelDelta[] childDeltas = delta.getChildDeltas();
	        for (int i = 0; i < childDeltas.length; i++) {
	            IModelDelta childDelta = childDeltas[i];
	            int modelIndex = childDelta.getIndex();
	            if (modelIndex >= 0 && (childDelta.getFlags() & IModelDelta.REVEAL) != 0) {
	                doUpdateElement(treePath, modelIndex);
	            }
	        }
        }
        
        checkIfRestoreComplete();
	}
}
