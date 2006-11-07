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

import org.eclipse.jface.databinding.DataBindingContext;
import org.eclipse.jface.databinding.beans.BeansObservables;
import org.eclipse.jface.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.examples.databinding.ModelObject;
import org.eclipse.swt.widgets.Display;

public class DefaultBindSupportFactoryBooleanPrimitiveTest extends TestCase {
    private DataBindingContext ctx;

    private TestDataObject dataObject;

    public void setUp() {
        Realm.setDefault(SWTObservables.getRealm(Display.getDefault()));

        ctx = new DataBindingContext();
        dataObject = new TestDataObject();
        dataObject.setStringVal("0");
        dataObject.setBooleanPrimitiveVal(false);
        dataObject.setBooleanVal(new Boolean(false));
    }

    public void testStringToBooleanPrimitiveConverter() {
        ctx.bindValue(BeansObservables.observeValue(dataObject, "stringVal"), BeansObservables.observeValue(dataObject,
                "booleanPrimitiveVal"), null);

        dataObject.setBooleanPrimitiveVal(true);
        assertEquals("boolean value does not match", true, dataObject.getBooleanPrimitiveVal());
        assertEquals("String value does not match", "true", dataObject.getStringVal());
        assertNull("No errors should be found.", ctx.getValidationError().getValue());

        dataObject.setStringVal("false");
        assertEquals("boolean value does not match", false, dataObject.getBooleanPrimitiveVal());
        assertEquals("String value does not match", "false", dataObject.getStringVal());
        assertNull("No errors should be found.", ctx.getValidationError().getValue());

        dataObject.setStringVal("");
        assertEquals("boolean value does not match", false, dataObject.getBooleanPrimitiveVal());
        assertEquals("String value does not match", "", dataObject.getStringVal());
        assertNotNull("Errors should be found.", ctx.getValidationError().getValue());

        dataObject.setStringVal(null);
        assertEquals("boolean value does not match", false, dataObject.getBooleanPrimitiveVal());
        assertNull("String value does not match", dataObject.getStringVal());
        assertNotNull("Errors should be found.", ctx.getValidationError().getValue());
    }

    public void testBooleanToBooleanPrimitiveConverter() {
        ctx.bindValue(BeansObservables.observeValue(dataObject, "booleanVal"),
                BeansObservables.observeValue(dataObject, "booleanPrimitiveVal"),
                null);

        dataObject.setBooleanPrimitiveVal(true);
        assertEquals("boolean value does not match", true, dataObject.getBooleanPrimitiveVal());
        assertEquals("Boolean value does not match", new Boolean(true), dataObject.getBooleanVal());
        assertNull("No errors should be found.", ctx.getValidationError().getValue());

        dataObject.setBooleanVal(new Boolean(false));
        assertEquals("boolean value does not match", false, dataObject.getBooleanPrimitiveVal());
        assertEquals("Boolean value does not match", new Boolean(false), dataObject.getBooleanVal());
        assertNull("No errors should be found.", ctx.getValidationError().getValue());

        dataObject.setBooleanVal(null);
        assertEquals("boolean value does not match", false, dataObject.getBooleanPrimitiveVal());
        assertNull("Boolean value does not match", dataObject.getBooleanVal());
        assertNotNull("Errors should be found.", ctx.getValidationError().getValue());
    }

    public void testObjectToBooleanPrimitiveConverter() {
        ctx.bindValue(BeansObservables.observeValue(dataObject, "objectVal"), BeansObservables.observeValue(dataObject,
                "booleanPrimitiveVal"), null);

        dataObject.setBooleanPrimitiveVal(true);
        assertEquals("boolean value does not match", true, dataObject.getBooleanPrimitiveVal());
        assertEquals("Object value does not match", new Boolean(true), dataObject.getObjectVal());
        assertNull("No errors should be found.", ctx.getValidationError().getValue());

        dataObject.setObjectVal(new Boolean(false));
        assertEquals("boolean value does not match", false, dataObject.getBooleanPrimitiveVal());
        assertEquals("Object value does not match", new Boolean(false), dataObject.getObjectVal());
        assertNull("No errors should be found.", ctx.getValidationError().getValue());

        dataObject.setObjectVal(null);
        assertEquals("boolean value does not match", false, dataObject.getBooleanPrimitiveVal());
        assertNull("Object value does not match", dataObject.getObjectVal());
        assertNotNull("Errors should be found.", ctx.getValidationError().getValue());

        Object object = new Object();
        dataObject.setObjectVal(object);
        assertEquals("boolean value does not match", false, dataObject.getBooleanPrimitiveVal());
        assertSame("Object value does not match", object, dataObject.getObjectVal());
        assertNotNull("Errors should be found.", ctx.getValidationError().getValue());
    }

    public class TestDataObject extends ModelObject {
        private boolean booleanPrimitiveValue;

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

        public boolean getBooleanPrimitiveVal() {
            return booleanPrimitiveValue;
        }

        public void setBooleanPrimitiveVal(boolean booleanPrimitiveValue) {
            boolean oldVal = this.booleanPrimitiveValue;
            this.booleanPrimitiveValue = booleanPrimitiveValue;
            firePropertyChange("booleanPrimitiveVal", new Boolean(oldVal), new Boolean(this.booleanPrimitiveValue));
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