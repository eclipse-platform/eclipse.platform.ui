/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;

import java.io.*;
import com.ibm.icu.text.BreakIterator;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.lucene.analysis.*;

/**
 * WordTokenStream obtains tokens containing words appropriate for use with
 * Lucene search engine.
 */
public final class WordTokenStream extends TokenStream {
	private static final int BUF_LEN = 4096;
	private static final int TOKENS_LEN = 512;
	private final Reader reader;
	private final BreakIterator boundary;
	private final ArrayList<Token> tokens;
	private int token;
	private int noTokens;
	private final char[] cbuf;
	/**
	 * Constructor
	 */
	public WordTokenStream(String fieldName, Reader reader, Locale locale) {
		this.reader = reader;
		boundary = BreakIterator.getWordInstance(locale);
		cbuf = new char[BUF_LEN];
		tokens = new ArrayList<Token>(TOKENS_LEN);

	}
	/**
	 * @see TokenStream#next()
	 */
	public final Token next() throws IOException {
		while (token >= noTokens) {
			// read BUF_LEN of chars
			int l;
			while ((l = reader.read(cbuf)) <= 0) {
				if (l < 0) {
					// EOF
					reader.close();
					return null;
				}
			}
			StringBuffer strbuf = new StringBuffer(l + 80);
			strbuf.append(cbuf, 0, l);
			// read more until white space (or EOF)
			int c;
			while (0 <= (c = reader.read())) {
				strbuf.append((char) c);
				if (c == ' ' || c == '\r' || c == '\n' || c == '\t') {
					break;
				}
			}

			String str = strbuf.toString();
			boundary.setText(str);

			int start = boundary.first();
			tokens.clear();
			wordsbreak : for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary
					.next()) {
				// determine if it is a word
				// any letter or digit between boundaries means it is a word
				for (int i = start; i < end; i++) {
					if (Character.isLetterOrDigit(str.charAt(i))) {
						// it is a word
						tokens.add(new Token(str.substring(start, end), start,
								end));
						continue wordsbreak;
					}
				}
			}

			if (c < 0) {
				reader.close();
				tokens.add((Token) null);
			}
			noTokens = tokens.size();
			token = 0;
		}

		return tokens.get(token++);

	}
}
