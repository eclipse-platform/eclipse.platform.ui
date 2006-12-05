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

public class DefaultBindSupportFactoryDoublePrimitiveTest extends AbstractBindSupportFactoryTest {

    private TestDataObject dataObject;

    public void setUp() {
    	super.setUp();
        dataObject = new TestDataObject();
        dataObject.setStringVal("0");
        dataObject.setDoublePrimitiveVal(0);
        dataObject.setDoubleVal(new Double(0));
    }

    public void testStringToDoublePrimitiveConverter() {
        ctx.bindValue(BeansObservables.observeValue(dataObject, "stringVal"), BeansObservables.observeValue(dataObject,
                "doublePrimitiveVal"), null);

        dataObject.setDoublePrimitiveVal(789.5);
        assertEquals("double value does not match", 789.5, dataObject.getDoublePrimitiveVal(), .001);
        assertEquals("String value does not match", "789.5", dataObject.getStringVal());
        assertNoErrorsFound();

        dataObject.setStringVal("910.5");
        assertEquals("double value does not match", 910.5, dataObject.getDoublePrimitiveVal(), .001);
        assertEquals("String value does not match", "910.5", dataObject.getStringVal());
        assertNoErrorsFound();

        dataObject.setStringVal("");
        assertEquals("double value does not match", 910.5, dataObject.getDoublePrimitiveVal(), .001);
        assertEquals("String value does not match", "", dataObject.getStringVal());
        assertErrorsFound();

        dataObject.setStringVal(null);
        assertEquals("double value does not match", 910.5, dataObject.getDoublePrimitiveVal(), .001);
        assertNull("String value does not match", dataObject.getStringVal());
        assertErrorsFound();
       }

    public void testDoubleToDoublePrimitiveConverter() {
        ctx.bindValue(BeansObservables.observeValue(dataObject, "doubleVal"), BeansObservables.observeValue(dataObject,
                "doublePrimitiveVal"), null);
        // ctx.bind(new Property(dataObject, "doubleVal"), new
        // Property(dataObject, "doublePrimitiveVal"), null);

        dataObject.setDoublePrimitiveVal(789.5);
        assertEquals("double value does not match", 789.5, dataObject.getDoublePrimitiveVal(), .001);
        assertEquals("Double value does not match", new Double(789.5), dataObject.getDoubleVal());
        assertNoErrorsFound();

        dataObject.setDoubleVal(new Double(910.5));
        assertEquals("double value does not match", 910.5, dataObject.getDoublePrimitiveVal(), .001);
        assertEquals("Double value does not match", new Double(910.5), dataObject.getDoubleVal());
        assertNoErrorsFound();

        dataObject.setDoubleVal(null);
        assertEquals("double value does not match", 910.5, dataObject.getDoublePrimitiveVal(), .001);
        assertNull("Double value does not match", dataObject.getDoubleVal());
        assertErrorsFound();
    }

    public void testObjectToDoublePrimitiveConverter() {
        ctx.bindValue(BeansObservables.observeValue(dataObject, "objectVal"), BeansObservables.observeValue(dataObject,
                "doublePrimitiveVal"), null);

        dataObject.setDoublePrimitiveVal(789.5);
        assertEquals("double value does not match", 789.5, dataObject.getDoublePrimitiveVal(), .001);
        assertEquals("Object value does not match", new Double(789.5), dataObject.getObjectVal());
        assertNoErrorsFound();

        dataObject.setObjectVal(new Double(910.5));
        assertEquals("double value does not match", 910.5, dataObject.getDoublePrimitiveVal(), .001);
        assertEquals("Object value does not match", new Double(910.5), dataObject.getObjectVal());
        assertNoErrorsFound();

        dataObject.setObjectVal(null);
        assertEquals("double value does not match", 910.5, dataObject.getDoublePrimitiveVal(), .001);
        assertNull("Object value does not match", dataObject.getObjectVal());
        assertErrorsFound();

        Object object = new Object();
        dataObject.setObjectVal(object);
        assertEquals("double value does not match", 910.5, dataObject.getDoublePrimitiveVal(), .001);
        assertSame("Object value does not match", object, dataObject.getObjectVal());
        assertErrorsFound();
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
}