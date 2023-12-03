/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance.delegate;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;

/**
 * Delegate interface for an observable value.
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
public interface IObservableValueContractDelegate extends
		IObservableContractDelegate {

	/**
	 * Creates a new observable value.
	 *
	 * @param realm
	 *            realm of the observable
	 * @return observable value
	 */
	public IObservableValue<?> createObservableValue(Realm realm);

	/**
	 * Returns the expected type of the observable.
	 *
	 * @return type
	 */
	public Object getValueType(IObservableValue<?> observable);

	/**
	 * Returns a valid value that is not the current value of the observable.
	 *
	 * @return value
	 */
	public Object createValue(IObservableValue<?> observable);
}
