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
package org.eclipse.core.internal.databinding.observable.tree;

/**
 * Context sensitive update request for whether elements have children.
 * 
 * @since 3.3
 */
public interface IHasChildrenUpdate extends IViewerUpdate {

	/**
	 * The elements this request is for specified as tree paths. An empty path
	 * identifies the root element.
	 * 
	 * @return elements as tree paths
	 */
	public TreePath[] getElements();

	/**
	 * Sets whether the given element has children.
	 * 
	 * @param element
	 *            tree path to element, or empty for root element
	 * @param hasChildren
	 *            whether it has children
	 */
	public void setHasChilren(TreePath element, boolean hasChildren);
}
