/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bugs 164268, 171616, 147515
 *     Matthew Hall - bug 221704, 234686, 246625, 226289, 246782, 194734,
 *                    195222, 247997
 *     Thomas Kratz - bug 213787
 *******************************************************************************/
package org.eclipse.core.databinding.beans;

import java.beans.PropertyDescriptor;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.internal.databinding.beans.BeanObservableListDecorator;
import org.eclipse.core.internal.databinding.beans.BeanObservableMapDecorator;
import org.eclipse.core.internal.databinding.beans.BeanObservableSetDecorator;
import org.eclipse.core.internal.databinding.beans.BeanObservableValueDecorator;
import org.eclipse.core.internal.databinding.beans.BeanPropertyHelper;
import org.eclipse.core.internal.databinding.beans.Util;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * A factory for creating observable objects of Java objects that conform to the
 * <a href="http://java.sun.com/products/javabeans/docs/spec.html">JavaBean
 * specification</a> for bound properties.
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
	 *            the name of the property. May be nested e.g. "parent.name"
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
	 *            the name of the property. May be nested e.g. "parent.name"
	 * @return an observable value tracking the current value of the named
	 *         property of the given bean
	 */
	public static IObservableValue observeValue(Realm realm, Object bean,
			String propertyName) {
		return BeanProperties.value(bean.getClass(), propertyName).observe(
				realm, bean);
	}

	/**
	 * Returns an observable map in the given observable set's realm tracking
	 * the current values of the named property for the beans in the given set.
	 * Elements in the set which do not have the named property will have null
	 * values, and attempts to {@link IObservableMap#put(Object, Object) put}
	 * values to these elements will be ignored.
	 * 
	 * @param domain
	 *            the set of bean objects
	 * @param propertyName
	 *            the name of the property. May be nested e.g. "parent.name"
	 * @return an observable map tracking the current values of the named
	 *         property for the beans in the given domain set
	 * @since 1.2
	 */
	public static IObservableMap observeMap(IObservableSet domain,
			String propertyName) {
		return BeanProperties.value(propertyName).observeDetail(domain);
	}

	/**
	 * Returns an observable map in the given observable set's realm tracking
	 * the current values of the named property for the beans in the given set.
	 * 
	 * @param domain
	 *            the set of bean objects
	 * @param beanClass
	 *            the common base type of bean objects that may be in the set
	 * @param propertyName
	 *            the name of the property. May be nested e.g. "parent.name"
	 * @return an observable map tracking the current values of the named
	 *         property for the beans in the given domain set
	 */
	public static IObservableMap observeMap(IObservableSet domain,
			Class beanClass, String propertyName) {
		return BeanProperties.value(beanClass, propertyName).observeDetail(
				domain);
	}

	/**
	 * Returns an observable map in the given realm tracking the map-typed named
	 * property of the given bean object.
	 * 
	 * @param realm
	 *            the realm
	 * @param bean
	 *            the bean object
	 * @param propertyName
	 *            the name of the property. May be nested e.g. "parent.name"
	 * @return an observable map tracking the map-typed named property of the
	 *         given bean object
	 * @since 1.1
	 */
	public static IObservableMap observeMap(Realm realm, Object bean,
			String propertyName) {
		return observeMap(realm, bean, propertyName, null, null);
	}

	/**
	 * Returns an observable map in the given realm tracking the map-typed named
	 * property of the given bean object.
	 * 
	 * @param realm
	 *            the realm
	 * @param bean
	 *            the bean object
	 * @param propertyName
	 *            the name of the property. May be nested e.g. "parent.name"
	 * @param keyType
	 *            the element type of the observable map's key set, or
	 *            <code>null</code> if untyped
	 * @param valueType
	 *            the element type of the observable map's values collection, or
	 *            <code>null</code> if untyped
	 * @return an observable map tracking the map-typed named property of the
	 *         given bean object
	 * @since 1.2
	 */
	public static IObservableMap observeMap(Realm realm, Object bean,
			String propertyName, Class keyType, Class valueType) {
		return BeanProperties.map(bean.getClass(), propertyName, keyType,
				valueType).observe(realm, bean);
	}

	/**
	 * Returns an observable map in the default realm tracking the map-typed
	 * named property of the given bean object.
	 * 
	 * @param bean
	 *            the bean object
	 * @param propertyName
	 *            the name of the property. May be nested e.g. "parent.name"
	 * @return an observable map tracking the map-typed named property of the
	 *         given bean object
	 * @since 1.2
	 */
	public static IObservableMap observeMap(Object bean, String propertyName) {
		return observeMap(Realm.getDefault(), bean, propertyName, null, null);
	}

	/**
	 * Returns an observable map in the default realm tracking the map-typed
	 * named property of the given bean object.
	 * 
	 * @param bean
	 *            the bean object
	 * @param propertyName
	 *            the name of the property
	 * @param keyType
	 *            the element type of the observable map's key set, or
	 *            <code>null</code> if untyped
	 * @param valueType
	 *            the element type of the observable map's values collection, or
	 *            <code>null</code> if untyped
	 * @return an observable map tracking the map-typed named property of the
	 *         given bean object
	 * @since 1.2
	 */
	public static IObservableMap observeMap(Object bean, String propertyName,
			Class keyType, Class valueType) {
		return observeMap(Realm.getDefault(), bean, propertyName, keyType,
				valueType);
	}

	/**
	 * Returns an array of observable maps in the given observable set's realm
	 * tracking the current values of the named properties for the beans in the
	 * given set. Elements in the set which do not have the named property will
	 * have null values, and attempts to
	 * {@link IObservableMap#put(Object, Object) put} values to these elements
	 * will be ignored.
	 * 
	 * @param domain
	 *            the set of objects
	 * @param propertyNames
	 *            the array of property names. May be nested e.g. "parent.name"
	 * @return an array of observable maps tracking the current values of the
	 *         named propertys for the beans in the given domain set
	 * @since 1.2
	 */
	public static IObservableMap[] observeMaps(IObservableSet domain,
			String[] propertyNames) {
		IObservableMap[] result = new IObservableMap[propertyNames.length];
		for (int i = 0; i < propertyNames.length; i++) {
			result[i] = observeMap(domain, propertyNames[i]);
		}
		return result;
	}

	/**
	 * Returns an array of observable maps in the given observable set's realm
	 * tracking the current values of the named properties for the beans in the
	 * given set.
	 * 
	 * @param domain
	 *            the set of objects
	 * @param beanClass
	 *            the common base type of objects that may be in the set
	 * @param propertyNames
	 *            the array of property names. May be nested e.g. "parent.name"
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
	 * collection-typed named property of the given bean object. The returned
	 * list is mutable.
	 * 
	 * @param realm
	 *            the realm
	 * @param bean
	 *            the object
	 * @param propertyName
	 *            the name of the collection-typed property
	 * @return an observable list tracking the collection-typed named property
	 *         of the given bean object
	 * @see #observeList(Realm, Object, String, Class)
	 */
	public static IObservableList observeList(Realm realm, Object bean,
			String propertyName) {
		return observeList(realm, bean, propertyName, null);
	}

	/**
	 * Returns an observable list in the default realm tracking the
	 * collection-typed named property of the given bean object. The returned
	 * list is mutable.
	 * 
	 * @param bean
	 *            the object
	 * @param propertyName
	 *            the name of the collection-typed property
	 * @return an observable list tracking the collection-typed named property
	 *         of the given bean object
	 * @see #observeList(Realm, Object, String, Class)
	 * @since 1.2
	 */
	public static IObservableList observeList(Object bean, String propertyName) {
		return observeList(Realm.getDefault(), bean, propertyName);
	}

	/**
	 * Returns an observable list in the given realm tracking the
	 * collection-typed named property of the given bean object. The returned
	 * list is mutable. When an item is added or removed the setter is invoked
	 * for the list on the parent bean to provide notification to other
	 * listeners via <code>PropertyChangeEvents</code>. This is done to provide
	 * the same behavior as is expected from arrays as specified in the bean
	 * spec in section 7.2.
	 * 
	 * @param realm
	 *            the realm
	 * @param bean
	 *            the bean object
	 * @param propertyName
	 *            the name of the property
	 * @param elementType
	 *            type of the elements in the list. If <code>null</code> and the
	 *            property is an array the type will be inferred. If
	 *            <code>null</code> and the property type cannot be inferred
	 *            element type will be <code>null</code>.
	 * @return an observable list tracking the collection-typed named property
	 *         of the given bean object
	 */
	public static IObservableList observeList(Realm realm, Object bean,
			String propertyName, Class elementType) {
		return BeanProperties.list(bean.getClass(), propertyName, elementType)
				.observe(realm, bean);
	}

	/**
	 * Returns an observable list in the default realm tracking the
	 * collection-typed named property of the given bean object. The returned
	 * list is mutable. When an item is added or removed the setter is invoked
	 * for the list on the parent bean to provide notification to other
	 * listeners via <code>PropertyChangeEvents</code>. This is done to provide
	 * the same behavior as is expected from arrays as specified in the bean
	 * spec in section 7.2.
	 * 
	 * @param bean
	 *            the bean object
	 * @param propertyName
	 *            the name of the property
	 * @param elementType
	 *            type of the elements in the list. If <code>null</code> and the
	 *            property is an array the type will be inferred. If
	 *            <code>null</code> and the property type cannot be inferred
	 *            element type will be <code>null</code>.
	 * @return an observable list tracking the collection-typed named property
	 *         of the given bean object
	 * @since 1.2
	 */
	public static IObservableList observeList(Object bean, String propertyName,
			Class elementType) {
		return observeList(Realm.getDefault(), bean, propertyName, elementType);
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
	 * Returns an observable set in the default realm tracking the
	 * collection-typed named property of the given bean object
	 * 
	 * @param bean
	 *            the bean object
	 * @param propertyName
	 *            the name of the property
	 * @return an observable set tracking the collection-typed named property of
	 *         the given bean object
	 * @since 1.2
	 */
	public static IObservableSet observeSet(Object bean, String propertyName) {
		return observeSet(Realm.getDefault(), bean, propertyName);
	}

	/**
	 * Returns a factory for creating observable values in the given realm,
	 * tracking the given property of a particular bean object
	 * 
	 * @param realm
	 *            the realm to use
	 * @param propertyName
	 *            the name of the property. May be nested e.g. "parent.name"
	 * @return an observable value factory
	 */
	public static IObservableFactory valueFactory(final Realm realm,
			final String propertyName) {
		return BeanProperties.value(propertyName).valueFactory(realm);
	}

	/**
	 * Returns a factory for creating observable values in the current default
	 * realm, tracking the given property of a particular bean object
	 * 
	 * @param propertyName
	 *            the name of the property. May be nested e.g. "parent.name"
	 * @return an observable value factory
	 * @since 1.2
	 */
	public static IObservableFactory valueFactory(String propertyName) {
		return valueFactory(Realm.getDefault(), propertyName);
	}

	/**
	 * Returns a factory for creating observable lists in the given realm,
	 * tracking the given property of a particular bean object
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
		return BeanProperties.list(propertyName, elementType)
				.listFactory(realm);
	}

	/**
	 * Returns a factory for creating observable lists in the current default
	 * realm, tracking the given property of a particular bean object
	 * 
	 * @param propertyName
	 *            the name of the property
	 * @param elementType
	 * @return an observable list factory
	 * @since 1.2
	 */
	public static IObservableFactory listFactory(String propertyName,
			Class elementType) {
		return listFactory(Realm.getDefault(), propertyName, elementType);
	}

	/**
	 * Returns a factory for creating observable sets in the given realm,
	 * tracking the given property of a particular bean object
	 * 
	 * @param realm
	 *            the realm to use
	 * @param propertyName
	 *            the name of the property
	 * @return an observable set factory
	 */
	public static IObservableFactory setFactory(final Realm realm,
			final String propertyName) {
		return BeanProperties.set(propertyName).setFactory(realm);
	}

	/**
	 * Returns a factory for creating observable sets in the current default
	 * realm, tracking the given property of a particular bean object
	 * 
	 * @param propertyName
	 *            the name of the property
	 * @return an observable set factory
	 * @since 1.2
	 */
	public static IObservableFactory setFactory(String propertyName) {
		return setFactory(Realm.getDefault(), propertyName);
	}

	/**
	 * Helper method for
	 * <code>MasterDetailObservables.detailValue(master, valueFactory(realm,
	 propertyName), propertyType)</code>
	 * 
	 * @param realm
	 * @param master
	 * @param propertyName
	 *            the name of the property. May be nested e.g. "parent.name"
	 * @param propertyType
	 *            can be <code>null</code>
	 * @return an observable value that tracks the current value of the named
	 *         property for the current value of the master observable value
	 * 
	 * @see MasterDetailObservables
	 * @deprecated Use
	 *             {@link #observeDetailValue(IObservableValue, String, Class)}
	 *             instead
	 */
	public static IObservableValue observeDetailValue(Realm realm,
			IObservableValue master, String propertyName, Class propertyType) {
		warnIfDifferentRealms(realm, master.getRealm());

		IObservableValue value = MasterDetailObservables.detailValue(master,
				BeanProperties.value(propertyName, propertyType).valueFactory(
						realm), propertyType);
		return new BeanObservableValueDecorator(value, BeanPropertyHelper
				.getValueTypePropertyDescriptor(master, propertyName));
	}

	/* package */static void warnIfDifferentRealms(Realm detailRealm,
			Realm masterRealm) {
		if (!Util.equals(detailRealm, masterRealm)) {
			Throwable throwable = new Throwable();
			throwable.fillInStackTrace();
			String message = "Detail realm (" + detailRealm //$NON-NLS-1$
					+ ") not equal to master realm (" //$NON-NLS-1$
					+ masterRealm + ")"; //$NON-NLS-1$
			Policy.getLog().log(
					new Status(IStatus.WARNING, Policy.JFACE_DATABINDING,
							message, throwable));
		}
	}

	/**
	 * Helper method for
	 * <code>MasterDetailObservables.detailValue(master, valueFactory(master.getRealm(), propertyName), propertyType)</code>
	 * 
	 * @param master
	 * @param propertyName
	 *            the name of the property. May be nested e.g. "parent.name"
	 * @param propertyType
	 *            can be <code>null</code>
	 * @return an observable value that tracks the current value of the named
	 *         property for the current value of the master observable value
	 * 
	 * @see MasterDetailObservables
	 * @since 1.2
	 */
	public static IObservableValue observeDetailValue(IObservableValue master,
			String propertyName, Class propertyType) {
		Class beanClass = null;
		if (master.getValueType() instanceof Class)
			beanClass = (Class) master.getValueType();
		return observeDetailValue(master, beanClass, propertyName, propertyType);
	}

	/**
	 * Helper method for
	 * <code>MasterDetailObservables.detailValue(master, valueFactory(realm,
	 * propertyName), propertyType)</code>. This method returns an
	 * {@link IBeanObservable} with a {@link PropertyDescriptor} based on the
	 * given master type and property name.
	 * 
	 * @param realm
	 *            the realm
	 * @param master
	 *            the master observable value, for example tracking the
	 *            selection in a list
	 * @param masterType
	 *            the type of the master observable value
	 * @param propertyName
	 *            the property name. May be nested e.g. "parent.name"
	 * @param propertyType
	 *            can be <code>null</code>
	 * @return an observable value that tracks the current value of the named
	 *         property for the current value of the master observable value
	 * 
	 * @see MasterDetailObservables
	 * @since 1.1
	 * @deprecated Use
	 *             {@link #observeDetailValue(IObservableValue, Class, String, Class)}
	 *             instead.
	 */
	public static IObservableValue observeDetailValue(Realm realm,
			IObservableValue master, Class masterType, String propertyName,
			Class propertyType) {
		warnIfDifferentRealms(realm, master.getRealm());
		Assert.isNotNull(masterType, "masterType cannot be null"); //$NON-NLS-1$
		IObservableValue value = MasterDetailObservables.detailValue(master,
				BeanProperties.value(masterType, propertyName, propertyType)
						.valueFactory(realm), propertyType);
		return new BeanObservableValueDecorator(value, BeanPropertyHelper
				.getPropertyDescriptor(masterType, propertyName));
	}

	/**
	 * Helper method for
	 * <code>MasterDetailObservables.detailValue(master, valueFactory(master.getRealm(), propertyName), propertyType)</code>
	 * . This method returns an {@link IBeanObservable} with a
	 * {@link PropertyDescriptor} based on the given master type and property
	 * name.
	 * 
	 * @param master
	 *            the master observable value, for example tracking the
	 *            selection in a list
	 * @param masterType
	 *            the type of the master observable value
	 * @param propertyName
	 *            the property name. May be nested e.g. "parent.name"
	 * @param propertyType
	 *            can be <code>null</code>
	 * @return an observable value that tracks the current value of the named
	 *         property for the current value of the master observable value
	 * 
	 * @see MasterDetailObservables
	 * @since 1.2
	 */
	public static IObservableValue observeDetailValue(IObservableValue master,
			Class masterType, String propertyName, Class propertyType) {
		return BeanProperties.value(masterType, propertyName, propertyType)
				.observeDetail(master);
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
	 * @deprecated Use
	 *             {@link #observeDetailList(IObservableValue, String, Class)}
	 *             instead
	 */
	public static IObservableList observeDetailList(Realm realm,
			IObservableValue master, String propertyName, Class propertyType) {
		warnIfDifferentRealms(realm, master.getRealm());
		IObservableList observableList = MasterDetailObservables.detailList(
				master, BeanProperties.list(propertyName, propertyType)
						.listFactory(realm), propertyType);
		return new BeanObservableListDecorator(observableList,
				BeanPropertyHelper.getValueTypePropertyDescriptor(master,
						propertyName));
	}

	/**
	 * Helper method for
	 * <code>MasterDetailObservables.detailList(master, listFactory(master.getRealm(), propertyName, propertyType), propertyType)</code>
	 * 
	 * @param master
	 * @param propertyName
	 * @param propertyType
	 *            can be <code>null</code>
	 * @return an observable list that tracks the named property for the current
	 *         value of the master observable value
	 * 
	 * @see MasterDetailObservables
	 * @since 1.2
	 */
	public static IObservableList observeDetailList(IObservableValue master,
			String propertyName, Class propertyType) {
		Class beanClass = null;
		if (master.getValueType() instanceof Class)
			beanClass = (Class) master.getValueType();
		return BeanProperties.list(beanClass, propertyName, propertyType)
				.observeDetail(master);
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
	 * @deprecated Use
	 *             {@link #observeDetailSet(IObservableValue, String, Class)}
	 *             instead.
	 */
	public static IObservableSet observeDetailSet(Realm realm,
			IObservableValue master, String propertyName, Class propertyType) {
		warnIfDifferentRealms(realm, master.getRealm());

		IObservableSet observableSet = MasterDetailObservables.detailSet(
				master, BeanProperties.set(propertyName, propertyType)
						.setFactory(realm), propertyType);
		return new BeanObservableSetDecorator(observableSet, BeanPropertyHelper
				.getValueTypePropertyDescriptor(master, propertyName));
	}

	/**
	 * Helper method for
	 * <code>MasterDetailObservables.detailSet(master, setFactory(master.getRealm(), propertyName), propertyType)</code>
	 * 
	 * @param master
	 * @param propertyName
	 * @param propertyType
	 *            can be <code>null</code>
	 * @return an observable set that tracks the named property for the current
	 *         value of the master observable value
	 * 
	 * @see MasterDetailObservables
	 * @since 1.2
	 */
	public static IObservableSet observeDetailSet(IObservableValue master,
			String propertyName, Class propertyType) {
		Class beanClass = null;
		if (master.getValueType() instanceof Class)
			beanClass = (Class) master.getValueType();
		return BeanProperties.set(beanClass, propertyName, propertyType)
				.observeDetail(master);
	}

	/**
	 * Helper method for
	 * <code>MasterDetailObservables.detailMap(master, mapFactory(realm, propertyName))</code>
	 * 
	 * @param realm
	 *            the realm
	 * @param master
	 * @param propertyName
	 * @return an observable map that tracks the map-type named property for the
	 *         current value of the master observable value.
	 * @since 1.1
	 * @deprecated Use {@link #observeDetailMap(IObservableValue, String)}
	 *             instead
	 */
	public static IObservableMap observeDetailMap(Realm realm,
			IObservableValue master, String propertyName) {
		warnIfDifferentRealms(realm, master.getRealm());
		IObservableMap observableMap = MasterDetailObservables.detailMap(
				master, BeanProperties.map(propertyName).mapFactory(realm));
		return new BeanObservableMapDecorator(observableMap, BeanPropertyHelper
				.getValueTypePropertyDescriptor(master, propertyName));
	}

	/**
	 * Helper method for
	 * <code>MasterDetailObservables.detailMap(master, mapFactory(master.getRealm(), propertyName))</code>
	 * 
	 * @param master
	 * @param propertyName
	 * @return an observable map that tracks the map-type named property for the
	 *         current value of the master observable value.
	 * @since 1.2
	 */
	public static IObservableMap observeDetailMap(IObservableValue master,
			String propertyName) {
		Class beanClass = null;
		if (master.getValueType() instanceof Class)
			beanClass = (Class) master.getValueType();
		return BeanProperties.map(beanClass, propertyName)
				.observeDetail(master);
	}

	/**
	 * Returns an observable set in the given realm tracking the
	 * collection-typed named property of the given bean object. The returned
	 * set is mutable. When an item is added or removed the setter is invoked
	 * for the set on the parent bean to provide notification to other listeners
	 * via <code>PropertyChangeEvents</code>. This is done to provide the same
	 * behavior as is expected from arrays as specified in the bean spec in
	 * section 7.2.
	 * 
	 * @param realm
	 *            the realm
	 * @param bean
	 *            the bean object
	 * @param propertyName
	 *            the name of the property
	 * @param elementType
	 *            type of the elements in the set. If <code>null</code> and the
	 *            property is an array the type will be inferred. If
	 *            <code>null</code> and the property type cannot be inferred
	 *            element type will be <code>null</code>.
	 * @return an observable set tracking the collection-typed named property of
	 *         the given bean object
	 */
	public static IObservableSet observeSet(Realm realm, Object bean,
			String propertyName, Class elementType) {
		return BeanProperties.set(bean.getClass(), propertyName, elementType)
				.observe(realm, bean);
	}

	/**
	 * Returns an observable set in the current default realm tracking the
	 * collection-typed named property of the given bean object. The returned
	 * set is mutable. When an item is added or removed the setter is invoked
	 * for the set on the parent bean to provide notification to other listeners
	 * via <code>PropertyChangeEvents</code>. This is done to provide the same
	 * behavior as is expected from arrays as specified in the bean spec in
	 * section 7.2.
	 * 
	 * @param bean
	 *            the bean object
	 * @param propertyName
	 *            the name of the property
	 * @param elementType
	 *            type of the elements in the set. If <code>null</code> and the
	 *            property is an array the type will be inferred. If
	 *            <code>null</code> and the property type cannot be inferred
	 *            element type will be <code>null</code>.
	 * @return an observable set tracking the collection-typed named property of
	 *         the given bean object
	 * @since 1.2
	 */
	public static IObservableSet observeSet(Object bean, String propertyName,
			Class elementType) {
		return observeSet(Realm.getDefault(), bean, propertyName, elementType);
	}

	/**
	 * Returns a factory for creating observable sets in the given realm,
	 * tracking the given property of a particular bean object
	 * 
	 * @param realm
	 *            the realm to use
	 * @param propertyName
	 *            the name of the property
	 * @param elementType
	 *            type of the elements in the set. If <code>null</code> and the
	 *            property is an array the type will be inferred. If
	 *            <code>null</code> and the property type cannot be inferred
	 *            element type will be <code>null</code>.
	 * @return a factory for creating observable sets in the given realm,
	 *         tracking the given property of a particular bean object
	 */
	public static IObservableFactory setFactory(final Realm realm,
			final String propertyName, final Class elementType) {
		return BeanProperties.set(propertyName, elementType).setFactory(realm);
	}

	/**
	 * Returns a factory for creating observable sets in the current default
	 * realm, tracking the given property of a particular bean object
	 * 
	 * @param propertyName
	 *            the name of the property
	 * @param elementType
	 *            type of the elements in the set. If <code>null</code> and the
	 *            property is an array the type will be inferred. If
	 *            <code>null</code> and the property type cannot be inferred
	 *            element type will be <code>null</code>.
	 * @return a factory for creating observable sets in the given realm,
	 *         tracking the given property of a particular bean object
	 * @since 1.2
	 */
	public static IObservableFactory setFactory(String propertyName,
			Class elementType) {
		return setFactory(Realm.getDefault(), propertyName, elementType);
	}

	/**
	 * Returns a factory for creating an observable map. The factory, when
	 * provided with an {@link IObservableSet}, will create an
	 * {@link IObservableMap} in the same realm as the underlying set that
	 * tracks the current values of the named property for the beans in the
	 * given set.
	 * 
	 * @param beanClass
	 *            the common base type of bean objects that may be in the set
	 * @param propertyName
	 *            the name of the property. May be nested e.g. "parent.name"
	 * @return a factory for creating {@link IObservableMap} objects
	 * 
	 * @since 1.1
	 */
	public static IObservableFactory setToMapFactory(final Class beanClass,
			final String propertyName) {
		return new IObservableFactory() {
			public IObservable createObservable(Object target) {
				return observeMap((IObservableSet) target, beanClass,
						propertyName);
			}
		};
	}

	/**
	 * Returns a factory for creating an observable map. The factory, when
	 * provided with a bean object, will create an {@link IObservableMap} in the
	 * given realm that tracks the map-typed named property for the specified
	 * bean.
	 * 
	 * @param realm
	 *            the realm assigned to observables created by the returned
	 *            factory.
	 * @param propertyName
	 *            the name of the property
	 * @return a factory for creating {@link IObservableMap} objects.
	 * @since 1.1
	 */
	public static IObservableFactory mapPropertyFactory(final Realm realm,
			final String propertyName) {
		return BeanProperties.map(propertyName).mapFactory(realm);
	}

	/**
	 * Returns a factory for creating an observable map. The factory, when
	 * provided with a bean object, will create an {@link IObservableMap} in the
	 * current default realm that tracks the map-typed named property for the
	 * specified bean.
	 * 
	 * @param propertyName
	 *            the name of the property
	 * @return a factory for creating {@link IObservableMap} objects.
	 * @since 1.2
	 */
	public static IObservableFactory mapPropertyFactory(String propertyName) {
		return mapPropertyFactory(Realm.getDefault(), propertyName);
	}
}
