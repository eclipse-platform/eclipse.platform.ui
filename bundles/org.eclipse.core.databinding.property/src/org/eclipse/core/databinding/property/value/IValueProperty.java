/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 195222
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.databinding.property.value;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.map.IMapProperty;
import org.eclipse.core.databinding.property.set.ISetProperty;

/**
 * Interface for value-typed properties
 *
 * @param <S>
 *            type of the source object
 * @param <T>
 *            type of the value of the property
 *
 * @since 1.2
 * @noimplement This interface is not intended to be implemented by clients.
 *              Clients should instead subclass one of the classes that
 *              implement this interface. Note that direct implementers of this
 *              interface outside of the framework will be broken in future
 *              releases when methods are added to this interface.
 * @see ValueProperty
 * @see SimpleValueProperty
 */
public interface IValueProperty<S, T> extends IProperty {
	/**
	 * Returns the value type of the property, or <code>null</code> if untyped.
	 *
	 * @return the value type of the property, or <code>null</code> if untyped.
	 */
	public Object getValueType();

	/**
	 * Returns the current value of this property on the specified property
	 * source.
	 *
	 * @param source
	 *            the property source (may be null)
	 * @return the current value of this property on the specified property
	 *         source.
	 * @since 1.3
	 */
	public T getValue(S source);

	/**
	 * Sets this property on the specified property source to the specified
	 * value.
	 * <p>
	 * <b>Note:</b> This method is made available to facilitate basic property
	 * access. However if the property source lacks property change
	 * notification, then observables on the source object may not be notified
	 * of the change. In most cases it is preferable to call
	 * {@link IObservableValue#setValue(Object)} on the observable instead.
	 *
	 * @param source
	 *            the property source (may be null)
	 * @param value
	 *            the new property value
	 * @since 1.3
	 */
	public void setValue(S source, T value);

	/**
	 * Returns an observable value observing this value property on the given
	 * property source.
	 *
	 * @param source
	 *            the property source
	 * @return an observable value observing this value property on the given
	 *         property source
	 */
	public IObservableValue<T> observe(S source);

	/**
	 * Returns an observable value observing this value property on the given
	 * property source
	 *
	 * @param realm
	 *            the observable's realm
	 * @param source
	 *            the property source
	 * @return an observable value observing this value property on the given
	 *         property source
	 */
	public IObservableValue<T> observe(Realm realm, S source);

	/**
	 * Returns a factory for creating observable values tracking this property
	 * of a particular property source.
	 *
	 * @return a factory for creating observable values tracking this property
	 *         of a particular property source.
	 */
	public IObservableFactory<S, IObservableValue<T>> valueFactory();

	/**
	 * Returns a factory for creating observable values in the given realm,
	 * tracking this property of a particular property source.
	 *
	 * @param realm
	 *            the realm
	 *
	 * @return a factory for creating observable values in the given realm,
	 *         tracking this property of a particular property source.
	 */
	public IObservableFactory<S, IObservableValue<T>> valueFactory(Realm realm);

	/**
	 * Returns an observable value on the master observable's realm which tracks
	 * this property on the current value of <code>master</code>.
	 *
	 * @param master
	 *            the master observable
	 * @return an observable value which tracks this property of the current
	 *         value of <code>master</code>.
	 */
	public <M extends S> IObservableValue<T> observeDetail(IObservableValue<M> master);

	/**
	 * Returns an observable list on the master observable's realm which tracks
	 * this property on each element of <code>master</code>.
	 *
	 * @param master
	 *            the master observable
	 * @return an observable list which tracks this property on each element of
	 *         the master observable.
	 */
	public <M extends S> IObservableList<T> observeDetail(IObservableList<M> master);

	/**
	 * Returns an observable map on the master observable's realm where the
	 * map's key set is the specified master set, and where each key maps to the
	 * current property value for each element.
	 *
	 * @param master
	 *            the master observable
	 * @return an observable map that tracks the current value of this property
	 *         for the elements in the given set.
	 */
	public <M extends S> IObservableMap<M, T> observeDetail(IObservableSet<M> master);

	/**
	 * Returns an observable map on the master observable's realm where the
	 * map's key set is the same as the master observable map, and where each
	 * value is the property value of the corresponding value in the master
	 * observable map.
	 *
	 * @param master
	 *            the master observable
	 * @return an observable map on the master observable's realm which tracks
	 *         the current value of this property for the elements in the given
	 *         map's values collection
	 */
	public <K, V extends S> IObservableMap<K, T> observeDetail(IObservableMap<K, V> master);

	/**
	 * Returns the nested combination of this property and the specified detail
	 * value property. Value modifications made through the returned property
	 * are delegated to the detail property, using the value of this property as
	 * the source.
	 *
	 * @param detailValue
	 *            the detail property
	 * @return the nested combination of the master and detail properties
	 */
	public <M> IValueProperty<S, M> value(IValueProperty<? super T, M> detailValue);

	/**
	 * Returns the nested combination of this property and the specified detail
	 * list property. List modifications made through the returned property are
	 * delegated to the detail property, using the value of the master property
	 * as the source.
	 *
	 * @param detailList
	 *            the detail property
	 * @return the nested combination of the master value and detail list
	 *         properties
	 */
	public <E> IListProperty<S, E> list(IListProperty<? super T, E> detailList);

	/**
	 * Returns the nested combination of this property and the specified detail
	 * set property. Set modifications made through the returned property are
	 * delegated to the detail property, using the value of the master property
	 * as the source.
	 *
	 * @param detailSet
	 *            the detail property
	 * @return the nested combination of the master value and detail set
	 *         properties
	 */
	public <E> ISetProperty<S, E> set(ISetProperty<? super T, E> detailSet);

	/**
	 * Returns the nested combination of this property and the specified detail
	 * map property. Map modifications made through the returned property are
	 * delegated to the detail property, using the value of the master property
	 * as the source.
	 *
	 * @param detailMap
	 *            the detail property
	 * @return the nested combination of the master value and detial map
	 *         properties
	 */
	public <K, V> IMapProperty<S, K, V> map(IMapProperty<? super T, K, V> detailMap);
}
