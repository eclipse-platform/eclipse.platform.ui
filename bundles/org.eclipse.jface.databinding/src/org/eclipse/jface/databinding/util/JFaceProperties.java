/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding.util;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.jface.internal.databinding.util.JFaceProperty;
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * Helper class for providing {@link IObservableValue} instances for properties
 * of an object that fires property changes events to an
 * {@link IPropertyChangeListener}.
 * 
 * @since 1.3
 */
public class JFaceProperties {

	/**
	 * Returns a property for observing the property of the given model object
	 * whose getter and setter use the suffix fieldName in the same manner as a
	 * Java bean and which fires events to an {@link IPropertyChangeListener}
	 * for the given propertyName when the value of the field changes.
	 * 
	 * @param clazz
	 *            the class defining the getter and setter
	 * @param fieldName
	 *            the field name
	 * @param propertyName
	 *            the property name
	 * 
	 * @return an observable value
	 */
	public static IValueProperty value(Class clazz, String fieldName,
			String propertyName) {
		return new JFaceProperty(fieldName, propertyName, clazz);
	}

}
