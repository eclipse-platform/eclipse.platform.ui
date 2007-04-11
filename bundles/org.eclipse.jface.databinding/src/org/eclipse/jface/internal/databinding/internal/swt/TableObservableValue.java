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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;

/**
 * @since 1.0
 * 
 */
public class TableObservableValue extends AbstractSWTObservableValue {

	private final Table table;

	private boolean updating = false;

	private int currentSelection;
	private String attribute;

	/**
	 * @param table
	 * @param attribute
	 */
	public TableObservableValue(Table table, String attribute) {
		super(table);
		this.table = table;
		this.attribute = attribute;
		
		currentSelection = table.getSelectionIndex();
		if (attribute.equals(SWTProperties.SELECTION)) {
			currentSelection = table.getSelectionIndex();
			table.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					if (!updating) {
						int newSelection = TableObservableValue.this.table
								.getSelectionIndex();
						fireValueChange(Diffs.createValueDiff(new Integer(
								currentSelection), new Integer(newSelection)));
						currentSelection = newSelection;
					}
				}

				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});
		} else {
			throw new IllegalArgumentException();
		}
	}

	public void doSetValue(Object value) {
		try {
			updating = true;
			int intValue = ((Integer) value).intValue();
			table.setSelection(intValue);
			currentSelection = intValue;
		} finally {
			updating = false;
		}
	}

	public Object doGetValue() {
		return new Integer(table.getSelectionIndex());
	}

	public Object getValueType() {
		return Integer.class;
	}

	/**
	 * @return attribute being observed
	 */
	public String getAttribute() {
		return attribute;
	}
}
