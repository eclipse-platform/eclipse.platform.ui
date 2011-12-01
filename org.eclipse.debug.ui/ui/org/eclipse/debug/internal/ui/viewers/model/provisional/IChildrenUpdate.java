/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model.provisional;


/**
 * Context sensitive children update request for a parent and subrange of its
 * children.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 3.3
 */
public interface IChildrenUpdate extends IViewerUpdate {

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
	 * @param offset child offset
	 */
	public void setChild(Object child, int offset); 	
}
