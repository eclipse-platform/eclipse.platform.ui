/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.examples.binding.javabean;

import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;

import org.eclipse.jface.binding.IChangeEvent;
import org.eclipse.jface.binding.UpdatableValue;

/**
 * @since 3.2
 *
 */
public class JavaBeanUpdatableValue extends UpdatableValue {
	private final Object object;

	private String propertyName;

	private Method getMethod;

	private Method setMethod;

	private PropertyChangeListener listener;

	private boolean updating = false;

	/**
	 * @param object
	 * @param propertyName
	 */
	public JavaBeanUpdatableValue(final Object object, String propertyName) {
		this.object = object;
		this.propertyName = propertyName;
		hookListener();
	}

	private Method getSetMethod() {
		if (setMethod != null)
			return setMethod;
		try {
			String setMethodName = setMethodName(propertyName);
			setMethod = object.getClass().getMethod(setMethodName,
					new Class[] { getGetMethod().getReturnType() });
		} catch (NoSuchMethodException e) {
		}
		return setMethod;
	}

	private Method getGetMethod() {
		if (getMethod != null)
			return getMethod;
		try {
			String getMethodName = getMethodName(propertyName);
			Method getMethod = object.getClass().getMethod(getMethodName, null);
		} catch (NoSuchMethodException e) {
		}
		return getMethod;
	}

	/**
	 * @param propertyName
	 * @return
	 */
	public static String getMethodName(String propertyName) {
		// TODO: <gm> need to deal with BeanInfo overrides </gm>
		StringBuffer getMethodName = new StringBuffer();
		getMethodName.append("get");
		getMethodName.append(propertyName.substring(0, 1).toUpperCase());
		getMethodName.append(propertyName.substring(1));
		return getMethodName.toString();
	}

	/**
	 * @param propertyName
	 * @return
	 */
	public static String setMethodName(String propertyName) {
		// TODO: <gm> need to deal with BeanInfo overrides </gm>
		StringBuffer getMethodName = new StringBuffer();
		getMethodName.append("set");
		getMethodName.append(propertyName.substring(0, 1).toUpperCase());
		getMethodName.append(propertyName.substring(1));
		return getMethodName.toString();
	}

	private void hookListener() {
		listener = new PropertyChangeListener() {
			public void propertyChange(java.beans.PropertyChangeEvent event) {
				fireChangeEvent(IChangeEvent.CHANGE, event.getOldValue(), event
						.getNewValue());
			}
		};
		// See if the object implements the API for property change listener
	}

	public void setValue(Object value) {
		updating = true;
		try {
			Object oldValue = getValue();
			getSetMethod().invoke(object, new Object[] { value });
			fireChangeEvent(IChangeEvent.CHANGE, oldValue, getValue());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			updating = false;
		}
	}

	public Object getValue() {
		try {
			return getGetMethod().invoke(object, null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void dispose() {
		super.dispose();
	}

	public String toString() {
		return propertyName.toString() + " of " + object.toString();
	}

	public Class getValueType() {
		return getGetMethod().getReturnType();
	}

}