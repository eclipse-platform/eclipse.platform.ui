/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.core.model;

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
