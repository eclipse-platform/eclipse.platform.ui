package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Result of the service
 */
public class JarVerificationResult {

	public static final int CANCEL_INSTALL = 0;
	public static final int OK_TO_INSTALL = 1;

	private int resultCode;
	private Exception resultException;
	/**
	 */
	public int getResultCode() {
		return resultCode;
	}
	/**
	 */
	public Exception getResultException() {
		return resultException;
	}
	/**
	 */
	public void setResultCode(int newResultCode) {
		resultCode = newResultCode;
	}
	/**
	 * called by JarVerificationService only
	 */
	void setResultException(Exception newResultException) {
		resultException = newResultException;
	}
}