/*******************************************************************************
 * Copyright (c) 2008, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 195222
 ******************************************************************************/

package org.eclipse.core.databinding.property.map;

import java.util.Map;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;

/**
 * Interface for map-typed properties
 * 
 * @since 1.2
 * @noimplement This interface is not intended to be implemented by clients.
 *              Clients should instead subclass one of the classes that
 *              implement this interface. Note that direct implementers of this
 *              interface outside of the framework will be broken in future
 *              releases when methods are added to this interface.
 * @see MapProperty
 * @see SimpleMapProperty
 */
public interface IMapProperty extends IProperty {
	/**
	 * Returns the element type of the map's key set or <code>null</code> if the
	 * key set is untyped.
	 * 
	 * @return the element type of the map's key set or <code>null</code> if the
	 *         key set is untyped.
	 */
	public Object getKeyType();

	/**
	 * Returns the element type of the map's values collection or
	 * <code>null</code> if the collection is untyped.
	 * 
	 * @return the element type of the map's values collection or
	 *         <code>null</code> if the collection is untyped.
	 */
	public Object getValueType();

	/**
	 * Returns an unmodifiable Map with the current contents of the source's map
	 * property.
	 * 
	 * @param source
	 *            the property source (may be null)
	 * @return a Map with the current contents of the source's map property
	 * @since 1.3
	 */
	public Map getMap(Object source);

	/**
	 * Updates the property on the source with the specified change.
	 * <p>
	 * <b>Note:</b> This method is made available to facilitate basic property
	 * access. However if the property source lacks property change
	 * notification, then observables on the source object may not be notified
	 * of the change. In most cases it is preferable to modify the source
	 * through an {@link IObservableMap} than through the property directly.
	 * </p>
	 * 
	 * @param source
	 *            the property source (may be null)
	 * @param map
	 *            the new map
	 * @since 1.3
	 */
	public void setMap(Object source, Map map);

	/**
	 * Updates the property on the source with the specified change.
	 * <p>
	 * <b>Note:</b> This method is made available to facilitate basic property
	 * access. However if the property source lacks property change
	 * notification, then observables on the source object may not be notified
	 * of the change. In most cases it is preferable to modify the source
	 * through an {@link IObservableMap} than through the property directly.
	 * </p>
	 * 
	 * @param source
	 *            the property source (may be null)
	 * @param diff
	 *            a diff describing the change
	 * @since 1.3
	 */
	public void updateMap(Object source, MapDiff diff);

	/**
	 * Returns an observable map observing this map property on the given
	 * property source
	 * 
	 * @param source
	 *            the property source
	 * @return an observable map observing this map-typed property on the given
	 *         property source
	 */
	public IObservableMap observe(Object source);

	/**
	 * Returns an observable map observing this map property on the given
	 * property source
	 * 
	 * @param realm
	 *            the observable's realm
	 * @param source
	 *            the property source
	 * @return an observable map observing this map-typed property on the given
	 *         property source
	 */
	public IObservableMap observe(Realm realm, Object source);

	/**
	 * Returns a factory for creating observable maps tracking this property of
	 * a particular property source.
	 * 
	 * @return a factory for creating observable maps tracking this property of
	 *         a particular property source.
	 */
	public IObservableFactory mapFactory();

	/**
	 * Returns a factory for creating observable maps in the given realm,
	 * tracking this property of a particular property source.
	 * 
	 * @param realm
	 *            the realm
	 * 
	 * @return a factory for creating observable maps in the given realm,
	 *         tracking this property of a particular property source.
	 */
	public IObservableFactory mapFactory(Realm realm);

	/**
	 * Returns an observable map on the master observable's realm which tracks
	 * this property of the values in the entry set of <code>master</code>.
	 * 
	 * @param master
	 *            the master observable
	 * @return an observable map on the master observable's realm which tracks
	 *         this property of the values in the entry set of
	 *         <code>master</code>.
	 */
	public IObservableMap observeDetail(IObservableValue master);

	/**
	 * Returns the nested combination of this property and the specified detail
	 * value property. Note that because this property is a projection of value
	 * properties over a values collection, the only modifications supported are
	 * through the {@link IObservableMap#put(Object, Object)} and
	 * {@link IObservableMap#putAll(java.util.Map)} methods. In the latter case,
	 * this property does not entries for keys not already contained in the
	 * master map's key set. Modifications made through the returned property
	 * are delegated to the detail property, using the corresponding entry value
	 * from the master property as the source.
	 * 
	 * @param detailValues
	 *            the detail property
	 * @return the nested combination of the master map and detail value
	 *         properties.
	 */
	public IMapProperty values(IValueProperty detailValues);
}
