/*******************************************************************************
 * Copyright (c) 2006 Cerner Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding.observable;

import java.util.Set;

import org.eclipse.jface.databinding.observable.list.IObservableList;
import org.eclipse.jface.databinding.observable.set.IObservableSet;
import org.eclipse.jface.databinding.observable.set.ObservableSet;
import org.eclipse.jface.internal.databinding.internal.observable.ProxyObservableSet;
import org.eclipse.jface.internal.databinding.internal.observable.UnmodifiableObservableList;

/**
 * Contains static methods to operate on or return
 * {@link IObservable Observables}.
 * 
 * @since 3.2
 */
public class Observables {
	/**
	 * @param list
	 * @return list Returns an unmodifiable view of the provided <code>list</code>.
	 */
	public static IObservableList unmodifiableObservableList(IObservableList list) {
		if (list == null) {
			throw new IllegalArgumentException(
					"Parameter " + list + " was null."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return new UnmodifiableObservableList(list);
	}

	/**
	 * @param set
	 * @return Returns an observableSet backed by the given set
	 */
	public static IObservableSet staticObservableSet(Set set) {
		return new ObservableSet(set, Object.class) {
			public void addChangeListener(IChangeListener listener) {
			}

			public void addStaleListener(IStaleListener listener) {
			}
		};
	}

	/**
	 * Returns an observable set that contains the same elements as the given
	 * set, and fires the same events as the given set, but can be disposed of
	 * without disposing of the wrapped set.
	 * 
	 * @param target the set to wrap
	 * @return a proxy observable set
	 */
	public static IObservableSet proxyObservableSet(IObservableSet target) {
		return new ProxyObservableSet(target);
	}
}
