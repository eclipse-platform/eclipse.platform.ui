/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.hyperlink;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;


/**
 * URL hyperlink detector.
 * <p>
 * NOTE: This API is work in progress and may change before the final API freeze. (FIXME)
 * </p>
 * 
 * @since 3.1
 */
public class URLHyperlinkDetector implements IHyperlinkDetector {

	private ITextViewer fTextViewer;

	
	/**
	 * Creates a new URL hyperlink detector.
	 *  
	 * @param textViewer the text viewer in which to detect the hyperlink
	 */
	public URLHyperlinkDetector(ITextViewer textViewer) {
		Assert.isNotNull(textViewer);
		fTextViewer= textViewer;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.IHyperlinkDetector#detectHyperlink(org.eclipse.jface.text.IRegion)
	 */
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region) {
		if (region == null || fTextViewer == null)
			return null;

		IDocument document= fTextViewer.getDocument();

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
		
		// URL protocol (left to "://")
		int urlOffsetInLine= urlSeparatorOffset;
		char ch;
		do {
			urlOffsetInLine--;
			ch= ' ';
			if (urlOffsetInLine > -1)
				ch= line.charAt(urlOffsetInLine);
		} while (!Character.isWhitespace(ch));
		urlOffsetInLine++;
		
		// Right to "://"
		StringTokenizer tokenizer= new StringTokenizer(line.substring(urlSeparatorOffset + 3));
		if (!tokenizer.hasMoreTokens())
			return null;
		
		int urlLength= tokenizer.nextToken().length() + 3 + urlSeparatorOffset - urlOffsetInLine;
		if (offsetInLine < urlOffsetInLine || offsetInLine > urlOffsetInLine + urlLength)
			return null;
		
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
