/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.TeamException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SaveContextXMLWriter extends PrintWriter {
	protected int tab;

	/* constants */
	protected static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"; //$NON-NLS-1$

	public SaveContextXMLWriter(OutputStream output) throws UnsupportedEncodingException {
		super(new OutputStreamWriter(output, "UTF8")); //$NON-NLS-1$
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
	private void printTag(String name, HashMap parameters) {
		printTag(name, parameters, true, true);
	}
	private void printTag(String name, HashMap parameters, boolean tab, boolean newLine) {
		printTag(name, parameters, tab, newLine, false);
	}
	private void printTag(String name, HashMap parameters, boolean tab, boolean newLine, boolean end) {
		StringBuffer sb = new StringBuffer();
		sb.append("<"); //$NON-NLS-1$
		sb.append(name);
		if (parameters != null)
			for (Enumeration enum = Collections.enumeration(parameters.keySet()); enum.hasMoreElements();) {
				sb.append(" "); //$NON-NLS-1$
				String key = (String) enum.nextElement();
				sb.append(key);
				sb.append("=\""); //$NON-NLS-1$
				sb.append(getEscaped(String.valueOf(parameters.get(key))));
				sb.append("\""); //$NON-NLS-1$
			}
		if (end)
			sb.append('/');
		sb.append(">"); //$NON-NLS-1$
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
	public void startAndEndTag(String name, HashMap parameters, boolean newLine) {
		printTag(name, parameters, true, true, true);
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
				return "lt"; //$NON-NLS-1$
			case '>' :
				return "gt"; //$NON-NLS-1$
			case '"' :
				return "quot"; //$NON-NLS-1$
			case '\'' :
				return "apos"; //$NON-NLS-1$
			case '&' :
				return "amp"; //$NON-NLS-1$
		}
		return null;
	}
	public void write(SaveContext item) {
		
		// start tag for this element
		String name = item.getName();
		String value = item.getValue();
		String[] attributeNames = item.getAttributeNames();
		HashMap attributes = new HashMap(attributeNames.length);
		for (int i = 0; i < attributeNames.length; i++) {
			String attrName = attributeNames[i];
			attributes.put(attrName, item.getAttribute(attrName));			
		}
		startTag(name, attributes);
		
		// write out child elements
		SaveContext[] children = item.getChildren();
		if(children != null) {
			for (int i = 0; i < children.length; i++) {
				SaveContext child = children[i];
				write(child);
			}
		}
		
		// value
		if(value != null) {
			println(value);
		}
		
		// end tag for this element
		endTag(name);		
	}
	
	static public void writeXMLPluginMetaFile(Plugin plugin, String filename, SaveContext element) throws TeamException {		
		IPath pluginStateLocation = plugin.getStateLocation();
		File tempFile = pluginStateLocation.append(filename + ".tmp").toFile(); //$NON-NLS-1$
		File stateFile = pluginStateLocation.append(filename).toFile();
		try {
			SaveContextXMLWriter writer = new SaveContextXMLWriter(new BufferedOutputStream(new FileOutputStream(tempFile)));
			try {
				writer.write(element);
			} finally {
				writer.close();
			}
			if (stateFile.exists()) {
				stateFile.delete();
			}
			boolean renamed = tempFile.renameTo(stateFile);
			if (!renamed) {
				throw new TeamException(new Status(Status.ERROR, TeamPlugin.ID, TeamException.UNABLE, Policy.bind("RepositoryManager.rename", tempFile.getAbsolutePath()), null));
			}
		} catch (IOException e) {
			throw new TeamException(new Status(Status.ERROR, TeamPlugin.ID, TeamException.UNABLE, Policy.bind("RepositoryManager.save",stateFile.getAbsolutePath()), e)); 
		}
	}
	
	static public SaveContext readXMLPluginMetaFile(Plugin plugin, String filename) throws TeamException {
		BufferedInputStream is = null;	
		try {
			IPath pluginStateLocation = plugin.getStateLocation();
			File file = pluginStateLocation.append(filename).toFile(); //$NON-NLS-1$
			if (file.exists()) {				
				is = new BufferedInputStream(new FileInputStream(file));
				SAXParser parser = new SAXParser();
				SaveContextXMLContentHandler handler = new SaveContextXMLContentHandler();
				parser.setContentHandler(handler);
				parser.parse(new InputSource(is));
				return handler.getSaveContext();
			}
			return null;
		} catch (SAXException ex) {
			throw new TeamException(Policy.bind("RepositoryManager.ioException"), ex);			
		} catch (IOException e) {
			throw new TeamException(Policy.bind("RepositoryManager.ioException"), e);
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch (IOException e1) {
					throw new TeamException(Policy.bind("RepositoryManager.ioException"), e1);
				}
			}
		}
	}
}
