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
package org.eclipse.help.internal.search;

import java.io.*;

/**
 * Parsed Document. It can be used to obtain multiple readers for the same
 * document.
 */
public class ParsedDocument {
	// Limit on how many characters will be indexed
	// from a large document
	private static final int charsLimit = 1000000;
	Reader reader;
	boolean read;
	char[] docChars;

	/**
	 * Constructor for ParsedDocument.
	 * 
	 * @param reader
	 *            reader obtained from the parser
	 */
	public ParsedDocument(Reader reader) {
		this.reader = reader;
		this.read = false;
	}
	public Reader newContentReader() {
		if (!read) {
			read = true;
			readDocument();
		}
		return new CharArrayReader(docChars);
	}
	private void readDocument() {
		CharArrayWriter writer = new CharArrayWriter();
		char[] buf = new char[4096];
		int n;
		int charsWritten = 0;
		try {
			while (0 <= (n = reader.read(buf))) {
				if (charsWritten < charsLimit) {
					if (n > charsLimit - charsWritten) {
						// do not exceed the specified limit of characters
						writer.write(buf, 0, charsLimit - charsWritten);
						charsWritten = charsLimit;
					} else {
						writer.write(buf, 0, n);
						charsWritten += n;
					}
				} else {
					// do not break out of the loop
					// keep reading to avoid breaking pipes
				}
			}
		} catch (IOException ioe) {
			// do not do anything, will use characters read so far
		} finally {
			try {
				reader.close();
			} catch (IOException ioe2) {
			}
		}
		docChars = writer.toCharArray();
	}
}
