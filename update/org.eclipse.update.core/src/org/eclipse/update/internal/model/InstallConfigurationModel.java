/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.model;
import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.core.URLEncoder;
import org.xml.sax.*;

/**
 * An InstallConfigurationModel is 
 * 
 */

public class InstallConfigurationModel extends ModelObject {

	// performance
	private URL bundleURL;
	private URL base;
	private boolean resolved = false;

	private boolean isCurrent;
	private URL locationURL;
	private String locationURLString;
	private Date date;
	private String label;
	private List /* of ConfiguretionActivityModel */
	activities;
	private List /* of configurationSiteModel */
	configurationSites;

	private long timeline;
	protected boolean initialized = false;

	/**
	 * default constructor. Create
	 */
	public InstallConfigurationModel() {
	}

	/**
	 * @since 2.0
	 */
	public ConfiguredSiteModel[] getConfigurationSitesModel() {
		if (!initialized) initialize();
		if (configurationSites == null)
			return new ConfiguredSiteModel[0];
	
		return (ConfiguredSiteModel[]) configurationSites.toArray(arrayTypeFor(configurationSites));
	}

	/**
	 * Adds the configuration to the list
	 * is called when adding a Site or parsing the XML file
	 * in this case we do not want to create a new activity, so we do not want t call
	 * addConfigurationSite()
	 */
	public void addConfigurationSiteModel(ConfiguredSiteModel site) {
		if (configurationSites == null) {
			configurationSites = new ArrayList();
		}
		if (!configurationSites.contains(site)) {
			configurationSites.add(site);
		}
	}

	public void setConfigurationSiteModel(ConfiguredSiteModel[] sites) {
		configurationSites = null;
		for (int i = 0; i < sites.length; i++) {
			addConfigurationSiteModel(sites[i]);
		}
	}

	/**
	 * @since 2.0
	 */
	public boolean removeConfigurationSiteModel(ConfiguredSiteModel site) {
		if (!initialized) initialize();

		if (configurationSites != null) {
			return configurationSites.remove(site);
		}

		return false;
	}

	/**
	 * @since 2.0
	 */
	public boolean isCurrent() {
		if (!initialized) initialize();
		return isCurrent;
	}

	/**
	 *  @since 2.0
	 */
	public void setCurrent(boolean isCurrent) {
		// do not check if writable as we may
		// set an install config as Not current
		this.isCurrent = isCurrent;
	}

	/**
	 * @since 2.0
	 */
	public ConfigurationActivityModel[] getActivityModel() {
		if (!initialized) initialize();
		if (activities == null)
			return new ConfigurationActivityModel[0];
		return (ConfigurationActivityModel[]) activities.toArray(arrayTypeFor(activities));
	}

	/**
	 * @since 2.0
	 */
	public void addActivityModel(ConfigurationActivityModel activity) {
		if (activities == null)
			activities = new ArrayList();
		if (!activities.contains(activity)) {
			activities.add(activity);
			activity.setInstallConfigurationModel(this);
		}
	}
	/**
	 * 
	 */
	public Date getCreationDate() {
		if (!initialized) initialize();
		return date;
	}
	/**
	 * Sets the date.
	 * @param date The date to set
	 */
	public void setCreationDate(Date date) {
		assertIsWriteable();
		this.date = date;
	}
	/**
	 * @since 2.0
	 */
	public URL getURL() {
		//if (!initialized) initialize();
		//no need to initialize, always set
		delayedResolve();
		return locationURL;
	}

	/**
	 * @since 2.0
	 */
	public String getLabel() {
		if (!initialized) initialize();
		return label;
	}

	/**
	 * @since 2.0.2
	 */

	public String toString() {
		return getLabel();
	}

	/**
	 * Sets the label.
	 * @param label The label to set
	 */
	public void setLabel(String label) {
		assertIsWriteable();
		this.label = label;
	}

	/**
	 * Gets the locationURLString.
	 * @return Returns a String
	 */
	public String getLocationURLString() {
		if (!initialized) delayedResolve();
		return locationURLString;
	}

	/**
	 * Sets the locationURLString.
	 * @param locationURLString The locationURLString to set
	 */
	public void setLocationURLString(String locationURLString) {
		assertIsWriteable();
		this.locationURLString = locationURLString;
		this.locationURL = null;
	}

	/*
	 * @see ModelObject#resolve(URL, ResourceBundle)
	 */
	public void resolve(URL base, URL bundleURL) throws MalformedURLException {

		this.base = base;
		this.bundleURL = bundleURL;

	}

	/**
	 * Returns the timeline.
	 * @return long
	 */
	public long getTimeline() {
		if (!initialized) initialize();
		return timeline;
	}

	/**
	 * Sets the timeline.
	 * @param timeline The timeline to set
	 */
	public void setTimeline(long timeline) {
		this.timeline = timeline;
	}

	/*
	 * initialize the configurations from the persistent model.
	 */
	private void initialize() {
		
		try {
			try {
				URL resolvedURL = URLEncoder.encode(getURL());
				InputStream in = UpdateCore.getPlugin().get(resolvedURL).getInputStream();
				new InstallConfigurationParser(in, this);
			} catch (FileNotFoundException exception) {
				UpdateCore.warn(locationURLString + " does not exist, The local site is not in synch with the file system and is pointing to a file that doesn't exist.", exception); //$NON-NLS-1$
				throw Utilities.newCoreException(Policy.bind("InstallConfiguration.ErrorDuringFileAccess", locationURLString), exception); //$NON-NLS-1$
			} catch (SAXException exception) {
				throw Utilities.newCoreException(Policy.bind("InstallConfiguration.ParsingErrorDuringCreation", locationURLString, "\r\n" + exception.toString()), exception); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (IOException exception) {
				throw Utilities.newCoreException(Policy.bind("InstallConfiguration.ErrorDuringFileAccess", locationURLString), exception); //$NON-NLS-1$
			}
			
		} catch (CoreException e) {
			UpdateCore.warn("Error processing configuration history:" + locationURL.toExternalForm(), e);
		} finally {
			initialized = true;
		}
		
		//finish resolve
		// PERF:
		try {
			// delegate
			resolveListReference(getActivityModel(), base, bundleURL);
			resolveListReference(getConfigurationSitesModel(), base, bundleURL);
		} catch (MalformedURLException e){}		
	}

	/*
	 * 
	 */
	private void delayedResolve() {

		// PERF: delay resolution
		if (resolved)
			return;

		resolved = true;
		// resolve local elements
		try {
			locationURL = resolveURL(base, bundleURL, locationURLString);			
		} catch (MalformedURLException e){}
		
	}
}
