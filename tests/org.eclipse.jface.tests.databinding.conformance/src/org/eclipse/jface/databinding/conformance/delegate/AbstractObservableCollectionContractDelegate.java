/*******************************************************************************
 * Copyright (c) 2007, 2014 IBM Corporation and others.
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
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 444829
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance.delegate;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;

/**
 * Abstract implementation of {@link IObservableCollectionContractDelegate}.
 *
 * @since 3.2
 */
public abstract class AbstractObservableCollectionContractDelegate<E> extends AbstractObservableContractDelegate
		implements IObservableCollectionContractDelegate<E> {

	/**
	 * Invokes
	 * {@link IObservableCollectionContractDelegate#createObservableCollection(Realm, int)}
	 * .
	 *
	 * @param realm
	 * @return observable
	 */
	@Override
	public final IObservable createObservable(Realm realm) {
		return createObservableCollection(realm, 0);
	}

	@Override
	public E createElement(IObservableCollection<E> collection) {
		// no op
		return null;
	}

	@Override
	public Object getElementType(IObservableCollection<E> collection) {
		// no op
		return null;
	}
}
