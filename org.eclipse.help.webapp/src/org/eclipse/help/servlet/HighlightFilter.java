/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

package org.eclipse.help.servlet;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

public class HighlightFilter implements IFilter {
	private String searchWord;
	private HttpServletRequest request;

	private static final String scriptPart1 =
		"\n<script language=\"JavaScript\">\n<!--\nvar keywords = new Array (";
	private static final String scriptPart3 =
		");\n-->\n</script>\n<script language=\"JavaScript\" src=\"";
	private static final String scriptPart5 = "highlight.js\"></script>\n";

	/**
	 * Constructor.
	 */
	public HighlightFilter(HttpServletRequest request) {
		this.request = request;
		this.searchWord = request.getParameter("resultof");
	}

	/*
	 * @see IFilter#filter(byte[])
	 */
	public byte[] filter(byte[] input) {
		if (searchWord == null) {
			return input;
		}
		if (!(UrlUtil.isIE(request) || UrlUtil.isMozilla(request))) {
			return input;
		}

		Collection keywords = getWords();
		keywords = removeWildCards(keywords);
		keywords = encodeKeyWords(keywords);
		byte[] script = createJScript(keywords);
		if (script == null) {
			return input;
		}

		return HeadFilterHelper.filter(input, script);
	}

	/**
	 * Creates Java Script that does highlighting
	 */
	private byte[] createJScript(Collection keywords) {
		StringBuffer buf = new StringBuffer(scriptPart1);
		// append comma separated list of keywords
		Iterator it = keywords.iterator();
		if (!it.hasNext())
			return null;
		String keyword = (String) it.next();
		buf.append("\"").append(keyword).append("\"");
		while (it.hasNext()) {
			keyword = (String) it.next();
			buf.append(", \"").append(keyword).append("\"");
		}
		//
		buf.append(scriptPart3);
		// append "../" to get to the webapp
		String path = request.getPathInfo();
		if (path != null) {
			for (int i;
				0 <= (i = path.indexOf('/'));
				path = path.substring(i + 1)) {
				buf.append("../");
			}
		}
		//
		buf.append(scriptPart5);
		return buf.toString().getBytes();
	}
	/**
	 * Extracts keywords from query that contains
	 * keywords dobule quoted and separated by space
	 * @return Collection of String
	 */
	private Collection getWords() {
		// Collect words to hash set to eliminate duplcates
		Collection tokens = new HashSet();
		//Divide along quotation marks
		StringTokenizer qTokenizer =
			new StringTokenizer(searchWord.trim(), "\"", true);
		boolean withinQuotation = false;
		String quotedString = "";
		while (qTokenizer.hasMoreTokens()) {
			String curToken = qTokenizer.nextToken();
			if (curToken.equals("\"")) {
				if (!withinQuotation) {
					//beginning of quoted string
					quotedString = "";
				} else {
					//end of quoted string
					tokens.add(quotedString);
				}
				withinQuotation = !withinQuotation;
				continue;
			}
			if (withinQuotation) {
				tokens.add(curToken);
			}
		}

		return tokens;

	}
	/**
	 * Encodes strings inside collection for embedding in HTML source
	 * @return Collection of String
	 */
	private Collection encodeKeyWords(Collection col) {
		if (col == null)
			return col;
		Collection result = new ArrayList();
		for (Iterator it = col.iterator(); it.hasNext();) {
			String word = (String) it.next();
			int l = word.length();
			if (l < 1)
				continue;
			result.add(UrlUtil.JavaScriptEncode(word));
		}
		return result;
	}

	/**
	 * Removes wildcard characters from words, by splitting words around wild cards
	 * @return Collection of String
	 */
	private Collection removeWildCards(Collection col) {
		if (col == null)
			return col;

		// Split words into parts: before "*" and after "*"
		Collection resultPass1 = new ArrayList();
		for (Iterator it = col.iterator(); it.hasNext();) {
			String word = (String) it.next();
			int index;
			while ((index = word.indexOf("*")) >= 0) {
				if (index > 0)
					resultPass1.add(word.substring(0, index));
				if (word.length() > index)
					word = word.substring(index + 1);
			}
			if (word.length() > 0)
				resultPass1.add(word);
		}

		// Split words into parts: before "?" and after "?"
		Collection resultPass2 = new ArrayList();
		for (Iterator it = resultPass1.iterator(); it.hasNext();) {
			String word = (String) it.next();
			int index;
			while ((index = word.indexOf("?")) >= 0) {
				if (index > 0)
					resultPass2.add(word.substring(0, index));
				if (word.length() > index)
					word = word.substring(index + 1);
			}
			if (word.length() > 0)
				resultPass2.add(word);
		}

		return resultPass2;
	}
}