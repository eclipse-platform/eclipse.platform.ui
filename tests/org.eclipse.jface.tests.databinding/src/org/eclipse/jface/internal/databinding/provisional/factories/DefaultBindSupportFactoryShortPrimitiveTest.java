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

public class DefaultBindSupportFactoryShortPrimitiveTest extends AbstractBindSupportFactoryTest {

    private TestDataObject dataObject;

    public void setUp() throws Exception {
    	super.setUp();
        dataObject = new TestDataObject();
        dataObject.setStringVal("0");
        dataObject.setShortPrimitiveVal((short) 0);
        dataObject.setShortVal(new Short((short) 0));
    }

    public void testStringToShortPrimitiveConverter() {
        ctx.bindValue(BeansObservables.observeValue(dataObject, "stringVal"), BeansObservables.observeValue(dataObject,
                "shortPrimitiveVal"), null);

        dataObject.setShortPrimitiveVal((short) 110);
        assertEquals("short value does not match", 110, dataObject.getShortPrimitiveVal());
        assertEquals("String value does not match", "110", dataObject.getStringVal());
        assertNoErrorsFound();

        dataObject.setStringVal("70");
        assertEquals("short value does not match", 70, dataObject.getShortPrimitiveVal());
        assertEquals("String value does not match", "70", dataObject.getStringVal());
        assertNoErrorsFound();

        dataObject.setStringVal("");
        assertEquals("short value does not match", 70, dataObject.getShortPrimitiveVal());
        assertEquals("String value does not match", "", dataObject.getStringVal());
        assertErrorsFound();

        dataObject.setStringVal(null);
        assertEquals("short value does not match", 70, dataObject.getShortPrimitiveVal());
        assertNull("String value does not match", dataObject.getStringVal());
        assertErrorsFound();
    }

    public void testShortToShortPrimitiveConverter() {
        ctx.bindValue(BeansObservables.observeValue(dataObject, "shortVal"), BeansObservables.observeValue(dataObject,
                "shortPrimitiveVal"), null);

        dataObject.setShortPrimitiveVal((short) 110);
        assertEquals("short value does not match", 110, dataObject.getShortPrimitiveVal());
        assertEquals("Short value does not match", new Short((short) 110), dataObject.getShortVal());
        assertNoErrorsFound();

        dataObject.setShortVal(new Short((short) 70));
        assertEquals("short value does not match", 70, dataObject.getShortPrimitiveVal());
        assertEquals("Short value does not match", new Short((short) 70), dataObject.getShortVal());
        assertNoErrorsFound();

        dataObject.setShortVal(null);
        assertEquals("short value does not match", 70, dataObject.getShortPrimitiveVal());
        assertNull("Short value does not match", dataObject.getShortVal());
        assertErrorsFound();
    }

    public void testObjectToShortPrimitiveConverter() {
        ctx.bindValue(BeansObservables.observeValue(dataObject, "objectVal"), BeansObservables.observeValue(dataObject,
                "shortPrimitiveVal"), null);

        dataObject.setShortPrimitiveVal((short) 110);
        assertEquals("short value does not match", 110, dataObject.getShortPrimitiveVal());
        assertEquals("Object value does not match", new Short((short) 110), dataObject.getObjectVal());
        assertNoErrorsFound();

        dataObject.setObjectVal(new Short((short) 70));
        assertEquals("short value does not match", 70, dataObject.getShortPrimitiveVal());
        assertEquals("Object value does not match", new Short((short) 70), dataObject.getObjectVal());
        assertNoErrorsFound();

        dataObject.setObjectVal(null);
        assertEquals("short value does not match", 70, dataObject.getShortPrimitiveVal());
        assertNull("Object value does not match", dataObject.getObjectVal());
        assertErrorsFound();

        Object object = new Object();
        dataObject.setObjectVal(object);
        assertEquals("short value does not match", 70, dataObject.getShortPrimitiveVal());
        assertSame("Object value does not match", object, dataObject.getObjectVal());
        assertErrorsFound();
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
}