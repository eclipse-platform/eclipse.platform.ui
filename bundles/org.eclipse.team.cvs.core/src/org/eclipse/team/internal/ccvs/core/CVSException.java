package org.eclipse.team.internal.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.connection.ResourceStatus;

/**
 * This is an exception that is thrown by the cvs-adaptor
 * for vcm
 * 
 * @see CoreExcpetion
 */

public class CVSException extends TeamException {

	public static final int SERVER_ERROR = -10; // XXX What should this number be?
	public static final int NO_SUCH_TAG = -11;
	public static final int CONFLICT = -12;
	public static final int WARNING = -13;
	
	public CVSException(
		int severity,
		int code,
		IPath path,
		String message,
		Throwable exception) {
		super(new ResourceStatus(severity, code, path, message, exception));
	}
	public CVSException(
		int severity,
		int code,
		IPath path,
		String message) {
		this(severity, code, path, message, null);
	}
	public CVSException(
		int severity,
		int code,
		IPath path,
		Throwable exception) {
		this(severity, code, path, null, exception);
	}
	public CVSException(
		int severity,
		int code,
		String message,
		Exception e) {
		super(new Status(severity, CVSProviderPlugin.ID, code, message, null));
	}
	public CVSException(
		int severity,
		int code,
		String message) {
		this(severity, code, message, null);
	}

	public CVSException(
		int severity,
		int code,
		Exception e) {
		super(new Status(severity, CVSProviderPlugin.ID, code, null, e));

	}

	public CVSException(String message) {
		super(new Status(IStatus.ERROR, CVSProviderPlugin.ID, UNABLE, message, null));
	}

	public CVSException(String message, IPath path) {
		this(message, path, null);
	}
	
	public CVSException(String message, Exception e) {
		this(IStatus.ERROR, UNABLE, message, e);
	}

	public CVSException(String message, IPath path, Throwable throwable) {
		this(new ResourceStatus(IStatus.ERROR, path, message, throwable));
	}
	public CVSException(IStatus status) {
		super(status);
	}
	
	/*
	 * Static helper methods for creating exceptions
	 */
	public static CVSException wrapException(IResource resource, String message,IOException e) {
		// NOTE: we should record the resource somehow
		// We should also inlcude the IO message
		return new CVSException(new Status(
				IStatus.ERROR,
				CVSProviderPlugin.ID,
				IO_FAILED,
				message,
				e));
	}
	/*
	 * Static helper methods for creating exceptions
	 */
	public static CVSException wrapException(IResource resource, String message, CoreException e) {
		return new CVSException(new Status(
				IStatus.ERROR,
				CVSProviderPlugin.ID,
				UNABLE,
				message,
				e));
	}
	/*
	 * Static helper methods for creating exceptions
	 */
	public static CVSException wrapException(Exception e) {
		return new CVSException(new Status(
				IStatus.ERROR,
				CVSProviderPlugin.ID,
				UNABLE,
				e.getMessage(),
				e));
	}
}