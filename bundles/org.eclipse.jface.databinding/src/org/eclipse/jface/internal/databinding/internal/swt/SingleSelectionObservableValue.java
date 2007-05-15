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
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.internal.swt;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.jface.internal.databinding.provisional.swt.AbstractSWTObservableValue;
import org.eclipse.swt.widgets.Control;

/**
 * @since 1.0
 * 
 */
abstract public class SingleSelectionObservableValue extends
		AbstractSWTObservableValue {

	private boolean updating = false;

	private int currentSelection;

	/**
	 * @param control
	 *            the control
	 */
	public SingleSelectionObservableValue(Control control) {
		super(control);

		currentSelection = doGetSelectionIndex();
		doAddSelectionListener(new Runnable(){
			public void run() {
				if (!updating) {
					int newSelection = doGetSelectionIndex();
					fireValueChange(Diffs.createValueDiff(new Integer(
							currentSelection), new Integer(newSelection)));
					currentSelection = newSelection;
				}
			}
		});
	}

	/**
	 * @param runnable
	 */
	protected abstract void doAddSelectionListener(Runnable runnable);

	public void doSetValue(Object value) {
		try {
			updating = true;
			int intValue = ((Integer) value).intValue();
			doSetSelectionIndex(intValue);
			currentSelection = intValue;
		} finally {
			updating = false;
		}
	}

	/**
	 * @param intValue
	 *            the selection index
	 */
	protected abstract void doSetSelectionIndex(int intValue);

	public Object doGetValue() {
		return new Integer(doGetSelectionIndex());
	}

	/**
	 * @return the selection index
	 */
	protected abstract int doGetSelectionIndex();

	public Object getValueType() {
		return Integer.TYPE;
	}

}
