package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * An interface for validating a cell editor's input.
 * <p>
 * This interface should be implemented by classes that wish to
 * act as cell editor validators.
 * </p>
 */
public interface ICellEditorValidator {
/**
 * Returns a string indicating whether the given value is valid;
 * <code>null</code> means valid, and non-<code>null</code> means
 * invalid, with the result being the error message to display
 * to the end user.
 *
 * @param value the value to be validated
 * @return the error message, or <code>null</code> indicating
 *	that the value is valid
 */
public String isValid(Object value);
}
