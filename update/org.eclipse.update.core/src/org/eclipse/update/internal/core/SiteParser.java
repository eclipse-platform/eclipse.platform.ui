package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.InvalidSiteTypeException;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * parse the default site.xml
 */

public class SiteParser extends DefaultHandler {

	private SAXParser parser;
	private InputStream siteStream;
	private Site site;
	private String text;
	private Category currentCategory;
	public static final String SITE = "site";
	public static final String FEATURE = "feature";
	public static final String ARCHIVE = "archive";
	public static final String CATEGORY_DEF = "category-def";
	public static final String CATEGORY = "category";
	public static final String DESCRIPTION = "description";

	private static final String DEFAULT_INFO_URL = "index.html";

	private ResourceBundle bundle;

	private IFeatureReference feature;

	/**
	 * Constructor for DefaultSiteParser
	 */
	public SiteParser(InputStream siteStream, ISite site) throws IOException, SAXException, CoreException {
		super();
		parser = new SAXParser();
		parser.setContentHandler(this);

		this.siteStream = siteStream;
		Assert.isTrue(site instanceof Site);
		this.site = (Site) site;

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug("Start parsing site:" + site.getURL().toExternalForm());
		}

		//bundle = ((Site) site).getResourceBundle();

		parser.parse(new InputSource(this.siteStream));
	}

	/**
	 * @see DefaultHandler#startElement(String, String, String, Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug("Start Element: uri:" + uri + " local Name:" + localName + " qName:" + qName);
		}
		try {

			String tag = localName.trim();

			// throw SaxException is the site is not valid
			try {
				if (tag.equalsIgnoreCase(SITE)) {
					processSite(attributes);
					return;
				}
			} catch (InvalidSiteTypeException e) {
				throw new SAXException(e);
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

			if (tag.equalsIgnoreCase(DESCRIPTION)) {
				currentCategory.setDescription(processInfo(attributes));
				return;
			}

			if (tag.equalsIgnoreCase(CATEGORY)) {
				processCategory(attributes);
				return;
			}
		} catch (MalformedURLException e) {
			throw new SAXException("error processing URL. Check the validity of the URLs", e);
		}

	}

	/** 
	 * process the Site info
	 */
	private void processSite(Attributes attributes) throws MalformedURLException, InvalidSiteTypeException {
		//
		String infoURL = attributes.getValue("url");
		infoURL = UpdateManagerUtils.getResourceString(infoURL, bundle);
		URL url = UpdateManagerUtils.getURL(site.getURL(), infoURL, DEFAULT_INFO_URL);
		//site.setInfoURL(url);

		// process the Site....if the site has a different type
		// throw an exception so the new parser can be used...
		String type = attributes.getValue("type");
		if (type!=null && type!="" && !type.equals(site.getType())) {
			InvalidSiteTypeException exception = new InvalidSiteTypeException(type);
			throw exception;
		}

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug("End process Site tag: infoURL:" + infoURL);
		}

	}

	/** 
	 * process the DefaultFeature info
	 */
	private void processFeature(Attributes attributes) throws MalformedURLException {

		// url
		URL url = UpdateManagerUtils.getURL(site.getURL(), attributes.getValue("url"), null);

		if (url != null) {
			feature = new FeatureReference();
			feature.setSite(site);
			//feature.setURL(url);			

			// the type of the feature
			String type = attributes.getValue("type");
			((FeatureReference) feature).setType(type);

			// add the feature
			//site.addFeatureReference(feature);

			// DEBUG:		
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
				UpdateManagerPlugin.getPlugin().debug("End Processing DefaultFeature Tag: url:" + url.toExternalForm() + "  ->type:" + type + " Class name:" + feature.getClass().toString());
			}

		} else {
			IStatus status = new Status(IStatus.WARNING, UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier(), IStatus.OK, "DefaultFeature doesn\'t have a URL", null);
			UpdateManagerPlugin.getPlugin().getLog().log(status);
		}

	}

	/** 
	 * process the Archive info
	 */
	private void processArchive(Attributes attributes) throws MalformedURLException {
		String id = attributes.getValue("path");
		String urlString = attributes.getValue("url");
		URL url = UpdateManagerUtils.getURL(site.getURL(), urlString, null);
		//site.addArchive(new URLEntry(id, url));

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug("End processing Archive: id:" + id + " ver:" + urlString);
		}

	}

	/** 
	 * process the Category  info
	 */
	private void processCategory(Attributes attributes) {
		// category
		String category = attributes.getValue("name");
		((FeatureReference) feature).addCategoryName(category);

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug("End processing Category: name:" + category);
		}
	}

	/** 
	 * process the Category Def info
	 */
	private void processCategoryDef(Attributes attributes) {
		String name = attributes.getValue("name");
		String label = attributes.getValue("label");
		label = UpdateManagerUtils.getResourceString(label, bundle);
		Category category = new Category(name, label);
		//site.addCategory(category);
		currentCategory = category;

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug("End processing CategoryDef: name:" + name + " label:" + label);
		}

	}

	/** 
	 * process the info
	 */
	private IURLEntry processInfo(Attributes attributes) throws MalformedURLException {
		String infoURL = attributes.getValue("url");
		infoURL = UpdateManagerUtils.getResourceString(infoURL, bundle);
		URL url = UpdateManagerUtils.getURL(site.getURL(), infoURL, null);
		URLEntry inf = new URLEntry();
		inf.resolve(url,null);

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug("Processed URLEntry: url:" + infoURL);
		}

		return inf;
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
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (text != null) {

			String tag = localName.trim();

			if (tag.equalsIgnoreCase(DESCRIPTION)) {
				((URLEntry) currentCategory.getDescription()).setAnnotation(text);

				// DEBUG:		
				if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
					UpdateManagerPlugin.getPlugin().debug("Found Description Text");
				}
			}

			// clean the text
			text = null;

		}

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug("End Element:" + uri + ":" + localName + ":" + qName);
		}

	}

	/**
	 * @see DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		text = new String(ch, start, length).trim();
	}

}