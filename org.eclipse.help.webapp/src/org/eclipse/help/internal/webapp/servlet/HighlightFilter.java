/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.webapp.servlet;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.webapp.data.*;
import org.eclipse.help.webapp.IFilter;

public class HighlightFilter implements IFilter {
	private static final String HIGHLIGHT_ON = "highlight-on"; //$NON-NLS-1$
	
	private static final String scriptPart1 = "\n<script type=\"text/javascript\">\n<!--\nvar keywords = new Array ("; //$NON-NLS-1$
	private static final String scriptPart2 = ");\nvar pluginDefault = "; //$NON-NLS-1$
	private static final String scriptPart3 = ";\n-->\n</script>\n<script type=\"text/javascript\" src=\""; //$NON-NLS-1$
	private static final String scriptPart5 = "advanced/highlight.js\"></script>\n"; //$NON-NLS-1$

	private static final String sheetRefPart1 = "<link id=\"highlightStyle\" rel=\"STYLESHEET\" href=\""; //$NON-NLS-1$
	private static final String sheetRefPart3 = "advanced/highlight.css\" charset=\"ISO-8859-1\" type=\"text/css\"></link>\n"; //$NON-NLS-1$
	
	/*
	 * @see IFilter#filter(HttpServletRequest, OutputStream)
	 */
	public OutputStream filter(HttpServletRequest req, OutputStream out) {
		String uri = req.getRequestURI();
		if (uri == null) {
			return out;
		}
		if (!(UrlUtil.isIE(req) || UrlUtil.isMozilla(req))) {
			return out;
		}

		Collection keywords = getWords(req);
		if (keywords.size() == 0) {
			return out;
		}
		keywords = removeWildCards(keywords);
		keywords = encodeKeyWords(keywords);
		byte[] script = createJScript(req, keywords);
		if (script == null) {
			return out;
		}

		return new FilterHTMLHeadOutputStream(out, script);
	}

	/**
	 * Creates Java Script that does highlighting
	 * 
	 * @param keywords
	 * @return byte[]
	 */
	private byte[] createJScript(HttpServletRequest req, Collection keywords) {
		StringBuffer buf = new StringBuffer(scriptPart1);
		StringBuffer buf2 = new StringBuffer(sheetRefPart1);
		// append comma separated list of keywords
		Iterator it = keywords.iterator();
		if (!it.hasNext())
			return null;
		String keyword = (String) it.next();
		buf.append("\"").append(keyword).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
		while (it.hasNext()) {
			keyword = (String) it.next();
			buf.append(", \"").append(keyword).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		buf.append(scriptPart2);
		buf.append(Platform.getPreferencesService().getBoolean
				(HelpBasePlugin.PLUGIN_ID, HIGHLIGHT_ON, false, null));
		buf.append(scriptPart3);
		// append "../" to get to the webapp
		String path = FilterUtils.getRelativePathPrefix(req);
		buf.append(path);
		buf2.append(path);
		
		buf.append(scriptPart5);
		buf.append(buf2.toString());
		buf.append(sheetRefPart3);
		try {
			return buf.toString().getBytes("ASCII"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException uee) {
			return new byte[0];
		}
	}
	
	/**
	 * Extracts keywords from query that contains keywords dobule quoted and
	 * separated by space
	 * 
	 * @return Collection of String
	 */
	private Collection getWords(HttpServletRequest req) {
		// Collect words to hash set to eliminate duplcates
		Collection tokens = new ArrayList();

		String searchWord = req.getParameter("resultof"); //$NON-NLS-1$
		if (searchWord == null) {
			return tokens;
		}
		//Divide along quotation marks
		StringTokenizer qTokenizer = new StringTokenizer(searchWord.trim(),
				"\"", true); //$NON-NLS-1$
		boolean withinQuotation = false;
		String quotedString = ""; //$NON-NLS-1$
		while (qTokenizer.hasMoreTokens()) {
			String curToken = qTokenizer.nextToken();
			if (curToken.equals("\"")) { //$NON-NLS-1$
				if (!withinQuotation) {
					//beginning of quoted string
					quotedString = ""; //$NON-NLS-1$
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
	 * 
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
	 * Removes wildcard characters from words, by splitting words around wild
	 * cards
	 * 
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
			while ((index = word.indexOf("*")) >= 0) { //$NON-NLS-1$
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
			while ((index = word.indexOf("?")) >= 0) { //$NON-NLS-1$
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
