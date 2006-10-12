/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.dynamic;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.help.HelpSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/*
 * Resolves includes by parsing the target document, extracting the element and
 * replacing the original include with it.
 */
public class IncludeResolver {
	
	private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	
	private DOMProcessor processor;
	private DOMReader reader;
	private String locale;
	
	/*
	 * Creates the resolver. It must have a DOMProcessor for processing the
	 * included content, and must know the locale of the content to include.
	 */
	public IncludeResolver(DOMProcessor processor, String locale) {
		this.processor = processor;
		this.locale = locale;
	}
	
	/*
	 * Resolves the include target to a processed Element.
	 */
	public Element resolve(String bundleId, String relativePath, String elementId) throws IOException, SAXException, ParserConfigurationException {
		String href = '/' + bundleId + '/' + relativePath;
		InputStream in = HelpSystem.getHelpContent(href, locale);
		try {
			Element element = findElement(in, elementId);
			processor.process(element, href);
			return element;
		}
		finally {
			try {
				in.close();
			}
			catch (IOException e) {}
		}
	}
	
	/*
	 * Finds the specified element from the given XML input stream.
	 */
	private Element findElement(InputStream in, String elementId) throws IOException, SAXException, ParserConfigurationException {
		if (reader == null) {
			reader = new DOMReader();
		}
		Document document = reader.read(in);
		Element elem = document.getElementById(elementId);
		if (elem != null) {
			return elem;
		}
		return findElement(document.getDocumentElement(), elementId);
	}
	
	/*
	 * Finds the specified element under the given DOM subtree.
	 */
	private Element findElement(Element elem, String elementId) {
		if (elem.getAttribute(ATTRIBUTE_ID).equals(elementId)) {
			return elem;
		}
		Node child = elem.getFirstChild();
		while (child != null) {
			Node next = child.getNextSibling();
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				Element result = findElement((Element)child, elementId);
				if (result != null) {
					return result;
				}
			}
			child = next;
		}
		return null;
	}
}
