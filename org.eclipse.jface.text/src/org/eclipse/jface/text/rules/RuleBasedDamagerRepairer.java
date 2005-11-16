/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text.rules;


import org.eclipse.jface.text.TextAttribute;


/**
 * @deprecated use <code>DefaultDamagerRepairer</code>
 */
public class RuleBasedDamagerRepairer extends DefaultDamagerRepairer {

	/**
	 * Creates a damager/repairer that uses the given scanner and returns the given default
	 * text attribute if the current token does not carry a text attribute.
	 *
	 * @param scanner the rule based scanner to be used
	 * @param defaultTextAttribute the text attribute to be returned if none is specified by the current token,
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
	 * @since 2.0
	 */
	public RuleBasedDamagerRepairer(RuleBasedScanner scanner) {
		super(scanner);
	}
}


