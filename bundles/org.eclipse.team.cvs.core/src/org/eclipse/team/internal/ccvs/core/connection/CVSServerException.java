package org.eclipse.team.internal.ccvs.core.connection;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;

/**
 * Client has received an error response from the server. 
 */
public class CVSServerException extends CVSException {
	public CVSServerException(String message) {
		super(new Status(IStatus.ERROR, CVSProviderPlugin.ID, UNABLE, message, null));
	}
}