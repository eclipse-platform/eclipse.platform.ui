
package org.eclipse.update.internal.security;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
 import java.security.cert.Certificate;
 
/**
 *
 */
public class CertificatePair {
	private Certificate root;
	private Certificate issuer;
	
	

	/**
	 * Gets the root.
	 * @return Returns a Certificate
	 */
	public Certificate getRoot() {
		return root;
	}

	/**
	 * Sets the root.
	 * @param root The root to set
	 */
	public void setRoot(Certificate root) {
		this.root = root;
	}

	/**
	 * Gets the issuer.
	 * @return Returns a Certificate
	 */
	public Certificate getIssuer() {
		return issuer;
	}

	/**
	 * Sets the issuer.
	 * @param issuer The issuer to set
	 */
	public void setIssuer(Certificate issuer) {
		this.issuer = issuer;
	}

	/*
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		
		if (obj==null) return false;
		
		if (!(obj instanceof CertificatePair)) return false;
		
		if (root==null || issuer==null) return false;
		
		CertificatePair pair = (CertificatePair)obj;
		
		return (root.equals(pair.getRoot()) && issuer.equals(pair.getIssuer()));
	}

}
