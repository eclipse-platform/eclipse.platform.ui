package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


/**
 * For internal use only. Not API. <p>
 * A find replace target extension is for extending
 * <code>IFindReplaceTarget</code> instances with new functionality.
 */
public interface IFindReplaceTargetExtension {
	
	/**
	 * Indicates that a session with the target begins.
	 */
	void beginSession();

	/**
	 * Indicates that a session with the target ends.
	 */
	void endSession();
	
}
