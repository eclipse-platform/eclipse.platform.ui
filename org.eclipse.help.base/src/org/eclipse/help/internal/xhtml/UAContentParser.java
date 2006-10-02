/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.xhtml;

import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.UndeclaredThrowableException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.help.internal.HelpPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class UAContentParser {

	private static String TAG_HTML = "html"; //$NON-NLS-1$

	private Document document;
	private boolean hasXHTMLContent;

	public UAContentParser(String content) {
		parseDocument(content);
	}

	public UAContentParser(InputStream content) {
		parseDocument(content);
	}

	/**
	 * Creates a config parser assuming that the passed content represents a URL to the content
	 * file.
	 */
	public void parseDocument(Object content) {
		document = doParse(content);
		if (document != null) {
			Element rootElement = document.getDocumentElement();
			// DocumentType docType = document.getDoctype();
			if (rootElement.getTagName().equals(TAG_HTML)) {
				hasXHTMLContent = true;
			}
		}
	}

	private DocumentBuilder createParser() {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			docFactory.setValidating(false);
			docFactory.setNamespaceAware(true);
			docFactory.setExpandEntityReferences(false);
			DocumentBuilder parser = docFactory.newDocumentBuilder();
			parser.setEntityResolver(new EntityResolver() {
				public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
					return new InputSource(new StringReader("")); //$NON-NLS-1$
				}
			});
			return parser;
		} catch (ParserConfigurationException pce) {
			HelpPlugin.logError(pce.getMessage(), pce);
		}
		return null;
	}

	private Document doParse(Object fileObject) {
		try {
			DocumentBuilder parser = createParser();
			if (fileObject instanceof String)
				return parser.parse((String) fileObject);
			else if (fileObject instanceof InputStream)
				return parser.parse((InputStream) fileObject);
			return null;
		}
		catch (Exception e) {
			// log it
			HelpPlugin.logError("An error occured while parsing: " + fileObject, e); //$NON-NLS-1$
			// wrap it in an unchecked wrapper so that it finds its way
			// to the error message
			throw new UndeclaredThrowableException(e);
		}
	}

	/**
	 * Returned the DOM representing the xml content file. May return null if parsing the file
	 * failed.
	 * 
	 * @return Returns the document.
	 */
	public Document getDocument() {
		return document;
	}

	public boolean hasXHTMLContent() {
		return hasXHTMLContent;
	}
}
