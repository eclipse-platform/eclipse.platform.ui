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

import org.eclipse.core.databinding.property.set.ISetProperty;

/**
 * An {@link ISetProperty} extension interface with convenience methods for
 * creating nested bean properties.
 *
 * @param <S> type of the source object
 * @param <E> type of the elements in the set
 *
 * @since 1.2
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBeanSetProperty<S, E> extends IBeanProperty, ISetProperty<S, E> {
	/**
	 * Returns a master-detail combination of this property and the specified
	 * value property.
	 *
	 * @param propertyName
	 *            the value property to observe. May be nested e.g.
	 *            "parent.name"
	 * @return a master-detail combination of this property and the specified
	 *         value property.
	 * @see #values(IBeanValueProperty)
	 */
	public <V> IBeanMapProperty<S, E, V> values(String propertyName);

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
	 * @see #values(IBeanValueProperty)
	 */
	public <V> IBeanMapProperty<S, E, V> values(String propertyName, Class<V> valueType);

	/**
	 * Returns a master-detail combination of this property and the specified
	 * value property. The returned property will observe the specified value
	 * property for all elements observed by this set property, mapping from
	 * this set property's elements (keys) to the specified value property's
	 * value for each element (values).
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * // Observes the set-typed &quot;children&quot; property of a Person object,
	 * // where the elements are Person objects
	 * IBeanSetProperty children = BeanProperties.set(Person.class, &quot;children&quot;,
	 * 		Person.class);
	 * // Observes the string-typed &quot;name&quot; property of a Person object
	 * IBeanValueProperty name = BeanProperties.value(Person.class, &quot;name&quot;);
	 * // Observes a map of children objects to their respective names.
	 * IBeanMapProperty childrenNames = children.values(name);
	 * </pre>
	 *
	 * @param property
	 *            the detail property to observe
	 * @return a master-detail combination of this property and the specified
	 *         value property.
	 */
	public <V> IBeanMapProperty<S, E, V> values(IBeanValueProperty<? super E, V> property);
}
