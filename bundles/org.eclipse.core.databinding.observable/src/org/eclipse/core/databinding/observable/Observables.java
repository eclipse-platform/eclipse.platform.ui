/*******************************************************************************
 * Copyright (c) 2006-2008 Cerner Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matt Carter - bug 212518 (constantObservableValue)
 *     Matthew Hall - bugs 208332, 212518, 219909, 184830
 *     Marko Topolnik - bug 184830
 ******************************************************************************/

package org.eclipse.core.databinding.observable;

import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.ObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.internal.databinding.observable.ConstantObservableValue;
import org.eclipse.core.internal.databinding.observable.EmptyObservableList;
import org.eclipse.core.internal.databinding.observable.EmptyObservableSet;
import org.eclipse.core.internal.databinding.observable.MapEntryObservableValue;
import org.eclipse.core.internal.databinding.observable.ProxyObservableList;
import org.eclipse.core.internal.databinding.observable.ProxyObservableSet;
import org.eclipse.core.internal.databinding.observable.StalenessObservableValue;
import org.eclipse.core.internal.databinding.observable.UnmodifiableObservableList;
import org.eclipse.core.internal.databinding.observable.UnmodifiableObservableSet;
import org.eclipse.core.internal.databinding.observable.UnmodifiableObservableValue;
import org.eclipse.core.runtime.Assert;

/**
 * Contains static methods to operate on or return
 * {@link IObservable Observables}.
 * 
 * @since 1.0
 */
public class Observables {
	/**
	 * Returns an unmodifiable observable value backed by the given observable
	 * value.
	 * 
	 * @param value
	 *            the value to wrap in an unmodifiable value
	 * @return an unmodifiable observable value backed by the given observable
	 *         value
	 * @since 1.1
	 */
	public static IObservableValue unmodifiableObservableValue(
			IObservableValue value) {
		Assert.isNotNull(value, "Argument 'value' cannot be null"); //$NON-NLS-1$
		return new UnmodifiableObservableValue(value);
	}

	/**
	 * Returns an observable value with the given constant value.
	 * 
	 * @param realm
	 *            the observable's realm
	 * @param value
	 *            the observable's constant value
	 * @param valueType
	 *            the observable's value type
	 * @return an immutable observable value with the given constant value
	 * @since 1.1
	 */
	public static IObservableValue constantObservableValue(Realm realm,
			Object value, Object valueType) {
		return new ConstantObservableValue(realm, value, valueType);
	}

	/**
	 * Returns an observable value with the given constant value.
	 * 
	 * @param realm
	 *            the observable's realm
	 * @param value
	 *            the observable's constant value
	 * @return an immutable observable value with the given constant value
	 * @since 1.1
	 */
	public static IObservableValue constantObservableValue(Realm realm,
			Object value) {
		return constantObservableValue(realm, value, null);
	}

	/**
	 * Returns an observable value with the given constant value.
	 * 
	 * @param value
	 *            the observable's constant value
	 * @param valueType
	 *            the observable's value type
	 * @return an immutable observable value with the given constant value
	 * @since 1.1
	 */
	public static IObservableValue constantObservableValue(Object value,
			Object valueType) {
		return constantObservableValue(Realm.getDefault(), value, valueType);
	}

	/**
	 * Returns an observable value with the given constant value.
	 * 
	 * @param value
	 *            the observable's constant value
	 * @return an immutable observable value with the given constant value
	 * @since 1.1
	 */
	public static IObservableValue constantObservableValue(Object value) {
		return constantObservableValue(Realm.getDefault(), value, null);
	}

	/**
	 * Returns an unmodifiable observable list backed by the given observable
	 * list.
	 * 
	 * @param list
	 *            the list to wrap in an unmodifiable list
	 * @return an unmodifiable observable list backed by the given observable
	 *         list
	 */
	public static IObservableList unmodifiableObservableList(
			IObservableList list) {
		if (list == null) {
			throw new IllegalArgumentException("List parameter cannot be null."); //$NON-NLS-1$
		}

		return new UnmodifiableObservableList(list);
	}

	/**
	 * Returns an unmodifiable observable set backed by the given observable
	 * set.
	 * 
	 * @param set
	 *            the set to wrap in an unmodifiable set
	 * @return an unmodifiable observable set backed by the given observable set
	 * @since 1.1
	 */
	public static IObservableSet unmodifiableObservableSet(IObservableSet set) {
		if (set == null) {
			throw new IllegalArgumentException("Set parameter cannot be null"); //$NON-NLS-1$
		}

		return new UnmodifiableObservableSet(set);
	}

	/**
	 * Returns an empty observable list. The returned list continues to work
	 * after it has been disposed of and can be disposed of multiple times.
	 * 
	 * @return an empty observable list.
	 */
	public static IObservableList emptyObservableList() {
		return emptyObservableList(Realm.getDefault(), null);
	}

	/**
	 * Returns an empty observable list of the given element type. The returned
	 * list continues to work after it has been disposed of and can be disposed
	 * of multiple times.
	 * 
	 * @param elementType
	 *            the element type of the returned list
	 * @return an empty observable list
	 * @since 1.1
	 */
	public static IObservableList emptyObservableList(Object elementType) {
		return emptyObservableList(Realm.getDefault(), elementType);
	}

	/**
	 * Returns an empty observable list belonging to the given realm. The
	 * returned list continues to work after it has been disposed of and can be
	 * disposed of multiple times.
	 * 
	 * @param realm
	 *            the realm of the returned list
	 * @return an empty observable list.
	 */
	public static IObservableList emptyObservableList(Realm realm) {
		return emptyObservableList(realm, null);
	}

	/**
	 * Returns an empty observable list of the given element type and belonging
	 * to the given realm. The returned list continues to work after it has been
	 * disposed of and can be disposed of multiple times.
	 * 
	 * @param realm
	 *            the realm of the returned list
	 * @param elementType
	 *            the element type of the returned list
	 * @return an empty observable list
	 * @since 1.1
	 */
	public static IObservableList emptyObservableList(Realm realm,
			Object elementType) {
		return new EmptyObservableList(realm, elementType);
	}

	/**
	 * Returns an empty observable set. The returned set continues to work after
	 * it has been disposed of and can be disposed of multiple times.
	 * 
	 * @return an empty observable set.
	 */
	public static IObservableSet emptyObservableSet() {
		return emptyObservableSet(Realm.getDefault(), null);
	}

	/**
	 * Returns an empty observable set of the given element type. The returned
	 * set continues to work after it has been disposed of and can be disposed
	 * of multiple times.
	 * 
	 * @param elementType
	 *            the element type of the returned set
	 * @return an empty observable set
	 * @since 1.1
	 */
	public static IObservableSet emptyObservableSet(Object elementType) {
		return emptyObservableSet(Realm.getDefault(), elementType);
	}

	/**
	 * Returns an empty observable set belonging to the given realm. The
	 * returned set continues to work after it has been disposed of and can be
	 * disposed of multiple times.
	 * 
	 * @param realm
	 *            the realm of the returned set
	 * @return an empty observable set.
	 */
	public static IObservableSet emptyObservableSet(Realm realm) {
		return emptyObservableSet(realm, null);
	}

	/**
	 * Returns an empty observable set of the given element type and belonging
	 * to the given realm. The returned set continues to work after it has been
	 * disposed of and can be disposed of multiple times.
	 * 
	 * @param realm
	 *            the realm of the returned set
	 * @param elementType
	 *            the element type of the returned set
	 * @return an empty observable set
	 * @since 1.1
	 */
	public static IObservableSet emptyObservableSet(Realm realm,
			Object elementType) {
		return new EmptyObservableSet(realm, elementType);
	}

	/**
	 * Returns an observable set backed by the given set.
	 * 
	 * @param set
	 *            the set to wrap in an IObservableSet
	 * @return an observable set backed by the given set
	 */
	public static IObservableSet staticObservableSet(Set set) {
		return staticObservableSet(Realm.getDefault(), set, Object.class);
	}

	/**
	 * Returns an observable set of the given element type, backed by the given
	 * set.
	 * 
	 * @param set
	 *            the set to wrap in an IObservableSet
	 * @param elementType
	 *            the element type of the returned set
	 * @return Returns an observable set backed by the given unchanging set
	 * @since 1.1
	 */
	public static IObservableSet staticObservableSet(Set set, Object elementType) {
		return staticObservableSet(Realm.getDefault(), set, elementType);
	}

	/**
	 * Returns an observable set belonging to the given realm, backed by the
	 * given set.
	 * 
	 * @param realm
	 *            the realm of the returned set
	 * @param set
	 *            the set to wrap in an IObservableSet
	 * @return an observable set backed by the given unchanging set
	 */
	public static IObservableSet staticObservableSet(Realm realm, Set set) {
		return staticObservableSet(realm, set, Object.class);
	}

	/**
	 * Returns an observable set of the given element type and belonging to the
	 * given realm, backed by the given set.
	 * 
	 * @param realm
	 *            the realm of the returned set
	 * @param set
	 *            the set to wrap in an IObservableSet
	 * @param elementType
	 *            the element type of the returned set
	 * @return an observable set backed by the given set
	 * @since 1.1
	 */
	public static IObservableSet staticObservableSet(Realm realm, Set set,
			Object elementType) {
		return new ObservableSet(realm, set, elementType) {
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
	 * @return a disposable proxy for the given observable set
	 */
	public static IObservableSet proxyObservableSet(IObservableSet target) {
		return new ProxyObservableSet(target);
	}

	/**
	 * Returns an observable list that contains the same elements as the given
	 * list, and fires the same events as the given list, but can be disposed of
	 * without disposing of the wrapped list.
	 * 
	 * @param target
	 *            the list to wrap
	 * @return a disposable proxy for the given observable list
	 * @since 1.1
	 */
	public static IObservableList proxyObservableList(IObservableList target) {
		return new ProxyObservableList(target);
	}

	/**
	 * Returns an observable list backed by the given list.
	 * 
	 * @param list
	 *            the list to wrap in an IObservableList
	 * @return an observable list backed by the given unchanging list
	 */
	public static IObservableList staticObservableList(List list) {
		return staticObservableList(Realm.getDefault(), list, Object.class);
	}

	/**
	 * Returns an observable list of the given element type, backed by the given
	 * list.
	 * 
	 * @param list
	 *            the list to wrap in an IObservableList
	 * @param elementType
	 *            the element type of the returned list
	 * @return an observable list backed by the given unchanging list
	 * @since 1.1
	 */
	public static IObservableList staticObservableList(List list,
			Object elementType) {
		return staticObservableList(Realm.getDefault(), list, elementType);
	}

	/**
	 * Returns an observable list belonging to the given realm, backed by the
	 * given list.
	 * 
	 * @param realm
	 *            the realm of the returned list
	 * @param list
	 *            the list to wrap in an IObservableList
	 * @return an observable list backed by the given unchanging list
	 */
	public static IObservableList staticObservableList(Realm realm, List list) {
		return staticObservableList(realm, list, Object.class);
	}

	/**
	 * Returns an observable list of the given element type and belonging to the
	 * given realm, backed by the given list.
	 * 
	 * @param realm
	 *            the realm of the returned list
	 * @param list
	 *            the list to wrap in an IObservableList
	 * @param elementType
	 *            the element type of the returned list
	 * @return an observable list backed by the given unchanging list
	 * @since 1.1
	 */
	public static IObservableList staticObservableList(Realm realm, List list,
			Object elementType) {
		return new ObservableList(realm, list, elementType) {
			public void addChangeListener(IChangeListener listener) {
			}

			public void addStaleListener(IStaleListener listener) {
			}

			public void addListChangeListener(IListChangeListener listener) {
			}
		};
	}

	/**
	 * Returns an observable value of type <code>Boolean.TYPE</code> which
	 * tracks whether the given observable is stale.
	 * 
	 * @param observable
	 *            the observable to track
	 * @return an observable value which tracks whether the given observable is
	 *         stale
	 * 
	 * @since 1.1
	 */
	public static IObservableValue observeStale(IObservable observable) {
		return new StalenessObservableValue(observable);
	}

	/**
	 * Returns an observable value that tracks changes to the value of an
	 * observable map's entry specified by its key.
	 * <p>
	 * The state where the key does not exist in the map is equivalent to the
	 * state where the key exists and its value is <code>null</code>. The
	 * transition between these two states is not considered a value change and
	 * no event is fired.
	 * 
	 * @param map
	 *            the observable map whose entry will be tracked.
	 * @param key
	 *            the key identifying the map entry to track.
	 * @param valueType
	 *            the type of the value. May be <code>null</code>, meaning
	 *            the value is untyped.
	 * @return an observable value that tracks the value associated with the
	 *         specified key in the given map
	 * @since 1.1
	 */
	public static IObservableValue observeMapEntry(IObservableMap map,
			Object key, Object valueType) {
		return new MapEntryObservableValue(map, key, valueType);
	}

	/**
	 * Returns a factory for creating obervable values tracking the value of the
	 * {@link IObservableMap observable map} entry identified by a particular
	 * key.
	 * 
	 * @param map
	 *            the observable map whose entry will be tracked.
	 * @param valueType
	 *            the type of the value. May be <code>null</code>, meaning
	 *            the value is untyped.
	 * @return a factory for creating observable values tracking the value of
	 *         the observable map entry identified by a particular key object.
	 * @since 1.1
	 */
	public static IObservableFactory mapEntryValueFactory(
			final IObservableMap map, final Object valueType) {
		return new IObservableFactory() {
			public IObservable createObservable(Object key) {
				return observeMapEntry(map, key, valueType);
			}
		};
	}

	/**
	 * Helper method for <code>MasterDetailObservables.detailValue(master,
	 * mapEntryValueFactory(map, valueType), valueType)</code>.
	 * 
	 * @param map
	 *            the observable map whose entry will be tracked.
	 * @param master
	 *            the observable value that identifies which map entry to track.
	 * @param valueType
	 *            the type of the value. May be <code>null</code>, meaning
	 *            the value is untyped.
	 * @return an observable value tracking the current value of the specified
	 *         key in the given map an observable value that tracks the current
	 *         value of the named property for the current value of the master
	 *         observable value
	 * @since 1.1
	 */
	public static IObservableValue observeDetailMapEntry(IObservableMap map,
			IObservableValue master, Object valueType) {
		return MasterDetailObservables.detailValue(master,
				mapEntryValueFactory(map, valueType), valueType);
	}
}
