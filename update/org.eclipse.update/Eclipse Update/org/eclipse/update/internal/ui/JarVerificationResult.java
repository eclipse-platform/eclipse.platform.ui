package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Result of the service
 */
public class JarVerificationResult {

	public static final int CANCEL_INSTALL = 0; //default value
	public static final int OK_TO_INSTALL = 1;
	
	private int resultCode;
	private Exception resultException;
/**
 * 
 * @return int
 */
public int getResultCode() {
	return resultCode;
}
/**
 * Can return null if no exception occured
 * @return java.lang.Exception
 */
public Exception getResultException() {
	return resultException;
}
/**
 * 
 * @param newResultCode int
 */
public void setResultCode(int newResultCode) {
	resultCode = newResultCode;
}
/**
 * called by JarVerificationService only
 * @param newResultException java.lang.Exception
 */
/*package*/ void setResultException(Exception newResultException) {
	resultException = newResultException;
}
}
