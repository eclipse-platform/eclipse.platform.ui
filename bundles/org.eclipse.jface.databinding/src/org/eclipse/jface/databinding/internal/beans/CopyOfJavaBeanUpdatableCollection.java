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

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.IUpdatable;
import org.eclipse.jface.databinding.IUpdatableCollection;
import org.eclipse.jface.databinding.IUpdatableValue;
import org.eclipse.jface.databinding.Property;
import org.eclipse.jface.databinding.Updatable;
import org.eclipse.jface.util.Assert;

/**
 * @since 3.2
 * 
 */
public class CopyOfJavaBeanUpdatableCollection extends Updatable implements
		IUpdatableCollection {

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

	private final Object object;
	private PropertyDescriptor descriptor;
	private Class elementType=null;
	private IDataBindingContext dataBindingContext;
	
	private IUpdatableValue objectUpdatable;
	private List elementUpdatables = new LinkedList();
	
	/**
	 * @param propertyDescription The property description object
	 * @param dataBindingContext The parent data binding context
	 * @param object The collection object we're binding
	 * @param descriptor A java.beans.PropertyDescriptor
	 * @param elementType  The element data type (still needed?)
	 */
	public CopyOfJavaBeanUpdatableCollection(Object propertyDescription,
			IDataBindingContext dataBindingContext, Object object,
			PropertyDescriptor descriptor, Class elementType) {
		this.object = object;
		this.descriptor = descriptor;
		this.elementType = elementType;
		this.dataBindingContext = dataBindingContext;
		
		// Create an IUpdatableValue for the collection itself
		objectUpdatable = (IUpdatableValue) dataBindingContext
				.createUpdatable(propertyDescription);

		// Create an IUpdatableValue for each collection element
		Object result = objectUpdatable.getValue();
		Object[] values = null;
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
		for (int i = 0; i < values.length; i++) {
			CollectionElement element = new CollectionElement(values[i]);
			elementUpdatables.add(dataBindingContext.createUpdatable(new Property(element, "value"))); //$NON-NLS-1$
		} 
	}

	public void dispose() {
		super.dispose();
		for (Iterator i = elementUpdatables.iterator(); i.hasNext();) {
			IUpdatable element = (IUpdatable) i.next();
			element.dispose();
		}
		objectUpdatable.dispose();
	}

	public int getSize() {
		return elementUpdatables.size();
	}

	public int addElement(Object value, int index) {
		if (descriptor.getPropertyType().isArray())
			Assert.isTrue(false, "cannot add elements"); //$NON-NLS-1$		   
		
		Collection list = (Collection)objectUpdatable.getValue();
		list.add(value);	
		if (index <= 0 || index > list.size())
			index = list.size();
		fireChangeEvent(ChangeEvent.ADD, null, value, index);
		return index;
	}

	public void removeElement(int index) {
		if (descriptor.getPropertyType().isArray())
			Assert.isTrue(false, "cannot remove elements"); //$NON-NLS-1$		   
		
		Collection list = (Collection)objectUpdatable.getValue();
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
//			Object[] values = getValues();
//			Object oldValue = values[index];
//			values[index] = value;
//			fireChangeEvent(ChangeEvent.CHANGE, oldValue, value, index);
		} finally {
			updating = false;
		}
	}

	public Object getElement(int index) {
//		return getValues()[index];
		return null;
	}

	public Class getElementType() {		
		return elementType;
	}

}

