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
		
        private static final long serialVersionUID = 1L;

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
		FileInputStream in = null;;
		try{
			in = new FileInputStream(pluginFile);
			return parse(in, PLUGINS + "/" + pluginFile.getParentFile().getName() + "/"); //$NON-NLS-1$ //$NON-NLS-2$
		}finally{
			if (in != null){
				try{
					in.close();
				}catch(IOException e){
				}
			}
		}
	}
	/**
	 * @since 3.0
	 */
	public synchronized PluginEntry parse(InputStream in, String bundleUrl) throws SAXException, IOException {
		try {
			pluginEntry = new PluginEntry();
			pluginEntry.setURL(bundleUrl);
			parser.parse(new InputSource(in), this);
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
			id = "_no_id_"; //$NON-NLS-1$
		pluginEntry.setVersionedIdentifier(new VersionedIdentifier(id, version));
		
		// stop parsing now
		throw new ParseCompleteException(""); //$NON-NLS-1$
	}
}
