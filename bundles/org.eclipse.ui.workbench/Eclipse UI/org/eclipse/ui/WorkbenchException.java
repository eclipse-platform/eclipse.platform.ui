package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
 
/**
 * A checked exception indicating a recoverable error occured internal to the
 * workbench. The status provides a further description of the problem.
 * <p>
 * This exception class is not intended to be subclassed by clients.
 * </p>
 */
public class WorkbenchException extends CoreException {
/**
 * Creates a new exception with the given message.
 * 
 * @param message the message
 */
public WorkbenchException(String message) {
	this(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, null));
}
/**
 * Creates a new exception with the given message.
 *
 * @param message the message
 * @param nestedException an exception to be wrapped by this WorkbenchException
 */
public WorkbenchException(String message, Throwable nestedException) {
	this(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, nestedException));
}
/**
 * Creates a new exception with the given status object.  The message
 * of the given status is used as the exception message.
 *
 * @param status the status object to be associated with this exception
 */
public WorkbenchException(IStatus status) {
	super(status);
}
}
