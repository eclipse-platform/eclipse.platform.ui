package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.UnsupportedEncodingException;

class Convert {
		
	/**
	 * Converts the string argument to a byte array.
	 */
	static String fromUTF8(byte[] b) {
		String result;
		try {
			result = new String(b,"UTF8");
		} catch (UnsupportedEncodingException e) {
			result = new String(b);
		}
		return result;
	}
	/**
	 * Converts the string argument to a byte array.
	 */
	static byte[] toUTF8(String s) {
		byte[] result;
		try {
			result = s.getBytes("UTF8");
		}
		catch (UnsupportedEncodingException e) {
			result = s.getBytes();
		}
		return result;
	}
}
