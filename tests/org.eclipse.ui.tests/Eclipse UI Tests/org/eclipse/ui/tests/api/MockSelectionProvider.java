/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

public class MockSelectionProvider implements ISelectionProvider {

    private List listeners = new ArrayList(3);

    /**
     * Fires out a selection to all listeners.
     */
    public void fireSelection() {
        fireSelection(new SelectionChangedEvent(this, StructuredSelection.EMPTY));
    }

    /**
     * Fires out a selection to all listeners.
     */
    public void fireSelection(SelectionChangedEvent event) {
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            ((ISelectionChangedListener) iter.next()).selectionChanged(event);
        }
    }

    /**
     * @see ISelectionProvider#addSelectionChangedListener(ISelectionChangedListener)
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        listeners.add(listener);
    }

    /**
     * @see ISelectionProvider#getSelection()
     */
    public ISelection getSelection() {
        return StructuredSelection.EMPTY;
    }

    /**
     * @see ISelectionProvider#removeSelectionChangedListener(ISelectionChangedListener)
     */
    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        listeners.remove(listener);
    }

    /**
     * @see ISelectionProvider#setSelection(ISelection)
     */
    public void setSelection(ISelection selection) {
    }
}

