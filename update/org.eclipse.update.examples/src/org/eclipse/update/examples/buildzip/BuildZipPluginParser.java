/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.examples.buildzip;

import java.io.IOException;
import java.io.InputStream;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parse default feature.xml
 */

public class BuildZipPluginParser extends DefaultHandler {

	private SAXParser parser;
	private String version;
	
	private static final String PLUGIN = "plugin";
	private static final String FRAGMENT = "fragment";
	
	private class ParseCompleteException extends SAXException {
		public ParseCompleteException(String arg0) {
			super(arg0);
		}
	}

	/**
	 * Constructor for DefaultFeatureParser
	 */
	public BuildZipPluginParser() {
		super();
		this.parser = new SAXParser();
		this.parser.setContentHandler(this);
	}
	
	/**
	 * @since 2.0
	 */
	public synchronized String parse(InputStream in) throws SAXException, IOException {
		try {
			version = null;
			parser.parse(new InputSource(in));
		} catch(ParseCompleteException e) {
		}
		return version;
	}
	
	/**
	 * @see DefaultHandler#startElement(String, String, String, Attributes)
	 */
	public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
		
		String tag = localName.trim();
	
		if (tag.equalsIgnoreCase(PLUGIN)) {
			processPlugin(attributes);
			return;
		}
	
		if (tag.equalsIgnoreCase(FRAGMENT)) {
			processPlugin(attributes);
			return;
		}
	}
	
	/** 
	 * process plugin entry info
	 */
	private void processPlugin(Attributes attributes) throws ParseCompleteException {
		version = attributes.getValue("version");
		throw new ParseCompleteException("");
	}
}


