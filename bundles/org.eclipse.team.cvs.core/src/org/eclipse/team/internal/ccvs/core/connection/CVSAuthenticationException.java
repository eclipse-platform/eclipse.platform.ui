package org.eclipse.team.internal.ccvs.core.connection;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
		super(
			Policy.bind("CVSAuthenticationException.detail", new Object[] { detail }),
			null,
			null);
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
	/**
	 * Creates a new <code>CVSAuthenticationException</code>
	 * 
	 * @param cvsroot the cvs server.
	 * @param throwable the exception that has caused the authentication
	 *  failure.
	 */
	public CVSAuthenticationException(String cvsroot, Throwable throwable) {
		super(
			Policy.bind("CVSAuthenticationException.normal", new Object[] { cvsroot }),
			null,
			null);
	}
}