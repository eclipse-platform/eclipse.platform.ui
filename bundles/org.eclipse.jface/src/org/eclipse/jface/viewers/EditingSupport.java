/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     											   fix in bug 151295
 ******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * EditingSupport is the abstract superclass of the support for cell editing.
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 * 
 */
public abstract class EditingSupport {
	/**
	 * Tabing from cell to cell is turned off
	 */
	public static final int TABING_NONE = 1;

	/**
	 * Should if the end of the row is reach started from the start/end of the
	 * row below/above
	 */
	public static final int TABING_MOVE_TO_ROW_NEIGHBOR = 1 << 1;

	/**
	 * Should if the end of the row is reach started from the beginning in the
	 * same row
	 */
	public static final int TABING_CYCLE_IN_ROW = 1 << 2;

	/**
	 * Support tabing to Cell above/below the current cell
	 */
	public static final int TABING_VERTICAL = 1 << 3;

	/**
	 * Should tabing from column to column with in one row be supported
	 */
	public static final int TABING_HORIZONTAL = 1 << 4;

	private ColumnViewer viewer;

	/**
	 * @param viewer
	 *            a new viewer
	 */
	public EditingSupport(ColumnViewer viewer) {
		Assert.isNotNull(viewer,"Viewer is not allowed to be null"); //$NON-NLS-1$
		this.viewer = viewer;
	}

	/**
	 * The editor to be shown
	 * 
	 * @param element
	 *            the model element
	 * @return the CellEditor
	 */
	protected abstract CellEditor getCellEditor(Object element);

	/**
	 * Is the cell editable
	 * 
	 * @param element
	 *            the model element
	 * @return true if editable
	 */
	protected abstract boolean canEdit(Object element);

	/**
	 * Get the value to set to the editor
	 * 
	 * @param element
	 *            the model element
	 * @return the value shown
	 */
	protected abstract Object getValue(Object element);

	/**
	 * Restore the value from the CellEditor
	 * 
	 * <p><b>Subclasses should overwrite!</b></p>
	 * 
	 * @param element
	 *            the model element
	 * @param value
	 *            the new value
	 */
	protected abstract void setValue(Object element, Object value);

	/**
	 * @return <code>true</code> if tabing supported
	 */
	protected boolean isTabingSupported() {
		return (TABING_NONE & getTabingStyle()) != TABING_NONE;
	}

	/**
	 * @return the bit mask representing the tabing style
	 */
	int getTabingStyle() {
		return viewer.getTabEditingStyle();
	}

	/**
	 * Process the travers event and opens the next available editor depending
	 * of the implemented strategy. The default implementation uses the style
	 * constants
	 * <ul>
	 * <li>{@link #TABING_MOVE_TO_ROW_NEIGHBOR}</li>
	 * <li>{@link #TABING_CYCLE_IN_ROW}</li>
	 * <li>{@link #TABING_VERTICAL}</li>
	 * <li>{@link #TABING_HORIZONTAL}</li>
	 * </ul>
	 * 
	 * <p>
	 * Subclasses may overwrite to implement their custom logic to edit the next
	 * cell
	 * </p>
	 * 
	 * @param columnIndex
	 *            the index of the current column
	 * @param row
	 *            the current row
	 * @param event
	 *            the travers event
	 */
	protected void processTraversEvent(int columnIndex,
			ViewerRow row, TraverseEvent event) {
		
		ViewerCell cell2edit = null;

		if (event.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
			event.doit = false;

			if ((event.stateMask & SWT.CTRL) == SWT.CTRL
					&& (getTabingStyle() & TABING_VERTICAL) == TABING_VERTICAL) {
				cell2edit = searchCellAboveBelow(row, viewer, columnIndex, true);
			} else if ((getTabingStyle() & TABING_HORIZONTAL) == TABING_HORIZONTAL) {
				cell2edit = searchPreviousCell(row, viewer, columnIndex,
						columnIndex);
			}
		} else if (event.detail == SWT.TRAVERSE_TAB_NEXT) {
			event.doit = false;

			if ((event.stateMask & SWT.CTRL) == SWT.CTRL
					&& (getTabingStyle() & TABING_VERTICAL) == TABING_VERTICAL) {
				cell2edit = searchCellAboveBelow(row, viewer, columnIndex,
						false);
			} else if ((getTabingStyle() & TABING_HORIZONTAL) == TABING_HORIZONTAL) {
				cell2edit = searchNextCell(row, viewer, columnIndex,
						columnIndex);
			}
		}

		if (cell2edit != null) {
			viewer.editElement(cell2edit.getElement(), cell2edit
					.getColumnIndex());
		}
	}

	private ViewerCell searchCellAboveBelow(ViewerRow row, ColumnViewer viewer,
			int columnIndex, boolean above) {
		ViewerCell rv = null;

		ViewerRow newRow = null;

		if (above) {
			newRow = getRowAbove(row, viewer);
		} else {
			newRow = getRowBelow(row, viewer);
		}

		if (newRow != null) {
			ViewerColumn column = viewer.getViewerColumn(columnIndex);
			if (column != null
					&& column.getEditingSupport().canEdit(
							newRow.getItem().getData())) {
				rv = newRow.getCell(columnIndex);
			} else {
				rv = searchCellAboveBelow(newRow, viewer, columnIndex, above);
			}
		}

		return rv;
	}

	private ViewerCell searchPreviousCell(ViewerRow row, ColumnViewer viewer,
			int columnIndex, int startIndex) {
		ViewerCell rv = null;

		if (columnIndex - 1 >= 0) {
			ViewerColumn column = viewer.getViewerColumn(columnIndex - 1);
			if (column != null
					&& column.getEditingSupport().canEdit(
							row.getItem().getData())) {
				rv = row.getCell(columnIndex - 1);
			} else {
				rv = searchPreviousCell(row, viewer, columnIndex - 1,
						startIndex);
			}
		} else {
			if ((getTabingStyle() & TABING_CYCLE_IN_ROW) == TABING_CYCLE_IN_ROW) {
				// Check that we don't get into endless loop
				if (columnIndex - 1 != startIndex) {
					// Don't subtract -1 from getColumnCount() we need to
					// start in the virtual column
					// next to it
					rv = searchPreviousCell(row, viewer, row.getColumnCount(),
							startIndex);
				}
			} else if ((getTabingStyle() & TABING_MOVE_TO_ROW_NEIGHBOR) == TABING_MOVE_TO_ROW_NEIGHBOR) {
				ViewerRow rowAbove = getRowAbove(row, viewer);
				if (rowAbove != null) {
					rv = searchPreviousCell(rowAbove, viewer, rowAbove
							.getColumnCount(), startIndex);
				}
			}
		}

		return rv;
	}

	private ViewerCell searchNextCell(ViewerRow row, ColumnViewer viewer,
			int columnIndex, int startIndex) {
		ViewerCell rv = null;

		if (columnIndex + 1 < row.getColumnCount()) {
			ViewerColumn column = viewer.getViewerColumn(columnIndex + 1);
			if (column != null
					&& column.getEditingSupport().canEdit(
							row.getItem().getData())) {
				rv = row.getCell(columnIndex + 1);
			} else {
				rv = searchNextCell(row, viewer, columnIndex + 1, startIndex);
			}
		} else {
			if ((getTabingStyle() & TABING_CYCLE_IN_ROW) == TABING_CYCLE_IN_ROW) {
				// Check that we don't get into endless loop
				if (columnIndex + 1 != startIndex) {
					// Start from -1 from the virtual column before the
					// first one
					rv = searchNextCell(row, viewer, -1, startIndex);
				}
			} else if ((getTabingStyle() & TABING_MOVE_TO_ROW_NEIGHBOR) == TABING_MOVE_TO_ROW_NEIGHBOR) {
				ViewerRow rowBelow = getRowBelow(row, viewer);
				if (rowBelow != null) {
					rv = searchNextCell(rowBelow, viewer, -1, startIndex);
				}
			}
		}

		return rv;
	}

	private ViewerRow getRowAbove(ViewerRow row, ColumnViewer viewer) {
		// TODO maybe there's a better solution maybe we should provide an
		// API in ViewerColumn
		// to find row above/below itself?
		Rectangle r = row.getBounds();
		return viewer.getViewerRow(new Point(r.x, r.y - 2));
	}

	private ViewerRow getRowBelow(ViewerRow row, ColumnViewer viewer) {
		// TODO maybe there's a better solution maybe we should provide an
		// API in ViewerColumn
		// to find row above/below itself?
		Rectangle r = row.getBounds();
		return viewer.getViewerRow(new Point(r.x,
				r.y + r.height + 2));
	}
	
	/**
	 * @return the viewer this editing support works for
	 */
	public ColumnViewer getViewer() {
		return viewer;
	}
}
