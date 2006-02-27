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
package org.eclipse.debug.internal.ui.model.viewers;

import java.util.ArrayList;
import java.util.List;

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
	public AsynchronousTreeModel(AsynchronousModelViewer viewer) {
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
	                    ModelAddRequestMonitor addRequest = new ModelAddRequestMonitor(node, treePath, this);
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
                        ModelRemoveRequestMonitor request = new ModelRemoveRequestMonitor(node, treePath, this);
                        requestScheduled(request);
                        request.done();
                        return;
                    }
                }
            }
            // refresh the parent, if present
            element = treePath.getSegment(treePath.getSegmentCount() - 2);
            nodes  = getNodes(element);
            if (nodes != null) {
                // find the right node
                for (int i = 0; i < nodes.length; i++) {
                    final ModelNode node = nodes[i];
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
    
    /**
     * Callback from async request to remove a node.
     * 
     * @param node
     */
    protected void remove(final ModelNode node) {
        // TODO: preserving selection?
        final ModelNode parentNode = node.getParentNode();
        if (parentNode == null) {
            return;
        }
        synchronized (this) {
            parentNode.removeChild(node);
            unmapNode(node);
            node.dispose();
        }
        final AsynchronousTreeModelViewer viewer = getTreeViewer();
        preservingSelection(new Runnable() {
			public void run() {
		        viewer.nodeDisposed(node);
				viewer.nodeChildrenChanged(parentNode);
			}
		});
    }
	
    /**
     * Asynchronous update for add request.
     * 
     * @param parent
     * @param element
     */
	protected void add(ModelNode parent, Object element) {
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
        }
        //TODO sort???
        // notify the viewer to update
        getTreeViewer().nodeChildrenChanged(parent);		
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
        	ModelNode node = nodes[i];
            List path = new ArrayList();
            path.add(element);
            node = node.getParentNode();
            while (node != null) {
                Object data = node.getElement();
                path.add(0, data);
                node = node.getParentNode();
            }
            paths[i] = new TreePath(path.toArray());
        }
        return paths;
    }	
	
    protected AsynchronousTreeModelViewer getTreeViewer() {
    	return (AsynchronousTreeModelViewer) getViewer();
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
        if (adapter != null) {
            IContainerRequestMonitor update = new ContainerModelRequestMonitor(node, this);
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
     * Called by <code>ContainerModelRequestMonitor</code> after it is determined
     * if the node contains children.
     * 
     * @param node
     * @param containsChildren
     */
     void setIsContainer(ModelNode node, boolean containsChildren) {
    	ModelNode[] prevChildren = null;
    	synchronized (this) {
			prevChildren = node.getChildrenNodes();
			node.setIsContainer(containsChildren);
			if (!containsChildren && prevChildren != null) {
                for (int i = 0; i < prevChildren.length; i++) {
                    ModelNode child = prevChildren[i];
                    unmapNode(child);
                    child.dispose();
                }
                node.setChildren(null);
			}
		}
//    	 update tree outside lock
        AsynchronousTreeModelViewer viewer = getTreeViewer();
		if (containsChildren) {
            if (prevChildren == null) {
                viewer.nodeChildrenChanged(node);
                viewer.nodeContainerChanged(node);
            } else {
                viewer.nodeContainerChanged(node);
            }
        } else if (!containsChildren && prevChildren != null) {            
            for (int i = 0; i < prevChildren.length; i++) {
                ModelNode child = prevChildren[i];
                viewer.nodeDisposed(child);
            }            
            viewer.nodeChildrenChanged(node);
        }
    }    
}
