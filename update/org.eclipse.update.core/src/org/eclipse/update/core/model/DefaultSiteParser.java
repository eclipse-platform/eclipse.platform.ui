package org.eclipse.update.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.update.core.URLEntry;
import org.eclipse.update.internal.core.UpdateManagerPlugin;
import org.eclipse.update.internal.core.UpdateManagerUtils;
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

	private MultiStatus status;

	private static final int IGNORED_ELEMENT_STATE = -1;
	private static final int STATE_INITIAL = 0;
	private static final int STATE_SITE = 1;
	private static final int STATE_FEATURE = 2;
	private static final int STATE_ARCHIVE = 3;
	private static final int STATE_CATEGORY = 4;
	private static final int STATE_CATEGORY_DEF = 5;
	private static final int STATE_DESCRIPTION = 6;
	private static final String PLUGIN_ID = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();

	public static boolean DEBUG = false;

	public static final String SITE = "site";
	public static final String FEATURE = "feature";
	public static final String ARCHIVE = "archive";
	public static final String CATEGORY_DEF = "category-def";
	public static final String CATEGORY = "category";
	public static final String DESCRIPTION = "description";

	private static final String DEFAULT_INFO_URL = "index.html";

	// Current State Information
	Stack stateStack = new Stack();

	// Current object stack (used to hold the current object we are
	// populating in this plugin descriptor
	Stack objectStack = new Stack();

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
		stateStack.push(new Integer(STATE_INITIAL));
		parser.parse(new InputSource(in));
		return (SiteMapModel) objectStack.pop();
	}

	/**
	 * @see DefaultHandler#startElement(String, String, String, Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if (DEBUG) {
			debug("State: " + (Integer) stateStack.peek());
			debug("Start Element: uri:" + uri + " local Name:" + localName + " qName:" + qName);
		}

		String tag = localName.trim();

		int state = ((Integer) stateStack.peek()).intValue();
		switch (state) {
			case IGNORED_ELEMENT_STATE :
				internalErrorUnknownTag("unknown element in ingored state:" + localName);
				break;
			case STATE_INITIAL :
				handleInitialState(localName, attributes);
				break;

			case STATE_SITE :
				handleSiteState(localName, attributes);
				break;

			case STATE_FEATURE :
				handleFeatureState(localName, attributes);
				break;

			case STATE_ARCHIVE :
				handleArchiveState(localName, attributes);
				break;

			case STATE_CATEGORY :
				handleCategoryState(localName, attributes);
				break;

			case STATE_CATEGORY_DEF :
				handleCategoryDefState(localName, attributes);
				break;

			case STATE_DESCRIPTION :
				handleDescriptionState(localName, attributes);
				break;

			default :
				internalErrorUnknownTag("unknown state:" + state);
				break;
		}

	}

	public void handleInitialState(String elementName, Attributes attributes) throws SAXException {
		if (elementName.equals(SITE)) {
			stateStack.push(new Integer(STATE_SITE));
			processSite(attributes);
		} else
			internalErrorUnknownTag("unknown initial element" + elementName);
	}

	public void handleSiteState(String elementName, Attributes attributes) {
		if (elementName.equals(DESCRIPTION)) {
			stateStack.push(new Integer(STATE_DESCRIPTION));
			processInfo(attributes);
		} else if (elementName.equals(FEATURE)) {
			stateStack.push(new Integer(STATE_FEATURE));
			processFeature(attributes);
		} else if (elementName.equals(ARCHIVE)) {
			stateStack.push(new Integer(STATE_ARCHIVE));
			processArchive(attributes);
		} else if (elementName.equals(CATEGORY_DEF)) {
			stateStack.push(new Integer(STATE_CATEGORY_DEF));
			processCategoryDef(attributes);
		} else
			internalErrorUnknownTag("unknown site element " + elementName);
	}

	public void handleFeatureState(String elementName, Attributes attributes) {
		if (elementName.equals(CATEGORY)) {
			stateStack.push(new Integer(STATE_CATEGORY));
			processCategory(attributes);
		} else
			internalErrorUnknownTag("unknown feature element" + elementName);
	}
	public void handleArchiveState(String elementName, Attributes attributes) {
		internalErrorUnknownTag("unknown archive element" + elementName);
	}
	public void handleCategoryState(String elementName, Attributes attributes) {
		internalErrorUnknownTag("unknown category element" + elementName);
	}
	public void handleCategoryDefState(String elementName, Attributes attributes) {
		if (elementName.equals(DESCRIPTION)) {
			stateStack.push(new Integer(STATE_DESCRIPTION));
			processInfo(attributes);
		} else
			internalErrorUnknownTag("unknown category definition element" + elementName);
	}
	public void handleDescriptionState(String elementName, Attributes attributes) {
		internalErrorUnknownTag("unknown description element" + elementName);
	}

	/** 
	 * process site info
	 */
	private void processSite(Attributes attributes) throws SAXException {
		// create site map
		SiteMapModel site = factory.createSiteMapModel();

		// Compatibility support for <site url=""/>. If <description> is specified,
		// it takes precedence
		// FIXME: do we still need it ?
		String infoURL = attributes.getValue("url");
		if (infoURL == null || infoURL.trim().equals(""))
			infoURL = DEFAULT_INFO_URL;
		URLEntryModel description = factory.createURLEntryModel();
		description.setURLString(infoURL);
		site.setDescriptionModel(description);

		// verify we can parse the site ...if the site has
		// a different type throw an exception to force reparsing
		// with the matching parser
		String type = attributes.getValue("type");
		if (!factory.canParseSiteType(type)) {
			throw new SAXException(new InvalidSiteTypeException(type));
		}
		site.setType(type);
		objectStack.push(site);

		if (DEBUG)
			debug("End process Site tag: infoURL:" + infoURL + " type:" + type);

	}

	/** 
	 * process feature info
	 */
	private void processFeature(Attributes attributes) {
		FeatureReferenceModel feature = factory.createFeatureReferenceModel();
		String urlInfo = UpdateManagerUtils.encode(attributes.getValue("url"));
		if (urlInfo == null || urlInfo.trim().equals(""))
			internalError("Invalid URL tag of a feature tag. Value is required.");
		feature.setURLString(urlInfo);

		String type = attributes.getValue("type");
		feature.setType(type);

		SiteMapModel site = (SiteMapModel) objectStack.peek();
		site.addFeatureReferenceModel(feature);
		feature.setSiteModel(site);

		objectStack.push(feature);

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
		String urlString = UpdateManagerUtils.encode(attributes.getValue("url"));
		archive.setURLString(urlString);

		SiteMapModel site = (SiteMapModel) objectStack.peek();
		site.addArchiveReferenceModel(archive);

		if (DEBUG)
			debug("End processing Archive: path:" + id + " url:" + urlString);
	}

	/** 
	 * process the Category  info
	 */
	private void processCategory(Attributes attributes) {
		String category = attributes.getValue("name");
		FeatureReferenceModel feature = (FeatureReferenceModel) objectStack.peek();
		feature.addCategoryName(category);

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

		SiteMapModel site = (SiteMapModel) objectStack.peek();
		site.addCategoryModel(category);
		objectStack.push(category);

		if (DEBUG)
			debug("End processing CategoryDef: name:" + name + " label:" + label);
	}

	/** 
	 * process URL info with element text
	 */
	private void processInfo(Attributes attributes) {
		URLEntryModel inf = factory.createURLEntryModel();
		String infoURL = attributes.getValue("url");
		inf.setURLString(infoURL);

		if (DEBUG)
			debug("Processed Info: url:" + infoURL);

		objectStack.push(inf);
	}

	/**
	 * @see DefaultHandler#endElement(String, String, String)
	 */
	public void endElement(String uri, String localName, String qName) {

		String tag = localName.trim();

		int state = ((Integer) stateStack.peek()).intValue();
		switch (state) {
			case IGNORED_ELEMENT_STATE :
				stateStack.pop();
				break;
			case STATE_INITIAL :
				internalError("Stack back to Initial State, error parsing file");
				break;

			case STATE_SITE :
				stateStack.pop();
				if (objectStack.peek() instanceof String) {
					String text = (String) objectStack.pop();
					SiteMapModel site = (SiteMapModel) objectStack.peek();
					site.getDescriptionModel().setAnnotation(text);
				}
				//do not pop
				break;

			case STATE_FEATURE :
				stateStack.pop();
				objectStack.pop();
				break;

			case STATE_ARCHIVE :
				stateStack.pop();
				break;

			case STATE_CATEGORY :
				stateStack.pop();

				break;

			case STATE_CATEGORY_DEF :
				stateStack.pop();
				if (objectStack.peek() instanceof String) {
					String text = (String) objectStack.pop();
					SiteCategoryModel category = (SiteCategoryModel) objectStack.peek();
					category.getDescriptionModel().setAnnotation(text);
				}
				objectStack.pop();
				break;

			case STATE_DESCRIPTION :
				stateStack.pop();
				URLEntryModel info = (URLEntry) objectStack.pop();
				int innerState = ((Integer) stateStack.peek()).intValue();
				switch (innerState) {

					case STATE_SITE :

						SiteMapModel siteModel = (SiteMapModel) objectStack.peek();
						siteModel.setDescriptionModel(info);
						break;

					case STATE_CATEGORY_DEF :
						SiteCategoryModel category = (SiteCategoryModel) objectStack.peek();
						category.setDescriptionModel(info);
						break;

					default :
						internalError("Description in wrong state:" + state);
						break;
				}
				break;

			default :
				internalError("unknown state:" + state);
				break;
		}

		if (DEBUG)
			debug("End Element:" + uri + ":" + localName + ":" + qName);
	}

	/**
	 * @see DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) {
		String text = new String(ch, start, length).trim();
		if (!text.equals(""))
			objectStack.push(text);
	}

	private void debug(String s) {
		System.out.println("DefaultSiteParser: " + s);
	}

	public void error(SAXParseException ex) {
		logStatus(ex);
	}

	public void fatalError(SAXParseException ex) throws SAXException {
		logStatus(ex);
		throw ex;
	}

	private void logStatus(SAXParseException ex) {
		String name = ex.getSystemId();
		if (name == null)
			name = "";
		else
			name = name.substring(1 + name.lastIndexOf("/"));

		String msg;
		if (name.equals(""))
			msg = "Error Parsing";
		else
			msg = "Error:" + name + " line:" + Integer.toString(ex.getLineNumber()) + " column:" + Integer.toString(ex.getColumnNumber()) + " message:" + ex.getMessage();
		error(new Status(IStatus.WARNING, PLUGIN_ID, Platform.PARSE_PROBLEM, msg, ex));
	}

	/**
	 * Handles an error state specified by the status.  The collection of all logged status
	 * objects can be accessed using <code>getStatus()</code>.
	 *
	 * @param error a status detailing the error condition
	 */
	public void error(IStatus error) {

		getStatus().add(error);
		UpdateManagerPlugin.getPlugin().getLog().log(error);
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING)
			UpdateManagerPlugin.getPlugin().debug(error.toString());
	}
	/**
	 * Handles an error state specified by the status.  The collection of all logged status
	 * objects can be accessed using <code>getStatus()</code>.
	 *
	 * @param error a status detailing the error condition
	 */
	public void internalErrorUnknownTag(String msg) {
		stateStack.push(new Integer(IGNORED_ELEMENT_STATE));
		internalError(msg);
	}
	/**
	 * Returns all of the status objects logged thus far by this factory.
	 *
	 * @return a multi-status containing all of the logged status objects
	 */
	public MultiStatus getStatus() {
		if (status == null) {
			status = new MultiStatus(PLUGIN_ID, Platform.PARSE_PROBLEM, "Error parsing Site.xml", null);
		}
		return status;
	}

	private void internalError(String message) {
		error(new Status(IStatus.WARNING, PLUGIN_ID, Platform.PARSE_PROBLEM, message, null));
	}
}