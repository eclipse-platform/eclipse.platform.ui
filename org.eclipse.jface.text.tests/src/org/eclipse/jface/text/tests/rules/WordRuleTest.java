/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests.rules;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.eclipse.swt.SWT;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.PatternRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * @since 3.3
 */
public class WordRuleTest {


	private static class SimpleWordDetector implements IWordDetector {
		@Override
		public boolean isWordStart(char c) {
			return !Character.isWhitespace(c);
		}

		@Override
		public boolean isWordPart(char c) {
			return !Character.isWhitespace(c);
		}
	}


	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=163116
	 */
	@Test
	public void testBug163116() throws Exception {
		IWordDetector detector= new IWordDetector() {

			@Override
			public boolean isWordPart(char c) {
				return true;
			}

			@Override
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

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=144355
	 */
	@Test
	public void testBug144355() throws Exception {
		IWordDetector detector= new SimpleWordDetector();

		String defaultTokenString= "defaultToken";
		Token defaultToken= new Token(defaultTokenString);

		String testTokenStringNormal= "TestTokenString";
		String testTokenStringDifferentCapitalization= "TestTOKENString";
		String testTokenStringCompletelyDifferent= "XXX";
		Token normalToken= new Token(testTokenStringNormal);

		WordRule rule= new WordRule(detector, defaultToken, true);
		rule.addWord(testTokenStringNormal, normalToken);

		// scenario 1
		// pre: pass in a normal string ("TestTokenString")
		// post: expect the normal token to be returned
		RuleBasedScanner scanner= new RuleBasedScanner();
		scanner.setRules(new IRule[] {rule});
		scanner.setRange(new Document(testTokenStringNormal), 0, testTokenStringNormal.length());
		assertTrue(scanner.nextToken().getData().equals(testTokenStringNormal));

		// scenario 2
		// pre: pass in a normal string but different capitalization ("TestTOKENString")
		// post: expect the normal token to be returned
		scanner= new RuleBasedScanner();
		scanner.setRules(new IRule[] {rule});
		scanner.setRange(new Document(testTokenStringDifferentCapitalization), 0, testTokenStringDifferentCapitalization.length());
		assertTrue(scanner.nextToken().getData().equals(testTokenStringNormal));

		// scenario 3
		// pre: pass in a completely different string ("XXX")
		// post: expect the default token to be returned because the string can't be matched
		scanner= new RuleBasedScanner();
		scanner.setRules(new IRule[] {rule});
		scanner.setRange(new Document(testTokenStringCompletelyDifferent), 0, testTokenStringCompletelyDifferent.length());
		assertTrue(scanner.nextToken().getData().equals(defaultTokenString));

		WordRule ruleWithoutIgnoreCase= new WordRule(detector, defaultToken);
		ruleWithoutIgnoreCase.addWord(testTokenStringNormal, normalToken);

		// scenario 4
		// pre: pass in a normal string ("TestTokenString")
		// post: expect the normal token to be returned
		scanner= new RuleBasedScanner();
		scanner.setRules(new IRule[] {ruleWithoutIgnoreCase});
		scanner.setRange(new Document(testTokenStringNormal), 0, testTokenStringNormal.length());
		assertTrue(scanner.nextToken().getData().equals(testTokenStringNormal));

		// scenario 5
		// pre: pass in a normal string but different capitalization ("TestTOKENString")
		// post: expect the default token to be returned
		scanner= new RuleBasedScanner();
		scanner.setRules(new IRule[] {ruleWithoutIgnoreCase});
		scanner.setRange(new Document(testTokenStringDifferentCapitalization), 0, testTokenStringDifferentCapitalization.length());
		assertTrue(scanner.nextToken().getData().equals(defaultTokenString));
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=175712
	@Test
	public void testBug175712_1() throws Exception {
		IRule[] rules= new IRule[2];

		IToken stepToken= new Token(new TextAttribute(null, null, SWT.BOLD));
		PatternRule stepRule= new PatternRule("(((", ")", stepToken, (char) 0,false);
		stepRule.setColumnConstraint(-1);
		rules[1]= stepRule;

		IToken titleToken= new Token(new TextAttribute(null, null, SWT.BOLD));
		WordRule wordRule= new WordRule(new SimpleWordDetector());
		wordRule.addWord("((", titleToken);
		rules[0]= wordRule;

		IDocument document= new Document("((( \n((\n- Cheese\n- Wine");
		RuleBasedScanner scanner= new RuleBasedScanner();
		scanner.setRules(rules);
		scanner.setRange(document, 0, document.getLength());

		IToken defaultToken= new Token(this);
		scanner.setDefaultReturnToken(defaultToken);

		IToken token= scanner.nextToken();
		assertSame(defaultToken, token);

		token= scanner.nextToken();
		assertSame(defaultToken, token);

		token= scanner.nextToken();
		assertSame(defaultToken, token);

		token= scanner.nextToken();
		assertSame(titleToken, token);

	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=175712
	@Test
	public void testBug175712_2() throws Exception {
		IRule[] rules= new IRule[2];

		IToken stepToken= new Token(new TextAttribute(null, null, SWT.BOLD));
		PatternRule stepRule= new PatternRule("(((", ")", stepToken, (char) 0,false);
		stepRule.setColumnConstraint(-1);
		rules[1]= stepRule;

		IToken titleToken= new Token(new TextAttribute(null, null, SWT.BOLD));
		WordRule wordRule= new WordRule(new SimpleWordDetector());
		wordRule.addWord("((", titleToken);
		rules[0]= wordRule;

		IDocument document= new Document("((\n((\n- Cheese\n- Wine");
		RuleBasedScanner scanner= new RuleBasedScanner();
		scanner.setRules(rules);
		scanner.setRange(document, 0, document.getLength());

		IToken defaultToken= new Token(this);
		scanner.setDefaultReturnToken(defaultToken);

		IToken token= scanner.nextToken();
		assertSame(titleToken, token);

	}


}
