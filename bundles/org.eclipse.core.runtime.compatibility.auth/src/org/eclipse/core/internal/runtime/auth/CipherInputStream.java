/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime.auth;

import java.io.*;

/**
 * Decrypts a stream of data that was encrypted using the
 * <code>Cipher</code> or <code>CipherOutputStream</code>.
 *
 * @see Cipher
 * @see CipherOutputStream
 */
public class CipherInputStream extends FilterInputStream {
	private static final int SKIP_BUFFER_SIZE = 2048;
	private Cipher cipher;

	/**
	 * Constructs a new <code>CipherInputStream</code> that decrypts the
	 * data in the given <code>InputStream</code>.  The data can only be
	 * decrypted if the given password is the same as that which was used
	 * to encrypt it.
	 *
	 * @param is
	 * @param password
	 */
	public CipherInputStream(InputStream is, String password) {
		super(is);
		cipher = new Cipher(Cipher.DECRYPT_MODE, password);
	}

	/**
	 * @see InputStream#markSupported()
	 */
	public boolean markSupported() {
		return false;
	}

	/**
	 * @see InputStream#read()
	 */
	public int read() throws IOException {
		int b = super.read();
		if (b == -1)
			return -1;
		try {
			return (cipher.cipher((byte) b)) & 0x00ff;
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * @see InputStream#read(byte[], int, int)
	 */
	public int read(byte b[], int off, int len) throws IOException {
		int bytesRead = in.read(b, off, len);
		if (bytesRead == -1)
			return -1;
		try {
			byte[] result = cipher.cipher(b, off, bytesRead);
			for (int i = 0; i < result.length; ++i)
				b[i + off] = result[i];
			return bytesRead;
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * @see InputStream#skip(long)
	 */
	public long skip(long n) throws IOException {
		byte[] buffer = new byte[SKIP_BUFFER_SIZE];

		int bytesRead = 0;
		long bytesRemaining = n;

		while (bytesRead != -1 && bytesRemaining > 0) {
			bytesRead = read(buffer, 0, (int) Math.min(SKIP_BUFFER_SIZE, bytesRemaining));
			if (bytesRead > 0) {
				bytesRemaining -= bytesRead;
			}
		}

		return n - bytesRemaining;
	}
}
