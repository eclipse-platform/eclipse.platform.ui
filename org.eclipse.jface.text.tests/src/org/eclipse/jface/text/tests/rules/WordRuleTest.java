/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests.rules;

import junit.framework.TestCase;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * @since 3.3
 */
public class WordRuleTest extends TestCase {

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=163116
	 */
	public void testBug163116() throws Exception {
		IWordDetector detector= new IWordDetector() {

			public boolean isWordPart(char c) {
				return true;
			}

			public boolean isWordStart(char c) {
				return true;
			}

		};

		WordRule rule= new WordRule(detector, new Token(this));

		RuleBasedScanner scanner= new RuleBasedScanner();
		scanner.setRules(new IRule[] { rule });
		scanner.setRange(new Document(), 0, 0);

		IToken token= null;
		int i= 0;
		while (token != Token.EOF && i++ < 1000)
			token= scanner.nextToken();

		assertTrue(i < 1000);

	}
}
