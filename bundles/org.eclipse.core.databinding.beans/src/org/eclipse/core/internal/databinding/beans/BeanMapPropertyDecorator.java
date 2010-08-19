/*******************************************************************************
 * Copyright (c) 2008, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 195222)
 *     Matthew Hall - bug 264307
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.beans.PropertyDescriptor;
import java.util.Map;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.IBeanMapProperty;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.map.IMapProperty;
import org.eclipse.core.databinding.property.map.MapProperty;

/**
 * @since 3.3
 * 
 */
public class BeanMapPropertyDecorator extends MapProperty implements
		IBeanMapProperty {
	private final IMapProperty delegate;
	private final PropertyDescriptor propertyDescriptor;

	/**
	 * @param delegate
	 * @param propertyDescriptor
	 */
	public BeanMapPropertyDecorator(IMapProperty delegate,
			PropertyDescriptor propertyDescriptor) {
		this.delegate = delegate;
		this.propertyDescriptor = propertyDescriptor;
	}

	public PropertyDescriptor getPropertyDescriptor() {
		return propertyDescriptor;
	}

	public Object getKeyType() {
		return delegate.getKeyType();
	}

	public Object getValueType() {
		return delegate.getValueType();
	}

	protected Map doGetMap(Object source) {
		return delegate.getMap(source);
	}

	protected void doSetMap(Object source, Map map) {
		delegate.setMap(source, map);
	}

	protected void doUpdateMap(Object source, MapDiff diff) {
		delegate.updateMap(source, diff);
	}

	public IBeanMapProperty values(String propertyName) {
		return values(propertyName, null);
	}

	public IBeanMapProperty values(String propertyName, Class valueType) {
		Class beanClass = (Class) delegate.getValueType();
		return values(BeanProperties.value(beanClass, propertyName, valueType));
	}

	public IBeanMapProperty values(IBeanValueProperty property) {
		return new BeanMapPropertyDecorator(super.values(property),
				property.getPropertyDescriptor());
	}

	public IObservableMap observe(Object source) {
		return new BeanObservableMapDecorator(delegate.observe(source),
				propertyDescriptor);
	}

	public IObservableMap observe(Realm realm, Object source) {
		return new BeanObservableMapDecorator(delegate.observe(realm, source),
				propertyDescriptor);
	}

	public IObservableMap observeDetail(IObservableValue master) {
		return new BeanObservableMapDecorator(delegate.observeDetail(master),
				propertyDescriptor);
	}

	public String toString() {
		return delegate.toString();
	}
}
