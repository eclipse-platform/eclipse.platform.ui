package org.eclipse.jface.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/*
 * Minimal interface to an input validator.
 * Input validators are used in <code>InputDialog</code>.
 */
public interface IInputValidator {
/**
 * Validates the given string.  Returns an error message to display
 * if the new text is invalid.  Returns <code>null</code> if there
 * is no error.  Note that the empty string is not treated the same
 * as <code>null</code>; it indicates an error state but with no message
 * to display.
 * 
 * @return an error message or <code>null</code> if no error
 */
public String isValid(String newText);
}
