package org.eclipse.update.internal.security;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

/**
 * Result of the service
 */
public class JarVerificationResult {

	public static final int ASK_USER = -1;
	public static final int CANCEL_INSTALL = 0;
	public static final int OK_TO_INSTALL = 1;

	private int resultCode;
	private int verificationCode;
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
	public void setResultException(Exception newResultException) {
		resultException = newResultException;
	}
	/**
	 * Gets the verificationCode.
	 * @return Returns a int
	 */
	public int getVerificationCode() {
		return verificationCode;
	}

	/**
	 * Sets the verificationCode.
	 * @param verificationCode The verificationCode to set
	 */
	public void setVerificationCode(int verificationCode) {
		this.verificationCode = verificationCode;
	}

}