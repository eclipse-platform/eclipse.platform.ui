/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	private List<ISelectionChangedListener> listeners = new ArrayList<>(3);

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
		Iterator<ISelectionChangedListener> iter = listeners.iterator();
		while (iter.hasNext()) {
			iter.next().selectionChanged(event);
		}
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		return StructuredSelection.EMPTY;
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
	}
}

