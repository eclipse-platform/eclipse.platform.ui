package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

public interface IVerificationListener {
	
	/**
	 * choices
	 */
	public static final int CHOICE_ABORT = 0;
	public static final int CHOICE_INSTALL_TRUST_ONCE = 1;
	public static final int CHOICE_INSTALL_TRUST_ALWAYS = 2;
	
	public int prompt(IVerificationResult notifier);
}
