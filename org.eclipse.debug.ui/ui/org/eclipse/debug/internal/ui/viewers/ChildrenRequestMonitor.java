/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import java.util.List;

import org.eclipse.swt.widgets.Widget;

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
    
	/**
	 * Collection of children retrieved
	 */
    private List fChildren = new ArrayList();

    /**
     * Constucts a monitor to retrieve and update the children of the given
     * widget.
     * 
     * @param widget widget to retrieve children for
     */
    ChildrenRequestMonitor(Widget widget, AsynchronousTreeViewer viewer) {
        super(widget, viewer);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.viewers.IChildrenRequestMonitor#addChild(java.lang.Object)
     */
    public void addChild(Object child) {
        fChildren.add(child);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.viewers.IChildrenRequestMonitor#addChildren(java.lang.Object[])
     */
    public void addChildren(Object[] children) {
        for (int i = 0; i < children.length; i++) {
            fChildren.add(children[i]);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.viewers.AsynchronousRequestMonitor#contains(org.eclipse.debug.ui.viewers.AsynchronousRequestMonitor)
     */
    protected boolean contains(AsynchronousRequestMonitor update) {
        return (update instanceof ChildrenRequestMonitor || update instanceof ContainerRequestMonitor) && contains(update.getWidget());
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.viewers.AsynchronousRequestMonitor#performUpdate()
     */
    protected void performUpdate() {
        ((AsynchronousTreeViewer)getViewer()).setChildren(getWidget(), fChildren);
    }

}
