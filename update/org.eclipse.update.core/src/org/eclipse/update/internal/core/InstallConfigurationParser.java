package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * parse the default site.xml
 */

public class InstallConfigurationParser extends DefaultHandler {

	private SAXParser parser;
	private InputStream siteStream;
	private InstallConfiguration config;
	private ConfigurationSite configSite;
	public static final String CONFIGURATION = "configuration";
	public static final String CONFIGURATION_SITE = "site";
	public static final String FEATURE = "feature";
	public static final String ACTIVITY = "activity";

	private ResourceBundle bundle;

	/**
	 * Constructor for DefaultSiteParser
	 */
	public InstallConfigurationParser(InputStream siteStream, IInstallConfiguration config) throws IOException, SAXException, CoreException {
		super();
		parser = new SAXParser();
		parser.setContentHandler(this);

		this.siteStream = siteStream;
		Assert.isTrue(config instanceof InstallConfiguration);
		this.config = (InstallConfiguration) config;

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug("Start parsing Configuration:" + (config).getURL().toExternalForm());
		}

		bundle = getResourceBundle();

		parser.parse(new InputSource(this.siteStream));
	}

	/**
	 * return the appropriate resource bundle for this sitelocal
	 */
	private ResourceBundle getResourceBundle() throws IOException, CoreException {
		ResourceBundle bundle = null;
		try {
			ClassLoader l = new URLClassLoader(new URL[] { config.getURL()}, null);
			bundle = ResourceBundle.getBundle(SiteLocal.SITE_LOCAL_FILE, Locale.getDefault(), l);
		} catch (MissingResourceException e) {
			//ok, there is no bundle, keep it as null
			//DEBUG:
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug(e.getLocalizedMessage() + ":" + config.getURL().toExternalForm());
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

			if (tag.equalsIgnoreCase(CONFIGURATION)) {
				processConfig(attributes);
				return;
			}

			if (tag.equalsIgnoreCase(CONFIGURATION_SITE)) {
				processSite(attributes);
				return;
			}

			if (tag.equalsIgnoreCase(FEATURE)) {
				processFeature(attributes);
				return;
			}

			if (tag.equalsIgnoreCase(ACTIVITY)) {
				processActivity(attributes);
				return;
			}

		} catch (MalformedURLException e) {
			throw new SAXException("error processing URL. Check the validity of the URLs", e);
		} catch (CoreException e) {
			throw new SAXException("error retrieving the site. Check validity of teh URL, the site.xml and the connection", e);
		}

	}

	/** 
	 * process the Site info
	 */
	private void processSite(Attributes attributes) throws MalformedURLException, CoreException {

		//site url
		String urlString = attributes.getValue("url");
		URL siteURL = new URL(urlString);
		ISite site = SiteManager.getSite(siteURL);

		// policy
		String policyString = attributes.getValue("policy");
		int policy = Integer.parseInt(policyString);

		// confguration site
		configSite = (ConfigurationSite) SiteManager.createConfigurationSite(site, policy);

		// install
		String installString = attributes.getValue("install");
		boolean installable = installString.trim().equalsIgnoreCase("true") ? true : false;
		configSite.setInstallSite(installable);

		// add to install configuration
		config.addConfigSite(configSite);

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug("End process config site url:" + urlString + " policy:" + policyString + " install:" + installString);
		}

	}

	/** 
	 * process the DefaultFeature info
	 */
	private void processFeature(Attributes attributes) throws MalformedURLException, CoreException {

		// url
		URL url = UpdateManagerUtils.getURL(configSite.getSite().getURL(), attributes.getValue("url"), null);
		
		// configured ?
		String configuredString = attributes.getValue("configured");
		boolean configured = configuredString.trim().equalsIgnoreCase("true")?true:false;

		if (url != null) {
			//IFeatureReference ref = ((Site) configSite.getSite()).getFeatureReferences(url);
			IFeatureReference ref = new FeatureReference();
			ref.setSite(configSite.getSite());
			ref.setURL(url);
			if (ref != null)
				if (configured){
					((ConfigurationPolicy)configSite.getConfigurationPolicy()).addConfiguredFeatureReference(ref);					
				}
				else
					((ConfigurationPolicy)configSite.getConfigurationPolicy()).addUnconfiguredFeatureReference(ref);

			// DEBUG:		
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
				UpdateManagerPlugin.getPlugin().debug("End Processing DefaultFeature Tag: url:" + url.toExternalForm());
			}

		} else {
			IStatus status = new Status(IStatus.WARNING, UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier(), IStatus.OK, "FeatureReference doesn\'t have a URL", null);
			UpdateManagerPlugin.getPlugin().getLog().log(status);
		}

	}

	/** 
	 * process the Activity info
	 */
	private void processActivity(Attributes attributes) throws MalformedURLException {

		// action
		String actionString = attributes.getValue("action");
		int action = Integer.parseInt(actionString);

		// create
		ConfigurationActivity activity = new ConfigurationActivity(action);

		// label
		String label = attributes.getValue("label");
		if (label != null)
			activity.setLabel(label);

		// date
		String dateString = attributes.getValue("date");
		Date date = new Date(Long.parseLong(dateString));
		activity.setDate(date);

		// status
		String statusString = attributes.getValue("status");
		int status = Integer.parseInt(statusString);
		activity.setStatus(status);
		
		config.addActivity(activity);

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug("End Processing Activity: action:" + actionString + " label: " + label + " date:" + dateString + " status" + statusString);
		}

	}

	/** 
	 * process the Config info
	 */
	private void processConfig(Attributes attributes) {

		// date
		long date = Long.parseLong(attributes.getValue("date"));
		config.setCreationDate(new Date(date));

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING) {
			UpdateManagerPlugin.getPlugin().debug("End Processing Config Tag: date:" + date);
		}

	}

}