package org.eclipse.core.internal.resources;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import java.io.*;
import java.util.*;
/**
 * A simple XML writer.
 */
public class XMLWriter extends PrintWriter {
	protected int tab;

	/* constants */
	protected static final String XML_VERSION = "<?xml version=\"1.0\"?>";
public XMLWriter(OutputStream output) {
	super(output);
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
		print(value);
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
			sb.append(parameters.get(key));
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
}
