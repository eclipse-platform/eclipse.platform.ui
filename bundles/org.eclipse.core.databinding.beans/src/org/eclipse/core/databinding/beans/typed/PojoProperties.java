/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 195222, 247997, 261843, 264307
 ******************************************************************************/

package org.eclipse.core.databinding.beans.typed;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.beans.IBeanListProperty;
import org.eclipse.core.databinding.beans.IBeanMapProperty;
import org.eclipse.core.databinding.beans.IBeanSetProperty;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.map.IMapProperty;
import org.eclipse.core.databinding.property.set.ISetProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.internal.databinding.beans.AnonymousPojoListProperty;
import org.eclipse.core.internal.databinding.beans.AnonymousPojoMapProperty;
import org.eclipse.core.internal.databinding.beans.AnonymousPojoSetProperty;
import org.eclipse.core.internal.databinding.beans.AnonymousPojoValueProperty;
import org.eclipse.core.internal.databinding.beans.BeanPropertyHelper;
import org.eclipse.core.internal.databinding.beans.PojoListProperty;
import org.eclipse.core.internal.databinding.beans.PojoListPropertyDecorator;
import org.eclipse.core.internal.databinding.beans.PojoMapProperty;
import org.eclipse.core.internal.databinding.beans.PojoMapPropertyDecorator;
import org.eclipse.core.internal.databinding.beans.PojoSetProperty;
import org.eclipse.core.internal.databinding.beans.PojoSetPropertyDecorator;
import org.eclipse.core.internal.databinding.beans.PojoValueProperty;
import org.eclipse.core.internal.databinding.beans.PojoValuePropertyDecorator;

/**
 * A factory for creating properties for POJOs (plain old java objects) that
 * conform to idea of an object with getters and setters but does not provide
 * {@link PropertyChangeEvent property change events} on change. This factory is
 * identical to {@link BeanProperties} except for this fact.
 * <p>
 * This class is a new version of the deprecated class with the same name in the
 * parent package. The difference is that this class returns typed property
 * objects. This class is located in its own package to be able to coexist with
 * the old version while having the same name.
 *
 * @since 1.5
 */
public class PojoProperties {
	/**
	 * Returns a value property for the given property name of an arbitrary bean
	 * class. Objects lacking the named property are treated the same as if the
	 * property always contains null.
	 *
	 * @param propertyName
	 *            the property name. May be nested e.g. "parent.name"
	 * @return a value property for the given property name of an arbitrary bean
	 *         class.
	 */
	public static <S, T> IBeanValueProperty<S, T> value(String propertyName) {
		return value(null, propertyName, null);
	}

	/**
	 * Returns a value property for the given property name of an arbitrary bean
	 * class. Objects lacking the named property are treated the same as if the
	 * property always contains null.
	 *
	 * @param propertyName
	 *            the property name. May be nested e.g. "parent.name"
	 * @param valueType
	 *            the value type of the returned value property
	 * @return a value property for the given property name of an arbitrary bean
	 *         class.
	 */
	public static <S, T> IBeanValueProperty<S, T> value(String propertyName, Class<T> valueType) {
		return value(null, propertyName, valueType);
	}

	/**
	 * Returns a value property for the given property name of the given bean
	 * class.
	 *
	 * @param beanClass
	 *            the bean class
	 * @param propertyName
	 *            the property name. May be nested e.g. "parent.name"
	 * @return a value property for the given property name of the given bean
	 *         class.
	 */
	public static <S, E> IBeanValueProperty<S, E> value(Class<S> beanClass, String propertyName) {
		return value(beanClass, propertyName, null);
	}

	/**
	 * Returns a value property for the given property name of the given bean
	 * class.
	 *
	 * @param beanClass
	 *            the bean class
	 * @param propertyName
	 *            the property name. May be nested e.g. "parent.name"
	 * @param valueType
	 *            the value type of the returned value property
	 * @return a value property for the given property name of the given bean
	 *         class.
	 */
	@SuppressWarnings("unchecked")
	public static <S, T> IBeanValueProperty<S, T> value(Class<S> beanClass, String propertyName, Class<T> valueType) {
		String[] propertyNames = split(propertyName);
		if (propertyNames.length > 1)
			valueType = null;

		IValueProperty<S, T> property;
		PropertyDescriptor propertyDescriptor;
		if (beanClass == null) {
			propertyDescriptor = null;
			property = new PojoValuePropertyDecorator<>(new AnonymousPojoValueProperty<>(propertyNames[0], valueType),
					null);
		} else {
			propertyDescriptor = BeanPropertyHelper.getPropertyDescriptor(beanClass, propertyNames[0]);
			property = new PojoValueProperty<>(propertyDescriptor, valueType);
		}

		IBeanValueProperty<S, T> beanProperty = new PojoValuePropertyDecorator<>(property, propertyDescriptor);
		for (int i = 1; i < propertyNames.length; i++) {
			beanProperty = (IBeanValueProperty<S, T>) beanProperty.value(propertyNames[i]);
		}
		return beanProperty;
	}

	private static String[] split(String propertyName) {
		if (propertyName.indexOf('.') == -1)
			return new String[] { propertyName };
		List<String> propertyNames = new ArrayList<>();
		int index;
		while ((index = propertyName.indexOf('.')) != -1) {
			propertyNames.add(propertyName.substring(0, index));
			propertyName = propertyName.substring(index + 1);
		}
		propertyNames.add(propertyName);
		return propertyNames.toArray(new String[propertyNames.size()]);
	}

	/**
	 * Returns a value property array for the given property names of the given
	 * bean class.
	 *
	 * @param beanClass
	 *            the bean class
	 * @param propertyNames
	 *            array of property names. May be nested e.g. "parent.name"
	 * @return a value property array for the given property names of the given
	 *         bean class.
	 */
	public static <S, T> IBeanValueProperty<S, T>[] values(Class<S> beanClass,
			String[] propertyNames) {
		@SuppressWarnings("unchecked")
		IBeanValueProperty<S, T>[] properties = (IBeanValueProperty<S, T>[]) new IBeanValueProperty<?, ?>[propertyNames.length];
		for (int i = 0; i < properties.length; i++)
			properties[i] = value(beanClass, propertyNames[i], null);
		return properties;
	}

	/**
	 * Returns a value property array for the given property names of an
	 * arbitrary bean class.
	 *
	 * @param propertyNames
	 *            array of property names. May be nested e.g. "parent.name"
	 * @return a value property array for the given property names of the given
	 *         bean class.
	 */
	public static <S, T> IBeanValueProperty<S, T>[] values(String... propertyNames) {
		return values(null, propertyNames);
	}

	/**
	 * Returns a set property for the given property name of an arbitrary bean
	 * class. Objects lacking the named property are treated the same as if the
	 * property always contains an empty set.
	 *
	 * @param propertyName
	 *            the property name
	 * @return a set property for the given property name of an arbitrary bean
	 *         class.
	 */
	public static <S, E> IBeanSetProperty<S, E> set(String propertyName) {
		return set(null, propertyName, null);
	}

	/**
	 * Returns a set property for the given property name of an arbitrary bean
	 * class. Objects lacking the named property are treated the same as if the
	 * property always contains an empty set.
	 *
	 * @param propertyName
	 *            the property name
	 * @param elementType
	 *            the element type of the returned set property
	 * @return a set property for the given property name of an arbitrary bean
	 *         class.
	 */
	public static <S, E> IBeanSetProperty<S, E> set(String propertyName, Class<E> elementType) {
		return set(null, propertyName, elementType);
	}

	/**
	 * Returns a set property for the given property name of the given bean
	 * class.
	 *
	 * @param beanClass
	 *            the bean class
	 * @param propertyName
	 *            the property name
	 * @return a set property for the given property name of the given bean
	 *         class.
	 */
	public static <S, E> IBeanSetProperty<S, E> set(Class<S> beanClass, String propertyName) {
		return set(beanClass, propertyName, null);
	}

	/**
	 * Returns a set property for the given property name of the given bean
	 * class.
	 *
	 * @param beanClass
	 *            the bean class
	 * @param propertyName
	 *            the property name
	 * @param elementType
	 *            the element type of the returned set property
	 * @return a set property for the given property name of the given bean
	 *         class.
	 */
	public static <S, E> IBeanSetProperty<S, E> set(Class<S> beanClass, String propertyName, Class<E> elementType) {
		PropertyDescriptor propertyDescriptor;
		ISetProperty<S, E> property;
		if (beanClass == null) {
			propertyDescriptor = null;
			property = new AnonymousPojoSetProperty<>(propertyName, elementType);
		} else {
			propertyDescriptor = BeanPropertyHelper.getPropertyDescriptor(
					beanClass, propertyName);
			property = new PojoSetProperty<>(propertyDescriptor, elementType);
		}
		return new PojoSetPropertyDecorator<>(property, propertyDescriptor);
	}

	/**
	 * Returns a list property for the given property name of an arbitrary bean
	 * class. Objects lacking the named property are treated the same as if the
	 * property always contains an empty list.
	 *
	 * @param propertyName
	 *            the property name
	 * @return a list property for the given property name of an arbitrary bean
	 *         class.
	 */
	public static <S, E> IBeanListProperty<S, E> list(String propertyName) {
		return list(null, propertyName, null);
	}

	/**
	 * Returns a list property for the given property name of an arbitrary bean
	 * class. Objects lacking the named property are treated the same as if the
	 * property always contains an empty list.
	 *
	 * @param propertyName
	 *            the property name
	 * @param elementType
	 *            the element type of the returned list property
	 * @return a list property for the given property name of the given bean
	 *         class.
	 */
	public static <S, E> IBeanListProperty<S, E> list(String propertyName, Class<E> elementType) {
		return list(null, propertyName, elementType);
	}

	/**
	 * Returns a list property for the given property name of the given bean
	 * class.
	 *
	 * @param beanClass
	 *            the bean class
	 * @param propertyName
	 *            the property name
	 * @return a list property for the given property name of the given bean
	 *         class.
	 */
	public static <S, E> IBeanListProperty<S, E> list(Class<S> beanClass, String propertyName) {
		return list(beanClass, propertyName, null);
	}

	/**
	 * Returns a list property for the given property name of the given bean
	 * class.
	 *
	 * @param beanClass
	 *            the bean class
	 * @param propertyName
	 *            the property name
	 * @param elementType
	 *            the element type of the returned list property
	 * @return a list property for the given property name of the given bean
	 *         class.
	 */
	public static <S, E> IBeanListProperty<S, E> list(Class<S> beanClass, String propertyName, Class<E> elementType) {
		PropertyDescriptor propertyDescriptor;
		IListProperty<S, E> property;
		if (beanClass == null) {
			propertyDescriptor = null;
			property = new AnonymousPojoListProperty<>(propertyName, elementType);
		} else {
			propertyDescriptor = BeanPropertyHelper.getPropertyDescriptor(
					beanClass, propertyName);
			property = new PojoListProperty<>(propertyDescriptor, elementType);
		}
		return new PojoListPropertyDecorator<>(property, propertyDescriptor);
	}

	/**
	 * Returns a map property for the given property name of an arbitrary bean
	 * class. Objects lacking the named property are treated the same as if the
	 * property always contains an empty map.
	 *
	 * @param propertyName
	 *            the property name
	 * @return a map property for the given property name of an arbitrary bean
	 *         class.
	 */
	public static <S, K, V> IBeanMapProperty<S, K, V> map(String propertyName) {
		return map(null, propertyName, null, null);
	}

	/**
	 * Returns a map property for the given property name of an arbitrary bean
	 * class. Objects lacking the named property are treated the same as if the
	 * property always contains an empty map.
	 *
	 * @param propertyName
	 *            the property name
	 * @param keyType
	 *            the key type for the returned map property
	 * @param valueType
	 *            the value type for the returned map property
	 * @return a map property for the given property name of an arbitrary bean
	 *         class.
	 */
	public static <S, K, V> IBeanMapProperty<S, K, V> map(String propertyName, Class<K> keyType, Class<V> valueType) {
		return map(null, propertyName, keyType, valueType);
	}

	/**
	 * Returns a map property for the given property name of the given bean
	 * class.
	 *
	 * @param beanClass
	 *            the bean class
	 * @param propertyName
	 *            the property name
	 * @return a map property for the given property name of the given bean
	 *         class.
	 */
	public static <S, K, V> IBeanMapProperty<S, K, V> map(Class<S> beanClass, String propertyName) {
		return map(beanClass, propertyName, null, null);
	}

	/**
	 * Returns a map property for the given property name of the given bean
	 * class.
	 *
	 * @param beanClass
	 *            the bean class
	 * @param propertyName
	 *            the property name
	 * @param keyType
	 *            the key type of the returned map property
	 * @param valueType
	 *            the value type of the returned map property
	 * @return a map property for the given property name of the given bean
	 *         class.
	 */
	public static <S, K, V> IBeanMapProperty<S, K, V> map(Class<S> beanClass, String propertyName, Class<K> keyType,
			Class<V> valueType) {
		PropertyDescriptor propertyDescriptor;
		IMapProperty<S, K, V> property;
		if (beanClass == null) {
			propertyDescriptor = null;
			property = new AnonymousPojoMapProperty<>(propertyName, keyType, valueType);
		} else {
			propertyDescriptor = BeanPropertyHelper.getPropertyDescriptor(beanClass, propertyName);
			property = new PojoMapProperty<>(propertyDescriptor, keyType, valueType);
		}
		return new PojoMapPropertyDecorator<>(property, propertyDescriptor);
	}
}
