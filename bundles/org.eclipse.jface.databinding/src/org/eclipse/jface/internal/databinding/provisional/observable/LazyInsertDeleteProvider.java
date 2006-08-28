/*******************************************************************************
 * Copyright (c) 2006 The Pampered Chef and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional.observable;

/**
 * An API for objects that can perform inserts and/or deletes in a collection
 * upon request.  This class supplies empty implementations of its methods and
 * is intended to be subclassed by clients.
 * 
 * @since 3.3
 */
public class LazyInsertDeleteProvider {
	/**
	 * Requests that the client insert a new element at the specified position.
	 * Clients are free to use or ignore the position (it's a hint) according
	 * to their own policy.
	 * 
	 * @param e the LazyInsertEvent TODO
	 * 
	 * @return An ILazyDataRequestor.NewObject containing the actual position
	 * where the object was inserted and the new object that was inserted, or
	 * null to indicate that no object was inserted.
	 */
	public ILazyDataRequestor.NewObject insertElementAt(LazyInsertEvent e) {
		return null;
	}
	
	/**
	 * Returns if the client can delete the object at the specified position.
	 * 
	 * @param e The position of the object to delete.
	 * @return true if the object can be deleted; false otherwise.
	 */
	public boolean canDeleteElementAt(LazyDeleteEvent e) {
		return false;
	}
	
	/**
	 * Requests that the client delete the object at the specified position.
	 * 
	 * @param e LazyInsertEvent TODO
	 */
	public void deleteElementAt(LazyDeleteEvent e) {
	}
}
