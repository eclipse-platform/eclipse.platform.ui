package org.eclipse.jface.layout;

/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     IBM Corporation - API refactoring and general maintenance
 *******************************************************************************/

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * The TableColumnAdapter is the ControlAdapter used to maintain Table sizes
 * {@link Table}.
 * 
 * @since 3.3
 * @see AbstractColumnAdapter
 * 
 * @deprecated This class will be replaced during the 3.3 M6 development cycle -
 *             for details see bug 171824
 * @since 3.3 <strong>EXPERIMENTAL</strong> This class or interface has been
 *        added as part of a work in progress. This API may change at any given
 *        time. Please do not use this API without consulting with the
 *        Platform/UI team.
 */
public class TableColumnAdapter extends AbstractColumnAdapter {

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param table
	 *            the table the layout is applied on
	 */
	public TableColumnAdapter(Table table) {
		super(table);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.layout.AbstractColumnAdapter#getColumnCount()
	 */
	int getColumnCount() {
		return ((Table) getControl()).getColumnCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.layout.AbstractColumnAdapter#setColumnWidths(int[])
	 */
	void setColumnWidths(int[] widths) {
		TableColumn[] columns = ((Table) getControl()).getColumns();
		for (int i = 0; i < widths.length; i++) {
			columns[i].setWidth(widths[i]);
		}
	}
}
