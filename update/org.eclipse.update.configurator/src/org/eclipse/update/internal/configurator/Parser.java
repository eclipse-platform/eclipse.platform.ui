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
package org.eclipse.update.internal.configurator;

import java.io.*;
import java.net.*;
import java.util.*;


public class Parser {

	private ArrayList elements = new ArrayList();

	/*
	 * Construct parser for the specified file
	 */
	public Parser(File file) {
		try {
			load(new FileInputStream(file));
		} catch (Exception e) {
			// continue ... actual parsing will report errors
		}
	}

	/*
	 * Construct parser for the specified URL
	 */
	public Parser(URL url) {
		try {
			load(url.openStream());
		} catch (Exception e) {
			// continue ... actual parsing will report errors
		}
	}

	/*
	 * Return selected elements as an (attribute-name, attribute-value) map.
	 * The name of the selected element is returned as the value of entry with
	 * name "<element>".
	 * @return attribute map for selected element, or <code>null</code>
	 */
	public HashMap getElement(Selector selector) {
		if (selector == null)
			return null;

		String element;
		for (int i = 0; i < elements.size(); i++) {
			// make pre-parse selector call
			element = (String) elements.get(i);
			if (selector.select(element)) {
				// parse selected entry
				HashMap attributes = new HashMap();
				String elementName;
				int j;
				// parse out element name
				for (j = 0; j < element.length(); j++) {
					if (Character.isWhitespace(element.charAt(j)))
						break;
				}
				if (j >= element.length()) {
					elementName = element;
				} else {
					elementName = element.substring(0, j);
					element = element.substring(j);
					// parse out attributes
					StringTokenizer t = new StringTokenizer(element, "=\""); //$NON-NLS-1$
					boolean isKey = true;
					String key = ""; //$NON-NLS-1$
					while (t.hasMoreTokens()) {
						String token = t.nextToken().trim();
						if (!token.equals("")) { //$NON-NLS-1$
							// collect (key, value) pairs
							if (isKey) {
								key = token;
								isKey = false;
							} else {
								attributes.put(key, token);
								isKey = true;
							}
						}
					}
				}
				// make post-parse selector call
				if (selector.select(elementName, attributes)) {
					attributes.put("<element>", elementName); //$NON-NLS-1$
					return attributes;
				}
			}
		}
		return null;
	}

	private void load(InputStream is) {
		if (is == null)
			return;

		// read file
		StringBuffer xml = new StringBuffer(4096);
		char[] iobuf = new char[4096];
		InputStreamReader r = null;
		try {
			r = new InputStreamReader(is);
			int len = r.read(iobuf, 0, iobuf.length);
			while (len != -1) {
				xml.append(iobuf, 0, len);
				len = r.read(iobuf, 0, iobuf.length);
			}
		} catch (Exception e) {
			return;
		} finally {
			if (r != null)
				try {
					r.close();
				} catch (IOException e) {
					// ignore
				}
		}

		// parse out element tokens
		String xmlString = xml.toString();
		StringTokenizer t = new StringTokenizer(xmlString, "<>"); //$NON-NLS-1$
		while (t.hasMoreTokens()) {
			String token = t.nextToken().trim();
			if (!token.equals("")) //$NON-NLS-1$
				elements.add(token);
		}
	}
}