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
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;

import org.eclipse.core.runtime.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Default feature parser.
 * Parses the feature manifest file as defined by the platform. Defers
 * to a model factory to create the actual concrete model objects. The 
 * update framework supplies two factory implementations:
 * <ul>
 * <li>@see org.eclipse.update.core.model.FeatureModelFactory
 * <li>@see org.eclipse.update.core.BaseFeatureFactory
 * </ul>
 * 
 * @since 2.0
 */
public class FeatureParser extends DefaultHandler {

	private SAXParser parser;
	private FeatureEntry feature;
	private URL url;

	private final static SAXParserFactory parserFactory =
		SAXParserFactory.newInstance();

	/**
	 * Constructs a feature parser.
	 * 
	 * @param factory feature model factory
	 * @since 2.0
	 */
	public FeatureParser() {
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
	 * Parses the specified input steam and constructs a feature model.
	 * The input stream is not closed as part of this operation.
	 * 
	 * @param in input stream
	 * @return feature model
	 * @exception SAXException
	 * @exception IOException
	 * @since 2.0
	 */
	public FeatureEntry parse(URL featureURL){
		try {
			this.url = featureURL;
			InputStream in = featureURL.openStream();
			parser.parse(new InputSource(in), this);
		} catch (SAXException e) {;
		} catch (IOException e) {
		}
		return feature;
	}

	/**
	 * Handle start of element tags
	 * @see DefaultHandler#startElement(String, String, String, Attributes)
	 * @since 2.0
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		Utils.debug("Start Element: uri:" + uri + " local Name:" + localName + " qName:" + qName);
		//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		if ("feature".equals(localName)) {
			processFeature(attributes);
			// stop parsing now
			throw new SAXException("");
		} else {
		}
		
	}

	/*
	 * Process feature information
	 */
	private void processFeature(Attributes attributes) {

		// identifier and version
		String id = attributes.getValue("id"); //$NON-NLS-1$
		String ver = attributes.getValue("version"); //$NON-NLS-1$

		if (id == null || id.trim().equals("") //$NON-NLS-1$
		|| ver == null || ver.trim().equals("")) { //$NON-NLS-1$
			System.out.println(Messages.getString("FeatureParser.IdOrVersionInvalid", new String[] { id, ver}));
			//$NON-NLS-1$
		} else {
//			String label = attributes.getValue("label"); //$NON-NLS-1$
//			String provider = attributes.getValue("provider-name"); //$NON-NLS-1$
//			String imageURL = attributes.getValue("image"); //$NON-NLS-1$
			String os = attributes.getValue("os"); //$NON-NLS-1$
			String ws = attributes.getValue("ws"); //$NON-NLS-1$
			String nl = attributes.getValue("nl"); //$NON-NLS-1$
			String arch = attributes.getValue("arch"); //$NON-NLS-1$
			if (!Utils.isValidEnvironment(os, ws, arch))
				return;
//			String exclusive = attributes.getValue("exclusive"); //$NON-NLS-1$
//			String affinity = attributes.getValue("colocation-affinity"); //$NON-NLS-1$

			String primary = attributes.getValue("primary"); //$NON-NLS-1$
			boolean isPrimary = "true".equals(primary);
			String application = attributes.getValue("application"); //$NON-NLS-1$
			String plugin = attributes.getValue("plugin");

			//TODO rootURLs
			feature = new FeatureEntry(id, ver, plugin, "", isPrimary, application, null );
			if ("file".equals(url.getProtocol())) {
				File f = new File(url.getFile().replace('/', File.separatorChar));
				feature.setURL("features" + "/" + f.getParentFile().getName() + "/");// + f.getName());
			} else {
				feature.setURL(url.toExternalForm());
			}

			Utils.
				debug("End process DefaultFeature tag: id:" +id + " ver:" +ver + " url:" + feature.getURL()); 	
		}
	}
}