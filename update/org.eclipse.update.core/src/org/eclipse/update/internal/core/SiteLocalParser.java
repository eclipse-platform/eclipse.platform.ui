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
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.update.core.IFeatureReference;
import org.eclipse.update.core.IInfo;
import org.eclipse.update.core.ILocalSite;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * parse the default site.xml
 */

public class SiteLocalParser extends DefaultHandler {

	private SAXParser parser;
	private InputStream siteStream;
	private SiteLocal site;
	private String text;
	public static final String SITE = "localsite";
	public static final String CONFIG = "config";

	private ResourceBundle bundle;

	private IFeatureReference feature;

	/**
	 * Constructor for DefaultSiteParser
	 */
	public SiteLocalParser(InputStream siteStream, ILocalSite site) throws IOException, SAXException, CoreException {
		super();
		parser = new SAXParser();
		parser.setContentHandler(this);

		this.siteStream = siteStream;
		Assert.isTrue(site instanceof Site);
		this.site = (SiteLocal) site;

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug("Start parsing localsite:" + ((SiteLocal)site).getLocation().toExternalForm());
		}

		bundle = ((Site) site).getResourceBundle();

		parser.parse(new InputSource(this.siteStream));
	}

	/**
	 * return the appropriate resource bundle for this sitelocal
	 */
	private ResourceBundle getResourceBundle()  throws IOException, CoreException {
		ResourceBundle bundle = null;
		try {
			ClassLoader l = new URLClassLoader(new URL[] {site.getLocation()}, null);
			bundle = ResourceBundle.getBundle(SiteLocal.INSTALL_CONFIGURATION_FILE, Locale.getDefault(), l);
		} catch (MissingResourceException e) {
			//ok, there is no bundle, keep it as null
			//DEBUG:
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug(e.getLocalizedMessage() + ":" + site.getLocation().toExternalForm());
			} 
		}
		return bundle;
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

			if (tag.equalsIgnoreCase(SITE)) {
				processSite(attributes);
				return;
			}

			if (tag.equalsIgnoreCase(CONFIG)) {
				processConfig(attributes);
				return;
			}
		} catch (MalformedURLException e) {
			throw new SAXException("error processing URL. Check the validity of the URLs", e);
		}

	}

	/** 
	 * process the Site info
	 */
	private void processSite(Attributes attributes) throws MalformedURLException {
		//
		String info = attributes.getValue("label");
		info = UpdateManagerUtils.getResourceString(info, bundle);
		site.setLabel(info);

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug("End process Site label:" + info);
		}

	}

	/** 
	 * process the Config info
	 */
	private void processConfig(Attributes attributes) throws MalformedURLException {

		// url
		URL url = UpdateManagerUtils.getURL(site.getLocation(), attributes.getValue("url"), null);
		String label = attributes.getValue("label");
		label = UpdateManagerUtils.getResourceString(label, bundle);
		InstallConfiguration config = new InstallConfiguration(url,label);
		// add the config
		site.addConfiguration(config);

			// DEBUG:		
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
				UpdateManagerPlugin.getPlugin().debug("End Processing Config Tag: url:" + url.toExternalForm() );
			}

	}

}