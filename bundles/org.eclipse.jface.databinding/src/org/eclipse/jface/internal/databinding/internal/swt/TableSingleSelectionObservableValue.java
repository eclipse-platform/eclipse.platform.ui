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
import org.eclipse.swt.widgets.Table;

/**
 * @since 1.0
 * 
 */
public class TableSingleSelectionObservableValue extends SingleSelectionObservableValue {

	/**
	 * @param table
	 */
	public TableSingleSelectionObservableValue(Table table) {
		super(table);
	}

	private Table getTable() {
		return (Table) getWidget();
	}

	protected void doAddSelectionListener(final Runnable runnable) {
		getTable().addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				runnable.run();
			}

			public void widgetSelected(SelectionEvent e) {
				runnable.run();
			}
		});
	}

	protected int doGetSelectionIndex() {
		return getTable().getSelectionIndex();
	}

	protected void doSetSelectionIndex(int index) {
		getTable().setSelection(index);
	}

}
