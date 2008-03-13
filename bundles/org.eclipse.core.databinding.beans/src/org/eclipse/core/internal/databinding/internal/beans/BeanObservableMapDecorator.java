/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 221704)
 ******************************************************************************/

package org.eclipse.core.internal.databinding.internal.beans;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.internal.databinding.Util;

/**
 * {@link IBeanObservable} decorator for an {@link IObservableMap}.
 * 
 * @since 3.3
 */
public class BeanObservableMapDecorator implements IObservableMap, IBeanObservable {
	private IObservableMap delegate;
	private Object observed;
	private PropertyDescriptor propertyDescriptor;

	/**
	 * @param delegate 
	 * @param observed 
	 * @param propertyDescriptor
	 */
	public BeanObservableMapDecorator(IObservableMap delegate,
			Object observed,
			PropertyDescriptor propertyDescriptor) {
		
		this.delegate = delegate;
		this.observed = observed;
		this.propertyDescriptor = propertyDescriptor;
	}

	public Realm getRealm() {
		return delegate.getRealm();
	}

	public boolean isStale() {
		return delegate.isStale();
	}

	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}

	public Set entrySet() {
		return delegate.entrySet();
	}

	public Object get(Object key) {
		return delegate.get(key);
	}

	public Set keySet() {
		return delegate.keySet();
	}

	public Object put(Object key, Object value) {
		return delegate.put(key, value);
	}

	public Object remove(Object key) {
		return delegate.remove(key);
	}

	public Collection values() {
		return delegate.values();
	}

	public void putAll(Map map) {
		delegate.putAll(map);
	}

	public void clear() {
		delegate.clear();
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public int size() {
		return delegate.size();
	}

	public Object getObserved() {
		return observed;
	}

	public PropertyDescriptor getPropertyDescriptor() {
		return propertyDescriptor;
	}

	/**
	 * @return the wrapped map
	 */
	public IObservableMap getDelegate() {
		return delegate;
	}	
	public void dispose() {
		delegate.dispose();
	}

	public void addChangeListener(IChangeListener listener) {
		delegate.addChangeListener(listener);
	}

	public void removeChangeListener(IChangeListener listener) {
		delegate.removeChangeListener(listener);
	}

	public void addMapChangeListener(IMapChangeListener listener) {
		delegate.addMapChangeListener(listener);
	}

	public void removeMapChangeListener(IMapChangeListener listener) {
		delegate.removeMapChangeListener(listener);
	}

	public void addStaleListener(IStaleListener listener) {
		delegate.addStaleListener(listener);
	}

	public void removeStaleListener(IStaleListener listener) {
		delegate.removeStaleListener(listener);
	}

	public boolean equals(Object obj) {
		if (obj instanceof BeanObservableMapDecorator) {
			BeanObservableMapDecorator other = (BeanObservableMapDecorator) obj;
			return Util.equals(other.delegate, delegate);
		}
		return Util.equals(delegate, obj);
	}

	public int hashCode() {
		return delegate.hashCode();
	}
}
