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
import org.eclipse.core.databinding.DefaultBindSupportFactory;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.examples.databinding.ModelObject;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.2
 * 
 */
public class DefaultBindSupportFactoryTest extends TestCase {
    /**
     * Asserts that the instances of Boolean that are returned from
     * {@link DefaultBindSupportFactory#isAssignableFromTo()} are not new
     * instances of Boolean.
     */
    public void test_isAssignableFromToBooleanInstances() {
        DefaultBindSupportFactory factory = new DefaultBindSupportFactory();
        Boolean b1 = factory.isAssignableFromTo(String.class, String.class);
        Boolean b2 = factory.isAssignableFromTo(String.class, String.class);

        assertNotNull(b1);
        assertNotNull(b2);
        assertTrue(b1.booleanValue());
        assertSame(b1, b2);

        b1 = factory.isAssignableFromTo(String.class, Integer.class);
        b2 = factory.isAssignableFromTo(String.class, Integer.class);

        assertNotNull(b1);
        assertNotNull(b2);
        assertFalse(b1.booleanValue());
        assertSame(b1, b2);
    }

    public void testStringToIntegerConverter() {
        Realm.setDefault(SWTObservables.getRealm(Display.getDefault()));

        DataBindingContext ctx = new DataBindingContext();
        TestDataObject dataObject = new TestDataObject();
        dataObject.setIntegerStringVal("123");
        dataObject.setIntStringVal("456");
        ctx.bindValue(BeansObservables.observeValue(dataObject, "intStringVal"),
                BeansObservables.observeValue(dataObject, "intVal"),
                null);

        ctx.bindValue(BeansObservables.observeValue(dataObject, "integerStringVal"),
                BeansObservables.observeValue(dataObject, "integerVal"),
                null);

        dataObject.setIntegerStringVal("789");
        assertEquals("Integer value does not match", new Integer(789), dataObject.getIntegerVal());

        dataObject.setIntStringVal("789");
        assertEquals("Int value does not match", 789, dataObject.getIntVal());
        assertNull("No errors should be found.", ctx.getValidationError().getValue());

        dataObject.setIntegerStringVal("");
        assertNull("Integer value not null", dataObject.getIntegerVal());

        dataObject.setIntStringVal("");
        assertNotNull("Validation error expected.", ctx.getValidationError().getValue());
        assertEquals("Int value should not have changed.", 789, dataObject.getIntVal());
    }

    public class TestDataObject extends ModelObject {
        private int intVal;

        private Integer integerVal;

        private String intStringVal;

        private String integerStringVal;

        public Integer getIntegerVal() {
            return integerVal;
        }

        public void setIntegerVal(Integer integerVal) {
            this.integerVal = integerVal;
        }

        public int getIntVal() {
            return intVal;
        }

        public void setIntVal(int intVal) {
            this.intVal = intVal;
        }

        public String getIntegerStringVal() {
            return integerStringVal;
        }

        public void setIntegerStringVal(String integerStringVal) {
            Object oldVal = this.integerStringVal;
            this.integerStringVal = integerStringVal;
            firePropertyChange("integerStringVal", oldVal, this.integerStringVal);
        }

        public String getIntStringVal() {
            return intStringVal;
        }

        public void setIntStringVal(String intStringVal) {
            Object oldVal = this.intStringVal;
            this.intStringVal = intStringVal;
            firePropertyChange("intStringVal", oldVal, this.intStringVal);
        }
    }
}
