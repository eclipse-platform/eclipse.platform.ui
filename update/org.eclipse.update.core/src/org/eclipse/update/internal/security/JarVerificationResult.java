package org.eclipse.update.internal.security;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Result of the service
 */
public class JarVerificationResult {

	public static final int ASK_USER = -1;
	public static final int CANCEL_INSTALL = 0;
	public static final int ERROR_INSTALL = 1;
	public static final int OK_TO_INSTALL = 2;

	private int resultCode;
	private int verificationCode;
	private Exception resultException;
	private List /*of Certificates[] */
	certificates;
	private CertificatePair[] rootCertificates;
	private CertificatePair foundCertificate; // certificate found in one keystore

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
	public void addCertificates(Certificate[] certs) {
		if (certificates == null)
			certificates = new ArrayList();
		certificates.add(certs);
		rootCertificates = null;
	}

	/**
	 * Gets the certificates.
	 * @return Returns a List
	 */
	public List getCertificates() {
		return certificates;
	}

	/**
	 * Assume certifcates are x.509
	 */
	public CertificatePair[] getRootCertificates() {
		if (rootCertificates == null) {
			List rootCertificatesList = new ArrayList();
			if (certificates != null && certificates.size() > 0) {
				Iterator iter = certificates.iterator();
				while (iter.hasNext()) {

					Certificate[] certs = (Certificate[]) iter.next();
					if (certs != null && certs.length > 0) {

						CertificatePair pair = new CertificatePair();
						pair.setIssuer(certs[0]);

						for (int i = 0; i < certs.length - 1; i++) {
							X509Certificate x509certRoot = (X509Certificate) certs[i];
							X509Certificate x509certIssuer = (X509Certificate) certs[i];
							if (!x509certRoot.getIssuerDN().equals(x509certIssuer.getSubjectDN())) {
								pair.setRoot(x509certRoot);
								if (!rootCertificatesList.contains(pair)) {
									rootCertificatesList.add(pair);
								}
								pair = new CertificatePair();
								pair.setIssuer(x509certIssuer);
							}
						}

						// add the latest one
						if (pair != null) {
							pair.setRoot(certs[certs.length - 1]);
							if (!rootCertificatesList.contains(pair)) {
								rootCertificatesList.add(pair);
							}
						}
					}
				}

			}

			if (rootCertificatesList.size() > 0) {
				rootCertificates = new CertificatePair[certificates.size()];
				rootCertificatesList.toArray(rootCertificates);
			}
		}
		return rootCertificates;
	}

	/**
	 * Gets the foundCertificate.
	 * @return Returns a Certificate
	 */
	public CertificatePair getFoundCertificate() {
		return foundCertificate;
	}

	/**
	 * Sets the foundCertificate.
	 * @param foundCertificate The foundCertificate to set
	 */
	public void setFoundCertificate(CertificatePair foundCertificate) {
		this.foundCertificate = foundCertificate;
	}

}