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
package org.eclipse.help.internal.base.remote;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.help.IIndexContribution;
import org.eclipse.help.internal.Node;
import org.eclipse.help.internal.index.Index;
import org.eclipse.help.internal.index.IndexContribution;
import org.eclipse.help.internal.index.IndexEntry;
import org.eclipse.help.internal.toc.Topic;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * Converts indexes serialized by the IndexServlet on remote help server back
 * into model objects. The XML is similar to index XML files but not identical
 * (it has all indexes in one, has indexContribution elements, etc.
 */
public class RemoteIndexParser extends DefaultHandler {

	private Stack stack = new Stack();
	private List contributions = new ArrayList();

	/*
	 * Parses the given serialized indexes and returns generated model objects.
	 */
	public IIndexContribution[] parse(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(in, this);
		return (IIndexContribution[])contributions.toArray(new IIndexContribution[contributions.size()]);
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("entry")) { //$NON-NLS-1$
			handleIndexEntry(attributes);
		}
		else if (qName.equals("topic")) { //$NON-NLS-1$
			handleTopic(attributes);
		}
		else if (qName.equals("index")) { //$NON-NLS-1$
			handleIndex(attributes);
		}
		else if (qName.equals("indexContribution")) { //$NON-NLS-1$
			handleIndexContribution(attributes);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("entry") //$NON-NLS-1$
				|| qName.equals("topic") //$NON-NLS-1$
				|| qName.equals("index") //$NON-NLS-1$
				|| qName.equals("indexContribution")) { //$NON-NLS-1$
			stack.pop();
		}
	}

	private void handleIndexContribution(Attributes attr) {
		String id = attr.getValue("id"); //$NON-NLS-1$
		String locale = attr.getValue("locale"); //$NON-NLS-1$
		IndexContribution contribution = new IndexContribution(id, null, locale);
		contributions.add(contribution);
		stack.push(contribution);
	}

	private void handleIndex(Attributes attr) {
		Index index = new Index();
		IndexContribution contribution = (IndexContribution)stack.peek();
		contribution.setIndex(index);
		stack.push(index);
	}
	
	private void handleIndexEntry(Attributes attr) {
		String keyword = attr.getValue("keyword"); //$NON-NLS-1$
		IndexEntry entry = new IndexEntry(keyword);
		Node node = (Node)stack.peek();
		node.addChild(entry);
		stack.push(entry);
	}
	
	private void handleTopic(Attributes attr) {
		String label = attr.getValue("label"); //$NON-NLS-1$
		String href = attr.getValue("href"); //$NON-NLS-1$
		Topic topic = new Topic(href, label);
		Node node = (Node)stack.peek();
		node.addChild(topic);
		stack.push(topic);
	}
}
