package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
/**
 * Verification result. An object implementing this interface represents
 * a token passed between the update framework and the verifier and verification
 * listener. The verifier uses the token to capture the result of the file
 * verification. It is then passed to the verification listener
 * to optionally present this information to the user (in an 
 * implementation-specific way). The verification listener in turn
 * uses this token to capture the desired response.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see 
 * @since 2.0
 */
public interface IVerificationResult {
	
	/**
	 * Indicates the file is not signed
	 * 
	 * @since 2.0
	 */
	public static final int TYPE_ENTRY_NOT_SIGNED = 1;
	
	/**
	 * Indicates the file has been modified since it was signed
	 * 
	 * @since 2.0
	 */
	public static final int TYPE_ENTRY_CORRUPTED = 2;
	
	/**
	 * Indicates the file was signed by a recognized signer
	 * 
	 * @since 2.0
	 */
	public static final int TYPE_ENTRY_SIGNED_RECOGNIZED = 3;
	
	/**
	 * Indicates the file was signed by a recognized signer and the user
	 * previously indicated the signer should be truested
	 * 
	 * @since 2.0
	 */
	public static final int TYPE_ENTRY_ALREADY_ACCEPTED = 4;	
	
	/**
	 * Indicates the file was signed byt the signer is not recognized
	 * 
	 * @since 2.0
	 */
	public static final int TYPE_ENTRY_SIGNED_UNRECOGNIZED = 5;
	
	/**
	 * Error occurred during verification
	 * 
	 * @since 2.0
	 */	
	public static final int UNKNOWN_ERROR = 6;
	
	/**
	 * Verification was cancelled
	 * 
	 * @since 2.0
	 */
	public static final int VERIFICATION_CANCELLED = 7;
	
	/**
	 * Verification failed due to unrecognized file
	 * 
	 * @since 2.0
	 */
	public static final int TYPE_ENTRY_UNRECOGNIZED = 8;
		
	/**
	 * Returns the content reference that is the target of the verification.
	 * 
	 * @return content reference
	 * @since 2.0
	 */
	public ContentReference getContentReference();
	
	/**
	 * Returns the feature the referenced file is part of.
	 * 
	 * @return feature
	 * @since 2.0
	 */
	public IFeature getFeature();
		
	/**
	 * Returns the verification code.
	 * 
	 * @return verification code, as defined in this interface.
	 * @since 2.0
	 */
	public int getVerificationCode();
	
	/**
	 * Returns display text describing the result of the verification.
	 * 
	 * @return result text, or <code>null</code>.
	 * @since 2.0
	 */
	public String getText();
				
	/**
	 * Returns text describing the signer
	 * 
	 * @return signer information, or <code>null</code>.
	 * @since 2.0
	 */
	public String getSignerInfo();
			
	/**
	 * Returns text describing the authority that verified/ certified 
	 * the signer
	 * 
	 * @return verifier information, or <code>null</code>.
	 * @since 2.0
	 */
	public String getVerifierInfo();

	/**
	 * Indicates whether the referenced file is part of the overall feature
	 * definition, or one of its component plug-in or non-plug-in entries.
	 * 
	 * @return <code>true</code> if reference is a feature file, 
	 * <code>false</code> if reference is a plug-in or non-plug-in file
	 * @since 2.0
	 */
	public boolean isFeatureVerification();

		
	/**
	 * Sets the verification result code.
	 * 
	 * @param type verification code, as defined in this interface
	 * @since 2.0
	 */
	public void setVerificationCode(int type);

	/**
	 * Sets the reference type for this verification.
	 * 
	 * @param isFeature <code>true</code> if reference should be verified
	 * as a feature file, <code>false</code> if reference should be verified
	 * as a plug-in or non-plug-in file
	 * @since 2.0
	 */
	public void isFeatureVerification(boolean isFeature);
		
	/**
	 * Get the response from the verification listener
	 * 
	 * @deprecated TBD
	 * @return response code, @see IVerificationListener
	 * @since 2.0
	 */ 
	public int getResultCode();	
	
		
	/**
	 * Returns an exception caught during verification
	 * 
	 * @return exception,or <code>null</code>.
	 * @since 2.0
	 */
	public Exception getResultException();


	/**
	 * Sets the verification response code
	 * 
	 * @param code response code, @see IVerificationListener
	 * @since 2.0
	 */ 
	public void setResultCode(int code);
	
	/**
	 * Sets the exception caught during verification
	 * 
	 * @param ex exception
	 * @since 2.0
	 */
	public void setResultException(Exception ex);
}
