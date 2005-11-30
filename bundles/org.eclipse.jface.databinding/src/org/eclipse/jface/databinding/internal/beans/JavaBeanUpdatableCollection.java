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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.databinding.IChangeEvent;
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

	private PropertyChangeListener collectionListener = new PropertyChangeListener() {
		public void propertyChange(java.beans.PropertyChangeEvent event) {
			if (!updating) {
				Object[] values = getValues();
				elementListenerSupport.setHookTargets(values);
				fireChangeEvent(IChangeEvent.CHANGE, null,
						null, IChangeEvent.POSITION_UNKNOWN);
			}
		}
	};

	private PropertyChangeListener elementListener = new PropertyChangeListener() {
		public void propertyChange(java.beans.PropertyChangeEvent event) {
			if (!updating) {
				Object[] values = getValues();
				int position = Arrays.asList(values).indexOf(event.getSource());
				if(position!=-1){
					fireChangeEvent(IChangeEvent.CHANGE, event.getSource(),
							event.getSource(), position);
				}
			}
		}
	};
	
	private boolean updating = false;

	private PropertyDescriptor descriptor;
	
	private Class elementType=null;
	
	private ListenerSupport collectionListenSupport = new ListenerSupport(collectionListener);
	
	private ListenerSupport elementListenerSupport = new ListenerSupport(elementListener);
	
	
	
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
		collectionListenSupport.hookListener(this.object);
		elementListenerSupport.setHookTargets(getValues());		
	}

	private void unhookListener(PropertyChangeListener listener, Object target) {
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
		elementListenerSupport.dispose();
		collectionListenSupport.dispose();
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
		fireChangeEvent(IChangeEvent.ADD, null, value, index);
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
		   fireChangeEvent(IChangeEvent.REMOVE, o, null, index);
		}		
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
		return elementType;
	}

}