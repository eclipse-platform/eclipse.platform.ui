/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - refactoring (bug 153993)
 *     											   fix in bug 151295
 *                                                 fix in bug 166500
 *******************************************************************************/

package org.eclipse.jface.viewers;


import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;

/**
 * This is the base for all editor implementations of Viewers. ColumnViewer
 * implementators have to subclass this viewer and implement the missing methods
 * 
 * @since 3.3 <strong>EXPERIMENTAL</strong> This class or interface has been
 *        added as part of a work in progress. This API may change at any given
 *        time. Please do not use this API without consulting with the
 *        Platform/UI team.
 * 
 */
public abstract class ColumnViewerEditor {
	private CellEditor cellEditor;

	private ICellEditorListener cellEditorListener;

	private FocusListener focusListener;

	private MouseListener mouseListener;

	private ColumnViewer viewer;

	private TraverseListener tabeditingListener;

	private int activationTime;

	private ViewerCell cell;

	private ColumnViewerEditorActivationEvent activationEvent;

	private ListenerList editorActivationListener;

	private ColumnViewerEditorActivationStrategy editorActivationStrategy;

	/**
	 * Tabing from cell to cell is turned off
	 */
	public static final int DEFAULT = 1;

	/**
	 * Should if the end of the row is reach started from the start/end of the
	 * row below/above
	 */
	public static final int TABBING_MOVE_TO_ROW_NEIGHBOR = 1 << 1;

	/**
	 * Should if the end of the row is reach started from the beginning in the
	 * same row
	 */
	public static final int TABBING_CYCLE_IN_ROW = 1 << 2;

	/**
	 * Support tabing to Cell above/below the current cell
	 */
	public static final int TABBING_VERTICAL = 1 << 3;

	/**
	 * Should tabing from column to column with in one row be supported
	 */
	public static final int TABBING_HORIZONTAL = 1 << 4;

	/**
	 * 
	 */
	public static final int KEYBOARD_ACTIVATION = 1 << 5;
	
	private int feature;

	/**
	 * @param viewer
	 * @param editorActivationStrategy
	 * @param feature
	 */
	protected ColumnViewerEditor(ColumnViewer viewer,
			ColumnViewerEditorActivationStrategy editorActivationStrategy, int feature) {
		this.viewer = viewer;
		this.editorActivationStrategy = editorActivationStrategy;
		if( (feature & KEYBOARD_ACTIVATION) == KEYBOARD_ACTIVATION ) {
			this.editorActivationStrategy.setEnableEditorActivationWithKeyboard(true);
		} 
		this.feature = feature;
		initCellEditorListener();
	}

	private void initCellEditorListener() {
		cellEditorListener = new ICellEditorListener() {
			public void editorValueChanged(boolean oldValidState,
					boolean newValidState) {
				// Ignore.
			}

			public void cancelEditor() {
				ColumnViewerEditor.this.cancelEditing();
			}

			public void applyEditorValue() {
				ColumnViewerEditor.this.applyEditorValue();
			}
		};
	}

	void activateCellEditor() {

		ViewerColumn part = viewer.getViewerColumn(cell.getColumnIndex());
		Object element = cell.getElement();

		if (part != null && part.getEditingSupport() != null
				&& part.getEditingSupport().canEdit(element)) {

			cellEditor = part.getEditingSupport().getCellEditor(element);
			if (cellEditor != null) {
				if (editorActivationListener != null
						&& !editorActivationListener.isEmpty()) {
					Object[] ls = editorActivationListener.getListeners();
					for (int i = 0; i < ls.length; i++) {

						if (activationEvent.cancel) {
							return;
						}

						((ColumnViewerEditorActivationListener) ls[i])
								.beforeEditorActivated(activationEvent);
					}
				}
				
				// Update the focus cell when we activated the editor with these 2 events
				if( activationEvent.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC || activationEvent.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL ) {
					updateFocusCell(cell);
				}
				
				
				cellEditor.addListener(cellEditorListener);
				part.getEditingSupport().initializeCellEditorValue(cellEditor, cell);
				
				// Tricky flow of control here:
				// activate() can trigger callback to cellEditorListener which
				// will clear cellEditor
				// so must get control first, but must still call activate()
				// even if there is no control.
				final Control control = cellEditor.getControl();
				cellEditor.activate(activationEvent);
				if (control == null) {
					return;
				}
				setLayoutData(cellEditor.getLayoutData());
				setEditor(control, (Item)cell.getItem(), cell.getColumnIndex());
				cellEditor.setFocus();
				if (focusListener == null) {
					focusListener = new FocusAdapter() {
						public void focusLost(FocusEvent e) {
							applyEditorValue();
						}
					};
				}
				control.addFocusListener(focusListener);

				mouseListener = new MouseAdapter() {
					public void mouseDown(MouseEvent e) {
						// time wrap?
						// check for expiration of doubleClickTime
						if (e.time <= activationTime) {
							control.removeMouseListener(mouseListener);
							cancelEditing();
							handleDoubleClickEvent();
						} else if (mouseListener != null) {
							control.removeMouseListener(mouseListener);
						}
					}
				};
				control.addMouseListener(mouseListener);

				if (tabeditingListener == null) {
					tabeditingListener = new TraverseListener() {

						public void keyTraversed(TraverseEvent e) {
							if ((feature & DEFAULT) != DEFAULT) {
								processTraverseEvent(cell.getColumnIndex(),
										viewer.getViewerRowFromItem(cell
												.getItem()), e);
							}
						}
					};
				}

				control.addTraverseListener(tabeditingListener);

				if (editorActivationListener != null
						&& !editorActivationListener.isEmpty()) {
					Object[] ls = editorActivationListener.getListeners();
					for (int i = 0; i < ls.length; i++) {
						((ColumnViewerEditorActivationListener) ls[i])
								.afterEditorActivated(activationEvent);
					}
				}
			}
		}
	}

	/**
	 * Applies the current value and deactivates the currently active cell
	 * editor.
	 */
	void applyEditorValue() {
		CellEditor c = this.cellEditor;
		if (c != null) {
			// null out cell editor before calling save
			// in case save results in applyEditorValue being re-entered
			// see 1GAHI8Z: ITPUI:ALL - How to code event notification when
			// using cell editor ?
			ColumnViewerEditorDeactivationEvent tmp = new ColumnViewerEditorDeactivationEvent(cell);
			if (editorActivationListener != null
					&& !editorActivationListener.isEmpty()) {
				Object[] ls = editorActivationListener.getListeners();
				for (int i = 0; i < ls.length; i++) {

					((ColumnViewerEditorActivationListener) ls[i])
							.beforeEditorDeactivated(tmp);
				}
			}

			this.cellEditor = null;
			this.activationEvent = null;
			Item t = (Item) this.cell.getItem();
			// don't null out table item -- same item is still selected
			if (t != null && !t.isDisposed()) {
				saveEditorValue(c);
			}
			setEditor(null, null, 0);
			c.removeListener(cellEditorListener);
			Control control = c.getControl();
			if (control != null) {
				if (mouseListener != null) {
					control.removeMouseListener(mouseListener);
					// Clear the instance not needed any more
					mouseListener = null;
				}
				if (focusListener != null) {
					control.removeFocusListener(focusListener);
				}

				if (tabeditingListener != null) {
					control.removeTraverseListener(tabeditingListener);
				}
			}
			c.deactivate();

			if (editorActivationListener != null
					&& !editorActivationListener.isEmpty()) {
				Object[] ls = editorActivationListener.getListeners();
				for (int i = 0; i < ls.length; i++) {
					((ColumnViewerEditorActivationListener) ls[i])
							.afterEditorDeactivated(tmp);
				}
			}
		}
	}

	/**
	 * Cancle editing
	 */
	void cancelEditing() {
		if (cellEditor != null) {
			ColumnViewerEditorDeactivationEvent tmp = new ColumnViewerEditorDeactivationEvent(cell);
			if (editorActivationListener != null
					&& !editorActivationListener.isEmpty()) {
				Object[] ls = editorActivationListener.getListeners();
				for (int i = 0; i < ls.length; i++) {
					
					((ColumnViewerEditorActivationListener) ls[i])
							.beforeEditorDeactivated(tmp);
				}
			}

			setEditor(null, null, 0);
			cellEditor.removeListener(cellEditorListener);

			Control control = cellEditor.getControl();
			if (control != null) {
				if (mouseListener != null) {
					control.removeMouseListener(mouseListener);
					// Clear the instance not needed any more
					mouseListener = null;
				}
				if (focusListener != null) {
					control.removeFocusListener(focusListener);
				}

				if (tabeditingListener != null) {
					control.removeTraverseListener(tabeditingListener);
				}
			}

			CellEditor oldEditor = cellEditor;
			cellEditor = null;
			activationEvent = null;
			oldEditor.deactivate();

			if (editorActivationListener != null
					&& !editorActivationListener.isEmpty()) {
				Object[] ls = editorActivationListener.getListeners();
				for (int i = 0; i < ls.length; i++) {
					((ColumnViewerEditorActivationListener) ls[i])
							.afterEditorDeactivated(tmp);
				}
			}
		}
	}

	/**
	 * Enable the editor by mouse down
	 * 
	 * @param event
	 */
	void handleEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
		if (editorActivationStrategy.isEditorActivationEvent(event)) {
			if (cellEditor != null) {
				applyEditorValue();
			}

			this.cell = (ViewerCell) event.getSource();
			
			activationEvent = event;
			activationTime = event.time
					+ Display.getCurrent().getDoubleClickTime();

			activateCellEditor();
		}
	}

	private void saveEditorValue(CellEditor cellEditor) {
		ViewerColumn part = viewer.getViewerColumn(cell.getColumnIndex());

		if (part != null && part.getEditingSupport() != null) {
			part.getEditingSupport().saveCellEditorValue(cellEditor, cell);
		}
	}

	/**
	 * Return whether there is an active cell editor.
	 * 
	 * @return <code>true</code> if there is an active cell editor; otherwise
	 *         <code>false</code> is returned.
	 */
	boolean isCellEditorActive() {
		return cellEditor != null;
	}

	void handleDoubleClickEvent() {
		viewer.fireDoubleClick(new DoubleClickEvent(viewer, viewer
				.getSelection()));
		viewer.fireOpen(new OpenEvent(viewer, viewer.getSelection()));
	}

	void addEditorActivationListener(
			ColumnViewerEditorActivationListener listener) {
		if (editorActivationListener == null) {
			editorActivationListener = new ListenerList();
		}
		editorActivationListener.add(listener);
	}

	void removeEditorActivationListener(
			ColumnViewerEditorActivationListener listener) {
		if (editorActivationListener != null) {
			editorActivationListener.remove(listener);
		}
	}

	/**
	 * Process the travers event and opens the next available editor depending
	 * of the implemented strategy. The default implementation uses the style
	 * constants
	 * <ul>
	 * <li>{@link ColumnViewerEditor#TABBING_MOVE_TO_ROW_NEIGHBOR}</li>
	 * <li>{@link ColumnViewerEditor#TABBING_CYCLE_IN_ROW}</li>
	 * <li>{@link ColumnViewerEditor#TABBING_VERTICAL}</li>
	 * <li>{@link ColumnViewerEditor#TABBING_HORIZONTAL}</li>
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
	 *            the current row - may only be used for the duration of this method call
	 * @param event
	 *            the travers event
	 */
	protected void processTraverseEvent(int columnIndex, ViewerRow row,
			TraverseEvent event) {

		ViewerCell cell2edit = null;

		if (event.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
			event.doit = false;

			if ((event.stateMask & SWT.CTRL) == SWT.CTRL
					&& (feature & TABBING_VERTICAL) == TABBING_VERTICAL) {
				cell2edit = searchCellAboveBelow(row, viewer, columnIndex, true);
			} else if ((feature & TABBING_HORIZONTAL) == TABBING_HORIZONTAL) {
				cell2edit = searchPreviousCell(row, viewer, columnIndex,
						columnIndex);
			}
		} else if (event.detail == SWT.TRAVERSE_TAB_NEXT) {
			event.doit = false;

			if ((event.stateMask & SWT.CTRL) == SWT.CTRL
					&& (feature & TABBING_VERTICAL) == TABBING_VERTICAL) {
				cell2edit = searchCellAboveBelow(row, viewer, columnIndex,
						false);
			} else if ((feature & TABBING_HORIZONTAL) == TABBING_HORIZONTAL) {
				cell2edit = searchNextCell(row, viewer, columnIndex,
						columnIndex);
			}
		}

		if (cell2edit != null) {

			viewer.getControl().setRedraw(false);
			ColumnViewerEditorActivationEvent acEvent = new ColumnViewerEditorActivationEvent(
					cell2edit, event);
			viewer.triggerEditorActivationEvent(acEvent);
			viewer.getControl().setRedraw(true);
		}
	}

	private ViewerCell searchCellAboveBelow(ViewerRow row, ColumnViewer viewer,
			int columnIndex, boolean above) {
		ViewerCell rv = null;

		ViewerRow newRow = null;

		if (above) {
			newRow = row.getNeighbor(ViewerRow.ABOVE, false);
		} else {
			newRow = row.getNeighbor(ViewerRow.BELOW, false);
		}

		if (newRow != null) {
			ViewerColumn column = viewer.getViewerColumn(columnIndex);
			if (column != null && column.getEditingSupport() != null
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
			if (column != null && column.getEditingSupport() != null
					&& column.getEditingSupport().canEdit(
							row.getItem().getData())) {
				rv = row.getCell(columnIndex - 1);
			} else {
				rv = searchPreviousCell(row, viewer, columnIndex - 1,
						startIndex);
			}
		} else {
			if ((feature & TABBING_CYCLE_IN_ROW) == TABBING_CYCLE_IN_ROW) {
				// Check that we don't get into endless loop
				if (columnIndex - 1 != startIndex) {
					// Don't subtract -1 from getColumnCount() we need to
					// start in the virtual column
					// next to it
					rv = searchPreviousCell(row, viewer, row.getColumnCount(),
							startIndex);
				}
			} else if ((feature & TABBING_MOVE_TO_ROW_NEIGHBOR) == TABBING_MOVE_TO_ROW_NEIGHBOR) {
				ViewerRow rowAbove = row.getNeighbor(ViewerRow.ABOVE, false);
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
			if (column != null && column.getEditingSupport() != null
					&& column.getEditingSupport().canEdit(
							row.getItem().getData())) {
				rv = row.getCell(columnIndex + 1);
			} else {
				rv = searchNextCell(row, viewer, columnIndex + 1, startIndex);
			}
		} else {
			if ((feature & TABBING_CYCLE_IN_ROW) == TABBING_CYCLE_IN_ROW) {
				// Check that we don't get into endless loop
				if (columnIndex + 1 != startIndex) {
					// Start from -1 from the virtual column before the
					// first one
					rv = searchNextCell(row, viewer, -1, startIndex);
				}
			} else if ((feature & TABBING_MOVE_TO_ROW_NEIGHBOR) == TABBING_MOVE_TO_ROW_NEIGHBOR) {
				ViewerRow rowBelow = row.getNeighbor(ViewerRow.BELOW, false);
				if (rowBelow != null) {
					rv = searchNextCell(rowBelow, viewer, -1, startIndex);
				}
			}
		}

		return rv;
	}

	/**
	 * Position the editor inside the control
	 * 
	 * @param w
	 *            the editor control
	 * @param item
	 *            the item (row) in which the editor is drawn in
	 * @param fColumnNumber
	 *            the column number in which the editor is shown
	 */
	protected abstract void setEditor(Control w, Item item, int fColumnNumber);

	/**
	 * set the layout data for the editor
	 * 
	 * @param layoutData
	 *            the layout data used when editor is displayed
	 */
	protected abstract void setLayoutData(CellEditor.LayoutData layoutData);

	/**
	 * @param focusCell updates the cell with the current input focus
	 */
	protected abstract void updateFocusCell(ViewerCell focusCell);
	
	/**
	 * @return the cell currently holding the focus
	 * 
	 */
	public ViewerCell getFocusCell() {
		return null;
	}

	/**
	 * @return the viewer working for
	 */
	protected ColumnViewer getViewer() {
		return viewer;
	}
}