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
 * Validates the given string.
 * 
 * @return <code>null</code> to indicate that the value is valid; 
 * otherwise, a localized error message that could be presented 
 * to the user
 */
public String isValid(String newText);
}
