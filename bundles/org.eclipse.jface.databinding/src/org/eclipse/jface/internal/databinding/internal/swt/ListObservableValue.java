/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *     Ashley Cambrell - bug 198904
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.internal.swt;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.jface.internal.databinding.provisional.swt.AbstractSWTObservableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;

/**
 * @since 3.2
 * 
 */
public class ListObservableValue extends AbstractSWTObservableValue {

	private final List list;

	private boolean updating = false;

	private String currentValue;

	private Listener listener;

	/**
	 * @param list
	 */
	public ListObservableValue(List list) {
		super(list);
		this.list = list;
		this.currentValue = (String) doGetValue();

		if ((list.getStyle() & SWT.MULTI) > 0)
			throw new IllegalArgumentException(
					"SWT.SINGLE support only for a List selection"); //$NON-NLS-1$

		listener = new Listener() {

			public void handleEvent(Event event) {
				if (!updating) {
					Object oldValue = currentValue;
					currentValue = (String) doGetValue();
					fireValueChange(Diffs.createValueDiff(oldValue,
							currentValue));
				}
			}

		};
		list.addListener(SWT.Selection, listener);
	}

	public void doSetValue(Object value) {
		String oldValue = null;
		if (list.getSelection() != null && list.getSelection().length > 0)
			oldValue = list.getSelection()[0];
		try {
			updating = true;
			String items[] = list.getItems();
			int index = -1;
			if (items != null && value != null) {
				for (int i = 0; i < items.length; i++) {
					if (value.equals(items[i])) {
						index = i;
						break;
					}
				}
				list.select(index); // -1 will not "unselect"
			}
			currentValue = (String) value;
		} finally {
			updating = false;
		}
		fireValueChange(Diffs.createValueDiff(oldValue, value));
	}

	public Object doGetValue() {
		int index = list.getSelectionIndex();
		if (index >= 0)
			return list.getItem(index);
		return null;
	}

	public Object getValueType() {
		return String.class;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.databinding.observable.value.AbstractObservableValue#dispose()
	 */
	public synchronized void dispose() {
		super.dispose();
		if (listener != null && !list.isDisposed()) {
			list.removeListener(SWT.Selection, listener);
		}
	}
}
