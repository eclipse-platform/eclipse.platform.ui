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
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.help.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class NodeReader extends DefaultHandler {

	private SAXParser parser;
	private Stack stack;
	private Node root;

	public Node read(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		root = null;
		if (parser == null) {
			parser = SAXParserFactory.newInstance().newSAXParser();
		}
		if (stack == null) {
			stack = new Stack();
		}
		else {
			stack.clear();
		}
		parser.parse(in, this);
		return root;
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		Node node = new Node();
		node.setName(name);
		int len = attributes.getLength();
		for (int i=0;i<len;++i) {
			node.setAttribute(attributes.getQName(i), attributes.getValue(i));
		}
		if (!stack.isEmpty()) {
			Node parent = (Node)stack.peek();
			parent.appendChild(node);
		}
		else {
			root = node;
		}
		stack.push(node);
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String name) throws SAXException {
		stack.pop();
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (!stack.isEmpty()) {
			String text = new String(ch, start, length);
			if (text.trim().length() > 0) {
				Node node = new Node();
				node.setValue(text);
				Node parent = (Node)stack.peek();
				parent.appendChild(node);
			}
		}
	}
}
