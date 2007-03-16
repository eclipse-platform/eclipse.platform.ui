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

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @since 3.3
 * 
 */
abstract class SWTFocusCellManager {
	
	private CellNavigationStrategy navigationDelegate;

	private ColumnViewer viewer;

	private ViewerCell focusCell;

	/**
	 * @param viewer
	 * @param focusDrawingDelegate
	 * @param navigationDelegate
	 */
	public SWTFocusCellManager(ColumnViewer viewer,
			FocusCellHighlighter focusDrawingDelegate,
			CellNavigationStrategy navigationDelegate) {
		this.viewer = viewer;
		this.navigationDelegate = navigationDelegate;
		hookListener(viewer);
	}

	private void handleMouseDown(Event event) {
		ViewerCell cell = viewer.getCell(new Point(event.x, event.y));
		if (cell != null) {

			if (!cell.equals(focusCell)) {
				this.focusCell = cell;
				viewer.getControl().redraw();
			}
		}
	}

	private void handleKeyDown(Event event) {
		ViewerCell tmp = null;

		if (navigationDelegate.isCollapseEvent(viewer, focusCell, event)) {
			navigationDelegate.collapse(viewer, focusCell, event);
		} else if (navigationDelegate.isExpandEvent(viewer, focusCell, event)) {
			navigationDelegate.expand(viewer, focusCell, event);
		} else if (navigationDelegate.isNavigationEvent(viewer, event)) {
			tmp = navigationDelegate.findSelectedCell(viewer, focusCell, event);

			if (tmp != null) {
				if (!tmp.equals(focusCell)) {
					this.focusCell = tmp;
					viewer.getControl().redraw();
				}
			}
		}

		if (navigationDelegate.shouldCancelEvent(viewer, event)) {
			event.doit = false;
		}
	}

	private void handleSelection(Event event) {
		if (focusCell != null && focusCell.getItem() != event.item
				&& event.item != null) {
			ViewerRow row = viewer.getViewerRowFromItem(event.item);
			Assert
					.isNotNull(row,
							"Internal Structure invalid. Row item has no row ViewerRow assigned"); //$NON-NLS-1$
			ViewerCell tmp = row.getCell(focusCell.getColumnIndex());
			if (!focusCell.equals(tmp)) {
				this.focusCell = tmp;
				viewer.getControl().redraw();
			}
		}
	}

	private void handleFocusIn(Event event) {
		if( focusCell == null ) {
			focusCell = getInitialFocusCell();
		}
	}
	
	abstract ViewerCell getInitialFocusCell();
	
	private void hookListener(ColumnViewer viewer) {
		Listener listener = new Listener() {

			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.MouseDown:
					handleMouseDown(event);
					break;
				case SWT.KeyDown:
					handleKeyDown(event);
					break;
				case SWT.Selection:
					handleSelection(event);
					break;
				case SWT.FocusIn:
					handleFocusIn(event);
					break;
				}
			}
		};

		viewer.getControl().addListener(SWT.MouseDown, listener);
		viewer.getControl().addListener(SWT.KeyDown, listener);
		viewer.getControl().addListener(SWT.Selection, listener);
		viewer.getControl().addListener(SWT.FocusIn, listener);
	}
	
	/**
	 * @return the cell with the focus
	 * 
	 */
	public ViewerCell getFocusCell() {
		return focusCell;
	}
	
	void setFocusCell(ViewerCell focusCell) {
		this.focusCell = focusCell;
	}
	
	ColumnViewer getViewer() {
		return viewer;
	}
}
