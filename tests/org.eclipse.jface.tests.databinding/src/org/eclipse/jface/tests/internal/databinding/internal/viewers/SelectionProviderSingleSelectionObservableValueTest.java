/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 116920
 *******************************************************************************/
package org.eclipse.jface.tests.internal.databinding.internal.viewers;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.internal.databinding.internal.viewers.SelectionProviderSingleSelectionObservableValue;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests for SelectionProviderSingleSelectionObservableValue.
 * 
 * @since 1.1
 */
public class SelectionProviderSingleSelectionObservableValueTest extends TestCase {
    private ISelectionProvider selectionProvider;

    private TableViewer viewer;

    private static String[] model = new String[] { "0", "1" };

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        Shell shell = new Shell();
        viewer = new TableViewer(shell, SWT.NONE);
        viewer.setContentProvider(new ContentProvider());
        viewer.setInput(model);
        selectionProvider = viewer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        Shell shell = viewer.getTable().getShell();
        if (!shell.isDisposed())
            shell.dispose();
    }

    public void testConstructorIllegalArgumentException() {
        try {
            new SelectionProviderSingleSelectionObservableValue(SWTObservables.getRealm(Display.getDefault()), null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Asserts that when a selection is set on the viewer:
     * <ul>
     * <li>the selection is available in the observable</li>
     * <li>Value change events are fired with appropriate diff values</li>
     * </ul>
     */
    public void testGetSetValue() {
        SelectionProviderSingleSelectionObservableValue observable = new SelectionProviderSingleSelectionObservableValue(
                SWTObservables.getRealm(Display.getDefault()),
                selectionProvider);
        ChangeListener listener = new ChangeListener();
        observable.addValueChangeListener(listener);
        assertNull(observable.getValue());

        selectionProvider.setSelection(new StructuredSelection(model[0]));
        assertEquals(1, listener.count);
        assertNull(listener.diff.getOldValue());
        assertEquals(model[0], listener.diff.getNewValue());
        assertEquals(observable, listener.source);
        assertEquals(model[0], observable.getValue());

        selectionProvider.setSelection(new StructuredSelection(model[1]));
        assertEquals(2, listener.count);
        assertEquals(model[0], listener.diff.getOldValue());
        assertEquals(model[1], listener.diff.getNewValue());
        assertEquals(observable, listener.source);
        assertEquals(model[1], observable.getValue());

        selectionProvider.setSelection(StructuredSelection.EMPTY);
        assertEquals(3, listener.count);
        assertEquals(model[1], listener.diff.getOldValue());
        assertNull(listener.diff.getNewValue());
        assertEquals(observable, listener.source);
        assertEquals(null, observable.getValue());
    }

    private class ChangeListener implements IValueChangeListener {
        int count = 0;

        IObservable source;

        ValueDiff diff;

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.databinding.observable.value.IValueChangeListener#handleValueChange(org.eclipse.jface.databinding.observable.value.IObservableValue,
         *      org.eclipse.jface.databinding.observable.value.ValueDiff)
         */
        public void handleValueChange(ValueChangeEvent event) {
            count++;
            this.source = event.getObservableValue();
            this.diff = event.diff;
        }
    }

    private class ContentProvider implements IStructuredContentProvider {
        public void dispose() {
            // TODO Auto-generated method stub

        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // TODO Auto-generated method stub

        }

        public Object[] getElements(Object inputElement) {
            return (String[]) inputElement;
        }
    }
}
