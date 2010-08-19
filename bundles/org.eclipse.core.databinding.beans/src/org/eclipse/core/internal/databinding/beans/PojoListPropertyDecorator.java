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
import java.util.List;

import org.eclipse.core.databinding.beans.IBeanListProperty;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.list.ListProperty;

/**
 * @since 3.3
 * 
 */
public class PojoListPropertyDecorator extends ListProperty implements
		IBeanListProperty {
	private final IListProperty delegate;
	private final PropertyDescriptor propertyDescriptor;

	/**
	 * @param delegate
	 * @param propertyDescriptor
	 */
	public PojoListPropertyDecorator(IListProperty delegate,
			PropertyDescriptor propertyDescriptor) {
		this.delegate = delegate;
		this.propertyDescriptor = propertyDescriptor;
	}

	public Object getElementType() {
		return delegate.getElementType();
	}

	protected List doGetList(Object source) {
		return delegate.getList(source);
	}

	protected void doSetList(Object source, List list) {
		delegate.setList(source, list);
	}

	protected void doUpdateList(Object source, ListDiff diff) {
		delegate.updateList(source, diff);
	}

	public IBeanListProperty values(String propertyName) {
		return values(propertyName, null);
	}

	public IBeanListProperty values(String propertyName, Class valueType) {
		Class beanClass = (Class) delegate.getElementType();
		return values(PojoProperties.value(beanClass, propertyName, valueType));
	}

	public IBeanListProperty values(IBeanValueProperty property) {
		return new PojoListPropertyDecorator(super.values(property),
				property.getPropertyDescriptor());
	}

	public PropertyDescriptor getPropertyDescriptor() {
		return propertyDescriptor;
	}

	public IObservableList observe(Object source) {
		return new BeanObservableListDecorator(delegate.observe(source),
				propertyDescriptor);
	}

	public IObservableList observe(Realm realm, Object source) {
		return new BeanObservableListDecorator(delegate.observe(realm, source),
				propertyDescriptor);
	}

	public IObservableList observeDetail(IObservableValue master) {
		return new BeanObservableListDecorator(delegate.observeDetail(master),
				propertyDescriptor);
	}

	public String toString() {
		return delegate.toString();
	}
}
