/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.search;

import java.io.*;
import java.text.BreakIterator;
import java.util.*;

import org.apache.lucene.analysis.*;

/**
 * WordTokenStream obtains tokens containing words
 * appropriate for use with Lucene search engine.
 */
public class WordTokenStream extends TokenStream {
	String fieldName;
	Reader reader;
	/**
	 * Iterator for obtaining Tokens
	 */
	Iterator tokens;
	Locale locale;
	IOException readerException;
	/**
	 * Constructor
	 * Creates a token stream out of Reader
	 */
	public WordTokenStream(String fieldName, Reader reader, Locale locale) {
		this.fieldName = fieldName;
		this.reader = reader;
		this.locale = locale;
		tokenizeReader();
	}
	/**
	 * @see TokenStream#next()
	 */
	public Token next() throws IOException {
		if (readerException != null) {
			throw readerException;
		}
		if (tokens.hasNext())
			return (Token) tokens.next();
		return null;
	}
	/**
	 * Tokenizes stream and crates token iterator
	 */
	private void tokenizeReader() {
		if (reader == null) {
			tokens = new ArrayList(0).iterator();
			return;
		}
		// conver Reader to String as Required by BreakIterator
		StringBuffer strbuf = new StringBuffer();
		char[] cbuf = new char[4096];
		int l;
		try {
			while (-1 < (l = reader.read(cbuf))) {
				strbuf.append(cbuf, 0, l);
			}
			reader.close();
		} catch (IOException ioe) {
			readerException = ioe;
			return;
		}
		String str = strbuf.toString();
		// divide into words
		BreakIterator boundary = BreakIterator.getWordInstance(locale);
		boundary.setText(str);

		int start = boundary.first();
		List tokenList = new ArrayList();
		wordsbreak : for (
			int end = boundary.next();
				end != BreakIterator.DONE;
				start = end, end = boundary.next()) {
			// determine if it is a word or characters between
			// any letter between boundaries means it is a word
			for (int i = start; i < end; i++) {
				if (Character.isLetter(str.charAt(i))) {
					// it is a word
					tokenList.add(new Token(str.substring(start, end), start, end));
					continue wordsbreak;
				}
			}
		}
		// create iterator
		tokens = tokenList.iterator();

	}
}