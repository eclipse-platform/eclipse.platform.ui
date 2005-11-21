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

import org.eclipse.swt.widgets.Widget;

/**
 * Implementation of an <code>IContainerRequestMonitor</code>. Collects whether
 * an element contains children. 
 * <p>
 * Not intended to be subclassed or instantiated by clients. For use
 * speficially with <code>AsynchronousTreeViewer</code>.
 * </p>
 * @since 3.2
 */
class ContainerRequestMonitor extends AsynchronousRequestMonitor implements IContainerRequestMonitor {
	
	/**
	 * Whether the item has children
	 */
	private boolean fIsChildren = false;

	/**
	 * Constructs an update request for the given item in the given viewer.
	 * 
	 * @param item item to update
	 * @param viewer viewer the update was issued for
	 */
	ContainerRequestMonitor(Widget item, AsynchronousTreeViewer viewer) {
		super(item, viewer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.viewers.AsynchronousRequestMonitor#performUpdate()
	 */
	protected void performUpdate() {
		((AsynchronousTreeViewer)getViewer()).setIsContainer(getWidget(), fIsChildren);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.viewers.AsynchronousRequestMonitor#contains(org.eclipse.debug.ui.viewers.AsynchronousRequestMonitor)
	 */
	protected boolean contains(AsynchronousRequestMonitor update) {
		return (update instanceof ChildrenRequestMonitor || update instanceof ContainerRequestMonitor) && contains(update.getWidget());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.viewers.IContainerRequestMonitor#setIsContainer(boolean)
	 */
	public void setIsContainer(boolean container) {
		fIsChildren = container;
	}

}
