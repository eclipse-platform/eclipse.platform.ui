package org.eclipse.update.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.UnsupportedEncodingException;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.StringTokenizer;

/**
 * Encodes a <code>URL</code> into an <code>ASCII</code> readable
 * <code>URL</code> that is safe for transport. Encoded
 * <code>URL</code>s can be decoded using the <code>URLDecoder</code>.
 *
 * @see URLDecoder
 */
public final class URLEncoder {
	/**
	 * Prevents instances from being created.
	 */
	private URLEncoder() {
	}
	/**
	 * Encodes the given <code>URL</code> into an <code>ASCII</code>
	 * readable <code>URL</code> that is safe for transport. Returns the
	 * result.
	 *
	 * @return the result of encoding the given <code>URL</code> into an
	 *         <code>ASCII</code> readable <code>URL</code> that is safe for
	 *         transport
	 */
	public static String encode(String url) {
		try {
			return encode(new URL(url)).toString();
		} catch (MalformedURLException e) {
		}

		String file;
		String ref = null;

		int lastSlashIndex = url.lastIndexOf('/');
		int lastHashIndex = url.lastIndexOf('#');
		if ((lastHashIndex - lastSlashIndex > 1) && lastHashIndex < url.length() - 1) {
			file = url.substring(0, lastHashIndex);
			ref = url.substring(lastHashIndex + 1, url.length());
		} else {
			file = url;
		}

		return encode(file, ref);
	}
	/**
	 * Encodes the given file and reference parts of a <code>URL</code> into
	 * an <code>ASCII</code> readable <code>String</code> that is safe for
	 * transport. Returns the result.
	 *
	 * @return the result of encoding the given file and reference parts of
	 *         a <code>URL</code> into an <code>ASCII</code> readable
	 *         <code>String</code> that is safe for transport
	 */
	public static String encode(String file, String ref) {
		StringBuffer buf = new StringBuffer();
		StringTokenizer tokenizer = new StringTokenizer(file, "/", true); //$NON-NLS-1$

		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.equals("/")) { //$NON-NLS-1$
				buf.append(token);
			} else {
				buf.append(encodeSegment(token));
			}
		}

		if (ref != null) {
			buf.append('#');
			buf.append(encodeSegment(ref));
		}

		return buf.toString();
	}
	/**
	 * Encodes the given <code>URL</code> into an <code>ASCII</code>
	 * readable <code>URL</code> that is safe for transport. Returns the
	 * result.
	 *
	 * @return the result of encoding the given <code>URL</code> into an
	 *         <code>ASCII</code> readable <code>URL</code> that is safe for
	 *         transport
	 */
	public static URL encode(URL url) throws MalformedURLException {
		String file = url.getFile();
		String ref = url.getRef();

		return new URL(url.getProtocol(), url.getHost(), url.getPort(), encode(file, ref));
	}
	private static String encodeSegment(String segment) {
		StringBuffer result = new StringBuffer(segment.length());

		for (int i = 0; i < segment.length(); ++i) {
			char c = segment.charAt(i);
			if (mustEncode(c)) {
				byte[] bytes = null;
				try {
					bytes = new Character(c).toString().getBytes("UTF8"); //$NON-NLS-1$
				} catch (UnsupportedEncodingException e) {
					Assert.isTrue(false, e.getMessage());
				}
				for (int j = 0; j < bytes.length; ++j) {
					result.append('%');
					result.append(Integer.toHexString((bytes[j] >> 4) & 0x0F));
					result.append(Integer.toHexString(bytes[j] & 0x0F));
				}
			} else {
				result.append(c);
			}
		}

		return result.toString();
	}
	private static boolean mustEncode(char c) {
		if (c >= 'a' && c <= 'z') {
			return false;
		}

		if (c >= 'A' && c <= 'Z') {
			return false;
		}

		if (c >= '0' && c <= '9') {
			return false;
		}

		if (c >= '\'' && c <= '.') {
			return false;
		}

		if (c == '!' || c == '$' || c == '_') {
			return false;
		}

		// needed otherwise file:///c:/file/ becomes file:///C%3a/file/
		if (c ==':'){
			return false;
		}
		return true;
	}
}