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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IUpdatableCollection;
import org.eclipse.jface.databinding.Updatable;
import org.eclipse.jface.util.Assert;

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
				fireChangeEvent(ChangeEvent.CHANGE, null,
						null, ChangeEvent.POSITION_UNKNOWN);
			}
		}
	};

	private boolean updating = false;

	private PropertyDescriptor descriptor;
	
	private Class elementType=null;
	
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

	/**
	 * @param object
	 * @param descriptor
	 */
	public JavaBeanUpdatableCollection(Object object,
			PropertyDescriptor descriptor, Class elementType) {
		this.object = object;
		this.descriptor = descriptor;
		this.elementType = elementType;		
		hookListener(this.object);
	}

	private void hookListener(Object target) {
		Method addPropertyChangeListenerMethod = null;
		try {
			addPropertyChangeListenerMethod = target.getClass().getMethod(
					"addPropertyChangeListener", //$NON-NLS-1$
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
					"removePropertyChangeListener", //$NON-NLS-1$
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

	
	private Object primGetValues() {
		try {
		   Method readMethod = descriptor.getReadMethod();
			if (!readMethod.isAccessible()) {
				readMethod.setAccessible(true);
			}
			return readMethod.invoke(object, new Object[0]);
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
		Assert.isTrue(false, "Could not read collection values"); //$NON-NLS-1$
		return null;
	}
	
	private Object[] getValues() {
		Object[] values = null;

		Object result = primGetValues();
		if (descriptor.getPropertyType().isArray())
			values = (Object[])result;
		else {
			//TODO add jUnit for POJO (var. SettableValue) collections  
			Collection list = (Collection) result;
			values = list.toArray();
		}
		return values;
	}

	public int addElement(Object value, int index) {
		if (descriptor.getPropertyType().isArray())
			Assert.isTrue(false, "cannot add elements"); //$NON-NLS-1$		   
		
		Collection list = (Collection)primGetValues();
		if (index <= 0 || index > list.size())
			index = list.size();
		list.add(value);	
		fireChangeEvent(ChangeEvent.ADD, null, value, index);
		return index;
		
	}

	public void removeElement(int index) {
		if (descriptor.getPropertyType().isArray())
			Assert.isTrue(false, "cannot remove elements"); //$NON-NLS-1$		   
		
		Collection list = (Collection)primGetValues();
		Object o = null;
		int i=0;
		for (Iterator iter = list.iterator(); iter.hasNext(); i++) {
			if (index==i) {
				o = iter.next();
				break;
			}
			iter.next();
		}
		if (o!=null) {
		   list.remove(o);
		   fireChangeEvent(ChangeEvent.REMOVE, o, null, index);
		}		
	}

	public void setElement(int index, Object value) {
		updating = true;
		try {
			Object[] values = getValues();
			Object oldValue = values[index];
			values[index] = value;
			fireChangeEvent(ChangeEvent.CHANGE, oldValue, value, index);
		} finally {
			updating = false;
		}
	}

	public Object getElement(int index) {
		return getValues()[index];
	}

	public Class getElementType() {		
		return elementType;
	}

}