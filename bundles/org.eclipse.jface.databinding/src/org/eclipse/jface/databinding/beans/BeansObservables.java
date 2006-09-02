/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.eclipse.jface.databinding.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.internal.beans.JavaBeanObservableValue;

/**
 * A factory for creating observable objects for properties of plain Java
 * objects with JavaBeans-style notification.
 * 
 * @since 1.1
 * 
 */
final public class BeansObservables {

	/**
	 * @param bean
	 * @param attributeName
	 * @return
	 */
	public static IObservableValue getAttribute(Object bean,
			String attributeName) {
		Class objectClass = bean.getClass();
		BeanInfo beanInfo;
		try {
			beanInfo = Introspector.getBeanInfo(objectClass);
		} catch (IntrospectionException e) {
			// cannot introspect, give up
			return null;
		}
		PropertyDescriptor[] propertyDescriptors = beanInfo
				.getPropertyDescriptors();
		for (int i = 0; i < propertyDescriptors.length; i++) {
			PropertyDescriptor descriptor = propertyDescriptors[i];
			if (descriptor.getName().equals(attributeName)) {
				return new JavaBeanObservableValue(bean, descriptor, null);
			}
		}
		throw new org.eclipse.jface.databinding.BindingException(
				"Could not find attribute with name " + attributeName + " for object " + bean); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
