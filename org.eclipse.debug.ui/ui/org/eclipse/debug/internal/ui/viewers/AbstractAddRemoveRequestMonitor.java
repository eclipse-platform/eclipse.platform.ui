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
 * Common function for adds and removes.
 *  
 * @since 3.2
 */
abstract class AbstractAddRemoveRequestMonitor extends AsynchronousRequestMonitor {
	
	private TreePath fPath;

	/**
	 * Constructs a request to add/reomve an element from the tree
	 * 
	 * @param node
	 * @param path path to the element in the tree
	 * @param model
	 * 
	 */
	AbstractAddRemoveRequestMonitor(ModelNode node, TreePath path, AsynchronousModel model) {
		super(node, model);
		fPath = path;
	}
	
	protected TreePath getPath() {
		return fPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousModelRequestMonitor#contains(org.eclipse.debug.internal.ui.viewers.AsynchronousModelRequestMonitor)
	 */
	protected boolean contains(AsynchronousRequestMonitor update) {
		if (update instanceof AbstractAddRemoveRequestMonitor) {
			((AbstractAddRemoveRequestMonitor)update).getPath().equals(getPath());
		}
		return false;
	}

}
