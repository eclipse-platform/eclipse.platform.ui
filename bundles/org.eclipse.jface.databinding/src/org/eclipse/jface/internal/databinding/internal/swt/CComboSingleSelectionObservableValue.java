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

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

/**
 * @since 1.0
 *
 */
public class CComboSingleSelectionObservableValue extends
		SingleSelectionObservableValue {

	private SelectionListener selectionListener;

	/**
	 * @param combo
	 */
	public CComboSingleSelectionObservableValue(CCombo combo) {
		super(combo);
	}
	
	/**
	 * @param realm
	 * @param combo
	 */
	public CComboSingleSelectionObservableValue(Realm realm, CCombo combo) {
		super(realm, combo);
	}

	private CCombo getCCombo() {
		return (CCombo) getWidget();
	}

	protected void doAddSelectionListener(final Runnable runnable) {
		selectionListener = new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				runnable.run();
			}

			public void widgetSelected(SelectionEvent e) {
				runnable.run();
			}
		};
		getCCombo().addSelectionListener(selectionListener);
	}

	protected int doGetSelectionIndex() {
		return getCCombo().getSelectionIndex();
	}

	protected void doSetSelectionIndex(int index) {
		getCCombo().setText(getCCombo().getItem(index));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.databinding.observable.value.AbstractObservableValue#dispose()
	 */
	public synchronized void dispose() {
		super.dispose();
		if (selectionListener != null && !getCCombo().isDisposed()) {
			getCCombo().removeSelectionListener(selectionListener);
		}

	}
}
