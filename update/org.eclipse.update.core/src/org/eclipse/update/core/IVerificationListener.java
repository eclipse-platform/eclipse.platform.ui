package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

public interface IVerificationListener {
	
	/**
	 * choices
	 */
	// User cancelled
	public static final int CHOICE_ABORT = 0;
	// Error occured, abort 
	public static final int CHOICE_ERROR = 1;	
	// Install for this time
	public static final int CHOICE_INSTALL_TRUST_ONCE = 2;
	//intall and persist info
	public static final int CHOICE_INSTALL_TRUST_ALWAYS = 3;
	
	public int prompt(IVerificationResult notifier);
}
