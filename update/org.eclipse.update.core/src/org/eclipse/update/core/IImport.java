package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * an Import is a requirement on a plug-in with a matching rule
 */
public interface IImport {

	/**
	 * Rules
	 */
	//	perfect | equivalent | compatible | greaterOrEqual
	static final int RULE_NONE 			= 0;
	static final int RULE_PERFECT			= 1;
	static final int RULE_EQUIVALENT 		= 2;
	static final int RULE_COMPATIBLE		= 3;
	static final int RULE_GRATER_OR_EQUAL	= 4;
	/** 
	 * Returns the Identifier of the required plug-in.
	 * 
	 * @return the plug-in Identifier
	 * @since 2.0 
	 */

	VersionedIdentifier getVersionedIdentifier();
	
	/**
	 * Returns a atching rule
	 * 
	 * @return matching rule
	 * @since 2.0 
	 */

	int getRule();
}

