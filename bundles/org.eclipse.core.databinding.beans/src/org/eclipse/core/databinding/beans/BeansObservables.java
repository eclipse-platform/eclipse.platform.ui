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
package org.eclipse.core.databinding.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.eclipse.core.databinding.BindingException;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.internal.databinding.internal.beans.JavaBeanObservableList;
import org.eclipse.core.internal.databinding.internal.beans.JavaBeanObservableMap;
import org.eclipse.core.internal.databinding.internal.beans.JavaBeanObservableSet;
import org.eclipse.core.internal.databinding.internal.beans.JavaBeanObservableValue;

/**
 * A factory for creating observable objects for properties of plain Java
 * objects with JavaBeans-style notification.
 * 
 * @since 1.1
 * 
 */
final public class BeansObservables {

	/**
	 * @param realm
	 * @param bean
	 * @param attributeName
	 * @return
	 */
	public static IObservableValue observeValue(Object bean,
			String attributeName) {
		return observeValue(Realm.getDefault(), bean, attributeName);
	}

	/**
	 * @param realm
	 * @param bean
	 * @param attributeName
	 * @return
	 */
	public static IObservableValue observeValue(Realm realm, Object bean,
			String attributeName) {
		PropertyDescriptor descriptor = getPropertyDescriptor(bean.getClass(),
				attributeName);
		return new JavaBeanObservableValue(realm, bean, descriptor, null);
	}

	/**
	 * @param domain
	 * @param beanClass
	 * @param attributeName
	 * @return
	 */
	public static IObservableMap observeMap(IObservableSet domain,
			Class beanClass, String attributeName) {
		PropertyDescriptor descriptor = getPropertyDescriptor(beanClass,
				attributeName);
		return new JavaBeanObservableMap(domain, descriptor);
	}

	private static PropertyDescriptor getPropertyDescriptor(Class beanClass,
			String attributeName) {
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
			if (descriptor.getName().equals(attributeName)) {
				return descriptor;
			}
		}
		throw new BindingException(
				"Could not find attribute with name " + attributeName + " in class " + beanClass); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @param domain
	 * @param beanClass
	 * @param attributeNames
	 * @return
	 */
	public static IObservableMap[] observeMaps(IObservableSet domain,
			Class beanClass, String[] attributeNames) {
		IObservableMap[] result = new IObservableMap[attributeNames.length];
		for (int i = 0; i < attributeNames.length; i++) {
			result[i] = observeMap(domain, beanClass, attributeNames[i]);
		}
		return result;
	}

	/**
	 * @param realm
	 * @param bean
	 * @param attributeName
	 * @return
	 */
	public static IObservableList observeList(Realm realm, Object bean,
			String attributeName) {
		PropertyDescriptor propertyDescriptor = getPropertyDescriptor(bean
				.getClass(), attributeName);
		Class propertyType = propertyDescriptor.getPropertyType();
		Class componentType = propertyType.isArray() ? propertyType
				.getComponentType() : Object.class;
		return new JavaBeanObservableList(realm, bean, propertyDescriptor,
				componentType);
	}

	/**
	 * @param realm
	 * @param bean
	 * @param attributeName
	 * @return
	 */
	public static IObservableSet observeSet(Realm realm, Object bean,
			String attributeName) {
		PropertyDescriptor propertyDescriptor = getPropertyDescriptor(bean
				.getClass(), attributeName);
		Class propertyType = propertyDescriptor.getPropertyType();
		Class componentType = propertyType.isArray() ? propertyType
				.getComponentType() : Object.class;
		return new JavaBeanObservableSet(realm, bean, propertyDescriptor,
				componentType);
	}

	/**
	 * @param realm
	 * @param propertyName
	 * @return
	 */
	public static IObservableFactory valueFactory(final Realm realm,
			final String propertyName) {
		return new IObservableFactory() {
			public IObservable createObservable(Object target) {
				return observeValue(realm, target, propertyName);
			}
		};
	}

	/**
	 * @param realm
	 * @param propertyName
	 * @return
	 */
	public static IObservableFactory listFactory(final Realm realm,
			final String propertyName) {
		return new IObservableFactory() {
			public IObservable createObservable(Object target) {
				return observeList(realm, target, propertyName);
			}
		};
	}

	/**
	 * @param realm
	 * @param propertyName
	 * @return
	 */
	public static IObservableFactory setFactory(final Realm realm,
			final String propertyName) {
		return new IObservableFactory() {
			public IObservable createObservable(Object target) {
				return observeSet(realm, target, propertyName);
			}
		};
	}

	/**
	 * Helper method for
	 * <code>MasterDetailObservables.detailValue(master, valueFactory(realm,
	 attributeName), attributeType)</code>
	 * 
	 * @param realm
	 * @param master
	 * @param attributeName
	 * @param attributeType
	 * @return
	 * 
	 * @see MasterDetailObservables
	 */
	public static IObservableValue observeDetailValue(Realm realm,
			IObservableValue master, String attributeName, Class attributeType) {
		return MasterDetailObservables.detailValue(master, valueFactory(realm,
				attributeName), attributeType);
	}

	/**
	 * Helper method for
	 * <code>MasterDetailObservables.detailList(master, listFactory(realm,
	 attributeName), attributeType)</code>
	 * 
	 * @param realm
	 * @param master
	 * @param attributeName
	 * @param attributeType
	 * @return
	 * 
	 * @see MasterDetailObservables
	 */
	public static IObservableList observeDetailList(Realm realm,
			IObservableValue master, String attributeName, Class attributeType) {
		return MasterDetailObservables.detailList(master, listFactory(realm,
				attributeName), attributeType);
	}

	/**
	 * Helper method for
	 * <code>MasterDetailObservables.detailSet(master, setFactory(realm,
	 attributeName), attributeType)</code>
	 * 
	 * @param realm
	 * @param master
	 * @param attributeName
	 * @param attributeType
	 * @return
	 * 
	 * @see MasterDetailObservables
	 */
	public static IObservableSet observeDetailSet(Realm realm,
			IObservableValue master, String attributeName, Class attributeType) {
		return MasterDetailObservables.detailSet(master, setFactory(realm,
				attributeName), attributeType);
	}

}
