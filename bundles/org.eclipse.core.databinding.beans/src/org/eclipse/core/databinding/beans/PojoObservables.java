/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 221704, 234686, 246625, 226289, 246782
 *******************************************************************************/

package org.eclipse.core.databinding.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.internal.databinding.beans.BeanObservableListDecorator;
import org.eclipse.core.internal.databinding.beans.BeanObservableMapDecorator;
import org.eclipse.core.internal.databinding.beans.BeanObservableSetDecorator;
import org.eclipse.core.internal.databinding.beans.BeanObservableValueDecorator;
import org.eclipse.core.internal.databinding.beans.JavaBeanObservableList;
import org.eclipse.core.internal.databinding.beans.JavaBeanObservableMap;
import org.eclipse.core.internal.databinding.beans.JavaBeanObservableSet;
import org.eclipse.core.internal.databinding.beans.JavaBeanObservableValue;
import org.eclipse.core.internal.databinding.beans.JavaBeanPropertyObservableMap;

/**
 * A factory for creating observable objects for POJOs (plain old java objects)
 * that conform to idea of an object with getters and setters but does not
 * provide {@link PropertyChangeEvent property change events} on change. This
 * factory is identical to {@link BeansObservables} except for this fact.
 * 
 * @since 1.1
 */
final public class PojoObservables {

	/**
	 * Returns an observable value in the default realm tracking the current
	 * value of the named property of the given pojo.
	 * 
	 * @param pojo
	 *            the object
	 * @param propertyName
	 *            the name of the property
	 * @return an observable value tracking the current value of the named
	 *         property of the given pojo
	 */
	public static IObservableValue observeValue(Object pojo, String propertyName) {
		return observeValue(Realm.getDefault(), pojo, propertyName);
	}

	/**
	 * Returns an observable value in the given realm tracking the current value
	 * of the named property of the given pojo.
	 * 
	 * @param realm
	 *            the realm
	 * @param pojo
	 *            the object
	 * @param propertyName
	 *            the name of the property
	 * @return an observable value tracking the current value of the named
	 *         property of the given pojo
	 */
	public static IObservableValue observeValue(Realm realm, Object pojo,
			String propertyName) {

		PropertyDescriptor descriptor = BeansObservables.getPropertyDescriptor(
				pojo.getClass(), propertyName);
		return new JavaBeanObservableValue(realm, pojo, descriptor, false);
	}

	/**
	 * Returns an observable map in the default realm tracking the current
	 * values of the named property for the pojos in the given set.
	 * 
	 * @param domain
	 *            the set of pojo objects
	 * @param pojoClass
	 *            the common base type of pojo objects that may be in the set
	 * @param propertyName
	 *            the name of the property
	 * @return an observable map tracking the current values of the named
	 *         property for the pojos in the given domain set
	 */
	public static IObservableMap observeMap(IObservableSet domain,
			Class pojoClass, String propertyName) {
		PropertyDescriptor descriptor = BeansObservables.getPropertyDescriptor(
				pojoClass, propertyName);
		return new JavaBeanObservableMap(domain, descriptor, false);
	}

	/**
	 * Returns an array of observable maps in the default realm tracking the
	 * current values of the named propertys for the pojos in the given set.
	 * 
	 * @param domain
	 *            the set of objects
	 * @param pojoClass
	 *            the common base type of objects that may be in the set
	 * @param propertyNames
	 *            the array of property names
	 * @return an array of observable maps tracking the current values of the
	 *         named propertys for the pojos in the given domain set
	 */
	public static IObservableMap[] observeMaps(IObservableSet domain,
			Class pojoClass, String[] propertyNames) {
		IObservableMap[] result = new IObservableMap[propertyNames.length];
		for (int i = 0; i < propertyNames.length; i++) {
			result[i] = observeMap(domain, pojoClass, propertyNames[i]);
		}
		return result;
	}

	/**
	 * Returns an observable map in the given realm tracking the map-typed named
	 * property of the given pojo object.
	 * 
	 * @param realm
	 *            the realm
	 * @param pojo
	 *            the pojo object
	 * @param propertyName
	 *            the name of the property
	 * @return an observable map tracking the map-typed named property of the
	 *         given pojo object
	 */
	public static IObservableMap observeMap(Realm realm, Object pojo,
			String propertyName) {
		return observeMap(realm, pojo, propertyName, null, null);
	}

	/**
	 * Returns an observable map in the given realm tracking the map-typed named
	 * property of the given pojo object.
	 * 
	 * @param realm
	 *            the realm
	 * @param pojo
	 *            the pojo object
	 * @param propertyName
	 *            the name of the property
	 * @param keyType
	 *            the element type of the observable map's key set, or
	 *            <code>null</code> if untyped
	 * @param valueType
	 *            the element type of the observable map's values collection, or
	 *            <code>null</code> if untyped
	 * @return an observable map tracking the map-typed named property of the
	 *         given pojo object
	 * @since 1.2
	 */
	public static IObservableMap observeMap(Realm realm, Object pojo,
			String propertyName, Class keyType, Class valueType) {
		PropertyDescriptor descriptor = BeansObservables.getPropertyDescriptor(
				pojo.getClass(), propertyName);
		return new JavaBeanPropertyObservableMap(realm, pojo, descriptor,
				keyType, valueType, false);
	}

	/**
	 * Returns an observable map in the default realm tracking the map-typed
	 * named property of the given pojo object.
	 * 
	 * @param pojo
	 *            the pojo object
	 * @param propertyName
	 *            the name of the property
	 * @return an observable map tracking the map-typed named property of the
	 *         given pojo object
	 * @since 1.2
	 */
	public static IObservableMap observeMap(Object pojo, String propertyName) {
		return observeMap(Realm.getDefault(), pojo, propertyName, null, null);
	}

	/**
	 * Returns an observable map in the default realm tracking the map-typed
	 * named property of the given pojo object.
	 * 
	 * @param pojo
	 *            the pojo object
	 * @param propertyName
	 *            the name of the property
	 * @param keyType
	 *            the element type of the observable map's key set, or
	 *            <code>null</code> if untyped
	 * @param valueType
	 *            the element type of the observable map's values collection, or
	 *            <code>null</code> if untyped
	 * @return an observable map tracking the map-typed named property of the
	 *         given pojo object
	 * @since 1.2
	 */
	public static IObservableMap observeMap(Object pojo, String propertyName,
			Class keyType, Class valueType) {
		return observeMap(Realm.getDefault(), pojo, propertyName, keyType,
				valueType);
	}

	/**
	 * Returns an observable list in the given realm tracking the
	 * collection-typed named property of the given pojo object. The returned
	 * list is mutable.
	 * 
	 * @param realm
	 *            the realm
	 * @param pojo
	 *            the object
	 * @param propertyName
	 *            the name of the collection-typed property
	 * @return an observable list tracking the collection-typed named property
	 *         of the given pojo object
	 * @see #observeList(Realm, Object, String, Class)
	 */
	public static IObservableList observeList(Realm realm, Object pojo,
			String propertyName) {
		return observeList(realm, pojo, propertyName, null);
	}

	/**
	 * Returns an observable list in the default realm tracking the
	 * collection-typed named property of the given pojo object. The returned
	 * list is mutable.
	 * 
	 * @param pojo
	 *            the object
	 * @param propertyName
	 *            the name of the collection-typed property
	 * @return an observable list tracking the collection-typed named property
	 *         of the given pojo object
	 * @see #observeList(Realm, Object, String, Class)
	 * @since 1.2
	 */
	public static IObservableList observeList(Object pojo, String propertyName) {
		return observeList(Realm.getDefault(), pojo, propertyName);
	}

	/**
	 * Returns an observable list in the given realm tracking the
	 * collection-typed named property of the given bean object. The returned
	 * list is mutable. When an item is added or removed the setter is invoked
	 * for the list on the parent bean to provide notification to other
	 * listeners via <code>PropertyChangeEvents</code>. This is done to
	 * provide the same behavior as is expected from arrays as specified in the
	 * bean spec in section 7.2.
	 * 
	 * @param realm
	 *            the realm
	 * @param pojo
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
	public static IObservableList observeList(Realm realm, Object pojo,
			String propertyName, Class elementType) {
		PropertyDescriptor propertyDescriptor = BeansObservables
				.getPropertyDescriptor(pojo.getClass(), propertyName);
		elementType = BeansObservables.getCollectionElementType(elementType,
				propertyDescriptor);

		return new JavaBeanObservableList(realm, pojo, propertyDescriptor,
				elementType, false);
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
	 * @param pojo
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
	public static IObservableList observeList(Object pojo, String propertyName,
			Class elementType) {
		return observeList(Realm.getDefault(), pojo, propertyName, elementType);
	}

	/**
	 * Returns an observable set in the given realm tracking the
	 * collection-typed named property of the given pojo object.
	 * 
	 * @param realm
	 *            the realm
	 * @param pojo
	 *            the pojo object
	 * @param propertyName
	 *            the name of the property
	 * @return an observable set tracking the collection-typed named property of
	 *         the given pojo object
	 */
	public static IObservableSet observeSet(Realm realm, Object pojo,
			String propertyName) {
		return observeSet(realm, pojo, propertyName, null);
	}

	/**
	 * Returns an observable set in the default realm tracking the
	 * collection-typed named property of the given pojo object.
	 * 
	 * @param pojo
	 *            the pojo object
	 * @param propertyName
	 *            the name of the property
	 * @return an observable set tracking the collection-typed named property of
	 *         the given pojo object
	 * @since 1.2
	 */
	public static IObservableSet observeSet(Object pojo, String propertyName) {
		return observeSet(Realm.getDefault(), pojo, propertyName);
	}

	/**
	 * Returns an observable set in the given realm tracking the
	 * collection-typed named property of the given pojo object.
	 * 
	 * @param realm
	 *            the realm
	 * @param pojo
	 *            the pojo object
	 * @param propertyName
	 *            the name of the property
	 * @param elementType
	 *            type of the elements in the set. If <code>null</code> and the
	 *            property is an array the type will be inferred. If
	 *            <code>null</code> and the property type cannot be inferred
	 *            element type will be <code>null</code>.
	 * @return an observable set that tracks the current value of the named
	 *         property for given pojo object
	 */
	public static IObservableSet observeSet(Realm realm, Object pojo,
			String propertyName, Class elementType) {
		PropertyDescriptor propertyDescriptor = BeansObservables
				.getPropertyDescriptor(pojo.getClass(), propertyName);
		elementType = BeansObservables.getCollectionElementType(elementType,
				propertyDescriptor);

		return new JavaBeanObservableSet(realm, pojo, propertyDescriptor,
				elementType, false);
	}

	/**
	 * Returns an observable set in the default realm, tracking the
	 * collection-typed named property of the given pojo object.
	 * 
	 * @param pojo
	 *            the pojo object
	 * @param propertyName
	 *            the name of the property
	 * @param elementType
	 *            type of the elements in the set. If <code>null</code> and the
	 *            property is an array the type will be inferred. If
	 *            <code>null</code> and the property type cannot be inferred
	 *            element type will be <code>null</code>.
	 * @return an observable set that tracks the current value of the named
	 *         property for given pojo object
	 * @since 1.2
	 */
	public static IObservableSet observeSet(Object pojo, String propertyName,
			Class elementType) {
		return observeSet(Realm.getDefault(), pojo, propertyName, elementType);
	}

	/**
	 * Returns a factory for creating observable values in the given realm,
	 * tracking the given property of a particular pojo object
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
	 * Returns a factory for creating observable values in the current default
	 * realm, tracking the given property of a particular pojo object
	 * 
	 * @param propertyName
	 *            the name of the property
	 * @return an observable value factory
	 * @since 1.2
	 */
	public static IObservableFactory valueFactory(String propertyName) {
		return valueFactory(Realm.getDefault(), propertyName);
	}

	/**
	 * Returns a factory for creating observable lists in the given realm,
	 * tracking the given property of a particular pojo object
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
	 * Returns a factory for creating observable lists in the current default
	 * realm, tracking the given property of a particular pojo object
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
	 * tracking the given property of a particular pojo object
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
	 * Returns a factory for creating observable sets in the current default
	 * realm, tracking the given property of a particular pojo object
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
	 * Returns a factory for creating observable set in the given realm,
	 * tracking the given property of a particular pojo object
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
	 * Returns a factory for creating observable set in the current default
	 * realm, tracking the given property of a particular pojo object
	 * 
	 * @param propertyName
	 *            the name of the property
	 * @param elementType
	 *            type of the elements in the set. If <code>null</code> and the
	 *            property is an array the type will be inferred. If
	 *            <code>null</code> and the property type cannot be inferred
	 *            element type will be <code>null</code>.
	 * @return an observable set factory for creating observable sets
	 * @since 1.2
	 */
	public static IObservableFactory setFactory(String propertyName,
			Class elementType) {
		return setFactory(Realm.getDefault(), propertyName, elementType);
	}

	/**
	 * Returns a factory for creating an observable map. The factory, when
	 * provided with a pojo object, will create an {@link IObservableMap} in the
	 * given realm that tracks the map-typed named property for the specified
	 * pojo.
	 * 
	 * @param realm
	 *            the realm assigned to observables created by the returned
	 *            factory.
	 * @param propertyName
	 *            the name of the property
	 * @return a factory for creating {@link IObservableMap} objects.
	 */
	public static IObservableFactory mapPropertyFactory(final Realm realm,
			final String propertyName) {
		return new IObservableFactory() {
			public IObservable createObservable(Object target) {
				return observeMap(realm, target, propertyName);
			}
		};
	}

	/**
	 * Returns a factory for creating an observable map. The factory, when
	 * provided with a pojo object, will create an {@link IObservableMap} in the
	 * current default realm that tracks the map-typed named property for the
	 * specified pojo.
	 * 
	 * @param propertyName
	 *            the name of the property
	 * @return a factory for creating {@link IObservableMap} objects.
	 * @since 1.2
	 */
	public static IObservableFactory mapPropertyFactory(String propertyName) {
		return mapPropertyFactory(Realm.getDefault(), propertyName);
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
	 * @deprecated Use
	 *             {@link #observeDetailValue(IObservableValue, String, Class)}
	 *             instead
	 */
	public static IObservableValue observeDetailValue(Realm realm,
			IObservableValue master, String propertyName, Class propertyType) {
		BeansObservables.warnIfDifferentRealms(realm, master.getRealm());

		IObservableValue value = MasterDetailObservables.detailValue(master,
				valueFactory(realm, propertyName), propertyType);
		BeanObservableValueDecorator decorator = new BeanObservableValueDecorator(
				value, BeansObservables.getValueTypePropertyDescriptor(master,
						propertyName));

		return decorator;
	}

	/**
	 * Helper method for
	 * <code>MasterDetailObservables.detailValue(master, valueFactory(master.getRealm, propertyName), propertyType)</code>
	 * 
	 * @param master
	 * @param propertyName
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
		return observeDetailValue(master.getRealm(), master, propertyName,
				propertyType);
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
		BeansObservables.warnIfDifferentRealms(realm, master.getRealm());
		IObservableList observableList = MasterDetailObservables.detailList(
				master, listFactory(realm, propertyName, propertyType),
				propertyType);
		BeanObservableListDecorator decorator = new BeanObservableListDecorator(
				observableList, BeansObservables
						.getValueTypePropertyDescriptor(master, propertyName));

		return decorator;
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
		return observeDetailList(master.getRealm(), master, propertyName,
				propertyType);
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
		BeansObservables.warnIfDifferentRealms(realm, master.getRealm());

		IObservableSet observableSet = MasterDetailObservables.detailSet(
				master, setFactory(realm, propertyName, propertyType),
				propertyType);
		BeanObservableSetDecorator decorator = new BeanObservableSetDecorator(
				observableSet, BeansObservables.getValueTypePropertyDescriptor(
						master, propertyName));

		return decorator;
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
		return observeDetailSet(master.getRealm(), master, propertyName,
				propertyType);
	}

	/**
	 * Helper method for
	 * <code>MasterDetailObservables.detailMap(master, mapFactory(realm, propertyName))</code>
	 * 
	 * @param realm
	 * @param master
	 * @param propertyName
	 * @return an observable map that tracks the map-type named property for the
	 *         current value of the master observable value.
	 * @deprecated Use {@link #observeDetailMap(IObservableValue, String)} instead
	 */
	public static IObservableMap observeDetailMap(Realm realm,
			IObservableValue master, String propertyName) {
		BeansObservables.warnIfDifferentRealms(realm, master.getRealm());
		IObservableMap observableMap = MasterDetailObservables.detailMap(
				master, mapPropertyFactory(realm, propertyName));
		BeanObservableMapDecorator decorator = new BeanObservableMapDecorator(
				observableMap, BeansObservables.getValueTypePropertyDescriptor(
						master, propertyName));
		return decorator;
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
		return observeDetailMap(master.getRealm(), master, propertyName);
	}
}
