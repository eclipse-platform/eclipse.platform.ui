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

import org.eclipse.help.IContext;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.INode;
import org.eclipse.help.internal.Node;
import org.eclipse.help.internal.toc.Topic;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * Converts a context serialized by the ContextServlet on remote help server
 * back into model objects. The XML format is the same as with contexts in
 * context XML files except there is only one.
 */
public class RemoteContextParser extends DefaultHandler {

	private Stack stack = new Stack();
	private Context context;
	private boolean inDescription;
	private StringBuffer description;

	/*
	 * Parses the given serialized indexes and returns generated model objects.
	 */
	public IContext parse(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(in, this);
		return context;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("context")) { //$NON-NLS-1$
			handleContext(attributes);
		}
		else if (qName.equals("description")) { //$NON-NLS-1$
			handleDescription(attributes);
		}
		else if (qName.equals("topic")) { //$NON-NLS-1$
			handleTopic(attributes);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("context") //$NON-NLS-1$
				|| qName.equals("topic")) { //$NON-NLS-1$
			stack.pop();
		}
		else if (qName.equals("description") && description != null && context != null) { //$NON-NLS-1$
			context.setText(description.toString());
			description = null;
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		if (inDescription && description != null) {
			description.append(ch, start, length);
		}
	}
	
	private void handleContext(Attributes attr) {
		context = new Context(null);
		stack.push(context);
	}
	
	private void handleDescription(Attributes attr) {
		inDescription = true;
		description = new StringBuffer();
	}

	private void handleTopic(Attributes attr) {
		String label = attr.getValue("label"); //$NON-NLS-1$
		String href = attr.getValue("href"); //$NON-NLS-1$
		Topic topic = new Topic(href, label);
		Node node = (Node)stack.peek();
		node.addChild(topic);
		stack.push(topic);
	}
	
	private static class Context extends Node implements IContext {
		private String text;
	    private IHelpResource[] topics;
		
		public Context(String text) {
			this.text = text;
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public IHelpResource[] getRelatedTopics() {
			if (topics == null) {
				INode[] children = getChildren();
				if (children.length > 0) {
					List list = new ArrayList();
					for (int i=0;i<children.length;++i) {
						if (children[i] instanceof IHelpResource) {
							list.add(children[i]);
						}
					}
					topics = (IHelpResource[])list.toArray(new IHelpResource[list.size()]);
				}
				else {
					topics = new IHelpResource[0];
				}
			}
			return topics;
	    }
	}
}
