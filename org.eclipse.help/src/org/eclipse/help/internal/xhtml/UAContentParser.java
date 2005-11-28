/***************************************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.help.internal.xhtml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.help.internal.HelpPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * 
 */
public class UAContentParser {

	private static String TAG_HTML = "html"; //$NON-NLS-1$
	protected static String XHTML1_TRANSITIONAL = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"; //$NON-NLS-1$
	protected static String XHTML1_STRICT = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"; //$NON-NLS-1$
	protected static String XHTML1_FRAMESET = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"; //$NON-NLS-1$


	protected static Hashtable dtdMap = new Hashtable();

	static {
		String dtdBaseLocation = "dtds/xhtml1-20020801/"; //$NON-NLS-1$

		String dtdLocation = dtdBaseLocation + "xhtml1-transitional.dtd"; //$NON-NLS-1$
		URL dtdURL_T = BundleUtil.getResourceAsURL(dtdLocation, "org.eclipse.ui.intro");
		dtdMap.put(XHTML1_TRANSITIONAL, dtdURL_T);

		dtdLocation = dtdBaseLocation + "xhtml1-strict.dtd"; //$NON-NLS-1$
		URL dtdURL_S = BundleUtil.getResourceAsURL(dtdLocation, "org.eclipse.ui.intro");
		dtdMap.put(XHTML1_STRICT, dtdURL_S);

		dtdLocation = dtdBaseLocation + "xhtml1-frameset.dtd"; //$NON-NLS-1$
		URL dtdURL_F = BundleUtil.getResourceAsURL(dtdLocation, "org.eclipse.ui.intro");
		dtdMap.put(XHTML1_FRAMESET, dtdURL_F);
	}



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
		try {
			document = doParse(content);
			if (document != null) {
				// xml file is loaded. check that it is XHTML
				Element rootElement = document.getDocumentElement();
				// DocumentType docType = document.getDoctype();
				if (rootElement.getTagName().equals(TAG_HTML)) {
					// rely on root element to detect if we have an XHTML file
					// and not on doctype. We need to support xhtml files with
					// no doctype.
					hasXHTMLContent = true;
				} else
					// not XHTML.
					document = null;
			}
		} catch (Exception e) {
			HelpPlugin.logError("Could not load content file: " + content, e); //$NON-NLS-1$
		}
	}

	private DocumentBuilder createParser() {

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			docFactory.setValidating(false);
			// if this is not set, Document.getElementsByTagNameNS() will fail.
			docFactory.setNamespaceAware(true);
			docFactory.setExpandEntityReferences(false);
			DocumentBuilder parser = docFactory.newDocumentBuilder();

			parser.setEntityResolver(new EntityResolver() {

				public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
						IOException {

					if (systemId.equals(XHTML1_TRANSITIONAL) || systemId.equals(XHTML1_STRICT)
							|| systemId.equals(XHTML1_FRAMESET)) {

						// be carefull here to support running as a jarred
						// plugin.
						URL dtdURL = (URL) dtdMap.get(systemId);
						InputSource in = new InputSource(dtdURL.openStream());
						in.setSystemId(dtdURL.toExternalForm());
						return in;
					}
					return null;
				}
			});
			return parser;
		} catch (ParserConfigurationException pce) {
			// Parser with specified options can't be built
			HelpPlugin.logError(pce.getMessage(), pce);
		}
		return null;
	}


	/**
	 * General parser method that can accept both String and InputStream for parsing.
	 * 
	 * @param fileObject
	 * @return
	 */
	private Document doParse(Object fileObject) {
		Document document = null;
		try {
			DocumentBuilder parser = createParser();
			if (fileObject instanceof String)
				document = parser.parse((String) fileObject);
			else if (fileObject instanceof InputStream)
				document = parser.parse((InputStream) fileObject);

			return document;

		} catch (SAXParseException spe) {
			StringBuffer buffer = new StringBuffer("Parser error in line "); //$NON-NLS-1$
			buffer.append(spe.getLineNumber());
			buffer.append(", uri "); //$NON-NLS-1$
			buffer.append(spe.getSystemId());
			buffer.append("\n"); //$NON-NLS-1$   
			buffer.append(spe.getMessage());

			// Use the contained exception.
			Exception x = spe;
			if (spe.getException() != null)
				x = spe.getException();
			HelpPlugin.logError(buffer.toString(), x);

		} catch (SAXException sxe) {
			Exception x = sxe;
			if (sxe.getException() != null)
				x = sxe.getException();
			HelpPlugin.logError(x.getMessage(), x);

		} catch (IOException ioe) {
			HelpPlugin.logError(ioe.getMessage(), ioe);
		}
		return null;
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
