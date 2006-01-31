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

/**
 * Represents a mapping from domain objects to cell values. The mapping will
 * fire a CHANGE_MANY event whenever one of the cell values changes for
 * a domain object. The getNewValue() in the ChangeEvent will be a Collection
 * of domain objects for which cell values have changed.
 * 
 * <p>
 * A function is only required to report CHANGE events for values in its domain
 * that it has previously been asked to evaluate. That is, an updatable function
 * will only fire a CHANGE event on an object that object was previously used as
 * the argument to computeResult. This is permits functions to optimize their
 * listeners. If the function is attaching a listener to each domain object, it
 * only needs to attach the listener the first time it is asked about that
 * object and it can remove the listener whenever it fires a CHANGE event for
 * the object.
 * </p>
 * 
 * Fires event types: CHANGE_MANY, STALE
 * 
 * <p>
 * Not intended to be implemented by clients. Clients should subclass
 * UpdatableFunction.
 * </p>
 * 
 * @see IUpdatableFunctionFactory
 * @since 3.2
 */
public interface IUpdatableCellProvider extends ICellProvider, IUpdatable {

	public IReadableSet getReadableSet();

}
