package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

public interface IVerificationResult {
	
	/**
	 * types
	 */
	public static final int TYPE_FEATURE_NOT_SIGNED = 0;
	public static final int TYPE_FEATURE_CORRUPTED = 1;
	public static final int TYPE_FEATURE_SIGNED_RECOGNIZED = 2;
	public static final int TYPE_FEATURE_SIGNED_UNRECOGNIZED = 3;
	public static final int TYPE_ENTRY_NOT_SIGNED = 4;
	public static final int TYPE_ENTRY_CORRUPTED = 5;
	public static final int TYPE_ENTRY_SIGNED_RECOGNIZED = 6;
	public static final int TYPE_ENTRY_SIGNED_UNRECOGNIZED = 7;
	// FIXME: maybe we need more/ others
	
	/**
	 * Sets the type of notification. 
	 * @see IVerificationNotifier#TYPE_FEATURE_NOT_SIGNED
	 * @see IVerificationNotifier#TYPE_FEATURE_CORRUPTED
	 * @see IVerificationNotifier#TYPE_FEATURE_SIGNED_RECOGNIZED
	 * @see IVerificationNotifier#TYPE_FEATURE_SIGNED_UNRECOGNIZED
	 * @see IVerificationNotifier#TYPE_ENTRY_NOT_SIGNED
	 * @see IVerificationNotifier#TYPE_ENTRY_CORRUPTED
	 * @see IVerificationNotifier#TYPE_ENTRY_SIGNED_RECOGNIZED
	 * @see IVerificationNotifier#TYPE_ENTRY_SIGNED_UNRECOGNIZED
	 */
	public void setType(int type);
	
	/**
	 * Sets the notifier text. If no text is supplied, default
	 * text may be generated based on the notification type
	 */
	public void setText(String text);
	
	/**
	 * Sets the signer info text
	 */
	public void setSignerInfo(String text);
	
	/**
	 * Sets the verifier (eg. CA) info text
	 */
	public void setVerifierInfo(String text);
	
	/**
	 * Sets the element identifier (based on type)
	 */
	public void setIdentifier(String text);
	
	/**
	 * Sets the element provider 
	 */
	public void setProvider(String text);
	
	/**
	 * Sets the element file name 
	 */
	public void setFileName(String text);

}
