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
package org.eclipse.jface.text.hyperlink;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

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
public class URLHyperlinkDetector implements IHyperlinkDetector {


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
	public URLHyperlinkDetector(ITextViewer textViewer) {
	}

	/*
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion, boolean)
	 */
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

		int urlSeparatorOffset= line.indexOf("://"); //$NON-NLS-1$
		if (urlSeparatorOffset < 0)
			return null;

		boolean startDoubleQuote= false;

		// URL protocol (left to "://")
		int urlOffsetInLine= urlSeparatorOffset;
		char ch;
		do {
			urlOffsetInLine--;
			ch= ' ';
			if (urlOffsetInLine > -1)
				ch= line.charAt(urlOffsetInLine);
			startDoubleQuote= ch == '"';
		} while (!Character.isWhitespace(ch) && !startDoubleQuote);
		urlOffsetInLine++;

		// Right to "://"
		StringTokenizer tokenizer= new StringTokenizer(line.substring(urlSeparatorOffset + 3));
		if (!tokenizer.hasMoreTokens())
			return null;

		int urlLength= tokenizer.nextToken().length() + 3 + urlSeparatorOffset - urlOffsetInLine;
		if (offsetInLine < urlOffsetInLine || offsetInLine > urlOffsetInLine + urlLength)
			return null;

		if (startDoubleQuote) {
			int endOffset= -1;
			int nextDoubleQuote= line.indexOf('"', urlOffsetInLine);
			int nextWhitespace= line.indexOf(' ', urlOffsetInLine);
			if (nextDoubleQuote != -1 && nextWhitespace != -1)
				endOffset= Math.min(nextDoubleQuote, nextWhitespace);
			else if (nextDoubleQuote != -1)
				endOffset= nextDoubleQuote;
			else if (nextWhitespace != -1)
				endOffset= nextWhitespace;
			if (endOffset != -1)
				urlLength= endOffset - urlOffsetInLine;
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
