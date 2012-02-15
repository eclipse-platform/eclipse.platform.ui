/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Plesner Hansen (plesner@quenta.org) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextStore;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcherExtension;

/**
 * Generic test of simple character pair matchers
 *
 * @since 3.3
 */
public abstract class AbstractPairMatcherTest extends TestCase {

	private final boolean fCaretInsideMatchedPair;

	public AbstractPairMatcherTest(boolean caretInsideMatchedPair) {
		fCaretInsideMatchedPair= caretInsideMatchedPair;
	}

	/**
	 * Constructs a new character pair matcher.
	 * 
	 * @param chars the characters to match
	 * @return the character pair matcher
	 */
	protected ICharacterPairMatcher createMatcher(final String chars) {
		return new DefaultCharacterPairMatcher(chars.toCharArray(), getDocumentPartitioning(), fCaretInsideMatchedPair);
	}

	/**
	 * Returns the partitioning treated by the matcher.
	 * 
	 * @return the partition
	 */
	protected String getDocumentPartitioning() {
		return IDocumentExtension3.DEFAULT_PARTITIONING;
	}

	/* --- T e s t s --- */

	/** Tests that the test case reader works */
	public void testTestCaseReader() {
		performReaderTest("%( )#", 0, 3, "( )");
		performReaderTest("#%", 0, 0, "");
	}

	/**
	 * Very simple checks.
	 * 
	 * @throws BadLocationException
	 */
	public void testSimpleMatchSameMatcher() throws BadLocationException {
		final ICharacterPairMatcher matcher= createMatcher("()[]{}");
		performMatch(matcher, "#(   )%");
		performMatch(matcher, "#[   ]%");
		performMatch(matcher, "#{   }%");
		performMatch(matcher, "(%   )#");
		performMatch(matcher, "[%   ]#");
		performMatch(matcher, "{%   }#");

		performMatch(matcher, "#(  %  )#");
		performMatch(matcher, "#[  %  ]#");
		performMatch(matcher, "#{  %  }#");

		matcher.dispose();
	}

	/**
	 * Very simple checks.
	 * 
	 * @throws BadLocationException
	 */
	public void testSimpleMatchDifferentMatchers() throws BadLocationException {
		performMatch("()[]{}", "#(   )%");
		performMatch("()[]{}", "#[   ]%");
		performMatch("()[]{}", "#{   }%");
		performMatch("()[]{}", "(%   )#");
		performMatch("()[]{}", "[%   ]#");
		performMatch("()[]{}", "{%   }#");

		performMatch("()[]{}", "#(  %  )#");
		performMatch("()[]{}", "#[  %  ]#");
		performMatch("()[]{}", "#{  %  }#");
	}

	/**
	 * Close matches.
	 * 
	 * @throws BadLocationException
	 */
	public void testCloseMatches() throws BadLocationException {
		final ICharacterPairMatcher matcher= createMatcher("()[]{}");
		performMatch(matcher, "(%)#");
		performMatch(matcher, "#(())%");
		performMatch(matcher, "(%())#");
		performMatch(matcher, "((%)#)");

		performMatch(matcher, "#(%)#");
		performMatch(matcher, "#(%())#");

		matcher.dispose();
	}


	/**
	 * Checks of simple situations where no matches should be found.
	 * 
	 * @throws BadLocationException
	 */
	public void testIncompleteMatch() throws BadLocationException {
		final ICharacterPairMatcher matcher= createMatcher("()[]{}");
		performMatch(matcher, "(% ");
		performMatch(matcher, "%(  )");
		performMatch(matcher, "( % )");
		performMatch(matcher, "%");
		matcher.dispose();
	}

	/**
	 * Test that it doesn't match across different partitions.
	 * 
	 * @throws BadLocationException
	 */
	public void testPartitioned() throws BadLocationException {
		final ICharacterPairMatcher matcher= createMatcher("()[]{}");
		performMatch(matcher, "(% |a a| )#");
		performMatch(matcher, "#( |a a| )%");
		performMatch(matcher, "|b #( )% b|");
		performMatch(matcher, "( |b )% b|");
		performMatch(matcher, "(% |b ) b|");
		performMatch(matcher, "|a ( a| )%");
		performMatch(matcher, "|a (% a| )");
		performMatch(matcher, "|c #( c| ) ( |c )% c|");
		performMatch(matcher, "|c (% c| ) ( |c )# c|");
		performMatch(matcher, "(% |a ) a| |b ) b| |c ) c| )#");

		performMatch(matcher, "#( % |a a| )#");
		performMatch(matcher, "|b #( % )# b|");
		performMatch(matcher, "|c #( % c| ) ( |c )# c|");
		performMatch(matcher, "|c #( c| ) ( |c % )# c|");
		performMatch(matcher, "#( % |a ) a| |b ) b| |c ) c| )#");

		matcher.dispose();
	}

	/**
	 * Test that it works properly next to partition boundaries.
	 * 
	 * @throws BadLocationException
	 */
	public void testTightPartitioned() throws BadLocationException {
		final ICharacterPairMatcher matcher= createMatcher("()[]{}");
		performMatch(matcher, "(|b)%b|");
		performMatch(matcher, "(%|b)b|");
		performMatch(matcher, "|a(a|)%");
		performMatch(matcher, "|a(%a|)");
		performMatch(matcher, "|c#(c|)(|c)%c|");
		performMatch(matcher, "|c(%c|)(|c)#c|");
		performMatch(matcher, "(%|a)a||b)b||c)c|)#");

		performMatch(matcher, "|c#(c|)(|%c)#c|");
		performMatch(matcher, "|c#(c%|)(|c)#c|");
		performMatch(matcher, "#(%|a)a||b)b||c)c|)#");

		matcher.dispose();
	}

	/** Test that nesting works properly */
	public void testNesting() {
		final ICharacterPairMatcher matcher= createMatcher("()[]{}");
		performMatch(matcher, " ( #( ( ( ) ) ( ) )% ) ");
		performMatch(matcher, " ( (% ( ( ) ) ( ) )# ) ");
		performMatch(matcher, " ( #( { ( ) } [ ] )% ) ");
		performMatch(matcher, " ( (% { ( ) } [ ] )# ) ");
		performMatch(matcher, " ( ( #{ ( ) }% [ ] ) ) ");
		performMatch(matcher, " ( ( {% ( ) }# [ ] ) ) ");
		performMatch(matcher, "a(b#(c(d(e)f)g(h)i)%j)k");
		performMatch(matcher, "a(b(%c(d(e)f)g(h)i)#j)k");
		performMatch(matcher, "a(b#(c{d(e)f}g[h]i)%j)k");
		performMatch(matcher, "a(b(%c{d(e)f}g[h]i)#j)k");
		performMatch(matcher, "a(b(c#{d(e)f}%g[h]i)j)k");
		performMatch(matcher, "a(b(c{%d(e)f}#g[h]i)j)k");

		performMatch(matcher, " ( #( ( ( ) ) ( ) % )# ) ");
		performMatch(matcher, " ( #( % ( ( ) ) ( ) )# ) ");
		performMatch(matcher, " ( #( { ( ) } [ ] % )# ) ");
		performMatch(matcher, " ( #( % { ( ) } [ ] )# ) ");
		performMatch(matcher, " ( ( #{ ( ) % }# [ ] ) ) ");
		performMatch(matcher, " ( ( #{ % ( ) }# [ ] ) ) ");
		performMatch(matcher, "a(b#(c(d(e)f)g(h)i%)#j)k");
		performMatch(matcher, "a(b#(%c(d(e)f)g(h)i)#j)k");
		performMatch(matcher, "a(b#(c{d(e)f}g[h]i%)#j)k");
		performMatch(matcher, "a(b#(%c{d(e)f}g[h]i)#j)k");
		performMatch(matcher, "a(b(c#{d(e)f%}#g[h]i)j)k");
		performMatch(matcher, "a(b(c#{%d(e)f}#g[h]i)j)k");

		matcher.dispose();
	}

	/**
	 * Test a few boundary conditions.
	 * 
	 * * @throws BadLocationException
	 */
	public void testBoundaries() throws BadLocationException {
		final ICharacterPairMatcher matcher= createMatcher("()[]{}");
		final StringDocument doc= new StringDocument("abcdefghijkl");
		assertNull(matcher.match(null, 0));
		assertNull(matcher.match(doc, -1));
		assertNull(matcher.match(doc, doc.getLength() + 1));
		matcher.dispose();
	}

	public void testBug156426() {
		final ICharacterPairMatcher matcher= createMatcher("()[]{}<>");
		performMatch(matcher, " #( a < b )% ");
		performMatch(matcher, " (% a < b )# ");
		performMatch(matcher, " #( a > b )% ");
		performMatch(matcher, " (% a > b )# ");
		matcher.dispose();
	}

	/* --- U t i l i t i e s --- */

	/**
	 * Checks that the test case reader reads the test case as specified.
	 * 
	 * @param testString the string to test
	 * @param expectedPos the expected position
	 * @param expectedMatch the expected match
	 * @param expectedString the expected string
	 */
	protected void performReaderTest(String testString, int expectedPos, int expectedMatch, String expectedString) {
		TestCase t0= createTestCase(testString);
		assertEquals(expectedPos, t0.fPos);
		assertEquals(expectedMatch, t0.fMatch2);
		assertEquals(expectedString, t0.fString);
	}

	/**
	 * Checks that the given matcher matches the input as specified.
	 * 
	 * @param matcher the matcher
	 * @param testCase the test string
	 */
	protected void performMatch(final ICharacterPairMatcher matcher, final String testCase) {
		final TestCase test= createTestCase(testCase);
		matcher.clear();
		final IRegion region;

		if (test.isEnclosingTestCase()) {
			assertTrue((matcher instanceof ICharacterPairMatcherExtension));
			ICharacterPairMatcherExtension matcherExtension= (ICharacterPairMatcherExtension)matcher;
			region= matcherExtension.findEnclosingPeerCharacters(test.getDocument(), test.fPos);
		} else {
			region= matcher.match(test.getDocument(), test.fPos);
		}

		if (test.fMatch2 == -1) {
			// if no match point has been specified there should be no match
			if (region != null) System.out.println(region.getOffset());
			assertNull(region);
		} else {
			assertNotNull(region);
			final boolean isForward= test.isEnclosingTestCase() ? false : test.fPos > test.fMatch2;
			assertEquals(isForward, matcher.getAnchor() == ICharacterPairMatcher.RIGHT);

			final int offset= (isForward || test.isEnclosingTestCase()) ? test.getOffset() : test.getOffset() - 1;
			final int length= ((isForward && !fCaretInsideMatchedPair) || test.isEnclosingTestCase()) ? test.getLength() : test.getLength() + 1;
			assertEquals(length, region.getLength());
			assertEquals(offset, region.getOffset());
		}
	}

	private void performMatch(final String delims, final String testCase) {
		final ICharacterPairMatcher matcher= createMatcher(delims);
		performMatch(matcher, testCase);
		matcher.dispose();
	}

	/**
	 * Creates a text case from a string. In the given string a '%' represents the position of the
	 * cursor and a '#' represents the position of the expected matching character.
	 * 
	 * @param str the string for which to create the test case
	 * @return the created test case
	 */
	public TestCase createTestCase(String str) {
		int pos= str.indexOf("%");
		assertFalse(pos == -1);
		int match1= str.indexOf("#");
		int match2= str.lastIndexOf("#");
		boolean enclosingTest= match1 != match2;

		if (fCaretInsideMatchedPair) {
			if (pos - 1 >= 0) {
				char ch= str.charAt(pos - 1);
				if ("()[]{}<>".indexOf(ch) % 2 == 1) {
					pos-= 1;
				}
			}
		}

		// account for the length of marker characters
		if (!enclosingTest) {
			if (match1 != -1 && match1 < pos)
				pos-= 1;
			if (pos < match1)
				match1-= 1;
		} else {
			pos-= 1;
			match2-= 2;
		}

		final String stripped= str.replaceAll("%", "").replaceAll("#", "");
		return enclosingTest ? new TestCase(stripped, pos, match1, match2) : new TestCase(stripped, pos, match1);
	}

	private class TestCase {

		public final String fString;

		public final int fPos, fMatch1, fMatch2;

		public TestCase(String string, int pos, int match) {
			this(string, pos, pos, match);
		}

		public TestCase(String string, int pos, int match1, int match2) {
			fString= string;
			fPos= pos;
			fMatch1= match1;
			fMatch2= match2;
		}

		public IDocument getDocument() {
			return new StringDocument(fString);
		}

		public int getLength() {
			return Math.abs(fMatch1 - fMatch2);
		}

		public int getOffset() {
			if (fMatch1 > fMatch2)
				return fMatch2;
			return fMatch1;
		}

		public boolean isEnclosingTestCase() {
			return fPos != fMatch1;
		}

	}

	private class StringDocument extends Document {

		public StringDocument(String str) {
			this.setTextStore(new StringTextStore(str));
			this.set(str);
			final IDocumentPartitioner part= createPartitioner();
			this.setDocumentPartitioner(getDocumentPartitioning(), part);
			part.connect(this);
		}

	}

	private static class StringTextStore implements ITextStore {

		private String fString;

		public StringTextStore(final String str) {
			fString= str;
		}

		public char get(int offset) {
			return fString.charAt(offset);
		}

		public String get(int offset, int length) {
			return fString.substring(offset, offset + length);
		}

		public int getLength() {
			return fString.length();
		}

		public void replace(int offset, int length, String text) {
			throw new UnsupportedOperationException();
		}

		public void set(String text) {
			fString= text;
		}

	}

	private static String DEFAULT_PARTITION= IDocument.DEFAULT_CONTENT_TYPE;

	private static IDocumentPartitioner createPartitioner() {
		final RuleBasedPartitionScanner scan= new RuleBasedPartitionScanner();
		final List/*<IPredicateRule>*/ rules= new ArrayList/*<IPredicateRule>*/();
		rules.add(new SingleLineRule("|a", "a|", new Token("a")));
		rules.add(new SingleLineRule("|b", "b|", new Token("b")));
		rules.add(new SingleLineRule("|c", "c|", new Token("c")));
		scan.setPredicateRules((IPredicateRule[]) rules.toArray(new IPredicateRule[rules.size()]));
		scan.setDefaultReturnToken(new Token(DEFAULT_PARTITION));
		return new FastPartitioner(scan, new String[] { DEFAULT_PARTITION, "a", "b", "c" });
	}

}
