/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Genady Beryozkin, me@genady.org - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * This class contains the hippie completion engine methods that actually
 * compute the possible completions.
 * <p>
 * This engine is used by the <code>org.eclipse.ui.texteditor.HippieCompleteAction</code>.
 * </p>
 *
 * TODO: Sort by editor type
 * TODO: Provide history option
 *
 * @since 3.1
 * @author Genady Beryozkin, me@genady.org
 */
public final class HippieCompletionEngine {

	/**
	 * Regular expression that is used to find words.
	 */
	// unicode identifier part
//	private static final String COMPLETION_WORD_REGEX= "[\\p{L}[\\p{Mn}[\\p{Pc}[\\p{Nd}[\\p{Nl}]]]]]+"; //$NON-NLS-1$
	// java identifier part (unicode id part + currency symbols)
	private static final String COMPLETION_WORD_REGEX= "[\\p{L}[\\p{Mn}[\\p{Pc}[\\p{Nd}[\\p{Nl}[\\p{Sc}]]]]]]+"; //$NON-NLS-1$
	/**
	 * The pre-compiled word pattern.
	 * 
	 * @since 3.2
	 */
	private static final Pattern COMPLETION_WORD_PATTERN= Pattern.compile(COMPLETION_WORD_REGEX);
	/**
	 * The word boundary pattern string.
	 * 
	 * @since 3.2
	 */
	private static final String COMPLETION_BOUNDARY= "(^|[\\s\\p{Z}[\\p{P}&&[\\P{Pc}]][\\p{S}&&[\\P{Sc}]]]+)";  //$NON-NLS-1$
	// with a 1.5 JRE, you can do this:
//	private static final String COMPLETION_WORD_REGEX= "\\p{javaUnicodeIdentifierPart}+"; //$NON-NLS-1$
//	private static final String COMPLETION_WORD_REGEX= "\\p{javaJavaIdentifierPart}+"; //$NON-NLS-1$

	/**
	 * Is completion case sensitive? Even if set to <code>false</code>, the
	 * case of the prefix won't be changed.
	 */
	private static final boolean CASE_SENSITIVE= true;

	/**
	 * Creates a new engine.
	 */
	public HippieCompletionEngine() {
	}

	/*
	 * Copied from {@link FindReplaceDocumentAdapter#asRegPattern(java.lang.String)}.
	 */
	/**
	 * Converts a non-regex string to a pattern that can be used with the regex
	 * search engine.
	 *
	 * @param string the non-regex pattern
	 * @return the string converted to a regex pattern
	 */
	private String asRegPattern(CharSequence string) {
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
	 * The method is equivalent to
	 * <code>getCompletionsForward(document, prefix, 0)</code>
	 *
	 * @param document the document to scan
	 * @param prefix the completion prefix
	 * @return the list of completions that are result of this document scan.
	 * @throws BadLocationException if some error occurs. Should not be ever
	 *         thrown.
	 * @see #getCompletionsForward(IDocument, CharSequence, int)
	 */
	public List getCompletions(IDocument document, CharSequence prefix) throws BadLocationException {
		return getCompletionsForward(document, prefix, 0);
	}

	/**
	 * Return the list of completion suggestions that correspond to the
	 * provided prefix.
	 * 
	 * @param document the document to be scanned
	 * @param prefix the prefix to search for
	 * @param firstPosition the initial position in the document that
	 *        the search will start from. In order to search from the
	 *        beginning of the document use <code>firstPosition=0</code>.
	 * @return a {@link List} of possible completions (as {@link String}s),
	 *         excluding the common prefix
	 * @throws BadLocationException if there is some error scanning the
	 *         document.
	 * @see #getCompletions(IDocument, CharSequence)
	 */
	public List getCompletionsForward(IDocument document, CharSequence prefix, int firstPosition) throws BadLocationException {
		ArrayList res= new ArrayList();
        if (firstPosition == document.getLength()) {
            return res;
        }

		FindReplaceDocumentAdapter searcher= new FindReplaceDocumentAdapter(document);

		// search only at word boundaries
		String searchPattern= COMPLETION_BOUNDARY + asRegPattern(prefix);

		IRegion reg= searcher.find(firstPosition, searchPattern, true, CASE_SENSITIVE, false, true);
		while (reg != null) {
			// since the boundary may be of nonzero length
			int wordSearchPos= reg.getOffset() + reg.getLength() - prefix.length();
			// try to complete to a word. case is irrelevant here.
			IRegion word= searcher.find(wordSearchPos, COMPLETION_WORD_REGEX, true, true, false, true);
			if (word.getLength() > prefix.length() ) { // empty suggestion will be added later
				String found= document.get(word.getOffset(), word.getLength());
				res.add(found.substring(prefix.length()));
			}
			int nextPos= word.getOffset() + word.getLength();
			if (nextPos >= document.getLength() ) {
				break;
			}
			reg= searcher.find(nextPos, searchPattern, true, CASE_SENSITIVE, false, true);
		}

		return res;
	}

	/**
	 * Search for possible completions in the backward direction. If there
     * is a possible completion that begins before <code>firstPosition</code>
     * but ends after that position, it will be included in the results.
	 *
	 * @param document the document to be scanned
	 * @param prefix the completion prefix
	 * @param firstPosition the caret position
	 * @return a {@link List} of possible completions ({@link String}s)
	 *         from the caret position to the beginning of the document.
     *         The empty suggestion is not included in the results.
	 * @throws BadLocationException if any error occurs
	 */
	public List getCompletionsBackwards(IDocument document, CharSequence prefix, int firstPosition) throws BadLocationException {
		ArrayList res= new ArrayList();

        // FindReplaceDocumentAdapter expects the start offset to be before the
        // actual caret position, probably for compatibility with forward search.
        if (firstPosition == 0) {
            return res;
        }

		FindReplaceDocumentAdapter searcher= new FindReplaceDocumentAdapter(document);

		// search only at word boundaries
		String searchPattern= COMPLETION_BOUNDARY + asRegPattern(prefix); 

		IRegion reg= searcher.find(0, searchPattern, true, CASE_SENSITIVE, false, true);
		while (reg != null) {
			// since the boundary may be of nonzero length
			int wordSearchPos= reg.getOffset() + reg.getLength() - prefix.length();
			// try to complete to a word. case is of no matter here
			IRegion word= searcher.find(wordSearchPos, COMPLETION_WORD_REGEX, true, true, false, true);
            if (word.getOffset() > firstPosition) {
                break;
            }
			if (word.getLength() > prefix.length() ) { // empty suggestion will be added later
				String found= document.get(word.getOffset(), word.getLength());
				res.add(found.substring(prefix.length()));
			}
            int nextPos= word.getOffset() + word.getLength();
            if (nextPos >= firstPosition ) { // for efficiency only
                break;
            }
			reg= searcher.find(nextPos, searchPattern, true, CASE_SENSITIVE, false, true);
		}
        Collections.reverse(res);

		return res;
	}

	/**
	 * Returns the text between the provided position and the preceding word boundary.
	 * 
	 * @param doc the document that will be scanned.
	 * @param pos the caret position.
	 * @return the text if found, or null.
	 * @throws BadLocationException if an error occurs.
	 * @since 3.2
	 */
	public String getPrefixString(IDocument doc, int pos) throws BadLocationException {
		Matcher m= COMPLETION_WORD_PATTERN.matcher(""); //$NON-NLS-1$
		int prevNonAlpha= pos;
		while (prevNonAlpha > 0) {
			m.reset(doc.get(prevNonAlpha-1, pos - prevNonAlpha + 1));
			if (!m.matches()) {
				break;
			}
			prevNonAlpha--;
		}
		if (prevNonAlpha != pos) {
			return doc.get(prevNonAlpha, pos - prevNonAlpha);
		}
		return null;
	}

	/**
	 * Remove duplicate suggestions (excluding the prefix), leaving the closest
	 * to list head.
	 *
	 * @param suggestions a list of suggestions ({@link String}).
	 * @return a list of unique completion suggestions.
	 */
	public List makeUnique(List suggestions) {
		HashSet seenAlready= new HashSet();
		ArrayList uniqueSuggestions= new ArrayList();

		for (Iterator i= suggestions.iterator(); i.hasNext();) {
			String suggestion= (String) i.next();
			if (!seenAlready.contains(suggestion)) {
				seenAlready.add(suggestion);
				uniqueSuggestions.add(suggestion);
			}
		}
		return uniqueSuggestions;
	}
}
