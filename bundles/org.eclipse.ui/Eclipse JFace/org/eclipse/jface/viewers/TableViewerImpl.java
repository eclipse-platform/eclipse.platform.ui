package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.TableEditor;
import java.util.*;

/**
 * Internal table viewer implementation.
 */ 
/* package */ abstract class TableViewerImpl {
	
	private boolean isActivating = false;
	private CellEditor cellEditor;
	private CellEditor[] cellEditors;
	private ICellModifier cellModifier;
	private String[] columnProperties;
	private Item tableItem;
	private int columnNumber;
	private ICellEditorListener cellEditorListener;
	private FocusListener focusListener;
	
	
TableViewerImpl() {
	initCellEditorListener();
}
/**
 * Activate a cell editor for the given column.
 */
private void activateCellEditor() {
	if (cellEditors != null) {
		if(cellEditors[columnNumber] != null && cellModifier != null) {
			Object element = tableItem.getData();
			String property = columnProperties[columnNumber];
			if (cellModifier.canModify(element, property)) {
				cellEditor = cellEditors[columnNumber];
				//table.showSelection();
				cellEditor.addListener(cellEditorListener);
				Object value = cellModifier.getValue(element, property);
				cellEditor.setValue(value);
				// Tricky flow of control here:
				// activate() can trigger callback to cellEditorListener which will clear cellEditor
				// so must get control first, but must still call activate() even if there is no control.
				Control control = cellEditor.getControl();
				cellEditor.activate();
				if (control == null)
					return;
		 		setLayoutData(cellEditor.getLayoutData());
				setEditor(control, tableItem, columnNumber);		
				cellEditor.setFocus();
				if(focusListener == null) {
					focusListener = new FocusAdapter() {
						public void focusLost(FocusEvent e) {
							applyEditorValue();
						}
					};
				}
				control.addFocusListener(focusListener);
			}
		}
	}
}
/**
 * Activate a cell editor for the given mouse position.
 */
private void activateCellEditor(MouseEvent event) {
	if (tableItem == null || tableItem.isDisposed()) {
		//item no longer exists
		return;
	}
	int columnToEdit;
	int columns = getColumnCount();
	if (columns == 0) {
		// If no TableColumn, Table acts as if it has a single column
		// which takes the whole width.
		columnToEdit = 0;
	}
	else {
		columnToEdit = -1;
		for (int i = 0; i < columns; i++) {
			Rectangle bounds = getBounds(tableItem, i);
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
 * Deactivates the currently active cell editor.
 */
public void applyEditorValue() {
	CellEditor c = this.cellEditor;
	if (c != null) {
		// null out cell editor before calling save
		// in case save results in applyEditorValue being re-entered
		// see 1GAHI8Z: ITPUI:ALL - How to code event notification when using cell editor ?
		this.cellEditor = null;
		Item t = this.tableItem;
		// don't null out table item -- same item is still selected
		if (t != null && !t.isDisposed()) {
			saveEditorValue(c, t);
		}
		setEditor(null, null, 0);
		c.removeListener(cellEditorListener);
		c.deactivate();
	}
}
/**
 * Cancels the active cell editor, without saving the value 
 * back to the domain model.
 */
public void cancelEditing() {
	if (cellEditor != null) {
		setEditor(null, null, 0);
		cellEditor.removeListener(cellEditorListener);
		CellEditor oldEditor = cellEditor;
		cellEditor = null;
		oldEditor.deactivate();
	}
}
/**
 * Start editing the given element. 
 */
public void editElement(Object element, int column) {
	if (cellEditor != null)
		applyEditorValue();

	setSelection(new StructuredSelection(element), true);
	Item[] selection = getSelection();
	if (selection.length != 1)
		return;

	tableItem = selection[0];

	// Make sure selection is visible
	showSelection();
	columnNumber = column;
	activateCellEditor();

}
abstract Rectangle getBounds(Item item, int columnNumber);
public CellEditor[] getCellEditors() {
	return cellEditors;
}
public ICellModifier getCellModifier() {
	return cellModifier; 
}
abstract int getColumnCount();
public Object[] getColumnProperties() {
	return columnProperties;
}
abstract Item[] getSelection();
/**
 * Handles the double click event.
 */
public void handleMouseDoubleClick(MouseEvent event) {
	//The last mouse down was a double click. Cancel
	//the cell editor activation.
	isActivating = false;
}
/**
 * Handles the mouse down event.
 * Activates the cell editor if it is not a double click.
 *
 * This implementation must:
 *	i) activate the cell editor when clicking over the item's text or over the item's image.
 *	ii) activate it only if the item is already selected.
 *	iii) do NOT activate it on a double click (whether the item is selected or not).
 */
public void handleMouseDown(MouseEvent event) {
	if (event.button != 1)
		return;
		
	boolean wasActivated = isCellEditorActive();
	if (wasActivated)
		applyEditorValue();

	Item[] items = getSelection();
	// Do not edit if more than one row is selected.
	if (items.length != 1) {
		tableItem = null;
		return;
	}

	if(tableItem != items[0]) {
		//This mouse down was a selection. Keep the selection and return;
		tableItem = items[0];
		return;
	}

	//It may be a double click. If so, the activation was started by the first click.
	if(isActivating || wasActivated)
		return;
		
	isActivating = true;
	//Post the activation. So it may be canceled if it was a double click.
	postActivation(event);
}
private void initCellEditorListener() {
	cellEditorListener = new ICellEditorListener() {
		public void editorValueChanged(boolean oldValidState, boolean newValidState) {
			// Ignore.
		}
		
		public void cancelEditor() {
			TableViewerImpl.this.cancelEditing();
		}
		
		public void applyEditorValue() {
			TableViewerImpl.this.applyEditorValue();
		}
	};
}
/**
 * Returns <code>true</code> if there is an active cell editor; otherwise
 * <code>false</code> is returned.
 */
public boolean isCellEditorActive() {
	return cellEditor != null;
}
/**
 * Handle the mouse down event.
 * Activate the cell editor if it is not a doble click.
 */
private void postActivation(final MouseEvent event) {
	if(!isActivating)
		return;
	
	(new Thread() {
		public void run() {
			try { Thread.sleep(400); } catch (Exception e){}
			if(isActivating) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						activateCellEditor(event);
						isActivating = false;
					}
				});
			}
		}
	}).start();
}
/**
 * Saves the value of the currently active cell editor,
 * by delegating to the cell modifier.
 */
private void saveEditorValue(CellEditor cellEditor, Item tableItem) {
	if (cellModifier != null) {
		if (!cellEditor.isValueValid()) {
			///Do what ???
		}
		String property = null;
		if (columnProperties != null && columnNumber < columnProperties.length)
			property = columnProperties[columnNumber];
		cellModifier.modify(tableItem, property, cellEditor.getValue());
	}
}
public void setCellEditors(CellEditor[] editors) {
	this.cellEditors = editors;
}
public void setCellModifier(ICellModifier modifier) {
	this.cellModifier = modifier; 
}
public void setColumnProperties(String[] columnProperties) {
	this.columnProperties = columnProperties;
}
abstract void setEditor(Control w, Item item, int fColumnNumber);
abstract void setLayoutData(CellEditor.LayoutData layoutData);
abstract void setSelection(StructuredSelection selection, boolean b);
abstract void showSelection();
}
