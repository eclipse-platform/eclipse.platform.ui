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

public class DefaultBindSupportFactoryDoublePrimitiveTest extends TestCase {
	private DataBindingContext ctx;
	private TestDataObject dataObject;

	public void setUp() {
		ctx = getDatabindingContext();
		dataObject = new TestDataObject();
		dataObject.setStringVal("0");
		dataObject.setDoublePrimitiveVal(0);
		dataObject.setDoubleVal(new Double(0));		
	}
	
	public void testStringToDoublePrimitiveConverter() {
		ctx.bind(new Property(dataObject, "stringVal"), new Property(dataObject, "doublePrimitiveVal"), null);
		
		dataObject.setDoublePrimitiveVal(789.5);
		assertEquals("double value does not match", 789.5, dataObject.getDoublePrimitiveVal(), .001);
		assertEquals("String value does not match", "789.5", dataObject.getStringVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());
		
		dataObject.setStringVal("910.5");
		assertEquals("double value does not match", 910.5, dataObject.getDoublePrimitiveVal(), .001);
		assertEquals("String value does not match", "910.5", dataObject.getStringVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());		

		dataObject.setStringVal("");
		assertEquals("double value does not match", 910.5, dataObject.getDoublePrimitiveVal(), .001);
		assertEquals("String value does not match", "", dataObject.getStringVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());		

		dataObject.setStringVal(null);
		assertEquals("double value does not match", 910.5, dataObject.getDoublePrimitiveVal(), .001);
		assertNull("String value does not match", dataObject.getStringVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());			
	}

	public void testDoubleToDoublePrimitiveConverter() {
		ctx.bind(new Property(dataObject, "doubleVal"), new Property(dataObject, "doublePrimitiveVal"), null);
		
		dataObject.setDoublePrimitiveVal(789.5);
		assertEquals("double value does not match", 789.5, dataObject.getDoublePrimitiveVal(), .001);
		assertEquals("Double value does not match", new Double(789.5), dataObject.getDoubleVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());
		
		dataObject.setDoubleVal(new Double(910.5));
		assertEquals("double value does not match", 910.5, dataObject.getDoublePrimitiveVal(), .001);
		assertEquals("Double value does not match", new Double(910.5), dataObject.getDoubleVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());		

		dataObject.setDoubleVal(null);
		assertEquals("double value does not match", 910.5, dataObject.getDoublePrimitiveVal(), .001);
		assertNull("Double value does not match", dataObject.getDoubleVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());		
	}
	
	public void testObjectToDoublePrimitiveConverter() {
		ctx.bind(new Property(dataObject, "objectVal"), new Property(dataObject, "doublePrimitiveVal"), null);
		
		dataObject.setDoublePrimitiveVal(789.5);
		assertEquals("double value does not match", 789.5, dataObject.getDoublePrimitiveVal(), .001);
		assertEquals("Object value does not match", new Double(789.5), dataObject.getObjectVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());
		
		dataObject.setObjectVal(new Double(910.5));
		assertEquals("double value does not match", 910.5, dataObject.getDoublePrimitiveVal(), .001);
		assertEquals("Object value does not match", new Double(910.5), dataObject.getObjectVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());		

		dataObject.setObjectVal(null);
		assertEquals("double value does not match", 910.5, dataObject.getDoublePrimitiveVal(), .001);
		assertNull("Object value does not match", dataObject.getObjectVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());		

		Object object = new Object();
		dataObject.setObjectVal(object);
		assertEquals("double value does not match", 910.5, dataObject.getDoublePrimitiveVal(), .001);
		assertSame("Object value does not match", object, dataObject.getObjectVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());			
	}
	
	public class TestDataObject extends ModelObject {
		private double doublePrimitiveValue;
		private String stringVal;
		private Double doubleVal;
		private Object objectVal;
		
		public Double getDoubleVal() {
			return doubleVal;
		}
		public void setDoubleVal(Double doubleVal) {
			Object oldVal = this.doubleVal;
			this.doubleVal = doubleVal;
			firePropertyChange("doubleVal", oldVal, this.doubleVal);
		}

		public double getDoublePrimitiveVal() {
			return doublePrimitiveValue;
		}
		public void setDoublePrimitiveVal(double doublePrimitiveValue) {
			double oldVal = this.doublePrimitiveValue;
			this.doublePrimitiveValue = doublePrimitiveValue;
			firePropertyChange("doublePrimitiveVal", new Double(oldVal), new Double(this.doublePrimitiveValue));
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