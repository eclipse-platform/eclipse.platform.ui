/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional.factories;

import junit.framework.TestCase;

import org.eclipse.jface.examples.databinding.ModelObject;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.beans.BeanObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.description.Property;
import org.eclipse.jface.internal.databinding.provisional.swt.SWTObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewersBindingFactory;
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewersObservableFactory;
import org.eclipse.swt.widgets.Widget;

public class DefaultBindSupportFactoryIntTest extends TestCase {
	private DataBindingContext ctx;
	private TestDataObject dataObject;

	public void setUp() {
		ctx = getDatabindingContext();
		dataObject = new TestDataObject();
		dataObject.setStringVal("0");
		dataObject.setIntVal(0);
		dataObject.setIntegerVal(new Integer(0));		
	}
	
	public void testStringToIntConverter() {
		ctx.bind(new Property(dataObject, "stringVal"), new Property(dataObject, "intVal"), null);
		
		dataObject.setIntVal(789);
		assertEquals("Int value does not match", 789, dataObject.getIntVal());
		assertEquals("String value does not match", "789", dataObject.getStringVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());
		
		dataObject.setStringVal("910");
		assertEquals("Int value does not match", 910, dataObject.getIntVal());
		assertEquals("String value does not match", "910", dataObject.getStringVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());		

		dataObject.setStringVal("");
		assertEquals("Int value does not match", 910, dataObject.getIntVal());
		assertEquals("String value does not match", "", dataObject.getStringVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());		

		dataObject.setStringVal(null);
		assertEquals("Int value does not match", 910, dataObject.getIntVal());
		assertNull("String value does not match", dataObject.getStringVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());			
	}

	public void testIntegerToIntConverter() {
		ctx.bind(new Property(dataObject, "integerVal"), new Property(dataObject, "intVal"), null);
		
		dataObject.setIntVal(789);
		assertEquals("Int value does not match", 789, dataObject.getIntVal());
		assertEquals("Integer value does not match", new Integer(789), dataObject.getIntegerVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());
		
		dataObject.setIntegerVal(new Integer(910));
		assertEquals("Int value does not match", 910, dataObject.getIntVal());
		assertEquals("Integer value does not match", new Integer(910), dataObject.getIntegerVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());		

		dataObject.setIntegerVal(null);
		assertEquals("Int value does not match", 910, dataObject.getIntVal());
		assertNull("Integer value does not match", dataObject.getIntegerVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());		
	}
	
	public void testObjectToIntegerConverter() {
		ctx.bind(new Property(dataObject, "objectVal"), new Property(dataObject, "intVal"), null);
		
		dataObject.setIntVal(789);
		assertEquals("Int value does not match", 789, dataObject.getIntVal());
		assertEquals("Object value does not match", new Integer(789), dataObject.getObjectVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());
		
		dataObject.setObjectVal(new Integer(910));
		assertEquals("Int value does not match", 910, dataObject.getIntVal());
		assertEquals("Object value does not match", new Integer(910), dataObject.getObjectVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());		

		dataObject.setObjectVal(null);
		assertEquals("Int value does not match", 910, dataObject.getIntVal());
		assertNull("Object value does not match", dataObject.getObjectVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());		

		Object object = new Object();
		dataObject.setObjectVal(object);
		assertEquals("Int value does not match", 910, dataObject.getIntVal());
		assertSame("Object value does not match", object, dataObject.getObjectVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());			
	}
	
	public class TestDataObject extends ModelObject {
		private int intVal;
		private String stringVal;
		private Integer integerVal;
		private Object objectVal;
		
		public Integer getIntegerVal() {
			return integerVal;
		}
		public void setIntegerVal(Integer integerVal) {
			Object oldVal = this.integerVal;
			this.integerVal = integerVal;
			firePropertyChange("integerVal", oldVal, this.integerVal);
		}

		public int getIntVal() {
			return intVal;
		}
		public void setIntVal(int intVal) {
			int oldVal = this.intVal;
			this.intVal = intVal;
			firePropertyChange("intVal", oldVal, this.intVal);
		}
		
		public String getStringVal() {
			return stringVal;
		}
		public void setStringVal(String stringVal) {
			Object oldVal = this.stringVal;
			this.stringVal = stringVal;
			firePropertyChange("stringVal", oldVal, this.stringVal);
		}

		public Object getObjectVal() {
			return objectVal;
		}
		public void setObjectVal(Object objectVal) {
			Object oldVal = this.objectVal;
			this.objectVal = objectVal;
			firePropertyChange("objectVal", oldVal, this.objectVal);
		}
	}
	
	/**
	 * @param aControl
	 * @return
	 */
	public static DataBindingContext getDatabindingContext() {
		final DataBindingContext context = new DataBindingContext();
		context.addObservableFactory(new DefaultObservableFactory(context));
		context.addObservableFactory(new BeanObservableFactory(context, null, new Class[]{Widget.class}));
		context.addObservableFactory(new NestedObservableFactory(context));
		context.addObservableFactory(new SWTObservableFactory());
		context.addObservableFactory(new ViewersObservableFactory());
		context.addBindingFactory(new DefaultBindingFactory());
		context.addBindingFactory(new ViewersBindingFactory());
		context.addBindSupportFactory(new DefaultBindSupportFactory());
		return context;
	}	
}
	