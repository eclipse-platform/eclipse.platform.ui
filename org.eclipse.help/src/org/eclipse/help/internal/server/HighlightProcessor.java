package org.eclipse.help.internal.server;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.*;
import java.net.URLDecoder;
import org.eclipse.help.internal.util.TString;
import org.eclipse.help.internal.navigation.HelpNavigationManager;
import org.eclipse.help.internal.HelpSystem;

/**
 * This class should perhaps be changed a bit, and so
 * should the SearchURL. Factor out some search utility
 * and have it return the correct processed stream.
 */
class HighlightProcessor implements OutputProcessor {
	private HelpURL url;
	private static final byte[] headTagBegin = "<head".getBytes();
	private static final byte[] headTagBeginCaps = "<HEAD".getBytes();
	private static final char headTagEnd = '>';
	private static final String scriptPart1 =
		"\n<script language=\"JavaScript\">\n<!--\nvar keywords = new Array (";
	private static final String scriptPart3 =
		");\nonload=highlight;\nfunction highlight()\n{\nvar newText = document.body.createTextRange();\nfor (var i = 0; i < keywords.length; i++) {\nwhile (newText.findText(keywords[i]) )\n{\nvar replacement = newText.text\nnewText.pasteHTML(\"<span class=highlight style='background-color:lightgrey'>\" + replacement + \"</span>\");\n}\nnewText = document.body.createTextRange();\n}\n}\n// -->\n</script>\n";

	//private int docNumber;
	private String query;

	/**
	 * HighlightProcessor constructor.
	 */
	public HighlightProcessor() {
		super();
	}
	public HighlightProcessor(HelpURL url) {
		this.url = url;
		//docNumber = Integer.parseInt(url.getValue("hitOrder"));
		query = url.getValue("resultof");
	}
	/**
	 * Crates Java Script that does hightlighting
	 */
	private byte[] createJScript(Collection keywords) {
		StringBuffer buf = new StringBuffer(scriptPart1);
		Iterator it = keywords.iterator();
		if (!it.hasNext())
			return null;
		String keyword = (String) it.next();
		buf.append("\"").append(keyword).append("\"");
		while (it.hasNext()) {
			keyword = (String) it.next();
			buf.append(", \"").append(keyword).append("\"");
		}
		buf.append(scriptPart3);
		return buf.toString().getBytes();
	}
	/**
	 * Extract keywords from query in the GTR format
	 * @return Collection of String
	 */
	private Collection getKeywords() {
		Collection tokens = new ArrayList();
		//Divide along quotation marks and brackets
		StringTokenizer qTokenizer = new StringTokenizer(query.trim(), "\"()", true);
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
				quotedString += (curToken);
			} else {
				//divide not quoted strings along white space
				StringTokenizer parser = new StringTokenizer(curToken.trim());
				while (parser.hasMoreTokens()) {
					tokens.add(parser.nextToken());
				}
			}

		}

		Collection keywords = new HashSet(); // to eliminate duplicate words
		for (Iterator it = tokens.iterator(); it.hasNext();) {
			String token = (String) it.next();
			String tokenLowerCase = token.toLowerCase();
			if (!tokenLowerCase.equals("\"")
				&& !tokenLowerCase.equals("(")
				&& !tokenLowerCase.equals(")")
				&& !tokenLowerCase.equals("and")
				&& !tokenLowerCase.equals("or")
				&& !tokenLowerCase.equals("not"))
				keywords.add(token);
		}
		return keywords;

	}
	/**
	 * Encodes strings inside collection for embedding in HTML source
	 * @return Collection of String
	 */
	private Collection JavaScriptEncode(Collection col) {
		if (col == null)
			return col;
		Collection result = new ArrayList();
		for (Iterator it = col.iterator(); it.hasNext();) {
			String keyword = (String) it.next();
			result.add(TString.getUnicodeEncoding(keyword));

		}
		return result;
	}
	public byte[] processOutput(byte[] input) {
		if (query == null)
			return input;

		Collection keywords = getKeywords();
		keywords = removeWildCards(keywords);
		keywords = JavaScriptEncode(keywords);
		byte[] script = createJScript(keywords);
		if (script == null)
			return input;

		// Create new buffer
		byte[] buffer = new byte[input.length + script.length];
		int bufPointer = 0;
		int inputPointer = 0;
		boolean foundHeadTagBegin = false;
		boolean foundHeadTagEnd = false;
		while (inputPointer < input.length) {
			// copy character
			buffer[bufPointer++] = input[inputPointer++];
			// look for head tag copied
			if (!foundHeadTagEnd
				&& !foundHeadTagBegin
				&& (bufPointer >= headTagBegin.length)) {
				for (int i = 0; i < headTagBegin.length; i++) {
					if ((buffer[bufPointer - headTagBegin.length + i] != headTagBegin[i])
						&& (buffer[bufPointer - headTagBegin.length + i] != headTagBeginCaps[i])) {
						break;
					}
					if (i == headTagBegin.length - 1)
						foundHeadTagBegin = true;
				}
			}
			if (!foundHeadTagEnd && foundHeadTagBegin && buffer[bufPointer - 1] == '>') {
				foundHeadTagEnd = true;
				//embed Script
				System.arraycopy(script, 0, buffer, bufPointer, script.length);
				bufPointer += script.length;
				// copy rest
				System.arraycopy(
					input,
					inputPointer,
					buffer,
					bufPointer,
					input.length - inputPointer);
				return buffer;
			}
		}
		return buffer;
	}
	/**
	 * Removes wildcard characters from keywords, by splitting keywords around wild cards
	 * @return Collection of String
	 */
	private Collection removeWildCards(Collection col) {
		if (col == null)
			return col;

		// Split keywords into parts: before "*" and after "*"
		Collection resultPass1 = new ArrayList();
		for (Iterator it = col.iterator(); it.hasNext();) {
			String keyword = (String) it.next();
			int index;
			while((index=keyword.indexOf("*"))>=0){
				if(index>0)
					resultPass1.add(keyword.substring(0, index));
				if(keyword.length()>index)
					keyword=keyword.substring(index+1);
			}
			if(keyword.length()>0)
				resultPass1.add(keyword);
		}
		
		// Split keywords into parts: before "?" and after "?"
		Collection resultPass2 = new ArrayList();
		for (Iterator it = resultPass1.iterator(); it.hasNext();) {
			String keyword = (String) it.next();
			int index;
			while((index=keyword.indexOf("?"))>=0){
				if(index>0)
					resultPass2.add(keyword.substring(0, index));
				if(keyword.length()>index)
					keyword=keyword.substring(index+1);
			}
			if(keyword.length()>0)
				resultPass2.add(keyword);
		}
		
		return resultPass2;
	}
}
