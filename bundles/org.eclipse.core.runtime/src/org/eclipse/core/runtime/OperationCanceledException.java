package org.eclipse.core.runtime;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
