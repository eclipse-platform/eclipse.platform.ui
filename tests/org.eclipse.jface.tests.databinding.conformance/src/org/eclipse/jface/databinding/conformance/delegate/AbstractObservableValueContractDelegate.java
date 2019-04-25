/*******************************************************************************
 * Copyright (c) 2007, 2014 Brad Reynolds and others.
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
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 444829
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance.delegate;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;

/**
 * Abstract implementation of {@link IObservableValueContractDelegate}.
 *
 * @since 1.1
 */
public abstract class AbstractObservableValueContractDelegate extends
		AbstractObservableContractDelegate implements
		IObservableValueContractDelegate {

	/**
	 * Invokes {@link #createObservableValue(Realm)}.
	 *
	 * @param realm
	 * @return observable
	 */
	@Override
	public final IObservable createObservable(Realm realm) {
		return createObservableValue(realm);
	}

	/**
	 * Default implementation returns <code>null</code>.
	 *
	 * @param observable
	 * @return value type
	 */
	@Override
	public Object getValueType(IObservableValue<?> observable) {
		// no op
		return null;
	}

	/**
	 * Default implementation returns <code>null</code>.
	 *
	 * @param observable
	 * @return value
	 */
	@Override
	public Object createValue(IObservableValue<?> observable) {
		// no op
		return null;
	}
}
