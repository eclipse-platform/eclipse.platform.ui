package org.eclipse.jface.preference;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*; 
 
/**
 * A field editor for an integer type preference.
 */
public class IntegerFieldEditor extends StringFieldEditor {
/**
 * Creates a new integer field editor 
 */
protected IntegerFieldEditor() {
}
/**
 * Creates an integer field editor.
 * 
 * @param name the name of the preference this field editor works on
 * @param labelText the label text of the field editor
 * @param parent the parent of the field editor's control
 */
public IntegerFieldEditor(String name, String labelText, Composite parent) {
	init(name, labelText);
	setTextLimit(10);
	setEmptyStringAllowed(false);
	setErrorMessage(JFaceResources.getString("IntegerFieldEditor.errorMessage"));//$NON-NLS-1$
	createControl(parent);
}
/* (non-Javadoc)
 * Method declared on StringFieldEditor.
 * Checks whether the entered String is a valid integer or not.
 */
protected boolean checkState() {

	Text text = getTextControl();

	if (text == null)
		return false;

	String numberString = text.getText();
	try {
		Integer number = Integer.valueOf(numberString);
		if (number.intValue() >= 0) {
			clearErrorMessage();
			return true;
		} else {
			showErrorMessage();
			return false;
		}
	} catch (NumberFormatException e1) {
		showErrorMessage();
	}

	return false;
}
/* (non-Javadoc)
 * Method declared on FieldEditor.
 */
protected void doLoad() {
	Text text = getTextControl();
	if (text != null) {
		int value = getPreferenceStore().getInt(getPreferenceName());
		text.setText("" + value);//$NON-NLS-1$
	}

}
/* (non-Javadoc)
 * Method declared on FieldEditor.
 */
protected void doLoadDefault() {
	Text text = getTextControl();
	if (text != null) {
		int value = getPreferenceStore().getDefaultInt(getPreferenceName());
		text.setText("" + value);//$NON-NLS-1$
	}
}
/* (non-Javadoc)
 * Method declared on FieldEditor.
 */
protected void doStore() {
	Text text = getTextControl();
	if (text != null) {
		Integer i = new Integer(text.getText());
		getPreferenceStore().setValue(getPreferenceName(), i.intValue());
	}
}
/**
 * Returns this field editor's current value as an integer.
 *
 * @return the value
 * @exception NumberFormatException if the <code>String</code> does not
 *   contain a parsable integer
 */
public int getIntValue() throws NumberFormatException {
	return new Integer(getStringValue()).intValue();
}
}
