package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.widgets.*;

/**
 * A cell editor that manages a checkbox.
 * The cell editor's value is a boolean.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Note that this implementation simply fakes it and does does not create
 * any new controls. The mere activation of this editor means that the value
 * of the check box is being toggled by the end users; the listener method
 * <code>applyEditorValue</code> is immediately called to signal the change.
 * </p>
 */
public class CheckboxCellEditor extends CellEditor {

	/**
	 * The checkbox value.
	 */
	/* package */ boolean value = false;
/**
 * Creates a new checkbox cell editor parented under the given control.
 * The cell editor value is a boolean value, which is initially <code>false</code>. 
 * Initially, the cell editor has no cell validator.
 *
 * @param parent the parent control
 */
public CheckboxCellEditor(Composite parent) {
	super(parent);
}
/**
 * The <code>CheckboxCellEditor</code> implementation of
 * this <code>CellEditor</code> framework method simulates
 * the toggling of the checkbox control and notifies
 * listeners with <code>ICellEditorListener.applyEditorValue</code>.
 */
public void activate() {
	value = !value;
	fireApplyEditorValue();
}
/**
 * The <code>CheckboxCellEditor</code> implementation of
 * this <code>CellEditor</code> framework method does
 * nothing and returns <code>null</code>.
 */
protected Control createControl(Composite parent) {
	return null;
}
/**
 * The <code>CheckboxCellEditor</code> implementation of
 * this <code>CellEditor</code> framework method returns
 * the checkbox setting wrapped as a <code>Boolean</code>.
 *
 * @return the Boolean checkbox value
 */
protected Object doGetValue() {
	return new Boolean(value);
}
/* (non-Javadoc)
 * Method declared on CellEditor.
 */
protected void doSetFocus() {
	// Ignore
}
/**
 * The <code>CheckboxCellEditor</code> implementation of
 * this <code>CellEditor</code> framework method accepts
 * a value wrapped as a <code>Boolean</code>.
 *
 * @param value a Boolean value
 */
protected void doSetValue(Object value) {
	Assert.isTrue(value instanceof Boolean);
	this.value = ((Boolean) value).booleanValue();
}
}
