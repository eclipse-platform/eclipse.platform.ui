package org.eclipse.update.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.StringTokenizer;

/**
 * Decodes a <code>URL</code> from an <code>ASCII</code> readable
 * <code>URL</code> that is safe for transport.
 *
 * @see URLEncoder
 */
public final class URLDecoder {
	/**
	 * Prevents instances from being created.
	 */
	private URLDecoder() {
	}
	/**
	 * Decodes the given <code>URL</code> from an <code>ASCII</code>
	 * readable <code>URL</code> that is safe for transport. Returns the
	 * result.
	 *
	 * @return the result of decoding the given <code>URL</code> from an
	 *         <code>ASCII</code> readable <code>URL</code> that is safe for
	 *         transport
	 */
	public static String decode(String url) {
		try {
			return decode(new URL(url)).toString();
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

		return decode(file, ref);
	}
	/**
	 * Decodes the file and reference parts of a <code>URL</code> from an
	 * <code>ASCII</code> readable <code>URL</code> that is safe for
	 * transport. Returns the result.
	 *
	 * @return the result of decoding the file and reference parts of a
	 *         <code>URL</code> from an <code>ASCII</code> readable
	 *         <code>URL</code> that is safe for transport
	 */
	public static String decode(String file, String ref) {
		StringBuffer buf = new StringBuffer();
		StringTokenizer tokenizer = new StringTokenizer(file, "/", true);

		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.equals("/")) {
				buf.append(token);
			} else {
				buf.append(decodeSegment(token));
			}
		}

		if (ref != null) {
			buf.append('#');
			buf.append(decodeSegment(ref));
		}

		return buf.toString();
	}
	/**
	 * Decodes the given <code>URL</code> from an <code>ASCII</code>
	 * readable <code>URL</code> that is safe for transport. Returns the
	 * result.
	 *
	 * @return the result of decoding the given <code>URL</code> from an
	 *         <code>ASCII</code> readable <code>URL</code> that is safe for
	 *         transport
	 */
	public static URL decode(URL url) {
		String file = url.getFile();
		String ref = url.getRef();

		try {
			return new URL(url.getProtocol(), url.getHost(), url.getPort(), decode(file, ref));
		} catch (MalformedURLException e) {
			Assert.isTrue(false, e.getMessage());
		}

		return null;
	}
	private static String decodeSegment(String segment) {
		StringBuffer result = new StringBuffer(segment.length());
		ByteArrayOutputStream buff = new ByteArrayOutputStream(10);

		for (int i = 0; i < segment.length(); ++i) {
			char c = segment.charAt(i);
			if (c == '%') {
				while (c == '%') {
					String hex = segment.substring(i + 1, i + 3);
					buff.write(Integer.parseInt(hex, 16));
					i = i + 3;
					c = segment.charAt(i);
				}
				try {
					result.append(buff.toString("UTF8"));
				} catch (UnsupportedEncodingException e) {
					Assert.isTrue(false, e.getMessage());
				}
				buff.reset();
				--i;
			} else {
				result.append(c);
			}
		}

		return result.toString();
	}
}