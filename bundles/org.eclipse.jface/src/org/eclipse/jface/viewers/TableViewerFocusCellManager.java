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
 * @since 3.3
 *
 */
public class TableViewerFocusCellManager extends SWTFocusCellManager {
	private static final CellNavigationStrategy TABLE_NAVIGATE = new CellNavigationStrategy();
	
	/**
	 * @param viewer
	 * @param focusDrawingDelegate
	 */
	public TableViewerFocusCellManager(TableViewer viewer,
			FocusCellHighlighter focusDrawingDelegate) {
		super(viewer, focusDrawingDelegate, TABLE_NAVIGATE);
	}

	ViewerCell getInitialFocusCell() {
		Table table = (Table) getViewer().getControl();
		
		if( table.getItemCount() > 0 ) {
			return getViewer().getViewerRowFromItem(table.getItem(0)).getCell(0);
		}
		
		return null;
	}

}
