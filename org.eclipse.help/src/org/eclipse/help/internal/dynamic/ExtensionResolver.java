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
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContentExtension;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.extension.ContentExtensionManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*
 * Resolves the content to use for extensions. For anchors, these are the
 * elements that will be inserted at the anchor. For replaces, these are the
 * elements that will replace the original element.
 */
public class ExtensionResolver {

	private static final String ELEMENT_BODY = "body"; //$NON-NLS-1$
	private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	
	private DOMProcessor processor;
	private DOMReader reader;
	private String locale;
	private ContentExtensionManager manager;
	
	/*
	 * Creates the resolver. The processor is needed to process the extension
	 * content, and locale because we're pulling in content from other documents.
	 */
	public ExtensionResolver(DOMProcessor processor, String locale) {
		this.processor = processor;
		this.locale = locale;
	}
	
	/*
	 * Resolves the given path (the element to be extended) into DOM nodes.
	 */
	public Node[] resolveExtension(String path, int type) {
		if (manager == null) {
			manager = HelpPlugin.getContentExtensionManager();
		}
		IContentExtension[] extensions = manager.getExtensions(path, type, locale);
		List list = new ArrayList();
		for (int i=0;i<extensions.length;++i) {
			String content = extensions[i].getContent();
			try {
				Node[] nodes = getContent(content);
				for (int j=0;j<nodes.length;++j) {
					list.add(nodes[j]);
				}
			}
			catch (Throwable t) {
				// ignore invalid extensions
			}
		}
		return (Node[])list.toArray(new Node[list.size()]);
	}
	
	/*
	 * Resolves the given content path (the content to insert/replace with) into
	 * DOM elements.
	 */
	private Node[] getContent(String content) throws IOException, SAXException, ParserConfigurationException {
		String bundleId = null;
		String relativePath = null;
		String elementId = null;
		
		int bundleStart = 0;
		// legacy; can omit leading slash
		if (content.charAt(0) == '/') {
			bundleStart = 1;
		}
		int bundleEnd = content.indexOf('/', bundleStart + 1);
		if (bundleEnd > bundleStart) {
			bundleId = content.substring(bundleStart, bundleEnd);
			int pathStart = bundleEnd + 1;
			int pathEnd = content.indexOf('#', pathStart + 1);
			if (pathEnd == -1) {
				// legacy; slash can be used instead of '#'
				int lastSlash = content.lastIndexOf('/');
				int secondLastSlash = content.lastIndexOf('/', lastSlash - 1);
				if (secondLastSlash != -1 && lastSlash > secondLastSlash) {
					String secondLastToken = content.substring(secondLastSlash + 1, lastSlash);
					if (secondLastToken.indexOf('.') != -1) {
						pathEnd = lastSlash;
					}
					else {
						pathEnd = content.length();
					}
				}
				else {
					pathEnd = content.length();
				}
			}
			relativePath = content.substring(pathStart, pathEnd);
			if (pathEnd < content.length()) {
				elementId = content.substring(pathEnd + 1);
			}
		}
		
		if (bundleId != null && relativePath != null) {
			return getContent(bundleId, relativePath, elementId);
		}
		return null;
	}
	
	/*
	 * Resolves the given parsed content fragments into DOM elements.
	 */
	private Node[] getContent(String bundleId, String relativePath, String elementId) throws IOException, SAXException, ParserConfigurationException {
		String href = '/' + bundleId + '/' + relativePath;
		InputStream in = HelpSystem.getHelpContent(href, locale);
		try {
			if (elementId != null) {
				Element element = findElement(in, elementId);
				processor.process(element, href);
				return new Node[] { element };
			}
			Node[] nodes = findBody(in);
			for (int i=0;i<nodes.length;++i) {
				if (nodes[i].getNodeType() == Node.ELEMENT_NODE) {
					processor.process((Element)nodes[i], href);
				}
			}
			return nodes;
		}
		finally {
			try {
				in.close();
			}
			catch (IOException e) {}
		}
	}
	
	/*
	 * Finds and returns the element with the given elementId from the
	 * XML input stream, or null if not found.
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
	 * Finds and returns the element with the given elementId from the
	 * under the given element, or null if not found.
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
	
	/*
	 * Extracts and returns the contents of the first body element (excluding
	 * the body element itself) in the given XML input stream.
	 */
	private Node[] findBody(InputStream in) throws IOException, SAXException, ParserConfigurationException {
		if (reader == null) {
			reader = new DOMReader();
		}
		Document document = reader.read(in);
		return findBody(document.getDocumentElement());
	}
	
	/*
	 * Finds and returns the contents of the first body element under the
	 * given element (excluding the body element itself).
	 */
	private Node[] findBody(Element elem) {
		if (ELEMENT_BODY.equals(elem.getNodeName())) {
			NodeList children = elem.getChildNodes();
			List list = new ArrayList();
			for (int i=0;i<children.getLength();++i) {
				Node node = children.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					list.add(node);
				}
			}
			return (Node[])list.toArray(new Node[list.size()]);
		}
		Node child = elem.getFirstChild();
		while (child != null) {
			Node next = child.getNextSibling();
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				Node[] result = findBody((Element)child);
				if (result != null) {
					return result;
				}
			}
			child = next;
		}
		return null;
	}
}
