package org.eclipse.update.internal.security;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.security.cert.Certificate;
import java.util.List;


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
	private List /*of Certificates[] */ certificates;
	
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
	
	/**
	 * adds an array of Certificates
	 */
	public void addCertificates(Certificate[] certs){
		certificates.add(certs);
	}

	/**
	 * Gets the certificates.
	 * @return Returns a List
	 */
	public List getCertificates() {
		return certificates;
	}

	
}