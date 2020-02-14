/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Ralf Heydenreich - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.dialogs.about;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public final class HyperlinkExtractor {
	private String aboutProperty;
	private List<HyperlinkRange> linkRanges;
	private List<String> links;

	public HyperlinkExtractor(final String aboutProperty) {
		this.aboutProperty = aboutProperty;
		linkRanges = new ArrayList<>();
		links = new ArrayList<>();
		extractLinks();
	}

	private void extractLinks() {

		// slightly modified version of jface url detection
		// see org.eclipse.jface.text.hyperlink.URLHyperlinkDetector

		int urlSeparatorOffset = aboutProperty.indexOf("://"); //$NON-NLS-1$
		while (urlSeparatorOffset >= 0) {

			boolean startDoubleQuote = false;

			// URL protocol (left to "://")
			int urlOffset = urlSeparatorOffset;
			char ch;
			do {
				urlOffset--;
				ch = ' ';
				if (urlOffset > -1) {
					ch = aboutProperty.charAt(urlOffset);
				}
				startDoubleQuote = ch == '"';
			} while (Character.isUnicodeIdentifierStart(ch));
			urlOffset++;

			// Right to "://"
			StringTokenizer tokenizer = new StringTokenizer(aboutProperty.substring(urlSeparatorOffset + 3),
					" \t\n\r\f<>", false); //$NON-NLS-1$
			if (!tokenizer.hasMoreTokens()) {
				return;
			}

			int urlLength = tokenizer.nextToken().length() + 3 + urlSeparatorOffset - urlOffset;

			if (startDoubleQuote) {
				int endOffset = -1;
				int nextDoubleQuote = aboutProperty.indexOf('"', urlOffset);
				int nextWhitespace = aboutProperty.indexOf(' ', urlOffset);
				if (nextDoubleQuote != -1 && nextWhitespace != -1) {
					endOffset = Math.min(nextDoubleQuote, nextWhitespace);
				} else if (nextDoubleQuote != -1) {
					endOffset = nextDoubleQuote;
				} else if (nextWhitespace != -1) {
					endOffset = nextWhitespace;
				}
				if (endOffset != -1) {
					urlLength = endOffset - urlOffset;
				}
			}

			linkRanges.add(new HyperlinkRange(urlOffset, urlLength));
			links.add(aboutProperty.substring(urlOffset, urlOffset + urlLength));

			urlSeparatorOffset = aboutProperty.indexOf("://", urlOffset + urlLength + 1); //$NON-NLS-1$
		}
	}

	public List<HyperlinkRange> getLinkRanges() {
		return linkRanges;
	}

	public List<String> getLinks() {
		return links;
	}

}
