package org.eclipse.update.internal.security;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.security.Principal;
import java.security.cert.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.Policy;
import org.eclipse.update.internal.core.UpdateManagerPlugin;
import sun.security.x509.X500Name;

/**
 * Result of the service
 */
public class JarVerificationResult implements IVerificationResult {


	private int resultCode;
	private int verificationCode;
	private Exception resultException;
	private List /*of Certificates[] */
	certificates;
	private CertificatePair[] rootCertificates;
	private CertificatePair foundCertificate; // certificate found in one keystore
	
	private String signerInfo;
	private String verifierInfo;
	private ContentReference contentReference;
	private String text;
	private IFeature feature;
	private boolean featureVerification;

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
	 * 
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
	private List getCertificates() {
		return certificates;
	}

	/**
	 * Assume certifcates are x.509
	 */
	public CertificatePair[] getRootCertificates() {
		if (rootCertificates == null) {
			rootCertificates = new CertificatePair[0];			
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
							X509Certificate x509certIssuer = (X509Certificate) certs[i+1];
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
				rootCertificates = new CertificatePair[rootCertificatesList.size()];				
				rootCertificatesList.toArray(rootCertificates);
			}
		}
		return rootCertificates;
	}

	/**
	 * Gets the foundCertificate.
	 * @return Returns a Certificate
	 */
	private CertificatePair getFoundCertificate() {
		return foundCertificate;
	}

	/**
	 * Sets the foundCertificate.
	 * @param foundCertificate The foundCertificate to set
	 */
	public void setFoundCertificate(CertificatePair foundCertificate) {
		this.foundCertificate = foundCertificate;
	}


	private void initializeCertificates(){
		X509Certificate certRoot = null;
		X509Certificate certIssuer = null;
		CertificatePair trustedCertificate;
		if (getFoundCertificate() == null) {
			CertificatePair[] certs = getRootCertificates();
			if (certs.length == 0)
				return;
			trustedCertificate = (CertificatePair) certs[0];
		} else {
			trustedCertificate = (CertificatePair) getFoundCertificate();
		}
		certRoot = (X509Certificate) trustedCertificate.getRoot();
		certIssuer = (X509Certificate) trustedCertificate.getIssuer();

		StringBuffer strb = new StringBuffer();
		strb.append(Policy.bind("JarVerificationResult.SubjectCA")); //$NON-NLS-1$
		strb.append("\r\n"); //$NON-NLS-1$
		strb.append(Policy.bind("JarVerificationResult.CAIssuer", issuerString(certIssuer.getSubjectDN()))); //$NON-NLS-1$
		strb.append("\r\n"); //$NON-NLS-1$
		strb.append(Policy.bind("JarVerificationResult.ValidBetween", dateString(certIssuer.getNotBefore()), dateString(certIssuer.getNotAfter()))); //$NON-NLS-1$
		strb.append(checkValidity(certIssuer));
		signerInfo = strb.toString();
		if (certIssuer != null && !certIssuer.equals(certRoot)) {
			strb = new StringBuffer();			
			strb.append(Policy.bind("JarVerificationResult.RootCA")); //$NON-NLS-1$
			strb.append("\r\n"); //$NON-NLS-1$
			strb.append(Policy.bind("JarVerificationResult.CAIssuer", issuerString(certIssuer.getIssuerDN()))); //$NON-NLS-1$
			strb.append("\r\n"); //$NON-NLS-1$
			strb.append(Policy.bind("JarVerificationResult.ValidBetween", dateString(certRoot.getNotBefore()), dateString(certRoot.getNotAfter()))); //$NON-NLS-1$ 
			strb.append(checkValidity(certRoot));
			verifierInfo = strb.toString();
		}

	}

	private String checkValidity(X509Certificate cert) {

		try {
			cert.checkValidity();
		} catch (CertificateExpiredException e) {
			return ("\r\n" + Policy.bind("JarVerificationResult.ExpiredCertificate")); //$NON-NLS-1$ 
		} catch (CertificateNotYetValidException e) {
			return ("\r\n" + Policy.bind("JarVerificationResult.CertificateNotYetValid")); //$NON-NLS-1$ 
		}
		return ("\r\n" + Policy.bind("JarVerificationResult.CertificateValid")); //$NON-NLS-1$
	}

	/**
	 * 
	 */
	private String issuerString(Principal principal) {
		try {
			if (principal instanceof X500Name) {
				String issuerString = "";
				X500Name name = (X500Name) principal;
				issuerString += (name.getDNQualifier() != null) ? name.getDNQualifier() + ",1" : "";
				issuerString += name.getCommonName();
				issuerString += (name.getOrganizationalUnit() != null) ? "," + name.getOrganizationalUnit() : "";
				issuerString += (name.getOrganization() != null) ? "," + name.getOrganization() : "";
				issuerString += (name.getLocality() != null) ? "," + name.getLocality() : "";
				issuerString += (name.getCountry() != null) ? "," + name.getCountry() : "";
				return issuerString;
			}
		} catch (Exception e) {
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS){
				IStatus status = Utilities.newCoreException("Error parsing X500 Certificate",e).getStatus();
				UpdateManagerPlugin.getPlugin().getLog().log(status);
			}
		}
		return principal.toString();
	}

	/**
	 * 
	 */
	private String dateString(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("EEE, MMM d, yyyyy");
		return formatter.format(date);
	}

	/*
	 * @see IVerificationResult#getSignerInfo()
	 */
	public String getSignerInfo() {
		if (signerInfo==null) initializeCertificates();
		return signerInfo;
	}

	/*
	 * @see IVerificationResult#getVerifierInfo()
	 */
	public String getVerifierInfo() {
		if (signerInfo==null) initializeCertificates();		
		return verifierInfo;
	}

	/*
	 * @see IVerificationResult#getContentReference()
	 */
	public ContentReference getContentReference() {
		return contentReference;
	}

	/*
	 * @see IVerificationResult#getFeature()
	 */
	public IFeature getFeature() {
		return feature;
	}

	/**
	 * 
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * 
	 */
	public void setContentReference(ContentReference ref) {
		this.contentReference = ref;
	}

	/**
	 * 
	 */
	public void setFeature(IFeature feature) {
		this.feature = feature;
	}

	/*
	 * @see IVerificationResult#getText()
	 */
	public String getText() {
		return null;
	}

	/**
	 * Sets the featureVerification.
	 * @param featureVerification The featureVerification to set
	 */
	public void isFeatureVerification(boolean featureVerification) {
		this.featureVerification = featureVerification;
	}

	/**
	 * Gets the featureVerification.
	 * @return Returns a boolean
	 */
	public boolean isFeatureVerification() {
		return featureVerification;
	}

}