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

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;

/**
 * @since 1.0
 * 
 */
public class ComboSingleSelectionObservableValue extends SingleSelectionObservableValue {

	/**
	 * @param combo
	 */
	public ComboSingleSelectionObservableValue(Combo combo) {
		super(combo);
	}

	private Combo getCombo() {
		return (Combo) getWidget();
	}

	protected void doAddSelectionListener(final Runnable runnable) {
		getCombo().addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				runnable.run();
			}

			public void widgetSelected(SelectionEvent e) {
				runnable.run();
			}
		});
	}

	protected int doGetSelectionIndex() {
		return getCombo().getSelectionIndex();
	}

	protected void doSetSelectionIndex(int index) {
		getCombo().setText(getCombo().getItem(index));
	}

}
