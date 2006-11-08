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
import org.eclipse.help.Node;
import org.xml.sax.SAXException;

/*
 * Resolves includes by parsing the target document, extracting the node and
 * replacing the original include with it.
 */
public class IncludeResolver {
	
	private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	
	private NodeProcessor processor;
	private NodeReader reader;
	private String locale;
	
	/*
	 * Creates the resolver. It must have a DOMProcessor for processing the
	 * included content, and must know the locale of the content to include.
	 */
	public IncludeResolver(NodeProcessor processor, NodeReader reader, String locale) {
		this.processor = processor;
		this.reader = reader;
		this.locale = locale;
	}
	
	/*
	 * Resolves the include target to a processed Element.
	 */
	public Node resolve(String bundleId, String relativePath, String nodeId) throws IOException, SAXException, ParserConfigurationException {
		String href = '/' + bundleId + '/' + relativePath;
		InputStream in = HelpSystem.getHelpContent(href, locale);
		try {
			Node node = findNode(in, nodeId);
			processor.process(node, href);
			return node;
		}
		finally {
			try {
				in.close();
			}
			catch (IOException e) {}
		}
	}
	
	/*
	 * Finds the specified node from the given XML input stream.
	 */
	private Node findNode(InputStream in, String nodeId) throws IOException, SAXException, ParserConfigurationException {
		Node node = reader.read(in);
		return findNode(node, nodeId);
	}
	
	/*
	 * Finds the specified node under the given subtree.
	 */
	private Node findNode(Node node, String nodeId) {
		String id = node.getAttribute(ATTRIBUTE_ID);
		if (id != null && id.equals(nodeId)) {
			return node;
		}
		Node[] children = node.getChildNodes();
		for (int i=0;i<children.length;++i) {
			Node result = findNode(children[i], nodeId);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
}
