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
package org.eclipse.update.internal.configurator;


import java.io.*;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Parse default feature.xml
 */

public class PluginParser extends DefaultHandler implements IConfigurationConstants {
	private final static SAXParserFactory parserFactory =
		SAXParserFactory.newInstance();
	private SAXParser parser;
	private PluginEntry pluginEntry;

	private class ParseCompleteException extends SAXException {
		public ParseCompleteException(String arg0) {
			super(arg0);
		}
	}

	/**
	 * Constructor for DefaultFeatureParser
	 */
	public PluginParser() {
		super();
		try {
			parserFactory.setNamespaceAware(true);
			this.parser = parserFactory.newSAXParser();
		} catch (ParserConfigurationException e) {
			System.out.println(e);
		} catch (SAXException e) {
			System.out.println(e);
		}
	}

	/**
	 * @since 2.0
	 */
	public synchronized PluginEntry parse(File pluginFile) throws SAXException, IOException {
		try {
			pluginEntry = new PluginEntry();
			pluginEntry.setURL(PLUGINS + "/" + pluginFile.getParentFile().getName() + "/" );
			parser.parse(new InputSource(new FileInputStream(pluginFile)), this);
		} catch (ParseCompleteException e) {
			// expected, we stopped the parsing when we have the information we need
			/// no need to pursue the parsing
		}
		return pluginEntry;
	}

	/**
	 * @see DefaultHandler#startElement(String, String, String, Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		String tag = localName.trim();

		if (tag.equalsIgnoreCase(CFG_PLUGIN)) {
			pluginEntry.isFragment(false);			
			processPlugin(attributes);
			return;
		}

		if (tag.equalsIgnoreCase(CFG_FRAGMENT)) {
			pluginEntry.isFragment(true);			
			processPlugin(attributes);
			return;
		}
	}

	/** 
	 * process plugin entry info
	 */
	private void processPlugin(Attributes attributes) throws ParseCompleteException {
		String id = attributes.getValue("id"); //$NON-NLS-1$
		String version = attributes.getValue("version"); //$NON-NLS-1$
		if (id == null || id.trim().length() == 0)
			id = "_no_id_";
		pluginEntry.setVersionedIdentifier(new VersionedIdentifier(id, version));
		
		// stop parsing now
		throw new ParseCompleteException(""); //$NON-NLS-1$
	}
}
