/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation; bug 153993
 *												   fix in bug 163317
 *												   fix in bug 151295, bug 167323
 ******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;

/**
 * The ColumnViewer is the abstract superclass of viewers that have columns
 * (e.g., AbstractTreeViewer and AbstractTableViewer). Concrete subclasses of
 * {@link ColumnViewer} should implement a matching concrete subclass of
 * {@link ViewerColumn}.
 * 
 * <strong> This class is not intended to be subclassed outside of the JFace
 * viewers framework.</strong>
 * 
 * @since 3.3 <strong>EXPERIMENTAL</strong> This class or interface has been
 *        added as part of a work in progress. This API may change at any given
 *        time. Please do not use this API without consulting with the
 *        Platform/UI team.
 * 
 */
public abstract class ColumnViewer extends StructuredViewer {

	private ToolTipSupport tooltipSupport;

	/**
	 * The cell is a cached viewer cell used for refreshing.
	 */
	private ViewerCell cell = new ViewerCell(null, 0);

	private AbstractViewerEditor viewerEditor;

	private int tabEditingStyle = EditingSupport.TABBING_NONE;
	
	/**
	 * Create a new instance of the receiver.
	 */
	public ColumnViewer() {
		viewerEditor = createViewerEditor();
	}

	protected void hookControl(Control control) {
		super.hookControl(control);
		hookEditingSupport(control);
	}

	/**
	 * Hook up the editing support. Subclasses may override.
	 * 
	 * @param control
	 *            the control you want to hook on
	 */
	protected void hookEditingSupport(Control control) {
		// Needed for backwards comp with AbstractTreeViewer and TableTreeViewer
		// who are not hooked this way others may already overwrite and provide
		// their
		// own impl
		if (viewerEditor != null) {
			control.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					viewerEditor.handleMouseDown(e);
				}
			});
		}
	}

	/**
	 * Creates the viewer editor used for editing cell contents. To be implemented by subclasses.
	 * 
	 * @return the editor, or <code>null</code> if this viewer does not support editing cell contents.
	 */
	protected abstract AbstractViewerEditor createViewerEditor();

	/**
	 * Returns the viewer cell at the given widget-relative coordinates, or
	 * <code>null</code> if there is no cell at that location
	 * 
	 * @param point
	 *            the widget-relative coordinates
	 * @return the cell or <code>null</code> if no cell is found at the given point
	 */
	ViewerCell getCell(Point point) {
		ViewerRow row = getViewerRow(point);
		if (row != null) {
			return row.getCell(point);
		}

		return null;
	}

	/**
	 * Returns the viewer row at the given widget-relative coordinates.
	 * 
	 * @param point
	 *            the widget-relative coordinates of the viewer row
	 * @return ViewerRow the row or <code>null</code> if no row is found at
	 *         the given coordinates
	 */
	protected ViewerRow getViewerRow(Point point) {
		Item item = getItemAt(point);

		if (item != null) {
			return getViewerRowFromItem(item);
		}

		return null;
	}

	/**
	 * Returns the viewer row associated with the given row widget.
	 * 
	 * @param item the row widget
	 * @return ViewerRow the associated viewer row
	 */
	protected ViewerRow getViewerRowFromItem(Widget item) {
		return (ViewerRow) item.getData(ViewerRow.ROWPART_KEY);
	}

	/**
	 * Returns the column widget at the given column index.
	 * 
	 * @param columnIndex the column index
	 * @return Widget the column widget
	 */
	protected abstract Widget getColumnViewerOwner(int columnIndex);

	/**
	 * Returns the viewer column for the given column index.
	 * 
	 * @param columnIndex
	 *            the column index
	 * @return the viewer column at the given index, or <code>null</code> if
	 *         there is none for the given index
	 */
	/* package */ ViewerColumn getViewerColumn(final int columnIndex) {

		ViewerColumn viewer;
		Widget columnOwner = getColumnViewerOwner(columnIndex);

		if (columnOwner == null) {
			return null;
		}

		viewer = (ViewerColumn) columnOwner
				.getData(ViewerColumn.COLUMN_VIEWER_KEY);

		if (viewer == null) {
			viewer = createViewerColumn(columnOwner, CellLabelProvider
					.createViewerLabelProvider(getLabelProvider()));
			setupEditingSupport(columnIndex, viewer);
		}

		if (viewer.getEditingSupport() == null && getCellModifier() != null) {
			setupEditingSupport(columnIndex, viewer);
		}

		return viewer;
	}

	/**
	 * Sets up editing support for the given column based on the "old" cell editor API.
	 * 
	 * @param columnIndex
	 * @param viewer
	 */
	private void setupEditingSupport(final int columnIndex, ViewerColumn viewer) {
		if (getCellModifier() != null) {
			viewer.setEditingSupport(new EditingSupport(this) {

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
	 * Creates a generic viewer column for the given column widget, based on the given label provider.
	 * 
	 * @param columnOwner the column widget
	 * @param labelProvider the label provider to use for the column
	 * @return ViewerColumn the viewer column
	 */
	private ViewerColumn createViewerColumn(Widget columnOwner,
			CellLabelProvider labelProvider) {
		ViewerColumn column = new ViewerColumn(this,columnOwner) {};
		column.setLabelProvider(labelProvider, false);
		return column;
	}

	/**
	 * Activate the support for custom tooltips.
	 */
	public void activateCustomTooltips() {
		if (tooltipSupport == null) {
			tooltipSupport = new ToolTipSupport(this);
		} else {
			tooltipSupport.activate();
		}
	}

	/**
	 * Deactivate the support for custom tooltips.
	 */
	public void deactivateCustomTooltips() {
		if (tooltipSupport != null) {
			tooltipSupport.deactivate();
		}
	}

	/**
	 * Update the cached cell object with the given row and column.
	 * 
	 * @param rowItem
	 * @param column
	 * @return ViewerCell
	 */
	/* package */ ViewerCell updateCell(ViewerRow rowItem, int column) {
		cell.update(rowItem, column);
		return cell;
	}

	/**
	 * Returns the {@link Item} at the given widget-relative coordinates, or
     * <code>null</code> if there is no item at the given coordinates.
	 * 
	 * @param point
	 *            the widget-relative coordinates
	 * @return the {@link Item} at the coordinates or <code>null</code> if there
	 *         is no item at the given coordinates
	 */
	protected abstract Item getItemAt(Point point);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#getItem(int, int)
	 */
	protected Item getItem(int x, int y) {
		return getItemAt(getControl().toControl(x, y));
	}

	/**
	 * The column viewer implementation of this <code>Viewer</code> framework
	 * method ensures that the given label provider is an instance of
	 * <code>ITableLabelProvider</code>, <code>ILabelProvider</code>, or
	 * <code>CellLabelProvider</code>.
	 * <p>
	 * If the label provider is an {@link ITableLabelProvider}, then it
	 * provides a separate label text and image for each column. Implementers of
	 * <code>ITableLabelProvider</code> may also implement
	 * {@link ITableColorProvider} and/or {@link ITableFontProvider} to provide
	 * colors and/or fonts.
	 * </p>
	 * <p>
	 * If the label provider is an <code>ILabelProvider</code>, then it
	 * provides only the label text and image for the first column, and any
	 * remaining columns are blank. Implementers of <code>ILabelProvider</code>
	 * may also implement {@link IColorProvider} and/or {@link IFontProvider} to
	 * provide colors and/or fonts.
	 * </p>
	 * 
	 */
	public void setLabelProvider(IBaseLabelProvider labelProvider) {
		Assert.isTrue(labelProvider instanceof ITableLabelProvider
				|| labelProvider instanceof ILabelProvider
				|| labelProvider instanceof CellLabelProvider);
		updateColumnParts(labelProvider);// Reset the label providers in the
		// columns
		super.setLabelProvider(labelProvider);
	}

	/**
	 * Clear the viewer parts for the columns
	 */
	private void updateColumnParts(IBaseLabelProvider labelProvider) {
		ViewerColumn column;
		int i = 0;

		while ((column = getViewerColumn(i++)) != null) {
			column.setLabelProvider(CellLabelProvider
					.createViewerLabelProvider(labelProvider), false);
		}
	}

	/**
	 * Cancels a currently active cell editor if one is active. All changes
     * already done in the cell editor are lost.
	 * 
	 * @since 3.1 (in subclasses, added in 3.3 to abstract class)
	 */
	public void cancelEditing() {
		if (viewerEditor != null) {
			viewerEditor.cancelEditing();
		}
	}

	/**
	 * Apply the value of the active cell editor if one is active.
	 * 
	 * @since 3.3
	 */
	protected void applyEditorValue() {
		if (viewerEditor != null) {
			viewerEditor.applyEditorValue();
		}
	}

	/**
	 * Starts editing the given element at the given column index.
	 * 
	 * @param element
	 *            the element
	 * @param column
	 *            the column index
	 * @since 3.1 (in subclasses, added in 3.3 to abstract class)
	 */
	public void editElement(Object element, int column) {
		if (viewerEditor != null) {
			viewerEditor.editElement(element, column);
		}
	}

	/**
	 * Return the CellEditors for the receiver, or <code>null</code> if no
     * cell editors are set.
	 * <p>
	 * Since 3.3, an alternative API is available, see
	 * {@link ViewerColumn#setEditingSupport(EditingSupport)} for a more flexible
	 * way of editing values in a column viewer.
	 * </p>
	 * 
	 * @return CellEditor[]
	 * @since 3.1 (in subclasses, added in 3.3 to abstract class)
	 * @see ViewerColumn#setEditingSupport(EditingSupport)
	 * @see EditingSupport
	 */
	public CellEditor[] getCellEditors() {
		if (viewerEditor != null) {
			return viewerEditor.getCellEditors();
		}
		return null;
	}

	/**
	 * Returns the cell modifier of this viewer, or <code>null</code> if none
     * has been set.
	 * 
	 * <p>
	 * Since 3.3, an alternative API is available, see
	 * {@link ViewerColumn#setEditingSupport(EditingSupport)} for a more flexible
	 * way of editing values in a column viewer.
	 * </p>
	 * 
	 * @return the cell modifier, or <code>null</code>
	 * @since 3.1 (in subclasses, added in 3.3 to abstract class)
	 * @see ViewerColumn#setEditingSupport(EditingSupport)
	 * @see EditingSupport
	 */
	public ICellModifier getCellModifier() {
		if (viewerEditor != null) {
			return viewerEditor.getCellModifier();
		}
		return null;
	}

	/**
	 * Returns the column properties of this table viewer. The properties must
	 * correspond with the columns of the table control. They are used to
	 * identify the column in a cell modifier.
	 * 
	 * <p>
	 * Since 3.3, an alternative API is available, see
	 * {@link ViewerColumn#setEditingSupport(EditingSupport)} for a more flexible
	 * way of editing values in a column viewer.
	 * </p>
	 * 
	 * @return the list of column properties
	 * @since 3.1 (in subclasses, added in 3.3 to abstract class)
	 * @see ViewerColumn#setEditingSupport(EditingSupport)
	 * @see EditingSupport
	 */
	public Object[] getColumnProperties() {
		if (viewerEditor != null) {
			return viewerEditor.getColumnProperties();
		}
		return null;
	}

	/**
	 * Returns whether there is an active cell editor.
	 * 
	 * <p>
	 * Since 3.3, an alternative API is available, see
	 * {@link ViewerColumn#setEditingSupport(EditingSupport)} for a more flexible
	 * way of editing values in a column viewer.
	 * </p>
	 * 
	 * @return <code>true</code> if there is an active cell editor, and
	 *         <code>false</code> otherwise
	 * @since 3.1 (in subclasses, added in 3.3 to abstract class)
	 * @see ViewerColumn#setEditingSupport(EditingSupport)
	 * @see EditingSupport
	 */
	public boolean isCellEditorActive() {
		if (viewerEditor != null) {
			return viewerEditor.isCellEditorActive();
		}
		return false;
	}

	/**
	 * Sets the cell editors of this column viewer. If editing is not supported
	 * by this viewer the call simply has no effect.
	 * 
	 * <p>
	 * Since 3.3, an alternative API is available, see
	 * {@link ViewerColumn#setEditingSupport(EditingSupport)} for a more flexible
	 * way of editing values in a column viewer.
	 * </p>
	 * 
	 * @param editors
	 *            the list of cell editors
	 * @since 3.1 (in subclasses, added in 3.3 to abstract class)
	 * @see ViewerColumn#setEditingSupport(EditingSupport)
	 * @see EditingSupport
	 */
	public void setCellEditors(CellEditor[] editors) {
		if (viewerEditor != null) {
			viewerEditor.setCellEditors(editors);
		}
	}

	/**
	 * Sets the cell modifier for this column viewer. This method does nothing if editing
	 * is not supported by this viewer.
	 * 
	 * <p>
	 * Since 3.3, an alternative API is available, see
	 * {@link ViewerColumn#setEditingSupport(EditingSupport)} for a more flexible
	 * way of editing values in a column viewer.
	 * </p>
	 * 
	 * @param modifier
	 *            the cell modifier
	 * @since 3.1 (in subclasses, added in 3.3 to abstract class)
	 * @see ViewerColumn#setEditingSupport(EditingSupport)
	 * @see EditingSupport
	 */
	public void setCellModifier(ICellModifier modifier) {
		if (viewerEditor != null) {
			viewerEditor.setCellModifier(modifier);
		}
	}

	/**
	 * Sets the column properties of this column viewer. The properties must
	 * correspond with the columns of the control. They are used to identify the
	 * column in a cell modifier. If editing is not supported by this viewer the
	 * call simply has no effect.
	 * 
	 * <p>
	 * Since 3.3, an alternative API is available, see
	 * {@link ViewerColumn#setEditingSupport(EditingSupport)} for a more flexible
	 * way of editing values in a column viewer.
	 * </p>
	 * 
	 * @param columnProperties
	 *            the list of column properties
	 * @since 3.1 (in subclasses, added in 3.3 to abstract class)
	 * @see ViewerColumn#setEditingSupport(EditingSupport)
	 * @see EditingSupport
	 */
	public void setColumnProperties(String[] columnProperties) {
		if (viewerEditor != null) {
			viewerEditor.setColumnProperties(columnProperties);
		}
	}
	
	/**
	 * The tab-editing style used if the default implementation is used
	 * 
	 * @param tabEditingStyle
	 *            bit mask for the tab editing style
	 */
	public void setTabEditingStyle(int tabEditingStyle) {
		this.tabEditingStyle = tabEditingStyle;
	}

	int getTabEditingStyle() {
		return this.tabEditingStyle;
	}
	
	/**
	 * Returns the number of columns contained in the receiver. If no columns
	 * were created by the programmer, this value is zero, despite the fact that
	 * visually, one column of items may be visible. This occurs when the
	 * programmer uses the column viewer like a list, adding elements but never
	 * creating a column.
	 * 
	 * @return the number of columns
	 * 
	 * @since 3.3
	 */
	protected abstract int doGetColumnCount();
}
