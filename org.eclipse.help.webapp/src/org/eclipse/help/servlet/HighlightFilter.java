/*
 * (c) Copyright IBM Corp. 2000-2002.
 * All Rights Reserved.
 */
 
package org.eclipse.help.servlet;

import java.util.*;
import java.net.URLDecoder;


public class HighlightFilter implements IFilter
{
	private String searchWord;

	private static final String scriptPart1 =
		"\n<script language=\"JavaScript\">\n<!--\nvar keywords = new Array (";
	private static final String scriptPart3 =
		");\nonload=highlight;\nfunction highlight()\n{\nvar newText = document.body.createTextRange();\nfor (var i = 0; i < keywords.length; i++) {\nif (keywords[i].length<3) continue;\nwhile (newText.findText(keywords[i]) )\n{\nvar replacement = newText.htmlText\nnewText.pasteHTML(\"<span class=highlight style='background-color:Highlight;color:HighlightText;'>\" + replacement + \"</span>\");\n}\nnewText = document.body.createTextRange();\n}\n}\n// -->\n</script>\n";


	/**
	 * Constructor.
	 */
	public HighlightFilter(String searchWord) {
		this.searchWord = searchWord;
	}

	/*
	 * @see IFilter#filter(byte[])
	 */
	public byte[] filter(byte[] input)
	{
		if (searchWord == null)
			return input;

		Collection keywords = getWords();
		keywords = removeWildCards(keywords);
		keywords = JavaScriptEncode(keywords);
		byte[] script = createJScript(keywords);
		if (script == null)
			return input;

		return HeadFilterHelper.filter(input, script);
	}
	
	/**
	 * Creates Java Script that does highlighting
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
	 * Extracts keywords from query 
	 * @return Collection of String
	 */
	private Collection getWords() {
		Collection tokens = new ArrayList();
		//Divide along quotation marks and brackets
		StringTokenizer qTokenizer = new StringTokenizer(searchWord.trim(), "\"()", true);
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

		Collection words = new HashSet(); // to eliminate duplicate words
		for (Iterator it = tokens.iterator(); it.hasNext();) {
			String token = (String) it.next();
			String tokenLowerCase = token.toLowerCase();
			if (!tokenLowerCase.equals("\"")
				&& !tokenLowerCase.equals("(")
				&& !tokenLowerCase.equals(")")
				&& !tokenLowerCase.equals("and")
				&& !tokenLowerCase.equals("or")
				&& !tokenLowerCase.equals("not"))
				words.add(token);
		}
		return words;

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
			String word = (String) it.next();
			
			int l = word.length();
			if (l < 1)
				continue;
			char[] wordChars = new char[l];
			word.getChars(0, l, wordChars, 0);
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
			result.add(jsEncoded.toString());

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
			while((index=word.indexOf("*"))>=0){
				if(index>0)
					resultPass1.add(word.substring(0, index));
				if(word.length()>index)
					word=word.substring(index+1);
			}
			if(word.length()>0)
				resultPass1.add(word);
		}
		
		// Split words into parts: before "?" and after "?"
		Collection resultPass2 = new ArrayList();
		for (Iterator it = resultPass1.iterator(); it.hasNext();) {
			String word = (String) it.next();
			int index;
			while((index=word.indexOf("?"))>=0){
				if(index>0)
					resultPass2.add(word.substring(0, index));
				if(word.length()>index)
					word=word.substring(index+1);
			}
			if(word.length()>0)
				resultPass2.add(word);
		}
		
		return resultPass2;
	}
}
