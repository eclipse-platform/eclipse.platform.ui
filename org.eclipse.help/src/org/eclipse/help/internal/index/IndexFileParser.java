/*******************************************************************************
 * Copyright (c) 2005, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation - 122967 [Help] Remote help system
 *******************************************************************************/
package org.eclipse.help.internal.index;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.help.IIndexContribution;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.Node;
import org.eclipse.help.internal.toc.HrefUtil;
import org.eclipse.help.internal.toc.Topic;
import org.eclipse.help.internal.util.FastStack;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class IndexFileParser extends DefaultHandler {

	private FastStack elementStack;
	private IndexFile indexFile;
	private IndexContribution indexContribution;
	private SAXParser parser;

    public IIndexContribution parse(IndexFile indexFile) throws IOException, SAXException {
		this.indexFile = indexFile;
		elementStack = new FastStack();
		indexContribution = null;
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
			InputStream in = indexFile.getInputStream();
			parser.parse(in, this);
			in.close();
		} catch (ParserConfigurationException pce) {
			HelpPlugin.logError(
					"SAXParser implementation could not be loaded.", pce); //$NON-NLS-1$
		}
		
		return indexContribution;
    }

	public final void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		
		Node node = null;
		if (qName.equals("index")) { //$NON-NLS-1$
			node = handleIndexElement(atts);
		} else if (qName.equals("entry")) { //$NON-NLS-1$
			node = handleEntryElement(atts);
		} else if (qName.equals("topic")) { //$NON-NLS-1$
			node = handleTopicElement(atts);
		} else {
			// ignore unknown elements
			return;
		}

		if (!elementStack.empty())
			((Node)elementStack.peek()).addChild(node);
		elementStack.push(node);		
	}

	public final void endElement(String namespaceURI, String localName,
			String qName) throws SAXException {
		if (qName.equals("index") || qName.equals("entry") //$NON-NLS-1$ //$NON-NLS-2$
				|| qName.equals("topic")) { //$NON-NLS-1$
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
	
	private Node handleIndexElement(Attributes atts) {
		String id = HrefUtil.normalizeHref(indexFile.getPluginId(), indexFile.getFile());
		String locale = indexFile.getLocale();
		Index index = new Index();
		indexContribution = new IndexContribution(id, index, locale);
		return index;
	}
	
	private Node handleEntryElement(Attributes atts) {
		String keyword = atts.getValue("keyword"); //$NON-NLS-1$
		// label is required
		if (keyword == null) {
			String msg = "Required attribute \"keyword\" missing from entry element in " + indexFile.getPluginId() + "/" + indexFile.getFile();; //$NON-NLS-1$ //$NON-NLS-2$
			HelpPlugin.logError(msg, null);
			// continue with empty keyword
			keyword = new String();
		}
		return new IndexEntry(keyword);
	}

	private Node handleTopicElement(Attributes atts) {
		String href = HrefUtil.normalizeHref(indexFile.getPluginId(), atts.getValue("href")); //$NON-NLS-1$
		// href is required
		if (href == null) {
			String msg = "Required attribute \"href\" missing from topic element in " + indexFile.getPluginId() + "/" + indexFile.getFile(); //$NON-NLS-1$ //$NON-NLS-2$
			HelpPlugin.logError(msg, null);
		}
		String label = atts.getValue("label"); //$NON-NLS-1$
		return new Topic(href, label);
	}
}
