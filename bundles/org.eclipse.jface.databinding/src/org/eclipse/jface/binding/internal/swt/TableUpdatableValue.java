/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.binding.internal.swt;

import org.eclipse.jface.binding.IChangeEvent;
import org.eclipse.jface.binding.UpdatableValue;
import org.eclipse.jface.binding.swt.SWTBindingConstants;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;

/**
 * @since 3.2
 *
 */
public class TableUpdatableValue extends UpdatableValue {

	private final Table table;

	private boolean updating = false;

	/**
	 * @param table
	 * @param attribute
	 */
	public TableUpdatableValue(Table table, String attribute) {
		this.table = table;
		if (attribute.equals(SWTBindingConstants.SELECTION)) {
			table.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					if (!updating) {
						fireChangeEvent( IChangeEvent.CHANGE, null, null);
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

	public void setValue(Object value) {
		try {
			updating = true;
			table.setSelection(((Integer) value).intValue());
		} finally {
			updating = false;
		}
	}

	public Object getValue() {
		return new Integer(table.getSelectionIndex());
	}

	public Class getValueType() {
		return Integer.class;
	}

}
