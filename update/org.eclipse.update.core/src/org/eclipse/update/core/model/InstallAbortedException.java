package org.eclipse.update.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;

/**
 * Exception thrown when the user cancelled an installation.
 * 
 * @see org.eclipse.update.core.Feature#install(IFeature, IVerificationListener, IProgressMonitor)
 * @since 2.0
 */
public class InstallAbortedException extends CoreException {

	/**
	 * Construct the exception indicating enclosing CoreException
	 * 
	 * @since 2.0
	 */
	public InstallAbortedException(String msg,Exception e) {
		super(new Status(IStatus.INFO,"org.eclipse.update.core",IStatus.OK,msg,e));
	}
}