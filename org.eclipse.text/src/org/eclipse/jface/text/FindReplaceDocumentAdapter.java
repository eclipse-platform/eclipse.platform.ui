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

package org.eclipse.jface.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * Provides search and replace operations on
 * {@link org.eclipse.jface.text.IDocument}.
 * <p>
 * Replaces
 * {@link org.eclipse.jface.text.IDocument#search(int, String, boolean, boolean, boolean)}.
 *
 * @since 3.0
 */
public class FindReplaceDocumentAdapter implements CharSequence {

	/**
	 * Internal type for operation codes.
	 */
	private static class FindReplaceOperationCode {
	}

	// Find/replace operation codes.
	private static final FindReplaceOperationCode FIND_FIRST= new FindReplaceOperationCode();
	private static final FindReplaceOperationCode FIND_NEXT= new FindReplaceOperationCode();
	private static final FindReplaceOperationCode REPLACE= new FindReplaceOperationCode();
	private static final FindReplaceOperationCode REPLACE_FIND_NEXT= new FindReplaceOperationCode();

	/**
	 * The adapted document.
	 */
	private IDocument fDocument;

	/**
	 * State for findReplace.
	 */
	private FindReplaceOperationCode fFindReplaceState= null;

	/**
	 * The matcher used in findReplace.
	 */
	private Matcher fFindReplaceMatcher;

	/**
	 * The match offset from the last findReplace call.
	 */
	private int fFindReplaceMatchOffset;

	/**
	 * Constructs a new find replace document adapter.
	 *
	 * @param document the adapted document
	 */
	public FindReplaceDocumentAdapter(IDocument document) {
		Assert.isNotNull(document);
		fDocument= document;
	}

	/**
	 * Returns the location of a given string in this adapter's document based on a set of search criteria.
	 *
	 * @param startOffset document offset at which search starts
	 * @param findString the string to find
	 * @param forwardSearch the search direction
	 * @param caseSensitive indicates whether lower and upper case should be distinguished
	 * @param wholeWord indicates whether the findString should be limited by white spaces as
	 * 			defined by Character.isWhiteSpace. Must not be used in combination with <code>regExSearch</code>.
	 * @param regExSearch if <code>true</code> findString represents a regular expression
	 * 			Must not be used in combination with <code>wholeWord</code>.
	 * @return the find or replace region or <code>null</code> if there was no match
	 * @throws BadLocationException if startOffset is an invalid document offset
	 * @throws PatternSyntaxException if a regular expression has invalid syntax
	 */
	public IRegion find(int startOffset, String findString, boolean forwardSearch, boolean caseSensitive, boolean wholeWord, boolean regExSearch) throws BadLocationException {
		Assert.isTrue(!(regExSearch && wholeWord));

		// Adjust offset to special meaning of -1
		if (startOffset == -1 && forwardSearch)
			startOffset= 0;
		if (startOffset == -1 && !forwardSearch)
			startOffset= length() - 1;

		return findReplace(FIND_FIRST, startOffset, findString, null, forwardSearch, caseSensitive, wholeWord, regExSearch);
	}

	/**
	 * Stateful findReplace executes a FIND, REPLACE, REPLACE_FIND or FIND_FIRST operation.
	 * In case of REPLACE and REPLACE_FIND it sends a <code>DocumentEvent</code> to all
	 * registered <code>IDocumentListener</code>.
	 *
	 * @param startOffset document offset at which search starts
	 * 			this value is only used in the FIND_FIRST operation and otherwise ignored
	 * @param findString the string to find
	 * 			this value is only used in the FIND_FIRST operation and otherwise ignored
	 * @param replaceText the string to replace the current match
	 * 			this value is only used in the REPLACE and REPLACE_FIND operations and otherwise ignored
	 * @param forwardSearch the search direction
	 * @param caseSensitive indicates whether lower and upper case should be distinguished
	 * @param wholeWord indicates whether the findString should be limited by white spaces as
	 * 			defined by Character.isWhiteSpace. Must not be used in combination with <code>regExSearch</code>.
	 * @param regExSearch if <code>true</code> this operation represents a regular expression
	 * 			Must not be used in combination with <code>wholeWord</code>.
	 * @param operationCode specifies what kind of operation is executed
	 * @return the find or replace region or <code>null</code> if there was no match
	 * @throws BadLocationException if startOffset is an invalid document offset
	 * @throws IllegalStateException if a REPLACE or REPLACE_FIND operation is not preceded by a successful FIND operation
	 * @throws PatternSyntaxException if a regular expression has invalid syntax
	 */
	private IRegion findReplace(final FindReplaceOperationCode operationCode, int startOffset, String findString, String replaceText, boolean forwardSearch, boolean caseSensitive, boolean wholeWord, boolean regExSearch) throws BadLocationException {

		// Validate option combinations
		Assert.isTrue(!(regExSearch && wholeWord));

		// Validate state
		if ((operationCode == REPLACE || operationCode == REPLACE_FIND_NEXT) && (fFindReplaceState != FIND_FIRST && fFindReplaceState != FIND_NEXT))
			throw new IllegalStateException("illegal findReplace state: cannot replace without preceding find"); //$NON-NLS-1$

		if (operationCode == FIND_FIRST) {
			// Reset

			if (findString == null || findString.length() == 0)
				return null;

			// Validate start offset
			if (startOffset < 0 || startOffset >= length())
				throw new BadLocationException();

			int patternFlags= 0;

			if (regExSearch)
				patternFlags |= Pattern.MULTILINE;

			if (!caseSensitive)
				patternFlags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;

			if (wholeWord)
				findString= "\\b" + findString + "\\b"; //$NON-NLS-1$ //$NON-NLS-2$

			if (!regExSearch && !wholeWord)
				findString= asRegPattern(findString);

			fFindReplaceMatchOffset= startOffset;
			if (fFindReplaceMatcher != null && fFindReplaceMatcher.pattern().pattern().equals(findString) && fFindReplaceMatcher.pattern().flags() == patternFlags) {
				/*
				 * Commented out for optimization:
				 * The call is not needed since FIND_FIRST uses find(int) which resets the matcher
				 */
				// fFindReplaceMatcher.reset();
			} else {
				Pattern pattern= Pattern.compile(findString, patternFlags);
				fFindReplaceMatcher= pattern.matcher(this);
			}
		}

		// Set state
		fFindReplaceState= operationCode;

		if (operationCode == REPLACE || operationCode == REPLACE_FIND_NEXT) {
			if (regExSearch) {
				Pattern pattern= fFindReplaceMatcher.pattern();
				Matcher replaceTextMatcher= pattern.matcher(fFindReplaceMatcher.group());
				try {
					replaceText= replaceTextMatcher.replaceFirst(replaceText);
				} catch (IndexOutOfBoundsException ex) {
					throw new PatternSyntaxException(ex.getLocalizedMessage(), replaceText, -1);
				}
			}

			int offset= fFindReplaceMatcher.start();
			fDocument.replace(offset, fFindReplaceMatcher.group().length(), replaceText);

			if (operationCode == REPLACE) {
				return new Region(offset, replaceText.length());
			}
		}

		if (operationCode != REPLACE) {
			if (forwardSearch) {

				boolean found= false;
				if (operationCode == FIND_FIRST)
					found= fFindReplaceMatcher.find(startOffset);
				else
					found= fFindReplaceMatcher.find();

				if (operationCode == REPLACE_FIND_NEXT)
					fFindReplaceState= FIND_NEXT;

				if (found && fFindReplaceMatcher.group().length() > 0)
					return new Region(fFindReplaceMatcher.start(), fFindReplaceMatcher.group().length());
				return null;
			}

			// backward search
			boolean found= fFindReplaceMatcher.find(0);
			int index= -1;
			int length= -1;
			while (found && fFindReplaceMatcher.start() + fFindReplaceMatcher.group().length() <= fFindReplaceMatchOffset + 1) {
				index= fFindReplaceMatcher.start();
				length= fFindReplaceMatcher.group().length();
				found= fFindReplaceMatcher.find(index + 1);
			}
			fFindReplaceMatchOffset= index;
			if (index > -1) {
				// must set matcher to correct position
				fFindReplaceMatcher.find(index);
				return new Region(index, length);
			}
			return null;
		}

		return null;
	}

	/**
	 * Converts a non-regex string to a pattern
	 * that can be used with the regex search engine.
	 *
	 * @param string the non-regex pattern
	 * @return the string converted to a regex pattern
	 */
	private String asRegPattern(String string) {
		StringBuffer out= new StringBuffer(string.length());
		boolean quoting= false;

		for (int i= 0, length= string.length(); i < length; i++) {
			char ch= string.charAt(i);
			if (ch == '\\') {
				if (quoting) {
					out.append("\\E"); //$NON-NLS-1$
					quoting= false;
				}
				out.append("\\\\"); //$NON-NLS-1$
				continue;
			}
			if (!quoting) {
				out.append("\\Q"); //$NON-NLS-1$
				quoting= true;
			}
			out.append(ch);
		}
		if (quoting)
			out.append("\\E"); //$NON-NLS-1$

		return out.toString();
	}

	/**
	 * Substitutes the previous match with the given text.
	 * Sends a <code>DocumentEvent</code> to all registered <code>IDocumentListener</code>.
	 *
	 * @param text the substitution text
	 * @param regExReplace if <code>true</code> <code>text</code> represents a regular expression
	 * @return the replace region or <code>null</code> if there was no match
	 * @throws BadLocationException if startOffset is an invalid document offset
	 * @throws IllegalStateException if a REPLACE or REPLACE_FIND operation is not preceded by a successful FIND operation
	 * @throws PatternSyntaxException if a regular expression has invalid syntax
	 *
	 * @see DocumentEvent
	 * @see IDocumentListener
	 */
	public IRegion replace(String text, boolean regExReplace) throws BadLocationException {
		return findReplace(REPLACE, -1, null, text, false, false, false, regExReplace);
	}

	// ---------- CharSequence implementation ----------

	/*
	 * @see java.lang.CharSequence#length()
	 */
	public int length() {
		return fDocument.getLength();
	}

	/*
	 * @see java.lang.CharSequence#charAt(int)
	 */
	public char charAt(int index) {
		try {
			return fDocument.getChar(index);
		} catch (BadLocationException e) {
			throw new IndexOutOfBoundsException();
		}
	}

	/*
	 * @see java.lang.CharSequence#subSequence(int, int)
	 */
	public CharSequence subSequence(int start, int end) {
		try {
			return fDocument.get(start, end - start);
		} catch (BadLocationException e) {
			throw new IndexOutOfBoundsException();
		}
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return fDocument.get();
	}
}
