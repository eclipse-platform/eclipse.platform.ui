package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.ConfigurationPropertyModel;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.ConfigurationActivityModel;
import org.eclipse.update.core.model.ConfigurationSiteModel;
import org.eclipse.update.core.model.InstallConfigurationModel;
import org.eclipse.update.core.model.InstallConfigurationParser;
import org.eclipse.update.core.model.SiteMapModel;
import org.xml.sax.SAXException;

/**
 * An InstallConfigurationModel is 
 * 
 */

public class InstallConfiguration extends InstallConfigurationModel implements IInstallConfiguration, IWritable {

	
	private ListenersList listeners = new ListenersList();

	public InstallConfiguration(){
	}

	/**
	 * default constructor. Create
	 */
	public InstallConfiguration(URL newLocation, String label) throws MalformedURLException {
		setLocationURLString(newLocation.toExternalForm());
		setLabel(label);
		setCurrent(false);
		resolve(newLocation,null);
	}
	
	
	/*
	 * copy constructor
	 */
	public InstallConfiguration(IInstallConfiguration config, URL newLocation, String label) throws MalformedURLException {
		setLocationURLString(newLocation.toExternalForm());
		setLabel(label);
		// do not copy list of listeners nor activities
		// ake a copy of the siteConfiguration object
		if (config != null) {
			IConfigurationSite[] sites = config.getConfigurationSites();
			if (sites != null) {
				for (int i = 0; i < sites.length; i++) {
					ConfigurationSite configSite = new ConfigurationSite(sites[i]);
					addConfigurationSiteModel(configSite);
				}
			}
		}
		// set dummy date as caller can call set date if the
		// date on the URL string has to be the same 
		setCreationDate(new Date());
		setCurrent(false);
		resolve(newLocation,null);
	}
	

	/**
	 * 
	 */
	public IConfigurationSite[] getConfigurationSites() {
		ConfigurationSiteModel[] result = getConfigurationSitesModel();
		if (result.length == 0)
			return new IConfigurationSite[0];
		else
			return (IConfigurationSite[]) result;
	}

	
	/*
	 * @see IInstallConfiguration#addConfigurationSite(IConfigurationSite)
	 */
	public void addConfigurationSite(IConfigurationSite site) {
		if (!isCurrent() || isReadOnly() )
			return;
			
		//Start UOW ?
		ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_SITE_INSTALL);
		activity.setLabel(site.getSite().getURL().toExternalForm());
		activity.setDate(new Date());
		addConfigurationSiteModel((ConfigurationSiteModel)site);
		// notify listeners
		Object[] configurationListeners = listeners.getListeners();
		for (int i = 0; i < configurationListeners.length; i++) {
			((IInstallConfigurationChangedListener) configurationListeners[i]).installSiteAdded(site);
		}
		// everything done ok
		activity.setStatus(IActivity.STATUS_OK);
		this.addActivityModel((ConfigurationActivityModel)activity);
	}

	public void removeConfigurationSite(IConfigurationSite site) {
		
		if (removeConfigurationSiteModel((ConfigurationSiteModel)site)){// notify listeners
			Object[] configurationListeners = listeners.getListeners();
			for (int i = 0; i < configurationListeners.length; i++) {
				((IInstallConfigurationChangedListener) configurationListeners[i]).installSiteRemoved(site);
			}
		}
	}

	/*
	 * @see IInstallConfiguration#addInstallConfigurationChangedListener(IInstallConfigurationChangedListener)
	 */
	public void addInstallConfigurationChangedListener(IInstallConfigurationChangedListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	/*
	 * @see IInstallConfiguration#removeInstallConfigurationChangedListener(IInstallConfigurationChangedListener)
	 */
	public void removeInstallConfigurationChangedListener(IInstallConfigurationChangedListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	/*
	 * @see IInstallConfiguration#export(File)
	 */
	public void export(File exportFile) throws CoreException {
		try {
			PrintWriter fileWriter = new PrintWriter(new FileOutputStream(exportFile));
			Writer writer = new Writer();
			writer.writeSite(this, fileWriter);
			fileWriter.close();
		} catch (FileNotFoundException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot save configration into " + exportFile.getAbsolutePath(), e);
			throw new CoreException(status);
		}
	}
	/**
	 * Deletes the configuration from its URL/location
	 */
	public void remove() {
		// save the configuration
		if (getURL().getProtocol().equalsIgnoreCase("file")) {
			// the location points to a file
			File file = new File(getURL().getFile());
			UpdateManagerUtils.removeFromFileSystem(file);
		}
	}
	/**
	 * Saves the configuration into its URL/location
	 */
	public void save() throws CoreException {
		// save the file
		saveConfigurationFile();
		
		// Write info for the next runtime
		IPlatformConfiguration runtimeConfiguration = BootLoader.getCurrentPlatformConfiguration();
		ConfigurationSiteModel[] configurationSites = getConfigurationSitesModel();
		for (int i = 0; i < configurationSites.length; i++) {
			IConfigurationSite element = (IConfigurationSite) configurationSites[i];
			ConfigurationPolicy configurationPolicy = (ConfigurationPolicy) element.getConfigurationPolicy();
			String[] pluginPath = configurationPolicy.getPluginPath(element.getSite());
			IPlatformConfiguration.ISitePolicy sitePolicy = runtimeConfiguration.createSitePolicy(configurationPolicy.getPolicy(), pluginPath);
			IPlatformConfiguration.ISiteEntry siteEntry = runtimeConfiguration.findConfiguredSite(element.getSite().getURL());
			if (siteEntry!=null){
				siteEntry.setSitePolicy(sitePolicy);
			} else {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Platform configuration not found :"+element.getSite().getURL().toExternalForm(), null);
				throw new CoreException(status);				
			}
			try {
				runtimeConfiguration.save();
			} catch (IOException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot save Platform Configuration ", e);
				throw new CoreException(status);
			}
		}
	}
	
	/**
	 * 
	 */
	public void saveConfigurationFile() throws CoreException {
		// save the configuration
		if (getURL().getProtocol().equalsIgnoreCase("file")) {
			// the location points to a file
			File file = new File(getURL().getFile());
			export(file);
		}
	}
	/*
	 * @see IWritable#write(int, PrintWriter)
	 */
	public void write(int indent, PrintWriter w) {
		String gap = "";
		for (int i = 0; i < indent; i++)
			gap += " ";
		String increment = "";
		for (int i = 0; i < IWritable.INDENT; i++)
			increment += " ";
		w.print(gap + "<" + InstallConfigurationParser.CONFIGURATION + " ");
		w.print("date=\"" + getCreationDate().getTime() + "\" ");
		w.println(">");
		w.println("");
		// site configurations
		if (getConfigurationSitesModel() != null) {
			ConfigurationSiteModel[] sites = getConfigurationSitesModel();
	 		for (int i = 0; i < sites.length; i++) {
				ConfigurationSite element = (ConfigurationSite) sites[i];
				((IWritable) element).write(indent + IWritable.INDENT, w);
			}
		}
		// activities
		if (getActivityModel()!=null) {
			ConfigurationActivityModel[] activities = getActivityModel();
			for (int i = 0; i < activities.length; i++) {
				ConfigurationActivity element = (ConfigurationActivity) activities[i];
				((IWritable) element).write(indent + IWritable.INDENT, w);
			}
		}
		// end
		w.println(gap + "</" + InstallConfigurationParser.CONFIGURATION + ">");
	}
	/**
	 * reverts this configuration to the match the new one
	 * 
	 * remove any site that are in the current but not in the old state
	 * 
	 * replace all the config sites of the current state with the old one
	 * 
	 * for all the sites left in the current state, calculate the revert
	 * 
	 */
	public void revertTo(IInstallConfiguration configuration, IProgressMonitor monitor, IProblemHandler handler) throws CoreException, InterruptedException {
		
		
		IConfigurationSite[] oldConfigSites = configuration.getConfigurationSites();
		IConfigurationSite[] nowConfigSites = this.getConfigurationSites();
				
		// create a hashtable of the *old* sites
		Map oldSitesMap = new Hashtable(0);
		for (int i = 0; i < oldConfigSites.length; i++) {
			IConfigurationSite element = oldConfigSites[i];
			oldSitesMap.put(element.getSite().getURL().toExternalForm(), element);
		}
		// create list of all the sites that map the *old* sites
		// we want the intersection between the old sites and the current sites
		if (nowConfigSites != null) {
			// for each current site, ask the old site
			// to calculate the delta 
			String key = null;
			for (int i = 0; i < nowConfigSites.length; i++) {
				key = nowConfigSites[i].getSite().getURL().toExternalForm();
				IConfigurationSite oldSite = (IConfigurationSite) oldSitesMap.get(key);
				if (oldSite != null) {
					((ConfigurationSite) oldSite).deltaWith(nowConfigSites[i], monitor, handler);
				}
			}
			// the new configuration has the exact same sites as the old configuration
			// the old configuration in the Map are either as-is because they don't exist
			// in the current one, or they are the delta from the current one to the old one
			Collection sites = oldSitesMap.values();
			if (sites!=null && !sites.isEmpty()){
				ConfigurationSiteModel[] sitesModel = new ConfigurationSiteModel[sites.size()];
				sites.toArray(sitesModel);
				setConfigurationSiteModel(sitesModel);
			}
		}
	}
		/*
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	/*
	 * @see IInstallConfiguration#getActivities()
	 */
	public IActivity[] getActivities() {
		if (getActivityModel().length==0)
			return new IActivity[0];
		return (IActivity[])getActivityModel();
	}

}