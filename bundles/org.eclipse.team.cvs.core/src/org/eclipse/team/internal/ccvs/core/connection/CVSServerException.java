package org.eclipse.team.internal.ccvs.core.connection;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSStatus;

/**
 * Client has received an error response from the server. 
 */
public class CVSServerException extends CVSException {
	
	/**
	 * Return true if the exception from the cvs server is the no tag error, and false
	 * otherwise.
	 */
	public boolean isNoTagException() {
		IStatus status = getStatus();
		if ( ! status.isMultiStatus())
			return false;
		IStatus[] children = ((MultiStatus)status).getChildren();
		if (children.length != 1)
			return false;
		if (children[0].getCode() == CVSStatus.NO_SUCH_TAG)
			return true;
		return false;
	}
	
	/**
	 * Return true if the exceptions status contains any error status messages
	 */
	public boolean containsErrors() {
		IStatus status = getStatus();
		if ( ! status.isMultiStatus())
			return status.getSeverity() == IStatus.ERROR;
		IStatus[] children = ((MultiStatus)status).getChildren();
		for (int i=0;i<children.length;i++) {
			if (children[i].getSeverity() == IStatus.ERROR)
				return true;
		}
		return false;
	}
	
	/**
	 * Return the CVSServerException for the given error message and error list
	 * 
	 * This is public due to packaging and should not be used by clients.
	 */	
	public static CVSServerException forError(String message, IStatus[] children) {
		if (children.length > 0) {
			return new CVSServerException(message, children);
		} else {
			return new CVSServerException(new CVSStatus(IStatus.ERROR, CVSStatus.SERVER_ERROR, message, null));
		}
	}
	
	public CVSServerException(IStatus status) {
		super(status);
	}
	
	private CVSServerException(String message, IStatus[] children) {
		super(new MultiStatus(CVSProviderPlugin.ID, CVSStatus.SERVER_ERROR, children, message, null));
	}
}