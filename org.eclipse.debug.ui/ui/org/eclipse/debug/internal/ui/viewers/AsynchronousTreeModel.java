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
package org.eclipse.debug.internal.ui.viewers;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousContentAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IContainerRequestMonitor;
import org.eclipse.jface.viewers.TreePath;


/**
 * Model for an asynchronous tree
 * 
 * @since 3.2
 */
public class AsynchronousTreeModel extends AsynchronousModel {

	/**
	 * Constructs a new empty tree model
	 * 
	 * @param viewer associated viewer
	 */
	public AsynchronousTreeModel(AsynchronousViewer viewer) {
		super(viewer);
	}
	
	/**
	 * Adds the given path to this model
	 * 
	 * @param treePath
	 */
	public synchronized void add(TreePath treePath) {
		if (treePath.getSegmentCount() > 1) {
	        int parentIndex = treePath.getSegmentCount() - 2;
	        Object parent = treePath.getSegment(parentIndex);

	        // find the paths to the parent, if it's present
	        ModelNode[] nodes = getNodes(parent);
	        if (nodes != null) {
	            // find the right node
	            for (int i = 0; i < nodes.length; i++) {
	                ModelNode node = nodes[i];
	                if (treePath.startsWith(node.getTreePath(), null)) {
	                    AddRequestMonitor addRequest = new AddRequestMonitor(node, treePath, this);
	                    requestScheduled(addRequest);
	                    addRequest.done();
	                    return;
	                }
	            }
	        }
	        // refresh the leaf parent, if any
	        for (int i = parentIndex - 1; i >= 0; i--) {
	            parent = treePath.getSegment(i);
	            nodes = getNodes(parent);
	            if (nodes != null) {
	                for (int j = 0; j < nodes.length; j++) {
	                    final ModelNode node = nodes[j];
	                    if (treePath.startsWith(node.getTreePath(), null)) {
	                    	Runnable runnable = new Runnable() {
								public void run() {
									getViewer().nodeChanged(node);
								}
							};
	                        getViewer().getControl().getDisplay().asyncExec(runnable);
	                        return;
	                    }
	                }
	            }
	        }
		}
	}
	
    /**
     * Removes the item specified in the given tree path from this model.
     * 
     * @param treePath
     */
    public synchronized void remove(TreePath treePath) {
        if (treePath.getSegmentCount() > 1) {
            // find the paths to the element, if it's present
            Object element = treePath.getLastSegment();
            ModelNode[] nodes = getNodes(element);
            if (nodes != null) {
                // find the right node
                for (int i = 0; i < nodes.length; i++) {
                    ModelNode node = nodes[i];
                    if (node.correspondsTo(treePath)) {
                        RemoveRequestMonitor request = new RemoveRequestMonitor(node, treePath, this);
                        requestScheduled(request);
                        request.done();
                        return;
                    }
                }
            }
            // find the first parent present, and update its children
            // to avoid a pending add in the subtree adding something
            // that has been removed
            int index = treePath.getSegmentCount() - 2;
            while (index >= 0) {
	            element = treePath.getSegment(index);
	            nodes  = getNodes(element);
	            if (nodes != null) {
	                // find the right node
	                for (int i = 0; i < nodes.length; i++) {
	                    ModelNode node = nodes[i];
	                    if (treePath.startsWith(node.getTreePath(), null)) {
	                    	updateChildren(node);
	                        return;
	                    }
	                }
	            }
	            index--;
            }
        }
    }	
    
    /**
     * Callback from async request to remove a node.
     * 
     * @param node
     */
    protected void remove(final ModelNode node) {
        final ModelNode parentNode = node.getParentNode();
        if (parentNode == null) {
            return;
        }
        int index = -1;
        synchronized (this) {
        	index = parentNode.getChildIndex(node);
            parentNode.removeChild(node);
            unmapNode(node);
            node.dispose();
            if (DEBUG_MODEL) {
            	DebugUIPlugin.debug("REMOVE: " + node);   //$NON-NLS-1$
            	DebugUIPlugin.debug(toString());
            }
        }
        final int unmapFrom = index;
        final AsynchronousTreeViewer viewer = getTreeViewer();
        preservingSelection(new Runnable() {
			public void run() {
				// unmap the removed node and all children that were shifted
				viewer.unmapNode(node);
				if (unmapFrom > -1) {
					ModelNode[] childrenNodes = parentNode.getChildrenNodes();
					for (int i = unmapFrom; i < childrenNodes.length; i++) {
						viewer.unmapNode(childrenNodes[i]);
					}
				}
		        viewer.nodeChildRemoved(parentNode, unmapFrom);
			}
		});
    }
	
    /**
     * Asynchronous update for add request.
     * 
     * @param parent
     * @param element
     */
	protected void add(final ModelNode parent, Object element) {
        Object[] children = filter(parent.getElement(), new Object[] { element });
        if (children.length == 0) {
            return; // added element was filtered out.
        }
        synchronized (this) {
        	ModelNode[] childrenNodes = parent.getChildrenNodes();
        	if (childrenNodes != null) {
        		for (int i = 0; i < childrenNodes.length; i++) {
        			if (element.equals(childrenNodes[i].getElement()))
        				return; //already added to the tree (probably via a refresh)
        		}
        	}
            ModelNode node = new ModelNode(parent, element);
            parent.addChild(node);
            mapElement(element, node);
            if (DEBUG_MODEL) {
            	DebugUIPlugin.debug("ADD: (parent) " + parent + " (child) " + element);   //$NON-NLS-1$//$NON-NLS-2$
            	DebugUIPlugin.debug(toString());
            }
        }
        //TODO sort???
        // notify the viewer to update
        
        preservingSelection(new Runnable() {
			public void run() {
				getTreeViewer().nodeChildrenAdded(parent);
			}
		});
        		
	}
	
    /**
     * Returns all paths to the given element or <code>null</code> if none.
     * 
     * @param element model element
     * @return paths to the given element or <code>null</code>
     */
    public synchronized TreePath[] getTreePaths(Object element) {
        ModelNode[] nodes = getNodes(element);
        if (nodes == null) {
            return null;
        }
        TreePath[] paths = new TreePath[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            paths[i] = nodes[i].getTreePath();
        }
        return paths;
    }	
	
    protected AsynchronousTreeViewer getTreeViewer() {
    	return (AsynchronousTreeViewer) getViewer();
    }
    
    /**
     * Updates whether the given node has children.
     * 
     * @param node
     *            node to update
     */
    protected void updateHasChildren(ModelNode node) {
        Object element = node.getElement();
        IAsynchronousContentAdapter adapter = getContentAdapter(element);
        if (adapter == null) {
        	adapter = fEmptyContentAdapter;
        }
        if (adapter != null) {
            IContainerRequestMonitor update = new ContainerRequestMonitor(node, this);
            requestScheduled(update);
            adapter.isContainer(element, getPresentationContext(), update);
        }
    }  

    /* (non-Javadoc)
     * 
     * Also unmaps chidren
     * 
     * @see org.eclipse.debug.internal.ui.viewers.AsynchronousModel#unmapNode(org.eclipse.debug.internal.ui.viewers.ModelNode)
     */
    protected synchronized void unmapNode(ModelNode node) {
        ModelNode[] childrenNodes = node.getChildrenNodes();
        if (childrenNodes != null) {
            for (int i = 0; i < childrenNodes.length; i++) {
                unmapNode(childrenNodes[i]);
            }
        }
        super.unmapNode(node);
    }    
    
    /**
     * Called by <code>ContainerRequestMonitor</code> after it is determined
     * if the node contains children.
     * 
     * @param node
     * @param containsChildren
     */
     void setIsContainer(final ModelNode node, boolean containsChildren) {
    	ModelNode[] unmapChildren = null;
    	synchronized (this) {
    		ModelNode[] prevChildren = node.getChildrenNodes();
			node.setIsContainer(containsChildren);
			if (!containsChildren && prevChildren != null) {
				unmapChildren = prevChildren;
                for (int i = 0; i < prevChildren.length; i++) {
                    ModelNode child = prevChildren[i];
                    unmapNode(child);
                    child.dispose();
                }
                node.setChildren(null);
			}
			if (DEBUG_MODEL) {
				DebugUIPlugin.debug("SET CONTAINER: " + node); //$NON-NLS-1$
				DebugUIPlugin.debug(toString());
			}
		}
//    	 update tree outside lock
    	final ModelNode[] finalUnmap = unmapChildren;
    	preservingSelection(new Runnable() {
			public void run() {
				if (finalUnmap != null) {
					for (int i = 0; i < finalUnmap.length; i++) {
						getViewer().unmapNode(finalUnmap[i]);
					}
				}
		    	getTreeViewer().nodeContainerChanged(node);
			}
		});
    	
    }    
}
