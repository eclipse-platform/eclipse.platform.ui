/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Shindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;

/**
 * The ColumnViewer is the abstract superclass of viewers that jave columns
 * (TreeViewer and TableViewer).
 * 
 * @since 3.3 <strong>EXPERIMENTAL</strong> This class or interface has been
 *        added as part of a work in progress. This API may change at any given
 *        time. Please do not use this API without consulting with the
 *        Platform/UI team.
 * 
 */
abstract class ColumnViewer extends StructuredViewer {

	private ToolTipSupport tooltipSupport;

	/**
	 * The cell is a cached viewer cell used for refreshing.
	 */
	private ViewerCell cell = new ViewerCell(null, 0);

	/**
	 * Create a new instance of the receiver.
	 */
	public ColumnViewer() {
		super();
	}

	/**
	 * Get the cell at this point.
	 * <p>
	 * <i>Subclasses should overwrite this method and provide a meaningful
	 * implementation</i>
	 * </p>
	 * 
	 * @param point
	 *            the point in the viewer where you need to corresponding cell
	 *            from
	 * @return the cell or if no cell is found at this point
	 */
	ViewerCell getCell(Point point) {
		ViewerRow row = getViewerRow(point);
		if (row != null) {
			return row.getCell(point);
		}

		return null;
	}

	/**
	 * Get the ViewerRow at point.
	 * 
	 * @param point
	 *            the point <b>relative to the display</b> you are interested
	 *            in
	 * @return ViewerRow the row or <code>null</code>if no row is found at
	 *         this point
	 */
	protected ViewerRow getViewerRow(Point point) {
		Item item = getItemAt(point);

		if (item != null) {
			return getViewerRowFromItem(item);
		}

		return null;
	}

	/**
	 * Get the ViewerRow associated with the Widget.
	 * 
	 * @param item
	 * @return ViewerRow
	 */
	protected ViewerRow getViewerRowFromItem(Widget item) {
		return (ViewerRow) item.getData(ViewerRow.ROWPART_KEY);
	}

	/**
	 * Get the widget for the column at columnIndex.
	 * 
	 * @param columnIndex
	 * @return Widget
	 */
	protected abstract Widget getColumnViewerOwner(int columnIndex);

	/**
	 * Returns the cell modifier of this viewer.
	 * 
	 * @return the cell modifier
	 * @since 3.1 (in subclasses, added in 3.3 to abstract class)
	 */
	public abstract ICellModifier getCellModifier();

	/**
	 * Return the CellEditors for the receiver.
	 * 
	 * @return CellEditor[]
	 * @since 3.1 (in subclasses, added in 3.3 to abstract class)
	 */
	public abstract CellEditor[] getCellEditors();

	/**
	 * Returns the column properties of this table viewer. The properties must
	 * correspond with the columns of the table control. They are used to
	 * identify the column in a cell modifier.
	 * 
	 * @return the list of column properties
	 * @since 3.1 (in subclasses, added in 3.3 to abstract class)
	 */
	public abstract Object[] getColumnProperties();

	/**
	 * Return the TableColumnViewer at columnIndex
	 * 
	 * @param columnIndex
	 * @return TableColumnViewer
	 */
	public ViewerColumn getViewerColumn(final int columnIndex) {

		ViewerColumn viewer;
		Widget columnOwner = getColumnViewerOwner(columnIndex);

		if (columnOwner == null) {
			return null;
		}

		viewer = (ViewerColumn) columnOwner
				.getData(ViewerColumn.COLUMN_VIEWER_KEY);

		if (viewer == null) {
			viewer = createColumnViewer(columnOwner, CellLabelProvider
					.createViewerLabelProvider(getLabelProvider()));
			setupEditingSupport(columnIndex, viewer);
		}

		if (viewer.getEditingSupport() == null && getCellModifier() != null) {
			setupEditingSupport(columnIndex, viewer);
		}

		return viewer;
	}

	/**
	 * Set the ViewerColumn at columnIndex
	 * 
	 * @param columnIndex
	 * @param viewer
	 */
	private void setupEditingSupport(final int columnIndex, ViewerColumn viewer) {
		if (getCellModifier() != null) {
			viewer.setEditingSupport(new EditingSupport() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
				 */
				public boolean canEdit(Object element) {
					return getCellModifier().canModify(element,
							(String) getColumnProperties()[columnIndex]);
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
				 */
				public CellEditor getCellEditor(Object element) {
					return getCellEditors()[columnIndex];
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
				 */
				public Object getValue(Object element) {
					return getCellModifier().getValue(element,
							(String) getColumnProperties()[columnIndex]);
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object,
				 *      java.lang.Object)
				 */
				public void setValue(Object element, Object value) {
					getCellModifier().modify(findItem(element),
							(String) getColumnProperties()[columnIndex], value);
				}
			});
		}
	}

	/**
	 * Create a ViewerColumn for the columnOwner.
	 * 
	 * @param columnOwner
	 * @param labelProvider
	 * @return ViewerColumn
	 */
	protected ViewerColumn createColumnViewer(Widget columnOwner,
			CellLabelProvider labelProvider) {
		return new ViewerColumn(columnOwner, labelProvider);
	}

	/**
	 * Activate the tooltip support.
	 */
	public void activateCustomTooltips() {
		if (tooltipSupport == null) {
			tooltipSupport = new ToolTipSupport(this);
		} else {
			tooltipSupport.activate();
		}
	}

	/**
	 * Deactivate the tooltip support.
	 */
	public void deactivateCustomTooltips() {
		if (tooltipSupport != null) {
			tooltipSupport.deactivate();
		}
	}

	/**
	 * Update the cached cell with the row and column.
	 * 
	 * @param rowItem
	 * @param column
	 * @return ViewerCell
	 */
	ViewerCell updateCell(ViewerRow rowItem, int column) {
		cell.update(rowItem, column);
		return cell;
	}

	/**
	 * Find the {@link Item} relative to the widget coordinate system.
	 * 
	 * @param point
	 *            the x/y coordinates relative to the widget
	 * @return the {@link Item} at the coordinates or <code>null</code> if no item is
	 *         located there
	 */
	protected Item getItemAt(Point point){
		return null;//Return null by default
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#getItem(int, int)
	 */
	protected Item getItem(int x, int y) {
		return getItemAt(getControl().toControl(x, y));
	}
}
