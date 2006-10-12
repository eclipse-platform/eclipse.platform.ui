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
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/*
 * A utility class that converts an input stream of XML into a Document
 * (a DOM) for processing.
 */
public class DOMReader {

	private DocumentBuilder builder;

	/*
	 * Converts the given input stream into a DOM.
	 */
	public Document read(InputStream in) throws IOException, SAXException, ParserConfigurationException {
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
		return builder.parse(in);
	}
}
