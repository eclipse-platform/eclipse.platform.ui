/*******************************************************************************
 * Copyright (c) 2006, 2015 Cerner Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matt Carter - bug 212518 (constantObservableValue)
 *     Matthew Hall - bugs 208332, 212518, 219909, 184830, 237718, 245647,
 *         226289
 *     Marko Topolnik - bug 184830
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.databinding.observable;

import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.observable.list.DecoratingObservableList;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.map.DecoratingObservableMap;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.set.DecoratingObservableSet;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.ObservableSet;
import org.eclipse.core.databinding.observable.value.DecoratingObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.IVetoableValue;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.ValueChangingEvent;
import org.eclipse.core.internal.databinding.observable.ConstantObservableValue;
import org.eclipse.core.internal.databinding.observable.DelayedObservableValue;
import org.eclipse.core.internal.databinding.observable.EmptyObservableList;
import org.eclipse.core.internal.databinding.observable.EmptyObservableSet;
import org.eclipse.core.internal.databinding.observable.MapEntryObservableValue;
import org.eclipse.core.internal.databinding.observable.StalenessObservableValue;
import org.eclipse.core.internal.databinding.observable.UnmodifiableObservableList;
import org.eclipse.core.internal.databinding.observable.UnmodifiableObservableMap;
import org.eclipse.core.internal.databinding.observable.UnmodifiableObservableSet;
import org.eclipse.core.internal.databinding.observable.UnmodifiableObservableValue;
import org.eclipse.core.runtime.Assert;

/**
 * Contains static methods to operate on or return {@link IObservable
 * Observables}.
 *
 * @since 1.0
 */
public class Observables {
	/**
	 * Returns an observable which delays notification of value change events
	 * from <code>observable</code> until <code>delay</code> milliseconds have
	 * elapsed since the last change event. This observable helps to boost
	 * performance in situations where an observable has computationally
	 * expensive listeners or many dependencies. A common use of this observable
	 * is to delay validation of user input until the user stops typing in a UI
	 * field.
	 * <p>
	 * To notify about pending changes, the returned observable fires a stale
	 * event when the wrapped observable value fires a change event, and remains
	 * stale until the delay has elapsed and the value change is fired. A call
	 * to {@link IObservableValue#getValue() getValue()} while a value change is
	 * pending will fire the value change immediately, short-circuiting the
	 * delay.
	 * <p>
	 * <b>Note:</b>
	 * <ul>
	 * <li>Use SWTObservables.observeDelayedValue() instead when the target
	 * observable is observing a SWT Control, or
	 * ViewersObservables.observeDelayedValue() when the target observable is
	 * observing a JFace Viewer. These observables ensure that pending value
	 * changes are fired when the underlying control loses focus. (Otherwise, it
	 * is possible for pending changes to be lost if a window is closed before
	 * the delay has elapsed.)
	 * <li>This observable does not forward {@link ValueChangingEvent} events
	 * from a wrapped {@link IVetoableValue}.
	 * </ul>
	 *
	 * @param <T>
	 *            the value type
	 *
	 * @param delay
	 *            the delay in milliseconds
	 * @param observable
	 *            the observable being delayed
	 * @return an observable which delays notification of value change events
	 *         from <code>observable</code> until <code>delay</code>
	 *         milliseconds have elapsed since the last change event.
	 *
	 * @since 1.2
	 */
	public static <T> IObservableValue<T> observeDelayedValue(int delay,
			IObservableValue<T> observable) {
		return new DelayedObservableValue<T>(delay, observable);
	}

	/**
	 * Returns an unmodifiable observable value backed by the given observable
	 * value.
	 *
	 * @param <T>
	 *            the value type
	 * @param value
	 *            the value to wrap in an unmodifiable value
	 * @return an unmodifiable observable value backed by the given observable
	 *         value
	 * @since 1.1
	 */
	public static <T> IObservableValue<T> unmodifiableObservableValue(
			IObservableValue<T> value) {
		Assert.isNotNull(value, "Argument 'value' cannot be null"); //$NON-NLS-1$
		return new UnmodifiableObservableValue<T>(value);
	}

	/**
	 * Returns an observable value with the given constant value.
	 *
	 * @param <T>
	 *            the value type
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
	public static <T> IObservableValue<T> constantObservableValue(Realm realm,
			T value, Object valueType) {
		return new ConstantObservableValue<T>(realm, value, valueType);
	}

	/**
	 * Returns an observable value with the given constant value.
	 *
	 * @param <T>
	 *
	 * @param realm
	 *            the observable's realm
	 * @param value
	 *            the observable's constant value
	 * @return an immutable observable value with the given constant value
	 * @since 1.1
	 */
	public static <T> IObservableValue<T> constantObservableValue(Realm realm,
			T value) {
		return constantObservableValue(realm, value, null);
	}

	/**
	 * Returns an observable value with the given constant value.
	 *
	 * @param <T>
	 *            the value type
	 *
	 * @param value
	 *            the observable's constant value
	 * @param valueType
	 *            the observable's value type
	 * @return an immutable observable value with the given constant value
	 * @since 1.1
	 */
	public static <T> IObservableValue<T> constantObservableValue(T value,
			T valueType) {
		return constantObservableValue(Realm.getDefault(), value, valueType);
	}

	/**
	 * Returns an observable value with the given constant value.
	 *
	 * @param <T>
	 *
	 * @param value
	 *            the observable's constant value
	 * @return an immutable observable value with the given constant value
	 * @since 1.1
	 */
	public static <T> IObservableValue<T> constantObservableValue(T value) {
		return constantObservableValue(Realm.getDefault(), value, null);
	}

	/**
	 * Returns an unmodifiable observable list backed by the given observable
	 * list.
	 *
	 * @param <E>
	 *            the element type
	 *
	 * @param list
	 *            the list to wrap in an unmodifiable list
	 * @return an unmodifiable observable list backed by the given observable
	 *         list
	 */
	public static <E> IObservableList<E> unmodifiableObservableList(
			IObservableList<E> list) {
		if (list == null) {
			throw new IllegalArgumentException("List parameter cannot be null."); //$NON-NLS-1$
		}

		return new UnmodifiableObservableList<E>(list);
	}

	/**
	 * Returns an unmodifiable observable set backed by the given observable
	 * set.
	 *
	 * @param <E>
	 *
	 * @param set
	 *            the set to wrap in an unmodifiable set
	 * @return an unmodifiable observable set backed by the given observable set
	 * @since 1.1
	 */
	public static <E> IObservableSet<E> unmodifiableObservableSet(
			IObservableSet<E> set) {
		if (set == null) {
			throw new IllegalArgumentException("Set parameter cannot be null"); //$NON-NLS-1$
		}

		return new UnmodifiableObservableSet<E>(set);
	}

	/**
	 * Returns an unmodifiable observable map backed by the given observable
	 * map.
	 *
	 * @param <K>
	 *            map key type
	 * @param <V>
	 *            map value type
	 *
	 * @param map
	 *            the map to wrap in an unmodifiable map
	 * @return an unmodifiable observable map backed by the given observable
	 *         map.
	 * @since 1.2
	 */
	public static <K, V> IObservableMap<K, V> unmodifiableObservableMap(
			IObservableMap<K, V> map) {
		if (map == null) {
			throw new IllegalArgumentException("Map parameter cannot be null"); //$NON-NLS-1$
		}

		return new UnmodifiableObservableMap<K, V>(map);
	}

	/**
	 * Returns an empty observable list. The returned list continues to work
	 * after it has been disposed of and can be disposed of multiple times.
	 *
	 * @param <E>
	 *            the list element type
	 *
	 * @return an empty observable list.
	 */
	public static <E> IObservableList<E> emptyObservableList() {
		return emptyObservableList(Realm.getDefault(), null);
	}

	/**
	 * Returns an empty observable list of the given element type. The returned
	 * list continues to work after it has been disposed of and can be disposed
	 * of multiple times.
	 *
	 * @param <E>
	 *            the list element type
	 *
	 * @param elementType
	 *            the element type of the returned list
	 * @return an empty observable list
	 * @since 1.1
	 */
	public static <E> IObservableList<E> emptyObservableList(Object elementType) {
		return emptyObservableList(Realm.getDefault(), elementType);
	}

	/**
	 * Returns an empty observable list belonging to the given realm. The
	 * returned list continues to work after it has been disposed of and can be
	 * disposed of multiple times.
	 *
	 * @param <E>
	 *            the list element type
	 *
	 * @param realm
	 *            the realm of the returned list
	 * @return an empty observable list.
	 */
	public static <E> IObservableList<E> emptyObservableList(Realm realm) {
		return emptyObservableList(realm, null);
	}

	/**
	 * Returns an empty observable list of the given element type and belonging
	 * to the given realm. The returned list continues to work after it has been
	 * disposed of and can be disposed of multiple times.
	 *
	 * @param <E>
	 *            the list element type
	 *
	 * @param realm
	 *            the realm of the returned list
	 * @param elementType
	 *            the element type of the returned list
	 * @return an empty observable list
	 * @since 1.1
	 */
	public static <E> IObservableList<E> emptyObservableList(Realm realm,
			Object elementType) {
		return new EmptyObservableList<E>(realm, elementType);
	}

	/**
	 * Returns an empty observable set. The returned set continues to work after
	 * it has been disposed of and can be disposed of multiple times.
	 *
	 * @param <E>
	 *            the set element type
	 *
	 * @return an empty observable set.
	 */
	public static <E> IObservableSet<E> emptyObservableSet() {
		return emptyObservableSet(Realm.getDefault(), null);
	}

	/**
	 * Returns an empty observable set of the given element type. The returned
	 * set continues to work after it has been disposed of and can be disposed
	 * of multiple times.
	 *
	 * @param <E>
	 *            the set element type
	 *
	 * @param elementType
	 *            the element type of the returned set
	 * @return an empty observable set
	 * @since 1.1
	 */
	public static <E> IObservableSet<E> emptyObservableSet(Object elementType) {
		return emptyObservableSet(Realm.getDefault(), elementType);
	}

	/**
	 * Returns an empty observable set belonging to the given realm. The
	 * returned set continues to work after it has been disposed of and can be
	 * disposed of multiple times.
	 *
	 * @param <E>
	 *            the set element type
	 *
	 * @param realm
	 *            the realm of the returned set
	 * @return an empty observable set.
	 */
	public static <E> IObservableSet<E> emptyObservableSet(Realm realm) {
		return emptyObservableSet(realm, null);
	}

	/**
	 * Returns an empty observable set of the given element type and belonging
	 * to the given realm. The returned set continues to work after it has been
	 * disposed of and can be disposed of multiple times.
	 *
	 * @param <E>
	 *            the set element type
	 *
	 * @param realm
	 *            the realm of the returned set
	 * @param elementType
	 *            the element type of the returned set
	 * @return an empty observable set
	 * @since 1.1
	 */
	public static <E> IObservableSet<E> emptyObservableSet(Realm realm,
			Object elementType) {
		return new EmptyObservableSet<E>(realm, elementType);
	}

	/**
	 * Returns an observable set backed by the given set.
	 *
	 * @param <E>
	 *            the set element type
	 *
	 * @param set
	 *            the set to wrap in an IObservableSet
	 * @return an observable set backed by the given set
	 */
	public static <E> IObservableSet<E> staticObservableSet(Set<E> set) {
		return staticObservableSet(Realm.getDefault(), set, Object.class);
	}

	/**
	 * Returns an observable set of the given element type, backed by the given
	 * set.
	 *
	 * @param <E>
	 *            the set element type
	 *
	 * @param set
	 *            the set to wrap in an IObservableSet
	 * @param elementType
	 *            the element type of the returned set
	 * @return Returns an observable set backed by the given unchanging set
	 * @since 1.1
	 */
	public static <E> IObservableSet<E> staticObservableSet(Set<E> set,
			Object elementType) {
		return staticObservableSet(Realm.getDefault(), set, elementType);
	}

	/**
	 * Returns an observable set belonging to the given realm, backed by the
	 * given set.
	 *
	 * @param <E>
	 *
	 * @param realm
	 *            the realm of the returned set
	 * @param set
	 *            the set to wrap in an IObservableSet
	 * @return an observable set backed by the given unchanging set
	 */
	public static <E> IObservableSet<E> staticObservableSet(Realm realm,
			Set<E> set) {
		return staticObservableSet(realm, set, Object.class);
	}

	/**
	 * Returns an observable set of the given element type and belonging to the
	 * given realm, backed by the given set.
	 *
	 * @param <E>
	 *            the set element type
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
	public static <E> IObservableSet<E> staticObservableSet(Realm realm,
			Set<E> set, Object elementType) {
		return new ObservableSet<E>(realm, set, elementType) {
			@Override
			public synchronized void addChangeListener(IChangeListener listener) {
			}

			@Override
			public synchronized void addStaleListener(IStaleListener listener) {
			}

			@Override
			public synchronized void addSetChangeListener(
					ISetChangeListener<? super E> listener) {
			}
		};
	}

	/**
	 * Returns an observable value that contains the same value as the given
	 * observable, and fires the same events as the given observable, but can be
	 * disposed of without disposing of the wrapped observable.
	 *
	 * @param <T>
	 *            the value type
	 *
	 * @param target
	 *            the observable value to wrap
	 * @return a disposable proxy for the given observable value.
	 * @since 1.2
	 */
	public static <T> IObservableValue<T> proxyObservableValue(
			IObservableValue<T> target) {
		return new DecoratingObservableValue<T>(target, false);
	}

	/**
	 * Returns an observable set that contains the same elements as the given
	 * set, and fires the same events as the given set, but can be disposed of
	 * without disposing of the wrapped set.
	 *
	 * @param <E>
	 *            the set element type
	 *
	 * @param target
	 *            the set to wrap
	 * @return a disposable proxy for the given observable set
	 */
	public static <E> IObservableSet<E> proxyObservableSet(
			IObservableSet<E> target) {
		return new DecoratingObservableSet<E>(target, false);
	}

	/**
	 * Returns an observable list that contains the same elements as the given
	 * list, and fires the same events as the given list, but can be disposed of
	 * without disposing of the wrapped list.
	 *
	 * @param <E>
	 *            the list element type
	 *
	 * @param target
	 *            the list to wrap
	 * @return a disposable proxy for the given observable list
	 * @since 1.1
	 */
	public static <E> IObservableList<E> proxyObservableList(
			IObservableList<E> target) {
		return new DecoratingObservableList<E>(target, false);
	}

	/**
	 * Returns an observable map that contains the same entries as the given
	 * map, and fires the same events as the given map, but can be disposed of
	 * without disposing of the wrapped map.
	 *
	 * @param <K>
	 *            the map key type
	 * @param <V>
	 *            the map value type
	 *
	 * @param target
	 *            the map to wrap
	 * @return a disposable proxy for the given observable map
	 * @since 1.2
	 */
	public static <K, V> IObservableMap<K, V> proxyObservableMap(
			IObservableMap<K, V> target) {
		return new DecoratingObservableMap<K, V>(target, false);
	}

	/**
	 * Returns an observable list backed by the given list.
	 *
	 * @param <E>
	 *            the list element type
	 *
	 * @param list
	 *            the list to wrap in an IObservableList
	 * @return an observable list backed by the given unchanging list
	 */
	public static <E> IObservableList<E> staticObservableList(List<E> list) {
		return staticObservableList(Realm.getDefault(), list, Object.class);
	}

	/**
	 * Returns an observable list of the given element type, backed by the given
	 * list.
	 *
	 * @param <E>
	 *            the list element type
	 *
	 * @param list
	 *            the list to wrap in an IObservableList
	 * @param elementType
	 *            the element type of the returned list
	 * @return an observable list backed by the given unchanging list
	 * @since 1.1
	 */
	public static <E> IObservableList<E> staticObservableList(List<E> list,
			Object elementType) {
		return staticObservableList(Realm.getDefault(), list, elementType);
	}

	/**
	 * Returns an observable list belonging to the given realm, backed by the
	 * given list.
	 *
	 * @param <E>
	 *            the list element type
	 *
	 * @param realm
	 *            the realm of the returned list
	 * @param list
	 *            the list to wrap in an IObservableList
	 * @return an observable list backed by the given unchanging list
	 */
	public static <E> IObservableList<E> staticObservableList(Realm realm,
			List<E> list) {
		return staticObservableList(realm, list, Object.class);
	}

	/**
	 * Returns an observable list of the given element type and belonging to the
	 * given realm, backed by the given list.
	 *
	 * @param <E>
	 *            the list element type
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
	public static <E> IObservableList<E> staticObservableList(Realm realm,
			List<E> list, Object elementType) {
		return new ObservableList<E>(realm, list, elementType) {
			@Override
			public synchronized void addChangeListener(IChangeListener listener) {
			}

			@Override
			public synchronized void addStaleListener(IStaleListener listener) {
			}

			@Override
			public synchronized void addListChangeListener(
					IListChangeListener<? super E> listener) {
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
	public static IObservableValue<Boolean> observeStale(IObservable observable) {
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
	 * @param <K>
	 *            the map key type
	 * @param <V>
	 *            the map value type
	 *
	 * @param map
	 *            the observable map whose entry will be tracked.
	 * @param key
	 *            the key identifying the map entry to track.
	 * @return an observable value that tracks the value associated with the
	 *         specified key in the given map
	 * @since 1.2
	 */
	public static <K, V> IObservableValue<V> observeMapEntry(
			IObservableMap<K, V> map, K key) {
		return observeMapEntry(map, key, map.getValueType());
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
	 * @param <K>
	 *            the map key type
	 * @param <V>
	 *            the map value type
	 *
	 * @param map
	 *            the observable map whose entry will be tracked.
	 * @param key
	 *            the key identifying the map entry to track.
	 * @param valueType
	 *            the type of the value. May be <code>null</code>, meaning the
	 *            value is untyped.
	 * @return an observable value that tracks the value associated with the
	 *         specified key in the given map
	 * @since 1.1
	 */
	public static <K, V> IObservableValue<V> observeMapEntry(
			IObservableMap<K, V> map, K key, Object valueType) {
		if (valueType == null)
			valueType = map.getValueType();
		return new MapEntryObservableValue<K, V>(map, key, valueType);
	}

	/**
	 * Returns a factory for creating observable values tracking the value of
	 * the {@link IObservableMap observable map} entry identified by a
	 * particular key.
	 *
	 * @param <K>
	 *            the map key type
	 * @param <V>
	 *            the map value type
	 *
	 * @param map
	 *            the observable map whose entry will be tracked.
	 * @param valueType
	 *            the type of the value. May be <code>null</code>, meaning the
	 *            value is untyped.
	 * @return a factory for creating observable values tracking the value of
	 *         the observable map entry identified by a particular key object.
	 * @since 1.1
	 */
	public static <K, V> IObservableFactory<K, IObservableValue<V>> mapEntryValueFactory(
			final IObservableMap<K, V> map, final Object valueType) {
		return new IObservableFactory<K, IObservableValue<V>>() {
			@Override
			public IObservableValue<V> createObservable(K key) {
				return observeMapEntry(map, key, valueType);
			}
		};
	}

	/**
	 * Helper method for <code>MasterDetailObservables.detailValue(master,
	 * mapEntryValueFactory(map, valueType), valueType)</code>.
	 *
	 * @param <K>
	 *            the map key type
	 * @param <V>
	 *            the map value type
	 *
	 * @param map
	 *            the observable map whose entry will be tracked.
	 * @param master
	 *            the observable value that identifies which map entry to track.
	 * @param valueType
	 *            the type of the value. May be <code>null</code>, meaning the
	 *            value is untyped.
	 * @return an observable value tracking the current value of the specified
	 *         key in the given map an observable value that tracks the current
	 *         value of the named property for the current value of the master
	 *         observable value
	 * @since 1.1
	 */
	public static <K, V> IObservableValue<V> observeDetailMapEntry(
			IObservableMap<K, V> map, IObservableValue<K> master,
			Object valueType) {
		return MasterDetailObservables.detailValue(master,
				mapEntryValueFactory(map, valueType), valueType);
	}

	/**
	 * Copies the current value of the source observable to the destination
	 * observable, and upon value change events fired by the source observable,
	 * updates the destination observable accordingly, until the source
	 * observable is disposed. This method assumes that both observables are on
	 * the same realm.
	 *
	 * @param <T>
	 *            the value type
	 *
	 * @param source
	 *            the source observable
	 * @param destination
	 *            the destination observable
	 * @since 1.2
	 */
	public static <T> void pipe(IObservableValue<T> source,
			final IObservableValue<? super T> destination) {
		destination.setValue(source.getValue());
		source.addValueChangeListener(new IValueChangeListener<T>() {
			@Override
			public void handleValueChange(ValueChangeEvent<? extends T> event) {
				destination.setValue(event.diff.getNewValue());
			}
		});
	}
}
