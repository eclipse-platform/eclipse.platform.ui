package org.eclipse.core.runtime;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * This exception is thrown to blow out of a long-running method 
 * when the user cancels it.
 */
public final class OperationCanceledException extends RuntimeException {
/**
 * Creates a new exception.
 */
public OperationCanceledException() {
	super();
}
/**
 * Creates a new exception with the given message.
 */
public OperationCanceledException(String message) {
	super(message);
}
}
