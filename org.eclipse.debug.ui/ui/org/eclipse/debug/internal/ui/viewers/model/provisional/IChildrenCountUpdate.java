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
package org.eclipse.debug.internal.ui.viewers.model.provisional;

import org.eclipse.jface.viewers.TreePath;


/**
 * Request monitor used to collect the number of children for an element in a viewer.
 * 
 * @since 3.3
 */
public interface IChildrenCountUpdate extends IViewerUpdate {
	
	/**
	 * Returns the element that a child count has been requested for
	 * as a tree path. An empty path identifies the root element.
	 * 
	 * @return element as a tree path
	 */
	public TreePath getElementPath();

	/**
	 * Sets the number of children for this update.
	 * 
	 * @param numChildren number of children
	 */
	public void setChildCount(int numChildren);
}
