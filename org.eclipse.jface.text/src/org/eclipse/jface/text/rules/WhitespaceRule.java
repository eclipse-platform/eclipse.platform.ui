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


import org.eclipse.jface.text.Assert;


/**
 * An implementation of <code>IRule</code> capable of detecting whitespace.
 * A whitespace rule uses a whitespace detector in order to find out which
 * characters are whitespace characters.
 *
 * @see IWhitespaceDetector
 */
public class WhitespaceRule implements IRule {

	/** The whitespace detector used by this rule */
	protected IWhitespaceDetector fDetector;

	/**
	 * Creates a rule which, with the help of an
	 * whitespace detector, will return a whitespace
	 * token when a whitespace is detected.
	 *
	 * @param detector the rule's whitespace detector, may not be <code>null</code>
	 */
	public WhitespaceRule(IWhitespaceDetector detector) {
		Assert.isNotNull(detector);
		fDetector= detector;
	}

	/*
	 * @see IRule#evaluate(ICharacterScanner)
	 */
	public IToken evaluate(ICharacterScanner scanner) {
		int c= scanner.read();
		if (fDetector.isWhitespace((char) c)) {
			do {
				c= scanner.read();
			} while (fDetector.isWhitespace((char) c));
			scanner.unread();
			return Token.WHITESPACE;
		}

		scanner.unread();
		return Token.UNDEFINED;
	}
}
