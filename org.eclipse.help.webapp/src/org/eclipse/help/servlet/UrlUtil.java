/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.servlet;
import java.io.*;

import javax.servlet.http.HttpServletRequest;

public class UrlUtil {
	public static String encode(String s) {
		try {
			return urlEncode(s.getBytes("UTF8"));
		} catch (UnsupportedEncodingException uee) {
			return null;
		}
	}
	public static String decode(String s) {
		try {
			if (s.indexOf("%u") < 0) {
				return new String(urlDecode(s), "UTF8");
			} else {
				// String was escaped in javascript 1.3
				// need to use slower unescape method
				return unescape(s);
			}
		} catch (UnsupportedEncodingException uee) {
			return null;
		}
	}
	private static String urlEncode(byte[] data) {
		StringBuffer buf = new StringBuffer(data.length);
		for (int i = 0; i < data.length; i++) {
			buf.append('%');
			buf.append(Character.forDigit((data[i] & 240) >>> 4, 16));
			buf.append(Character.forDigit(data[i] & 15, 16));
		}
		return buf.toString();
	}
	private static byte[] urlDecode(String encodedURL) {
		int len = encodedURL.length();
		ByteArrayOutputStream os = new ByteArrayOutputStream(len);
		for (int i = 0; i < len;) {
			switch (encodedURL.charAt(i)) {
				case '%' :
					if (len >= i + 3) {
						os.write(Integer.parseInt(encodedURL.substring(i + 1, i + 3), 16));
					}
					i += 3;
					break;
				case '+' : //exception from standard
					os.write(' ');
					i++;
					break;
				default :
					os.write(encodedURL.charAt(i++));
					break;
			}

		}
		return os.toByteArray();
	}
	/**
	 * Decodes strings encoded with Javascript 1.3 escape
	 */
	private static String unescape(String encodedURL) {
		int len = encodedURL.length();
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < len;) {
			ByteArrayOutputStream tempOs;
			switch (encodedURL.charAt(i)) {
				case '%' :
					if ((len > i + 1) && encodedURL.charAt(i + 1) != 'u') {
						// byte encoded as %XX
						if (len >= i + 3) {
							tempOs = new ByteArrayOutputStream(1);
							tempOs.write(Integer.parseInt(encodedURL.substring(i + 1, i + 3), 16));
							try {
								buf.append(new String(tempOs.toByteArray(), "UTF8"));
							} catch (UnsupportedEncodingException uee) {
								return null;
							}
						}
						i += 3;

					} else {
						// char escaped to the form %uHHLL
						if (len >= i + 6) {
							tempOs = new ByteArrayOutputStream(2);
							tempOs.write(Integer.parseInt(encodedURL.substring(i + 2, i + 4), 16));
							tempOs.write(Integer.parseInt(encodedURL.substring(i + 4, i + 6), 16));
							try {
								buf.append(new String(tempOs.toByteArray(), "UnicodeBigUnmarked"));
							} catch (UnsupportedEncodingException uee) {
								return null;
							}
						}
						i += 6;

					}

					break;
				case '+' : //exception from standard
					buf.append(' ');
					i++;
					break;
				default :
					tempOs = new ByteArrayOutputStream(1);
					tempOs.write(encodedURL.charAt(i++));
					try {
						buf.append(new String(tempOs.toByteArray(), "UTF8"));
					} catch (UnsupportedEncodingException uee) {
						return null;
					}

					break;
			}

		}
		return buf.toString();
	}
	/**
	 * Obtains parameter from request.
	 * request.getParameter() returns incorrect string
	 * for non ASCII queries encoded from UTF-8 bytes
	 */
	public static String getRequestParameter(
		HttpServletRequest request,
		String parameterName) {
		String query = request.getQueryString();
		if (query == null)
			return null;
		int start = query.indexOf(parameterName + "=");
		if (start < 0) {
			return null;
		} else {
			start += parameterName.length() + 1;
			if (start >= query.length()) {
				return "";
			}
		}
		int end = query.indexOf("&", start);
		if (end <= 0)
			end = query.length();
		return UrlUtil.decode(query.substring(start, end));
	}
	/**
	 * Encodes string for embedding in JavaScript source
	 */
	public static String JavaScriptEncode(String str) {
		char[] wordChars = new char[str.length()];
		str.getChars(0, str.length(), wordChars, 0);
		StringBuffer jsEncoded = new StringBuffer();
		for (int j = 0; j < wordChars.length; j++) {
			String charInHex = Integer.toString((int) wordChars[j], 16).toUpperCase();
			switch (charInHex.length()) {
				case 1 :
					jsEncoded.append("\\u000").append(charInHex);
					break;
				case 2 :
					jsEncoded.append("\\u00").append(charInHex);
					break;
				case 3 :
					jsEncoded.append("\\u0").append(charInHex);
					break;
				default :
					jsEncoded.append("\\u").append(charInHex);
					break;
			}
		}
		return jsEncoded.toString();
	}

}