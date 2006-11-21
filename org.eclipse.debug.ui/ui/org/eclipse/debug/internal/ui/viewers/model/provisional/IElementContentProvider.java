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
	 * specified requests.
	 * 
	 * @param updates each update specifies an element to update and provides
	 *  a store for the result
	 */
	public void update(IChildrenCountUpdate[] updates);
	
	/**
	 * Updates children as requested by the given updates.
	 * 
	 * @param updates each update specifies children to update and stores results
	 */	
	public void update(IChildrenUpdate[] updates);
	
	/**
	 * Updates whether elements have children.
	 * 
	 * @param updates each update specifies an element to update and provides
	 *  a store for the result
	 */
	public void update(IHasChildrenUpdate[] updates);
	
}
