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

package org.eclipse.core.databinding.observable;

import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.ObservableSet;
import org.eclipse.core.internal.databinding.observable.EmptyObservableList;
import org.eclipse.core.internal.databinding.observable.EmptyObservableSet;
import org.eclipse.core.internal.databinding.observable.ProxyObservableSet;
import org.eclipse.core.internal.databinding.observable.UnmodifiableObservableList;

/**
 * Contains static methods to operate on or return
 * {@link IObservable Observables}.
 * 
 * @since 3.2
 */
public class Observables {
	/**
	 * @param list
	 * @return list Returns an unmodifiable view of the provided
	 *         <code>list</code>.
	 */
	public static IObservableList unmodifiableObservableList(
			IObservableList list) {
		if (list == null) {
			throw new IllegalArgumentException(
					"Parameter " + list + " was null."); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return new UnmodifiableObservableList(list);
	}

	/**
	 * Returns an empty observable list. The returned list continues to work
	 * after it has been disposed of and can be disposed of multiple times.
	 * 
	 * @return an empty observable list.
	 */
	public static IObservableList emptyObservableList() {
		return new EmptyObservableList(Realm.getDefault());
	}

	/**
	 * Returns an empty observable list. The returned list continues to work
	 * after it has been disposed of and can be disposed of multiple times.
	 * 
	 * @param realm
	 * @return an empty observable list.
	 */
	public static IObservableList emptyObservableList(Realm realm) {
		return new EmptyObservableList(realm);
	}

	/**
	 * Returns an empty observable set. The returned set continues to work after
	 * it has been disposed of and can be disposed of multiple times.
	 * 
	 * @param realm
	 * @return an empty observable set.
	 */
	public static IObservableSet emptyObservableSet() {
		return new EmptyObservableSet(Realm.getDefault());
	}

	/**
	 * Returns an empty observable set. The returned set continues to work after
	 * it has been disposed of and can be disposed of multiple times.
	 * 
	 * @param realm
	 * @return an empty observable set.
	 */
	public static IObservableSet emptyObservableSet(Realm realm) {
		return new EmptyObservableSet(realm);
	}

	/**
	 * @param realm
	 * @param set
	 * @return Returns an observableSet backed by the given set
	 */
	public static IObservableSet staticObservableSet(Set set) {
		return new ObservableSet(Realm.getDefault(), set, Object.class) {
			public void addChangeListener(IChangeListener listener) {
			}

			public void addStaleListener(IStaleListener listener) {
			}

			public void addSetChangeListener(ISetChangeListener listener) {
			}
		};
	}

	/**
	 * @param realm
	 * @param set
	 * @return Returns an observableSet backed by the given set
	 */
	public static IObservableSet staticObservableSet(Realm realm, Set set) {
		return new ObservableSet(realm, set, Object.class) {
			public void addChangeListener(IChangeListener listener) {
			}

			public void addStaleListener(IStaleListener listener) {
			}

			public void addSetChangeListener(ISetChangeListener listener) {
			}
		};
	}

	/**
	 * Returns an observable set that contains the same elements as the given
	 * set, and fires the same events as the given set, but can be disposed of
	 * without disposing of the wrapped set.
	 * 
	 * @param target
	 *            the set to wrap
	 * @return a proxy observable set
	 */
	public static IObservableSet proxyObservableSet(IObservableSet target) {
		return new ProxyObservableSet(target);
	}

	/**
	 * @param realm
	 * @param list
	 * @return an observable list that never fires events
	 */
	public static IObservableList staticObservableList(List list) {
		return staticObservableList(Realm.getDefault(), list);
	}

	/**
	 * @param realm
	 * @param list
	 * @return an observable list that never fires events
	 */
	public static IObservableList staticObservableList(Realm realm, List list) {
		return new ObservableList(realm, list, Object.class) {
			public void addChangeListener(IChangeListener listener) {
			}

			public void addStaleListener(IStaleListener listener) {
			}

			public void addListChangeListener(IListChangeListener listener) {
			}
		};
	}
}
