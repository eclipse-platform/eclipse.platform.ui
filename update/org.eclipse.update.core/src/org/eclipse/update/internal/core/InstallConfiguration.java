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
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.IFeatureReference;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.internal.model.*;

/**
 * An InstallConfigurationModel is 
 * 
 */

public class InstallConfiguration extends InstallConfigurationModel implements IInstallConfiguration, IWritable {

	private ListenersList listeners = new ListenersList();

	public InstallConfiguration() {
	}

	/**
	 * default constructor. Create
	 */
	public InstallConfiguration(URL newLocation, String label) throws MalformedURLException {
		setLocationURLString(newLocation.toExternalForm());
		setLabel(label);
		setCurrent(false);
		resolve(newLocation, null);
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
			IConfiguredSite[] sites = config.getConfiguredSites();
			if (sites != null) {
				for (int i = 0; i < sites.length; i++) {
					ConfiguredSite configSite = new ConfiguredSite(sites[i]);
					addConfigurationSiteModel(configSite);
				}
			}
		}
		// set dummy date as caller can call set date if the
		// date on the URL string has to be the same 
		setCreationDate(new Date());
		setCurrent(false);
		resolve(newLocation, null);
	}

	/**
	 * 
	 */
	public IConfiguredSite[] getConfiguredSites() {
		ConfiguredSiteModel[] result = getConfigurationSitesModel();
		if (result.length == 0)
			return new IConfiguredSite[0];
		else
			return (IConfiguredSite[]) result;
	}

	/**
	 * 
	 */
	public void addConfiguredSite(IConfiguredSite site) {
		if (!isCurrent() && isReadOnly())
			return;

		//Start UOW ?
		ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_SITE_INSTALL);
		activity.setLabel(site.getSite().getURL().toExternalForm());
		activity.setDate(new Date());
		ConfiguredSiteModel configSiteModel = (ConfiguredSiteModel) site;
		addConfigurationSiteModel(configSiteModel);
		configSiteModel.setInstallConfigurationModel(this);
		// notify listeners
		Object[] configurationListeners = listeners.getListeners();
		for (int i = 0; i < configurationListeners.length; i++) {
			((IInstallConfigurationChangedListener) configurationListeners[i]).installSiteAdded(site);
		}
		// everything done ok
		activity.setStatus(IActivity.STATUS_OK);
		this.addActivityModel((ConfigurationActivityModel) activity);
	}

	/**
	 * add multiple sites in one activity
	 */
	public void setConfigurationSites(IConfiguredSite[] site) {
		if (!isCurrent() && isReadOnly())
			return;

		if (site == null)
			return;

		for (int index = 0; index < site.length; index++) {
			addConfiguredSite(site[index]);
		}

	}

	/**
	 * 
	 */
	public void removeConfiguredSite(IConfiguredSite site) {

		if (removeConfigurationSiteModel((ConfiguredSiteModel) site)) { // notify listeners
			Object[] configurationListeners = listeners.getListeners();
			for (int i = 0; i < configurationListeners.length; i++) {
				((IInstallConfigurationChangedListener) configurationListeners[i]).installSiteRemoved(site);
			}

			//activity
			ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_SITE_REMOVE);
			activity.setLabel(site.getSite().getURL().toExternalForm());
			activity.setDate(new Date());
			activity.setStatus(IActivity.STATUS_OK);
			this.addActivityModel((ConfigurationActivityModel) activity);

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
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("InstallConfiguration.UnableToSaveConfiguration", exportFile.getAbsolutePath()), e); //$NON-NLS-1$
			throw new CoreException(status);
		}
	}
	/**
	 * Deletes the configuration from its URL/location
	 */
	public void remove() {
		// save the configuration
		if (getURL().getProtocol().equalsIgnoreCase("file")) { //$NON-NLS-1$
			// the location points to a file
			File file = new File(getURL().getFile());
			UpdateManagerUtils.removeFromFileSystem(file);
		}
	}
	/**
	 * Saves the configuration into its URL/location
	 */
	public void save(boolean isTransient) throws CoreException {

		// save the configuration.xml file
		saveConfigurationFile(isTransient);

		// Write info  into platform for the next runtime
		IPlatformConfiguration runtimeConfiguration = BootLoader.getCurrentPlatformConfiguration();
		ConfiguredSiteModel[] configurationSites = getConfigurationSitesModel();

		for (int i = 0; i < configurationSites.length; i++) {
			IConfiguredSite element = (IConfiguredSite) configurationSites[i];
			ConfigurationPolicy configurationPolicy = ((ConfiguredSite) element).getConfigurationPolicy();

			// obtain the list of plugins
			ConfiguredSite cSite = ((ConfiguredSite) element);
			String[] pluginPath = configurationPolicy.getPluginPath(element.getSite(), cSite.getPreviousPluginPath());

			IPlatformConfiguration.ISitePolicy sitePolicy = runtimeConfiguration.createSitePolicy(configurationPolicy.getPolicy(), pluginPath);

			// determine the URL to check 
			URL urlToCheck = null;
			ConfiguredSiteModel configSiteModel = null;
			try {
				configSiteModel = (ConfiguredSiteModel) element;
				urlToCheck = new URL(configSiteModel.getPlatformURLString());
			} catch (MalformedURLException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("InstallConfiguration.UnableToCreateURL",configSiteModel.getPlatformURLString()), e); //$NON-NLS-1$
				throw new CoreException(status);
			} catch (ClassCastException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("InstallConfiguration.UnableToCast"), e); //$NON-NLS-1$
				throw new CoreException(status);
			}

			// if the URL already exist, set the policy
			IPlatformConfiguration.ISiteEntry siteEntry = runtimeConfiguration.findConfiguredSite(urlToCheck);
			if (siteEntry != null) {
				siteEntry.setSitePolicy(sitePolicy);
			} else {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("InstallConfiguration.UnableToFindConfiguredSite",urlToCheck.toExternalForm(), runtimeConfiguration.getConfigurationLocation().toExternalForm()), null); //$NON-NLS-1$
				throw new CoreException(status);
			}
		}

		try {
			runtimeConfiguration.save();
		} catch (IOException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("InstallConfiguration.UnableToSavePlatformConfiguration", runtimeConfiguration.getConfigurationLocation().toExternalForm()), e); //$NON-NLS-1$
			throw new CoreException(status);
		}
	}

	/**
	 * 
	 */
	public void saveConfigurationFile(boolean isTransient) throws CoreException {
		// save the configuration
		if (getURL().getProtocol().equalsIgnoreCase("file")) { //$NON-NLS-1$
			// the location points to a file
			File file = new File(getURL().getFile());
			if (isTransient) file.deleteOnExit();
			export(file);
		}
	}
	/*
	 * @see IWritable#write(int, PrintWriter)
	 */
	public void write(int indent, PrintWriter w) {
		String gap = ""; //$NON-NLS-1$
		for (int i = 0; i < indent; i++)
			gap += " "; //$NON-NLS-1$
		String increment = ""; //$NON-NLS-1$
		for (int i = 0; i < IWritable.INDENT; i++)
			increment += " "; //$NON-NLS-1$
		w.print(gap + "<" + InstallConfigurationParser.CONFIGURATION + " "); //$NON-NLS-1$ //$NON-NLS-2$
		long time = (getCreationDate()!=null)?getCreationDate().getTime():0L;
		w.print("date=\"" + time + "\" "); //$NON-NLS-1$ //$NON-NLS-2$
		w.println(">"); //$NON-NLS-1$
		w.println(""); //$NON-NLS-1$
		// site configurations
		if (getConfigurationSitesModel() != null) {
			ConfiguredSiteModel[] sites = getConfigurationSitesModel();
			for (int i = 0; i < sites.length; i++) {
				ConfiguredSite element = (ConfiguredSite) sites[i];
				((IWritable) element).write(indent + IWritable.INDENT, w);
			}
		}
		// activities
		if (getActivityModel() != null) {
			ConfigurationActivityModel[] activities = getActivityModel();
			for (int i = 0; i < activities.length; i++) {
				ConfigurationActivity element = (ConfigurationActivity) activities[i];
				((IWritable) element).write(indent + IWritable.INDENT, w);
			}
		}
		// end
		w.println(gap + "</" + InstallConfigurationParser.CONFIGURATION + ">"); //$NON-NLS-1$ //$NON-NLS-2$
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

		IConfiguredSite[] oldConfigSites = configuration.getConfiguredSites();
		IConfiguredSite[] nowConfigSites = this.getConfiguredSites();

		// create a hashtable of the *old* sites
		Map oldSitesMap = new Hashtable(0);
		for (int i = 0; i < oldConfigSites.length; i++) {
			IConfiguredSite element = oldConfigSites[i];
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
				IConfiguredSite oldSite = (IConfiguredSite) oldSitesMap.get(key);
				if (oldSite != null) {
					// the Site existed before, calculate teh delta between its current state and the
					// state we are reverting to
					 ((ConfiguredSite) oldSite).deltaWith(nowConfigSites[i], monitor, handler);
					nowConfigSites[i] = oldSite;
				} else {
					// the site didn't exist in the InstallConfiguration we are reverting to
					// unconfigure everything from this site so it is still present
					IFeatureReference[] featuresToUnconfigure = nowConfigSites[i].getSite().getFeatureReferences();
					for (int j = 0; j < featuresToUnconfigure.length; j++) {
						nowConfigSites[i].unconfigure(featuresToUnconfigure[j].getFeature());
					}
				}
			}
			// the new configuration has the exact same sites as the old configuration
			// the old configuration in the Map are either as-is because they don't exist
			// in the current one, or they are the delta from the current one to the old one
			Collection sites = oldSitesMap.values();
			if (sites != null && !sites.isEmpty()) {
				ConfiguredSiteModel[] sitesModel = new ConfiguredSiteModel[sites.size()];
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
		if (getActivityModel().length == 0)
			return new IActivity[0];
		return (IActivity[]) getActivityModel();
	}

}