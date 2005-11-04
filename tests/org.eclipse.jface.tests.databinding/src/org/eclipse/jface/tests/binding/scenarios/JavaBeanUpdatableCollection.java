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

package org.eclipse.jface.tests.binding.scenarios;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.databinding.IChangeEvent;
import org.eclipse.jface.databinding.IUpdatableCollection;
import org.eclipse.jface.databinding.Updatable;

/**
 * @since 3.2
 * 
 */
public class JavaBeanUpdatableCollection extends Updatable implements
		IUpdatableCollection {
	private final Object object;

	private PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(java.beans.PropertyChangeEvent event) {
			if (!updating) {
				Object[] newValues = getValues();
				// make a copy of the set of objects listened to
				Set elementsToUnhook = new HashSet(elementsListenedTo);
				for (int i = 0; i < newValues.length; i++) {
					Object newValue = newValues[i];
					IdentityWrapper identityWrapper = new IdentityWrapper(newValue);
					if(!elementsToUnhook.remove(identityWrapper)) {
						// element was not in the set, add listener to it
						elementsListenedTo.add(identityWrapper);
						hookListener(newValue);
					}
				}
				for (Iterator it = elementsToUnhook.iterator(); it.hasNext();) {
					Object o = it.next();
					elementsListenedTo.remove(new IdentityWrapper(o));
					unhookListener(o);
				}
				fireChangeEvent(IChangeEvent.CHANGE, null,
						null, IChangeEvent.POSITION_UNKNOWN);
			}
		}
	};

	private boolean updating = false;

	private PropertyDescriptor descriptor;
	
	private Set elementsListenedTo = new HashSet();
	
	private static class IdentityWrapper {
		private final Object o;
		IdentityWrapper(Object o) {
			this.o = o;
		}
		public boolean equals(Object obj) {
			if(obj.getClass()!=IdentityWrapper.class) {
				return false;
			}
			return o==((IdentityWrapper)obj).o;
		}
		public int hashCode() {
			return System.identityHashCode(o);
		}
	}

	public JavaBeanUpdatableCollection(Object object,
			PropertyDescriptor descriptor) {
		this.object = object;
		this.descriptor = descriptor;
		hookListener(this.object);
	}

	private void hookListener(Object target) {
		Method addPropertyChangeListenerMethod = null;
		try {
			addPropertyChangeListenerMethod = target.getClass().getMethod(
					"addPropertyChangeListener",
					new Class[] { PropertyChangeListener.class });
		} catch (SecurityException e) {
			// ignore
		} catch (NoSuchMethodException e) {
			// ignore
		}
		if (addPropertyChangeListenerMethod != null) {
			try {
				addPropertyChangeListenerMethod.invoke(target,
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
	}

	private void unhookListener(Object target) {
		Method removePropertyChangeListenerMethod = null;
		try {
			removePropertyChangeListenerMethod = target.getClass().getMethod(
					"removePropertyChangeListener",
					new Class[] { PropertyChangeListener.class });
		} catch (SecurityException e) {
			// ignore
		} catch (NoSuchMethodException e) {
			// ignore
		}
		if (removePropertyChangeListenerMethod != null) {
			try {
				removePropertyChangeListenerMethod.invoke(target,
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
	}

	public void dispose() {
		super.dispose();
		for (Iterator it = elementsListenedTo.iterator(); it.hasNext();) {
			unhookListener(it.next());
		}
		unhookListener(object);
	}

	public int getSize() {
		return getValues().length;
	}

	private Object[] getValues() {
		try {
			return (Object[]) descriptor.getReadMethod().invoke(object,
					new Object[0]);
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
		throw new AssertionError("Could not read collection values");
	}

	public int addElement(Object value, int index) {
		throw new AssertionError("cannot add elements");
	}

	public void removeElement(int index) {
		throw new AssertionError("cannot remove elements");
	}

	public void setElement(int index, Object value) {
		updating = true;
		try {
			Object[] values = getValues();
			Object oldValue = values[index];
			values[index] = value;
			fireChangeEvent(IChangeEvent.CHANGE, oldValue, value, index);
		} finally {
			updating = false;
		}
	}

	public Object getElement(int index) {
		return getValues()[index];
	}

	public Class getElementType() {
		return descriptor.getPropertyType().getComponentType();
	}

}