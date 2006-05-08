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
 * Request to remove an item from a tree.
 * 
 * @since 3.2
 */
class RemoveRequestMonitor extends AbstractAddRemoveRequestMonitor {

	/**
	 * Removes the given node from the given model.
	 * 
	 * @param node
	 * @param path path to the element to remove
	 * @param model
	 */
	RemoveRequestMonitor(ModelNode node, TreePath path, AsynchronousModel model) {
		super(node, path, model);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousModelRequestMonitor#performUpdate()
	 */
	protected void performUpdate() {
		((AsynchronousTreeModel)getModel()).remove(getNode());
	}

}
