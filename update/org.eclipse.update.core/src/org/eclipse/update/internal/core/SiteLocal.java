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
import org.eclipse.update.core.model.*;
import org.xml.sax.SAXException;

/**
 * This class manages the configurations.
 */

public class SiteLocal extends SiteLocalModel implements ILocalSite, IWritable {

	private static IPluginEntry[] allRunningPluginEntry;
	private ListenersList listeners = new ListenersList();

	/**
	 * initialize the configurations from the persistent model.
	 * The configurations are per user, so we save the data in the 
	 * user path, not the .metadata of any workspace, so the data
	 * is shared between the workspaces.
	 */
	public static ILocalSite getLocalSite() throws CoreException {
		URL configXML = null;
		SiteLocal site = new SiteLocal();

		// obtain read/write location
		IPlatformConfiguration currentPlatformConfiguration = BootLoader.getCurrentPlatformConfiguration();
		IPlatformConfiguration platformConfig = currentPlatformConfiguration;
		try {
			URL platformConfigurationLocation = platformConfig.getConfigurationLocation();
			URL location = Platform.resolve(platformConfigurationLocation);
			configXML = UpdateManagerUtils.getURL(location, SITE_LOCAL_FILE, null);

			// set it into the ILocalSite
			site.setLocationURLString(configXML.toExternalForm());
			site.resolve(configXML, null);

			//attempt to parse the SITE_LOCAL_FILE file	
			URL resolvedURL = URLEncoder.encode(configXML);
			new SiteLocalParser(resolvedURL.openStream(), site);

			// check if we have to reconcile
			long bootStamp = currentPlatformConfiguration.getChangeStamp();
			if (site.getStamp() != bootStamp) {
				if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
					UpdateManagerPlugin.getPlugin().debug("Reconcile platform stamp:" + bootStamp + " is different from LocalSite stamp:" + site.getStamp());
				}
				site.setStamp(bootStamp);
				site.reconcile();
				site.save();
			} else {
				// no reconciliation, preserve the list of plugins from the platform anyway
				site.preserveRuntimePluginPath();
			}

		} catch (FileNotFoundException exception) {
			// file SITE_LOCAL_FILE doesn't exist, ok, log it 
			// and reconcile
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug(site.getLocationURLString() + " does not exist, there is no previous state or install history we can recover, we shall use default.");
			}
			long bootStamp = currentPlatformConfiguration.getChangeStamp();
			site.setStamp(bootStamp);
			site.reconcile();
			site.save();

		} catch (SAXException exception) {
			throw newCoreException("Error during parsing of the install config XML:" + site.getLocationURLString(), exception);
		} catch (MalformedURLException exception) {
			throw newCoreException("Cannot create URL from: " + site.getLocationURLString() + " & " + SITE_LOCAL_FILE, exception);
		} catch (IOException exception) {
			throw newCoreException("Cannot read xml file: " + configXML, exception);
		}

		return site;
	}

	private SiteLocal() {
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
				throw newCoreException("Cannot save site into " + file.getAbsolutePath(), e);
			} catch (MalformedURLException e) {
				throw newCoreException("Cannot get handle on configuration file " + getLocationURL().toExternalForm() + " : " + SITE_LOCAL_FILE, e);
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
		w.print("stamp=\"" + BootLoader.getCurrentPlatformConfiguration().getChangeStamp() + "\" ");
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
		writeConfig(gap + increment, w, (InstallConfigurationModel) getCurrentConfiguration());
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

	/**
	 * @since 2.0
	 */
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
	public IInstallConfiguration cloneConfigurationSite(IInstallConfiguration installConfig, URL newFile, String name) throws CoreException {

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
			result = new InstallConfiguration(installConfig, newFile, name);
			// set teh same date in the installConfig
			result.setCreationDate(currentDate);
		} catch (MalformedURLException e) {
			throw newCoreException("Cannot create a new configuration in:" + newFileName, e);
		}
		return result;
	}

	/**
	 * @since 2.0
	 */
	public IInstallConfiguration cloneCurrentConfiguration(URL newFile, String name) throws CoreException {
		return cloneConfigurationSite(getCurrentConfiguration(), newFile, name);
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
				throw newCoreException("Cannot create a new preserved configuration in:" + newFileName, e);
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
			InstallConfigurationModel currentConfigurationModel = (InstallConfigurationModel) getCurrentConfiguration();
			ConfigurationSiteModel[] allConfiguredSites = currentConfigurationModel.getConfigurationSitesModel();
			if (allConfiguredSites != null) {
				for (int indexSites = 0; indexSites < allConfiguredSites.length; indexSites++) {
					IFeatureReference[] features = ((IConfigurationSite) allConfiguredSites[indexSites]).getConfiguredFeatures();
					if (features != null) {
						for (int indexFeatures = 0; indexFeatures < features.length; indexFeatures++) {
							if (!features[indexFeatures].equals(feature)) {
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
		if (getCurrentConfigurationModel() == null) {
			int index = 0;
			if ((index = getConfigurationHistoryModel().length) == 0) {
				return null;
			} else {
				InstallConfigurationModel config = getConfigurationHistoryModel()[index - 1];
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
		} catch (MalformedURLException e) {
			throw newCoreException("Unable to import Configuration " + importURL.toExternalForm(), e);
		}
		return config;
	}

	/*
	 * @see ILocalSite#getConfigurationHistory()
	 */
	public IInstallConfiguration[] getConfigurationHistory() {
		if (getConfigurationHistoryModel().length == 0)
			return new IInstallConfiguration[0];
		return (IInstallConfiguration[]) getConfigurationHistoryModel();
	}

	/*
	 * @see ILocalSite#reconcile()
	 */
	public void reconcile() throws CoreException {
		try {
			IPlatformConfiguration platformConfig = BootLoader.getCurrentPlatformConfiguration();
			IPlatformConfiguration.ISiteEntry[] siteEntries = platformConfig.getConfiguredSites();

			// Either it is a new site or it already exists, or it is deleted
			// new site only exist in platformConfig
			List modified = new ArrayList();
			List toInstall = new ArrayList();
			List brokenLink = new ArrayList();
			IConfigurationSite[] configured = new IConfigurationSite[0];

			// sites from the current configuration
			if (getCurrentConfiguration() != null)
				configured = getCurrentConfiguration().getConfigurationSites();

			// sites from the platform			
			for (int siteIndex = 0; siteIndex < siteEntries.length; siteIndex++) {

				URL resolvedURL = Platform.resolve(siteEntries[siteIndex].getURL());
				boolean found = false;
				for (int index = 0; index < configured.length && !found; index++) {

					// the array may have hole as we set found site to null
					if (configured[index] != null) {
						if (configured[index].getSite().getURL().equals(resolvedURL)) {
							found = true;
							((ConfigurationSite) configured[index]).setPreviousPluginPath(siteEntries[siteIndex].getSitePolicy().getList());
							modified.add(configured[index]);
							configured[index] = null;
						}
					}
				}
				// new site not found, create it
				if (!found) {
					ISite site = SiteManager.getSite(resolvedURL);
					//site policy
					IPlatformConfiguration.ISitePolicy sitePolicy = siteEntries[siteIndex].getSitePolicy();
					ConfigurationSite configSite = (ConfigurationSite) new BaseSiteLocalFactory().createConfigurationSiteModel((SiteMapModel) site, sitePolicy.getType());
					configSite.setPlatformURLString(siteEntries[siteIndex].getURL().toExternalForm());
					configSite.setPreviousPluginPath(siteEntries[siteIndex].getSitePolicy().getList());

					//the site may not be read-write
					configSite.setInstallSite(siteEntries[siteIndex].isUpdateable());

					// check if the features are configured
					IFeatureReference[] ref = configSite.getSite().getFeatureReferences();
					for (int i = 0; i < ref.length; i++) {
						checkConfigure(ref[i], configSite);
					}

					toInstall.add(configSite);
				}
			}

			// get the broken sites.... teh one that are not setup and not modified
			for (int g = 0; g < configured.length; g++) {
				if (configured[g] != null)
					brokenLink.add(configured[g]);
			}

			// we now have three lists

			// create new InstallConfiguration
			IInstallConfiguration newDefaultConfiguration = cloneConfigurationSite(null, null, null);

			// check modified config site
			// and add them back
			Iterator checkIter = modified.iterator();
			while (checkIter.hasNext()) {
				IConfigurationSite element = (IConfigurationSite) checkIter.next();
				newDefaultConfiguration.addConfigurationSite(reconcile(element));
			}

			// add new sites
			Iterator addIter = toInstall.iterator();
			while (addIter.hasNext()) {
				IConfigurationSite element = (IConfigurationSite) addIter.next();
				newDefaultConfiguration.addConfigurationSite(element);
			}

			// add the broken one as is
			Iterator brokenIter = brokenLink.iterator();
			while (brokenIter.hasNext()) {
				IConfigurationSite element = (IConfigurationSite) addIter.next();
				((ConfigurationSiteModel) element).setBroken(true);
				newDefaultConfiguration.addConfigurationSite(element);
			}

			this.addConfiguration(newDefaultConfiguration);

		} catch (IOException e) {
			throw newCoreException("Cannot create the Local Site: " + e.getMessage(), e);
		}
	}

	private IConfigurationSite reconcile(IConfigurationSite toReconcile) throws CoreException {

		// create a copy of the ConfigSite without any feature
		// this is not a clone
		SiteMapModel siteModel = (SiteMapModel) toReconcile.getSite();
		int policy = toReconcile.getConfigurationPolicy().getPolicy();
		ConfigurationSiteModel newSiteModel = new BaseSiteLocalFactory().createConfigurationSiteModel(siteModel, policy);

		// copy values
		newSiteModel.setInstallSite(toReconcile.isInstallSite());
		newSiteModel.setPlatformURLString(((ConfigurationSiteModel) toReconcile).getPlatformURLString());
		newSiteModel.setPreviousPluginPath(toReconcile.getPreviousPluginPath());

		// check the Features that are still here
		List toCheck = new ArrayList();
		List brokenFeature = new ArrayList();
		IFeatureReference[] configured = toReconcile.getSite().getFeatureReferences();
		FeatureReferenceModel[] found = newSiteModel.getSiteModel().getFeatureReferenceModels();

		for (int i = 0; i < found.length; i++) {
			for (int j = 0; j < configured.length; j++) {
				if (configured[j] != null && configured[j].equals(found[i])) {
					toCheck.add(configured[j]);
					configured[j] = null;
				}
			}
		}

		// the features that are still in the array are broken
		for (int k = 0; k < configured.length; k++) {
			if (configured[k] != null)
				brokenFeature.add(configured[k]);
		}

		// check the Plugins of all the features
		// every plugin of the feature must be on the site
		ISite currentSite = null;
		IPluginEntry[] siteEntries = null;
		Iterator featureIter = toCheck.iterator();
		while (featureIter.hasNext()) {
			IFeatureReference element = (IFeatureReference) featureIter.next();

			if (currentSite == null || !currentSite.equals(element.getSite())) {
				currentSite = element.getSite();
				siteEntries = currentSite.getPluginEntries();
			}
			IPluginEntry[] featuresEntries = element.getFeature().getPluginEntries();
			IPluginEntry[] result = substract(featuresEntries, siteEntries);
			if (result == null || (result.length != 0)) {
				((FeatureReferenceModel) element).setBroken(true);
				IPluginEntry[] missing = substract(featuresEntries, result);
				String listOfMissingPlugins = "";
				for (int k = 0; k < missing.length; k++) {
					listOfMissingPlugins = "\r\nplugin:" + missing[k].getVersionIdentifier().toString();
				}
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "The feature " + element.getURL().toExternalForm() + " requires some missing plugins from the site:" + currentSite.getURL().toExternalForm() + listOfMissingPlugins, null);
				UpdateManagerPlugin.getPlugin().getLog().log(status);
			}
			checkConfigure(element, (ConfigurationSite) newSiteModel);
		}

		//add broken features
		Iterator brokenIter = brokenFeature.iterator();
		while (brokenIter.hasNext()) {
			IFeatureReference element = (IFeatureReference) featureIter.next();
			((FeatureReferenceModel) element).setBroken(true);
			// if it is broken (ie the feature does not exist, we 
			//cannot check the plugins... consider as unconfigured)
			 ((IConfigurationSite) newSiteModel).unconfigure(element, null);
		}

		return (IConfigurationSite) newSiteModel;
	}

	/**
	 * Returns the plugin entries that are in source array and
	 * missing from target array
	 */
	private IPluginEntry[] substract(IPluginEntry[] sourceArray, IPluginEntry[] targetArray) {

		// No pluginEntry to Install, return Nothing to instal
		if (sourceArray == null || sourceArray.length == 0) {
			return new IPluginEntry[0];
		}

		// No pluginEntry installed, Install them all
		if (targetArray == null || targetArray.length == 0) {
			return sourceArray;
		}

		// if a IPluginEntry from sourceArray is NOT in
		// targetArray, add it to the list
		List list1 = Arrays.asList(targetArray);
		List result = new ArrayList(0);
		for (int i = 0; i < sourceArray.length; i++) {
			if (!list1.contains(sourceArray[i]))
				result.add(sourceArray[i]);
		}

		IPluginEntry[] resultEntry = new IPluginEntry[result.size()];
		if (result.size() > 0)
			result.toArray(resultEntry);

		return resultEntry;
	}

	/**
	 * Check if all the plugins of the feature are configured in the runtime
	 * depending on teh answer add it to the newSite as either configured or unconfigured
	 * We have to check all the running plugin, even if they come from different site
	 * A feature may be broken because all the plugins are not in the Site, but may be configured
	 * because soem of the needed plugins come from other site
	 */
	private void checkConfigure(IFeatureReference ref, ConfigurationSite newConfigSite) throws CoreException {
		// check if all the needed plugins are part of all plugin
		boolean configured = false;
		//FIXME right now we figure the exact match
		// but a feature can be running b/c a *compatible* plugin 
		// match has been found
		// we should check the id only, forget about version ???
		// algorithm has to be reviewed...
		IPluginEntry[] allPlugins = getAllRunningPlugin();
		IFeature feature = ref.getFeature();
		IPluginEntry[] result = new IPluginEntry[0];
		if (feature != null) {
			IPluginEntry[] featurePlugins = ref.getFeature().getPluginEntries();
			result = substract(featurePlugins, allPlugins);
			if (result.length == 0) {
				configured = true;
			}

		} else {
			((FeatureReference) ref).setBroken(true);
		}

		// there are some plugins the feature need that are not present
		if (!configured || ref.isBroken()) {
			(newConfigSite.getConfigurationPolicyModel()).addUnconfiguredFeatureReference((FeatureReferenceModel) ref);
		} else {
			(newConfigSite.getConfigurationPolicyModel()).addConfiguredFeatureReference((FeatureReferenceModel) ref);
		}

	}

	private static IPluginEntry[] getAllRunningPlugin() throws CoreException {
		if (allRunningPluginEntry == null) {
			// get all the running plugins
			URL[] pluginPathURL = BootLoader.getCurrentPlatformConfiguration().getPluginPath();
			IPluginDescriptor[] descriptors = Platform.getPluginRegistry().getPluginDescriptors();
			allRunningPluginEntry = new PluginEntry[descriptors.length];
			if (descriptors.length > 0) {
				for (int i = 0; i < descriptors.length; i++) {
					String ver = descriptors[i].getVersionIdentifier().toString();
					String id = descriptors[i].getUniqueIdentifier().toString();
					PluginEntry entry = new PluginEntry();
					entry.setPluginIdentifier(id);
					entry.setPluginVersion(ver);
					allRunningPluginEntry[i] = entry;
				}
			}
		}

		return allRunningPluginEntry;
	}

	/**
	 * Add teh list of plugins the platform found for each site. This list will ba preserved in 
	 * a transient way so we do not lose explicitly set plugins
	 */
	private void preserveRuntimePluginPath() throws CoreException {
		try {
			IPlatformConfiguration platformConfig = BootLoader.getCurrentPlatformConfiguration();
			IPlatformConfiguration.ISiteEntry[] siteEntries = platformConfig.getConfiguredSites();

			// sites from the current configuration
			IConfigurationSite[] configured = new IConfigurationSite[0];
			if (getCurrentConfiguration() != null)
				configured = getCurrentConfiguration().getConfigurationSites();

			// sites from the platform			
			for (int siteIndex = 0; siteIndex < siteEntries.length; siteIndex++) {

				URL resolvedURL = Platform.resolve(siteEntries[siteIndex].getURL());
				boolean found = false;
				for (int index = 0; index < configured.length && !found; index++) {

					// the array may have hole as we set found site to null
					if (configured[index] != null) {
						if (configured[index].getSite().getURL().equals(resolvedURL)) {
							found = true;
							((ConfigurationSite) configured[index]).setPreviousPluginPath(siteEntries[siteIndex].getSitePolicy().getList());
							configured[index] = null;
						}
					}
				}
			}
		} catch (IOException e) {
			throw newCoreException("Cannot create the Local Site: " + e.getMessage(), e);
		}
	}

	/**
	 * returns a Core Exception
	 */
	private static CoreException newCoreException(String message, Throwable exception) throws CoreException {
		String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		return new CoreException(new Status(IStatus.ERROR, id, IStatus.OK, message, exception));
	}
}