package org.eclipse.jface.text.rules;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * Defines the interface for a rule used in the
 * scanning of text for the purpose of document
 * partitioning or text styling.
 *
 * @see ICharacterScanner
 */
public interface IRule {
	
	/**
	 * Evaluates the rule by examining the characters available from 
	 * the provided character scanner. The token returned by this rule 
	 * returns <code>true</code> when calling <code>isUndefined</code>,
	 * if the text the rule investigated does not match the rule's requirements.
	 *
	 * @param scanner the character scanner to be used by this rule
	 * @return the token computed by the rule
	 */
	IToken evaluate(ICharacterScanner scanner);
}
