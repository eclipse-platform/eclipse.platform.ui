package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.*;
import org.eclipse.jface.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import java.util.*;
import java.util.List; // disambiguate from SWT List

/**
 * Abstract base class for cell editors. Implements property change listener handling,
 * and SWT window management.
 * <p>
 * Subclasses implement particular kinds of cell editors. This package contains various
 * specialized cell editors:
 * <ul>
 *   <li><code>TextCellEditor</code> - for simple text strings</li>
 *   <li><code>ColorCellEditor</code> - for colors</li>
 *   <li><code>ComboBoxCellEditor</code> - value selected from drop-down combo box</li>
 *   <li><code>CheckboxCellEditor</code> - boolean valued checkbox</li>
 *   <li><code>DialogCellEditor</code> - value from arbitrary dialog</li>
 * </ul>
 * </p>
 */
public abstract class CellEditor {

	/**
	 * List of cell editor listeners (element type: <code>ICellEditorListener</code>).
	 */
	private ListenerList listeners = new ListenerList(3);
	/**
	 * List of cell editor property change listeners 
	 * (element type: <code>IPropertyChangeListener</code>).
	 */
	private ListenerList propertyChangeListeners = new ListenerList(3);
	
	/**
	 * Indicates whether this cell editor's current value is valid.
	 */
	private boolean valid = false;
	
	/**
	 * Optional cell editor validator; <code>null</code> if none.
	 */
	private ICellEditorValidator validator = null;

	/**
	 * The error message string to display for invalid values;
	 * <code>null</code> if none (that is, the value is valid).
	 */
	private String errorMessage = null;
	
	/**
	 * Indicates whether this cell editor has been changed recently.
	 */
	private boolean dirty = false;

	/**
	 * This cell editor's control, or <code>null</code>
	 * if not created yet.
	 */
	private Control control = null;

	/**
	 * This cell editor's style
	 */
	private int style = SWT.NONE;

	/** 
	 * Struct-like layout data for cell editors, with reasonable defaults
	 * for all fields.
	 */	
	public static class LayoutData {
		/**
		 * Horizontal alignment; <code>SWT.LEFT</code> by default.
		 */
		public int horizontalAlignment = SWT.LEFT;

		/**
		 * Indicates control grabs additional space; <code>true</code> by default.
		 */
		public boolean grabHorizontal = true;

		/**
		 * Minimum width in pixels; <code>50</code> pixels by default.
		 */
		public int minimumWidth = 50;
	}

	/**
	 * Property name for the copy action
	 */
	public static final String COPY = "copy"; //$NON-NLS-1$

	/**
	 * Property name for the cut action
	 */
	public static final String CUT = "cut"; //$NON-NLS-1$
	
	/**
	 * Property name for the delete action
	 */
	public static final String DELETE = "delete"; //$NON-NLS-1$

	/**
	 * Property name for the find action
	 */
	public static final String FIND = "find"; //$NON-NLS-1$

	/**
	 * Property name for the paste action
	 */
	public static final String PASTE = "paste"; //$NON-NLS-1$

	/**
	 * Property name for the redo action
	 */
	public static final String REDO = "redo"; //$NON-NLS-1$

	/**
	 * Property name for the select all action
	 */
	public static final String SELECT_ALL = "selectall"; //$NON-NLS-1$

	/**
	 * Property name for the undo action
	 */
	public static final String UNDO = "undo"; //$NON-NLS-1$
/**
 * Creates a new cell editor under the given parent control.
 * The cell editor has no cell validator.
 *
 * @param parent the parent control
 */
protected CellEditor(Composite parent) {
	this(parent, SWT.NONE);
}
/**
 * Creates a new cell editor under the given parent control.
 * The cell editor has no cell validator.
 *
 * @param parent the parent control
 * @param style the style bits
 * @since 2.1
 */
protected CellEditor(Composite parent, int style) {
	this.style = style;
	control = createControl(parent);

	// See 1GD5CA6: ITPUI:ALL - TaskView.setSelection does not work
	// Control is created with isVisible()==true by default.
	// This causes composite.setFocus() to work incorrectly.
	// The cell editor's control grabs focus instead, even if it is not active.
	// Make the control invisible here by default.
	deactivate();
}
/**
 * Activates this cell editor.
 * <p>
 * The default implementation of this framework method
 * does nothing. Subclasses may reimplement.
 * </p>
 */
public void activate() {
}
/**
 * Adds a listener to this cell editor.
 * Has no effect if an identical listener is already registered.
 *
 * @param listener a cell editor listener
 */
public void addListener(ICellEditorListener listener) {
	listeners.add(listener);
}
/**
 * Adds a property change listener to this cell editor.
 * Has no effect if an identical property change listener 
 * is already registered.
 *
 * @param listener a property change listener
 */
public void addPropertyChangeListener(IPropertyChangeListener listener) {
	propertyChangeListeners.add(listener);
}
/**
 * Creates the control for this cell editor under the given parent control.
 * <p>
 * This framework method must be implemented by concrete
 * subclasses.
 * </p>
 *
 * @param parent the parent control
 * @return the new control, or <code>null</code> if this cell editor has no control
 */
protected abstract Control createControl(Composite parent);
/**
 * Hides this cell editor's control. Does nothing if this 
 * cell editor is not visible.
 */
public void deactivate() {
	if (control != null && !control.isDisposed())
		control.setVisible(false);
}
/**
 * Disposes of this cell editor and frees any associated SWT resources.
 */
public void dispose() {
	// XXX: Should not need to check if we are already disposed but this
	// was occuring in the property sheet.
	if (control != null && !control.isDisposed()) {
		control.dispose();
	}
}
/**
 * Returns this cell editor's value.
 * <p>
 * This framework method must be implemented by concrete subclasses.
 * </p>
 *
 * @return the value of this cell editor
 * @see #getValue
 */
protected abstract Object doGetValue();
/**
 * Sets the focus to the cell editor's control.
 * <p>
 * This framework method must be implemented by concrete subclasses.
 * </p>
 *
 * @see #setFocus
 */
protected abstract void doSetFocus();
/**
 * Sets this cell editor's value.
 * <p>
 * This framework method must be implemented by concrete subclasses.
 * </p>
 *
 * @param value the value of this cell editor
 * @see #setValue
 */
protected abstract void doSetValue(Object value);
/**
 * Notifies all registered cell editor listeners of an apply event.
 * Only listeners registered at the time this method is called are notified.
 *
 * @see ICellEditorListener#applyEditorValue
 */
protected void fireApplyEditorValue() {
	Object[] listeners = this.listeners.getListeners();
	for (int i = 0; i < listeners.length; ++i) {
		((ICellEditorListener) listeners[i]).applyEditorValue();
	}
}
/**
 * Notifies all registered cell editor listeners that editing has been
 * canceled.
 *
 * @see ICellEditorListener#cancelEditor
 */
protected void fireCancelEditor() {
	Object[] listeners = this.listeners.getListeners();
	for (int i = 0; i < listeners.length; ++i) {
		((ICellEditorListener) listeners[i]).cancelEditor();
	}
}
/**
 * Notifies all registered cell editor listeners of a value change.
 *
 * @param oldValidState the valid state before the end user changed the value
 * @param newValidState the current valid state
 * @see ICellEditorListener#editorValueChanged
 */
protected void fireEditorValueChanged(boolean oldValidState, boolean newValidState) {
	Object[] listeners = this.listeners.getListeners();
	for (int i = 0; i < listeners.length; ++i) {
		((ICellEditorListener) listeners[i]).editorValueChanged(oldValidState, newValidState);
	}
}
/**
 * Notifies all registered property listeners
 * of an enablement change.
 *
 * @param actionId the id indicating what action's enablement has changed.
 */
protected void fireEnablementChanged(String actionId) {
	Object[] listeners = propertyChangeListeners.getListeners();
	for (int i = 0; i < listeners.length; ++i) {
		((IPropertyChangeListener) listeners[i]).propertyChange(new PropertyChangeEvent(this, actionId, null, null));
	}
}
/**
 * Returns the style bits for this this cell editor.
 *
 * @return the style for this cell editor
 * @since 2.1
 */
public int getStyle() {
	return style;
}
/**
 * Returns the control used to implement this cell editor.
 *
 * @return the control, or <code>null</code> if this cell editor has no control
 */
public Control getControl() {
	return control;
}
/**
 * Returns the current error message for this cell editor.
 * 
 * @return the error message if the cell editor is in an invalid state,
 *  and <code>null</code> if the cell editor is valid
 */
public String getErrorMessage() {
	return errorMessage;
}
/**
 * Returns a layout data object for this cell editor.
 * This is called each time the cell editor is activated
 * and controls the layout of the SWT table editor.
 * <p>
 * The default implementation of this method sets the 
 * minimum width to the control's preferred width.
 * Subclasses may extend or reimplement.
 * </p>
 *
 * @return the layout data object 
 */
public LayoutData getLayoutData() {
	LayoutData result = new LayoutData();
	Control control = getControl();
	if (control != null) {
		result.minimumWidth = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
	}
	return result;
}
/**
 * Returns the input validator for this cell editor.
 *
 * @return the input validator, or <code>null</code> if none
 */
public ICellEditorValidator getValidator() {
	return validator;
}
/**
 * Returns this cell editor's value provided that it has a valid one.
 *
 * @return the value of this cell editor, or <code>null</code>
 *   if the cell editor does not contain a valid value
 */
public final Object getValue() {
	if (!valid)
		return null;

	return doGetValue();
}
/**
 * Returns whether this cell editor is activated.
 *
 * @return <code>true</code> if this cell editor's control is
 *   currently visible, and <code>false</code> if not visible
 */
public boolean isActivated() {
	return control != null && control.isVisible();
}
/**
 * Returns <code>true</code> if this cell editor is
 * able to perform the copy action.
 * <p>
 * This default implementation always returns 
 * <code>false</code>.
 * </p>
 * <p>
 * Subclasses may override
 * </p>
 * @return <code>true</code> if copy is possible,
 *  <code>false</code> otherwise
 */
public boolean isCopyEnabled() {
	return false;
}
/**
 * Returns whether the given value is valid for this cell editor.
 * This cell editor's validator (if any) makes the actual determination.
 *
 * @return <code>true</code> if the value is valid, and <code>false</code>
 *  if invalid
 */
protected boolean isCorrect(Object value) {
	errorMessage = null;
	if (validator == null)
		return true;

	errorMessage = validator.isValid(value);
	return (errorMessage == null || errorMessage.equals(""));//$NON-NLS-1$
}
/**
 * Returns <code>true</code> if this cell editor is
 * able to perform the cut action.
 * <p>
 * This default implementation always returns 
 * <code>false</code>.
 * </p>
 * <p>
 * Subclasses may override
 * </p>
 * @return <code>true</code> if cut is possible,
 *  <code>false</code> otherwise
 */
public boolean isCutEnabled() {
	return false;
}
/**
 * Returns <code>true</code> if this cell editor is
 * able to perform the delete action.
 * <p>
 * This default implementation always returns 
 * <code>false</code>.
 * </p>
 * <p>
 * Subclasses may override
 * </p>
 * @return <code>true</code> if delete is possible,
 *  <code>false</code> otherwise
 */
public boolean isDeleteEnabled() {
	return false;
}
/**
 * Returns whether the value of this cell editor has changed since the
 * last call to <code>setValue</code>.
 *
 * @return <code>true</code> if the value has changed, and <code>false</code>
 *  if unchanged
 */
public boolean isDirty() {
	return dirty;
}
/**
 * Returns <code>true</code> if this cell editor is
 * able to perform the find action.
 * <p>
 * This default implementation always returns 
 * <code>false</code>.
 * </p>
 * <p>
 * Subclasses may override
 * </p>
 * @return <code>true</code> if find is possible,
 *  <code>false</code> otherwise
 */
public boolean isFindEnabled() {
	return false;
}
/**
 * Returns <code>true</code> if this cell editor is
 * able to perform the paste action.
 * <p>
 * This default implementation always returns 
 * <code>false</code>.
 * </p>
 * <p>
 * Subclasses may override
 * </p>
 * @return <code>true</code> if paste is possible,
 *  <code>false</code> otherwise
 */
public boolean isPasteEnabled() {
	return false;
}
/**
 * Returns <code>true</code> if this cell editor is
 * able to perform the redo action.
 * <p>
 * This default implementation always returns 
 * <code>false</code>.
 * </p>
 * <p>
 * Subclasses may override
 * </p>
 * @return <code>true</code> if redo is possible,
 *  <code>false</code> otherwise
 */
public boolean isRedoEnabled() {
	return false;
}
/**
 * Returns <code>true</code> if this cell editor is
 * able to perform the select all action.
 * <p>
 * This default implementation always returns 
 * <code>false</code>.
 * </p>
 * <p>
 * Subclasses may override
 * </p>
 * @return <code>true</code> if select all is possible,
 *  <code>false</code> otherwise
 */
public boolean isSelectAllEnabled() {
	return false;
}
/**
 * Returns <code>true</code> if this cell editor is
 * able to perform the undo action.
 * <p>
 * This default implementation always returns 
 * <code>false</code>.
 * </p>
 * <p>
 * Subclasses may override
 * </p>
 * @return <code>true</code> if undo is possible,
 *  <code>false</code> otherwise
 */
public boolean isUndoEnabled() {
	return false;
}
/**
 * Returns whether this cell editor has a valid value.
 * The default value is false.
 *
 * @return <code>true</code> if the value is valid, and <code>false</code>
 *  if invalid
 *
 * @see #setValueValid
 */
public boolean isValueValid() {
	return valid;
}
/**
 * Processes a key release event that occurred in this cell editor.
 * <p>
 * The default implementation of this framework method interprets
 * the ESC key as canceling editing, and the RETURN key
 * as applying the current value. Subclasses should call this method
 * at appropriate times. Subclasses may also extend or reimplement.
 * </p>
 *
 * @param keyEvent the key event
 */
protected void keyReleaseOccured(KeyEvent keyEvent) {
	if (keyEvent.character == '\u001b') { // Escape character
		fireCancelEditor();
		return;
	} else if (keyEvent.character == '\r') { // Return key
		fireApplyEditorValue();
		return;
	}
}
/**
 * Performs the copy action.
 * This default implementation does nothing.
 * <p>
 * Subclasses may override
 * </p>
 */
public void performCopy() {
}
/**
 * Performs the cut action.
 * This default implementation does nothing.
 * <p>
 * Subclasses may override
 * </p>
 */
public void performCut() {
}
/**
 * Performs the delete action.
 * This default implementation does nothing.
 * <p>
 * Subclasses may override
 * </p>
 */
public void performDelete() {
}
/**
 * Performs the find action.
 * This default implementation does nothing.
 * <p>
 * Subclasses may override
 * </p>
 */
public void performFind() {
}
/**
 * Performs the paste action.
 * This default implementation does nothing.
 * <p>
 * Subclasses may override
 * </p>
 */
public void performPaste() {
}
/**
 * Performs the redo action.
 * This default implementation does nothing.
 * <p>
 * Subclasses may override
 * </p>
 */
public void performRedo() {
}
/**
 * Performs the select all action.
 * This default implementation does nothing.
 * <p>
 * Subclasses may override
 * </p>
 */
public void performSelectAll() {
}
/**
 * Performs the undo action.
 * This default implementation does nothing.
 * <p>
 * Subclasses may override
 * </p>
 */
public void performUndo() {
}
/**
 * Removes the given listener from this cell editor.
 * Has no affect if an identical listener is not registered.
 *
 * @param listener a cell editor listener
 */
public void removeListener(ICellEditorListener listener) {
	listeners.remove(listener);
}
/**
 * Removes the given property change listener from this cell editor.
 * Has no affect if an identical property change listener is not 
 * registered.
 *
 * @param listener a property change listener
 */
public void removePropertyChangeListener(IPropertyChangeListener listener) {
	propertyChangeListeners.remove(listener);
}
/**
 * Sets or clears the current error message for this cell editor.
 * 
 * @param message the error message, or <code>null</code> to clear
 */
protected void setErrorMessage(String message) {
	errorMessage = message;
}
/**
 * Sets the focus to the cell editor's control.
 */
public void setFocus() {
	doSetFocus();
}
/**
 * Sets the input validator for this cell editor.
 *
 * @param validator the input validator, or <code>null</code> if none
 */
public void setValidator(ICellEditorValidator validator) {
	this.validator = validator;
}
/**
 * Sets this cell editor's value.
 *
 * @param value the value of this cell editor
 */
public final void setValue(Object value) {
	valid = isCorrect(value);
	dirty = false;
	doSetValue(value);
}
/**
 * Sets the valid state of this cell editor.
 * The default value is false.
 * Subclasses should call this method on construction.
 *
 * @param valid <code>true</code> if the current valie is valid,
 *  and <code>false</code> if invalid
 *
 * @see #isValueValid
 */
protected void setValueValid(boolean valid) {
	this.valid = valid;
}
/**
 * The value has changed.  
 * Updates the valid state flag, marks this cell editor as dirty,
 * and notifies all registered cell editor listeners of a value change.
 *
 * @param oldValidState the valid state before the end user changed the value
 * @param newValidState the current valid state
 * @see ICellEditorListener#editorValueChanged
 */
protected void valueChanged(boolean oldValidState, boolean newValidState) {
	valid = newValidState;
	dirty = true;
	fireEditorValueChanged(oldValidState, newValidState);
}
}
