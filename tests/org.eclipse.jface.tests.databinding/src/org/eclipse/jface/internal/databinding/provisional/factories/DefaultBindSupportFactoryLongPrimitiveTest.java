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

import junit.framework.TestCase;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.examples.databinding.ModelObject;
import org.eclipse.swt.widgets.Display;

public class DefaultBindSupportFactoryLongPrimitiveTest extends TestCase {
    private DataBindingContext ctx;

    private TestDataObject dataObject;

    public void setUp() {
        Realm.setDefault(SWTObservables.getRealm(Display.getDefault()));
        ctx = new DataBindingContext();
        dataObject = new TestDataObject();
        dataObject.setStringVal("0");
        dataObject.setLongPrimitiveVal((long) 0);
        dataObject.setLongVal(new Long((long) 0));
    }

    public void testStringToLongPrimitiveConverter() {
        ctx.bindValue(BeansObservables.observeValue(dataObject, "stringVal"), BeansObservables.observeValue(dataObject,
                "longPrimitiveVal"), null);

        dataObject.setLongPrimitiveVal((long) 110);
        assertEquals("long value does not match", 110, dataObject.getLongPrimitiveVal(), .001);
        assertEquals("String value does not match", "110", dataObject.getStringVal());
        assertNull("No errors should be found.", ctx.getValidationError().getValue());

        dataObject.setStringVal("70");
        assertEquals("long value does not match", 70, dataObject.getLongPrimitiveVal(), .001);
        assertEquals("String value does not match", "70", dataObject.getStringVal());
        assertNull("No errors should be found.", ctx.getValidationError().getValue());

        dataObject.setStringVal("");
        assertEquals("long value does not match", 70, dataObject.getLongPrimitiveVal(), .001);
        assertEquals("String value does not match", "", dataObject.getStringVal());
        assertNotNull("Errors should be found.", ctx.getValidationError().getValue());

        dataObject.setStringVal(null);
        assertEquals("long value does not match", 70, dataObject.getLongPrimitiveVal(), .001);
        assertNull("String value does not match", dataObject.getStringVal());
        assertNotNull("Errors should be found.", ctx.getValidationError().getValue());
    }

    public void testLongToLongPrimitiveConverter() {
        ctx.bindValue(BeansObservables.observeValue(dataObject, "longVal"), BeansObservables.observeValue(dataObject,
                "longPrimitiveVal"), null);

        dataObject.setLongPrimitiveVal((long) 110);
        assertEquals("long value does not match", 110, dataObject.getLongPrimitiveVal(), .001);
        assertEquals("Long value does not match", new Long((long) 110), dataObject.getLongVal());
        assertNull("No errors should be found.", ctx.getValidationError().getValue());

        dataObject.setLongVal(new Long((long) 70));
        assertEquals("long value does not match", 70, dataObject.getLongPrimitiveVal(), .001);
        assertEquals("Long value does not match", new Long((long) 70), dataObject.getLongVal());
        assertNull("No errors should be found.", ctx.getValidationError().getValue());

        dataObject.setLongVal(null);
        assertEquals("long value does not match", 70, dataObject.getLongPrimitiveVal(), .001);
        assertNull("Long value does not match", dataObject.getLongVal());
        assertNotNull("Errors should be found.", ctx.getValidationError().getValue());
    }

    public void testObjectToLongPrimitiveConverter() {
        ctx.bindValue(BeansObservables.observeValue(dataObject, "objectVal"), BeansObservables.observeValue(dataObject,
                "longPrimitiveVal"), null);

        dataObject.setLongPrimitiveVal((long) 110);
        assertEquals("long value does not match", 110, dataObject.getLongPrimitiveVal(), .001);
        assertEquals("Object value does not match", new Long((long) 110), dataObject.getObjectVal());
        assertNull("No errors should be found.", ctx.getValidationError().getValue());

        dataObject.setObjectVal(new Long((long) 70));
        assertEquals("long value does not match", 70, dataObject.getLongPrimitiveVal(), .001);
        assertEquals("Object value does not match", new Long((long) 70), dataObject.getObjectVal());
        assertNull("No errors should be found.", ctx.getValidationError().getValue());

        dataObject.setObjectVal(null);
        assertEquals("long value does not match", 70, dataObject.getLongPrimitiveVal(), .001);
        assertNull("Object value does not match", dataObject.getObjectVal());
        assertNotNull("Errors should be found.", ctx.getValidationError().getValue());

        Object object = new Object();
        dataObject.setObjectVal(object);
        assertEquals("long value does not match", 70, dataObject.getLongPrimitiveVal(), .001);
        assertSame("Object value does not match", object, dataObject.getObjectVal());
        assertNotNull("Errors should be found.", ctx.getValidationError().getValue());
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