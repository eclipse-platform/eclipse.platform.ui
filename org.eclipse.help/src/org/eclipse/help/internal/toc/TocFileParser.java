/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.toc;

import java.io.*;
import java.text.*;
import java.util.*;

import javax.xml.parsers.*;

import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Used to create TocFile's Toc object from contributed toc xml file.
 */
class TocFileParser extends DefaultHandler {
	protected TocBuilder builder;

	protected FastStack elementStack;

	protected TocFile tocFile;

	static SAXParserFactory factory = SAXParserFactory.newInstance();

	private static XMLParserPool parserPool = new XMLParserPool();

	/**
	 * Constructor
	 */
	public TocFileParser(TocBuilder builder) {
		super();
		this.builder = builder;
	}

	/**
	 * @see ErrorHandler#error(SAXParseException)
	 */
	public void error(SAXParseException ex) throws SAXException {
		HelpPlugin.logError("Error parsing Table of Contents file, " //$NON-NLS-1$
				+ getErrorDetails(ex), null);
	}

	/**
	 * @see ErrorHandler#fatalError(SAXParseException)
	 */
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

	/**
	 * Gets the toc
	 */
	public void parse(TocFile tocFile) {
		this.tocFile = tocFile;
		elementStack = new FastStack();
		InputStream is = tocFile.getInputStream();
		if (is == null)
			return;
		InputSource inputSource = new InputSource(is);
		String file = "/" + tocFile.getPluginID() + "/" + tocFile.getHref(); //$NON-NLS-1$ //$NON-NLS-2$
		inputSource.setSystemId(file);
		try {
			SAXParser parser = parserPool.obtainParser();
			try {
				parser.parse(inputSource, this);
				is.close();
			} finally {
				parserPool.releaseParser(parser);
			}
		} catch (ParserConfigurationException pce) {
			HelpPlugin.logError(
					"SAXParser implementation could not be loaded.", pce); //$NON-NLS-1$
		} catch (SAXException se) {
			HelpPlugin.logError("Error loading Table of Contents file " + file //$NON-NLS-1$
					+ ".", se); //$NON-NLS-1$
		} catch (IOException ioe) {
			HelpPlugin.logError("Error loading Table of Contents file " + file //$NON-NLS-1$
					+ ".", ioe); //$NON-NLS-1$
		}
	}

	/**
	 * @see ContentHandler#startElement(String, String, String, Attributes)
	 */
	public final void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		TocNode node = null;
		if (qName.equals("toc")) { //$NON-NLS-1$
			node = new Toc(tocFile, atts);
			tocFile.setToc((Toc) node);
		} else if (qName.equals("topic")) { //$NON-NLS-1$
			node = new Topic(tocFile, atts);
		} else if (qName.equals("link")) { //$NON-NLS-1$
			node = new Link(tocFile, atts);
		} else if (qName.equals("anchor")) { //$NON-NLS-1$
			node = new Anchor(tocFile, atts);
		} else
			return; // perhaps throw some exception
		if (!elementStack.empty())
			((TocNode) elementStack.peek()).addChild(node);
		elementStack.push(node);
		// do any builder specific actions in the node
		node.build(builder);
	}

	/**
	 * @see ContentHandler#endElement(String, String, String)
	 */
	public final void endElement(String namespaceURI, String localName,
			String qName) throws SAXException {
		elementStack.pop();
	}

	/**
	 * @see EntityResolver This method implementation prevents loading external
	 *      entities instead of calling
	 *      org.apache.xerces.parsers.SaxParser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
	 */
	public InputSource resolveEntity(String publicId, String systemId) {
		InputSource source = new InputSource(new ByteArrayInputStream(
				new byte[0]));
		source.setPublicId(publicId);
		source.setSystemId(systemId);
		return source;
	}

	/**
	 * This class maintain pool of parsers that can be used for parsing TOC
	 * files. The parsers should be returned to the pool for reuse.
	 */
	static class XMLParserPool {
		private ArrayList pool = new ArrayList();

		SAXParser obtainParser() throws ParserConfigurationException,
				SAXException {
			SAXParser p;
			int free = pool.size();
			if (free > 0) {
				p = (SAXParser) pool.remove(free - 1);
			} else {
				p = factory.newSAXParser();
			}
			return p;
		}

		void releaseParser(SAXParser parser) {
			pool.add(parser);
		}
	}
}