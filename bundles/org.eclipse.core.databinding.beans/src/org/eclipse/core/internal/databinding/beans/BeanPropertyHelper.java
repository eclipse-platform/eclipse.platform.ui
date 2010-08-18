/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Martin Frey <martin.frey@logica.com> - bug 256150
 *     Matthew Hall - bug 264307
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 1.2
 * 
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
			if (!writeMethod.isAccessible()) {
				writeMethod.setAccessible(true);
			}
			writeMethod.invoke(source, new Object[] { value });
		} catch (InvocationTargetException e) {
			/*
			 * InvocationTargetException wraps any exception thrown by the
			 * invoked method.
			 */
			throw new RuntimeException(e.getCause());
		} catch (Exception e) {
			if (BeansObservables.DEBUG) {
				Policy
						.getLog()
						.log(
								new Status(
										IStatus.WARNING,
										Policy.JFACE_DATABINDING,
										IStatus.OK,
										"Could not change value of " + source + "." + propertyDescriptor.getName(), e)); //$NON-NLS-1$ //$NON-NLS-2$
			}
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
			if (!readMethod.isAccessible()) {
				readMethod.setAccessible(true);
			}
			return readMethod.invoke(source, null);
		} catch (InvocationTargetException e) {
			/*
			 * InvocationTargetException wraps any exception thrown by the
			 * invoked method.
			 */
			throw new RuntimeException(e.getCause());
		} catch (Exception e) {
			if (BeansObservables.DEBUG) {
				Policy
						.getLog()
						.log(
								new Status(
										IStatus.WARNING,
										Policy.JFACE_DATABINDING,
										IStatus.OK,
										"Could not read value of " + source + "." + propertyDescriptor.getName(), e)); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return null;
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
	public static Class getCollectionPropertyElementType(
			PropertyDescriptor descriptor) {
		Class propertyType = descriptor.getPropertyType();
		return propertyType.isArray() ? propertyType.getComponentType()
				: Object.class;
	}

	/**
	 * @param beanClass
	 * @param propertyName
	 * @return the PropertyDescriptor for the named property on the given bean
	 *         class
	 */
	public static PropertyDescriptor getPropertyDescriptor(Class beanClass,
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
			for (int i = 0; i < propertyDescriptors.length; i++) {
				PropertyDescriptor descriptor = propertyDescriptors[i];
				if (descriptor.getName().equals(propertyName)) {
					return descriptor;
				}
			}
		} else {
			try {
				PropertyDescriptor propertyDescriptors[];
				List pds = new ArrayList();
				getInterfacePropertyDescriptors(pds, beanClass);
				if (pds.size() > 0) {
					propertyDescriptors = (PropertyDescriptor[]) pds
							.toArray(new PropertyDescriptor[pds.size()]);
					PropertyDescriptor descriptor;
					for (int i = 0; i < propertyDescriptors.length; i++) {
						descriptor = propertyDescriptors[i];
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
	 * @throws IntrospectionException
	 */
	private static void getInterfacePropertyDescriptors(
			List propertyDescriptors, Class iface)
			throws IntrospectionException {
		BeanInfo beanInfo = Introspector.getBeanInfo(iface);
		PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
		for (int i = 0; i < pds.length; i++) {
			PropertyDescriptor pd = pds[i];
			propertyDescriptors.add(pd);
		}
		Class[] subIntfs = iface.getInterfaces();
		for (int j = 0; j < subIntfs.length; j++) {
			getInterfacePropertyDescriptors(propertyDescriptors, subIntfs[j]);
		}
	}

	/**
	 * @param observable
	 * @param propertyName
	 * @return property descriptor or <code>null</code>
	 */
	/* package */public static PropertyDescriptor getValueTypePropertyDescriptor(
			IObservableValue observable, String propertyName) {
		if (observable.getValueType() != null)
			return getPropertyDescriptor((Class) observable.getValueType(),
					propertyName);
		return null;
	}

	/**
	 * @param propertyDescriptor
	 * @return String description of property descriptor
	 */
	public static String propertyName(PropertyDescriptor propertyDescriptor) {
		Class beanClass = propertyDescriptor.getReadMethod()
				.getDeclaringClass();
		return shortClassName(beanClass)
				+ "." + propertyDescriptor.getName() + ""; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @param beanClass
	 * @return class name excluding package
	 */
	public static String shortClassName(Class beanClass) {
		if (beanClass == null)
			return "?"; //$NON-NLS-1$
		String className = beanClass.getName();
		int lastDot = className.lastIndexOf('.');
		if (lastDot != -1)
			className = className.substring(lastDot + 1);
		return className;
	}
}
