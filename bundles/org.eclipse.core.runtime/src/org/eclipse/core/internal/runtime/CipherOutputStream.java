package org.eclipse.core.internal.runtime;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;

/**
 * Encrypts a stream of data that can be decrypted using the
 * <code>Cipher</code> or <code>CipherInputStream</code>.
 *
 * @see Cipher
 * @see CipherInputStream
 */
public class CipherOutputStream extends FilterOutputStream {
	private Cipher cipher;
/**
 * Constructs a new <code>CipherOutputStream</code> that encrypts the
 * data in the given <code>OutputStream</code>.  Once the data is
 * encrypted it can be decrypted by suppying the encrupted data and
 * given password to a <code>Cipher</code> or
 * <code>CipherInputStream</code>.
 *
 * @param os
 * @param password
 */
public CipherOutputStream(OutputStream os, String password) {
	super(os);
	cipher = new Cipher(Cipher.ENCRYPT_MODE, password);
}
/**
 * @see OutputStream#write(int)
 */
public void write(int b) throws IOException {
	try {
		out.write(cipher.cipher((byte) b));
	} catch (Exception e) {
		throw new IOException(e.getMessage());
	}
}
}
