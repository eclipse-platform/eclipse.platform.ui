/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.UpdatableCollection;
import org.eclipse.jface.util.Assert;

/**
 * @since 3.2
 * 
 */
public class JavaBeanUpdatableCollection extends UpdatableCollection {
	private final Object object;

	private PropertyChangeListener collectionListener = new PropertyChangeListener() {
		public void propertyChange(java.beans.PropertyChangeEvent event) {
			if (!updating) {
				Object[] values = getValues();
				elementListenerSupport.setHookTargets(values);
				fireChangeEvent(ChangeEvent.CHANGE, null,
						null, ChangeEvent.POSITION_UNKNOWN);
			}
		}
	};

	private PropertyChangeListener elementListener = new PropertyChangeListener() {
		public void propertyChange(java.beans.PropertyChangeEvent event) {
			if (!updating) {
				Object[] values = getValues();
				int position = Arrays.asList(values).indexOf(event.getSource());
				if(position!=-1){
					fireChangeEvent(ChangeEvent.CHANGE, event.getSource(),
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
			if (list != null) {
				values = list.toArray();
			} else {
				values = new Object[] {};
			}
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

	public List getElements() {
		List elements = new ArrayList();
		for (int i = 0; i < getSize(); i++) {
			elements.add(getElement(i));
		}
		return elements;
	}
}