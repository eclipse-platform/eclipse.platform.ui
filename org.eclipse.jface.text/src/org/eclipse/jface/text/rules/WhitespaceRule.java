/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - [misc] Allow custom token for WhitespaceRule - https://bugs.eclipse.org/bugs/show_bug.cgi?id=251224
 *******************************************************************************/
package org.eclipse.jface.text.rules;


import org.eclipse.core.runtime.Assert;


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
	 * The token returned for whitespace.
	 * @since 3.5
	 */
	protected final IToken fWhitespaceToken;

	/**
	 * Creates a rule which, with the help of an whitespace detector, will return
	 * {@link Token#WHITESPACE} when a whitespace is detected.
	 * 
	 * @param detector the rule's whitespace detector
	 */
	public WhitespaceRule(IWhitespaceDetector detector) {
		this(detector, Token.WHITESPACE);
	}

	/**
	 * Creates a rule which, with the help of an whitespace detector, will return the given
	 * whitespace token when a whitespace is detected.
	 * 
	 * @param detector the rule's whitespace detector
	 * @param token the token returned for whitespace
	 * @since 3.5
	 */
	public WhitespaceRule(IWhitespaceDetector detector, IToken token) {
		Assert.isNotNull(detector);
		Assert.isNotNull(token);
		fDetector= detector;
		fWhitespaceToken= token;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return {@link #fWhitespaceToken} if whitespace got detected, {@link Token#UNDEFINED}
	 *         otherwise
	 */
	public IToken evaluate(ICharacterScanner scanner) {
		int c= scanner.read();
		if (fDetector.isWhitespace((char) c)) {
			do {
				c= scanner.read();
			} while (fDetector.isWhitespace((char) c));
			scanner.unread();
			return fWhitespaceToken;
		}

		scanner.unread();
		return Token.UNDEFINED;
	}
}
