/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
 * Context sensitive children update request for a parent and subrange of its
 * children.
 *  
 * @since 3.3
 */
public interface IChildrenUpdate extends IViewerUpdate {

	/**
	 * Returns the parent element that children are being requested for
	 * as a tree path. An empty path identifies the root element.
	 * 
	 * @return parent element as a tree path
	 */
	public TreePath getParent();
	
	/**
	 * Returns the offset at which children have been requested for. This is
	 * the index of the first child being requested.
	 * 
	 * @return offset at which children have been requested for
	 */
	public int getOffset();
	
	/**
	 * Returns the number of children requested.
	 * 
	 * @return number of children requested
	 */
	public int getLength();
	
	/**
	 * Sets the child for this request's parent at the given offset.
	 * 
	 * @param child child
	 * @param index child offset
	 * 
	 * TODO: what to do with <code>null</code>
	 */
	public void setChild(Object child, int index); 	
}
