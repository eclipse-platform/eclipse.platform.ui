/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 195222, 247997, 261843, 264307
 ******************************************************************************/

package org.eclipse.core.databinding.beans;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.map.IMapProperty;
import org.eclipse.core.databinding.property.set.ISetProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.internal.databinding.beans.AnonymousBeanListProperty;
import org.eclipse.core.internal.databinding.beans.AnonymousBeanMapProperty;
import org.eclipse.core.internal.databinding.beans.AnonymousBeanSetProperty;
import org.eclipse.core.internal.databinding.beans.AnonymousBeanValueProperty;
import org.eclipse.core.internal.databinding.beans.BeanListProperty;
import org.eclipse.core.internal.databinding.beans.BeanListPropertyDecorator;
import org.eclipse.core.internal.databinding.beans.BeanMapProperty;
import org.eclipse.core.internal.databinding.beans.BeanMapPropertyDecorator;
import org.eclipse.core.internal.databinding.beans.BeanPropertyHelper;
import org.eclipse.core.internal.databinding.beans.BeanSetProperty;
import org.eclipse.core.internal.databinding.beans.BeanSetPropertyDecorator;
import org.eclipse.core.internal.databinding.beans.BeanValueProperty;
import org.eclipse.core.internal.databinding.beans.BeanValuePropertyDecorator;

/**
 * A factory for creating properties for Java objects that conform to the <a
 * href="http://java.sun.com/products/javabeans/docs/spec.html">JavaBean
 * specification</a> for bound properties.
 * 
 * @since 1.2
 */
public class BeanProperties {
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
	public static IBeanValueProperty value(String propertyName) {
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
	public static IBeanValueProperty value(String propertyName, Class valueType) {
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
	public static IBeanValueProperty value(Class beanClass, String propertyName) {
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
	public static IBeanValueProperty value(Class beanClass,
			String propertyName, Class valueType) {
		String[] propertyNames = split(propertyName);
		if (propertyNames.length > 1)
			valueType = null;

		PropertyDescriptor propertyDescriptor;
		IValueProperty property;
		if (beanClass == null) {
			propertyDescriptor = null;
			property = new AnonymousBeanValueProperty(propertyNames[0],
					valueType);
		} else {
			propertyDescriptor = BeanPropertyHelper.getPropertyDescriptor(
					beanClass, propertyNames[0]);
			property = new BeanValueProperty(propertyDescriptor, valueType);
		}

		IBeanValueProperty beanProperty = new BeanValuePropertyDecorator(
				property, propertyDescriptor);
		for (int i = 1; i < propertyNames.length; i++) {
			beanProperty = beanProperty.value(propertyNames[i]);
		}
		return beanProperty;
	}

	private static String[] split(String propertyName) {
		if (propertyName.indexOf('.') == -1)
			return new String[] { propertyName };
		List propertyNames = new ArrayList();
		int index;
		while ((index = propertyName.indexOf('.')) != -1) {
			propertyNames.add(propertyName.substring(0, index));
			propertyName = propertyName.substring(index + 1);
		}
		propertyNames.add(propertyName);
		return (String[]) propertyNames
				.toArray(new String[propertyNames.size()]);
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
	public static IBeanValueProperty[] values(Class beanClass,
			String[] propertyNames) {
		IBeanValueProperty[] properties = new IBeanValueProperty[propertyNames.length];
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
	public static IBeanValueProperty[] values(String[] propertyNames) {
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
	public static IBeanSetProperty set(String propertyName) {
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
	public static IBeanSetProperty set(String propertyName, Class elementType) {
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
	public static IBeanSetProperty set(Class beanClass, String propertyName) {
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
	public static IBeanSetProperty set(Class beanClass, String propertyName,
			Class elementType) {
		PropertyDescriptor propertyDescriptor;
		ISetProperty property;
		if (beanClass == null) {
			propertyDescriptor = null;
			property = new AnonymousBeanSetProperty(propertyName, elementType);
		} else {
			propertyDescriptor = BeanPropertyHelper.getPropertyDescriptor(
					beanClass, propertyName);
			property = new BeanSetProperty(propertyDescriptor, elementType);
		}
		return new BeanSetPropertyDecorator(property, propertyDescriptor);
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
	public static IBeanListProperty list(String propertyName) {
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
	public static IBeanListProperty list(String propertyName, Class elementType) {
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
	public static IBeanListProperty list(Class beanClass, String propertyName) {
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
	public static IBeanListProperty list(Class beanClass, String propertyName,
			Class elementType) {
		PropertyDescriptor propertyDescriptor;
		IListProperty property;
		if (beanClass == null) {
			propertyDescriptor = null;
			property = new AnonymousBeanListProperty(propertyName, elementType);
		} else {
			propertyDescriptor = BeanPropertyHelper.getPropertyDescriptor(
					beanClass, propertyName);
			property = new BeanListProperty(propertyDescriptor, elementType);
		}
		return new BeanListPropertyDecorator(property, propertyDescriptor);
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
	public static IBeanMapProperty map(String propertyName) {
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
	public static IBeanMapProperty map(String propertyName, Class keyType,
			Class valueType) {
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
	public static IBeanMapProperty map(Class beanClass, String propertyName) {
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
	 *            the key type for the returned map property
	 * @param valueType
	 *            the value type for the returned map property
	 * @return a map property for the given property name of the given bean
	 *         class.
	 */
	public static IBeanMapProperty map(Class beanClass, String propertyName,
			Class keyType, Class valueType) {
		PropertyDescriptor propertyDescriptor;
		IMapProperty property;
		if (beanClass == null) {
			propertyDescriptor = null;
			property = new AnonymousBeanMapProperty(propertyName, keyType,
					valueType);
		} else {
			propertyDescriptor = BeanPropertyHelper.getPropertyDescriptor(
					beanClass, propertyName);
			property = new BeanMapProperty(propertyDescriptor, keyType,
					valueType);
		}
		return new BeanMapPropertyDecorator(property, propertyDescriptor);
	}
}
