package org.eclipse.update.internal.model;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.ISessionDelta;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.FeatureReferenceModel;
import org.eclipse.update.core.model.SiteModel;
import org.eclipse.update.internal.core.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.eclipse.update.internal.core.Policy;

/**
 * parse the default site.xml
 */

public class InstallChangeParser extends DefaultHandler {

	private SAXParser parser;
	private SessionDelta change;
	private File file;
	private ISite currentSite;

	private static final String CHANGE = "change"; //$NON-NLS-1$
	private static final String NEW_FEATURE = "newFeatures"; //$NON-NLS-1$
	private static final String REFERENCE = "reference"; //$NON-NLS-1$
	private static final String SITE = "site"; //$NON-NLS-1$	

	private ResourceBundle bundle;

	/**
	 * Constructor for InstallChangeParser
	 */
	public InstallChangeParser(File file)
		throws IOException, SAXException, CoreException {
		super();
		parser = new SAXParser();
		parser.setContentHandler(this);

		InputStream changeStream = new FileInputStream(file);
		this.file = file;
		
		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug("Start parsing Install Change:");
			//$NON-NLS-1$
		}

		parser.parse(new InputSource(changeStream));
	}

	/*
	 * returns the parsed InstallChanged
	 */
	 public ISessionDelta getInstallChange(){
	 	return change;
	 }

	/*
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
			//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		try {

			String tag = localName.trim();

			if (tag.equalsIgnoreCase(CHANGE)) {
				processChange(attributes);
				return;
			}

			if (tag.equalsIgnoreCase(NEW_FEATURE)) {
				processNewFeature(attributes);
				return;
			}

			if (tag.equalsIgnoreCase(SITE)) {
				processSite(attributes);
				return;
			}

			if (tag.equalsIgnoreCase(REFERENCE)) {
				processFeatureReference(attributes);
				return;
			}
		} catch (MalformedURLException e) {
			throw new SAXException(
				Policy.bind("Parser.UnableToCreateURL", e.getMessage()),
				e);
			//$NON-NLS-1$
		} catch (CoreException e) {
			throw new SAXException(Policy.bind("Parser.InternalError", e.toString()), e);
			//$NON-NLS-1$
		}
	}

	/* 
	 * 
	 */
	private void processFeatureReference(Attributes attributes)
		throws MalformedURLException, CoreException {

		// url
		String path = attributes.getValue("url"); //$NON-NLS-1$
		URL url = new URL(path);

		if (url != null) {
			FeatureReference ref = new FeatureReference();
			ref.setSite(currentSite);
			ref.setURL(url);
			change.addReference(ref);
			
			// DEBUG:		
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
				UpdateManagerPlugin.getPlugin().debug(
					"End Processing Feature Reference: url:" + url.toExternalForm());
				//$NON-NLS-1$
			}
		} else {
			String id =
				UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status =
				new Status(
					IStatus.WARNING,
					id,
					IStatus.OK,
					Policy.bind("InstallConfigurationParser.FeatureReferenceNoURL"),
					null);
			//$NON-NLS-1$
			UpdateManagerPlugin.getPlugin().getLog().log(status);
		}
	}

	/*
	 * process the Site info
	 */
	private void processSite(Attributes attributes) throws MalformedURLException, CoreException {

		//site url
		String urlString = attributes.getValue("url"); //$NON-NLS-1$
		URL siteURL = new URL(urlString);
		currentSite = SiteManager.getSite(siteURL);

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug(
				"End process config site url:" + urlString);
			//$NON-NLS-1$
		}
	}

	/* 
	 * 
	 */
	private void processNewFeature(Attributes attributes) {

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug("End Processing New Features:");
			//$NON-NLS-1$
		}
	}

	/* 
	 * 
	 */
	private void processChange(Attributes attributes) {

		// date
		long date = Long.parseLong(attributes.getValue("date")); //$NON-NLS-1$
		change.setCreationDate(new Date(date));
		
		// set the file
		change.setFile(file);

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug("End Processing Change: date:" + date);
			//$NON-NLS-1$
		}

	}

}