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
 * Request to add an item to a tree.
 *  
 * @since 3.2
 */
class AddRequestMonitor extends AbstractAddRemoveRequestMonitor {

	/**
	 * Adds the given child to the specified parent.
	 * 
	 * @param parent
	 * @param path path to the child in the tree@param child
	 * @param viewer
	 * 
	 */
	AddRequestMonitor(Widget parent, TreePath path, AsynchronousTreeViewer viewer) {
		super(parent, path, viewer);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousRequestMonitor#performUpdate()
	 */
	protected void performUpdate() {
		((AsynchronousTreeViewer)getViewer()).add(getWidget(), getPath().getLastSegment());
	}

}
