/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *                                                 fix in bug: 210752
 *******************************************************************************/

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
	 * Create a new manager with a default navigation strategy:
	 * <ul>
	 * <li><code>SWT.ARROW_UP</code>: navigate to cell above</li>
	 * <li><code>SWT.ARROW_DOWN</code>: navigate to cell below</li>
	 * <li><code>SWT.ARROW_RIGHT</code>: navigate to next visible cell on the right</li>
	 * <li><code>SWT.ARROW_LEFT</code>: navigate to next visible cell on the left</li>
	 * </ul>
	 *
	 * @param viewer
	 *            the viewer the manager is bound to
	 * @param focusDrawingDelegate
	 *            the delegate responsible to highlight selected cell
	 */
	public TableViewerFocusCellManager(TableViewer viewer,
			FocusCellHighlighter focusDrawingDelegate) {
		this(viewer, focusDrawingDelegate, TABLE_NAVIGATE);
	}

	/**
	 * Create a new manager
	 *
	 * @param viewer
	 *            the viewer the manager is bound to
	 * @param focusDrawingDelegate
	 *            the delegate responsible to highlight selected cell
	 * @param navigationStrategy
	 *            the strategy used to navigate the cells
	 */
	public TableViewerFocusCellManager(TableViewer viewer,
			FocusCellHighlighter focusDrawingDelegate,
			CellNavigationStrategy navigationStrategy) {
		super(viewer, focusDrawingDelegate, navigationStrategy);
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
