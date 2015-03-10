/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance.delegate;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;

/**
 * Delegate interface for observables.
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
public interface IObservableContractDelegate {
	/**
	 * Notifies the delegate of the start of a test.
	 */
	public void setUp();

	/**
	 * Notifies the delegate of the end of a test.
	 */
	public void tearDown();

	/**
	 * Invokes an operation to set the stale state of the provided
	 * <code>observable</code>.
	 *
	 * @param observable
	 * @param stale
	 */
	public void setStale(IObservable observable, boolean stale);

	/**
	 * Creates a new observable.
	 *
	 * @param realm realm of the observable
	 * @return observable
	 */
	public IObservable createObservable(Realm realm);

	/**
	 * Invokes a change operation resulting in a change event being fired from
	 * the observable. The preferred approach is to change the observed object
	 * resulting in an observable event. There is no guaranteed as to the state
	 * of the observable when invoked. The observable could be disposed to
	 * assert the proper behavior of dispose.
	 *
	 * @param observable
	 */
	public void change(IObservable observable);
}
