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
	static final int RULE_NONE 				= 0;
	static final int RULE_EQUAL 				= 1;
	static final int RULE_EQUIVALENT 	= 2;
	static final int RULE_COMPATIBLE	= 3;
	static final int RULE_HIGER				= 4;

	/** 
	 * Returns the Identifier of the required plug-in.
	 * 
	 * @return the plug-in Identifier
	 */
	VersionedIdentifier getIdentifier();
	
	/**
	 * Returns a atching rule
	 * 
	 * @return matching rule
	 */
	int getRule();
}

