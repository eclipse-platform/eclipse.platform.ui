package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.ResourceStatus;

/**
 * This Exception indicates that you have tried to call
 * a CVSFolder-Specific function on a folder that is not
 * (yet) a cvs-folder.
 */
public class NotCVSFolderException extends CVSException {
	
	public NotCVSFolderException(
		int severity,
		int code,
		IPath path,
		String message,
		Throwable exception) {
		super(new ResourceStatus(severity, code, path, message, exception));
	}
	public NotCVSFolderException(
		int severity,
		int code,
		IPath path,
		String message) {
		this(severity, code, path, message, null);
	}
	public NotCVSFolderException(
		int severity,
		int code,
		IPath path,
		Throwable exception) {
		this(severity, code, path, null, exception);
	}
	public NotCVSFolderException(
		int severity,
		int code,
		String message,
		Exception e) {
		super(new Status(severity, CVSProviderPlugin.ID, code, message, null));
	}
	public NotCVSFolderException(
		int severity,
		int code,
		String message) {
		this(severity, code, message, null);
	}

	public NotCVSFolderException(
		int severity,
		int code,
		Exception e) {
		super(new Status(severity, CVSProviderPlugin.ID, code, null, e));

	}

	public NotCVSFolderException(String message) {
		super(new Status(IStatus.ERROR, CVSProviderPlugin.ID, IStatus.ERROR, message, null));
	}

	public NotCVSFolderException(String message, IPath path) {
		this(message, path, null);
	}

	public NotCVSFolderException(String message, IPath path, Throwable throwable) {
		this(new ResourceStatus(IStatus.ERROR, path, message, throwable));
	}
	public NotCVSFolderException(IStatus status) {
		super(status);
	}
}


