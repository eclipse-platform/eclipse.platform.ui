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
 * Marker interface for Observables that request data lazily.
 * 
 * @since 3.3
 */
public interface ILazyDataRequestor extends IObservable {
	/**
	 * Represents a new object that was inserted into a lazy collection.
	 * @since 3.3
	 */
	public static class NewObject {
		/**
		 * The position where the insert actually occurred
		 */
		public int position;
		
		/**
		 * The object that was inserted
		 */
		public Object it;
		
		/**
		 * @param position The position where the insert actually occurred
		 * @param it The object that was inserted
		 */
		public NewObject(int position, Object it) {
			this.position = position;
			this.it = it;
		}
	}
	
	/**
	 * Sets the number of elements in the entire list that we are browsing.
	 * 
	 * @param size The new size to set.
	 */
	void setSize(int size);
	
	/**
	 * Adds the listener to the set of listeners that will be invoked when
	 * a method in the ILazyElementListener interface is invoked.
	 * 
	 * @param p The listener to add.
	 */
	void addElementProvider(ILazyListElementProvider p);
	
	/**
	 * Removes the listener from the set of listeners that will be invoked when
	 * a method in the ILazyElementListener interface is invoked.
	 * 
	 * @param p The listener to remove.
	 */
	void removeElementProvider(ILazyListElementProvider p);
	
	/**
	 * Adds the listener to the set of listeners that will be invoked when
	 * a method in the LazyInsertDeleteProvider interface is invoked.
	 * 
	 * @param p The listener to add.
	 */
	void addInsertDeleteProvider(LazyInsertDeleteProvider p);
	
	/**
	 * Removes the listener from the set of listeners that will be invoked when
	 * a method in the LazyInsertDeleteProvider interface is invoked.
	 * 
	 * @param p The listener to remove.
	 */
	void removeInsertDeleteProvider(LazyInsertDeleteProvider p);

	/**
	 * @param position
	 * @param element
	 */
	void add(int position, Object element);

	/**
	 * @param position
	 */
	Object remove(int position);
}
