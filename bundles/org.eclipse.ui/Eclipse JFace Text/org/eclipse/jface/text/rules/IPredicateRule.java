package org.eclipse.jface.text.rules;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Defines the interface for a rule used in the
 * scanning of text for the purpose of document
 * partitioning or text styling. A predicate rule
 * can only returns a single success token.
 *
 * @see ICharacterScanner
 */
public interface IPredicateRule extends IRule {
	
	/**
	 * Returns the success token of this predicate rule.
	 *
	 * @return the success token of this rule
	 */
	IToken getSuccessToken();
	
	/**
	 * @param scanner the character scanner to be used by this rule
	 * @param resume indicates that the rules starts working between the opening and the closing delimiter
	 * @return the token computed by the rule
	 */
	IToken evaluate(ICharacterScanner scanner, boolean resume);
}
