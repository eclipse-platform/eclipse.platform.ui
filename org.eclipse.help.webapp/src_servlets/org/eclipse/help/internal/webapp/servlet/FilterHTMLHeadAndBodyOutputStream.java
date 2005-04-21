/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.webapp.servlet;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Filters output stream and inserts specified bytes before the end of HEAD
 * element (before &lt;/html&gt; tag) of HTML in the stream.
 */
public class FilterHTMLHeadAndBodyOutputStream extends FilterOutputStream {
	private static final int STATE_START = 0;

	private static final int STATE_LT = 1;

	private static final int STATE_LT_SLASH = 2;

	private static final int STATE_LT_SLASH_H = 3;

	private static final int STATE_LT_SLASH_HE = 4;

	private static final int STATE_LT_SLASH_HEA = 5;

	private static final int STATE_LT_SLASH_HEAD = 6;

	private static final int STATE_AFTER_HEAD = 7;

	private static final int STATE_LT_B = 8;

	private static final int STATE_LT_BO = 9;

	private static final int STATE_LT_BOD = 10;

	private static final int STATE_LT_BODY = 11;

	private static final int STATE_IN_BODY = 12;

	private static final int STATE_LT_SLASH_B = 13;

	private static final int STATE_LT_SLASH_BO = 14;

	private static final int STATE_LT_SLASH_BOD = 15;

	private static final int STATE_LT_SLASH_BODY = 16;

	private static final int STATE_DONE = 17;

	private int areaState = STATE_START;

	private int state = STATE_START;

	private byte[] toHead;

	private byte[] toInsert;

	private byte[] toAppend;

	ByteArrayOutputStream buffer = new ByteArrayOutputStream(7);

	/**
	 * Constructor.
	 * 
	 * @param out
	 *            sink output stream
	 * @param bytesForHead
	 *            bytes to insert at the end of head of HTML or
	 *            <code>null</code>
	 * @param bytesToInsert
	 *            bytes to insert at the begining of body of HTML or
	 *            <code>null</code>
	 * @param bytesToAppend
	 *            bytes to append at the end of body of HTML or
	 *            <code>null</code>
	 */
	public FilterHTMLHeadAndBodyOutputStream(OutputStream out,
			byte[] bytesForHead, byte[] bytesToInsert, byte[] bytesToAppend) {
		super(out);
		toHead = bytesForHead;
		toInsert = bytesToInsert;
		toAppend = bytesToAppend;
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
		switch (state) {
		case STATE_START:
		case STATE_AFTER_HEAD:
		case STATE_IN_BODY:
			if (b == '<') {
				buffer.write(b);
				state = STATE_LT;
			} else {
				out.write(b);
			}
			break;
		case STATE_LT:
			buffer.write(b);
			if (b == '/') {
				state = STATE_LT_SLASH;
			} else if (b == 'b' || b == 'B') {
				state = STATE_LT_B;
			} else {
				reset();
			}
			break;
		case STATE_LT_B:
			buffer.write(b);
			if (b == 'o' || b == 'O') {
				state = STATE_LT_BO;
			} else {
				reset();
			}
			break;
		case STATE_LT_BO:
			buffer.write(b);
			if (b == 'd' || b == 'D') {
				state = STATE_LT_BOD;
			} else {
				reset();
			}
			break;
		case STATE_LT_BOD:
			buffer.write(b);
			if (b == 'y' || b == 'Y') {
				state = STATE_LT_BODY;
			} else {
				reset();
			}
			break;
		case STATE_LT_BODY:
			buffer.write(b);
			if (b == '>') {
				out.write(buffer.toByteArray());
				buffer.reset();
				if (toInsert != null) {
					out.write('\n');
					out.write(toInsert);
					out.write('\n');
				}
				areaState = STATE_IN_BODY;
				state = STATE_IN_BODY;
			}
			break;
		case STATE_LT_SLASH:
			buffer.write(b);
			if (b == 'h' || b == 'H') {
				state = STATE_LT_SLASH_H;
			} else if (b == 'b' || b == 'B') {
				state = STATE_LT_SLASH_B;
			} else {
				reset();
			}
			break;
		case STATE_LT_SLASH_H:
			buffer.write(b);
			if (b == 'e' || b == 'E') {
				state = STATE_LT_SLASH_HE;
			} else {
				reset();
			}
			break;
		case STATE_LT_SLASH_HE:
			buffer.write(b);
			if (b == 'a' || b == 'A') {
				state = STATE_LT_SLASH_HEA;
			} else {
				reset();
			}
			break;
		case STATE_LT_SLASH_HEA:
			buffer.write(b);
			if (b == 'd' || b == 'D') {
				state = STATE_LT_SLASH_HEAD;
			} else {
				reset();
			}
			break;
		case STATE_LT_SLASH_HEAD:
			buffer.write(b);
			if (b == '>') {
				if (toHead != null) {
					out.write(toHead);
					out.write('\n');
				}
				areaState = STATE_AFTER_HEAD;
				state = STATE_AFTER_HEAD;
				out.write(buffer.toByteArray());
				buffer.reset();
			} else {
				reset();
			}
			break;
		case STATE_LT_SLASH_B:
			buffer.write(b);
			if (b == 'o' || b == 'O') {
				state = STATE_LT_SLASH_BO;
			} else {
				reset();
			}
			break;
		case STATE_LT_SLASH_BO:
			buffer.write(b);
			if (b == 'd' || b == 'D') {
				state = STATE_LT_SLASH_BOD;
			} else {
				reset();
			}
			break;
		case STATE_LT_SLASH_BOD:
			buffer.write(b);
			if (b == 'y' || b == 'Y') {
				state = STATE_LT_SLASH_BODY;
			} else {
				reset();
			}
			break;
		case STATE_LT_SLASH_BODY:
			buffer.write(b);
			if (b == '>') {
				if (toAppend != null) {
					out.write(toAppend);
					out.write('\n');
				}
				reset();
				areaState = STATE_DONE;
				state = STATE_DONE;
			} else {
				reset();
			}
			break;
		default: // (case STATE_DONE):
			out.write(b);
			break;
		}
	}

	private void reset() throws IOException {
		out.write(buffer.toByteArray());
		buffer.reset();
		state = areaState;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FilterOutputStream#close()
	 */
	public void close() throws IOException {
		reset();
		super.close();
	}
}
