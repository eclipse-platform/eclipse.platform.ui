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

import org.eclipse.help.IContentExtension;
import org.eclipse.help.internal.extension.ContentExtension;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RemoteExtensionParser extends DefaultHandler {

	private List extensions = new ArrayList();

	public IContentExtension[] parse(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(in, this);
		return (IContentExtension[])extensions.toArray(new IContentExtension[extensions.size()]);
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("contentExtension")) { //$NON-NLS-1$
			handleContentExtension(attributes);
		}
	}
	
	private void handleContentExtension(Attributes attr) {
		String content = attr.getValue("content"); //$NON-NLS-1$
		String path = attr.getValue("path"); //$NON-NLS-1$
		int type = 0;
		try {
			type = Integer.parseInt(attr.getValue("type")); //$NON-NLS-1$
		}
		catch (Throwable t) {}
		ContentExtension ext = new ContentExtension(content, path, type);
		extensions.add(ext);
	}
}
