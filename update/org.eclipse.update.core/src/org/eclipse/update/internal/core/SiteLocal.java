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
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.ConfigurationActivityModel;
import org.eclipse.update.core.model.ConfigurationSiteModel;
import org.eclipse.update.core.model.InstallConfigurationModel;
import org.eclipse.update.core.model.SiteLocalModel;
import org.eclipse.update.core.model.SiteLocalParser;
import org.xml.sax.SAXException;

/**
 * This class manages the configurations.
 */

public class SiteLocal extends SiteLocalModel implements ILocalSite, IWritable {

	private ListenersList listeners = new ListenersList();

	/**
	 * initialize the configurations from the persistent model.
	 * The configurations are per user, so we save the data in the 
	 * user path, not the .metadata of any workspace, so the data
	 * is shared between the workspaces.
	 */
	public static ILocalSite getLocalSite() throws CoreException {
		URL configXML = null;
		SiteLocal site = null;

		try {
			site = new SiteLocal();
			
			// obtain read/write location
			IPlatformConfiguration platformConfig = BootLoader.getCurrentPlatformConfiguration();
			URL location = Platform.resolve(platformConfig.getConfigurationLocation());
	 		configXML = UpdateManagerUtils.getURL(location, SITE_LOCAL_FILE, null);
	 		
	 		// set it into the ILocalSite
			site.setLocationURLString(configXML.toExternalForm());
			site.resolve(configXML, null);
				 		
			//attempt to parse the SITE_LOCAL_FILE file	
			URL resolvedURL = URLEncoder.encode(configXML);
			new SiteLocalParser(resolvedURL.openStream(), site);
			
		} catch (FileNotFoundException exception) {
			// file doesn't exist, ok, log it and continue 
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug(site.getLocationURLString() + " does not exist, there is no previous state or install history we can recover, we shall use default.");
			}

			createDefaultConfiguration(site);

			// FIXME: always save ?
			site.save();

		} catch (SAXException exception) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error during parsing of the install config XML:" + site.getLocationURLString(), exception);
			throw new CoreException(status);
		} catch (MalformedURLException exception) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot create URL from: " + site.getLocationURLString() + " & " + SITE_LOCAL_FILE, exception);
			throw new CoreException(status);
		} catch (IOException exception) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot read xml file: " + configXML, exception);
			throw new CoreException(status);
		}

		return site;
	}

	private static void createDefaultConfiguration(ILocalSite localSite) throws CoreException {

		try {
			IPlatformConfiguration.ISiteEntry[] siteEntries = BootLoader.getCurrentPlatformConfiguration().getConfiguredSites();

			// create first InstallConfiguration
			IInstallConfiguration newDefaultConfiguration = localSite.cloneCurrentConfiguration(null, null);
			localSite.addConfiguration(newDefaultConfiguration);			

			IConfigurationSite[] configSites = new IConfigurationSite[siteEntries.length];
			// add each site to the configuration
			for (int siteIndex = 0; siteIndex < siteEntries.length; siteIndex++) {

				URL resolvedURL = Platform.resolve(siteEntries[siteIndex].getURL());
				ISite site = SiteManager.getSite(resolvedURL);

				//site policy
				IPlatformConfiguration.ISitePolicy sitePolicy = siteEntries[siteIndex].getSitePolicy();
				ConfigurationSite configSite = (ConfigurationSite) SiteManager.createConfigurationSite(site,sitePolicy.getType());
				
				//the site may not be read-write
				configSite.setInstallSite(siteEntries[siteIndex].isUpdateable());
				configSites[siteIndex] = configSite;
			}

			InstallConfiguration currentConfig = (InstallConfiguration)localSite.getCurrentConfiguration();
			((InstallConfiguration)currentConfig).setConfigurationSites(configSites);

		} catch (Exception e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot create the Local Site: " + e.getMessage(), e);
			throw new CoreException(status);
		}
	}

	private SiteLocal(){
	}

	/**
	 * adds a new configuration to the LocalSite
	 *  the newly added configuration is teh current one
	 */
	public void addConfiguration(IInstallConfiguration config) {
		if (config != null) {
			addConfigurationModel((InstallConfigurationModel) config);

			// check if we have to remove a configuration
			// the first added is #0
			while (getConfigurationHistory().length > getMaximumHistory()) {
				InstallConfigurationModel removedConfig = getConfigurationHistoryModel()[0];
				if (removeConfigurationModel(removedConfig)) {

					// DEBUG:
					if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_CONFIGURATION) {
						UpdateManagerPlugin.getPlugin().debug("Removed configuration :" + removedConfig.getLabel());
					}

					// notify listeners
					Object[] siteLocalListeners = listeners.getListeners();
					for (int i = 0; i < siteLocalListeners.length; i++) {
						((ILocalSiteChangedListener) siteLocalListeners[i]).installConfigurationRemoved((IInstallConfiguration) removedConfig);
					}
				}
				// FIXME: remove file ? Can be shared or remote !!!
			}

			// set configuration as current		
			if (getCurrentConfigurationModel() != null)
				getCurrentConfigurationModel().setCurrent(false);
			config.setCurrent(true);
			setCurrentConfigurationModel((InstallConfigurationModel) config);
			((InstallConfigurationModel) config).markReadOnly();

			// notify listeners
			Object[] siteLocalListeners = listeners.getListeners();
			for (int i = 0; i < siteLocalListeners.length; i++) {
				((ILocalSiteChangedListener) siteLocalListeners[i]).currentInstallConfigurationChanged(config);
			}
		}

	}
	/*
	 * @see ILocalSite#addLocalSiteChangedListener(ILocalSiteChangedListener)
	 */
	public void addLocalSiteChangedListener(ILocalSiteChangedListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/*
	 * @see ILocalSite#removeLocalSiteChangedListener(ILocalSiteChangedListener)
	 */
	public void removeLocalSiteChangedListener(ILocalSiteChangedListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * Saves the site into the config file
	 */
	public void save() throws CoreException {

		// Save the current configuration as
		// the other are already saved
		// and set runtim info for next startup
		 ((InstallConfiguration) getCurrentConfiguration()).save();

		// save the local site
		if (getLocationURL().getProtocol().equalsIgnoreCase("file")) {
			File file = null;
			try {
				URL newURL = UpdateManagerUtils.getURL(getLocationURL(), SITE_LOCAL_FILE, null);
				file = new File(newURL.getFile());
				PrintWriter fileWriter = new PrintWriter(new FileOutputStream(file));
				Writer writer = new Writer();
				writer.writeSite(this, fileWriter);
				fileWriter.close();
			} catch (FileNotFoundException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot save site into " + file.getAbsolutePath(), e);
				throw new CoreException(status);
			} catch (MalformedURLException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot get handle on configuration file " + getLocationURL().toExternalForm() + " : " + SITE_LOCAL_FILE, e);
				throw new CoreException(status);
			}
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

		w.print(gap + "<" + SiteLocalParser.SITE + " ");
		if (getLabel() != null) {
			w.print("label=\"" + Writer.xmlSafe(getLabel()) + "\" ");
		}
		w.print("history=\"" + getMaximumHistory() + "\" ");
		w.println(">");
		w.println("");

		// teh last one is teh current configuration
		InstallConfigurationModel[] configurations = getConfigurationHistoryModel();
		for (int index = 0; index < configurations.length; index++) {
			InstallConfigurationModel element = configurations[index];
			if (!element.isCurrent()) {
				writeConfig(gap + increment, w, element);
			}
		}
		// write current configuration last
		writeConfig(gap + increment, w, (InstallConfigurationModel)getCurrentConfiguration());
		w.println("");

		if (getPreservedConfigurations() != null) {
			// write preserved configurations
			w.print(gap + increment + "<" + SiteLocalParser.PRESERVED_CONFIGURATIONS + ">");

			InstallConfigurationModel[] preservedConfig = getPreservedConfigurationsModel();
			for (int index = 0; index < preservedConfig.length; index++) {
				InstallConfigurationModel element = preservedConfig[index];
				writeConfig(gap + increment + increment, w, element);
			}
			w.println("");
			w.print(gap + increment + "</" + SiteLocalParser.PRESERVED_CONFIGURATIONS + ">");
		}
		// end
		w.println("</" + SiteLocalParser.SITE + ">");
	}

	private void writeConfig(String gap, PrintWriter w, InstallConfigurationModel config) {
		w.print(gap + "<" + SiteLocalParser.CONFIG + " ");
		String URLInfoString = UpdateManagerUtils.getURLAsString(getLocationURL(), config.getURL());
		w.print("url=\"" + Writer.xmlSafe(URLInfoString) + "\" ");

		if (config.getLabel() != null) {
			w.print("label=\"" + Writer.xmlSafe(config.getLabel()) + "\"");
		}

		w.println("/>");
	}

	/**
	 * @since 2.0
	 */
	public IInstallConfiguration cloneCurrentConfiguration(URL newFile, String name) throws CoreException {

		// save previous current configuration
		if (getCurrentConfiguration() != null)
			 ((InstallConfiguration) getCurrentConfiguration()).saveConfigurationFile();

		InstallConfiguration result = null;
		Date currentDate = new Date();

		String newFileName = UpdateManagerUtils.getLocalRandomIdentifier(DEFAULT_CONFIG_FILE, currentDate);
		try {
			if (newFile == null)
				newFile = UpdateManagerUtils.getURL(getLocationURL(), newFileName, null);
			// pass the date onto teh name
			if (name == null)
				name = currentDate.toString();
			result = new InstallConfiguration(getCurrentConfiguration(), newFile, name);
			// set teh same date in the installConfig
			result.setCreationDate(currentDate);
		} catch (MalformedURLException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot create a new configuration in:" + newFileName, e);
			throw new CoreException(status);
		}
		return result;
	}

	/**
	 * @since 2.0
	 */
	public void revertTo(IInstallConfiguration configuration, IProgressMonitor monitor, IProblemHandler handler) throws CoreException {

		// create the activity 
		//Start UOW ?
		ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_REVERT);
		activity.setLabel(configuration.getLabel());
		activity.setDate(new Date());
		IInstallConfiguration newConfiguration = null;

		try {
			// create a configuration
			newConfiguration = cloneCurrentConfiguration(null, configuration.getLabel());

			// process delta
			// the Configured featuresConfigured are the same as the old configuration
			// the unconfigured featuresConfigured are the rest...
			 ((InstallConfiguration) newConfiguration).revertTo(configuration, monitor, handler);

			// add to the stack which will set up as current
			addConfiguration(newConfiguration);

			// everything done ok
			activity.setStatus(IActivity.STATUS_OK);
		} catch (CoreException e) {
			// error
			activity.setStatus(IActivity.STATUS_NOK);
			throw e;
		} catch (InterruptedException e) {
			//user decided not to revert, do nothing
			// because we didn't add the configuration to the history
		} finally {
			if (newConfiguration != null)
				 ((InstallConfiguration) newConfiguration).addActivityModel((ConfigurationActivityModel) activity);
		}

	}

	/**
	 * @since 2.0
	 */
	public void addToPreservedConfigurations(IInstallConfiguration configuration) throws CoreException {
		if (configuration != null) {

			// create new configuration based on the one to preserve
			InstallConfiguration newConfiguration = null;
			String newFileName = UpdateManagerUtils.getLocalRandomIdentifier(DEFAULT_PRESERVED_CONFIG_FILE, new Date());
			try {
				URL newFile = UpdateManagerUtils.getURL(getLocationURL(), newFileName, null);
				// pass the date onto teh name
				Date currentDate = configuration.getCreationDate();
				String name = configuration.getLabel();
				newConfiguration = new InstallConfiguration(configuration, newFile, name);
				// set teh same date in the installConfig
				newConfiguration.setCreationDate(currentDate);

				System.out.println("addToPreserved URL:" + newFile.toExternalForm() + " name:" + name + " date:" + currentDate.getTime());
			} catch (MalformedURLException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot create a new preserved configuration in:" + newFileName, e);
				throw new CoreException(status);
			}
			((InstallConfiguration) newConfiguration).saveConfigurationFile();

			// add to the list			
			addPreservedInstallConfigurationModel((InstallConfigurationModel) newConfiguration);
		}
	}

	/*
	 * @see ILocalSite#getPreservedConfigurationFor(IInstallConfiguration)
	 */
	public IInstallConfiguration getPreservedConfigurationFor(IInstallConfiguration configuration) {

		// based on time stamp for now
		InstallConfigurationModel preservedConfig = null;
		if (configuration != null) {
			InstallConfigurationModel[] preservedConfigurations = getPreservedConfigurationsModel();
			if (preservedConfigurations != null) {
				for (int indexPreserved = 0; indexPreserved < preservedConfigurations.length; indexPreserved++) {
					if (configuration.getCreationDate().equals(preservedConfigurations[indexPreserved].getCreationDate())) {
						preservedConfig = preservedConfigurations[indexPreserved];
						break;
					}
				}
			}
		}

		return (IInstallConfiguration) preservedConfig;
	}

	/**
	 * returns a list of PluginEntries that are not used by any other configured feature
	 */
	public IPluginEntry[] getUnusedPluginEntries(IFeature feature) throws CoreException {

		IPluginEntry[] pluginsToRemove = new IPluginEntry[0];

		// get the plugins from the feature
		IPluginEntry[] entries = feature.getPluginEntries();
		if (entries != null) {
			// get all the other plugins from all the other features
			Set allPluginID = new HashSet();
			InstallConfigurationModel currentConfigurationModel = (InstallConfigurationModel)getCurrentConfiguration();
			ConfigurationSiteModel[] allConfiguredSites = currentConfigurationModel.getConfigurationSitesModel();
			if (allConfiguredSites != null) {
				for (int indexSites = 0; indexSites < allConfiguredSites.length; indexSites++) {
					IFeatureReference[] features = ((IConfigurationSite) allConfiguredSites[indexSites]).getConfiguredFeatures();
					if (features != null) {
						for (int indexFeatures = 0; indexFeatures < features.length; indexFeatures++) {
							if (!features[indexFeatures].getURL().equals(feature.getURL())) {
								IPluginEntry[] pluginEntries = features[indexFeatures].getFeature().getPluginEntries();
								if (pluginEntries != null) {
									for (int indexEntries = 0; indexEntries < pluginEntries.length; indexEntries++) {
										allPluginID.add(entries[indexEntries].getVersionIdentifier());
									}
								}
							}
						}
					}
				}
			}

			// create the delta with the plugins that may be still used by other configured or unconfigured feature
			List plugins = new ArrayList();
			for (int indexPlugins = 0; indexPlugins < entries.length; indexPlugins++) {
				if (!allPluginID.contains(entries[indexPlugins].getVersionIdentifier())) {
					plugins.add(entries[indexPlugins]);
				}
			}

			// move List into Array
			if (!plugins.isEmpty()) {
				pluginsToRemove = new IPluginEntry[plugins.size()];
				plugins.toArray(pluginsToRemove);
			}

		}

		return pluginsToRemove;
	}

	/*
	 * @see ILocalSite#getCurrentConfiguration()
	 * LocalSiteModel#getCurrentConfigurationModel() may return null if
	 * we just parsed LocalSite.xml
	 */
	public IInstallConfiguration getCurrentConfiguration() {
		if (getCurrentConfigurationModel() == null){
			int index = 0;
			if ((index = getConfigurationHistoryModel().length)==0) {
				return null;
			} else {
				InstallConfigurationModel config = getConfigurationHistoryModel()[index-1];
				config.setCurrent(true);
				setCurrentConfigurationModel(config);
			}
		}
		return (IInstallConfiguration) getCurrentConfigurationModel();
	}

	/*
	 * @see ILocalSite#getPreservedConfigurations()
	 */
	public IInstallConfiguration[] getPreservedConfigurations() {
		if (getPreservedConfigurationsModel().length == 0)
			return new IInstallConfiguration[0];
		return (IInstallConfiguration[]) getPreservedConfigurationsModel();
	}

	/*
	 * @see ILocalSite#removeFromPreservedConfigurations(IInstallConfiguration)
	 */
	public void removeFromPreservedConfigurations(IInstallConfiguration configuration) {
		if (removePreservedConfigurationModel((InstallConfigurationModel) configuration))
			 ((InstallConfiguration) configuration).remove();
	}

	/**
	 * 
	 */
	public IInstallConfiguration importConfiguration(URL importURL, String label) throws CoreException {
		InstallConfiguration config = null;
		try {
			config = new InstallConfiguration(importURL, label);
		} catch (MalformedURLException e){
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Unable to import Configuration " + importURL.toExternalForm(), e);
			throw new CoreException(status);			
		}
		return config;
	}

	/*
	 * @see ILocalSite#getConfigurationHistory()
	 */
	public IInstallConfiguration[] getConfigurationHistory() {
		if (getConfigurationHistoryModel().length==0)
			return new IInstallConfiguration[0];
		return (IInstallConfiguration[])getConfigurationHistoryModel();
	}

}

