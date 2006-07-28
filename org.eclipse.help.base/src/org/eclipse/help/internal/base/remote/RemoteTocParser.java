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

import org.eclipse.help.ITocContribution;
import org.eclipse.help.internal.Anchor;
import org.eclipse.help.internal.Filter;
import org.eclipse.help.internal.Include;
import org.eclipse.help.internal.Node;
import org.eclipse.help.internal.toc.Toc;
import org.eclipse.help.internal.toc.TocContribution;
import org.eclipse.help.internal.toc.Topic;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * Converts TOCs serialized by the TocServlet on remote help server back into
 * model objects. The XML is similar to TOC XML files but not identical (it has
 * all TOCs in one, includes instead of links, tocContributions, etc.
 */
public class RemoteTocParser extends DefaultHandler {

	private Stack stack = new Stack();
	private List contributions = new ArrayList();

	/*
	 * Parses the given serialized TOC and returns generated model objects.
	 */
	public ITocContribution[] parse(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(in, this);
		return (ITocContribution[])contributions.toArray(new ITocContribution[contributions.size()]);
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("tocContribution")) { //$NON-NLS-1$
			handleTocContribution(attributes);
		}
		else if (qName.equals("toc")) { //$NON-NLS-1$
			handleToc(attributes);
		}
		else if (qName.equals("topic")) { //$NON-NLS-1$
			handleTopic(attributes);
		}
		else if (qName.equals("filter")) { //$NON-NLS-1$
			handleFilter(attributes);
		}
		else if (qName.equals("include")) { //$NON-NLS-1$
			handleInclude(attributes);
		}
		else if (qName.equals("anchor")) { //$NON-NLS-1$
			handleAnchor(attributes);
		}
		else if (qName.equals("extraDoc")) { //$NON-NLS-1$
			handleExtraDoc(attributes);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("tocContribution") //$NON-NLS-1$
				|| qName.equals("toc") //$NON-NLS-1$
				|| qName.equals("topic") //$NON-NLS-1$
				|| qName.equals("filter")) { //$NON-NLS-1$
			stack.pop();
		}
	}

	private void handleTocContribution(Attributes attr) {
		String id = attr.getValue("id"); //$NON-NLS-1$
		String categoryId = attr.getValue("categoryId"); //$NON-NLS-1$
		String locale = attr.getValue("locale"); //$NON-NLS-1$
		String linkTo = attr.getValue("linkTo"); //$NON-NLS-1$
		boolean isPrimary = false;
		String s = attr.getValue("isPrimary"); //$NON-NLS-1$
		if (s != null && Boolean.toString(true).equals(s)) {
			isPrimary = true;
		}
		TocContribution contribution = new TocContribution(id, categoryId, locale, null, linkTo, isPrimary, null);
		contributions.add(contribution);
		stack.push(contribution);
	}

	private void handleToc(Attributes attr) {
		String label = attr.getValue("label"); //$NON-NLS-1$
		String topic = attr.getValue("topic"); //$NON-NLS-1$
		Toc toc = new Toc(label, topic);
		TocContribution contribution = (TocContribution)stack.peek();
		contribution.setToc(toc);
		toc.setTocContribution(contribution);
		stack.push(toc);
	}

	private void handleTopic(Attributes attr) {
		String label = attr.getValue("label"); //$NON-NLS-1$
		String href = attr.getValue("href"); //$NON-NLS-1$
		Topic topic = new Topic(href, label);
		Node node = (Node)stack.peek();
		node.addChild(topic);
		stack.push(topic);
	}

	private void handleFilter(Attributes attr) {
		String expression = attr.getValue("expression"); //$NON-NLS-1$
		Filter filter = new Filter(expression);
		Node node = (Node)stack.peek();
		node.addChild(filter);
		stack.push(filter);
	}

	private void handleInclude(Attributes attr) {
		String target = attr.getValue("target"); //$NON-NLS-1$
		Include include = new Include(target);
		Node node = (Node)stack.peek();
		node.addChild(include);
	}

	private void handleAnchor(Attributes attr) {
		String id = attr.getValue("id"); //$NON-NLS-1$
		Anchor anchor = new Anchor(id);
		Node node = (Node)stack.peek();
		node.addChild(anchor);
	}

	private void handleExtraDoc(Attributes attr) {
		String href = attr.getValue("href"); //$NON-NLS-1$
		TocContribution contribution = (TocContribution)stack.peek();
		contribution.addExtraDocument(href);
	}
}
