/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.search;

import java.io.*;

/**
 * Parsed Document.
 * It can be used to obtain multiple readers for the same document.
 */
public class ParsedDocument {
	Reader reader;
	boolean read;
	char[] docChars;

	/**
	 * Constructor for ParsedDocument.
	 * @param reader reader obtained from the parser
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
		try {
			while (0 <= (n = reader.read(buf))) {
				writer.write(buf, 0, n);
			}
		} catch (IOException ioe) {
			// do not do anything, will use characters read so far
		}
		docChars=writer.toCharArray();
	}
}
