/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.client;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.Policy;

/**
 * Stream which detects CRLF in text file contents recieved from the server
 */
public class CRLFDetectInputStream extends FilterInputStream {

	private boolean previousCR;
	private String filename;

	protected CRLFDetectInputStream(InputStream in, String filename) {
		super(in);
		this.filename = filename;
	}

	/**
	 * Wraps the underlying stream's method.
	 * Translates CR/LF sequences to LFs transparently.
	 * @throws InterruptedIOException if the operation was interrupted before all of the
	 *         bytes specified have been skipped, bytesTransferred will be zero
	 * @throws IOException if an i/o error occurs
	 */
	public int read() throws IOException {
		int next = in.read();
		if (next != -1) {
			testForCRLF((byte)next);
		}
		return next;
	}

	/**
	 * Wraps the underlying stream's method.
	 * Translates CR/LF sequences to LFs transparently.
	 * @throws InterruptedIOException if the operation was interrupted before all of the
	 *         bytes specified have been skipped, bytesTransferred may be non-zero
	 * @throws IOException if an i/o error occurs
	 */
	public int read(byte[] buffer, int off, int len) throws IOException {
		int count = super.read(buffer, off, len);
		for (int i = off; i < count; i++) {
			testForCRLF(buffer[i]);
		}
		return count;
	}
	
	/**
	 * Test the byte to see if a CRLF sequence was read
	 */
	private void testForCRLF(byte next) {
		if (previousCR && next == '\n') {
			CVSProviderPlugin.log(IStatus.WARNING, Policy.bind("CRLFDetectInputStream.0", filename), null); //$NON-NLS-1$
		}
		previousCR = (next == '\r');
	}
}
