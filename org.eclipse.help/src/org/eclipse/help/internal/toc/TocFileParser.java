/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.toc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.help.ITocContribution;
import org.eclipse.help.internal.Anchor;
import org.eclipse.help.internal.Node;
import org.eclipse.help.internal.Filter;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.Include;
import org.eclipse.help.internal.util.FastStack;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.icu.text.MessageFormat;

public class TocFileParser extends DefaultHandler {

	private FastStack elementStack;
	private TocFile tocFile;
	private TocContribution tocContribution;
	private List filterNodes;
	private SAXParser parser;

	public void error(SAXParseException ex) throws SAXException {
		HelpPlugin.logError("Error parsing Table of Contents file, " //$NON-NLS-1$
				+ getErrorDetails(ex), null);
	}

	public void fatalError(SAXParseException ex) throws SAXException {
		HelpPlugin.logError("Failed to parse Table of Contents file, " //$NON-NLS-1$
				+ getErrorDetails(ex), ex);
	}

	protected String getErrorDetails(SAXParseException ex) {
		String param0 = ex.getSystemId();
		Integer param1 = new Integer(ex.getLineNumber());
		Integer param2 = new Integer(ex.getColumnNumber());
		String param3 = ex.getMessage();
		String message = MessageFormat
				.format(
						"URL: {0} at line: {1,number,integer}, column: {2,number,integer}.\r\n{3}", //$NON-NLS-1$
						new Object[] { param0, param1, param2, param3 });
		return message;
	}

	public ITocContribution parse(TocFile tocFile) throws IOException, SAXException {
		this.tocFile = tocFile;
		elementStack = new FastStack();
		tocContribution = null;
		filterNodes = new ArrayList();
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
			InputStream in = tocFile.getInputStream();
			parser.parse(in, this);
			in.close();
		} catch (ParserConfigurationException pce) {
			HelpPlugin.logError(
					"SAXParser implementation could not be loaded.", pce); //$NON-NLS-1$
		}
		
		/*
		 * For each node that had a filter attribute, slip in a filter node
		 * above it to contain it.
		 */
		Iterator iter = filterNodes.iterator();
		while (iter.hasNext()) {
			Object[] entry = (Object[])iter.next();
			Node node = (Node)entry[0];
			Node parent = node.getParentInternal();
			if (parent != null) {
				String filterExpression = (String)entry[1];
				Filter filter = new Filter(filterExpression);
				filter.addChild(node);
				parent.replaceChild(node, filter);
			}
		}
		
		return tocContribution;
	}

	public final void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		
		Node node = null;
		if (qName.equals("toc")) { //$NON-NLS-1$
			node = handleTocElement(atts);
		} else if (qName.equals("topic")) { //$NON-NLS-1$
			node = handleTopicElement(atts);
		} else if (qName.equals("link")) { //$NON-NLS-1$
			node = handleLinkElement(atts);
		} else if (qName.equals("anchor")) { //$NON-NLS-1$
			node = handleAnchorElement(atts);
		} else if (qName.equals("filter")) { //$NON-NLS-1$
			handleFilterElement(atts);
			return;
		} else {
			// ignore unknown elements
			return;
		}

		/*
		 * If the node has a filter attribute, make note of it so we can
		 * wrap it in a filter element after.
		 */
		String filterAttribute = atts.getValue("filter"); //$NON-NLS-1$
		if (filterAttribute != null) {
			Node parent = (Node)elementStack.peek();
			if (parent != null) {
				filterNodes.add(new Object[] { node, filterAttribute });
			}
		}

		if (!elementStack.empty())
			((Node)elementStack.peek()).addChild(node);
		elementStack.push(node);		
	}

	public final void endElement(String namespaceURI, String localName,
			String qName) throws SAXException {
		if (qName.equals("toc") || qName.equals("topic") //$NON-NLS-1$ //$NON-NLS-2$
				|| qName.equals("link") || qName.equals("anchor")) { //$NON-NLS-1$ //$NON-NLS-2$
			elementStack.pop();
		}
	}

	public InputSource resolveEntity(String publicId, String systemId) {
		InputSource source = new InputSource(new ByteArrayInputStream(
				new byte[0]));
		source.setPublicId(publicId);
		source.setSystemId(systemId);
		return source;
	}
	
	private Node handleTocElement(Attributes atts) {
		String label = atts.getValue("label"); //$NON-NLS-1$
		// label is required
		if (label == null) {
			String msg = "Required attribute \"label\" missing from toc element in " + tocFile.getPluginId() + "/" + tocFile.getFile();; //$NON-NLS-1$ //$NON-NLS-2$
			HelpPlugin.logError(msg, null);
			// continue with empty label
			label = new String();
		}
		String topic = HrefUtil.normalizeHref(tocFile.getPluginId(), atts.getValue("topic")); //$NON-NLS-1$
		String linkTo = HrefUtil.normalizeHref(tocFile.getPluginId(), atts.getValue("link_to")); //$NON-NLS-1$
		String id = HrefUtil.normalizeHref(tocFile.getPluginId(), tocFile.getFile());
		String categoryId = tocFile.getCategory();
		String locale = tocFile.getLocale();
		Toc toc = new Toc(label, topic);
		String[] extraDocuments = DocumentFinder.collectExtraDocuments(tocFile);
		boolean isPrimary = tocFile.isPrimary();
		
		tocContribution = new TocContribution(id, categoryId, locale, toc, linkTo, isPrimary, extraDocuments);
		toc.setTocContribution(tocContribution);
		return toc;
	}
	
	private Node handleTopicElement(Attributes atts) {
		String label = atts.getValue("label"); //$NON-NLS-1$
		// label is required
		if (label == null) {
			String msg = "Required attribute \"label\" missing from topic element in " + tocFile.getPluginId() + "/" + tocFile.getFile();; //$NON-NLS-1$ //$NON-NLS-2$
			HelpPlugin.logError(msg, null);
			// continue with empty label
			label = new String();
		}
		String href = HrefUtil.normalizeHref(tocFile.getPluginId(), atts.getValue("href")); //$NON-NLS-1$
		return new Topic(href, label);
	}

	private Node handleLinkElement(Attributes atts) {
		String toc = HrefUtil.normalizeHref(tocFile.getPluginId(), atts.getValue("toc")); //$NON-NLS-1$
		// toc is required
		if (toc == null) {
			String msg = "Required attribute \"toc\" missing from link element in " + tocFile.getPluginId() + "/" + tocFile.getFile(); //$NON-NLS-1$ //$NON-NLS-2$
			HelpPlugin.logError(msg, null);
		}
		return new Include(toc);
	}

	private Node handleAnchorElement(Attributes atts) {
		String id = atts.getValue("id"); //$NON-NLS-1$
		// id is required
		if (id == null) {
			String msg = "Required attribute \"id\" missing from anchor element in " + tocFile.getPluginId() + "/" + tocFile.getFile(); //$NON-NLS-1$ //$NON-NLS-2$
			HelpPlugin.logError(msg, null);
		}
		return new Anchor(id);
	}

	private void handleFilterElement(Attributes atts) {
		if (!elementStack.isEmpty()) {
			String name = atts.getValue("name"); //$NON-NLS-1$
			String value = atts.getValue("value"); //$NON-NLS-1$
			if (name == null || value == null) {
				String msg = "Filter element missing one or more of required attributes {name, value} " + tocFile.getPluginId() + "/" + tocFile.getFile(); //$NON-NLS-1$ //$NON-NLS-2$
				HelpPlugin.logError(msg, null);
			}
			boolean isNot = value.charAt(0) == '!';
			if (isNot) {
				value = value.substring(1);
			}
			String expression = name + (isNot ? "!=" : "=") + value; //$NON-NLS-1$ //$NON-NLS-2$
			Node parent = (Node)elementStack.peek();
			filterNodes.add(new Object[] { parent, expression });
		}
	}
}
