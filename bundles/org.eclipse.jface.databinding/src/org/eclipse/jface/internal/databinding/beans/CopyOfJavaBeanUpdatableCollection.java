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
package org.eclipse.jface.internal.databinding.beans;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.DataBinding;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.IUpdatableFactory;
import org.eclipse.jface.databinding.IUpdatableValue;
import org.eclipse.jface.databinding.JavaBeansScalarUpdatableValueFactory;
import org.eclipse.jface.databinding.Property;
import org.eclipse.jface.databinding.UpdatableCollection;
import org.eclipse.jface.util.Assert;

/**
 * @since 3.2
 * 
 */
public class CopyOfJavaBeanUpdatableCollection extends UpdatableCollection {

//	private PropertyChangeListener collectionListener = new PropertyChangeListener() {
//		public void propertyChange(java.beans.PropertyChangeEvent event) {
//			if (!updating) {
//				Object[] values = getValues();
//				elementListenerSupport.setHookTargets(values);
//				fireChangeEvent(ChangeEvent.CHANGE, null,
//						null, ChangeEvent.POSITION_UNKNOWN);
//			}
//		}
//	};
//
//	private PropertyChangeListener elementListener = new PropertyChangeListener() {
//		public void propertyChange(java.beans.PropertyChangeEvent event) {
//			if (!updating) {
//				Object[] values = getValues();
//				int position = Arrays.asList(values).indexOf(event.getSource());
//				if(position!=-1){
//					fireChangeEvent(ChangeEvent.CHANGE, event.getSource(),
//							event.getSource(), position);
//				}
//			}
//		}
//	};
//	
//	private ListenerSupport collectionListenSupport = new ListenerSupport(collectionListener);
//	private ListenerSupport elementListenerSupport = new ListenerSupport(elementListener);
	

	private boolean updating = false;

	private PropertyDescriptor descriptor;
	private Class elementType=null;
	
	private IUpdatableValue objectUpdatable;
	
	/**
	 * @param object The collection object we're binding
	 * @param descriptor A java.beans.PropertyDescriptor
	 * @param elementType  The element data type (still needed?)
	 */
	public CopyOfJavaBeanUpdatableCollection(Object object,
			PropertyDescriptor descriptor, Class elementType) {
		this.descriptor = descriptor;
		this.elementType = elementType;
		
		// Create an IUpdatableValue for the collection itself
		IDataBindingContext dbc = DataBinding.createContext(new IUpdatableFactory[] {
				new JavaBeansScalarUpdatableValueFactory()
		});
		objectUpdatable = (IUpdatableValue) dbc.createUpdatable(new Property(object, descriptor.getName()));
		objectUpdatable.addChangeListener(changeListener);
	}
	
	private IChangeListener changeListener = new IChangeListener() {
		public void handleChange(ChangeEvent changeEvent) {
			if (!updating) {
				fireChangeEvent(changeEvent.getChangeType(), null,
						null, ChangeEvent.POSITION_UNKNOWN);
			}
		}
	};

	public void dispose() {
		super.dispose();
		objectUpdatable.dispose();
	}

	private Collection collection() {
		return (Collection)objectUpdatable.getValue();
	}

	private Object[] array() {
		return (Object[])objectUpdatable.getValue();
	}

	public int computeSize() {
		if (descriptor.getPropertyType().isArray()) {
			return array().length;
		}
		return collection().size();
	}

	public int addElement(Object value, int index) {
		if (descriptor.getPropertyType().isArray())
			Assert.isTrue(false, "cannot add elements"); //$NON-NLS-1$		   
		
		Collection list = collection();
		list.add(value);	
		if (index <= 0 || index > list.size())
			index = list.size();
		fireChangeEvent(ChangeEvent.ADD, null, value, index);
		return index;
	}
	
	private Object findElement(int index) {
		Collection list = collection();
		Object o = null;
		int i=0;
		for (Iterator iter = list.iterator(); iter.hasNext(); i++) {
			if (index==i) {
				o = iter.next();
				break;
			}
			iter.next();
		}
		return o;
	}

	public void removeElement(int index) {
		if (descriptor.getPropertyType().isArray())
			Assert.isTrue(false, "cannot remove elements"); //$NON-NLS-1$		   
		
		Object o = findElement(index);
		if (o!=null) {
		   collection().remove(o);
		   fireChangeEvent(ChangeEvent.REMOVE, o, null, index);
		}		
	}

	public void setElement(int index, Object value) {
		if (updating) {
			return;
		}
		updating = true;
		try {
			Object oldValue;
			if (descriptor.getPropertyType().isArray()) {
				oldValue = array()[index];
				array()[index] = value;
			} else {
				Collection c = collection();
				oldValue = findElement(index);
				try {
					// Try to preserve collection order if possible
					Method setMethod = c.getClass().getMethod("set", new Class[] {Integer.TYPE, Object.class}); //$NON-NLS-1$
					setMethod.invoke(c, new Object[] {new Integer(index), value});
				} catch (Exception e) {
					// If we can't preserve order
					if (oldValue != null) {
						c.remove(oldValue);
					}
					c.add(value);
				}
			}
			fireChangeEvent(ChangeEvent.CHANGE, oldValue, value, index);
		} finally {
			updating = false;
		}
	}

	public Object computeElement(int index) {
		if (descriptor.getPropertyType().isArray()) {
			return array()[index];
		}
		return findElement(index);
	}

	public Class getElementType() {		
		return elementType;
	}

}

