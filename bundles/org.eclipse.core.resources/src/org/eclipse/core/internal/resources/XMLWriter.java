package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.*;
/**
 * A simple XML writer.
 */
public class XMLWriter extends PrintWriter {
	protected int tab;

	/* constants */
	protected static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

public XMLWriter(OutputStream output) throws UnsupportedEncodingException {
	super(new OutputStreamWriter(output, "UTF8"));
	tab = 0;
	println(XML_VERSION);
}
public void endTag(String name) {
	tab--;
	printTag('/' + name, null);
}
public void printSimpleTag(String name, Object value) {
	if (value != null) {
		printTag(name, null, true, false);
		print(getEscaped(String.valueOf(value)));
		printTag('/' + name, null, false, true);
	}
}
public void printTabulation() {
	for (int i = 0; i < tab; i++)
		super.print('\t');
}
public void printTag(String name, HashMap parameters) {
	printTag(name, parameters, true, true);
}
public void printTag(String name, HashMap parameters, boolean tab, boolean newLine) {
	StringBuffer sb = new StringBuffer();
	sb.append("<");
	sb.append(name);
	if (parameters != null)
		for (Enumeration enum = Collections.enumeration(parameters.keySet()); enum.hasMoreElements();) {
			sb.append(" ");
			String key = (String) enum.nextElement();
			sb.append(key);
			sb.append("=\"");
			sb.append(getEscaped(String.valueOf(parameters.get(key))));
			sb.append("\"");
		}
	sb.append(">");
	if (tab)
		printTabulation();
	if (newLine)
		println(sb.toString());
	else
		print(sb.toString());
}
public void startTag(String name, HashMap parameters) {
	startTag(name, parameters, true);
}
public void startTag(String name, HashMap parameters, boolean newLine) {
	printTag(name, parameters, true, newLine);
	tab++;
}
private static void appendEscapedChar(StringBuffer buffer, char c) {
	String replacement = getReplacement(c);
	if (replacement != null) {
		buffer.append('&');
		buffer.append(replacement);
		buffer.append(';');
	} else {
		buffer.append(c);
	}
}
public static String getEscaped(String s) {
	StringBuffer result = new StringBuffer(s.length() + 10);
	for (int i = 0; i < s.length(); ++i)
		appendEscapedChar(result, s.charAt(i));
	return result.toString();
}
private static String getReplacement(char c) {
	// Encode special XML characters into the equivalent character references.
	// These five are defined by default for all XML documents.
	switch (c) {
		case '<' :
			return "lt";
		case '>' :
			return "gt";
		case '"' :
			return "quot";
		case '\'' :
			return "apos";
		case '&' :
			return "amp";
	}
	return null;
}
}
