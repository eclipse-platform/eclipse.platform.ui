package org.eclipse.core.internal.runtime;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * <P>Encrypts or decrypts a sequence of bytes. The bytes are decrypted
 * by supplying the same password that was given when the bytes were
 * encrypted.
 * <P>Here is an example showing how to encrypt and then decrypt the
 * string "Hello, world!" using the password "music":
 * <pre>
 *     String password = "music";
 *     byte[] data = "Hello, world!".getBytes("UTF8");
 *
 *     // Encrypt
 *     Cipher cipher = new Cipher(ENCRYPT_MODE, password);
 *     byte[] encrypted = cipher.cipher(data);
 *
 *     // Decrypt
 *     cipher = new Cipher(DECRYPT_MODE, password);
 *     byte[] decrypted = cipher.cipher(encrypted);
 * </pre>
 */
public class Cipher {
	public static final int DECRYPT_MODE = -1;
	public static final int ENCRYPT_MODE = 1;

	private int mode = 0;
	private String password = null;
	private SecureRandom secureRandom = null;
/**
 * Initializes the cipher with the given mode and password. This method
 * must be called first (before any encryption of decryption takes
 * place) to specify whether the cipher should be in encrypt or decrypt
 * mode and to set the password.
 *
 * @param mode
 * @param password
 */
public Cipher (int mode, String password){
	this.mode = mode;
	this.password = password;
	this.secureRandom = null;
}
/**
 * Encrypts or decrypts (depending on which mode the cipher is in) the
 * given data and returns the result.
 *
 * @param data
 * @return     the result of encrypting or decrypting the given data
 */
public byte[] cipher(byte[] data) throws Exception {
	return transform(data, 0, data.length, mode);
}
/**
 * Encrypts or decrypts (depending on which mode the cipher is in) the
 * given data and returns the result.
 *
 * @param data the byte array containg the given data
 * @param off  the index of the first byte in the given byte array
 *             to be transformed
 * @param len  the index after the last byte in the given byte array
 *             to be transformed
 * @return     the result of encrypting or decrypting the given data
 */
public byte[] cipher(byte[] data, int off, int len) throws Exception {
	return transform(data, off, len, mode);
}
/**
 * Encrypts or decrypts (depending on which mode the cipher is in) the
 * given byte and returns the result.
 *
 * @param datum the given byte
 * @return      the result of encrypting or decrypting the given byte
 */
public byte cipher(byte datum) throws Exception {
	byte[] data = { datum };
	return cipher(data)[0];
}
private byte[] getSeed() throws Exception {
	MessageDigest messageDigest = MessageDigest.getInstance("SHA"); //$NON-NLS-1$
	return messageDigest.digest(password.getBytes("UTF8")); //$NON-NLS-1$
}
private byte[] nextRandom(int length) throws Exception {
	if (secureRandom == null) {
		secureRandom = SecureRandom.getInstance("SHA1PRNG"); //$NON-NLS-1$
		secureRandom.setSeed(getSeed());
	}
	byte[] nextRandom = new byte[length];
	secureRandom.nextBytes(nextRandom);
	return nextRandom;
}
private byte[] transform(byte[] data, int off, int len, int mode) throws Exception {
	byte[] result = nextRandom(len);
	for (int i = 0; i < len; ++i) {
		result[i] = (byte) (data[i + off] + mode * result[i]);
	}
	return result;
}
}
