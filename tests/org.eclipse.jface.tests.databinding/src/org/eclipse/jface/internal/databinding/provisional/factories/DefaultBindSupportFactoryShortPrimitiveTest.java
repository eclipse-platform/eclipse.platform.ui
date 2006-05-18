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

public class DefaultBindSupportFactoryShortPrimitiveTest extends TestCase {
	private DataBindingContext ctx;
	private TestDataObject dataObject;

	public void setUp() {
		ctx = getDatabindingContext();
		dataObject = new TestDataObject();
		dataObject.setStringVal("0");
		dataObject.setShortPrimitiveVal((short) 0);
		dataObject.setShortVal(new Short((short) 0));		
	}
	
	public void testStringToShortPrimitiveConverter() {
		ctx.bind(new Property(dataObject, "stringVal"), new Property(dataObject, "shortPrimitiveVal"), null);
		
		dataObject.setShortPrimitiveVal((short)110);
		assertEquals("short value does not match", 110, dataObject.getShortPrimitiveVal());
		assertEquals("String value does not match", "110", dataObject.getStringVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());
		
		dataObject.setStringVal("70");
		assertEquals("short value does not match", 70, dataObject.getShortPrimitiveVal());
		assertEquals("String value does not match", "70", dataObject.getStringVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());		

		dataObject.setStringVal("");
		assertEquals("short value does not match", 70, dataObject.getShortPrimitiveVal());
		assertEquals("String value does not match", "", dataObject.getStringVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());		

		dataObject.setStringVal(null);
		assertEquals("short value does not match", 70, dataObject.getShortPrimitiveVal());
		assertNull("String value does not match", dataObject.getStringVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());			
	}

	public void testShortToShortPrimitiveConverter() {
		ctx.bind(new Property(dataObject, "shortVal"), new Property(dataObject, "shortPrimitiveVal"), null);
		
		dataObject.setShortPrimitiveVal((short)110);
		assertEquals("short value does not match", 110, dataObject.getShortPrimitiveVal());
		assertEquals("Short value does not match", new Short((short)110), dataObject.getShortVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());
		
		dataObject.setShortVal(new Short((short)70));
		assertEquals("short value does not match", 70, dataObject.getShortPrimitiveVal());
		assertEquals("Short value does not match", new Short((short)70), dataObject.getShortVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());		

		dataObject.setShortVal(null);
		assertEquals("short value does not match", 70, dataObject.getShortPrimitiveVal());
		assertNull("Short value does not match", dataObject.getShortVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());		
	}
	
	public void testObjectToShortPrimitiveConverter() {
		ctx.bind(new Property(dataObject, "objectVal"), new Property(dataObject, "shortPrimitiveVal"), null);
		
		dataObject.setShortPrimitiveVal((short)110);
		assertEquals("short value does not match", 110, dataObject.getShortPrimitiveVal());
		assertEquals("Object value does not match", new Short((short)110), dataObject.getObjectVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());
		
		dataObject.setObjectVal(new Short((short)70));
		assertEquals("short value does not match", 70, dataObject.getShortPrimitiveVal());
		assertEquals("Object value does not match", new Short((short)70), dataObject.getObjectVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());		

		dataObject.setObjectVal(null);
		assertEquals("short value does not match", 70, dataObject.getShortPrimitiveVal());
		assertNull("Object value does not match", dataObject.getObjectVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());		

		Object object = new Object();
		dataObject.setObjectVal(object);
		assertEquals("short value does not match", 70, dataObject.getShortPrimitiveVal());
		assertSame("Object value does not match", object, dataObject.getObjectVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());			
	}
	
	public class TestDataObject extends ModelObject {
		private short shortPrimitiveValue;
		private String stringVal;
		private Short shortVal;
		private Object objectVal;
		
		public Short getShortVal() {
			return shortVal;
		}
		public void setShortVal(Short shortVal) {
			Object oldVal = this.shortVal;
			this.shortVal = shortVal;
			firePropertyChange("shortVal", oldVal, this.shortVal);
		}

		public short getShortPrimitiveVal() {
			return shortPrimitiveValue;
		}
		public void setShortPrimitiveVal(short shortPrimitiveValue) {
			short oldVal = this.shortPrimitiveValue;
			this.shortPrimitiveValue = shortPrimitiveValue;
			firePropertyChange("shortPrimitiveVal", new Short(oldVal), new Short(this.shortPrimitiveValue));
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