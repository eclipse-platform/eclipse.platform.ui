package org.eclipse.update.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.InputStream;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.update.core.*;
import org.eclipse.update.core.PluginEntry;
import org.eclipse.update.core.VersionedIdentifier;
import org.eclipse.update.internal.core.PluginEntryContentConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parse default feature.xml
 */

public class DefaultPluginParser extends DefaultHandler {

	private SAXParser parser;
	private String id = null;
	private String version = null;
	private PluginEntry pluginEntry;
	
	private static final String PLUGIN = "plugin";
	private static final String FRAGMENT = "fragment";
	private static final String ID = "id";
	private static final String VERSION = "version";	
	
	private class ParseCompleteException extends SAXException {
		public ParseCompleteException(String arg0) {
			super(arg0);
		}
	}

	/**
	 * Constructor for DefaultFeatureParser
	 */
	public DefaultPluginParser() {
		super();
		this.parser = new SAXParser();
		this.parser.setContentHandler(this);
	}
	
	/**
	 * @since 2.0
	 */
	public synchronized PluginEntry parse(InputStream in) throws SAXException, IOException {
		try {
			pluginEntry = new PluginEntry();			
			parser.parse(new InputSource(in));
			pluginEntry.setIdentifier(new VersionedIdentifier(id,version));			
		} catch(ParseCompleteException e) {
		}
		return pluginEntry;
	}
	
	/**
	 * @see DefaultHandler#startElement(String, String, String, Attributes)
	 */
	public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
		
		String tag = localName.trim();
	
		if (tag.equalsIgnoreCase(PLUGIN)) {
			processPlugin(attributes);
			pluginEntry.isFragment(false);
			return;
		}
	
		if (tag.equalsIgnoreCase(FRAGMENT)) {
			processPlugin(attributes);
			pluginEntry.isFragment(true);
			return;
		}
	}
	
	/** 
	 * process plugin entry info
	 */
	private void processPlugin(Attributes attributes) throws ParseCompleteException {
		id = attributes.getValue("id");
		version = attributes.getValue("version");
		throw new ParseCompleteException("");
	}
}


