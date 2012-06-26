/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;

import com.ibm.icu.text.BreakIterator;
import java.io.IOException;
import java.io.Reader;
import java.util.Locale;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * WordTokenStream obtains tokens containing words appropriate for use with
 * Lucene search engine.
 */
public final class WordTokenStream extends Tokenizer {
	private static final int BUF_LEN = 4096;
	private final Reader reader;
	private final BreakIterator boundary;
	private StringBuffer strbuf;
	
	private int start = 0;
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

	/**
	 * Constructor
	 */
	public WordTokenStream(String fieldName, Reader reader, Locale locale) {
		this.reader = reader;
		boundary = BreakIterator.getWordInstance(locale);

	}
	/**
	 * @see TokenStream#incrementToken()
	 */
	@Override
	public boolean incrementToken() throws IOException {
	    clearAttributes();
	    int length = 0;
	    char[] buffer = termAtt.buffer();

	    int end;
	    if(strbuf == null) {
			int available;
			char[] cbuf = new char[BUF_LEN];
			while ((available = reader.read(cbuf)) <= 0) {
				if (available < 0) {
					reader.close();
					return false;
				}
			}
			strbuf = new StringBuffer(available + 80);
			strbuf.append(cbuf, 0, available);
			// read more until white space (or EOF)
			int c;
			while (0 <= (c = reader.read())) {
				strbuf.append((char) c);
				if (c == ' ' || c == '\r' || c == '\n' || c == '\t') {
					break;
				}
			}

			if (c < 0) {
				reader.close();
			}
			
			boundary.setText(strbuf.toString());
			start = boundary.first();
	    }
	    else {
	    	start = boundary.next();
	    }
	    
		for (end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
			// determine if it is a word
			// any letter or digit between boundaries means it is a word
			for (int i = start; i < end; i++) {
				if (Character.isLetterOrDigit(strbuf.charAt(i))) {
					// it is a word
					length = end - start;
					if (length >= buffer.length-1)
						 buffer = termAtt.resizeBuffer(2+length);
					termAtt.setLength(length);
					strbuf.getChars(start, end, buffer, 0);
					return true;
				}
			}
		}
		
	    return false;	
	}
	
	public void reset() throws IOException {
		super.reset();
		clearAttributes();
	}
	  
	public void close() throws IOException {
		/// Unlikely to be called as this is a reused
	    if (this.reader != null) {
	    	this.reader.close();
	    }
	}
}
