package org.eclipse.jface.resource;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */

/**
 * An exception indicating that a string value could not be
 * converted into the requested data type.
 *
 * @see StringConverter
 */ 
public class DataFormatException extends IllegalArgumentException {
/**
 * Creates a new exception.
 */
public DataFormatException() {
	super();
}
/**
 * Creates a new exception.
 *
 * @param message the message
 */
public DataFormatException(String message) {
	super(message);
}
}
