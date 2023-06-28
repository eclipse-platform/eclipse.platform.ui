/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla <b.muskalla@gmx.net> - https://bugs.eclipse.org/bugs/show_bug.cgi?id=156433
 *******************************************************************************/
package org.eclipse.jface.text.hyperlink;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;


/**
 * URL hyperlink detector.
 *
 * @since 3.1
 */
public class URLHyperlinkDetector extends AbstractHyperlinkDetector {

	private static final String STOP_CHARACTERS= " \t\n\r\f<>"; //$NON-NLS-1$

	/**
	 * Creates a new URL hyperlink detector.
	 *
	 * @since 3.2
	 */
	public URLHyperlinkDetector() {
	}

	/**
	 * Creates a new URL hyperlink detector.
	 *
	 * @param textViewer the text viewer in which to detect the hyperlink
	 * @deprecated As of 3.2, replaced by {@link URLHyperlinkDetector}
	 */
	@Deprecated
	public URLHyperlinkDetector(ITextViewer textViewer) {
	}

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || textViewer == null)
			return null;

		IDocument document= textViewer.getDocument();

		int offset= region.getOffset();

		String urlString= null;
		if (document == null)
			return null;

		IRegion lineInfo;
		String line;
		try {
			lineInfo= document.getLineInformationOfOffset(offset);
			line= document.get(lineInfo.getOffset(), lineInfo.getLength());
		} catch (BadLocationException ex) {
			return null;
		}

		int offsetInLine= offset - lineInfo.getOffset();

		char quote= 0;
		int urlOffsetInLine= 0;
		int urlLength= 0;
		int lineEnd= line.length();

		int urlSeparatorOffset= line.indexOf("://"); //$NON-NLS-1$
		while (urlSeparatorOffset >= 0) {

			// URL protocol (left to "://")
			urlOffsetInLine= urlSeparatorOffset;
			char ch;
			do {
				urlOffsetInLine--;
				ch= ' ';
				if (urlOffsetInLine > -1)
					ch= line.charAt(urlOffsetInLine);
				if (ch == '"' || ch == '\'')
					quote= ch;
			} while (Character.isUnicodeIdentifierStart(ch));
			urlOffsetInLine++;
			// Handle prefixes like "scm:https://foo": scan further back
			if (ch == ':') {
				int i= urlOffsetInLine - 1;
				while (i >= 0) {
					ch= line.charAt(i--);
					if (ch == '"' || ch == '\'') {
						quote= ch;
						break;
					}
					if (ch != ':' && !Character.isUnicodeIdentifierStart(ch)) {
						break;
					}
				}
			}
			// Right to "://"
			int afterSeparator= urlSeparatorOffset + 3;
			int end= afterSeparator;
			while (end < lineEnd && STOP_CHARACTERS.indexOf(line.charAt(end)) < 0) {
				end++;
			}
			// Remove trailing periods.
			while (end > afterSeparator && line.charAt(end - 1) == '.') {
				end--;
			}
			if (end > afterSeparator) {
				urlLength= end - urlOffsetInLine;
				if (offsetInLine >= urlOffsetInLine && offsetInLine <= urlOffsetInLine + urlLength) {
					break;
				}
			}

			urlSeparatorOffset= line.indexOf("://", afterSeparator); //$NON-NLS-1$
		}

		if (urlSeparatorOffset < 0)
			return null;

		if (quote != 0) {
			int endOffset= -1;
			int nextQuote= line.indexOf(quote, urlOffsetInLine);
			int nextWhitespace= line.indexOf(' ', urlOffsetInLine);
			if (nextQuote != -1 && nextWhitespace != -1)
				endOffset= Math.min(nextQuote, nextWhitespace);
			else if (nextQuote != -1)
				endOffset= nextQuote;
			else if (nextWhitespace != -1)
				endOffset= nextWhitespace;
			if (endOffset != -1)
				urlLength= endOffset - urlOffsetInLine;
		}

		if (urlOffsetInLine + urlLength == urlSeparatorOffset + 3) {
			return null; // Only "scheme://"
		}

		// Set and validate URL string
		try {
			urlString= line.substring(urlOffsetInLine, urlOffsetInLine + urlLength);
			new URL(urlString);
		} catch (MalformedURLException ex) {
			urlString= null;
			return null;
		}

		IRegion urlRegion= new Region(lineInfo.getOffset() + urlOffsetInLine, urlLength);
		return new IHyperlink[] {new URLHyperlink(urlRegion, urlString)};
	}

}
