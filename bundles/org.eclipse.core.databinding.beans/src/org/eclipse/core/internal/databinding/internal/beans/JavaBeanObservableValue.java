/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *     Brad Reynolds - bug 164134
 *******************************************************************************/
package org.eclipse.core.internal.databinding.internal.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.databinding.BindingException;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 1.0
 * 
 */
public class JavaBeanObservableValue extends AbstractObservableValue {
	private final Object object;

	private PropertyChangeListener listener;

	private boolean updating = false;

	private final PropertyDescriptor propertyDescriptor;
	private final Class overrideType;

	/**
	 * @param realm
	 * @param object
	 * @param descriptor
	 * @param overrideType
	 */
	public JavaBeanObservableValue(Realm realm, Object object,
			PropertyDescriptor descriptor, Class overrideType) {
		super(realm);
		this.object = object;
		this.propertyDescriptor = descriptor;
		this.overrideType = overrideType;
	}

	protected void firstListenerAdded() {
		listener = new PropertyChangeListener() {
			public void propertyChange(java.beans.PropertyChangeEvent event) {
				if (!updating
						&& event.getPropertyName().equals(
								propertyDescriptor.getName())) {
					final ValueDiff diff = Diffs.createValueDiff(event.getOldValue(),
											event.getNewValue());
					getRealm().exec(new Runnable(){
						public void run() {
							fireValueChange(diff);
						}});
				}
			}
		};
		Method addPropertyChangeListenerMethod = null;
		try {
			addPropertyChangeListenerMethod = object.getClass().getMethod(
					"addPropertyChangeListener", //$NON-NLS-1$
					new Class[] { String.class, PropertyChangeListener.class });
		} catch (SecurityException e) {
			// ignore
		} catch (NoSuchMethodException e) {
			// ignore
		}
		if (addPropertyChangeListenerMethod != null) {
			try {
				addPropertyChangeListenerMethod.invoke(object, new Object[] {
						propertyDescriptor.getName(), listener });
				return;
			} catch (IllegalArgumentException e) {
				if (BeansObservables.DEBUG) {
					Policy
							.getLog()
							.log(
									new Status(
											IStatus.WARNING,
											Policy.JFACE_DATABINDING,
											IStatus.OK,
											"Could not attach listener to " + object, e)); //$NON-NLS-1$
				}
			} catch (IllegalAccessException e) {
				if (BeansObservables.DEBUG) {
					Policy
							.getLog()
							.log(
									new Status(
											IStatus.WARNING,
											Policy.JFACE_DATABINDING,
											IStatus.OK,
											"Could not attach listener to " + object, e)); //$NON-NLS-1$
				}
			} catch (InvocationTargetException e) {
				if (BeansObservables.DEBUG) {
					Policy
							.getLog()
							.log(
									new Status(
											IStatus.WARNING,
											Policy.JFACE_DATABINDING,
											IStatus.OK,
											"Could not attach listener to " + object, e)); //$NON-NLS-1$
				}
			}
		}
		// set listener to null because we are not listening
		listener = null;
	}

	public void doSetValue(Object value) {
		updating = true;
		try {
			Object oldValue = doGetValue();
			Method writeMethod = propertyDescriptor.getWriteMethod();
			if (!writeMethod.isAccessible()) {
				writeMethod.setAccessible(true);
			}
			writeMethod.invoke(object, new Object[] { value });
			fireValueChange(Diffs.createValueDiff(oldValue, doGetValue()));
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
		if (listener != null) {
			Method removePropertyChangeListenerMethod = null;
			try {
				removePropertyChangeListenerMethod = object.getClass()
						.getMethod(
								"removePropertyChangeListener", //$NON-NLS-1$
								new Class[] { String.class,
										PropertyChangeListener.class });
			} catch (SecurityException e) {
				if (BeansObservables.DEBUG) {
					Policy
							.getLog()
							.log(
									new Status(
											IStatus.WARNING,
											Policy.JFACE_DATABINDING,
											IStatus.OK,
											"Could not remove listener from " + object, e)); //$NON-NLS-1$
				}
			} catch (NoSuchMethodException e) {
				if (BeansObservables.DEBUG) {
					Policy
							.getLog()
							.log(
									new Status(
											IStatus.WARNING,
											Policy.JFACE_DATABINDING,
											IStatus.OK,
											"Could not remove listener from " + object, e)); //$NON-NLS-1$
				}
			}
			if (removePropertyChangeListenerMethod != null) {
				try {
					removePropertyChangeListenerMethod.invoke(object,
							new Object[] { propertyDescriptor.getName(),
									listener });
				} catch (IllegalArgumentException e) {
					if (BeansObservables.DEBUG) {
						Policy
								.getLog()
								.log(
										new Status(
												IStatus.WARNING,
												Policy.JFACE_DATABINDING,
												IStatus.OK,
												"Could not remove listener from " + object, e)); //$NON-NLS-1$
					}
				} catch (IllegalAccessException e) {
					if (BeansObservables.DEBUG) {
						Policy
								.getLog()
								.log(
										new Status(
												IStatus.WARNING,
												Policy.JFACE_DATABINDING,
												IStatus.OK,
												"Could not remove listener from " + object, e)); //$NON-NLS-1$
					}
				} catch (InvocationTargetException e) {
					if (BeansObservables.DEBUG) {
						Policy
								.getLog()
								.log(
										new Status(
												IStatus.WARNING,
												Policy.JFACE_DATABINDING,
												IStatus.OK,
												"Could not remove listener from " + object, e)); //$NON-NLS-1$
					}
				}
			}
			// set listener to null because we are no longer listening
			listener = null;
		}
	}

	public Object getValueType() {
		Class type = propertyDescriptor.getPropertyType();
		if (type == Object.class && overrideType != null)
			type = overrideType;
		return type;
	}
}
