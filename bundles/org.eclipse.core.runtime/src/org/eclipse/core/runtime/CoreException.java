package org.eclipse.core.runtime;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
/**
 * A checked expection representing a failure.
 * <p>
 * Core exceptions contain a status object describing the 
 * cause of the exception.
 * </p>
 *
 * @see IStatus
 */
public class CoreException extends Exception {

	/** Status object. */
	private IStatus status;
/**
 * Creates a new exception with the given status object.  The message
 * of the given status is used as the exception message.
 *
 * @param status the status object to be associated with this exception
 */
public CoreException(IStatus status) {
	super(status.getMessage());
	this.status = status;
}
/**
 * Returns the status object for this exception.
 *
 * @return a status object
 */
public final IStatus getStatus() {
	return status;
}
}
