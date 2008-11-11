/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *     Brad Reynolds - bug 164134, 171616
 *     Matthew Hall - bug 246103
 *******************************************************************************/
package org.eclipse.core.internal.databinding.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.databinding.BindingException;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.internal.databinding.Util;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 1.0
 * 
 */
public class JavaBeanObservableValue extends AbstractObservableValue implements IBeanObservable {
	private final Object object;
	private boolean updating = false;

	private final PropertyDescriptor propertyDescriptor;
	private ListenerSupport listenerSupport;

	private boolean attachListeners;

	// Applicable only while hasListeners() == true
	private Object cachedValue;

	/**
	 * @param realm
	 * @param object
	 * @param descriptor
	 */
	public JavaBeanObservableValue(Realm realm, Object object,
			PropertyDescriptor descriptor) {
		this(realm, object, descriptor, true);
	}

	/**
	 * @param realm
	 * @param object
	 * @param descriptor
	 * @param attachListeners
	 */
	public JavaBeanObservableValue(Realm realm, Object object,
			PropertyDescriptor descriptor, boolean attachListeners) {
		super(realm);
		this.object = object;
		this.propertyDescriptor = descriptor;
		this.attachListeners = attachListeners;
	}

	protected void firstListenerAdded() {
		if (!attachListeners) {
			return;
		}

		if (listenerSupport == null) {
			PropertyChangeListener listener = new PropertyChangeListener() {
				public void propertyChange(
						final java.beans.PropertyChangeEvent event) {
					if (!updating) {
						getRealm().exec(new Runnable() {
							public void run() {
								Object oldValue = event.getOldValue();
								Object newValue = event.getNewValue();
								if (oldValue == null && newValue == null) {
									// this condition is provided for in the
									// bean spec, and indicates that an 
									// unknown change occured.

									oldValue = cachedValue;
									newValue = doGetValue();
								}
								cachedValue = newValue;
								if (!Util.equals(oldValue, newValue)) {
									fireValueChange(Diffs.createValueDiff(
											oldValue, newValue));
								}
							}
						});
					}
				}
			};
			listenerSupport = new ListenerSupport(listener, propertyDescriptor
					.getName());
		}
		
		listenerSupport.hookListener(object);
		cachedValue = doGetValue();
	}

	public void doSetValue(Object value) {
		updating = true;
		try {
			Object oldValue = doGetValue();
			
			if (!Util.equals(oldValue, value)) {
				Method writeMethod = propertyDescriptor.getWriteMethod();
				if (!writeMethod.isAccessible()) {
					writeMethod.setAccessible(true);
				}
				writeMethod.invoke(object, new Object[] { value });
			}
			
			if (hasListeners()) {
				// oldValue contains the live value which may be different from
				// the cached value if the bean does not have listener API or
				// does not fire events properly. For consistency we want to
				// provide the cached value as the old value, rather than the
				// live value so that consumers that hook/unhook listeners can
				// do so without maintaining caches of their own.
				oldValue = cachedValue;
				cachedValue = doGetValue();
				if (!Util.equals(oldValue, cachedValue)) {
					fireValueChange(Diffs
							.createValueDiff(oldValue, cachedValue));
				}
			}
		} catch (InvocationTargetException e) {
			/*
			 * InvocationTargetException wraps any exception thrown by the
			 * invoked method.
			 */
			throw new RuntimeException(e.getCause());
		} catch (Exception e) {
			if (BeansObservables.DEBUG) {
				Policy
						.getLog()
						.log(
								new Status(
										IStatus.WARNING,
										Policy.JFACE_DATABINDING,
										IStatus.OK,
										"Could not change value of " + object + "." + propertyDescriptor.getName(), e)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} finally {
			updating = false;
		}
	}

	public Object doGetValue() {
		try {
			Method readMethod = propertyDescriptor.getReadMethod();
			if (readMethod == null) {
				throw new BindingException(propertyDescriptor.getName()
						+ " property does not have a read method."); //$NON-NLS-1$
			}
			if (!readMethod.isAccessible()) {
				readMethod.setAccessible(true);
			}
			return readMethod.invoke(object, null);
		} catch (InvocationTargetException e) {
			/*
			 * InvocationTargetException wraps any exception thrown by the
			 * invoked method.
			 */
			throw new RuntimeException(e.getCause());
		} catch (Exception e) {
			if (BeansObservables.DEBUG) {
				Policy
						.getLog()
						.log(
								new Status(
										IStatus.WARNING,
										Policy.JFACE_DATABINDING,
										IStatus.OK,
										"Could not read value of " + object + "." + propertyDescriptor.getName(), e)); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return null;
		}
	}

	protected void lastListenerRemoved() {
		unhookListener();
	}

	private void unhookListener() {
		cachedValue = null;
		if (listenerSupport != null) {
			listenerSupport.dispose();
			listenerSupport = null;
		}
	}

	public Object getValueType() {
		return propertyDescriptor.getPropertyType();
	}

	public Object getObserved() {
		return object;
	}

	public PropertyDescriptor getPropertyDescriptor() {
		return propertyDescriptor;
	}

	public synchronized void dispose() {
		unhookListener();
		super.dispose();
	}
}
