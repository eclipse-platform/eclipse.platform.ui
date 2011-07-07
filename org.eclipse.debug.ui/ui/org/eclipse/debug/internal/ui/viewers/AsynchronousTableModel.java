/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @since 3.2
 *
 */
public class AsynchronousTableModel extends AsynchronousModel {

	/**
	 * Constructs a new table model.
	 * 
	 * @param viewer the backing viewer
	 */
	public AsynchronousTableModel(AsynchronousViewer viewer) {
		super(viewer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.AsynchronousModel#add(org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelNode, java.lang.Object)
	 */
	protected void add(ModelNode parent, Object element) {}
	
	/**
	 * Adds the given elements to the table.
	 * 
	 * @param elements the new elements to add
	 */
	public void add(Object[] elements) {
		TableAddRequestMonitor update = new TableAddRequestMonitor(getRootNode(), elements, this);
		requestScheduled(update);
		update.done();
	}
	
	/**
	 * Notification add request is complete.
	 * 
	 * @param elements elements to add
	 */
	protected void added(Object[] elements) {
		List kids = null;
		boolean changed = false;
    	synchronized (this) {
    		ModelNode[] childrenNodes = getRootNode().getChildrenNodes();
    		if (childrenNodes == null) {
    			kids = new ArrayList(elements.length);
    		} else {
    			kids = new ArrayList(elements.length + childrenNodes.length);
    			for (int i = 0; i < childrenNodes.length; i++) {
					kids.add(childrenNodes[i].getElement());
				}
    		}
    		for (int i = 0; i < elements.length; i++) {
    			if (!kids.contains(elements[i])) {
    				kids.add(elements[i]);
    				changed = true;
    			}
			}
		}
    	if (changed) {
    		setChildren(getRootNode(), kids);
    	}
	}
	
	/**
	 * Inserts the given elements to the table.
	 * 
	 * @param elements the new elements to insert
	 * @param index the index to insert the elements at
	 */
	public void insert(Object[] elements, int index) {
		TableAddRequestMonitor update = new TableInsertRequestMonitor(getRootNode(), elements, index, this);
		requestScheduled(update);
		update.done();
	}
	
	/**
	 * Notification insert request is complete.
	 * 
	 * @param elements elements to add
	 * @param index index to insert at
	 */
	protected void inserted(Object[] elements, int index) {
		List kids = null;
		boolean changed = false;
    	synchronized (this) {
    		ModelNode[] childrenNodes = getRootNode().getChildrenNodes();
    		if (childrenNodes == null) {
    			kids = new ArrayList(elements.length);
    		} else {
    			kids = new ArrayList(elements.length + childrenNodes.length);
    			for (int i = 0; i < childrenNodes.length; i++) {
					kids.add(childrenNodes[i].getElement());
				}
    		}
    		for (int i = 0; i < elements.length; i++) {
    			if (!kids.contains(elements[i])) {
    				kids.add(index, elements[i]);
    				index++;
    				changed = true;
    			}
			}
		}
    	if (changed) {
    		setChildren(getRootNode(), kids);
    	}
	}	

	/**
	 * Removes the given elements from the table.
	 * 
	 * @param elements the elements to remove
	 */
	public void remove(Object[] elements) {
		TableRemoveRequestMonitor update = new TableRemoveRequestMonitor(getRootNode(), elements, this);
		requestScheduled(update);
		update.done();
	}	
	
	/**
	 * Notification remove request is complete.
	 * 
	 * @param elements elements to remove
	 */
	protected void removed(Object[] elements) {
		List kids = null;
		boolean changed = false;
    	synchronized (this) {
    		ModelNode[] childrenNodes = getRootNode().getChildrenNodes();
    		if (childrenNodes != null) {
    			kids = new ArrayList(childrenNodes.length);
    			for (int i = 0; i < childrenNodes.length; i++) {
					kids.add(childrenNodes[i].getElement());
				}
    			for (int i = 0; i < elements.length; i++) {
        			if (kids.remove(elements[i])) {
        				changed = true;
        			}
    			}
    		}
		}
    	if (changed) {
    		setChildren(getRootNode(), kids);
    	}
	}	
	
	/**
	 * Adds the given elements to the table.
	 * @param element the element to replace 
	 * @param replacement the element to replace the old element with
	 */
	public void replace(Object element, Object replacement) {
		TableReplaceRequestMonitor update = new TableReplaceRequestMonitor(getRootNode(), element, replacement, this);
		requestScheduled(update);
		update.done();
	}
	
	/**
	 * Notification add request is complete.
	 * @param element the element to be replaced 
	 * @param replacement the element that replaced the old element
	 */
	protected void replaced(Object element, Object replacement) {
        Object[] filtered = filter(getRootNode().getElement(), new Object[] { replacement });
        if (filtered.length == 0) {
            remove(new Object[]{element});
            return;
        }		
		List list = new ArrayList();
    	synchronized (this) {
    		ModelNode[] nodes = getNodes(element);
    		for (int i = 0; i < nodes.length; i++) {
				ModelNode node = nodes[i];
				node.remap(replacement);
				list.add(node);
			}
		}
    	if (!list.isEmpty()) {
    		Iterator iterator = list.iterator();
    		while (iterator.hasNext()) {
    			ModelNode node = (ModelNode) iterator.next();
    			getViewer().nodeChanged(node);
    		}
    	}
	}	
}
