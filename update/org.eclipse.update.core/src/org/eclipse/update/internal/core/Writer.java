package org.eclipse.update.internal.core;


/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintWriter;

import org.eclipse.update.configuration.*;
import org.eclipse.update.configuration.IInstallConfiguration;
import org.eclipse.update.configuration.ILocalSite;
import org.eclipse.update.core.ISite;

public class Writer {
public Writer() {
	super();
}

public void writeSite(IWritable element, PrintWriter w) {

	w.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	w.println("");
	w.println("<!-- File written by Update manager 2.0 -->");
	w.println("<!-- comments in this file are not preserved -->");
	w.println("");
	((IWritable)element).write(0,w);

}

/**
 * Method appendEscapedChar.
 * @param buffer
 * @param c
 */
private static void appendEscapedChar(StringBuffer buffer, char c) {
	String replacement = getReplacement(c);
	if (replacement != null) {
		buffer.append('&');
		buffer.append(replacement);
		buffer.append(';');
	} else {
		if ((c >= ' ' && c <= 0x7E) || c == '\n' || c == '\r' || c == '\t') {
			buffer.append(c);
		} else {
			buffer.append("&#");
			buffer.append(Integer.toString(c));
			buffer.append(';');
		}
	}
}


/**
 * Method xmlSafe.
 * @param s
 * @return String
 */
public static String xmlSafe(String s) {
	StringBuffer result = new StringBuffer(s.length() + 10);
	for (int i = 0; i < s.length(); ++i)
		appendEscapedChar(result, s.charAt(i));
	return result.toString();
}
/**
 * Method getReplacement.
 * @param c
 * @return String
 */
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
