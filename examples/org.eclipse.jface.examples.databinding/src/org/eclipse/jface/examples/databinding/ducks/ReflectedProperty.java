/*******************************************************************************
 * Copyright (c) 2006, 2014 The Pampered Chef and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.ducks;

/**
 * Encapsulates a single JavaBeans-style property
 *
 * @since 3.3
 */
public class ReflectedProperty {
	private String propertyName;
	private ReflectedMethod getter;
	private ReflectedMethod setter;

	/**
	 * Construct a ReflectedProperty on some object, given the property name.
	 *
	 * @param object The object
	 * @param propertyName The property name
	 */
	public ReflectedProperty(Object object, String propertyName) {
		this.propertyName = propertyName;
		getter = new ReflectedMethod(object, makeGetterName(propertyName), new Class[] {});
		if (!getter.exists()) {
			getter = new ReflectedMethod(object, makeBooleanGetterName(propertyName), new Class[] {});
			if (!getter.exists()) {
				throw new IllegalArgumentException("Cannot find getter for " + propertyName);
			}
		}
		setter = new ReflectedMethod(object, makeSetterName(propertyName), new Class[] {getter.getType()});
	}

	private String makeBooleanGetterName(String propertyName) {
		return "is" + capitalize(propertyName);
	}

	private String makeSetterName(String propertyName) {
		return "set" + capitalize(propertyName);
	}

	private String makeGetterName(String propertyName) {
		return "get" + capitalize(propertyName);
	}

	private String capitalize(String string) {
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	/**
	 * Return the property's type.  This is the same as the type returned by
	 * the getter.
	 *
	 * @return The property's data type.
	 */
	public Class getType() {
		return getter.getType();
	}

	/**
	 * Return the property's value.
	 *
	 * @return The value in the property.
	 */
	public Object get() {
		return getter.invoke(new Object[] {});
	}

	/**
	 * Set the property's value.  If the property is read-only, the request
	 * is ignored.
	 *
	 * @param newValue The value to set.
	 */
	public void set(Object newValue) {
		setter.invoke(new Object[] {newValue});
	}

	/**
	 * Returns if the property is read-only.
	 *
	 * @return false if the property has a setter; true otherwise.
	 */
	public boolean isReadOnly() {
		return !setter.exists();
	}

	/**
	 * Returns the property's name.
	 *
	 * @return The property name.
	 */
	public String getPropertyName() {
		return propertyName;
	}
}
