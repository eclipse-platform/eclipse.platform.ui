package org.eclipse.team.internal.ccvs.ssh;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
public abstract class Cipher {
public abstract void decipher(byte[] src, int srcPos, byte[] dst, int dstPos, int len);
public abstract void encipher(byte[] src, int srcPos, byte[] dst, int dstPos, int len);
public static Cipher getInstance(String algorithm) {
	try {
		Class c = Class.forName("org.eclipse.team.internal.ccvs.ssh." + algorithm);
		return (Cipher) c.newInstance();
	} catch (Exception e) {
		return null;
	}
}
public abstract void setKey(byte[] key);
}
