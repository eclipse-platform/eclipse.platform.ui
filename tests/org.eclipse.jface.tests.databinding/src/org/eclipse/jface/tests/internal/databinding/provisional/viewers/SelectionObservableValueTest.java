/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.provisional.viewers;

import junit.framework.TestCase;

import org.eclipse.jface.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.observable.value.IValueChangeListener;
import org.eclipse.jface.databinding.observable.value.ValueDiff;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.3
 */
public class SelectionObservableValueTest extends TestCase {
    public void testSetValue() throws Exception {
        Shell shell = new Shell();
        ListViewer viewer = new ListViewer(shell);
        String[] items = new String[] {"1", "2", "3"};
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setInput(items);
        
        IObservableValue observable = ViewersObservables.observeSingleSelection(viewer);
        ValueChangeListener listener = new ValueChangeListener();
        observable.addValueChangeListener(listener);
        
        assertNull(observable.getValue());
        assertEquals(StructuredSelection.EMPTY, viewer.getSelection());
        assertEquals(0, listener.count);
        
        observable.setValue(items[0]);
        assertEquals("observable value", items[0], observable.getValue());
        assertEquals("viewer selection", items[0], ((StructuredSelection) viewer.getSelection()).getFirstElement());
        assertEquals("value change event", 1, listener.count);
        
        observable.setValue(items[0]);
        assertEquals("value did not change, event should not have fired", 1, listener.count);
        
        observable.setValue(null);
        assertNull(observable.getValue());
        assertEquals("viewer selection should be empty", StructuredSelection.EMPTY, viewer.getSelection());
        assertEquals("value change event did not fire", 2, listener.count);
    }
    
    private static class ValueChangeListener implements IValueChangeListener {
        int count;
        
        public void handleValueChange(IObservableValue source, ValueDiff diff) {
            count++;
        }        
    }
}
