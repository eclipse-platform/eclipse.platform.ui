/*******************************************************************************
 * Copyright (c) 2008, 2018 Matthew Hall and others.
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
 *     Martin Frey <martin.frey@logica.com> - bug 256150
 *     Matthew Hall - bug 264307
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 445446
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 1.2
 */
public class BeanPropertyHelper {
	/**
	 * Sets the contents of the given property on the given source object to the
	 * given value.
	 *
	 * @param source
	 *            the source object which has the property being updated
	 * @param propertyDescriptor
	 *            the property being changed
	 * @param value
	 *            the new value of the property
	 */
	public static void writeProperty(Object source,
			PropertyDescriptor propertyDescriptor, Object value) {
		try {
			Method writeMethod = propertyDescriptor.getWriteMethod();
			if (null == writeMethod) {
				throw new IllegalArgumentException(
						"Missing public setter method for " //$NON-NLS-1$
								+ propertyDescriptor.getName() + " property"); //$NON-NLS-1$
			}
			setAccessible(writeMethod);
			writeMethod.invoke(source, value);
		} catch (InvocationTargetException e) {
			/*
			 * InvocationTargetException wraps any exception thrown by the
			 * invoked method.
			 */
			throw new RuntimeException(e.getCause());
		} catch (Exception e) {
			Policy.getLog()
					.log(new Status(
							IStatus.WARNING,
							Policy.JFACE_DATABINDING,
							IStatus.OK,
							"Could not change value of " + source + "." + propertyDescriptor.getName(), e)); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Returns the contents of the given property for the given bean.
	 *
	 * @param source
	 *            the source bean
	 * @param propertyDescriptor
	 *            the property to retrieve
	 * @return the contents of the given property for the given bean.
	 */
	public static Object readProperty(Object source,
			PropertyDescriptor propertyDescriptor) {
		try {
			Method readMethod = propertyDescriptor.getReadMethod();
			if (readMethod == null) {
				throw new IllegalArgumentException(propertyDescriptor.getName()
						+ " property does not have a read method."); //$NON-NLS-1$
			}
			setAccessible(readMethod);
			return readMethod.invoke(source);
		} catch (InvocationTargetException e) {
			/*
			 * InvocationTargetException wraps any exception thrown by the
			 * invoked method.
			 */
			throw new RuntimeException(e.getCause());
		} catch (Exception e) {
			Policy.getLog()
					.log(new Status(
							IStatus.WARNING,
							Policy.JFACE_DATABINDING,
							IStatus.OK,
							"Could not read value of " + source + "." + propertyDescriptor.getName(), e)); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}

	/**
	 * Wrapper around deprecated {@link Method#isAccessible}. Using that method is
	 * still the right thing to do, even in presence of the new methods
	 * {@link Method#canAccess} and {@link Method#trySetAccessible}, since we only
	 * do that to avoid redundant calls to {@link Method#setAccessible}, and the
	 * permission check that entails.
	 */
	@SuppressWarnings("deprecation")
	static void setAccessible(Method method) {
		if (!method.isAccessible()) {
			method.setAccessible(true);
		}
	}

	/**
	 * Returns the element type of the given collection-typed property for the
	 * given bean.
	 *
	 * @param descriptor
	 *            the property being inspected
	 * @return the element type of the given collection-typed property if it is
	 *         an array property, or Object.class otherwise.
	 */
	public static Class<?> getCollectionPropertyElementType(
			PropertyDescriptor descriptor) {
		Class<?> propertyType = descriptor.getPropertyType();
		return propertyType.isArray() ? propertyType.getComponentType()
				: Object.class;
	}

	/**
	 * @return the PropertyDescriptor for the named property on the given bean
	 *         class
	 */
	public static PropertyDescriptor getPropertyDescriptor(Class<?> beanClass,
			String propertyName) {
		if (!beanClass.isInterface()) {
			BeanInfo beanInfo;
			try {
				beanInfo = Introspector.getBeanInfo(beanClass);
			} catch (IntrospectionException e) {
				// cannot introspect, give up
				return null;
			}
			PropertyDescriptor[] propertyDescriptors = beanInfo
					.getPropertyDescriptors();
			for (PropertyDescriptor descriptor : propertyDescriptors) {
				if (descriptor.getName().equals(propertyName)) {
					return descriptor;
				}
			}
		} else {
			try {
				List<PropertyDescriptor> pds = new ArrayList<>();
				getInterfacePropertyDescriptors(pds, beanClass);
				if (pds.size() > 0) {
					for (PropertyDescriptor descriptor : pds.toArray(new PropertyDescriptor[pds.size()])) {
						if (descriptor.getName().equals(propertyName))
							return descriptor;
					}
				}
			} catch (IntrospectionException e) {
				// cannot introspect, give up
				return null;
			}
		}
		throw new IllegalArgumentException(
				"Could not find property with name " + propertyName + " in class " + beanClass); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Goes recursively into the interface and gets all defined
	 * propertyDescriptors
	 *
	 * @param propertyDescriptors
	 *            The result list of all PropertyDescriptors the given interface
	 *            defines (hierarchical)
	 * @param iface
	 *            The interface to fetch the PropertyDescriptors
	 */
	private static void getInterfacePropertyDescriptors(
			List<PropertyDescriptor> propertyDescriptors, Class<?> iface)
			throws IntrospectionException {
		BeanInfo beanInfo = Introspector.getBeanInfo(iface);
		PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
		propertyDescriptors.addAll(Arrays.asList(pds));
		Class<?>[] subIntfs = iface.getInterfaces();
		for (Class<?> subIntf : subIntfs) {
			getInterfacePropertyDescriptors(propertyDescriptors, subIntf);
		}
	}

	/**
	 * @return property descriptor or <code>null</code>
	 */
	/* package */public static PropertyDescriptor getValueTypePropertyDescriptor(IObservableValue<?> observable,
			String propertyName) {
		if (observable.getValueType() != null)
			return getPropertyDescriptor((Class<?>) observable.getValueType(),
					propertyName);
		return null;
	}

	/**
	 * @return String description of property descriptor
	 */
	public static String propertyName(PropertyDescriptor propertyDescriptor) {
		Class<?> beanClass = propertyDescriptor.getReadMethod()
				.getDeclaringClass();
		return shortClassName(beanClass)
				+ "." + propertyDescriptor.getName() + ""; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @return class name excluding package
	 */
	public static String shortClassName(Class<?> beanClass) {
		if (beanClass == null)
			return "?"; //$NON-NLS-1$
		String className = beanClass.getName();
		int lastDot = className.lastIndexOf('.');
		if (lastDot != -1)
			className = className.substring(lastDot + 1);
		return className;
	}
}
