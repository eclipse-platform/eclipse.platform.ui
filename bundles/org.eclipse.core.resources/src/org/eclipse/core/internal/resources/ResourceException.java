package org.eclipse.core.internal.resources;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.*;

/**
 * A checked expection representing a failure.
 * <p>
 * Core exceptions contain a status object describing the 
 * cause of the exception.
 * </p>
 *
 * @see IStatus
 */
public class ResourceException extends CoreException {
public ResourceException(int code, IPath path, String message, Throwable exception) {
	super(new ResourceStatus(code, path, message, exception));
}
/**
 * Constructs a new exception with the given status object.
 *
 * @param status the status object to be associated with this exception
 * @see IStatus
 */
public ResourceException(IStatus status) {
	super(status);
}
}
