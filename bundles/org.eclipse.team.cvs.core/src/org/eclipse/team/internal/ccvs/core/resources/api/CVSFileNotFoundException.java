package org.eclipse.team.internal.ccvs.core.resources.api;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.ResourceStatus;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;

/**
 * This exception represents the attemp to access a file/folder
 * that did not exist.
 */
public class CVSFileNotFoundException extends CVSException {

	public CVSFileNotFoundException(
		int severity,
		int code,
		IPath path,
		String message,
		Throwable exception) {
		super(new ResourceStatus(severity, code, path, message, exception));
	}
	public CVSFileNotFoundException(
		int severity,
		int code,
		IPath path,
		String message) {
		this(severity, code, path, message, null);
	}
	public CVSFileNotFoundException(
		int severity,
		int code,
		IPath path,
		Throwable exception) {
		this(severity, code, path, null, exception);
	}
	public CVSFileNotFoundException(
		int severity,
		int code,
		String message,
		Exception e) {
		super(new Status(severity, CVSProviderPlugin.ID, code, message, null));
	}
	public CVSFileNotFoundException(
		int severity,
		int code,
		String message) {
		this(severity, code, message, null);
	}

	public CVSFileNotFoundException(
		int severity,
		int code,
		Exception e) {
		super(new Status(severity, CVSProviderPlugin.ID, code, null, e));

	}

	public CVSFileNotFoundException(String message) {
		super(new Status(IStatus.ERROR, CVSProviderPlugin.ID, IStatus.ERROR, message, null));
	}

	public CVSFileNotFoundException(String message, IPath path) {
		this(message, path, null);
	}

	public CVSFileNotFoundException(String message, IPath path, Throwable throwable) {
		this(new ResourceStatus(IStatus.ERROR, path, message, throwable));
	}
	public CVSFileNotFoundException(IStatus status) {
		super(status);
	}

}

