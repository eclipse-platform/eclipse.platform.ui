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

package org.eclipse.help.internal.webapp.servlet;

import java.io.*;

/**
 * Filter output stream that inserts specified bytes after the HEAD element of
 * HTML in the stream.
 */
public class FilterHTMLHeadOutputStream extends FilterOutputStream {
	private static final int STATE_START = 0;
	private static final int STATE_LT = 1;
	private static final int STATE_LT_H = 2;
	private static final int STATE_LT_HE = 3;
	private static final int STATE_LT_HEA = 4;
	private static final int STATE_LT_HEAD = 5;
	private static final int STATE_DONE = 6;
	private int state = STATE_START;
	private byte[] toInsert;

	/**
	 * Constructor.
	 * 
	 * @param out
	 *            sink output stream
	 * @param bytesToInsert
	 *            bytes to insert in head of HTML
	 */
	public FilterHTMLHeadOutputStream(OutputStream out, byte[] bytesToInsert) {
		super(out);
		toInsert = bytesToInsert;
	}
	/**
	 * Writes the specified <code>byte</code> to this output stream.
	 * <p>
	 * The underlying stream might have a more bytes written to it, following
	 * the &lt;head&gt; HTML element.
	 * <p>
	 * Implements the abstract <tt>write</tt> method of <tt>OutputStream</tt>.
	 * 
	 * @param b
	 *            the <code>byte</code>.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	public final void write(int b) throws IOException {
		out.write(b);
		switch (state) {
			case STATE_START :
				if (b == '<') {
					state = STATE_LT;
				}
				break;
			case STATE_LT :
				if (b == 'h' || b == 'H') {
					state = STATE_LT_H;
				} else {
					state = STATE_START;
				}
				break;
			case STATE_LT_H :
				if (b == 'e' || b == 'E') {
					state = STATE_LT_HE;
				} else {
					state = STATE_START;
				}
				break;
			case STATE_LT_HE :
				if (b == 'a' || b == 'A') {
					state = STATE_LT_HEA;
				} else {
					state = STATE_START;
				}
				break;
			case STATE_LT_HEA :
				if (b == 'd' || b == 'D') {
					state = STATE_LT_HEAD;
				} else {
					state = STATE_START;
				}
				break;
			case STATE_LT_HEAD :
				if (b == '>') {
					// insert extra bytes here
					out.write(toInsert);
					state = STATE_DONE;
				} else if (b == '<') {
					state = STATE_START;
				}
				break;
			default : // (case STATE_DONE):
				break;
		}
	}

	public void write(byte b[], int off, int len) throws IOException {
		if (state == STATE_DONE) {
			out.write(b, off, len);
		} else {
			for (int i = 0; i < len; i++) {
				write(b[off + i]);
			}
		}
	}

}
