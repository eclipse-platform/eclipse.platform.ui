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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @since 3.3
 * 
 */
public class FocusCellOwnerDrawHighlighter extends FocusCellHighlighter {

	private ViewerCell oldCell;

	/**
	 * @param viewer
	 *            the viewer
	 */
	public FocusCellOwnerDrawHighlighter(ColumnViewer viewer) {
		super(viewer);
		hookListener(viewer);
	}

	private void markFocusedCell(Event event, ViewerCell cell) {
		Color background = getSelectedCellBackgroundColor(cell);
		Color foreground = getSelectedCellForegroundColor(cell);

		if (foreground != null || background != null) {
			GC gc = event.gc;

			if (background == null) {
				background = cell.getItem().getDisplay().getSystemColor(
						SWT.COLOR_LIST_SELECTION);
			}

			if (foreground == null) {
				foreground = cell.getItem().getDisplay().getSystemColor(
						SWT.COLOR_LIST_SELECTION_TEXT);
			}

			gc.setBackground(background);
			gc.setForeground(foreground);
			gc.fillRectangle(event.getBounds());

			// This is a workaround for an SWT-Bug on WinXP bug 169517
			gc.drawText(" ", cell.getBounds().x, cell.getBounds().y, false); //$NON-NLS-1$
			event.detail &= ~SWT.SELECTED;
		}
	}

	private void removeSelectionInformation(Event event, ViewerCell cell) {
		GC gc = event.gc;
		gc.setBackground(cell.getViewerRow().getBackground(
				cell.getColumnIndex()));
		gc.setForeground(cell.getViewerRow().getForeground(
				cell.getColumnIndex()));
		gc.fillRectangle(cell.getBounds());
		// This is a workaround for an SWT-Bug on WinXP bug 169517
		gc.drawText(" ", cell.getBounds().x, cell.getBounds().y, false); //$NON-NLS-1$
		event.detail &= ~SWT.SELECTED;
	}

	private void hookListener(final ColumnViewer viewer) {

		Listener listener = new Listener() {

			public void handleEvent(Event event) {
				if ((event.detail & SWT.SELECTED) > 0) {
					ViewerCell focusCell = getFocusCell();
					ViewerRow row = viewer.getViewerRowFromItem(event.item);

					Assert
							.isNotNull(row,
									"Internal structure invalid. Item without associated row is not possible."); //$NON-NLS-1$

					ViewerCell cell = row.getCell(event.index);

					if (focusCell == null || !cell.equals(focusCell)) {
						removeSelectionInformation(event, cell);
					} else {
						markFocusedCell(event, cell);
					}
				}
			}

		};
		viewer.getControl().addListener(SWT.EraseItem, listener);
	}

	/**
	 * @param cell
	 *            the cell which is colored
	 * @return the color
	 */
	protected Color getSelectedCellBackgroundColor(ViewerCell cell) {
		return null;
	}

	/**
	 * @param cell
	 *            the cell which is colored
	 * @return the color
	 */
	protected Color getSelectedCellForegroundColor(ViewerCell cell) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.FocusCellHighlighter#focusCellChanged(org.eclipse.jface.viewers.ViewerCell)
	 */
	protected void focusCellChanged(ViewerCell cell) {
		super.focusCellChanged(cell);

		// Redraw new area
		if (cell != null) {
			Rectangle rect = cell.getBounds();
			int x = cell.getColumnIndex() == 0 ? 0 : rect.x;
			int width = cell.getColumnIndex() == 0 ? rect.x + rect.width
					: rect.width;
			cell.getControl().redraw(x, rect.y, width, rect.height, true);
		}

		if (oldCell != null) {
			Rectangle rect = oldCell.getBounds();
			int x = oldCell.getColumnIndex() == 0 ? 0 : rect.x;
			int width = oldCell.getColumnIndex() == 0 ? rect.x + rect.width
					: rect.width;
			oldCell.getControl().redraw(x, rect.y, width, rect.height, true);
		}

		this.oldCell = cell;
	}
}
