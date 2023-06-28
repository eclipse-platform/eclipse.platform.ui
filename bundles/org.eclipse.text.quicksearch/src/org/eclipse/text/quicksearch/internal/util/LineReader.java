/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Provides a helper to efficiently split a file into
 * lines. Similar to BufferedReader.readline, but it also keeps
 * track of character position while reading. This is needed to
 * ease translation from line-relative offsets into stream-relative
 * offsets.
 *
 * @author Kris De Volder
 */
public class LineReader implements AutoCloseable{

	private static final int EXPECTED_LINE_LENGTH = 160;
	public static final int DEFAULT_MAX_LINE_LENGTH = 1000;

	private BufferedReader input;

	//This simple implementation just wraps a BufferedReader and StringBuilder
	//to do the buffering and String building.
	//It may be more efficient to implement our own buffering like BufferedReader
	//does.

	public LineReader(Reader reader) {
		this(reader, DEFAULT_MAX_LINE_LENGTH);
	}

	public LineReader(Reader reader, int maxLineLength) {
		input = buffered(reader);
		MAX_LINE_LENGTH = maxLineLength;
	}


	private StringBuilder line = new StringBuilder(EXPECTED_LINE_LENGTH);

	private final int MAX_LINE_LENGTH;
	private int lineOffset = -1; //Start pos of last line read.
	private int offset = 0; //position of next char in input.
	private int mark = 0; //mark offset in underlying stream

	private BufferedReader buffered(Reader reader) {
		//If already buffered don't wrap it again.
		if (reader instanceof BufferedReader) {
			return (BufferedReader) reader;
		} else {
			return new BufferedReader(reader);
		}
	}

	/**
	 * Close the underlying stream. Does nothing if already closed.
	 */
	@Override
	public void close() {
		BufferedReader toClose = null;
		synchronized (input) {
			if (input==null) {
				return;
			}
			toClose = input;
			input = null;
		}
		try {
			toClose.close();
		} catch (IOException e) {
			//Ignore.
		}
	}

	public String readLine() throws IOException {
		lineOffset = offset; //remember start of line
		int maxOffset = offset + MAX_LINE_LENGTH;
		//Read text until we see either a CR, CR LF or LF.
		int c = read();
		if (c==-1) {
			return null;
		}
		//read until newline
		while (c!='\r' && c!='\n' && c!=-1) {
			line.append((char)c);
			c = read();
			if (offset>maxOffset) {
				throw new IOException("Very long lines of text. Minified file?"); //$NON-NLS-1$
			}
		}
		//Last char read was some kind of line terminator. But only read first char of it.
		if (c=='\r') {
			mark(); //next char may be part of next line. May need to 'unread' it.
			int next = read();
			if (next == '\n') {
				//skip
			} else {
				unread();
			}
		}
		try {
			return line.toString();
		} finally {
			line.setLength(0);
		}
	}

	private void unread() throws IOException {
		offset = mark;
		input.reset();
	}

	private void mark() throws IOException {
		mark = offset;
		input.mark(1);
	}

	private int read() throws IOException {
		try {
			offset++;
			return input.read();
		} catch (IOException e) {
			//pretend errors are like EOF.
			return -1;
		}
	}

	/**
	 * @return The offset of the start of the last line read relative to beginning of the stream; or -1 if
	 * no line has been read yet.
	 */
	public int getLastLineOffset() {
		return lineOffset;
	}

}
