/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   IBM Corporation - initial API and implementation
 * 	   Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * 												 - fix for bug 183850, 182652, 182800, 215069
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * A concrete implementation of {@link FocusCellHighlighter} using by setting
 * the control into owner draw mode and highlighting the currently selected
 * cell. To make the use this class you should create the control with the
 * {@link SWT#FULL_SELECTION} bit set
 * 
 * This class can be subclassed to configure how the coloring of the selected
 * cell.
 * 
 * @since 3.3
 * 
 */
public class FocusCellOwnerDrawHighlighter extends FocusCellHighlighter {
	/**
	 * Create a new instance which can be passed to a
	 * {@link TreeViewerFocusCellManager}
	 * 
	 * @param viewer
	 *            the viewer
	 */
	public FocusCellOwnerDrawHighlighter(ColumnViewer viewer) {
		super(viewer);
		hookListener(viewer);
	}

	private void markFocusedCell(Event event, ViewerCell cell) {
		Color background = (cell.getControl().isFocusControl()) ? getSelectedCellBackgroundColor(cell)
				: getSelectedCellBackgroundColorNoFocus(cell);
		Color foreground = (cell.getControl().isFocusControl()) ? getSelectedCellForegroundColor(cell)
				: getSelectedCellForegroundColorNoFocus(cell);

		if (foreground != null || background != null || onlyTextHighlighting(cell)) {
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
			
			if (onlyTextHighlighting(cell)) {
				Rectangle area = event.getBounds();
				Rectangle rect = cell.getTextBounds();
				if( rect != null ) {
					area.x = rect.x;
				}
				gc.fillRectangle(area);
			} else {
				gc.fillRectangle(event.getBounds());
			}
			
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
	 * The color to use when rendering the background of the selected cell when
	 * the control has the input focus
	 * 
	 * @param cell
	 *            the cell which is colored
	 * @return the color or <code>null</code> to use the default
	 */
	protected Color getSelectedCellBackgroundColor(ViewerCell cell) {
		return null;
	}

	/**
	 * The color to use when rendering the foreground (=text) of the selected
	 * cell when the control has the input focus
	 * 
	 * @param cell
	 *            the cell which is colored
	 * @return the color or <code>null</code> to use the default
	 */
	protected Color getSelectedCellForegroundColor(ViewerCell cell) {
		return null;
	}

	/**
	 * The color to use when rendering the foreground (=text) of the selected
	 * cell when the control has <b>no</b> input focus
	 * 
	 * @param cell
	 *            the cell which is colored
	 * @return the color or <code>null</code> to use the same used when
	 *         control has focus
	 * @since 3.4
	 */
	protected Color getSelectedCellForegroundColorNoFocus(ViewerCell cell) {
		return null;
	}

	/**
	 * The color to use when rendering the background of the selected cell when
	 * the control has <b>no</b> input focus
	 * 
	 * @param cell
	 *            the cell which is colored
	 * @return the color or <code>null</code> to use the same used when
	 *         control has focus
	 * @since 3.4
	 */
	protected Color getSelectedCellBackgroundColorNoFocus(ViewerCell cell) {
		return null;
	}

	/**
	 * Controls whether the whole cell or only the text-area is highlighted
	 * 
	 * @param cell
	 *            the cell which is highlighted
	 * @return <code>true</code> if only the text area should be highlighted
	 * @since 3.4
	 */
	protected boolean onlyTextHighlighting(ViewerCell cell) {
		return false;
	}

	protected void focusCellChanged(ViewerCell newCell, ViewerCell oldCell) {
		super.focusCellChanged(newCell, oldCell);

		// Redraw new area
		if (newCell != null) {
			Rectangle rect = newCell.getBounds();
			int x = newCell.getColumnIndex() == 0 ? 0 : rect.x;
			int width = newCell.getColumnIndex() == 0 ? rect.x + rect.width
					: rect.width;
			// 1 is a fix for Linux-GTK
			newCell.getControl().redraw(x, rect.y - 1, width, rect.height + 1,
					true);
		}

		if (oldCell != null) {
			Rectangle rect = oldCell.getBounds();
			int x = oldCell.getColumnIndex() == 0 ? 0 : rect.x;
			int width = oldCell.getColumnIndex() == 0 ? rect.x + rect.width
					: rect.width;
			// 1 is a fix for Linux-GTK
			oldCell.getControl().redraw(x, rect.y - 1, width, rect.height + 1,
					true);
		}
	}
}
