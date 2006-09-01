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

package org.eclipse.jface.internal.databinding.provisional.observable.mapping;

import java.util.Set;

import org.eclipse.jface.databinding.observable.set.IObservableSet;

/**
 * A bidirectional mapping whose changes can be tracked by a mapping change listener.
 * 
 * <p>
 * This interface is not intended to be implemented by clients. Clients should
 * instead subclass one of the classes that implement this interface. Note that
 * direct implementers of this interface outside of the framework will be broken
 * in future releases when methods are added to this interface.
 * </p>
 * 
 * @since 1.0
 * 
 */
public interface IBidirectionalMapping extends IObservableMapping {

	/**
	 * Returns the current set of actual results of the function. Callers can
	 * register listeners on this set to respond to changes in the range of the
	 * function.
	 * 
	 * @return the set of possible results of the function.
	 */
	public IObservableSet getRange();

	/**
	 * Returns the set of elements in the domain that map onto the given value
	 * from the range. That is, it returns the set of objects that you can pass
	 * to getFunctionValue that will end up returning the given value.
	 * 
	 * @param value
	 *            element from the range
	 * @return collection of all elements in the d
	 */
	public Set getDomainElementsForValue(Object value);

}
