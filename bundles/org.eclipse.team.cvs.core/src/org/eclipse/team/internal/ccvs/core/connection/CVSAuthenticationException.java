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
package org.eclipse.team.internal.ccvs.core.connection;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;

public class CVSAuthenticationException extends CVSException {

	/**
	 * Creates a new <code>CVSAuthenticationException</code>
	 * 
	 * @param detail  a message that describes the exception in detail.
	 */
	public CVSAuthenticationException(String detail) {
		super(Policy.bind("CVSAuthenticationException.detail", new Object[] { detail })); //$NON-NLS-1$ 
	}
	
	/**
	 * Creates a new <code>CVSAuthenticationException</code>
	 * 
	 * @param cvsroot the cvs server.
	 * @param detail  a message that describes the exception in detail.
	 */
	public CVSAuthenticationException(String cvsroot, String detail) {
		this(detail);
	}
	/**
	 * Creates a new <code>CVSAuthenticationException</code>
	 * 
	 * @param status the status result describing this exception.
	 */
	public CVSAuthenticationException(IStatus status) {
		super(status);
	}
}
