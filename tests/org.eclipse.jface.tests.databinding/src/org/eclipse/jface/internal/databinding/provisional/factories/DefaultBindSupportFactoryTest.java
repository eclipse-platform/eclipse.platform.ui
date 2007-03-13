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

/**
 * @since 3.2
 * 
 */
public class DefaultBindSupportFactoryTest extends AbstractBindSupportFactoryTest {

    public void testStringToIntegerConverter() {
        TestDataObject dataObject = new TestDataObject();
        dataObject.setIntegerStringVal("123");
        dataObject.setIntStringVal("456");
        ctx.bindValue(BeansObservables.observeValue(dataObject, "intStringVal"),
                BeansObservables.observeValue(dataObject, "intVal"),
                null, null);

        ctx.bindValue(BeansObservables.observeValue(dataObject, "integerStringVal"),
                BeansObservables.observeValue(dataObject, "integerVal"),
                null, null);

        dataObject.setIntegerStringVal("789");
        assertEquals("Integer value does not match", new Integer(789), dataObject.getIntegerVal());

        dataObject.setIntStringVal("789");
        assertEquals("Int value does not match", 789, dataObject.getIntVal());
        assertNoErrorsFound();

        dataObject.setIntegerStringVal("");
        assertNull("Integer value not null", dataObject.getIntegerVal());

        dataObject.setIntStringVal("");
        assertErrorsFound();
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
