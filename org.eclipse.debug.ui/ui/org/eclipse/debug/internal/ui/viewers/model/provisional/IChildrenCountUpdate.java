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
	 * Returns the parent elements that children counts have been requested for
	 * as tree paths. An empty path identifies the root element.
	 * 
	 * @return parent elements as tree paths
	 */
	public TreePath[] getParents();

	/**
	 * Sets the number of children for the given parent.
	 * 
	 * @param parent parent element or empty path for root element
	 * @param numChildren number of children
	 */
	public void setChildCount(TreePath parentPath, int numChildren);
}
