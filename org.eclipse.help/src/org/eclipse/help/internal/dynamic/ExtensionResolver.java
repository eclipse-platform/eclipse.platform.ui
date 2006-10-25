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
import org.eclipse.help.Node;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.extension.ContentExtensionManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/*
 * Resolves the content to use for extensions. For anchors, these are the
 * nodes that will be inserted at the anchor. For replaces, these are the
 * nodes that will replace the original nodes.
 */
public class ExtensionResolver {

	private static final String ELEMENT_BODY = "body"; //$NON-NLS-1$
	private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	
	private DocumentProcessor processor;
	private DOMReader reader;
	private String locale;
	private ContentExtensionManager manager;
	
	/*
	 * Creates the resolver. The processor is needed to process the extension
	 * content, and locale because we're pulling in content from other documents.
	 */
	public ExtensionResolver(DocumentProcessor processor, String locale) {
		this.processor = processor;
		this.locale = locale;
	}
	
	/*
	 * Resolves the given path into nodes to be inserted.
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
	 * nodes.
	 */
	private Node[] getContent(String content) throws IOException, SAXException, ParserConfigurationException {
		String bundleId = null;
		String relativePath = null;
		String nodeId = null;
		
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
				nodeId = content.substring(pathEnd + 1);
			}
		}
		
		if (bundleId != null && relativePath != null) {
			return getContent(bundleId, relativePath, nodeId);
		}
		return null;
	}
	
	/*
	 * Resolves the given parsed content fragments into nodes.
	 */
	private Node[] getContent(String bundleId, String relativePath, String nodeId) throws IOException, SAXException, ParserConfigurationException {
		String href = '/' + bundleId + '/' + relativePath;
		InputStream in = HelpSystem.getHelpContent(href, locale);
		try {
			if (nodeId != null) {
				Node node = findNode(in, nodeId);
				processor.process(node, href);
				return new Node[] { node };
			}
			Node[] nodes = findBody(in);
			for (int i=0;i<nodes.length;++i) {
				processor.process(nodes[i], href);
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
	 * Finds and returns the node with the given nodeId from the XML input
	 * stream, or null if not found.
	 */
	private Node findNode(InputStream in, String nodeId) throws IOException, SAXException, ParserConfigurationException {
		if (reader == null) {
			reader = new DOMReader();
		}
		Document document = reader.read(in);
		Node node = new DOMNode(document);
		return findNode(node, nodeId);
	}
	
	/*
	 * Finds and returns the node with the given nodeId from the under the
	 * given node, or null if not found.
	 */
	private Node findNode(Node node, String nodeId) {
		String id = node.getAttribute(ATTRIBUTE_ID);
		if (id != null && id.equals(nodeId)) {
			return node;
		}
		Node[] children = node.getChildren();
		for (int i=0;i<children.length;++i) {
			Node result = findNode(children[i], nodeId);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
	
	/*
	 * Extracts and returns the contents of the first body node (excluding
	 * the body node itself) in the given XML input stream.
	 */
	private Node[] findBody(InputStream in) throws IOException, SAXException, ParserConfigurationException {
		if (reader == null) {
			reader = new DOMReader();
		}
		Document document = reader.read(in);
		Node node = new DOMNode(document);
		return findBody(node);
	}
	
	/*
	 * Finds and returns the contents of the first body node under the
	 * given node (excluding the body node itself).
	 */
	private Node[] findBody(Node node) {
		if (ELEMENT_BODY.equals(node.getName())) {
			return node.getChildren();
		}
		Node[] children = node.getChildren();
		for (int i=0;i<children.length;++i) {
			Node[] result = findBody(children[i]);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
}
