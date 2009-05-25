/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 118516
 *******************************************************************************/
package org.eclipse.core.internal.databinding.beans;

import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This is a helper that will hook up and listen for
 * <code>PropertyChangeEvent</code> events for a set of target JavaBeans
 * 
 * @since 1.0
 */
public class BeanPropertyListenerSupport {
	/**
	 * Start listen to target (if it supports the JavaBean property change
	 * listener pattern)
	 * 
	 * @param bean
	 * @param propertyName
	 * @param listener
	 */
	public static void hookListener(Object bean, String propertyName,
			PropertyChangeListener listener) {
		Assert.isNotNull(bean, "Bean cannot be null"); //$NON-NLS-1$
		Assert.isNotNull(listener, "Listener cannot be null"); //$NON-NLS-1$
		Assert.isNotNull(propertyName, "Property name cannot be null"); //$NON-NLS-1$
		processListener(bean, propertyName, listener,
				"addPropertyChangeListener", "Could not attach listener to ");//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Stop listen to target
	 * 
	 * @param bean
	 * @param propertyName
	 * @param listener
	 */
	public static void unhookListener(Object bean, String propertyName,
			PropertyChangeListener listener) {
		Assert.isNotNull(bean, "Bean cannot be null"); //$NON-NLS-1$
		Assert.isNotNull(listener, "Listener cannot be null"); //$NON-NLS-1$
		Assert.isNotNull(propertyName, "Property name cannot be null"); //$NON-NLS-1$

		processListener(
				bean,
				propertyName,
				listener,
				"removePropertyChangeListener", "Cound not remove listener from "); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Invokes the method for the provided <code>methodName</code> attempting to
	 * first use the method with the property name and then the unnamed version.
	 * 
	 * @param bean
	 *            object to invoke the method on
	 * @param methodName
	 *            either addPropertyChangeListener or
	 *            removePropertyChangeListener
	 * @param message
	 *            string that will be prefixed to the target in an error message
	 * 
	 * @return <code>true</code> if the method was invoked successfully
	 */
	private static boolean processListener(Object bean, String propertyName,
			PropertyChangeListener listener, String methodName, String message) {
		Method method = null;
		Object[] parameters = null;

		try {
			try {
				method = bean.getClass().getMethod(
						methodName,
						new Class[] { String.class,
								PropertyChangeListener.class });

				parameters = new Object[] { propertyName, listener };
			} catch (NoSuchMethodException e) {
				method = bean.getClass().getMethod(methodName,
						new Class[] { PropertyChangeListener.class });

				parameters = new Object[] { listener };
			}
		} catch (SecurityException e) {
			// ignore
		} catch (NoSuchMethodException e) {
			log(IStatus.WARNING, message + bean, e);
		}

		if (method != null) {
			if (!method.isAccessible()) {
				method.setAccessible(true);
			}
			try {
				method.invoke(bean, parameters);
				return true;
			} catch (IllegalArgumentException e) {
				log(IStatus.WARNING, message + bean, e);
			} catch (IllegalAccessException e) {
				log(IStatus.WARNING, message + bean, e);
			} catch (InvocationTargetException e) {
				log(IStatus.WARNING, message + bean, e);
			}
		}
		return false;
	}

	/**
	 * Logs a message to the Data Binding logger.
	 */
	private static void log(int severity, String message, Throwable throwable) {
		if (BeansObservables.DEBUG) {
			Policy.getLog().log(
					new Status(severity, Policy.JFACE_DATABINDING, IStatus.OK,
							message, throwable));
		}
	}
}
