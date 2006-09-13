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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.help.internal.xhtml.UATopicExtension;
import org.eclipse.help.internal.xhtml.UATopicReplace;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RemoteExtensionParser extends DefaultHandler {

	private List extensions = new ArrayList();

	public List parse(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(in, this);
		return extensions;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("topicExtension")) { //$NON-NLS-1$
			handleTopicExtension(attributes);
		}
		else if (qName.equals("topicReplace")) { //$NON-NLS-1$
			handleTopicReplace(attributes);
		}
	}

	private void handleTopicExtension(Attributes attr) {
		String targetHref = attr.getValue("targetHref"); //$NON-NLS-1$
		String targetAnchorId = attr.getValue("targetAnchorId"); //$NON-NLS-1$
		String contentHref = attr.getValue("contentHref"); //$NON-NLS-1$
		String contentElementId = attr.getValue("contentElementId"); //$NON-NLS-1$
		extensions.add(new UATopicExtension(targetHref, targetAnchorId, contentHref, contentElementId));
	}

	private void handleTopicReplace(Attributes attr) {
		String targetHref = attr.getValue("targetHref"); //$NON-NLS-1$
		String targetElementId = attr.getValue("targetElementId"); //$NON-NLS-1$
		String contentHref = attr.getValue("contentHref"); //$NON-NLS-1$
		String contentElementId = attr.getValue("contentElementId"); //$NON-NLS-1$
		extensions.add(new UATopicReplace(targetHref, targetElementId, contentHref, contentElementId));
	}
}
