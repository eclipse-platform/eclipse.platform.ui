package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IStatus;
 
/**
 * A checked exception indicating a workbench part cannot be initialized 
 * correctly. The message text provides a further description of the problem.
 * <p>
 * This exception class is not intended to be subclassed by clients.
 * </p>
 */
public class PartInitException extends WorkbenchException {
/**
 * Creates a new exception with the given message.
 * 
 * @param message the message
 */
public PartInitException(String message) {
	super(message);
}
/**
 * Creates a new exception with the given message.
 * 
 * @param message the message
 * @parem nestedException a exception to be wrapped by this PartInitException
 */
public PartInitException(String message,Throwable nestedException) {
	super(message, nestedException);
}
/**
 * Creates a new exception with the given status object.  The message
 * of the given status is used as the exception message.
 *
 * @param status the status object to be associated with this exception
 */
public PartInitException(IStatus status) {
	super(status);
}
}
