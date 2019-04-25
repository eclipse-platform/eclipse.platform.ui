/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance.delegate;

import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;

/**
 * Delegate interface for an IObservableCollection.
 *
 * <p>
 * This interface is not intended to be implemented by clients. Clients should
 * instead subclass one of the classes that implement this interface. Note that
 * direct implementers of this interface outside of the framework will be broken
 * in future releases when methods are added to this interface.
 * </p>
 *
 * @since 1.1
 */
public interface IObservableCollectionContractDelegate<E> extends
		IObservableContractDelegate {
	/**
	 * Creates a new observable collection with the provided
	 * <code>elementCount</code>.
	 *
	 * @param realm realm of the collection
	 * @param elementCount
	 *            number of elements to initialize the collection with
	 *
	 * @return new observable collection
	 */
	public IObservableCollection<E> createObservableCollection(Realm realm, int elementCount);

	/**
	 * Creates a new element of the appropriate type for the provided
	 * <code>collection</code>. This element will be employed to assert the
	 * addition and removal of elements in the collection.
	 *
	 * @param collection
	 * @return valid element for the collection
	 */
	public E createElement(IObservableCollection<E> collection);

	/**
	 * Returns the expected type of the elements in the collection.
	 *
	 * @param collection
	 * @return element type
	 */
	public Object getElementType(IObservableCollection<E> collection);
}
