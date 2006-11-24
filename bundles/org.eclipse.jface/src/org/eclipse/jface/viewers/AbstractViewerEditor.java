/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - refactoring (bug 153993)
 ******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Rectangle;
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
public abstract class AbstractViewerEditor {
	private CellEditor cellEditor;

	private CellEditor[] cellEditors;

	private ICellModifier cellModifier;

	private String[] columnProperties;

	private int columnNumber;

	private ICellEditorListener cellEditorListener;

	private FocusListener focusListener;

	private MouseListener mouseListener;

	private int doubleClickExpirationTime;

	private ColumnViewer viewer;

	private Item item;

	/**
	 * Create a new editor implementation for the viewer
	 * 
	 * @param viewer
	 *            the column viewer
	 */
	public AbstractViewerEditor(ColumnViewer viewer) {
		this.viewer = viewer;
		initCellEditorListener();
	}

	private void initCellEditorListener() {
		cellEditorListener = new ICellEditorListener() {
			public void editorValueChanged(boolean oldValidState,
					boolean newValidState) {
				// Ignore.
			}

			public void cancelEditor() {
				AbstractViewerEditor.this.cancelEditing();
			}

			public void applyEditorValue() {
				AbstractViewerEditor.this.applyEditorValue();
			}
		};
	}

	void activateCellEditor() {

		ViewerColumn part = viewer.getViewerColumn(columnNumber);
		Object element = item.getData();

		if (part != null && part.getEditingSupport() != null
				&& part.getEditingSupport().canEdit(element)) {
			cellEditor = part.getEditingSupport().getCellEditor(element);
			if (cellEditor != null) {
				cellEditor.addListener(cellEditorListener);
				Object value = part.getEditingSupport().getValue(element);
				cellEditor.setValue(value);
				// Tricky flow of control here:
				// activate() can trigger callback to cellEditorListener which
				// will clear cellEditor
				// so must get control first, but must still call activate()
				// even if there is no control.
				final Control control = cellEditor.getControl();
				cellEditor.activate();
				if (control == null) {
					return;
				}
				setLayoutData(cellEditor.getLayoutData());
				setEditor(control, item, columnNumber);
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
						if (e.time <= doubleClickExpirationTime) {
							control.removeMouseListener(mouseListener);
							cancelEditing();
							handleDoubleClickEvent();
						} else if (mouseListener != null) {
							control.removeMouseListener(mouseListener);
						}
					}
				};
				control.addMouseListener(mouseListener);
			}
		}
	}

	/**
	 * Activate a cell editor for the given mouse position.
	 */
	private void activateCellEditor(MouseEvent event) {
		if (item == null || item.isDisposed()) {
			// item no longer exists
			return;
		}
		int columnToEdit;
		ViewerRow row = viewer.getViewerRowFromItem(item);
		int columns = row.getColumnCount();
		if (columns == 0) {
			// If no TableColumn, Table acts as if it has a single column
			// which takes the whole width.
			columnToEdit = 0;
		} else {
			columnToEdit = -1;
			for (int i = 0; i < columns; i++) {
				Rectangle bounds = row.getBounds(i);
				if (bounds.contains(event.x, event.y)) {
					columnToEdit = i;
					break;
				}
			}
			if (columnToEdit == -1) {
				return;
			}
		}

		columnNumber = columnToEdit;
		activateCellEditor();
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
			this.cellEditor = null;
			Item t = this.item;
			// don't null out table item -- same item is still selected
			if (t != null && !t.isDisposed()) {
				saveEditorValue(c, t);
			}
			setEditor(null, null, 0);
			c.removeListener(cellEditorListener);
			Control control = c.getControl();
			if (control != null) {
				if (mouseListener != null) {
					control.removeMouseListener(mouseListener);
				}
				if (focusListener != null) {
					control.removeFocusListener(focusListener);
				}
			}
			c.deactivate();
		}
	}

	/**
	 * Cancle editing
	 */
	void cancelEditing() {
		if (cellEditor != null) {
			setEditor(null, null, 0);
			cellEditor.removeListener(cellEditorListener);
			CellEditor oldEditor = cellEditor;
			cellEditor = null;
			oldEditor.deactivate();
		}
	}

	/**
	 * Enable the editor by mouse down
	 * 
	 * @param event
	 */
	void handleMouseDown(MouseEvent event) {
		if (event.button != 1) {
			return;
		}

		if (cellEditor != null) {
			applyEditorValue();
		}

		// activate the cell editor immediately. If a second mouseDown
		// is received prior to the expiration of the doubleClick time then
		// the cell editor will be deactivated and a doubleClick event will
		// be processed.
		//
		doubleClickExpirationTime = event.time
				+ Display.getCurrent().getDoubleClickTime();

		Item[] items = getSelection();
		// Do not edit if more than one row is selected.
		if (items.length != 1) {
			item = null;
			return;
		}
		item = items[0];
		activateCellEditor(event);
	}

	/**
	 * Start editing the given element.
	 * 
	 * @param element
	 * @param column
	 */
	void editElement(Object element, int column) {
		if (cellEditor != null) {
			applyEditorValue();
		}

		setSelection(createSelection(element), true);
		Item[] selection = getSelection();
		if (selection.length != 1) {
			return;
		}

		item = selection[0];

		// Make sure selection is visible
		showSelection();
		columnNumber = column;
		activateCellEditor();

	}

	private void saveEditorValue(CellEditor cellEditor, Item tableItem) {
		ViewerColumn part = viewer.getViewerColumn(columnNumber);

		if (part != null && part.getEditingSupport() != null) {
			part.getEditingSupport().setValue(tableItem.getData(),
					cellEditor.getValue());
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

	/**
	 * Set the cell editors
	 * 
	 * @param editors
	 */
	void setCellEditors(CellEditor[] editors) {
		this.cellEditors = editors;
	}

	/**
	 * Set the cell modifier
	 * 
	 * @param modifier
	 */
	void setCellModifier(ICellModifier modifier) {
		this.cellModifier = modifier;
	}

	/**
	 * Set the column properties
	 * 
	 * @param columnProperties
	 */
	void setColumnProperties(String[] columnProperties) {
		this.columnProperties = columnProperties;
	}

	/**
	 * Return the properties for the column
	 * 
	 * @return the array of column properties
	 */
	Object[] getColumnProperties() {
		return columnProperties;
	}

	/**
	 * Get the cell modifier
	 * 
	 * @return the cell modifier
	 */
	ICellModifier getCellModifier() {
		return cellModifier;
	}

	/**
	 * Return the array of CellEditors used in the viewer
	 * 
	 * @return the cell editors
	 */
	CellEditor[] getCellEditors() {
		return cellEditors;
	}

	void setSelection(StructuredSelection selection, boolean b) {
		viewer.setSelection(selection, b);
	}

	void handleDoubleClickEvent() {
		viewer.fireDoubleClick(new DoubleClickEvent(viewer, viewer
				.getSelection()));
		viewer.fireOpen(new OpenEvent(viewer, viewer.getSelection()));
	}

	/**
	 * Create a selection for this model element
	 * 
	 * @param element
	 *            the element for which the selection is created
	 * @return the selection created
	 */
	protected abstract StructuredSelection createSelection(Object element);

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
	 * Show up the current selection (scroll the selection into view)
	 */
	protected abstract void showSelection();

	/**
	 * @return the current selection
	 */
	protected abstract Item[] getSelection();
}
