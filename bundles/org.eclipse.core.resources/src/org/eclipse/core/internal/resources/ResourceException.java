package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.IResourceStatus;
import java.io.PrintStream;
import java.io.PrintWriter;

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

/**
 * Prints a stack trace out for the exception, and
 * any nested exception that it may have embedded in
 * its Status object.
 */
public void printStackTrace() {
	printStackTrace(System.err);
}
/**
 * Prints a stack trace out for the exception, and
 * any nested exception that it may have embedded in
 * its Status object.
 */
public void printStackTrace(PrintStream output) {
	synchronized (output) {
		IStatus status = getStatus();
		if (status.getException() != null) {
			String path = "()";
			if (status instanceof IResourceStatus)
				path = "(" + ((IResourceStatus)status).getPath() + ")"	;
			output.print(getClass().getName() + path + "[" + status.getCode() + "]: ");
			status.getException().printStackTrace(output);
		} else
			super.printStackTrace(output);
	}
}
/**
 * Prints a stack trace out for the exception, and
 * any nested exception that it may have embedded in
 * its Status object.
 */
public void printStackTrace(PrintWriter output) {
	synchronized (output) {
		IStatus status = getStatus();
		if (status.getException() != null) {
			String path = "()";
			if (status instanceof IResourceStatus)
				path = "(" + ((IResourceStatus)status).getPath() + ")"	;
			output.print(getClass().getName() + path + "[" + status.getCode() + "]: ");
			status.getException().printStackTrace(output);
		} else
			super.printStackTrace(output);
	}
}

}
