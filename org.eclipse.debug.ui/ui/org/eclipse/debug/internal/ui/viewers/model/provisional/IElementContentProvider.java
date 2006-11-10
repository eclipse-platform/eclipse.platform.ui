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


/**
 * Provides content for an element in a virtual viewer.
 * 
 * @since 3.3
 */
public interface IElementContentProvider {

	/**
	 * Updates the number of children for the given parent elements in the
	 * specified request.
	 * 
	 * @param update specifies counts to update and stores result
	 */
	public void update(IChildrenCountUpdate update);
	
	/**
	 * Updates children as requested by the update.
	 * 
	 * @param update specifies children to update and stores result
	 */	
	public void update(IChildrenUpdate update);
	
	/**
	 * Updates whether elements have children.
	 * 
	 * @param update specifies elements to update and stores result
	 */
	public void update(IHasChildrenUpdate update);
	
}
