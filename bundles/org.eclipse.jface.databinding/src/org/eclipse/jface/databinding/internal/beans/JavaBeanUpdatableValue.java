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
package org.eclipse.jface.databinding.internal.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.UpdatableValue;

/**
 * @since 3.2
 * 
 */
public class JavaBeanUpdatableValue extends UpdatableValue {
	private final Object object;

	private PropertyChangeListener listener;

	private boolean updating = false;

	private final PropertyDescriptor propertyDescriptor;

	/**
	 * @param object
	 * @param descriptor
	 */
	public JavaBeanUpdatableValue(Object object, PropertyDescriptor descriptor) {
		this.object = object;
		this.propertyDescriptor = descriptor;
		hookListener();
	}

	private void hookListener() {
		listener = new PropertyChangeListener() {
			public void propertyChange(java.beans.PropertyChangeEvent event) {
				if (!updating) {
					fireChangeEvent(ChangeEvent.CHANGE, event.getOldValue(),
							event.getNewValue());
				}
			}
		};
		Method addPropertyChangeListenerMethod = null;
		try {
			addPropertyChangeListenerMethod = object.getClass().getMethod(
					"addPropertyChangeListener", //$NON-NLS-1$
					new Class[] { PropertyChangeListener.class });
		} catch (SecurityException e) {
			// ignore
		} catch (NoSuchMethodException e) {
			// ignore
		}
		if (addPropertyChangeListenerMethod != null) {
			try {
				addPropertyChangeListenerMethod.invoke(object,
						new Object[] { listener });
				return;
			} catch (IllegalArgumentException e) {
				// ignore
			} catch (IllegalAccessException e) {
				// ignore
			} catch (InvocationTargetException e) {
				// ignore
			}
		}
		// set listener to null because we are not listening
		listener = null;
	}

	public void setValue(Object value) {
		updating = true;
		try {
			Object oldValue = getValue();
			Method writeMethod = propertyDescriptor.getWriteMethod();
			if (!writeMethod.isAccessible()) {
				writeMethod.setAccessible(true);
			}
			writeMethod.invoke(object,
					new Object[] { value });
			fireChangeEvent(ChangeEvent.CHANGE, oldValue, getValue());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			updating = false;
		}
	}

	public Object getValue() {
		try {
			Method readMethod = propertyDescriptor.getReadMethod();
			if (!readMethod.isAccessible()) {
				readMethod.setAccessible(true);
			}
			return readMethod.invoke(object, null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void dispose() {
		super.dispose();
		if (listener != null) {
			Method removePropertyChangeListenerMethod = null;
			try {
				removePropertyChangeListenerMethod = object.getClass()
						.getMethod("removePropertyChangeListener", //$NON-NLS-1$
								new Class[] { PropertyChangeListener.class });
			} catch (SecurityException e) {
				// best effort - ignore
			} catch (NoSuchMethodException e) {
				// best effort - ignore
			}
			if (removePropertyChangeListenerMethod != null) {
				try {
					removePropertyChangeListenerMethod.invoke(object,
							new Object[] { listener });
				} catch (IllegalArgumentException e) {
					// best effort - ignore
				} catch (IllegalAccessException e) {
					// best effort - ignore
				} catch (InvocationTargetException e) {
					// best effort - ignore
				}
			}
			// set listener to null because we are no longer listening
			listener = null;
		}
	}

	public Class getValueType() {
		return propertyDescriptor.getPropertyType();
	}

}