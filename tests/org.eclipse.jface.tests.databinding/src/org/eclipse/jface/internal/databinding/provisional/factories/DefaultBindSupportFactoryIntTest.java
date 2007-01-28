/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional.factories;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.jface.examples.databinding.ModelObject;

public class DefaultBindSupportFactoryIntTest extends AbstractBindSupportFactoryTest {
	private TestDataObject dataObject;

	public void setUp() throws Exception {
		super.setUp();
		dataObject = new TestDataObject();
		dataObject.setStringVal("0");
		dataObject.setIntVal(0);
		dataObject.setIntegerVal(new Integer(0));		
	}
	
	public void testStringToIntConverter() {
        ctx.bindValue(BeansObservables.observeValue(dataObject, "stringVal"), BeansObservables.observeValue(dataObject, "intVal"), null);
		
		dataObject.setIntVal(789);
		assertEquals("Int value does not match", 789, dataObject.getIntVal());
		assertEquals("String value does not match", "789", dataObject.getStringVal());
        assertNoErrorsFound();
		
		dataObject.setStringVal("910");
		assertEquals("Int value does not match", 910, dataObject.getIntVal());
		assertEquals("String value does not match", "910", dataObject.getStringVal());
        assertNoErrorsFound();

		dataObject.setStringVal("");
		assertEquals("Int value does not match", 910, dataObject.getIntVal());
		assertEquals("String value does not match", "", dataObject.getStringVal());
        assertErrorsFound();

		dataObject.setStringVal(null);
		assertEquals("Int value does not match", 910, dataObject.getIntVal());
		assertNull("String value does not match", dataObject.getStringVal());
        assertErrorsFound();
	}

	public void testIntegerToIntConverter() {
	    ctx.bindValue(BeansObservables.observeValue(dataObject, "integerVal"), BeansObservables.observeValue(dataObject, "intVal"), null);
		
		dataObject.setIntVal(789);
		assertEquals("Int value does not match", 789, dataObject.getIntVal());
		assertEquals("Integer value does not match", new Integer(789), dataObject.getIntegerVal());
        assertNoErrorsFound();
		
		dataObject.setIntegerVal(new Integer(910));
		assertEquals("Int value does not match", 910, dataObject.getIntVal());
		assertEquals("Integer value does not match", new Integer(910), dataObject.getIntegerVal());
        assertNoErrorsFound();

		dataObject.setIntegerVal(null);
		assertEquals("Int value does not match", 910, dataObject.getIntVal());
		assertNull("Integer value does not match", dataObject.getIntegerVal());
        assertErrorsFound();
	}
	
	public void testObjectToIntegerConverter() {
	    ctx.bindValue(BeansObservables.observeValue(dataObject, "objectVal"), BeansObservables.observeValue(dataObject, "intVal"), null);
		
		dataObject.setIntVal(789);
		assertEquals("Int value does not match", 789, dataObject.getIntVal());
		assertEquals("Object value does not match", new Integer(789), dataObject.getObjectVal());
        assertNoErrorsFound();
		
		dataObject.setObjectVal(new Integer(910));
		assertEquals("Int value does not match", 910, dataObject.getIntVal());
		assertEquals("Object value does not match", new Integer(910), dataObject.getObjectVal());
        assertNoErrorsFound();

		dataObject.setObjectVal(null);
		assertEquals("Int value does not match", 910, dataObject.getIntVal());
		assertNull("Object value does not match", dataObject.getObjectVal());
        assertErrorsFound();

		Object object = new Object();
		dataObject.setObjectVal(object);
		assertEquals("Int value does not match", 910, dataObject.getIntVal());
		assertSame("Object value does not match", object, dataObject.getObjectVal());
        assertErrorsFound();
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
}
	