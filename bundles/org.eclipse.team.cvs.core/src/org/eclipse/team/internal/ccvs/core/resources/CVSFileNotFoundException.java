package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.CVSAuthenticationException;
import org.eclipse.team.internal.ccvs.core.connection.ResourceStatus;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;

/**
 * This exception represents the attemp to access a file/folder
 * that did not exist.
 */
public class CVSFileNotFoundException extends CVSException {
	
	public CVSFileNotFoundException(IStatus status) {
		super(status);
	}
	
	public CVSFileNotFoundException(String message) {
		super(message);
	}
}

