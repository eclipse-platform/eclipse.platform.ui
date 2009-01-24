/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation
 *     Matthew Hall - bug 195222, 247997
 ******************************************************************************/

package org.eclipse.core.databinding.property.map;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.internal.databinding.property.map.SimplePropertyObservableMap;

/**
 * Simplified abstract implementation of IMapProperty. This class takes care of
 * most of the functional requirements for an IMapProperty implementation,
 * leaving only the property-specific details to subclasses.
 * <p>
 * Subclasses must implement these methods:
 * <ul>
 * <li> {@link #getKeyType()}
 * <li> {@link #getValueType()}
 * <li> {@link #doGetMap(Object)}
 * <li> {@link #doSetMap(Object, Map, MapDiff)}
 * <li> {@link #adaptListener(ISimplePropertyListener)}
 * <li> {@link #doAddListener(Object, INativePropertyListener)}
 * <li> {@link #doRemoveListener(Object, INativePropertyListener)}
 * </ul>
 * <p>
 * In addition, we recommended overriding {@link #toString()} to return a
 * description suitable for debugging purposes.
 * 
 * @since 1.2
 */
public abstract class SimpleMapProperty extends MapProperty {
	public IObservableMap observe(Realm realm, Object source) {
		return new SimplePropertyObservableMap(realm, source, this);
	}

	// Accessors

	/**
	 * Returns an unmodifiable Map with the current contents of the source's map
	 * property.
	 * 
	 * @param source
	 *            the property source
	 * @return a Map with the current contents of the source's map property
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public final Map getMap(Object source) {
		if (source == null)
			return Collections.EMPTY_MAP;
		return Collections.unmodifiableMap(doGetMap(source));
	}

	/**
	 * Returns a Map with the current contents of the source's map property
	 * 
	 * @param source
	 *            the property source
	 * @return a Map with the current contents of the source's map property
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected abstract Map doGetMap(Object source);

	// Mutators

	/**
	 * Updates the property on the source with the specified change.
	 * 
	 * @param source
	 *            the property source
	 * @param map
	 *            the new map
	 * @param diff
	 *            a diff describing the change
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public final void setMap(Object source, Map map, MapDiff diff) {
		if (source != null && !diff.isEmpty())
			doSetMap(source, map, diff);
	}

	/**
	 * Updates the property on the source with the specified change.
	 * 
	 * @param source
	 *            the property source
	 * @param map
	 *            the new map
	 * @param diff
	 *            a diff describing the change
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected abstract void doSetMap(Object source, Map map, MapDiff diff);

	// Listeners

	/**
	 * Returns a listener which implements the correct listener interface for
	 * the expected source object, and which parlays property change events from
	 * the source object to the given listener. If there is no listener API for
	 * this property, this method returns null.
	 * 
	 * @param listener
	 *            the property listener to receive events
	 * @return a native listener which parlays property change events to the
	 *         specified listener.
	 * @throws ClassCastException
	 *             if the provided listener does not implement the correct
	 *             listener interface (IValueProperty, IListProperty,
	 *             ISetProperty or IMapProperty) depending on the property.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public abstract INativePropertyListener adaptListener(
			ISimplePropertyListener listener);

	/**
	 * Adds the specified listener as a listener for this property on the
	 * specified property source. If the source object has no listener API for
	 * this property (i.e. {@link #adaptListener(ISimplePropertyListener)}
	 * returns null), this method does nothing.
	 * 
	 * @param source
	 *            the property source
	 * @param listener
	 *            a listener obtained from calling
	 *            {@link #adaptListener(ISimplePropertyListener)} .
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public final void addListener(Object source,
			INativePropertyListener listener) {
		if (source != null)
			doAddListener(source, listener);
	}

	/**
	 * Adds the specified listener as a listener for this property on the
	 * specified property source. If the source object has no listener API for
	 * this property (i.e. {@link #adaptListener(ISimplePropertyListener)}
	 * returns null), this method does nothing.
	 * 
	 * @param source
	 *            the property source
	 * @param listener
	 *            a listener obtained from calling
	 *            {@link #adaptListener(ISimplePropertyListener)} .
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected abstract void doAddListener(Object source,
			INativePropertyListener listener);

	/**
	 * Removes the specified listener as a listener for this property on the
	 * specified property source. If the source object has no listener API for
	 * this property (i.e. {@link #adaptListener(ISimplePropertyListener)}
	 * returns null), this method does nothing.
	 * 
	 * @param source
	 *            the property source
	 * @param listener
	 *            a listener obtained from calling
	 *            {@link #adaptListener(ISimplePropertyListener)} .
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public final void removeListener(Object source,
			INativePropertyListener listener) {
		if (source != null)
			doRemoveListener(source, listener);
	}

	/**
	 * Removes the specified listener as a listener for this property on the
	 * specified property source. If the source object has no listener API for
	 * this property (i.e. {@link #adaptListener(ISimplePropertyListener)}
	 * returns null), this method does nothing.
	 * 
	 * @param source
	 *            the property source
	 * @param listener
	 *            a listener obtained from calling
	 *            {@link #adaptListener(ISimplePropertyListener)} .
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected abstract void doRemoveListener(Object source,
			INativePropertyListener listener);
}
