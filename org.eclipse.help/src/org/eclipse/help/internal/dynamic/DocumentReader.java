/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.help.internal.UAElement;
import org.eclipse.help.internal.UAElementFactory;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DocumentReader {

	private DocumentBuilder builder;

	public UAElement read(InputStream in) throws IOException, SAXException, ParserConfigurationException {
		return read(in, null);
	}
	
	public UAElement read(InputStream in, String charset) throws IOException, SAXException, ParserConfigurationException {
		if (builder == null) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false);
			factory.setExpandEntityReferences(false);
			builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new EntityResolver() {
				public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
					return new InputSource(new StringReader("")); //$NON-NLS-1$
				}
			});
		}
		InputSource input = null;
		if (charset != null) {
			input = new InputSource(new InputStreamReader(in, charset));
		}
		else {
			input = new InputSource(in);
		}
		Document document = builder.parse(input);
		return UAElementFactory.newElement(document.getDocumentElement());
	}
}
