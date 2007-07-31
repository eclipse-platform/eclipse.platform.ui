/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 171616
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.internal.beans;

import java.beans.PropertyDescriptor;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.internal.databinding.internal.beans.JavaBeanObservableValue;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.jface.tests.databinding.EventTrackers.ValueChangeEventTracker;

/**
 * @since 3.2
 */
public class JavaBeanObservableValueTest extends AbstractDefaultRealmTestCase {
	private Bean bean;
	private JavaBeanObservableValue observableValue;
	private PropertyDescriptor propertyDescriptor;
	private String propertyName;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		bean = new Bean();
		propertyName = "value";
		propertyDescriptor = new PropertyDescriptor(propertyName, Bean.class);
		observableValue = new JavaBeanObservableValue(Realm.getDefault(), bean, propertyDescriptor);
	}
	
    public void testSetsValueInBean() throws Exception {
        String value = "value";
        assertNull(observableValue.getValue());
        observableValue.setValue(value);
        assertEquals("value", value, observableValue.getValue());
    }
    
    public void testGetObserved() throws Exception {
    	assertEquals(bean, observableValue.getObserved());
	}
    
    public void testGetPropertyDescriptor() throws Exception {
    	assertEquals(propertyDescriptor, observableValue.getPropertyDescriptor());
	}
    
    public void testRegistersListenerAfterFirstListenerIsAdded() throws Exception {
		assertFalse(bean.changeSupport.hasListeners(propertyName));
		observableValue.addValueChangeListener(new ValueChangeEventTracker());
		assertTrue(bean.changeSupport.hasListeners(propertyName));
	}
    
    public void testRemovesListenerAfterLastListenerIsRemoved() throws Exception {
    	ValueChangeEventTracker listener = new ValueChangeEventTracker();
		observableValue.addValueChangeListener(listener);
		
		assertTrue(bean.changeSupport.hasListeners(propertyName));
		observableValue.removeValueChangeListener(listener);
		assertFalse(bean.changeSupport.hasListeners(propertyName));
	}
    
    public void testFiresValueChangeEvents() throws Exception {
    	ValueChangeEventTracker listener = new ValueChangeEventTracker();
    	observableValue.addValueChangeListener(listener);
    	
    	assertEquals(0, listener.count);
    	bean.setValue(bean.getValue() + bean.getValue());
    	assertEquals(1, listener.count);
	}

	public void testSetValueThrowsExceptionThrownByBean() throws Exception {
		ThrowsSetException temp = new ThrowsSetException();
		JavaBeanObservableValue observable = new JavaBeanObservableValue(Realm
				.getDefault(), temp,
				new PropertyDescriptor("value", ThrowsSetException.class));

		try {
			observable.setValue("");
			fail("exception should have been thrown");
		} catch (RuntimeException e) {	
			assertEquals(temp.thrownException, e.getCause());
		}
	}
	
	public void testGetValueThrowsExceptionThrownByBean() throws Exception {
		ThrowsGetException temp = new ThrowsGetException();
		JavaBeanObservableValue observable = new JavaBeanObservableValue(Realm
				.getDefault(), temp,
				new PropertyDescriptor("value", ThrowsGetException.class));

		try {
			observable.getValue();
			fail("exception should have been thrown");
		} catch (RuntimeException e) {	
			assertEquals(temp.thrownException, e.getCause());
		}
	}
	
	/**
	 * Throws an exception when the value is set.
	 * 
	 * @since 3.2
	 */
	/* package */ class ThrowsSetException {
		private String value;
		/* package */ NullPointerException thrownException;
		
		public void setValue(String value) {
			throw thrownException = new NullPointerException();
		}
		
		public String getValue() {
			return value;
		}
	}
	
	/* package */ class ThrowsGetException {
		public String value;
		/* package */ NullPointerException thrownException;
		
		public String getValue() {
			throw thrownException = new NullPointerException();
		}
		
		public void setValue(String value) {
			this.value = value;
		}
		
	}
}
