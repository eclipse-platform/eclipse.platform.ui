package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

public interface IVerificationResult {
	
	/**
	 * verification codes
	 */
	static final int TYPE_ENTRY_NOT_SIGNED = 1;
	static final int TYPE_ENTRY_CORRUPTED = 2;
	static final int TYPE_ENTRY_SIGNED_RECOGNIZED = 3;
	static final int TYPE_ENTRY_ALREADY_ACCEPTED = 4;	
	static final int TYPE_ENTRY_SIGNED_UNRECOGNIZED = 5;
	
	static final int UNKNOWN_ERROR = 6;
	static final int VERIFICATION_CANCELLED = 7;
	static final int TYPE_ENTRY_UNRECOGNIZED = 8;
		
	

	
	/**
	 * Returns the content reference that is verified 
	 */
	public ContentReference getContentReference();

	
	/**
	 * Returns the feature
	 */
	public IFeature getFeature();
	
	
	/**
	 * Returns the type of notification. 
	 * @see IVerificationNotifier#TYPE_FEATURE_NOT_SIGNED
	 * @see IVerificationNotifier#TYPE_FEATURE_CORRUPTED
	 * @see IVerificationNotifier#TYPE_FEATURE_SIGNED_RECOGNIZED
	 * @see IVerificationNotifier#TYPE_FEATURE_SIGNED_UNRECOGNIZED
	 * @see IVerificationNotifier#TYPE_FEATURE_NOT_SIGNED
	 * @see IVerificationNotifier#TYPE_FEATURE_CORRUPTED
	 * @see IVerificationNotifier#TYPE_FEATURE_SIGNED_RECOGNIZED
	 * @see IVerificationNotifier#TYPE_FEATURE_SIGNED_UNRECOGNIZED
	 */
	public int getVerificationCode();

	
	
		
	/**
	 * Returns the notifier text. If no text is supplied, default
	 * text may be generated based on the notification type
	 */
	public String getText();
		
		
	/**
	 * Returns the signer info text
	 */
	public String getSignerInfo();
		
	
	/**
	 * Returns the verifier (eg. CA) info text
	 */
	public String getVerifierInfo();

	
	
	/**
	 * 		
	 */
	public boolean isFeatureVerification();

		
	/**
	 * Sets the type of notification. 
	 * @see IVerificationNotifier#TYPE_FEATURE_NOT_SIGNED
	 * @see IVerificationNotifier#TYPE_FEATURE_CORRUPTED
	 * @see IVerificationNotifier#TYPE_FEATURE_SIGNED_RECOGNIZED
	 * @see IVerificationNotifier#TYPE_FEATURE_SIGNED_UNRECOGNIZED
	 * @see IVerificationNotifier#TYPE_FEATURE_NOT_SIGNED
	 * @see IVerificationNotifier#TYPE_FEATURE_CORRUPTED
	 * @see IVerificationNotifier#TYPE_FEATURE_SIGNED_RECOGNIZED
	 * @see IVerificationNotifier#TYPE_FEATURE_SIGNED_UNRECOGNIZED
	 */
	public void setVerificationCode(int type);

	/**
	 * 		
	 */
	public void isFeatureVerification(boolean verification);
		
	/**
	 * @deprecated will be removed
	 */ 
	public int getResultCode();	
	
		
	/**
	 * Returns the exception that may have occured while verifying
	 */
	public Exception getResultException();


	/**
	 * Sets the result code
	 * @see IVerificationNotifier#CANCEL_INSTALL
	 * @see IVerificationNotifier#ERROR_INSTALL
	 * @see IVerificationNotifier#static final int OK_TO_INSTALL
	 */ 
	public void setResultCode(int code);
	
	/**
	 * Sets the exception that may have occured while verifying
	 */
	public void setResultException(Exception ex);


}
