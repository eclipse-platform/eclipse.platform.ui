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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.FeatureReferenceModel;
import org.eclipse.update.core.model.SiteModel;
import org.eclipse.update.internal.model.*;
import org.xml.sax.SAXException;

/**
 * This class manages the configurations.
 */

public class SiteLocal extends SiteLocalModel implements ILocalSite, IWritable {

	private static IPluginEntry[] allRunningPluginEntry;
	private static SiteLocal localSite;
	private ListenersList listeners = new ListenersList();
	private boolean isTransient = false;
	
	private static final String UPDATE_STATE_SUFFIX = ".metadata";

	/**
	 * initialize the configurations from the persistent model.
	 * The configurations are per user, so we save the data in the 
	 * user path, not the .metadata of any workspace, so the data
	 * is shared between the workspaces.
	 */
	public static ILocalSite getLocalSite() throws CoreException {
		
		if (localSite!=null) return localSite;
		
		URL configXML = null;
		SiteLocal localSite = new SiteLocal();

		// obtain read/write location
		IPlatformConfiguration currentPlatformConfiguration = BootLoader.getCurrentPlatformConfiguration();
		//localSite.isTransient(currentPlatformConfiguration.isTransient());
		try {
			URL location = getUpdateStateLocation(currentPlatformConfiguration);
			configXML = UpdateManagerUtils.getURL(location, SITE_LOCAL_FILE, null);

			// set it into the ILocalSite
			localSite.setLocationURLString(configXML.toExternalForm());
			localSite.resolve(configXML, null);

			//attempt to parse the SITE_LOCAL_FILE file	
			URL resolvedURL = URLEncoder.encode(configXML);
			new SiteLocalParser(resolvedURL.openStream(), localSite);

			// check if we have to reconcile
			long bootStamp = currentPlatformConfiguration.getChangeStamp();
			if (localSite.getStamp() != bootStamp) {
				if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
					UpdateManagerPlugin.getPlugin().debug("Reconcile platform stamp:" + bootStamp + " is different from LocalSite stamp:" + localSite.getStamp()); //$NON-NLS-1$ //$NON-NLS-2$
				}
				localSite.setStamp(bootStamp);
				localSite.reconcile();
				localSite.save();
			} else {
				// no reconciliation, preserve the list of plugins from the platform anyway
				localSite.preserveRuntimePluginPath();
			}

		} catch (FileNotFoundException exception) {
			// file SITE_LOCAL_FILE doesn't exist, ok, log it 
			// and reconcile
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug(localSite.getLocationURLString() + " does not exist, there is no previous state or install history we can recover, we shall use default."); //$NON-NLS-1$
			}
			long bootStamp = currentPlatformConfiguration.getChangeStamp();
			localSite.setStamp(bootStamp);
			localSite.reconcile();
			localSite.save();

		} catch (SAXException exception) {
			throw Utilities.newCoreException(Policy.bind("SiteLocal.ErrorParsingSavedState",localSite.getLocationURLString()), exception); //$NON-NLS-1$
		} catch (MalformedURLException exception) {
			throw Utilities.newCoreException(Policy.bind("SiteLocal.UnableToCreateURLFor", localSite.getLocationURLString() + " & " + SITE_LOCAL_FILE), exception); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (IOException exception) {
			throw Utilities.newCoreException(Policy.bind("SiteLocal.UnableToAccessFile", configXML.toExternalForm()), exception); //$NON-NLS-1$
		}

		return localSite;
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
			while (getConfigurationHistory().length > getMaximumHistoryCount()) {
				InstallConfigurationModel removedConfig = getConfigurationHistoryModel()[0];
				if (removeConfigurationModel(removedConfig)) {

					// DEBUG:
					if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_CONFIGURATION) {
						UpdateManagerPlugin.getPlugin().debug("Removed configuration :" + removedConfig.getLabel()); //$NON-NLS-1$
					}

					// notify listeners
					Object[] siteLocalListeners = listeners.getListeners();
					for (int i = 0; i < siteLocalListeners.length; i++) {
						((ILocalSiteChangedListener) siteLocalListeners[i]).installConfigurationRemoved((IInstallConfiguration) removedConfig);
					}
				}
				// FIXME: should we remove file ? Can they be shared or remote !!!
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
		 ((InstallConfiguration) getCurrentConfiguration()).save(isTransient());

		// save the local site
		if (getLocationURL().getProtocol().equalsIgnoreCase("file")) { //$NON-NLS-1$
			File file = null;
			try {
				URL newURL = UpdateManagerUtils.getURL(getLocationURL(), SITE_LOCAL_FILE, null);
				file = new File(newURL.getFile());
				if (isTransient()) file.deleteOnExit();
				PrintWriter fileWriter = new PrintWriter(new FileOutputStream(file));
				Writer writer = new Writer();
				writer.writeSite(this, fileWriter);
				fileWriter.close();
			} catch (FileNotFoundException e) {
				throw Utilities.newCoreException(Policy.bind("SiteLocal.UnableToSaveStateIn") + file.getAbsolutePath(), e); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				throw Utilities.newCoreException(Policy.bind("SiteLocal.UnableToCreateURLFor") + getLocationURL().toExternalForm() + " : " + SITE_LOCAL_FILE, e); //$NON-NLS-2$ //$NON-NLS-1$
			}
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

		w.print(gap + "<" + SiteLocalParser.SITE + " "); //$NON-NLS-1$ //$NON-NLS-2$
		if (getLabel() != null) {
			w.print("label=\"" + Writer.xmlSafe(getLabel()) + "\" "); //$NON-NLS-1$ //$NON-NLS-2$
		}
		w.print("history=\"" + getMaximumHistoryCount() + "\" "); //$NON-NLS-1$ //$NON-NLS-2$
		w.print("stamp=\"" + BootLoader.getCurrentPlatformConfiguration().getChangeStamp() + "\" "); //$NON-NLS-1$ //$NON-NLS-2$
		w.println(">"); //$NON-NLS-1$
		w.println(""); //$NON-NLS-1$

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
		w.println(""); //$NON-NLS-1$

		if (getPreservedConfigurations() != null) {
			// write preserved configurations
			w.print(gap + increment + "<" + SiteLocalParser.PRESERVED_CONFIGURATIONS + ">"); //$NON-NLS-1$ //$NON-NLS-2$

			InstallConfigurationModel[] preservedConfig = getPreservedConfigurationsModel();
			for (int index = 0; index < preservedConfig.length; index++) {
				InstallConfigurationModel element = preservedConfig[index];
				writeConfig(gap + increment + increment, w, element);
			}
			w.println(""); //$NON-NLS-1$
			w.print(gap + increment + "</" + SiteLocalParser.PRESERVED_CONFIGURATIONS + ">"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// end
		w.println("</" + SiteLocalParser.SITE + ">"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @since 2.0
	 */
	private void writeConfig(String gap, PrintWriter w, InstallConfigurationModel config) {
		w.print(gap + "<" + SiteLocalParser.CONFIG + " "); //$NON-NLS-1$ //$NON-NLS-2$
		String URLInfoString = UpdateManagerUtils.getURLAsString(getLocationURL(), config.getURL());
		w.print("url=\"" + Writer.xmlSafe(URLInfoString) + "\" "); //$NON-NLS-1$ //$NON-NLS-2$

		if (config.getLabel() != null) {
			w.print("label=\"" + Writer.xmlSafe(config.getLabel()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		}

		w.println("/>"); //$NON-NLS-1$
	}

	/**
	 * @since 2.0
	 */
	private IInstallConfiguration cloneConfigurationSite(IInstallConfiguration installConfig, URL newFile, String name) throws CoreException {

		// save previous current configuration
		if (getCurrentConfiguration() != null)
			 ((InstallConfiguration) getCurrentConfiguration()).saveConfigurationFile(isTransient());

		InstallConfiguration result = null;
		Date currentDate = new Date();

		String newFileName = UpdateManagerUtils.getLocalRandomIdentifier(DEFAULT_CONFIG_FILE, currentDate);
		try {
			if (newFile == null)
				newFile = UpdateManagerUtils.getURL(getLocationURL(), newFileName, null);
			// pass the date onto the name
			if (name == null)
				name = currentDate.toString();
			result = new InstallConfiguration(installConfig, newFile, name);
			// set teh same date in the installConfig
			result.setCreationDate(currentDate);
		} catch (MalformedURLException e) {
			throw Utilities.newCoreException(Policy.bind("SiteLocal.UnableToCreateURLFor") + newFileName, e); //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * @since 2.0
	 */
	public IInstallConfiguration cloneCurrentConfiguration() throws CoreException {
		return cloneConfigurationSite(getCurrentConfiguration(), null, null);
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
			newConfiguration = cloneCurrentConfiguration();
			newConfiguration.setLabel(configuration.getLabel());

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

			} catch (MalformedURLException e) {
				throw Utilities.newCoreException(Policy.bind("SiteLocal.UnableToCreateURLFor") + newFileName, e); //$NON-NLS-1$
			}
			((InstallConfiguration) newConfiguration).saveConfigurationFile(isTransient());

			// add to the list			
			addPreservedInstallConfigurationModel((InstallConfigurationModel) newConfiguration);
		}
	}

	/*
	 * @see ILocalSite#getPreservedConfigurationFor(IInstallConfiguration)
	 */
	public IInstallConfiguration findPreservedConfigurationFor(IInstallConfiguration configuration) {

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

	/*
	 * @see ILocalSite#getConfigurationHistory()
	 */
	public IInstallConfiguration[] getConfigurationHistory() {
		if (getConfigurationHistoryModel().length == 0)
			return new IInstallConfiguration[0];
		return (IInstallConfiguration[]) getConfigurationHistoryModel();
	}

	/*
	 * Reconciliation is the comparison between the old preserved state and the new one from platform.cfg
	 * 
	 * If the old state contained sites that are not in the new state, the old sites are not added to the state
	 * 
	 * If the new state contains sites that were not in the old state, configure the site and configure all the found features
	 * 
	 * If the sites are in both states, verify the features
	 * 
	 * if the old site contained features that are not in the new site, the features are not added to the site
	 * 
	 * if the new site contains feature that were not in the old site, configure the new feature
	 * 
	 * if the feature is in both site (old and new), use old feature state
	 * 
	 * When adding a feature to a site, we will check if the feature is broken or not. 
	 * A feature is broken when at least one of its plugin is not installed on the site.
	 * 
	 * At the end, go over all the site, get the configured features and make sure that if we find duplicates
	 * only one feature is configured
	 */
	public void reconcile() throws CoreException {

		IPlatformConfiguration platformConfig = BootLoader.getCurrentPlatformConfiguration();
		IPlatformConfiguration.ISiteEntry[] newSiteEntries = platformConfig.getConfiguredSites();

		// Either it is a new site or it already exists, or it is deleted
		// new site only exist in platformConfig
		List modified = new ArrayList();
		List toInstall = new ArrayList();
		IConfiguredSite[] oldConfiguredSites = new IConfiguredSite[0];

		// sites from the current configuration
		if (getCurrentConfiguration() != null)
			oldConfiguredSites = getCurrentConfiguration().getConfiguredSites();

		// check if sites from the platform are new or modified
		for (int siteIndex = 0; siteIndex < newSiteEntries.length; siteIndex++) {

			URL resolvedURL = resolveSiteEntry(newSiteEntries[siteIndex]);

			boolean found = false;
			for (int index = 0; index < oldConfiguredSites.length && !found; index++) {
				if (oldConfiguredSites[index].getSite().getURL().equals(resolvedURL)) {
					found = true;
					((ConfiguredSite) oldConfiguredSites[index]).setPreviousPluginPath(newSiteEntries[siteIndex].getSitePolicy().getList());
					modified.add(oldConfiguredSites[index]);
				}
			}

			// new site not found, create it
			if (!found) {
				ISite site = SiteManager.getSite(resolvedURL);
				//site policy
				IPlatformConfiguration.ISitePolicy sitePolicy = newSiteEntries[siteIndex].getSitePolicy();
				ConfiguredSite configSite = (ConfiguredSite) new BaseSiteLocalFactory().createConfigurationSiteModel((SiteModel) site, sitePolicy.getType());
				configSite.setPlatformURLString(newSiteEntries[siteIndex].getURL().toExternalForm());
				configSite.setPreviousPluginPath(newSiteEntries[siteIndex].getSitePolicy().getList());

				//the site may not be read-write
				configSite.isUpdateable(newSiteEntries[siteIndex].isUpdateable());

				toInstall.add(configSite);
			}
		}

		// create new InstallConfiguration
		IInstallConfiguration newDefaultConfiguration = cloneConfigurationSite(null, null, null);

		// check modified config site
		// and add them back
		Iterator checkIter = modified.iterator();
		while (checkIter.hasNext()) {
			IConfiguredSite modifiedOldConfigSite = (IConfiguredSite) checkIter.next();
			newDefaultConfiguration.addConfiguredSite(reconcile(modifiedOldConfigSite));
		}

		// add new sites
		Iterator addIter = toInstall.iterator();
		while (addIter.hasNext()) {
			IConfiguredSite newFoundSite = (IConfiguredSite) addIter.next();
			newDefaultConfiguration.addConfiguredSite(newFoundSite);
		}

		// add the configuration as the currentConfig
		this.addConfiguration(newDefaultConfiguration);

	}

	/**
	 * 
	 */
	private URL resolveSiteEntry(IPlatformConfiguration.ISiteEntry newSiteEntry) throws CoreException {
		URL resolvedURL = null;
		try {
			resolvedURL = Platform.resolve(newSiteEntry.getURL());
		} catch (IOException e) {
			throw Utilities.newCoreException(Policy.bind("SiteLocal.UnableToResolve",newSiteEntry.getURL().toExternalForm()), e); //$NON-NLS-1$
		}
		return resolvedURL;
	}

	/**
	 * Compare the old version of the configuration Site with the new one.
	 * 
	 */
	private IConfiguredSite reconcile(IConfiguredSite oldConfiguredSiteToReconcile) throws CoreException {

		// create a copy of the ConfigSite without any feature
		// this is not a clone
		SiteModel siteModel = (SiteModel) oldConfiguredSiteToReconcile.getSite();
		ConfiguredSite cSiteToReconcile = (ConfiguredSite) oldConfiguredSiteToReconcile;
		int policy = cSiteToReconcile.getConfigurationPolicy().getPolicy();
		ConfiguredSiteModel newSiteModel = new BaseSiteLocalFactory().createConfigurationSiteModel(siteModel, policy);

		// copy values
		newSiteModel.isUpdateable(cSiteToReconcile.isUpdateable());
		newSiteModel.setPlatformURLString(((ConfiguredSiteModel) oldConfiguredSiteToReconcile).getPlatformURLString());
		newSiteModel.setPreviousPluginPath(cSiteToReconcile.getPreviousPluginPath());

		// check the Features that are still here
		List toCheck = new ArrayList();
		IFeatureReference[] oldConfiguredFeatures = oldConfiguredSiteToReconcile.getSite().getFeatureReferences();
		FeatureReferenceModel[] newFoundFeatures = newSiteModel.getSiteModel().getFeatureReferenceModels();

		for (int i = 0; i < newFoundFeatures.length; i++) {
			boolean newFeatureFound = false;
			for (int j = 0; j < oldConfiguredFeatures.length; j++) {
				if (oldConfiguredFeatures[j] != null && oldConfiguredFeatures[j].equals(newFoundFeatures[i])) {
					toCheck.add(oldConfiguredFeatures[j]);
					newFeatureFound = true;
				}
			}

			// new fature to add as configured
			if (!newFeatureFound) {
				(newSiteModel.getConfigurationPolicyModel()).addConfiguredFeatureReference((FeatureReferenceModel) newFoundFeatures[i]);
			}
		}

		// if a feature has been found in new and old state
		// use old state
		Iterator featureIter = toCheck.iterator();
		ConfiguredSite oldConfiguredSite = (ConfiguredSite) oldConfiguredSiteToReconcile;
		ConfigurationPolicy oldConfigurationPolicy = oldConfiguredSite.getConfigurationPolicy();
		while (featureIter.hasNext()) {
			IFeatureReference element = (IFeatureReference) featureIter.next();
			FeatureReferenceModel oldFeatureRef = (FeatureReferenceModel) element;
			if (oldConfigurationPolicy.isConfigured(oldFeatureRef)) {
				(newSiteModel.getConfigurationPolicyModel()).addConfiguredFeatureReference(oldFeatureRef);
			} else {
				(newSiteModel.getConfigurationPolicyModel()).addUnconfiguredFeatureReference(oldFeatureRef);
			}
		}

		return (IConfiguredSite) newSiteModel;
	}

	/**
	 * Validate we have only one configured feature across the different sites
	 * even if we found multiples
	 * 
	 * If we find 2 features, the one with a higher version is configured
	 * If they have teh same version, the first feature is configured
	 */
	private void checkConfiguredFeatures(IInstallConfiguration newDefaultConfiguration) throws CoreException {

		IConfiguredSite[] configuredSites = newDefaultConfiguration.getConfiguredSites();
		if (configuredSites.length > 1) {
			for (int indexConfiguredSites = 0; indexConfiguredSites < configuredSites.length - 1; indexConfiguredSites++) {
				IFeatureReference[] configuredFeatures = configuredSites[indexConfiguredSites].getConfiguredFeatures();
				for (int indexConfiguredFeatures = 0; indexConfiguredFeatures < configuredFeatures.length; indexConfiguredFeatures++) {
					IFeatureReference featureToCompare = configuredFeatures[indexConfiguredFeatures];
					for (int i = indexConfiguredSites + 1; i < configuredSites.length; i++) {
						IFeatureReference[] possibleFeatureReference = configuredSites[i].getConfiguredFeatures();
						for (int j = 0; j < possibleFeatureReference.length; j++) {
							int result = compare(featureToCompare, possibleFeatureReference[j]);
							if (result != 0) {
								if (result == 1) {
									configuredSites[i].unconfigure(possibleFeatureReference[j].getFeature(), null);
								};
								if (result == 2) {
									configuredSites[indexConfiguredSites].unconfigure(featureToCompare.getFeature(), null);
									// do not break, we can continue, even if the feature is unconfigured
									// if we find another feature in another site, we may unconfigure it.
									// but it would have been unconfigured anyway because we confured another version of the same feature
									// so if teh version we find is lower than our version, by transition, it is lower then the feature that unconfigured us
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * compare 2 feature references
	 * returns 0 if the feature are different
	 * returns 1 if the version of feature 1 is > version of feature 2
	 * returns 2 if opposite
	 */
	private int compare(IFeatureReference featureRef1, IFeatureReference featureRef2) throws CoreException {
		if (featureRef1 == null)
			return 0;

		IFeature feature1 = featureRef1.getFeature();
		IFeature feature2 = featureRef2.getFeature();

		if (feature1.equals(feature2)) {
			Version version1 = feature1.getVersionedIdentifier().getVersion();
			Version version2 = feature2.getVersionedIdentifier().getVersion();
			if (version1 != null) {
				int result = (version1.compare(version2));
				if (result == -1) {
					return 2;
				} else {
					return 1;
				}
			} else {
				return 2;
			}
		}
		return 0;
	};

	/**
	 * Returns all the plugins currently configured in the pluginRegistry
	 */
	private static IPluginEntry[] getAllRunningPlugin() throws CoreException {
		if (allRunningPluginEntry == null) {
			// get all the running plugins
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
	 * Add the list of plugins the platform found for each site. This list will be preserved in 
	 * a transient way. 
	 * 
	 * We do not lose explicitly set plugins found in platform.cfg.
	 */
	private void preserveRuntimePluginPath() throws CoreException {

		IPlatformConfiguration platformConfig = BootLoader.getCurrentPlatformConfiguration();
		IPlatformConfiguration.ISiteEntry[] siteEntries = platformConfig.getConfiguredSites();

		// sites from the current configuration
		IConfiguredSite[] configured = new IConfiguredSite[0];
		if (getCurrentConfiguration() != null)
			configured = getCurrentConfiguration().getConfiguredSites();

		// sites from the platform			
		for (int siteIndex = 0; siteIndex < siteEntries.length; siteIndex++) {
			URL resolvedURL =resolveSiteEntry(siteEntries[siteIndex]);
			
			boolean found = false;
			for (int index = 0; index < configured.length && !found; index++) {

				// the array may have hole as we set found site to null
				if (configured[index] != null) {
					if (configured[index].getSite().getURL().equals(resolvedURL)) {
						found = true;
						((ConfiguredSite) configured[index]).setPreviousPluginPath(siteEntries[siteIndex].getSitePolicy().getList());
						configured[index] = null;
					}
				}
			}
		}

	}
	
	/*
	 * Get update state location relative to platform configuration
	 */
	 private static URL getUpdateStateLocation(IPlatformConfiguration config) throws IOException {
	 	// Create a directory location for update state files. This
	 	// directory name is constructed by adding a well-known suffix
		// to the name of the corresponding platform  configuration. This
		// way, we can have multiple platform configuration files in
		// the same directory without ending up with update state conflicts.
		// For example: platform configuration file:C:/platform.cfg results
		// in update state location file:C:/platform.cfg.update/
	 	URL configLocation = Platform.resolve(config.getConfigurationLocation());
		String temp = configLocation.toExternalForm();
		temp += UPDATE_STATE_SUFFIX + "/";
		URL updateLocation = new URL(temp);
		if (updateLocation.getProtocol().equals("file")) {
			// ensure path exists. Handle transient configurations
			ArrayList list = new ArrayList();
			File path = new File(updateLocation.getFile());
			while (path != null) { // walk up to first dir that exists
				if (!path.exists()) {
					list.add(path);
					path = path.getParentFile();
				} else
					path = null;
			}
			for (int i=list.size()-1; i>=0; i--) { // walk down to create missing dirs
				path = (File) list.get(i);
				path.mkdir();
				if (config.isTransient())
					path.deleteOnExit();
			}	
		}
		return updateLocation;
	 }

	/**
	 * Gets the isTransient.
	 * @return Returns a boolean
	 */
	public boolean isTransient() {
		return isTransient;
	}

	/**
	 * Sets the isTransient.
	 * @param isTransient The isTransient to set
	 */
	private void isTransient(boolean isTransient) {
		this.isTransient = isTransient;
	}

}