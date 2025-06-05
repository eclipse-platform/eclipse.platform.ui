/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation; bug 153993
 *												   fix in bug 163317, 151295, 167323, 167858, 184346, 187826, 201905
 *     Stefan Winkler <stefan@winklerweb.net> - Bug 242231
 *******************************************************************************/

package org.eclipse.jface.viewers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.internal.InternalPolicy;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.internal.ExpandableNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;

/**
 * The ColumnViewer is the abstract superclass of viewers that have columns
 * (e.g., AbstractTreeViewer and AbstractTableViewer). Concrete subclasses of
 * {@link ColumnViewer} should implement a matching concrete subclass of {@link
 * ViewerColumn}.
 * <p>
 * <strong> This class is not intended to be subclassed outside of the JFace
 * viewers framework.</strong>
 * </p>
 *
 * @since 3.3
 */
public abstract class ColumnViewer extends StructuredViewer {

	/**
	 * Number of items to be shown before an {@link ExpandableNode} is displayed,
	 * default is zero (no limits).
	 */
	private int itemsLimit;

	private CellEditor[] cellEditors;

	private ICellModifier cellModifier;

	private String[] columnProperties;

	private ColumnViewerEditor viewerEditor;

	private boolean busy;
	private boolean logWhenBusy = true; // initially true, set to false

	private MouseListener mouseListener;

	private Set<ExpandableNode> expandableNodes;

	// after logging for the first
	// time

	/**
	 * Create a new instance of the receiver.
	 */
	public ColumnViewer() {
		expandableNodes = new HashSet<>();
	}

	@Override
	protected void hookControl(Control control) {
		super.hookControl(control);
		viewerEditor = createViewerEditor();
		hookEditingSupport(control);
	}

	/**
	 * Hook up the editing support. Subclasses may override.
	 *
	 * @param control
	 * 		the control you want to hook on
	 */
	protected void hookEditingSupport(Control control) {
		// Needed for backwards comp with AbstractTreeViewer
		// which is not hooked this way others may already overwrite and provide
		// their
		// own impl
		if (viewerEditor != null) {
			mouseListener = new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					// Workaround for bug 185817
					if (e.count != 2) {
						handleMouseDown(e);
					}
				}

				@Override
				public void mouseDoubleClick(MouseEvent e) {
					handleMouseDown(e);
				}
			};
			control.addMouseListener(mouseListener);
		}
	}

	/**
	 * Creates the viewer editor used for editing cell contents. To be
	 * implemented by subclasses.
	 *
	 * @return the editor, or <code>null</code> if this viewer does not support
	 * 	editing cell contents.
	 */
	protected abstract ColumnViewerEditor createViewerEditor();

	/**
	 * Returns the viewer cell at the given widget-relative coordinates, or
	 * <code>null</code> if there is no cell at that location
	 *
	 * @param point
	 * 		the widget-relative coordinates
	 * @return the cell or <code>null</code> if no cell is found at the given
	 * 	point
	 *
	 * @since 3.4
	 */
	public ViewerCell getCell(Point point) {
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
	 * 		the widget-relative coordinates of the viewer row
	 * @return ViewerRow the row or <code>null</code> if no row is found at the
	 * 	given coordinates
	 */
	protected ViewerRow getViewerRow(Point point) {
		Item item = getItemAt(point);

		if (item != null) {
			return getViewerRowFromItem(item);
		}

		return null;
	}

	/**
	 * Returns a {@link ViewerRow} associated with the given row widget.
	 * Implementations may re-use the same instance for different row widgets;
	 * callers can only use the viewer row locally and until the next call to
	 * this method.
	 *
	 * @param item
	 * 		the row widget
	 * @return ViewerRow a viewer row object
	 */
	protected abstract ViewerRow getViewerRowFromItem(Widget item);

	/**
	 * Returns the column widget at the given column index.
	 *
	 * @param columnIndex
	 * 		the column index
	 * @return Widget the column widget
	 */
	protected abstract Widget getColumnViewerOwner(int columnIndex);

	/**
	 * Returns the viewer column for the given column index.
	 *
	 * @param columnIndex
	 * 		the column index
	 * @return the viewer column at the given index, or <code>null</code> if
	 * 	there is none for the given index
	 */
	/* package */ViewerColumn getViewerColumn(final int columnIndex) {

		ViewerColumn viewer;
		Widget columnOwner = getColumnViewerOwner(columnIndex);

		if (columnOwner == null || columnOwner.isDisposed()) {
			return null;
		}

		viewer = (ViewerColumn) columnOwner
				.getData(ViewerColumn.COLUMN_VIEWER_KEY);

		if (viewer == null) {
			viewer = createViewerColumn(columnOwner, CellLabelProvider
					.createViewerLabelProvider(this, getLabelProvider()));
			setupEditingSupport(columnIndex, viewer);
		}

		if (viewer.getEditingSupport() == null && getCellModifier() != null) {
			setupEditingSupport(columnIndex, viewer);
		}

		return viewer;
	}

	/**
	 * Sets up editing support for the given column based on the "old" cell
	 * editor API.
	 */
	private void setupEditingSupport(final int columnIndex, ViewerColumn viewer) {
		if (getCellModifier() != null) {
			viewer.setEditingSupport(new EditingSupport(this) {

				@Override
				public boolean canEdit(Object element) {
					Object[] properties = getColumnProperties();

					if (columnIndex < properties.length) {
						return getCellModifier().canModify(element,
								(String) getColumnProperties()[columnIndex]);
					}

					return false;
				}

				@Override
				public CellEditor getCellEditor(Object element) {
					CellEditor[] editors = getCellEditors();
					if (columnIndex < editors.length) {
						return getCellEditors()[columnIndex];
					}
					return null;
				}

				@Override
				public Object getValue(Object element) {
					Object[] properties = getColumnProperties();

					if (columnIndex < properties.length) {
						return getCellModifier().getValue(element,
								(String) getColumnProperties()[columnIndex]);
					}

					return null;
				}

				@Override
				public void setValue(Object element, Object value) {
					Object[] properties = getColumnProperties();

					if (columnIndex < properties.length) {
						getCellModifier().modify(findItem(element),
								(String) getColumnProperties()[columnIndex],
								value);
					}
				}

				@Override
				boolean isLegacySupport() {
					return true;
				}
			});
		}
	}

	/**
	 * Creates a generic viewer column for the given column widget, based on the
	 * given label provider.
	 *
	 * @param columnOwner
	 * 		the column widget
	 * @param labelProvider
	 * 		the label provider to use for the column
	 * @return ViewerColumn the viewer column
	 */
	private ViewerColumn createViewerColumn(Widget columnOwner,
			CellLabelProvider labelProvider) {
		ViewerColumn column = new ViewerColumn(this, columnOwner) {
		};
		column.setLabelProvider(labelProvider, false);
		return column;
	}

	/**
	 * Returns the {@link Item} at the given widget-relative coordinates, or
	 * <code>null</code> if there is no item at the given coordinates.
	 *
	 * @param point
	 * 		the widget-relative coordinates
	 * @return the {@link Item} at the coordinates or <code>null</code> if there
	 * 	is no item at the given coordinates
	 */
	protected abstract Item getItemAt(Point point);

	@Override
	protected Item getItem(int x, int y) {
		return getItemAt(getControl().toControl(x, y));
	}

	/**
	 * The column viewer implementation of this <code>Viewer</code> framework
	 * method ensures that the given label provider is an instance of
	 * <code>ITableLabelProvider</code>, <code>ILabelProvider</code>, or
	 * <code>CellLabelProvider</code>.
	 * <p>
	 * If the label provider is an {@link ITableLabelProvider} , then it
	 * provides a separate label text and image for each column. Implementers of
	 * <code>ITableLabelProvider</code> may also implement {@link
	 * ITableColorProvider} and/or {@link ITableFontProvider} to provide colors
	 * and/or fonts.
	 * </p>
	 * <p>
	 * If the label provider is an <code>ILabelProvider</code> , then it
	 * provides only the label text and image for the first column, and any
	 * remaining columns are blank. Implementers of <code>ILabelProvider</code>
	 * may also implement {@link IColorProvider} and/or {@link IFontProvider} to
	 * provide colors and/or fonts.
	 * </p>
	 */
	@Override
	public void setLabelProvider(IBaseLabelProvider labelProvider) {
		Assert.isTrue(labelProvider instanceof ITableLabelProvider
				|| labelProvider instanceof ILabelProvider
				|| labelProvider instanceof CellLabelProvider);
		updateColumnParts(labelProvider);// Reset the label providers in the
		// columns
		if (labelProvider instanceof CellLabelProvider) {
			((CellLabelProvider) labelProvider).initialize(this, null);
		}
		super.setLabelProvider(labelProvider);
	}

	@Override
	void internalDisposeLabelProvider(IBaseLabelProvider oldProvider) {
		if (oldProvider instanceof CellLabelProvider) {
			((CellLabelProvider) oldProvider).dispose(this, null);
		} else {
			super.internalDisposeLabelProvider(oldProvider);
		}
	}

	/**
	 * Clear the viewer parts for the columns
	 */
	private void updateColumnParts(IBaseLabelProvider labelProvider) {
		ViewerColumn column;
		int i = 0;

		while ((column = getViewerColumn(i++)) != null) {
			column.setLabelProvider(CellLabelProvider
					.createViewerLabelProvider(this, labelProvider), false);
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
	 * @since 3.11 (public - protected since 3.3)
	 */
	public void applyEditorValue() {
		if (viewerEditor != null) {
			viewerEditor.applyEditorValue();
		}
	}

	/**
	 * Starts editing the given element at the given column index.
	 *
	 * @param element
	 * 		the model element
	 * @param column
	 * 		the column index
	 * @since 3.1 (in subclasses, added in 3.3 to abstract class)
	 */
	public void editElement(Object element, int column) {
		if (viewerEditor != null) {
			try {
				getControl().setRedraw(false);
				// Set the selection at first because in Tree's
				// the element might not be materialized
				setSelection(new StructuredSelection(element), true);

				Widget item = findItem(element);
				if (item != null) {
					ViewerRow row = getViewerRowFromItem(item);
					if (row != null) {
						ViewerCell cell = row.getCell(column);
						if (cell != null) {
							triggerEditorActivationEvent(new ColumnViewerEditorActivationEvent(
									cell));
						}
					}
				}
			} finally {
				getControl().setRedraw(true);
			}
		}
	}

	/**
	 * Return the CellEditors for the receiver, or <code>null</code> if no cell
	 * editors are set.
	 * <p>
	 * Since 3.3, an alternative API is available, see {@link
	 * ViewerColumn#setEditingSupport(EditingSupport)} for a more flexible way
	 * of editing values in a column viewer.
	 * </p>
	 *
	 *
	 * @return CellEditor[]
	 * @since 3.1 (in subclasses, added in 3.3 to abstract class)
	 * @see ViewerColumn#setEditingSupport(EditingSupport)
	 * @see EditingSupport
	 */
	public CellEditor[] getCellEditors() {
		return cellEditors;
	}

	/**
	 * Returns the cell modifier of this viewer, or <code>null</code> if none
	 * has been set.
	 *
	 * <p>
	 * Since 3.3, an alternative API is available, see {@link
	 * ViewerColumn#setEditingSupport(EditingSupport)} for a more flexible way
	 * of editing values in a column viewer.
	 * </p>
	 *
	 * @return the cell modifier, or <code>null</code>
	 * @since 3.1 (in subclasses, added in 3.3 to abstract class)
	 * @see ViewerColumn#setEditingSupport(EditingSupport)
	 * @see EditingSupport
	 */
	public ICellModifier getCellModifier() {
		return cellModifier;
	}

	/**
	 * Returns the column properties of this table viewer. The properties must
	 * correspond with the columns of the table control. They are used to
	 * identify the column in a cell modifier.
	 *
	 * <p>
	 * Since 3.3, an alternative API is available, see {@link
	 * ViewerColumn#setEditingSupport(EditingSupport)} for a more flexible way
	 * of editing values in a column viewer.
	 * </p>
	 *
	 * @return the list of column properties
	 * @since 3.1 (in subclasses, added in 3.3 to abstract class)
	 * @see ViewerColumn#setEditingSupport(EditingSupport)
	 * @see EditingSupport
	 */
	public Object[] getColumnProperties() {
		return columnProperties;
	}

	/**
	 * Returns whether there is an active cell editor.
	 *
	 * <p>
	 * Since 3.3, an alternative API is available, see {@link
	 * ViewerColumn#setEditingSupport(EditingSupport)} for a more flexible way
	 * of editing values in a column viewer.
	 * </p>
	 *
	 * @return <code>true</code> if there is an active cell editor, and
	 * 	<code>false</code> otherwise
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

	@Override
	public void refresh(Object element) {
		if (checkBusy())
			return;

		if (isCellEditorActive()) {
			cancelEditing();
		}

		super.refresh(element);
	}

	@Override
	public void refresh(Object element, boolean updateLabels) {
		if (checkBusy())
			return;

		if (isCellEditorActive()) {
			cancelEditing();
		}

		super.refresh(element, updateLabels);
	}

	@Override
	public void update(Object element, String[] properties) {
		if (checkBusy())
			return;
		super.update(element, properties);
	}

	/**
	 * Sets the cell editors of this column viewer. If editing is not supported
	 * by this viewer the call simply has no effect.
	 *
	 * <p>
	 * Since 3.3, an alternative API is available, see {@link
	 * ViewerColumn#setEditingSupport(EditingSupport)} for a more flexible way
	 * of editing values in a column viewer.
	 * </p>
	 * <p>
	 * Users setting up an editable {@link TreeViewer} or {@link TableViewer} with more than 1 column <b>have</b>
	 * to pass the SWT.FULL_SELECTION style bit
	 * </p>
	 * @param editors
	 * 		the list of cell editors
	 * @since 3.1 (in subclasses, added in 3.3 to abstract class)
	 * @see ViewerColumn#setEditingSupport(EditingSupport)
	 * @see EditingSupport
	 */
	public void setCellEditors(CellEditor[] editors) {
		this.cellEditors = editors;
	}

	/**
	 * Sets the cell modifier for this column viewer. This method does nothing
	 * if editing is not supported by this viewer.
	 *
	 * <p>
	 * Since 3.3, an alternative API is available, see {@link
	 * ViewerColumn#setEditingSupport(EditingSupport)} for a more flexible way
	 * of editing values in a column viewer.
	 * </p>
	 * <p>
	 * Users setting up an editable {@link TreeViewer} or {@link TableViewer} with more than 1 column <b>have</b>
	 * to pass the SWT.FULL_SELECTION style bit
	 * </p>
	 * @param modifier
	 * 		the cell modifier
	 * @since 3.1 (in subclasses, added in 3.3 to abstract class)
	 * @see ViewerColumn#setEditingSupport(EditingSupport)
	 * @see EditingSupport
	 */
	public void setCellModifier(ICellModifier modifier) {
		this.cellModifier = modifier;
	}

	/**
	 * Sets the column properties of this column viewer. The properties must
	 * correspond with the columns of the control. They are used to identify the
	 * column in a cell modifier. If editing is not supported by this viewer the
	 * call simply has no effect.
	 *
	 * <p>
	 * Since 3.3, an alternative API is available, see {@link
	 * ViewerColumn#setEditingSupport(EditingSupport)} for a more flexible way
	 * of editing values in a column viewer.
	 * </p>
	 * <p>
	 * Users setting up an editable {@link TreeViewer} or {@link TableViewer} with more than 1 column <b>have</b>
	 * to pass the SWT.FULL_SELECTION style bit
	 * </p>
	 * @param columnProperties
	 * 		the list of column properties
	 * @since 3.1 (in subclasses, added in 3.3 to abstract class)
	 * @see ViewerColumn#setEditingSupport(EditingSupport)
	 * @see EditingSupport
	 */
	public void setColumnProperties(String[] columnProperties) {
		this.columnProperties = columnProperties;
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

	/**
	 * Returns the label provider associated with the column at the given index
	 * or <code>null</code> if no column with this index is known.
	 *
	 * @param columnIndex
	 * 		the column index
	 * @return the label provider associated with the column or
	 * 	<code>null</code> if no column with this index is known
	 *
	 * @since 3.3
	 */
	public CellLabelProvider getLabelProvider(int columnIndex) {
		ViewerColumn column = getViewerColumn(columnIndex);
		if (column != null) {
			return column.getLabelProvider();
		}
		return null;
	}

	private void handleMouseDown(MouseEvent e) {
		ViewerCell cell = getCell(new Point(e.x, e.y));

		if (cell != null) {
			if (cell.getElement() instanceof ExpandableNode) {
				handleExpandableNodeClicked(cell.getItem());
			} else {
				triggerEditorActivationEvent(new ColumnViewerEditorActivationEvent(cell, e));
			}
		}
	}

	@Override
	protected void handleDispose(DisposeEvent event) {
		if (mouseListener != null && event.widget instanceof Control) {
			((Control)event.widget).removeMouseListener(mouseListener);
			mouseListener = null;
		}
		super.handleDispose(event);
	}

	/**
	 * Invoking this method fires an editor activation event which tries to
	 * enable the editor but before this event is passed to {@link
	 * ColumnViewerEditorActivationStrategy} to see if this event should really
	 * trigger editor activation
	 *
	 * @param event
	 * 		the activation event
	 */
	protected void triggerEditorActivationEvent(
			ColumnViewerEditorActivationEvent event) {
		viewerEditor.handleEditorActivationEvent(event);
	}

	/**
	 * @param columnViewerEditor
	 * 		the new column viewer editor
	 */
	public void setColumnViewerEditor(ColumnViewerEditor columnViewerEditor) {
		Assert.isNotNull(columnViewerEditor);
		this.viewerEditor = columnViewerEditor;
	}

	/**
	 * @return the currently attached viewer editor
	 */
	public ColumnViewerEditor getColumnViewerEditor() {
		return viewerEditor;
	}

	@Override
	protected Object[] getRawChildren(Object parent) {
		boolean oldBusy = isBusy();
		setBusy(true);
		try {
			return super.getRawChildren(parent);
		} finally {
			setBusy(oldBusy);
		}
	}

	void clearLegacyEditingSetup() {
		if (!getControl().isDisposed() && getCellEditors() != null) {
			int count = doGetColumnCount();

			for (int i = 0; i < count || i == 0; i++) {
				Widget owner = getColumnViewerOwner(i);
				if (owner != null && !owner.isDisposed()) {
					ViewerColumn column = (ViewerColumn) owner
							.getData(ViewerColumn.COLUMN_VIEWER_KEY);
					if (column != null) {
						EditingSupport e = column.getEditingSupport();
						// Ensure that only EditingSupports are wiped that are
						// setup
						// for Legacy reasons
						if (e != null && e.isLegacySupport()) {
							column.setEditingSupport(null);
						}
					}
				}
			}
		}
	}

	/**
	 * Checks if this viewer is currently busy, logging a warning and returning
	 * <code>true</code> if it is busy. A column viewer is busy when it is
	 * processing a refresh, add, remove, insert, replace, setItemCount,
	 * expandToLevel, update, setExpandedElements, or similar method that may
	 * make calls to client code. Column viewers are not designed to handle
	 * reentrant calls while they are busy. The method returns <code>true</code>
	 * if the viewer is busy. It is recommended that this method be used by
	 * subclasses to determine whether the viewer is busy to return early from
	 * state-changing methods.
	 *
	 * <p>
	 * This method is not intended to be overridden by subclasses.
	 * </p>
	 *
	 * @return <code>true</code> if the viewer is busy.
	 *
	 * @since 3.4
	 */
	protected boolean checkBusy() {
		if (isBusy()) {
			if (logWhenBusy) {
				String message = "Ignored reentrant call while viewer is busy."; //$NON-NLS-1$
				if (!InternalPolicy.DEBUG_LOG_REENTRANT_VIEWER_CALLS) {
					// stop logging after the first
					logWhenBusy = false;
					message += " This is only logged once per viewer instance," + //$NON-NLS-1$
							" but similar calls will still be ignored."; //$NON-NLS-1$
				}
				Policy.getLog().log(Status.warning(message));
			}
			return true;
		}
		return false;
	}

	/**
	 * Sets the busy state of this viewer. Subclasses MUST use <code>try</code>
	 * ...<code>finally</code> as follows to ensure that the busy flag is reset
	 * to its original value:
	 *
	 * <pre>
	 * boolean oldBusy = isBusy();
	 * setBusy(true);
	 * try {
	 * 	// do work
	 * } finally {
	 * 	setBusy(oldBusy);
	 * }
	 * </pre>
	 *
	 * <p>
	 * This method is not intended to be overridden by subclasses.
	 * </p>
	 *
	 * @param busy
	 * 		the new value of the busy flag
	 *
	 * @since 3.4
	 */
	protected void setBusy(boolean busy) {
		this.busy = busy;
	}

	/**
	 * Returns <code>true</code> if this viewer is currently busy processing a
	 * refresh, add, remove, insert, replace, setItemCount, expandToLevel,
	 * update, setExpandedElements, or similar method that may make calls to
	 * client code. Column viewers are not designed to handle reentrant calls
	 * while they are busy. It is recommended that clients avoid using this
	 * method if they can ensure by other means that they will not make
	 * reentrant calls to methods like the ones listed above. See bug 184991 for
	 * background discussion.
	 *
	 * <p>
	 * This method is not intended to be overridden by subclasses.
	 * </p>
	 *
	 * @return Returns whether this viewer is busy.
	 *
	 * @since 3.4
	 */
	public boolean isBusy() {
		return busy;
	}

	@Override
	protected Object[] getSortedChildren(Object parent) {
		Object[] sorted = super.getSortedChildren(parent);
		return applyItemsLimit(parent, sorted);
	}

	/**
	 * Apply items limit for the items to be created on the viewer. This method is
	 * supposed to be called at the end of {@link #getSortedChildren(Object)} call,
	 * which always returns the complete list of elements to be populated.
	 * <p>
	 * <ul>
	 * <li>If the {@link #setDisplayIncrementally(int)} is not used by viewer, this method
	 * does nothing.</li>
	 * <li>If the {@link #setDisplayIncrementally(int)} is used by viewer, this method might
	 * modify the list by reducing number of elements according to the limit set,
	 * and by adding an {@link ExpandableNode} as last element.</li>
	 * <li>If the {@link #getSortedChildren(Object)} is not overridden by the
	 * subclass, this method shouldn't be used by clients.</li>
	 * </ul>
	 * Note that in case of parent is {@link ExpandableNode} we will return next
	 * block of limited elements to be created.
	 *
	 * @param parent parent element
	 * @param sorted all children of given parent, as returned by
	 *               {@link #getSortedChildren(Object)}
	 * @return returns only limited items.
	 * @see ColumnViewer#setDisplayIncrementally(int)
	 */
	final Object[] applyItemsLimit(Object parent, Object[] sorted) {
		// limit the number of items to be created. sorted always gets the remaining
		// elements to be created.
		final int itemsLimit = getItemsLimit();
		if (itemsLimit <= 0 || sorted.length <= itemsLimit || sorted.length == itemsLimit + 1) {
			return sorted;
		}

		int offSet = itemsLimit;
		int srcPos = 0;

		Object[] partialChildren = new Object[itemsLimit + 1];

		// Extract a subset of children
		System.arraycopy(sorted, srcPos, partialChildren, 0, itemsLimit);

		if (parent instanceof ExpandableNode expNode) {
			// pass on original children
			sorted = expNode.getAllElements();
			srcPos = expNode.getOffset();
			offSet = srcPos + itemsLimit;
		}

		// Add an expandable node
		partialChildren[itemsLimit] = createExpandableNode(sorted, offSet, itemsLimit);

		return partialChildren;
	}

	/**
	 * Concrete viewer is supposed to "expand" {@link ExpandableNode} located at
	 * given cell.
	 * <p>
	 * Default implementation does nothing.
	 *
	 * @param cell selected on click
	 */
	void handleExpandableNodeClicked(Widget cell) {
		// default implementation does nothing. Actual viewers can decide how to
		// populate remaining elements.
	}

	/**
	 * If the items limit is not set, or the number of visible children is below the
	 * items limit, this method does nothing and returns {@code null}.
	 * <p>
	 * In other case, this method tries to fetch (probably updated) model elements
	 * up to the number of given visible elements, expanding possible existing
	 * {@link ExpandableNode} elements if needed.
	 * <p>
	 * Implementation note: we should not dispose already visible items and we
	 * should display limited elements along with already visible items.
	 *
	 * @param parent          model item to be refreshed.
	 * @param visibleChildren currently visible children. This includes any elements
	 *                        already expanded by user.
	 * @return list of children to be displayed/refreshed or {@code null} if given
	 *         items length does not exceed items limit
	 */
	Object[] getChildrenWithLimitApplied(final Object parent, Item[] visibleChildren) {
		final int limit = getItemsLimit();
		final int visibleItemsLength = visibleChildren.length;
		if (visibleItemsLength < limit || limit <= 0) {
			return null;
		}

		// fetch entire sorted children we need them in any of next cases.
		setDisplayIncrementally(0);
		Object[] sortedAll;
		try {
			sortedAll = getSortedChildren(parent);
		} finally {
			setDisplayIncrementally(limit);
		}

		// model has lost some elements and length is less then visible items.
		if (sortedAll.length < visibleItemsLength) {
			return sortedAll;
		}

		// all elements from the model were visible. Probably refresh triggered to update
		// labels.
		if (sortedAll.length == visibleItemsLength
				&& !(visibleChildren[visibleItemsLength - 1].getData() instanceof ExpandableNode)) {
			return sortedAll;
		}

		// there can any number of elements in the model. but viewer was showing
		// ExpandableNode. Then return the same length.
		if (visibleChildren[visibleItemsLength - 1].getData() instanceof ExpandableNode) {
			if (sortedAll.length == visibleItemsLength) {
				// model returns now exact the visible number of elements (note, last visible is
				// expandable node): just return all without expandable node
				return sortedAll;
			}

			// Now we need exactly previously visible length.
			Object[] subArray = new Object[visibleItemsLength];
			System.arraycopy(sortedAll, 0, subArray, 0, visibleItemsLength - 1);
			subArray[visibleItemsLength - 1] = new ExpandableNode(sortedAll, visibleItemsLength - 1, limit, this);
			return subArray;
		}

		// probably model has updated with huge data and requesting refresh. we should
		// not let the viewer explode.
		// How many next elements can be populated ?
		// We will populate only visible items + limit.
		int max = visibleItemsLength + limit;

		if (sortedAll.length < max) {
			return sortedAll;
		}

		// create 'max' elements.
		Object[] subArray = new Object[max];
		System.arraycopy(sortedAll, 0, subArray, 0, max - 1);
		subArray[max - 1] = new ExpandableNode(sortedAll, max - 1, limit, this);
		return subArray;
	}

	/**
	 * Returns the current viewer limit on direct children at one level. Limit that
	 * is less than or equal to zero has no effect on the viewer.
	 *
	 * @return current limit
	 */
	int getItemsLimit() {
		return itemsLimit;
	}

	/**
	 * Sets the viewers items limit on direct children at one level.
	 * <p>
	 * If the number of direct children will exceed this limit, the viewer will only
	 * show a subset of children up to the limit and add an {@link ExpandableNode}
	 * element after last shown item.
	 * </p>
	 * <p>
	 * This method must be called before {@link #setInput(Object)}. A parameter less
	 * than or equal to zero has no effect on the viewer.
	 * </p>
	 * <p>
	 * This API does not guaranteed to work with {@link SWT#VIRTUAL} viewers.
	 * </p>
	 *
	 * @param incrementSize A non-negative integer greater than 0 to enable items
	 *                      limit
	 * @since 3.31
	 */
	public void setDisplayIncrementally(int incrementSize) {
		itemsLimit = incrementSize;
	}

	ExpandableNode createExpandableNode(Object[] result, int startOffSet, int limit) {
		ExpandableNode expandableNode = new ExpandableNode(result, startOffSet, limit, this);
		expandableNodes.add(expandableNode);
		return expandableNode;
	}

	@Override
	protected void disassociate(Item item) {
		Object element = item.getData();
		if (element instanceof ExpandableNode expNode) {
			expandableNodes.remove(expNode);
		}
		super.disassociate(item);
	}

	Set<ExpandableNode> getExpandableNodes() {
		return expandableNodes;
	}

	@Override
	protected void handleDoubleSelect(SelectionEvent event) {
		if (event.item != null && event.item.getData() instanceof ExpandableNode) {
			handleExpandableNodeClicked(event.item);
			// we do not want client listeners to be notified for this item.
			return;
		}
		super.handleDoubleSelect(event);
	}

	/**
	 * Return if it is an instance of ExpandableNode
	 *
	 * @param element model object representing a special "expandable" node
	 * @return return if it is an instance of ExpandableNode
	 * @since 3.31
	 */
	public final boolean isExpandableNode(Object element) {
		return element instanceof ExpandableNode;

	}

	@Override
	protected void updateSelection(ISelection selection) {
		super.updateSelection(getUpdatedSelection(selection));
	}

	@Override
	protected void firePostSelectionChanged(SelectionChangedEvent event) {
		ISelection givenSel = event.getSelection();
		ISelection updatedSel = getUpdatedSelection(givenSel);
		if (givenSel != updatedSel) {
			event = new SelectionChangedEvent(event.getSelectionProvider(), updatedSel);
		}
		super.firePostSelectionChanged(event);
	}

	@SuppressWarnings("unchecked")
	ISelection getUpdatedSelection(ISelection selection) {
		if (getItemsLimit() <= 0) {
			return selection;
		}

		if (selection instanceof StructuredSelection structSel) {
			List<Object> list = new ArrayList<>(structSel.toList());
			Iterator<?> itr = list.iterator();
			boolean found = false;
			while (itr.hasNext()) {
				if (itr.next() instanceof ExpandableNode) {
					itr.remove();
					found = true;
				}
			}
			if (found) {
				return new StructuredSelection(list);
			}
		}
		// by default return given selection.
		return selection;
	}

	@Override
	protected void unmapAllElements() {
		expandableNodes.clear();
		super.unmapAllElements();
	}
}
