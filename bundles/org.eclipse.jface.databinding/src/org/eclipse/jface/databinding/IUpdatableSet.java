/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding;

import java.util.Collection;

/**
 * @since 3.2
 *
 */
public interface IUpdatableSet extends IReadableSet, IUpdatable {

	/**
	 * Clears the elements in the reciever 
	 */
	public void clear();

	/**
	 * Adds the given element to the set. Has no effect if the set already contains the element.
	 * 
	 * @param toAdd element to add
	 */
	public void add(Object toAdd);

	/**
	 * Removes the given element from the set. Has no effect if the set does not contain the
	 * element
	 * 
	 * @param toRemove element to remove
	 */
	public void remove(Object toRemove);

	/**
	 * Adds the given elements to the reciever. Elements already contained in the reciever are ignored.
	 * 
	 * @param toAdd elements to add
	 */
	public void addAll(Collection toAdd);

	/**
	 * Removes the given elements from the reciever. Elements not in the reciever are ignored.
	 * 
	 * @param toAdd elements to add
	 */
	public void removeAll(Collection toRemove);

}