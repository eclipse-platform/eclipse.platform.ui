package org.eclipse.jface.resource;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
