package org.eclipse.jface.viewers;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import java.text.MessageFormat;

/**
 * A cell editor that manages a text entry field.
 * The cell editor's value is the text string itself.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class TextCellEditor extends CellEditor {

	/**
	 * The text control; initially <code>null</code>.
	 */
	protected Text text;

	private ModifyListener modifyListener;
/**
 * Creates a new text string cell editor parented under the given control.
 * The cell editor value is the string itself, which is initially the empty string. 
 * Initially, the cell editor has no cell validator.
 *
 * @param parent the parent control
 */
public TextCellEditor(Composite parent) {
	super(parent);
}
/* (non-Javadoc)
 * Method declared on CellEditor.
 */
protected Control createControl(Composite parent) {
	// specify no borders on text widget as cell outline in
	// table already provides the look of a border.
	text = new Text(parent, SWT.SINGLE);
	text.addKeyListener(new KeyAdapter() {
		public void keyReleased(KeyEvent e) {
			keyReleaseOccured(e);
		}
	});
	text.addTraverseListener(new TraverseListener() {
		public void keyTraversed(TraverseEvent e) {
			if (e.detail == SWT.TRAVERSE_ESCAPE || e.detail == SWT.TRAVERSE_RETURN) {
				e.doit = false;
			}
		}
	});
	
	text.setFont(parent.getFont());
	text.setBackground(parent.getBackground());
	text.setText("");//$NON-NLS-1$
	text.addModifyListener(getModifyListener());
	return text;
}
/**
 * The <code>TextCellEditor</code> implementation of
 * this <code>CellEditor</code> framework method returns
 * the text string.
 *
 * @return the text string
 */
protected Object doGetValue() {
	return text.getText();
}
/* (non-Javadoc)
 * Method declared on CellEditor.
 */
protected void doSetFocus() {
	if (text != null) {
		text.selectAll();
		text.setFocus();
	}
}
/**
 * The <code>TextCellEditor</code> implementation of
 * this <code>CellEditor</code> framework method accepts
 * a text string (type <code>String</code>).
 *
 * @param value a text string (type <code>String</code>)
 */
protected void doSetValue(Object value) {
	Assert.isTrue(text != null && (value instanceof String));
	text.removeModifyListener(getModifyListener());
	text.setText((String) value);
	text.addModifyListener(getModifyListener());
}
/**
 * Processes a modify event that occurred in this text cell editor.
 * This framework method performs validation and sets the error message
 * accordingly, and then reports a change via <code>fireEditorValueChanged</code>.
 * Subclasses should call this method at appropriate times. Subclasses
 * may extend or reimplement.
 *
 * @param e the SWT modify event
 */
protected void editOccured(ModifyEvent e) {
	String value = text.getText();
	if (value == null)
		value = "";//$NON-NLS-1$
	Object typedValue = value;
	boolean oldValidState = isValueValid();
	boolean newValidState = isCorrect(typedValue);
	if (typedValue == null && newValidState)
		Assert.isTrue(false, "Validator isn't limiting the cell editor's type range");//$NON-NLS-1$
	if (!newValidState) {
		// try to insert the current value into the error message.
		setErrorMessage(MessageFormat.format(getErrorMessage(), new Object[] {value}));
	}
	valueChanged(oldValidState, newValidState);
}
/**
 * Since a text editor field is scrollable we don't
 * set a minimumSize.
 */
public LayoutData getLayoutData() {
	return new LayoutData();
}
/**
 * Return the modify listener.
 */
private ModifyListener getModifyListener() {
	if (modifyListener == null) {
		modifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				editOccured(e);
			}
		};
	}
	return modifyListener;
}
/**
 * The <code>TextCellEditor</code> implementation of this
 * <code>CellEditor</code> method copies the
 * current selection to the clipboard. 
 */
public void performCopy() {
	text.copy();
}
/**
 * The <code>TextCellEditor</code> implementation of this
 * <code>CellEditor</code> method cuts the
 * current selection to the clipboard. 
 */
public void performCut() {
	text.cut();
}
/**
 * The <code>TextCellEditor</code> implementation of this
 * <code>CellEditor</code> method pastes the
 * the clipboard contents over the current selection. 
 */
public void performPaste() {
	text.paste();
}
/**
 * The <code>TextCellEditor</code> implementation of this
 * <code>CellEditor</code> method selects all of the
 * current text. 
 */
public void performSelectAll() {
	text.selectAll();
}
}
