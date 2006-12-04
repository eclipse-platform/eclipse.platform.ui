/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - bug 158687
 *     Brad Reynolds - bug 164653
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.observable.value;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.2
 */
public class WritableValueTest extends TestCase {
    /**
     * Asserts that ValueChange events are only fired when the value changes.
     * 
     * @throws Exception
     */
    public void testValueChangeOnlyFiresOnChange() throws Exception {
        WritableValue writableValue = new WritableValue(SWTObservables.getRealm(Display.getDefault()), null);
        ValueChangeCounter counter = new ValueChangeCounter();
        writableValue.addValueChangeListener(counter);
        
        assertEquals(0, counter.count);
        //set same
        writableValue.setValue(null);
        assertEquals(0, counter.count);
        
        //set different
        writableValue.setValue("value");
        assertEquals(1, counter.count);
        
        //set same
        writableValue.setValue("value");
        assertEquals(1, counter.count);
        
        //set different
        writableValue.setValue(null);
        assertEquals(2, counter.count);
    }
    
    public void testDoSetValue() throws Exception {
        WritableValue writableValue = new WritableValue(SWTObservables.getRealm(Display.getDefault()), null);
        Object value = new Object();
        writableValue.setValue(value);
        assertEquals(value, writableValue.getValue());
    }
    
    private static class ValueChangeCounter implements IValueChangeListener {
        int count;

        public void handleValueChange(IObservableValue source, ValueDiff diff) {
            count++;
        }
    }
}
