package org.eclipse.update.internal.model;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.model.ModelObject;
import org.eclipse.update.internal.core.*;
import org.xml.sax.SAXException;
import org.eclipse.update.internal.core.Policy;

/**
 * An InstallConfigurationModel is 
 * 
 */

public class InstallConfigurationModel extends ModelObject {
	/**
	 * initialize the configurations from the persistent model.
	 */
	public void initialize() throws CoreException {
		try {
			URL resolvedURL = URLEncoder.encode(getURL());
			new InstallConfigurationParser(resolvedURL.openStream(), this);
		} catch (FileNotFoundException exception) {
			// file doesn't exist, ok, log it and continue 
			// log no config
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug(getLocationURLString() + " does not exist, The local site is not in synch with the file system and is pointing to a file that doesn't exist."); //$NON-NLS-1$
			}
			this.setLabel(Policy.bind("InstallConfiguration.DeletedConfiguration")); //$NON-NLS-1$
		} catch (SAXException exception) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("InstallConfiguration.ParsingErrorDuringCreation", getLocationURLString(),"\r\n"+exception.toString()), exception); //$NON-NLS-1$ //$NON-NLS-2$
			throw new CoreException(status);
		} catch (IOException exception) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("InstallConfiguration.ErrorDuringFileAccess",getLocationURLString()), exception); //$NON-NLS-1$
			throw new CoreException(status);
		}
	}

	private boolean isCurrent;
	private URL locationURL;
	private String locationURLString;	
	private Date date;
	private String label;
	private List /* of ConfiguretionActivityModel */activities;
	private List /* of configurationSiteModel */ configurationSites;

	/**
	 * default constructor. Create
	 */
	public InstallConfigurationModel(){
	}
	
	/**
	 * @since 2.0
	 */
	public ConfiguredSiteModel[] getConfigurationSitesModel() {
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
		if (!configurationSites.contains(site))
			configurationSites.add(site);
	}
	

	public void setConfigurationSiteModel(ConfiguredSiteModel[] sites) {
		configurationSites=null;
		for (int i = 0; i < sites.length; i++) {
			addConfigurationSiteModel(sites[i]);
		}
	}
	
	/**
	 * @since 2.0
	 */
	public boolean removeConfigurationSiteModel(ConfiguredSiteModel site) {
		if (!isCurrent)
			return false;
			
		if (configurationSites != null) {
			return configurationSites.remove(site);
		}
		
		return false;
	}
	
	/**
	 * @since 2.0
	 */
	public boolean isCurrent() {
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
	if (activities==null)
			return new ConfigurationActivityModel[0];
	return (ConfigurationActivityModel[]) activities.toArray(arrayTypeFor(activities));
	}
	
	/**
	 * @since 2.0
	 */
	public void addActivityModel(ConfigurationActivityModel activity) {
		if (activities == null)
			activities = new ArrayList();
		if (!activities.contains(activity)){
			activities.add(activity);
			activity.setInstallConfigurationModel(this);
		}
	}
	/**
	 * 
	 */
	public Date getCreationDate() {
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
		return locationURL;
	}
	
	/**
	 * @since 2.0
	 */
	public String getLabel() {
		return label;
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
	public void resolve(URL base, ResourceBundle bundle)
		throws MalformedURLException {
		// local
		locationURL = resolveURL(base,bundle,locationURLString);
		
		// delagate
		resolveListReference(getActivityModel(),base,bundle);
		resolveListReference(getConfigurationSitesModel(),base,bundle);
	}

}