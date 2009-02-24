/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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

/**
 * High performance reader. Assumes the input stream is ASCII.
 */
public class ASCIIReader extends Reader {
	private InputStream stream;
	int bufSize;
	byte[] buf;
	/**
	 * @param stream
	 *            InputStream
	 * @param bufSize
	 *            size of internal buffer
	 */
	public ASCIIReader(InputStream stream, int bufSize) {
		this.stream = stream;
		this.bufSize = bufSize;
		buf = new byte[bufSize];
	}

	/**
	 * @see java.io.Reader#read(char[], int, int)
	 */
	public int read(char[] cbuf, int off, int len) throws IOException {
		int n = stream.read(buf, 0, Math.min(bufSize, len));
		for (int i = 0; i < n; i++) {
			cbuf[off + i] = (char) buf[i];
		}
		return n;
	}

	/**
	 * @see java.io.Reader#close()
	 */
	public void close() throws IOException {
		stream.close();
	}

}
