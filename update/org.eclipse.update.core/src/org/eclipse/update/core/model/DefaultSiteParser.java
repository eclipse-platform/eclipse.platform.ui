package org.eclipse.update.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.IOException;
import java.io.InputStream;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * parse default site.xml
 */

public class DefaultSiteParser extends DefaultHandler {

	private SAXParser parser;
	private SiteModelFactory factory;
	private SiteMapModel site;	
	private SiteCategoryModel currentCategory;	
	private FeatureReferenceModel currentFeature;
	private String text;
	
	private int state;
	private static final int STATE_INITIAL = 0;
	private static final int STATE_SITE = 1;
	private static final int STATE_FEATURE = 2;
	private static final int STATE_ARCHIVE = 3;
	private static final int STATE_CATEGORY_DEF = 4;
	
	public static boolean DEBUG = false;

	public static final String SITE = "site";
	public static final String FEATURE = "feature";
	public static final String ARCHIVE = "archive";
	public static final String CATEGORY_DEF = "category-def";
	public static final String CATEGORY = "category";
	public static final String DESCRIPTION = "description";

	private static final String DEFAULT_INFO_URL = "index.html";

	/**
	 * Constructor for DefaultSiteParser
	 */
	public DefaultSiteParser(SiteModelFactory factory) {
		super();
		this.parser = new SAXParser();
		this.parser.setContentHandler(this);
		this.factory = factory;
	
		if (DEBUG)
			debug("Created");
	}
	
	/**
	 * @since 2.0
	 */
	public SiteMapModel parse(InputStream in) throws SAXException, IOException {
		state = STATE_INITIAL;
		parser.parse(new InputSource(in));
		return site;
	}

	/**
	 * @see DefaultHandler#startElement(String, String, String, Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if (DEBUG)
			debug("Start Element: uri:" + uri + " local Name:" + localName + " qName:" + qName);
		
		String tag = localName.trim();

		// throw SaxException if the site is type does not match
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
			if (state == STATE_SITE) {
				site.setDescriptionModel(processInfo(attributes));
			} else if (state == STATE_CATEGORY_DEF) {
				currentCategory.setDescriptionModel(processInfo(attributes));
			}
			return;
		}

		if (tag.equalsIgnoreCase(CATEGORY)) {
			processCategory(attributes);
			return;
		}
	}

	/** 
	 * process site info
	 */
	private void processSite(Attributes attributes) throws InvalidSiteTypeException {
		// create site map
		site = factory.createSiteMapModel();
		
		// Compatibility support for <site url=""/>. If <description> is specified,
		// it takes precedence
		String infoURL = attributes.getValue("url");
		if (infoURL==null || infoURL.trim().equals(""))
			infoURL = DEFAULT_INFO_URL;
		URLEntryModel description = factory.createURLEntryModel();
		description.setURLString(infoURL);
		site.setDescriptionModel(description);

		// verify we can parse the site ...if the site has
		// a different type throw an exception to force reparsing
		// with the matching parser
		String type = attributes.getValue("type");
		if (!factory.canParseSiteType(type)) {
			throw new InvalidSiteTypeException(type);
		}
		site.setType(type);
		state = STATE_SITE;
		
		if (DEBUG) 
			debug("End process Site tag: infoURL:" + infoURL + " type:" + type);

	}

	/** 
	 * process feature info
	 */
	private void processFeature(Attributes attributes) {
		FeatureReferenceModel feature = factory.createFeatureReferenceModel();
		String urlInfo = attributes.getValue("url");
		feature.setURLString(urlInfo);

		String type = attributes.getValue("type");
		feature.setType(type);

		site.addFeatureReferenceModel(feature);
		feature.setSiteModel(site);
		currentFeature = feature;
		
		if (DEBUG)
			debug("End Processing DefaultFeature Tag: url:" + urlInfo + " type:" + type);
	}

	/** 
	 * process archive info
	 */
	private void processArchive(Attributes attributes) {
		ArchiveReferenceModel archive = factory.createArchiveReferenceModel();		
		String id = attributes.getValue("path");
		archive.setPath(id);
		String urlString = attributes.getValue("url");
		archive.setURLString(urlString);
		site.addArchiveReferenceModel(archive);

		if (DEBUG) 
			debug("End processing Archive: path:" + id + " url:" + urlString);
	}

	/** 
	 * process the Category  info
	 */
	private void processCategory(Attributes attributes) {
		String category = attributes.getValue("name");
		if (currentFeature != null)
			currentFeature.addCategoryName(category);

		if (DEBUG)
			debug("End processing Category: name:" + category);
	}

	/** 
	 * process category def info
	 */
	private void processCategoryDef(Attributes attributes) {
		SiteCategoryModel category = factory.createSiteCategoryModel();
		String name = attributes.getValue("name");
		String label = attributes.getValue("label");
		category.setName(name);
		category.setLabel(label);
		site.addCategoryModel(category);
		currentCategory = category;
		state = STATE_CATEGORY_DEF;
	
		if (DEBUG) 
			debug("End processing CategoryDef: name:" + name + " label:" + label);
	}

	/** 
	 * process URL info with element text
	 */
	private URLEntryModel processInfo(Attributes attributes)  {
		URLEntryModel inf = factory.createURLEntryModel();
		String infoURL = attributes.getValue("url");
		inf.setURLString(infoURL);

		if (DEBUG) 
			debug("Processed Info: url:" + infoURL);

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
	public void endElement(String uri, String localName, String qName) {

		if (text != null) {

			String tag = localName.trim();

			if (tag.equalsIgnoreCase(DESCRIPTION)) {
				if (state == STATE_SITE) {
					site.getDescriptionModel().setAnnotation(text);
				} else if (state == STATE_CATEGORY_DEF) {
					currentCategory.getDescriptionModel().setAnnotation(text);
				}
					
				if (DEBUG) 
					debug("Found Description Text");
			}

			// clean the text
			text = null;

		}
	
		if (DEBUG)
			debug("End Element:" + uri + ":" + localName + ":" + qName);
	}

	/**
	 * @see DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) {
		text = new String(ch, start, length).trim();
	}
	
	private void debug(String s) {
		System.out.println("DefaultSiteParser: "+s);
	}
}