package org.eclipse.jface.text.rules;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.jface.text.TextAttribute;


/**
 * @deprecated use DefaultDamagerRepairer
 */
public class RuleBasedDamagerRepairer extends DefaultDamagerRepairer {
		
	/**
	 * Creates a damager/repairer that uses the given scanner and returns the given default 
	 * text attribute if the current token does not carry a text attribute.
	 *
	 * @param scanner the rule based scanner to be used
	 * @param defaultTextAttribute the text attribute to be returned if non is specified by the current token,
	 * 			may not be <code>null</code>
	 * 
	 * @deprecated use RuleBasedDamagerRepairer(RuleBasedScanner) instead
	 */
	public RuleBasedDamagerRepairer(RuleBasedScanner scanner, TextAttribute defaultTextAttribute) {
		super(scanner, defaultTextAttribute);
	}
	
	/**
	 * Creates a damager/repairer that uses the given scanner. The scanner may not be <code>null</code>
	 * and is assumed to return only token that carry text attributes.
	 *
	 * @param scanner the rule based scanner to be used, may not be <code>null</code>
	 */
	public RuleBasedDamagerRepairer(RuleBasedScanner scanner) {
		super(scanner);
	}
}


