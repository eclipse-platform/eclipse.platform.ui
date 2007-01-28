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

public class DefaultBindSupportFactoryLongPrimitiveTest extends AbstractBindSupportFactoryTest {

    private TestDataObject dataObject;

    public void setUp() throws Exception {
    	super.setUp();
        dataObject = new TestDataObject();
        dataObject.setStringVal("0");
        dataObject.setLongPrimitiveVal(0);
        dataObject.setLongVal(new Long(0));
    }

    public void testStringToLongPrimitiveConverter() {
        ctx.bindValue(BeansObservables.observeValue(dataObject, "stringVal"), BeansObservables.observeValue(dataObject,
                "longPrimitiveVal"), null);

        dataObject.setLongPrimitiveVal(110);
        assertEquals("long value does not match", 110, dataObject.getLongPrimitiveVal(), .001);
        assertEquals("String value does not match", "110", dataObject.getStringVal());
        assertNoErrorsFound();

        dataObject.setStringVal("70");
        assertEquals("long value does not match", 70, dataObject.getLongPrimitiveVal(), .001);
        assertEquals("String value does not match", "70", dataObject.getStringVal());
        assertNoErrorsFound();

        dataObject.setStringVal("");
        assertEquals("long value does not match", 70, dataObject.getLongPrimitiveVal(), .001);
        assertEquals("String value does not match", "", dataObject.getStringVal());
        assertErrorsFound();

        dataObject.setStringVal(null);
        assertEquals("long value does not match", 70, dataObject.getLongPrimitiveVal(), .001);
        assertNull("String value does not match", dataObject.getStringVal());
        assertErrorsFound();
    }

    public void testLongToLongPrimitiveConverter() {
        ctx.bindValue(BeansObservables.observeValue(dataObject, "longVal"), BeansObservables.observeValue(dataObject,
                "longPrimitiveVal"), null);

        dataObject.setLongPrimitiveVal(110);
        assertEquals("long value does not match", 110, dataObject.getLongPrimitiveVal(), .001);
        assertEquals("Long value does not match", new Long(110), dataObject.getLongVal());
        assertNoErrorsFound();

        dataObject.setLongVal(new Long(70));
        assertEquals("long value does not match", 70, dataObject.getLongPrimitiveVal(), .001);
        assertEquals("Long value does not match", new Long(70), dataObject.getLongVal());
        assertNoErrorsFound();

        dataObject.setLongVal(null);
        assertEquals("long value does not match", 70, dataObject.getLongPrimitiveVal(), .001);
        assertNull("Long value does not match", dataObject.getLongVal());
        assertErrorsFound();
    }

    public void testObjectToLongPrimitiveConverter() {
        ctx.bindValue(BeansObservables.observeValue(dataObject, "objectVal"), BeansObservables.observeValue(dataObject,
                "longPrimitiveVal"), null);

        dataObject.setLongPrimitiveVal(110);
        assertEquals("long value does not match", 110, dataObject.getLongPrimitiveVal(), .001);
        assertEquals("Object value does not match", new Long(110), dataObject.getObjectVal());
        assertNoErrorsFound();

        dataObject.setObjectVal(new Long(70));
        assertEquals("long value does not match", 70, dataObject.getLongPrimitiveVal(), .001);
        assertEquals("Object value does not match", new Long(70), dataObject.getObjectVal());
        assertNoErrorsFound();

        dataObject.setObjectVal(null);
        assertEquals("long value does not match", 70, dataObject.getLongPrimitiveVal(), .001);
        assertNull("Object value does not match", dataObject.getObjectVal());
        assertErrorsFound();

        Object object = new Object();
        dataObject.setObjectVal(object);
        assertEquals("long value does not match", 70, dataObject.getLongPrimitiveVal(), .001);
        assertSame("Object value does not match", object, dataObject.getObjectVal());
        assertErrorsFound();
    }

    public class TestDataObject extends ModelObject {
        private long longPrimitiveValue;

        private String stringVal;

        private Long longVal;

        private Object objectVal;

        public Long getLongVal() {
            return longVal;
        }

        public void setLongVal(Long longVal) {
            Object oldVal = this.longVal;
            this.longVal = longVal;
            firePropertyChange("longVal", oldVal, this.longVal);
        }

        public long getLongPrimitiveVal() {
            return longPrimitiveValue;
        }

        public void setLongPrimitiveVal(long longPrimitiveValue) {
            long oldVal = this.longPrimitiveValue;
            this.longPrimitiveValue = longPrimitiveValue;
            firePropertyChange("longPrimitiveVal", new Long(oldVal), new Long(this.longPrimitiveValue));
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