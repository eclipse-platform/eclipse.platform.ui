/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - Fix for viewer state save/restore [188704] 
 *     Pawel Piech (Wind River) - added support for a virtual tree model viewer (Bug 242489)
 *     Dorin Ciuca - Top index fix (Bug 324100)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDeltaVisitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Display;

/**
 * Content provider for a virtual tree.
 * 
 * @since 3.3
 */
public class TreeModelContentProvider extends ModelContentProvider implements ITreeModelContentProvider {
	
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
			ChildrenCountUpdate request = new ChildrenCountUpdate(this, getViewer().getInput(), path, element, contentAdapter, getPresentationContext());
			schedule(request);
		}
	}	
	
	protected synchronized void doUpdateElement(TreePath parentPath, int modelIndex) {
		Object parent = getElement(parentPath);
		IElementContentProvider contentAdapter = ViewerAdapterService.getContentProvider(parent);
		if (contentAdapter != null) {
			ChildrenUpdate request = new ChildrenUpdate(this, getViewer().getInput(), parentPath, parent, modelIndex, contentAdapter, getPresentationContext());
			schedule(request);
		}			
	}	
	
	protected synchronized void doUpdateHasChildren(TreePath path) {
		Object element = getElement(path);
		IElementContentProvider contentAdapter = ViewerAdapterService.getContentProvider(element);
		if (contentAdapter != null) {
			HasChildrenUpdate request = new HasChildrenUpdate(this, getViewer().getInput(), path, element, contentAdapter, getPresentationContext());
			schedule(request);
		}
	}		
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#getPresentationContext()
	 */
	protected IPresentationContext getPresentationContext() {
	    ITreeModelViewer viewer = getViewer();
	    if (viewer != null) {
	        return viewer.getPresentationContext();
	    } 
	    return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleAdd(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleAdd(IModelDelta delta) {
		IModelDelta parentDelta = delta.getParentDelta();
		TreePath parentPath = getViewerTreePath(parentDelta);
		Object element = delta.getElement();
		int count = parentDelta.getChildCount();
		if (count > 0) {
		    setModelChildCount(parentPath, count);
		    int modelIndex = count - 1;
		    if (delta.getIndex() != -1) {
		    	// assume addition at end, unless index specified by delta
		    	modelIndex = delta.getIndex();
		    }
			if (shouldFilter(parentPath, element)) {
				addFilteredIndex(parentPath, modelIndex, element);
				if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
					System.out.println("[filtered] handleAdd(" + delta.getElement() + ") > modelIndex: " + modelIndex); //$NON-NLS-1$ //$NON-NLS-2$
				}
				// it was filtered so the child count does not change
			} else {
				if (isFiltered(parentPath, modelIndex)) {
					clearFilteredChild(parentPath, modelIndex);
				}
                int viewIndex = modelToViewIndex(parentPath, modelIndex);
				int viewCount = modelToViewChildCount(parentPath, count);
				if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
					System.out.println("handleAdd(" + delta.getElement() + ") viewIndex: " + viewIndex + " modelIndex: " + modelIndex + " viewCount: " + viewCount + " modelCount: " + count); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				}
				getViewer().setChildCount(parentPath, viewCount);
				getViewer().autoExpand(parentPath);
				getViewer().replace(parentPath, viewIndex, element);
				TreePath childPath = parentPath.createChildPath(element);
				updateHasChildren(childPath);
				restorePendingStateOnUpdate(childPath, modelIndex, false, false, false);
			}	        
		} else {
			if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
				System.out.println("handleAdd(" + delta.getElement() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		    doUpdateChildCount(getViewerTreePath(delta.getParentDelta()));
		}
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
		appendToPendingStateDelta(treePath);
		getViewer().refresh(getElement(treePath));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ModelContentProvider#handleCollapse(org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta)
	 */
	protected void handleCollapse(IModelDelta delta) {
		TreePath elementPath = getViewerTreePath(delta);
		getViewer().setExpandedState(elementPath, false);
        cancelRestore(elementPath, IModelDelta.EXPAND);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleExpand(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleExpand(IModelDelta delta) {
		// expand each parent, then this node
		IModelDelta parentDelta = delta.getParentDelta();
		if (parentDelta != null) {
			if ((parentDelta.getFlags() & IModelDelta.EXPAND) == 0) {
				handleExpand(parentDelta);
			}
			expand(delta);
		} else {
	        int childCount = delta.getChildCount();
	        TreePath elementPath = getViewerTreePath(delta);
	        if (childCount > 0) {
	            int viewCount = modelToViewChildCount(elementPath, childCount);
	            if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
	                System.out.println("[expand] setChildCount(" + delta.getElement() + ", (model) " + childCount + " (view) " + viewCount); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	            }
	            getViewer().setChildCount(elementPath, viewCount);	            
	        }
		}
	}
	
	protected void expand(IModelDelta delta) {
		int childCount = delta.getChildCount();
		int modelIndex = delta.getIndex();
		ITreeModelContentProviderTarget treeViewer = getViewer();
		TreePath elementPath = getViewerTreePath(delta);
		if (modelIndex >= 0) {
			TreePath parentPath = elementPath.getParentPath();
			if (parentPath == null) {
				parentPath = TreePath.EMPTY;
			}
			int viewIndex = modelToViewIndex(parentPath, modelIndex);
			if (viewIndex >= 0) {
				if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
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
			if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
				System.out.println("[expand] setChildCount(" + delta.getElement() + ", (model) " + childCount + " (view) " + viewCount); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			treeViewer.setChildCount(elementPath, viewCount);
	        if (!treeViewer.getExpandedState(elementPath)) {
	            treeViewer.expandToLevel(elementPath, 1);
	            cancelRestore(elementPath, IModelDelta.COLLAPSE);
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
			if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
				System.out.println("[unfilter] abort unfilter element: " + element + ", (model) " + modelIndex);  //$NON-NLS-1$ //$NON-NLS-2$
			}
			// still filtered, stop
			return -1;
		}
		// clear the filter an insert the element
		clearFilteredChild(parentPath, modelIndex);
		int viewIndex = modelToViewIndex(parentPath, modelIndex);
		if (viewIndex >= 0) {
			if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
				System.out.println("[unfilter] insert(" + parentPath.getLastSegment() + ", (model) " + modelIndex + " (view) " + viewIndex + ", " + element); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			getViewer().insert(parentPath, element, viewIndex);
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
		getViewer().insert(getViewerTreePath(delta.getParentDelta()), delta.getElement(), delta.getIndex());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleRemove(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleRemove(IModelDelta delta) {
		if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
			System.out.println("handleRemove(" + delta.getElement() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		IModelDelta parentDelta = delta.getParentDelta();
		ITreeModelContentProviderTarget treeViewer = getViewer();
		TreePath parentPath = getViewerTreePath(parentDelta);
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
		    itemCount = treeViewer.getChildCount(parentPath);
		    if (itemCount == -1) {
		        clearFilters(parentPath);
		    }
		    viewIndex = treeViewer.findElementIndex(parentPath, element);
		    if (viewIndex >= 0) {
		        modelIndex = viewToModelIndex(parentPath, viewIndex);
		    } else {
		        unmappedIndex = treeViewer.findElementIndex(parentPath, null);
		    }
		} else {
			viewIndex = modelToViewIndex(parentPath, modelIndex);
		}
		if (modelIndex >= 0) {
			// found the element
			if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
				System.out.println(" - (found) remove(" + parentPath.getLastSegment() + ", viewIndex: " + viewIndex + " modelIndex: " + modelIndex); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			rescheduleUpdates(parentPath, modelIndex);
			getViewer().remove(parentPath, viewIndex);
			removeElementFromFilters(parentPath, modelIndex);
			return;
		}
		if (unmappedIndex >= 0) {
			// did not find the element, but found an unmapped item.
			// remove the unmapped item in it's place and update filters
			if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
				System.out.println(" - (not found) remove(" + parentPath.getLastSegment() + ", viewIndex: " + viewIndex + " modelIndex: " + modelIndex + " unmapped index: " + unmappedIndex); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			modelIndex = viewToModelIndex(parentPath, unmappedIndex);
			rescheduleUpdates(parentPath, modelIndex);
			getViewer().remove(parentPath, unmappedIndex);
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
		if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
			System.out.println(" - (not found) remove/refresh(" + delta.getElement()); //$NON-NLS-1$
		}
		getViewer().remove(getViewerTreePath(delta));
		clearFilters(parentPath);
		getViewer().refresh(parentDelta.getElement());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleReplace(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleReplace(IModelDelta delta) {
		TreePath parentPath = getViewerTreePath(delta.getParentDelta());
		getViewer().replace(parentPath, delta.getIndex(), delta.getElement());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleSelect(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleSelect(IModelDelta delta) {
		int modelIndex = delta.getIndex();
		ITreeModelContentProviderTarget treeViewer = getViewer();
		// check if selection is allowed
		IStructuredSelection candidate = new TreeSelection(getViewerTreePath(delta));
		if ((delta.getFlags() & IModelDelta.FORCE) == 0 && 
		    !treeViewer.overrideSelection(treeViewer.getSelection(), candidate)) 
		{
			return;
		}
		// empty the selection before replacing elements to avoid materializing elements (@see bug 305739)
		treeViewer.clearSelectionQuiet();
		if (modelIndex >= 0) {
			IModelDelta parentDelta = delta.getParentDelta();
			TreePath parentPath = getViewerTreePath(parentDelta);
			int viewIndex = modelToViewIndex(parentPath, modelIndex);
			if (viewIndex >= 0) {
				// when viewIndex < 0, the element has been filtered - so we should not try to replace
				int modelCount = parentDelta.getChildCount();
				if (modelCount > 0) {
					int viewCount = modelToViewChildCount(parentPath, modelCount);
					if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
						System.out.println("[select] setChildCount(" + parentDelta.getElement() + ", (model) " + parentDelta.getChildCount() + " (view) " + viewCount ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
					treeViewer.setChildCount(parentPath, viewCount);
				}
				if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
					System.out.println("[select] replace(" + parentDelta.getElement() + ", (model) " + modelIndex + " (view) " + viewIndex + ", " + delta.getElement()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				treeViewer.replace(parentPath, viewIndex, delta.getElement());
			}
		}
		TreePath selectionPath = getViewerTreePath(delta);
		if (treeViewer.trySelection(new TreeSelection(selectionPath), false, (delta.getFlags() | IModelDelta.FORCE) == 0)) {
	        cancelRestore(selectionPath, IModelDelta.SELECT);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleState(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleState(IModelDelta delta) {
		getViewer().update(delta.getElement());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ModelContentProvider#handleReveal(org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta)
	 */
	protected void handleReveal(IModelDelta delta) {
		IModelDelta parentDelta = delta.getParentDelta();
		if (parentDelta != null) {
			handleExpand(parentDelta);
			reveal(delta);
            cancelRestore(getViewerTreePath(delta), IModelDelta.REVEAL);
		}
	}
	
	protected void reveal(IModelDelta delta) {
		int modelIndex = delta.getIndex();
		ITreeModelContentProviderTarget treeViewer = getViewer();
		TreePath elementPath = getViewerTreePath(delta);
		if (modelIndex >= 0) {
			TreePath parentPath = elementPath.getParentPath();
			if (parentPath == null) {
				parentPath = TreePath.EMPTY;
			}
			int viewIndex = modelToViewIndex(parentPath, modelIndex);
			if (viewIndex >= 0) {
				if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
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

			// only move tree based on force flag and selection policy
			if ((delta.getFlags() & IModelDelta.FORCE) != 0 ||
			    treeViewer.overrideSelection(treeViewer.getSelection(), new TreeSelection(elementPath))) 
			{
			    treeViewer.reveal(parentPath, viewIndex);
			}
		}
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#buildViewerState(org.eclipse.debug.internal.ui.viewers.provisional.ModelDelta)
	 */
	protected void buildViewerState(ModelDelta delta) {
        ITreeModelContentProviderTarget viewer = getViewer();
        viewer.saveElementState(EMPTY_TREE_PATH, delta, IModelDelta.SELECT | IModelDelta.EXPAND);
		
		// Add memento for top item if it is mapped to an element.  The reveal memento
		// is in its own path to avoid requesting unnecessary data when restoring it.
		TreePath topElementPath = viewer.getTopElementPath();
		if (topElementPath != null) {
			ModelDelta parentDelta = delta;
			TreePath parentPath = EMPTY_TREE_PATH;
			for (int i = 0; i < topElementPath.getSegmentCount(); i++) {
			    Object element = topElementPath.getSegment(i);
			    int index = viewer.findElementIndex(parentPath, element);
                ModelDelta childDelta = parentDelta.getChildDelta(element);
                if (childDelta == null) {
                    parentDelta = parentDelta.addNode(element, index, IModelDelta.NO_CHANGE);
                } else {
                    parentDelta = childDelta;
                }
                parentPath = parentPath.createChildPath(element);
			}
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.REVEAL);
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
        int count = getViewer().getChildCount(TreePath.EMPTY);
        for (int i = 0; i < count; i++) {
            Object data = getViewer().getChildElement(TreePath.EMPTY, i);
            if (data != null) {
                restorePendingStateOnUpdate(new TreePath(new Object[]{data}), i, false, false, false);
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
		if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
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
		if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
			System.out.println("updateElement("+ getElement(parentPath) + ", " + viewIndex + ") > modelIndex = " + modelIndex); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		doUpdateElement(parentPath, modelIndex);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILazyTreePathContentProvider#updateHasChildren(org.eclipse.jface.viewers.TreePath)
	 */
	public synchronized void updateHasChildren(TreePath path) {
		if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
			System.out.println("updateHasChildren(" + getElement(path)); //$NON-NLS-1$
		}
		doUpdateHasChildren(path);
	}

	/**
	 * @param delta
	 */
	void restorePendingStateNode(final ModelDelta delta, boolean knowsHasChildren, boolean knowsChildCount, boolean checkChildrenRealized) {
		final TreePath treePath = getViewerTreePath(delta);
		final ITreeModelContentProviderTarget viewer = getViewer();

        // Attempt to expand the node only if the children are known.
		if (knowsHasChildren) {
		    if ((delta.getFlags() & IModelDelta.EXPAND) != 0) {
	            if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
	                System.out.println("\tRESTORE EXPAND: " + treePath.getLastSegment()); //$NON-NLS-1$
	            }
    			viewer.expandToLevel(treePath, 1);
                delta.setFlags(delta.getFlags() & ~IModelDelta.EXPAND);
		    }
            if ((delta.getFlags() & IModelDelta.COLLAPSE) != 0) {
                if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                    System.out.println("\tRESTORE COLLAPSE: " + treePath.getLastSegment()); //$NON-NLS-1$
                }
                // Check auto-expand before collapsing an element (bug 335734)
                int autoexpand = getViewer().getAutoExpandLevel();
                if (autoexpand != ITreeModelViewer.ALL_LEVELS && autoexpand < (treePath.getSegmentCount() + 1)) {
                    getViewer().setExpandedState(treePath, false);
                }
                delta.setFlags(delta.getFlags() & ~IModelDelta.COLLAPSE);
            }
		}
		
		if ((delta.getFlags() & IModelDelta.SELECT) != 0) {
            delta.setFlags(delta.getFlags() & ~IModelDelta.SELECT);
            if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                System.out.println("\tRESTORE SELECT: " + treePath.getLastSegment()); //$NON-NLS-1$
            }
            ITreeSelection currentSelection = (ITreeSelection)viewer.getSelection();
            if (currentSelection == null || currentSelection.isEmpty()) {
                viewer.setSelection(new TreeSelection(treePath), false, false);
            } else {
                TreePath[] currentPaths = currentSelection.getPaths();
                boolean pathInSelection = false;
                for (int i = 0; i < currentPaths.length; i++) {
                    if (currentPaths[i].equals(treePath)) {
                        pathInSelection = true;
                        break;
                    }
                }
                // Only set the selection if the element is not yet in 
                // selection.  Otherwise the setSelection() call will 
                // update selection listeners needlessly. 
                if (!pathInSelection) {
                    TreePath[] newPaths = new TreePath[currentPaths.length + 1];
                    System.arraycopy(currentPaths, 0, newPaths, 0, currentPaths.length);
                    newPaths[newPaths.length - 1] = treePath;
                    viewer.setSelection(new TreeSelection(newPaths), false, false);
                }
            }
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
            	Assert.isTrue(fPendingSetTopItem == null);
                final TreePath parentPath = treePath.getParentPath();
                
                // listen when current updates are complete and only 
                // then do the REVEAL
                fPendingSetTopItem = new IPendingRevealDelta() {
                	// Revealing some elements can trigger expanding some of elements
                	// that have been just revealed. Therefore, we have to check one 
                	// more time after the new triggered updates are completed if we
                	// have to set again the top index
                	private int counter = 0;
                	private Object modelInput = fPendingState.getElement();
                	
					public void viewerUpdatesBegin() {
					}

					public void viewerUpdatesComplete() {
						// assume that fRequestsInProgress is empty if we got here
						Assert.isTrue(fRequestsInProgress.isEmpty());
                    	
						final Display viewerDisplay = viewer.getDisplay();
                    	if (fWaitingRequests.isEmpty() && !viewerDisplay.isDisposed()) {
                    		viewerDisplay.asyncExec(new Runnable() {
		                        public void run() {
		                        	if (TreeModelContentProvider.this.isDisposed()) {
		                        		return;
		                        	}
		                        	
		                        	TreePath topPath = viewer.getTopElementPath();
		                        	if (!treePath.equals(topPath)) {
						                int index = viewer.findElementIndex(parentPath, treePath.getLastSegment());
						                if (index >= 0) { 
						                    if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
						                        System.out.println("\tRESTORE REVEAL: " + treePath.getLastSegment()); //$NON-NLS-1$
						                    }
						                    viewer.reveal(parentPath, index);
						                    
						                }
		                        	}
		                        }
		                    });
                    		
							counter++;
							// in case the pending state was already set to null, we assume that
							// all others elements are restored, so we don't expect that REVEAL will
							// trigger other updates
							if (counter > 1 || fPendingState == null) {
		                        dispose();		                        
							}
                    	}

					}

					public void updateStarted(IViewerUpdate update) {
					}

					public void updateComplete(IViewerUpdate update) {
					}
					
					public ModelDelta getDelta() {
						return delta;
					}

					public void dispose() {
						// remove myself as viewer update listener
                        viewer.removeViewerUpdateListener(this);
                        
                        // top item is set
                        fPendingSetTopItem = null;
                        
                        if (fPendingState == null) {
                            if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                                System.out.println("STATE RESTORE COMPELTE: " + fPendingState); //$NON-NLS-1$
                            }
                            notifyStateUpdate(modelInput, STATE_RESTORE_SEQUENCE_COMPLETE, null);
                        } else {
                            checkIfRestoreComplete();
                        }
                    }
                	
                };
                viewer.addViewerUpdateListener(fPendingSetTopItem);
            }            
		}

        // If we know the child count of the element, look for the reveal 
        // flag in the child deltas.  For the children with reveal flag start 
        // a new update.  
        // If the child delta's index is out of range, strip the reveal flag
        // since it is no longer applicable.
        if (knowsChildCount) {
            int childCount = viewer.getChildCount(treePath);
            if (childCount >= 0) {
		        ModelDelta[] childDeltas = (ModelDelta[])delta.getChildDeltas();
		        for (int i = 0; i < childDeltas.length; i++) {
		            ModelDelta childDelta = childDeltas[i];
		            int modelIndex = childDelta.getIndex();
    	            if (modelIndex >= 0 && (childDelta.getFlags() & IModelDelta.REVEAL) != 0) {
    	            	if (modelIndex < childCount) {
    	            		doUpdateElement(treePath, modelIndex);
    	            	} else {
    		            	childDelta.setFlags(childDelta.getFlags() & ~IModelDelta.REVEAL);
    	            	}	    	            
    	            }
		        }
            }
        }
        
        // Some children of this element were just updated.  If all its 
        // children are now realized, clear out any elements that still 
        // have flags, because they represent elements that were removed.
        if ((checkChildrenRealized && getElementChildrenRealized(treePath)) ||
             (knowsHasChildren && !viewer.getHasChildren(treePath)) ) 
        {
            if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                System.out.println("\tRESTORE CONTENT: " + treePath.getLastSegment()); //$NON-NLS-1$
            }
            delta.setFlags(delta.getFlags() & ~IModelDelta.CONTENT);            
        }
	}

}
