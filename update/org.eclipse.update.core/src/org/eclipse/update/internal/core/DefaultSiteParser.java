package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import java.util.ArrayList;
import java.util.List;
import org.apache.xerces.parsers.SAXParser;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.update.core.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * parse the default site.xml
 */

public class DefaultSiteParser extends DefaultHandler {

	private SAXParser parser;
	private InputStream siteStream;
	private AbstractSite site;
	private static final String SITE = "site";
	private static final String FEATURE = "feature";
	private static final String ARCHIVE = "archive";
	private static final String CATEGORY_DEF = "category-def";
	private static final String CATEGORY = "category";

	private static final String DEFAULT_INFO_URL = "index.html";

	private ResourceBundle bundle;

	private AbstractFeature feature;

	/**
	 * Constructor for DefaultSiteParser
	 */
	public DefaultSiteParser(InputStream siteStream, ISite site)
		throws IOException, SAXException {
		super();
		parser = new SAXParser();
		parser.setContentHandler(this);

		this.siteStream = siteStream;
		Assert.isTrue(site instanceof AbstractSite);
		this.site = (AbstractSite) site;

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug(
				"Start parsing:" + site.getURL().toExternalForm());
		}

		try {
			ClassLoader l = new URLClassLoader(new URL[] { site.getURL()}, null);
			bundle = ResourceBundle.getBundle("site", Locale.getDefault(), l);
		} catch (MissingResourceException e) {
			//ok, there is no bundle, keep it as null
			//DEBUG:
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug(
					e.getLocalizedMessage() + ":" + site.getURL().toExternalForm());
			}
		}
		parser.parse(new InputSource(this.siteStream));
	}

	/**
	 * @see DefaultHandler#startElement(String, String, String, Attributes)
	 */
	public void startElement(
		String uri,
		String localName,
		String qName,
		Attributes attributes)
		throws SAXException {

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug(
				"Start Element: uri:" + uri + " local Name:" + localName + " qName:" + qName);
		}
		try {
			
		String tag = localName.trim();

		if (tag.equalsIgnoreCase(SITE)) {
			processSite(attributes);
			return;
		}

		if (tag.equalsIgnoreCase(FEATURE)) {
			processFeature(attributes);
			return;
		}

		if (tag.equalsIgnoreCase(ARCHIVE)) {
			processArchive(attributes);
			return;
		}

		if (tag.equalsIgnoreCase(CATEGORY_DEF)) {
			processCategoryDef(attributes);
			return;
		}

		if (tag.equalsIgnoreCase(CATEGORY)) {
			processCategory(attributes);
			return;
		}
		} catch (MalformedURLException e){
			throw new SAXException("error processing URL. Check the validity of the URLs",e);
		}

	}

	/** 
	 * process the Site info
	 */
	private void processSite(Attributes attributes)  throws MalformedURLException {
		//
		String infoURL = attributes.getValue("url");
		infoURL = UpdateManagerUtils.getResourceString(infoURL, bundle);
		URL url = UpdateManagerUtils.getURL(site.getURL(), infoURL, DEFAULT_INFO_URL);
		site.setInfoURL(url);

		// process the Site....if the site has a different type
		// throw an exception so the new parser can be used...
		// TODO:

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug(
				"End process Site tag: infoURL:" + infoURL);
		}

	}

	/** 
	 * process the Feature info
	 */
	private void processFeature(Attributes attributes) throws MalformedURLException {

		// url
		URL url =
			UpdateManagerUtils.getURL(site.getURL(), attributes.getValue("url"), null);

		if (url != null) {
			// the type of the feature
			String type = attributes.getValue("type");
			if (type == null || type.equals("")) {
				feature = new DefaultPackagedFeature(url, site);
			} else {
				Assert.isTrue(
					false,
					"Not implemented Yet... do not use 'type' in the feature tag of site.xml");
				//FIXME: manages creation of feature...
			}

			// add the feature
			site.addFeature(feature);

			// DEBUG:		
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
				UpdateManagerPlugin.getPlugin().debug(
					"End Processing Feature Tag: url:" + url.toExternalForm() + " type" + type);
			}

		} else {
			IStatus status =
				new Status(
					IStatus.WARNING,
					UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier(),
					IStatus.OK,
					"Feature doesn\'t have a URL",
					null);
			UpdateManagerPlugin.getPlugin().getLog().log(status);
		}

	}

	/** 
	 * process the Archive info
	 */
	private void processArchive(Attributes attributes) throws MalformedURLException {
		String id = attributes.getValue("id");
		String urlString = attributes.getValue("url");
		URL url = UpdateManagerUtils.getURL(site.getURL(), urlString, null);
		site.addArchive(new Info(id, url));

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug(
				"End processing Archive: id:" + id + " ver:" + urlString);
		}

	}

	/** 
	 * process the Category  info
	 */
	private void processCategory(Attributes attributes) {
		// category
		String category = attributes.getValue("name");
		feature.addCategoryString(category);

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug(
				"End processing Category: name:" + category);
		}
	}

	/** 
	 * process the Category Def info
	 */
	private void processCategoryDef(Attributes attributes) {
		String name = attributes.getValue("name");
		String label = attributes.getValue("label");
		label = UpdateManagerUtils.getResourceString(label, bundle);
		ICategory category = new DefaultCategory(name, label);
		site.addCategory(category);

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug(
				"End processing CategoryDef: name:" + name + " label:" + label);
		}

	}

	/**
	 * @see DefaultHandler#error(SAXParseException)
	 */
	public void error(SAXParseException arg0) throws SAXException {
		super.error(arg0);
	}

	/**
	 * @see DefaultHandler#endElement(String, String, String)
	 */
	public void endElement(String uri, String localName, String qName)
		throws SAXException {

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug(
				"End Element:" + uri + ":" + localName + ":" + qName);
		}

	}

}