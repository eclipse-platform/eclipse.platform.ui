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

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SaveContextXMLContentHandler
 */
public class SaveContextXMLContentHandler extends DefaultHandler {

	private StringBuffer buffer = new StringBuffer();
	private Stack contextStack = new Stack();
	private SaveContext last;
	private Map children = new HashMap();

	public SaveContextXMLContentHandler() {
	}

	/**
	 * @see ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] chars, int startIndex, int length) throws SAXException {
		buffer.append(chars, startIndex, length);
	}

	/**
	 * @see ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {		
		SaveContext ctx = (SaveContext)contextStack.peek();
		if (!localName.equals(ctx.getName())) {
			// keep going
		} else {
			last = (SaveContext)contextStack.pop();
			if(! contextStack.isEmpty()) {
				SaveContext parent = (SaveContext)contextStack.peek();
				parent.putChild(ctx);
			}
		}
	}
	
	/**
	 * @see ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String namespaceURI, String localName, String qName,	Attributes atts) throws SAXException {
		SaveContext context = new SaveContext();
		context.setName(localName);
		for (int i = 0; i < atts.getLength(); i++) {
			String attrName = atts.getLocalName(i);
			String attrValue = atts.getValue(i);
			context.putString(attrName, attrValue);
		} 
		// empty buffer
		buffer = new StringBuffer();
		contextStack.push(context);
	}
	
	public SaveContext getSaveContext() {
		return last;
	}
}
