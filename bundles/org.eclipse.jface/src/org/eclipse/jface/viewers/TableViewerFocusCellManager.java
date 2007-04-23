/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.widgets.Table;

/**
 * This class is responsible to provide the concept of cells for {@link Table}.
 * This concept is needed to provide features like editor activation with the
 * keyboard
 * 
 * @since 3.3
 * 
 */
public class TableViewerFocusCellManager extends SWTFocusCellManager {
	private static final CellNavigationStrategy TABLE_NAVIGATE = new CellNavigationStrategy();

	/**
	 * Create a new manager 
	 * 
	 * @param viewer
	 *            the viewer the manager is bound to
	 * @param focusDrawingDelegate
	 *            the delegate responsible to highlight selected cell
	 */
	public TableViewerFocusCellManager(TableViewer viewer,
			FocusCellHighlighter focusDrawingDelegate) {
		super(viewer, focusDrawingDelegate, TABLE_NAVIGATE);
	}

	ViewerCell getInitialFocusCell() {
		Table table = (Table) getViewer().getControl();

		if (table.getItemCount() > 0) {
			return getViewer().getViewerRowFromItem(table.getItem(0))
					.getCell(0);
		}

		return null;
	}

}
