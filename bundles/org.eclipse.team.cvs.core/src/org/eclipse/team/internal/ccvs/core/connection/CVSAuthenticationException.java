/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;

public class CVSAuthenticationException extends CVSException {

	private static final long serialVersionUID = 1L;

    /**
	 * Code indicating that authentication can be retried after 
	 * prompting the user for corrected authentication information
	 */
	public static final int RETRY = 1;
	
	/**
	 * Code indicating that authentication should not be reattempted.
	 */
	public static final int NO_RETRY = 2;
	
	/**
	 * Creates a new <code>CVSAuthenticationException</code>
	 * 
	 * @param detail  a message that describes the exception in detail.
	 * @param code indicates whether authentication can be retried or not
	 */
	public CVSAuthenticationException(String detail, int code) {
		this(new CVSStatus(IStatus.ERROR, code, 
			Policy.bind("CVSAuthenticationException.detail", new Object[] { detail }))); //$NON-NLS-1$ 
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
