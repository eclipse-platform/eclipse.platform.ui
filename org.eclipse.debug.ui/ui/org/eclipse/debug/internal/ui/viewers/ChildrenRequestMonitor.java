/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
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

import org.eclipse.debug.internal.ui.viewers.provisional.IChildrenRequestMonitor;

/**
 * Implementation for <code>IChildrenRequestMonitor</code>. Collects
 * children from an asynchronous tree content adapter.  
 * <p>
 * Not intended to be subclassed or instantiated by clients. For use
 * speficially with <code>AsynchronousTreeViewer</code>.
 * </p>
 * @since 3.2
 */
class ChildrenRequestMonitor extends AsynchronousRequestMonitor implements IChildrenRequestMonitor {
    
    private boolean fFirstUpdate = true;
    
	/**
	 * Collection of children retrieved
	 */
    private List fChildren = new ArrayList();

    /**
     * Constucts a monitor to retrieve and update the children of the given
     * node.
     * 
     * @param parent parent to retrieve children for
     * @param model model being updated
     */
    ChildrenRequestMonitor(ModelNode parent, AsynchronousModel model) {
        super(parent, model);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.viewers.IChildrenRequestMonitor#addChild(java.lang.Object)
     */
    public void addChild(Object child) {
        synchronized (fChildren) {
            fChildren.add(child);
        }
        
        scheduleViewerUpdate(250);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.viewers.IChildrenRequestMonitor#addChildren(java.lang.Object[])
     */
    public void addChildren(Object[] children) {
        synchronized (fChildren) {
            for (int i = 0; i < children.length; i++) {
                fChildren.add(children[i]);
            }
        }
        
        scheduleViewerUpdate(0);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.viewers.AsynchronousRequestMonitor#contains(org.eclipse.debug.ui.viewers.AsynchronousRequestMonitor)
     */
    protected boolean contains(AsynchronousRequestMonitor update) {
        return (update instanceof ChildrenRequestMonitor) && contains(update.getNode());
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.viewers.AsynchronousRequestMonitor#performUpdate()
     */
    protected void performUpdate() {
        synchronized (fChildren) {
            if (fFirstUpdate) {
            	getModel().setChildren(getNode(), fChildren);
                fFirstUpdate = false;
            } else {
                for (Iterator iter = fChildren.iterator(); iter.hasNext();) {
                    Object child = iter.next();
                    getModel().add(getNode(), child);    
                }
            }
            fChildren.clear();
        }
    }

}
