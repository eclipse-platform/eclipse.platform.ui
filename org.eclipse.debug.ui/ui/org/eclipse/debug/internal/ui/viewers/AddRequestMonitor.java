/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers;

import org.eclipse.jface.viewers.TreePath;



/**
 * Request to add an item to a tree.
 *  
 * @since 3.2
 */
class AddRequestMonitor extends AbstractAddRemoveRequestMonitor {

	/**
	 * Adds the given child to the specified parent.
	 * 
	 * @param parent parent node
	 * @param path path to the child in the tree@param child
	 * @param viewer
	 * 
	 */
	AddRequestMonitor(ModelNode parent, TreePath path, AsynchronousModel model) {
		super(parent, path, model);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousModelRequestMonitor#performUpdate()
	 */
	protected void performUpdate() {
		((AsynchronousTreeModel)getModel()).add(getNode(), getPath().getLastSegment());
	}

}
