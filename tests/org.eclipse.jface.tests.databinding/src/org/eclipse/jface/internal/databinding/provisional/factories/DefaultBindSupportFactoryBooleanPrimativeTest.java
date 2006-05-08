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

public class DefaultBindSupportFactoryBooleanPrimativeTest extends TestCase {
	private DataBindingContext ctx;
	private TestDataObject dataObject;

	public void setUp() {
		ctx = getDatabindingContext();
		dataObject = new TestDataObject();
		dataObject.setStringVal("0");
		dataObject.setBooleanPrimativeVal(false);
		dataObject.setBooleanVal(new Boolean(false));		
	}
	
	public void testStringToBooleanPrimativeConverter() {
		ctx.bind(new Property(dataObject, "stringVal"), new Property(dataObject, "booleanPrimativeVal"), null);
		
		dataObject.setBooleanPrimativeVal(true);
		assertEquals("boolean value does not match", true, dataObject.getBooleanPrimativeVal());
		assertEquals("String value does not match", "true", dataObject.getStringVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());
		
		dataObject.setStringVal("false");
		assertEquals("boolean value does not match", false, dataObject.getBooleanPrimativeVal());
		assertEquals("String value does not match", "false", dataObject.getStringVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());		

		dataObject.setStringVal("");
		assertEquals("boolean value does not match", false, dataObject.getBooleanPrimativeVal());
		assertEquals("String value does not match", "", dataObject.getStringVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());		

		dataObject.setStringVal(null);
		assertEquals("boolean value does not match", false, dataObject.getBooleanPrimativeVal());
		assertNull("String value does not match", dataObject.getStringVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());			
	}

	public void testBooleanToBooleanPrimativeConverter() {
		ctx.bind(new Property(dataObject, "booleanVal"), new Property(dataObject, "booleanPrimativeVal"), null);
		
		dataObject.setBooleanPrimativeVal(true);
		assertEquals("boolean value does not match", true, dataObject.getBooleanPrimativeVal());
		assertEquals("Boolean value does not match", new Boolean(true), dataObject.getBooleanVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());
		
		dataObject.setBooleanVal(new Boolean(false));
		assertEquals("boolean value does not match", false, dataObject.getBooleanPrimativeVal());
		assertEquals("Boolean value does not match", new Boolean(false), dataObject.getBooleanVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());		

		dataObject.setBooleanVal(null);
		assertEquals("boolean value does not match", false, dataObject.getBooleanPrimativeVal());
		assertNull("Boolean value does not match", dataObject.getBooleanVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());		
	}
	
	public void testObjectToBooleanPrimativeConverter() {
		ctx.bind(new Property(dataObject, "objectVal"), new Property(dataObject, "booleanPrimativeVal"), null);
		
		dataObject.setBooleanPrimativeVal(true);
		assertEquals("boolean value does not match", true, dataObject.getBooleanPrimativeVal());
		assertEquals("Object value does not match", new Boolean(true), dataObject.getObjectVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());
		
		dataObject.setObjectVal(new Boolean(false));
		assertEquals("boolean value does not match", false, dataObject.getBooleanPrimativeVal());
		assertEquals("Object value does not match", new Boolean(false), dataObject.getObjectVal());
		assertNull("No errors should be found.", ctx.getValidationError().getValue());		

		dataObject.setObjectVal(null);
		assertEquals("boolean value does not match", false, dataObject.getBooleanPrimativeVal());
		assertNull("Object value does not match", dataObject.getObjectVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());		

		Object object = new Object();
		dataObject.setObjectVal(object);
		assertEquals("boolean value does not match", false, dataObject.getBooleanPrimativeVal());
		assertSame("Object value does not match", object, dataObject.getObjectVal());
		assertNotNull("Errors should be found.", ctx.getValidationError().getValue());			
	}
	
	public class TestDataObject extends ModelObject {
		private boolean booleanPrimativeValue;
		private String stringVal;
		private Boolean booleanVal;
		private Object objectVal;
		
		public Boolean getBooleanVal() {
			return booleanVal;
		}
		public void setBooleanVal(Boolean booleanVal) {
			Object oldVal = this.booleanVal;
			this.booleanVal = booleanVal;
			firePropertyChange("booleanVal", oldVal, this.booleanVal);
		}

		public boolean getBooleanPrimativeVal() {
			return booleanPrimativeValue;
		}
		public void setBooleanPrimativeVal(boolean booleanPrimativeValue) {
			boolean oldVal = this.booleanPrimativeValue;
			this.booleanPrimativeValue = booleanPrimativeValue;
			firePropertyChange("booleanPrimativeVal", new Boolean(oldVal), new Boolean(this.booleanPrimativeValue));
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