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
 *     Matthew Hall - initial API and implementation (bug 195222)
 ******************************************************************************/

package org.eclipse.core.databinding.beans;

import org.eclipse.core.databinding.property.value.IValueProperty;

/**
 * An {@link IValueProperty} extension interface with convenience methods for
 * creating nested bean properties.
 *
 * @param <S> type of the source object
 * @param <T> type of the value of the property
 *
 * @since 1.2
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBeanValueProperty<S, T> extends IBeanProperty, IValueProperty<S, T> {
	/**
	 * Returns a master-detail combination of this property and the specified
	 * value property.
	 *
	 * @param propertyName
	 *            the value property to observe. May be nested e.g.
	 *            "parent.name"
	 * @return a master-detail combination of this property and the specified
	 *         value property.
	 * @see #value(IBeanValueProperty)
	 */
	public <T2> IBeanValueProperty<S, T2> value(String propertyName);

	/**
	 * Returns a master-detail combination of this property and the specified
	 * value property.
	 *
	 * @param propertyName
	 *            the value property to observe. May be nested e.g.
	 *            "parent.name"
	 * @param valueType
	 *            the value type of the named property
	 * @return a master-detail combination of this property and the specified
	 *         value property.
	 * @see #value(IBeanValueProperty)
	 */
	public <T2> IBeanValueProperty<S, T2> value(String propertyName, Class<T2> valueType);

	/**
	 * Returns a master-detail combination of this property and the specified
	 * value property. The returned property will observe the specified detail
	 * value property for the value of the master value property.
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * // Observes the Node-typed &quot;parent&quot; property of a Node object
	 * IBeanValueProperty parent = BeanProperties.value(Node.class, &quot;parent&quot;);
	 * // Observes the string-typed &quot;name&quot; property of a Node object
	 * IBeanValueProperty name = BeanProperties.value(Node.class, &quot;name&quot;);
	 * // Observes the name of the parent of a Node object.
	 * IBeanValueProperty parentName = parent.value(name);
	 * </pre>
	 *
	 * @param property
	 *            the detail property to observe
	 * @return a master-detail combination of this property and the specified
	 *         value property.
	 */
	public <T2> IBeanValueProperty<S, T2> value(IBeanValueProperty<? super T, T2> property);

	/**
	 * Returns a master-detail combination of this property and the specified
	 * list property.
	 *
	 * @param propertyName
	 *            the list property to observe
	 * @return a master-detail combination of this property and the specified
	 *         list property.
	 * @see #list(IBeanListProperty)
	 */
	public <E> IBeanListProperty<S, E> list(String propertyName);

	/**
	 * Returns a master-detail combination of this property and the specified
	 * list property.
	 *
	 * @param propertyName
	 *            the list property to observe
	 * @param elementType
	 *            the element type of the named property
	 * @return a master-detail combination of this property and the specified
	 *         list property.
	 * @see #list(IBeanListProperty)
	 */
	public <E> IBeanListProperty<S, E> list(String propertyName, Class<E> elementType);

	/**
	 * Returns a master-detail combination of this property and the specified
	 * list property. The returned property will observe the specified list
	 * property for the value of the master property.
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * // Observes the Node-typed &quot;parent&quot; property of a Node object.
	 * IBeanValueProperty parent = BeanProperties.value(Node.class, &quot;parent&quot;);
	 * // Observes the List-typed &quot;children&quot; property of a Node object
	 * // where the elements are Node objects
	 * IBeanListProperty children = BeanProperties.list(Node.class, &quot;children&quot;,
	 * 		Node.class);
	 * // Observes the children of the parent (siblings) of a Node object.
	 * IBeanListProperty siblings = parent.list(children);
	 * </pre>
	 *
	 * @param property
	 *            the detail property to observe
	 * @return a master-detail combination of this property and the specified
	 *         list property.
	 */
	public <E> IBeanListProperty<S, E> list(IBeanListProperty<? super T, E> property);

	/**
	 * Returns a master-detail combination of this property and the specified
	 * set property.
	 *
	 * @param propertyName
	 *            the set property to observe
	 * @return a master-detail combination of this property and the specified
	 *         set property.
	 * @see #set(IBeanSetProperty)
	 */
	public <E> IBeanSetProperty<S, E> set(String propertyName);

	/**
	 * Returns a master-detail combination of this property and the specified
	 * set property.
	 *
	 * @param propertyName
	 *            the set property to observe
	 * @param elementType
	 *            the element type of the named property
	 * @return a master-detail combination of this property and the specified
	 *         set property.
	 * @see #set(IBeanSetProperty)
	 */
	public <E> IBeanSetProperty<S, E> set(String propertyName, Class<E> elementType);

	/**
	 * Returns a master-detail combination of this property and the specified
	 * set property. The returned property will observe the specified set
	 * property for the value of the master property.
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * // Observes the Node-typed &quot;parent&quot; property of a Node object.
	 * IBeanValueProperty parent = BeanProperties.value(Node.class, &quot;parent&quot;);
	 * // Observes the Set-typed &quot;children&quot; property of a Node object
	 * // where the elements are Node objects
	 * IBeanSetProperty children = BeanProperties.set(Node.class, &quot;children&quot;,
	 * 		Node.class);
	 * // Observes the children of the parent (siblings) of a Node object.
	 * IBeanSetProperty siblings = parent.set(children);
	 * </pre>
	 *
	 * @param property
	 *            the detail property to observe
	 * @return a master-detail combination of this property and the specified
	 *         set property.
	 */
	public <E> IBeanSetProperty<S, E> set(IBeanSetProperty<? super T, E> property);

	/**
	 * Returns a master-detail combination of this property and the specified
	 * map property.
	 *
	 * @param propertyName
	 *            the map property to observe
	 * @return a master-detail combination of this property and the specified
	 *         map property.
	 * @see #map(IBeanMapProperty)
	 */
	public <K, V> IBeanMapProperty<S, K, V> map(String propertyName);

	/**
	 * Returns a master-detail combination of this property and the specified
	 * map property.
	 *
	 * @param propertyName
	 *            the map property to observe
	 * @param keyType
	 *            the key type of the named property
	 * @param valueType
	 *            the value type of the named property
	 * @return a master-detail combination of this property and the specified
	 *         map property.
	 * @see #map(IBeanMapProperty)
	 */
	public <K, V> IBeanMapProperty<S, K, V> map(String propertyName, Class<K> keyType, Class<V> valueType);

	/**
	 * Returns a master-detail combination of this property and the specified
	 * map property. The returned property will observe the specified map
	 * property for the value of the master property.
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * // Observes the Contact-typed &quot;supervisor&quot; property of a
	 * // Contact class
	 * IBeanValueProperty supervisor = BeanProperties.value(Contact.class,
	 * 		&quot;supervisor&quot;);
	 * // Observes the property &quot;phoneNumbers&quot; of a Contact object--a property mapping
	 * // from PhoneNumberType to PhoneNumber &quot;set-typed &quot;children&quot;,
	 * IBeanMapProperty phoneNumbers = BeanProperties.map(Contact.class,
	 * 		&quot;phoneNumbers&quot;, PhoneNumberType.class, PhoneNumber.class);
	 * // Observes the phone numbers of a contact's supervisor:
	 * IBeanMapProperty supervisorPhoneNumbers = supervisor.map(phoneNumbers);
	 * </pre>
	 *
	 * @param property
	 *            the detail property to observe
	 * @return a master-detail combination of this property and the specified
	 *         map property.
	 */
	public <K, V> IBeanMapProperty<S, K, V> map(IBeanMapProperty<? super T, K, V> property);
}
