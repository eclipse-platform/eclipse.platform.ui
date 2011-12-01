/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River - Pawel Piech - Need a clarification on usage of IElement*Provider interfaces with update arrays (Bug 213609)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model.provisional;


/**
 * Provides content for an element in a tree model viewer.
 * <p>
 * Note: provider methods are called in the Display thread of the viewer.
 * To avoid blocking the UI, long running operations should be performed 
 * asynchronously.
 * </p>
 * 
 * @since 3.3
 */
public interface IElementContentProvider {

	/**
	 * Updates the number of children for the given parent elements in the
	 * specified requests.
	 * 
	 * @param updates Each update specifies an element to update and provides
	 *  a store for the result.  The update array is guaranteed to have at least 
	 *  one element, and for all updates to have the same presentation context.
	 */
	public void update(IChildrenCountUpdate[] updates);
	
	/**
	 * Updates children as requested by the given updates.
	 * 
	 * @param updates Each update specifies children to update and stores results.
	 *  The update array is guaranteed to have at least one element, and for  
     *  all updates to have the same presentation context.
	 */	
	public void update(IChildrenUpdate[] updates);
	
	/**
	 * Updates whether elements have children.
	 * 
	 * @param updates Each update specifies an element to update and provides
	 *  a store for the result.  The update array is guaranteed to have at least 
	 *  one element, and for all updates to have the same presentation context.
	 */
	public void update(IHasChildrenUpdate[] updates);
	
}
