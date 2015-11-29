/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Cagatay Calli <ccalli@gmail.com> - [find/replace] retain caps when replacing - https://bugs.eclipse.org/bugs/show_bug.cgi?id=28949
 *     Cagatay Calli <ccalli@gmail.com> - [find/replace] define & fix behavior of retain caps with other escapes and text before \C - https://bugs.eclipse.org/bugs/show_bug.cgi?id=217061
 *******************************************************************************/
package org.eclipse.jface.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.Assert;


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
	 * Retain case mode constants.
	 * @since 3.4
	 */
	private static final int RC_MIXED= 0;
	private static final int RC_UPPER= 1;
	private static final int RC_LOWER= 2;
	private static final int RC_FIRSTUPPER= 3;


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
	 * Retain case mode
	 */
	private int fRetainCaseMode;

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
			if (startOffset < 0 || startOffset > length())
				throw new BadLocationException();

			int patternFlags= 0;

			if (regExSearch) {
				patternFlags |= Pattern.MULTILINE;
				findString= substituteLinebreak(findString);
			}

			if (!caseSensitive)
				patternFlags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;

			if (!regExSearch)
				findString= asRegPattern(findString);

			if (wholeWord)
				findString= "\\b" + findString + "\\b"; //$NON-NLS-1$ //$NON-NLS-2$

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
				String prevMatch= fFindReplaceMatcher.group();
				try {
					replaceText= interpretReplaceEscapes(replaceText, prevMatch);
					Matcher replaceTextMatcher= pattern.matcher(prevMatch);
					replaceText= replaceTextMatcher.replaceFirst(replaceText);
				} catch (IndexOutOfBoundsException ex) {
					throw new PatternSyntaxException(ex.getLocalizedMessage(), replaceText, -1);
				}
			}

			int offset= fFindReplaceMatcher.start();
			int length= fFindReplaceMatcher.group().length();

			if (fDocument instanceof IRepairableDocumentExtension
					&& ((IRepairableDocumentExtension)fDocument).isLineInformationRepairNeeded(offset, length, replaceText)) {
				String message= TextMessages.getString("FindReplaceDocumentAdapter.incompatibleLineDelimiter"); //$NON-NLS-1$
				throw new PatternSyntaxException(message, replaceText, offset);
			}

			fDocument.replace(offset, length, replaceText);

			if (operationCode == REPLACE) {
				return new Region(offset, replaceText.length());
			}
		}

		if (operationCode != REPLACE) {
			try {
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
			} catch (StackOverflowError e) {
				String message= TextMessages.getString("FindReplaceDocumentAdapter.patternTooComplex"); //$NON-NLS-1$
				throw new PatternSyntaxException(message, findString, -1);
			}
		}

		return null;
	}

	/**
	 * Substitutes \R in a regex find pattern with (?>\r\n?|\n)
	 *
	 * @param findString the original find pattern
	 * @return the transformed find pattern
	 * @throws PatternSyntaxException if \R is added at an illegal position (e.g. in a character set)
	 * @since 3.4
	 */
	private String substituteLinebreak(String findString) throws PatternSyntaxException {
		int length= findString.length();
		StringBuffer buf= new StringBuffer(length);

		int inCharGroup= 0;
		int inBraces= 0;
		boolean inQuote= false;
		for (int i= 0; i < length; i++) {
			char ch= findString.charAt(i);
			switch (ch) {
				case '[':
					buf.append(ch);
					if (! inQuote)
						inCharGroup++;
					break;

				case ']':
					buf.append(ch);
					if (! inQuote)
						inCharGroup--;
					break;

				case '{':
					buf.append(ch);
					if (! inQuote && inCharGroup == 0)
						inBraces++;
					break;

				case '}':
					buf.append(ch);
					if (! inQuote && inCharGroup == 0)
						inBraces--;
					break;

				case '\\':
					if (i + 1 < length) {
						char ch1= findString.charAt(i + 1);
						if (inQuote) {
							if (ch1 == 'E')
								inQuote= false;
							buf.append(ch).append(ch1);
							i++;

						} else if (ch1 == 'R') {
							if (inCharGroup > 0 || inBraces > 0) {
								String msg= TextMessages.getString("FindReplaceDocumentAdapter.illegalLinebreak"); //$NON-NLS-1$
								throw new PatternSyntaxException(msg, findString, i);
							}
							buf.append("(?>\\r\\n?|\\n)"); //$NON-NLS-1$
							i++;

						} else {
							if (ch1 == 'Q') {
								inQuote= true;
							}
							buf.append(ch).append(ch1);
							i++;
						}
					} else {
						buf.append(ch);
					}
					break;

				default:
					buf.append(ch);
					break;
			}

		}
		return buf.toString();
	}

	/**
	 * Interprets current Retain Case mode (all upper-case,all lower-case,capitalized or mixed)
	 * and appends the character <code>ch</code> to <code>buf</code> after processing.
	 *
	 * @param buf the output buffer
	 * @param ch the character to process
	 * @since 3.4
	 */
	private void interpretRetainCase(StringBuffer buf, char ch) {
		if (fRetainCaseMode == RC_UPPER)
			buf.append(String.valueOf(ch).toUpperCase());
		else if (fRetainCaseMode == RC_LOWER)
			buf.append(String.valueOf(ch).toLowerCase());
		else if (fRetainCaseMode == RC_FIRSTUPPER) {
			buf.append(String.valueOf(ch).toUpperCase());
			fRetainCaseMode= RC_MIXED;
		} else
			buf.append(ch);
	}

	/**
	 * Interprets escaped characters in the given replace pattern.
	 *
	 * @param replaceText the replace pattern
	 * @param foundText the found pattern to be replaced
	 * @return a replace pattern with escaped characters substituted by the respective characters
	 * @since 3.4
	 */
	private String interpretReplaceEscapes(String replaceText, String foundText) {
		int length= replaceText.length();
		boolean inEscape= false;
		StringBuffer buf= new StringBuffer(length);

		/* every string we did not check looks mixed at first
		 * so initialize retain case mode with RC_MIXED
		 */
		fRetainCaseMode= RC_MIXED;

		for (int i= 0; i < length; i++) {
			final char ch= replaceText.charAt(i);
			if (inEscape) {
				i= interpretReplaceEscape(ch, i, buf, replaceText, foundText);
				inEscape= false;

			} else if (ch == '\\') {
				inEscape= true;

			} else if (ch == '$') {
				buf.append(ch);

				/*
				 * Feature in java.util.regex.Matcher#replaceFirst(String):
				 * $00, $000, etc. are interpreted as $0 and
				 * $01, $001, etc. are interpreted as $1, etc. .
				 * If we support \0 as replacement pattern for capturing group 0,
				 * it would not be possible any more to write a replacement pattern
				 * that appends 0 to a capturing group (like $0\0).
				 * The fix is to interpret \00 and $00 as $0\0, and
				 * \01 and $01 as $0\1, etc.
				 */
				if (i + 2 < length) {
					char ch1= replaceText.charAt(i + 1);
					char ch2= replaceText.charAt(i + 2);
					if (ch1 == '0' && '0' <= ch2 && ch2 <= '9') {
						buf.append("0\\"); //$NON-NLS-1$
						i++; // consume the 0
					}
				}
			} else {
				interpretRetainCase(buf, ch);
			}
		}

		if (inEscape) {
			// '\' as last character is invalid, but we still add it to get an error message
			buf.append('\\');
		}
		return buf.toString();
	}

	/**
	 * Interprets the escaped character <code>ch</code> at offset <code>i</code>
	 * of the <code>replaceText</code> and appends the interpretation to <code>buf</code>.
	 *
	 * @param ch the escaped character
	 * @param i the offset
	 * @param buf the output buffer
	 * @param replaceText the original replace pattern
	 * @param foundText the found pattern to be replaced
	 * @return the new offset
	 * @since 3.4
	 */
	private int interpretReplaceEscape(final char ch, int i, StringBuffer buf, String replaceText, String foundText) {
		int length= replaceText.length();
		switch (ch) {
			case 'r':
				buf.append('\r');
				break;
			case 'n':
				buf.append('\n');
				break;
			case 't':
				buf.append('\t');
				break;
			case 'f':
				buf.append('\f');
				break;
			case 'a':
				buf.append('\u0007');
				break;
			case 'e':
				buf.append('\u001B');
				break;
			case 'R': //see http://www.unicode.org/unicode/reports/tr18/#Line_Boundaries
				buf.append(TextUtilities.getDefaultLineDelimiter(fDocument));
				break;
			/*
			 * \0 for octal is not supported in replace string, since it
			 * would conflict with capturing group \0, etc.
			 */
			case '0':
				buf.append('$').append(ch);
				/*
				 * See explanation in "Feature in java.util.regex.Matcher#replaceFirst(String)"
				 * in interpretReplaceEscape(String) above.
				 */
				if (i + 1 < length) {
					char ch1= replaceText.charAt(i + 1);
					if ('0' <= ch1 && ch1 <= '9') {
						buf.append('\\');
					}
				}
				break;

			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				buf.append('$').append(ch);
				break;

			case 'c':
				if (i + 1 < length) {
					char ch1= replaceText.charAt(i + 1);
					interpretRetainCase(buf, (char)(ch1 ^ 64));
					i++;
				} else {
					String msg= TextMessages.getFormattedString("FindReplaceDocumentAdapter.illegalControlEscape", "\\c"); //$NON-NLS-1$ //$NON-NLS-2$
					throw new PatternSyntaxException(msg, replaceText, i);
				}
				break;

			case 'x':
				if (i + 2 < length) {
					int parsedInt;
					try {
						parsedInt= Integer.parseInt(replaceText.substring(i + 1, i + 3), 16);
						if (parsedInt < 0)
							throw new NumberFormatException();
					} catch (NumberFormatException e) {
						String msg= TextMessages.getFormattedString("FindReplaceDocumentAdapter.illegalHexEscape", replaceText.substring(i - 1, i + 3)); //$NON-NLS-1$
						throw new PatternSyntaxException(msg, replaceText, i);
					}
					interpretRetainCase(buf, (char) parsedInt);
					i+= 2;
				} else {
					String msg= TextMessages.getFormattedString("FindReplaceDocumentAdapter.illegalHexEscape", replaceText.substring(i - 1, length)); //$NON-NLS-1$
					throw new PatternSyntaxException(msg, replaceText, i);
				}
				break;

			case 'u':
				if (i + 4 < length) {
					int parsedInt;
					try {
						parsedInt= Integer.parseInt(replaceText.substring(i + 1, i + 5), 16);
						if (parsedInt < 0)
							throw new NumberFormatException();
					} catch (NumberFormatException e) {
						String msg= TextMessages.getFormattedString("FindReplaceDocumentAdapter.illegalUnicodeEscape", replaceText.substring(i - 1, i + 5)); //$NON-NLS-1$
						throw new PatternSyntaxException(msg, replaceText, i);
					}
					interpretRetainCase(buf, (char) parsedInt);
					i+= 4;
				} else {
					String msg= TextMessages.getFormattedString("FindReplaceDocumentAdapter.illegalUnicodeEscape", replaceText.substring(i - 1, length)); //$NON-NLS-1$
					throw new PatternSyntaxException(msg, replaceText, i);
				}
				break;

			case 'C':
				if(foundText.toUpperCase().equals(foundText)) // is whole match upper-case?
					fRetainCaseMode= RC_UPPER;
				else if (foundText.toLowerCase().equals(foundText)) // is whole match lower-case?
					fRetainCaseMode= RC_LOWER;
				else if(Character.isUpperCase(foundText.charAt(0))) // is first character upper-case?
					fRetainCaseMode= RC_FIRSTUPPER;
				else
					fRetainCaseMode= RC_MIXED;
				break;

			default:
				// unknown escape k: append uninterpreted \k
				buf.append('\\').append(ch);
				break;
		}
		return i;
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

	@Override
	public int length() {
		return fDocument.getLength();
	}

	@Override
	public char charAt(int index) {
		try {
			return fDocument.getChar(index);
		} catch (BadLocationException e) {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		try {
			return fDocument.get(start, end - start);
		} catch (BadLocationException e) {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public String toString() {
		return fDocument.get();
	}

	/**
	 * Escapes special characters in the string, such that the resulting pattern
	 * matches the given string.
	 *
	 * @param string the string to escape
	 * @return a regex pattern that matches the given string
	 * @since 3.5
	 */
	public static String escapeForRegExPattern(String string) {
		//implements https://bugs.eclipse.org/bugs/show_bug.cgi?id=44422
	
		StringBuffer pattern= new StringBuffer(string.length() + 16);
		int length= string.length();
		for (int i= 0; i < length; i++) {
			char ch= string.charAt(i);
			switch (ch) {
				case '\\':
				case '(':
				case ')':
				case '[':
				case ']':
				case '{':
				case '}':
				case '.':
				case '?':
				case '*':
				case '+':
				case '|':
				case '^':
				case '$':
					pattern.append('\\').append(ch);
					break;
	
				case '\r':
					if (i + 1 < length && string.charAt(i + 1) == '\n')
						i++;
					//$FALL-THROUGH$
				case '\n':
					pattern.append("\\R"); //$NON-NLS-1$
					break;
				case '\t':
					pattern.append("\\t"); //$NON-NLS-1$
					break;
				case '\f':
					pattern.append("\\f"); //$NON-NLS-1$
					break;
				case 0x07:
					pattern.append("\\a"); //$NON-NLS-1$
					break;
				case 0x1B:
					pattern.append("\\e"); //$NON-NLS-1$
					break;
	
				default:
					if (0 <= ch && ch < 0x20) {
						pattern.append("\\x"); //$NON-NLS-1$
						String hexString= Integer.toHexString(ch).toUpperCase();
						if (hexString.length() == 1)
							pattern.append('0');
						pattern.append(hexString);
					} else {
						pattern.append(ch);
					}
			}
		}
		return pattern.toString();
	}
}
