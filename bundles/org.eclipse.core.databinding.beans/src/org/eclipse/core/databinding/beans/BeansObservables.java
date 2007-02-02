/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164268, 171616
 *     Brad Reynolds - bug 147515
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
import org.eclipse.core.internal.databinding.internal.beans.BeanObservableListDecorator;
import org.eclipse.core.internal.databinding.internal.beans.BeanObservableSetDecorator;
import org.eclipse.core.internal.databinding.internal.beans.BeanObservableValueDecorator;
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
	 * 
	 */
	public static final boolean DEBUG = true;

	/**
	 * Returns an observable value in the default realm tracking the current
	 * value of the named property of the given bean.
	 * 
	 * @param bean
	 *            the object
	 * @param propertyName
	 *            the name of the property
	 * @return an observable value tracking the current value of the named
	 *         property of the given bean
	 */
	public static IObservableValue observeValue(Object bean, String propertyName) {
		return observeValue(Realm.getDefault(), bean, propertyName);
	}

	/**
	 * Returns an observable value in the given realm tracking the current value
	 * of the named property of the given bean.
	 * 
	 * @param realm
	 *            the realm
	 * @param bean
	 *            the object
	 * @param propertyName
	 *            the name of the property
	 * @return an observable value tracking the current value of the named
	 *         property of the given bean
	 */
	public static IObservableValue observeValue(Realm realm, Object bean,
			String propertyName) {
		PropertyDescriptor descriptor = getPropertyDescriptor(bean.getClass(),
				propertyName);
		return new JavaBeanObservableValue(realm, bean, descriptor, null);
	}

	/**
	 * Returns an observable map in the default realm tracking the current
	 * values of the named property for the beans in the given set.
	 * 
	 * @param domain
	 *            the set of bean objects
	 * @param beanClass
	 *            the common base type of bean objects that may be in the set
	 * @param propertyName
	 *            the name of the property
	 * @return an observable map tracking the current values of the named
	 *         property for the beans in the given domain set
	 */
	public static IObservableMap observeMap(IObservableSet domain,
			Class beanClass, String propertyName) {
		PropertyDescriptor descriptor = getPropertyDescriptor(beanClass,
				propertyName);
		return new JavaBeanObservableMap(domain, descriptor);
	}

	private static PropertyDescriptor getPropertyDescriptor(Class beanClass,
			String propertyName) {
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
		throw new BindingException(
				"Could not find property with name " + propertyName + " in class " + beanClass); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns an array of observable maps in the default realm tracking the
	 * current values of the named propertys for the beans in the given set.
	 * 
	 * @param domain
	 *            the set of objects
	 * @param beanClass
	 *            the common base type of objects that may be in the set
	 * @param propertyNames
	 *            the array of property names
	 * @return an array of observable maps tracking the current values of the
	 *         named propertys for the beans in the given domain set
	 */
	public static IObservableMap[] observeMaps(IObservableSet domain,
			Class beanClass, String[] propertyNames) {
		IObservableMap[] result = new IObservableMap[propertyNames.length];
		for (int i = 0; i < propertyNames.length; i++) {
			result[i] = observeMap(domain, beanClass, propertyNames[i]);
		}
		return result;
	}

	/**
	 * Returns an observable list in the given realm tracking the
	 * collection-typed named property of the given bean object
	 * 
	 * @param realm
	 *            the realm
	 * @param bean
	 *            the object
	 * @param propertyName
	 *            the name of the collection-typed property
	 * @return an observable list tracking the collection-typed named property
	 *         of the given bean object
	 * 
	 */
	public static IObservableList observeList(Realm realm, Object bean,
			String propertyName) {
		return observeList(realm, bean, propertyName, null);
	}

	/**
	 * Returns an observable list in the given realm tracking the
	 * collection-typed named property of the given bean object
	 * 
	 * @param realm
	 *            the realm
	 * @param bean
	 *            the bean object
	 * @param propertyName
	 *            the name of the property
	 * @param elementType
	 *            type of the elements in the list. If <code>null</code> and
	 *            the property is an array the type will be inferred. If
	 *            <code>null</code> and the property type cannot be inferred
	 *            element type will be <code>null</code>.
	 * @return an observable list tracking the collection-typed named property
	 *         of the given bean object
	 */
	public static IObservableList observeList(Realm realm, Object bean,
			String propertyName, Class elementType) {
		PropertyDescriptor propertyDescriptor = getPropertyDescriptor(bean
				.getClass(), propertyName);
		elementType = getCollectionElementType(elementType, propertyDescriptor);

		return new JavaBeanObservableList(realm, bean, propertyDescriptor,
				elementType);
	}

	/**
	 * Returns an observable set in the given realm tracking the
	 * collection-typed named property of the given bean object
	 * 
	 * @param realm
	 *            the realm
	 * @param bean
	 *            the bean object
	 * @param propertyName
	 *            the name of the property
	 * @return an observable set tracking the collection-typed named property of
	 *         the given bean object
	 */
	public static IObservableSet observeSet(Realm realm, Object bean,
			String propertyName) {
		return observeSet(realm, bean, propertyName, null);
	}

	/**
	 * Returns a factory for creating obervable values tracking the given
	 * property of a particular bean object
	 * 
	 * @param realm
	 *            the realm to use
	 * @param propertyName
	 *            the name of the property
	 * @return an observable value factory
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
	 * Returns a factory for creating obervable lists tracking the given
	 * property of a particular bean object
	 * 
	 * @param realm
	 *            the realm to use
	 * @param propertyName
	 *            the name of the property
	 * @param elementType
	 * @return an observable list factory
	 */
	public static IObservableFactory listFactory(final Realm realm,
			final String propertyName, final Class elementType) {
		return new IObservableFactory() {
			public IObservable createObservable(Object target) {
				return observeList(realm, target, propertyName, elementType);
			}
		};
	}

	/**
	 * Returns a factory for creating obervable sets tracking the given property
	 * of a particular bean object
	 * 
	 * @param realm
	 *            the realm to use
	 * @param propertyName
	 *            the name of the property
	 * @return an observable set factory
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
	 propertyName), propertyType)</code>
	 * 
	 * @param realm
	 * @param master
	 * @param propertyName
	 * @param propertyType
	 *            can be <code>null</code>
	 * @return an observable value that tracks the current value of the named
	 *         property for the current value of the master observable value
	 * 
	 * @see MasterDetailObservables
	 */
	public static IObservableValue observeDetailValue(Realm realm,
			IObservableValue master, String propertyName, Class propertyType) {

		IObservableValue value = MasterDetailObservables.detailValue(master,
				valueFactory(realm, propertyName), propertyType);
		BeanObservableValueDecorator decorator = new BeanObservableValueDecorator(
				value, master, getValueTypePropertyDescriptor(master,
						propertyName));

		return decorator;
	}

	/**
	 * Helper method for
	 * <code>MasterDetailObservables.detailList(master, listFactory(realm,
	 propertyName, propertyType), propertyType)</code>
	 * 
	 * @param realm
	 * @param master
	 * @param propertyName
	 * @param propertyType
	 *            can be <code>null</code>
	 * @return an observable list that tracks the named property for the current
	 *         value of the master observable value
	 * 
	 * @see MasterDetailObservables
	 */
	public static IObservableList observeDetailList(Realm realm,
			IObservableValue master, String propertyName, Class propertyType) {
		IObservableList observableList = MasterDetailObservables.detailList(
				master, listFactory(realm, propertyName, propertyType),
				propertyType);
		BeanObservableListDecorator decorator = new BeanObservableListDecorator(
				observableList, master, getValueTypePropertyDescriptor(master,
						propertyName));

		return decorator;
	}

	/**
	 * Helper method for
	 * <code>MasterDetailObservables.detailSet(master, setFactory(realm,
	 propertyName), propertyType)</code>
	 * 
	 * @param realm
	 * @param master
	 * @param propertyName
	 * @param propertyType
	 *            can be <code>null</code>
	 * @return an observable set that tracks the named property for the current
	 *         value of the master observable value
	 * 
	 * @see MasterDetailObservables
	 */
	public static IObservableSet observeDetailSet(Realm realm,
			IObservableValue master, String propertyName, Class propertyType) {

		IObservableSet observableSet = MasterDetailObservables.detailSet(
				master, setFactory(realm, propertyName, propertyType),
				propertyType);
		BeanObservableSetDecorator decorator = new BeanObservableSetDecorator(
				observableSet, master, getValueTypePropertyDescriptor(master,
						propertyName));

		return decorator;
	}

	/**
	 * @param realm
	 * @param bean
	 * @param propertyName
	 * @param elementType
	 *            can be <code>null</code>
	 * @return an observable set that tracks the current value of the named
	 *         property for given bean object
	 */
	public static IObservableSet observeSet(Realm realm, Object bean,
			String propertyName, Class elementType) {
		PropertyDescriptor propertyDescriptor = getPropertyDescriptor(bean
				.getClass(), propertyName);
		elementType = getCollectionElementType(elementType, propertyDescriptor);

		return new JavaBeanObservableSet(realm, bean, propertyDescriptor,
				elementType);
	}

	/**
	 * @param realm
	 * @param propertyName
	 * @param elementType
	 *            can be <code>null</code>
	 * @return an observable set factory for creating observable sets
	 */
	public static IObservableFactory setFactory(final Realm realm,
			final String propertyName, final Class elementType) {
		return new IObservableFactory() {
			public IObservable createObservable(Object target) {
				return observeSet(realm, target, propertyName, elementType);
			}
		};
	}

	/**
	 * @param elementType
	 *            can be <code>null</code>
	 * @param propertyDescriptor
	 * @return type of the items in a collection/array property
	 */
	private static Class getCollectionElementType(Class elementType,
			PropertyDescriptor propertyDescriptor) {
		if (elementType == null) {
			Class propertyType = propertyDescriptor.getPropertyType();
			elementType = propertyType.isArray() ? propertyType
					.getComponentType() : Object.class;
		}

		return elementType;
	}

	/**
	 * @param observable
	 * @param propertyName
	 * @return property descriptor or <code>null</code>
	 */
	private static PropertyDescriptor getValueTypePropertyDescriptor(
			IObservableValue observable, String propertyName) {
		return (observable.getValueType() != null) ? getPropertyDescriptor(
				(Class) observable.getValueType(), propertyName) : null;
	}
}
