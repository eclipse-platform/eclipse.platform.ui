package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved. 
 */
import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.FeatureReferenceModel;
import org.eclipse.update.core.model.SiteModel;
import org.eclipse.update.internal.model.*;
import org.xml.sax.SAXException;

/**
 * This class manages the configurations.
 */

public class SiteLocal
	extends SiteLocalModel
	implements ILocalSite, IWritable {

	private static IPluginEntry[] allRunningPluginEntry;
	private ListenersList listeners = new ListenersList();
	private SiteReconciler reconciler;
	private boolean isTransient = false;
	private List /* of IPluginEntry */
	allConfiguredPlugins;

	private static final String UPDATE_STATE_SUFFIX = ".metadata";

	/**
	 * initialize the configurations from the persistent model.
	 * The configurations are per user, so we save the data in the 
	 * user path, not the .metadata of any workspace, so the data
	 * is shared between the workspaces.
	 */
	public static ILocalSite getLocalSite() throws CoreException {

		SiteLocal localSite = new SiteLocal();

		// obtain platform configuration
		IPlatformConfiguration currentPlatformConfiguration =
			BootLoader.getCurrentPlatformConfiguration();
		localSite.isTransient(currentPlatformConfiguration.isTransient());

		try {

			// obtain LocalSite.xml location
			URL location;
			try {
				location = getUpdateStateLocation(currentPlatformConfiguration);
			} catch (IOException exception) {
				throw Utilities
					.newCoreException(Policy.bind(Policy.bind("SiteLocal.UnableToRetrieveRWArea")),
				//$NON-NLS-1$
				exception);
			}

			URL configXML = UpdateManagerUtils.getURL(location, SITE_LOCAL_FILE, null);
			localSite.setLocationURLString(configXML.toExternalForm());
			localSite.resolve(configXML, null);

			parseLocalSiteFile(localSite, currentPlatformConfiguration, configXML);

			// check if we have to reconcile, if the timestamp has changed
			long bootStamp = currentPlatformConfiguration.getChangeStamp();
			if (localSite.getStamp() != bootStamp) {
				if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
					UpdateManagerPlugin.getPlugin().debug(
						"Reconcile platform stamp:"
							+ bootStamp
							+ " is different from LocalSite stamp:"
							+ localSite.getStamp());
					//$NON-NLS-1$ //$NON-NLS-2$
				}

				localSite.reconcile();

			} else {
				// no reconciliation, preserve the list of plugins from the platform anyway
				localSite.preserveRuntimePluginPath();
			}
		} catch (MalformedURLException exception) {
			throw Utilities.newCoreException(
				Policy.bind(
					"SiteLocal.UnableToCreateURLFor",
					localSite.getLocationURLString() + " & " + SITE_LOCAL_FILE),
				exception);
			//$NON-NLS-1$ //$NON-NLS-2$
		}

		return localSite;
	}

	/**
	 * Create the localSite object either by parsing, recovering from the file system, or reconciling with the platform configuration
	 */
	private static void parseLocalSiteFile(
		SiteLocal localSite,
		IPlatformConfiguration currentPlatformConfiguration,
		URL configXML)
		throws MalformedURLException, CoreException {

		//attempt to parse the LocalSite.xml	
		URL resolvedURL = URLEncoder.encode(configXML);
		try {
			new SiteLocalParser(resolvedURL.openStream(), localSite);
		} catch (FileNotFoundException exception) {
			// file SITE_LOCAL_FILE doesn't exist, ok, log it 
			// and reconcile with platform configuration
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug(
					localSite.getLocationURLString()
						+ " does not exist, there is no previous state or install history we can recover from, we shall use default from platform configuration.");
				//$NON-NLS-1$
			}

			localSite.reconcile();

		} catch (SAXException exception) {
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				Utilities.logException(
					Policy.bind(
						"SiteLocal.ErrorParsingSavedState",
						localSite.getLocationURLString()),
					exception);
				//$NON-NLS-1$
			}
			recoverSiteLocal(resolvedURL, localSite);

		} catch (IOException exception) {
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				Utilities.logException(
					Policy.bind("SiteLocal.UnableToAccessFile", configXML.toExternalForm()),
					exception);
				//$NON-NLS-1$
			}

			recoverSiteLocal(resolvedURL, localSite);
		}
	}

	/**
	 * 
	 */
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
					if (UpdateManagerPlugin.DEBUG
						&& UpdateManagerPlugin.DEBUG_SHOW_CONFIGURATION) {
						UpdateManagerPlugin.getPlugin().debug(
							"Removed configuration :" + removedConfig.getLabel());
						//$NON-NLS-1$
					}

					// notify listeners
					Object[] siteLocalListeners = listeners.getListeners();
					for (int i = 0; i < siteLocalListeners.length; i++) {
						(
							(ILocalSiteChangedListener) siteLocalListeners[i]).installConfigurationRemoved(
							(IInstallConfiguration) removedConfig);
					}

					//remove files
					URL url = removedConfig.getURL();
					UpdateManagerUtils.removeFromFileSystem(new File(url.getFile()));
				}

			}

			// set configuration as current		
			if (getCurrentConfigurationModel() != null)
				getCurrentConfigurationModel().setCurrent(false);
			if (config instanceof InstallConfiguration)
				 ((InstallConfiguration) config).setCurrent(true);

			setCurrentConfigurationModel((InstallConfigurationModel) config);
			((InstallConfigurationModel) config).markReadOnly();

			// notify listeners
			Object[] siteLocalListeners = listeners.getListeners();
			for (int i = 0; i < siteLocalListeners.length; i++) {
				(
					(
						ILocalSiteChangedListener) siteLocalListeners[i])
							.currentInstallConfigurationChanged(
					config);
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
		if ("file".equalsIgnoreCase(getLocationURL().getProtocol())) { //$NON-NLS-1$
			File file = null;
			try {
				URL newURL = UpdateManagerUtils.getURL(getLocationURL(), SITE_LOCAL_FILE, null);
				file = new File(newURL.getFile());
				if (isTransient())
					file.deleteOnExit();
				Writer writer = new Writer(file, "UTF8");
				writer.write(this);
			} catch (FileNotFoundException e) {
				throw Utilities.newCoreException(
					Policy.bind("SiteLocal.UnableToSaveStateIn", file.getAbsolutePath()),
					e);
				//$NON-NLS-1$
			} catch (UnsupportedEncodingException e) {
				throw Utilities.newCoreException(
					Policy.bind("SiteLocal.UnableToEncodeConfiguration", file.getAbsolutePath()),
					e);
				//$NON-NLS-1$
			} catch (MalformedURLException e) {
				throw Utilities.newCoreException(
					Policy.bind("SiteLocal.UnableToCreateURLFor")
						+ getLocationURL().toExternalForm()
						+ " : "
						+ SITE_LOCAL_FILE,
					e);
				//$NON-NLS-2$ //$NON-NLS-1$
			}
		}
	}
	/*
	 * @see IWritable#write(int, PrintWriter)
	 */
	public void write(int indent, PrintWriter w) {

		// force the recalculation to avoid reconciliation
		IPlatformConfiguration platformConfig =
			BootLoader.getCurrentPlatformConfiguration();
		platformConfig.refresh();
		long changeStamp = platformConfig.getChangeStamp();
		this.setStamp(changeStamp);

		String gap = ""; //$NON-NLS-1$
		for (int i = 0; i < indent; i++)
			gap += " "; //$NON-NLS-1$
		String increment = ""; //$NON-NLS-1$
		for (int i = 0; i < IWritable.INDENT; i++)
			increment += " "; //$NON-NLS-1$

		// SITE 
		w.print(gap + "<" + SiteLocalParser.SITE + " "); //$NON-NLS-1$ //$NON-NLS-2$
		if (getLabel() != null) {
			w.print(gap + "label=\"" + Writer.xmlSafe(getLabel()) + "\" ");
			//$NON-NLS-1$ //$NON-NLS-2$
		}
		w.print(gap + "history=\"" + getMaximumHistoryCount() + "\" ");
		//$NON-NLS-1$ //$NON-NLS-2$
		w.print(gap + "stamp=\"" + changeStamp + "\" >"); //$NON-NLS-1$ //$NON-NLS-2$
		w.println(""); //$NON-NLS-1$

		// CONFIGURATIONS
		// the last one is the current configuration
		InstallConfigurationModel[] configurations = getConfigurationHistoryModel();
		for (int index = 0; index < configurations.length; index++) {
			InstallConfigurationModel element = configurations[index];
			if (!element.isCurrent()) {
				writeConfig(gap + increment, w, element);
			}
		}
		// write current configuration last
		writeConfig(
			gap + increment,
			w,
			(InstallConfigurationModel) getCurrentConfiguration());
		w.println(""); //$NON-NLS-1$

		// PRESERVED CONFIGURATIONS
		if (getPreservedConfigurations() != null
			&& getPreservedConfigurations().length != 0) {
			// write preserved configurations
			w.println(
				gap + increment + "<" + SiteLocalParser.PRESERVED_CONFIGURATIONS + ">");
			//$NON-NLS-1$ //$NON-NLS-2$

			InstallConfigurationModel[] preservedConfig = getPreservedConfigurationsModel();
			for (int index = 0; index < preservedConfig.length; index++) {
				InstallConfigurationModel element = preservedConfig[index];
				writeConfig(gap + increment + increment, w, element);
			}
			w.println(
				gap + increment + "</" + SiteLocalParser.PRESERVED_CONFIGURATIONS + ">");
			//$NON-NLS-1$ //$NON-NLS-2$
		}
		// end
		w.println(gap + "</" + SiteLocalParser.SITE + ">"); //$NON-NLS-1$ //$NON-NLS-2$

		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
			UpdateManagerPlugin.getPlugin().debug("Saved change stamp:" + changeStamp);
			//$NON-NLS-1$
		}

	}

	/**
	 * @since 2.0
	 */
	private void writeConfig(
		String gap,
		PrintWriter w,
		InstallConfigurationModel config) {
		w.print(gap + "<" + SiteLocalParser.CONFIG + " "); //$NON-NLS-1$ //$NON-NLS-2$
		String URLInfoString =
			UpdateManagerUtils.getURLAsString(getLocationURL(), config.getURL());
		w.print("url=\"" + Writer.xmlSafe(URLInfoString) + "\" ");
		//$NON-NLS-1$ //$NON-NLS-2$

		if (config.getLabel() != null) {
			w.print("label=\"" + Writer.xmlSafe(config.getLabel()) + "\"");
			//$NON-NLS-1$ //$NON-NLS-2$
		}

		w.println("/>"); //$NON-NLS-1$
	}

	/**
	 * @since 2.0
	 */
	/*package*/
	IInstallConfiguration cloneConfigurationSite(
		IInstallConfiguration installConfig,
		URL newFile,
		String name)
		throws CoreException {

		// save previous current configuration
		if (getCurrentConfiguration() != null)
			((InstallConfiguration) getCurrentConfiguration()).saveConfigurationFile(
				isTransient());

		InstallConfiguration result = null;
		Date currentDate = new Date();

		String newFileName =
			UpdateManagerUtils.getLocalRandomIdentifier(DEFAULT_CONFIG_FILE, currentDate);
		try {
			if (newFile == null)
				newFile = UpdateManagerUtils.getURL(getLocationURL(), newFileName, null);
			// pass the date onto the name
			if (name == null)
				name = Utilities.format(currentDate);
			result = new InstallConfiguration(installConfig, newFile, name);
			// set teh same date in the installConfig
			result.setCreationDate(currentDate);
		} catch (MalformedURLException e) {
			throw Utilities.newCoreException(
				Policy.bind("SiteLocal.UnableToCreateURLFor") + newFileName,
				e);
			//$NON-NLS-1$
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
	public void revertTo(
		IInstallConfiguration configuration,
		IProgressMonitor monitor,
		IProblemHandler handler)
		throws CoreException {

		// create the activity 
		//Start UOW ?
		ConfigurationActivity activity =
			new ConfigurationActivity(IActivity.ACTION_REVERT);
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
			((InstallConfiguration) newConfiguration).revertTo(
				configuration,
				monitor,
				handler);

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
				((InstallConfiguration) newConfiguration).addActivityModel(
					(ConfigurationActivityModel) activity);
		}

	}

	/**
	 * @since 2.0
	 */
	public void addToPreservedConfigurations(IInstallConfiguration configuration)
		throws CoreException {
		if (configuration != null) {

			// create new configuration based on the one to preserve
			InstallConfiguration newConfiguration = null;
			String newFileName =
				UpdateManagerUtils.getLocalRandomIdentifier(
					DEFAULT_PRESERVED_CONFIG_FILE,
					new Date());
			try {
				URL newFile = UpdateManagerUtils.getURL(getLocationURL(), newFileName, null);
				// pass the date onto teh name
				Date currentDate = configuration.getCreationDate();
				String name = configuration.getLabel();
				newConfiguration = new InstallConfiguration(configuration, newFile, name);
				// set teh same date in the installConfig
				newConfiguration.setCreationDate(currentDate);

			} catch (MalformedURLException e) {
				throw Utilities.newCoreException(
					Policy.bind("SiteLocal.UnableToCreateURLFor") + newFileName,
					e);
				//$NON-NLS-1$
			}

			// activity
			ConfigurationActivity activity =
				new ConfigurationActivity(IActivity.ACTION_ADD_PRESERVED);
			activity.setLabel(configuration.getLabel());
			activity.setDate(new Date());
			activity.setStatus(IActivity.STATUS_OK);
			((InstallConfiguration) newConfiguration).addActivityModel(activity);
			((InstallConfiguration) newConfiguration).saveConfigurationFile(isTransient());

			// add to the list			
			addPreservedInstallConfigurationModel(
				(InstallConfigurationModel) newConfiguration);
		}
	}

	/*
	 * @see ILocalSite#getPreservedConfigurationFor(IInstallConfiguration)
	 */
	public IInstallConfiguration findPreservedConfigurationFor(IInstallConfiguration configuration) {

		// based on time stamp for now
		InstallConfigurationModel preservedConfig = null;
		if (configuration != null) {
			InstallConfigurationModel[] preservedConfigurations =
				getPreservedConfigurationsModel();
			if (preservedConfigurations != null) {
				for (int indexPreserved = 0;
					indexPreserved < preservedConfigurations.length;
					indexPreserved++) {
					if (configuration
						.getCreationDate()
						.equals(preservedConfigurations[indexPreserved].getCreationDate())) {
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
		if (
			removePreservedConfigurationModel((InstallConfigurationModel) configuration))
			 ((InstallConfiguration) configuration).remove();
	}

	/**
	 * Add the list of plugins the platform found for each site. This list will be preserved in 
	 * a transient way. 
	 * 
	 * We do not lose explicitly set plugins found in platform.cfg.
	 */
	private void preserveRuntimePluginPath() throws CoreException {

		IPlatformConfiguration platformConfig =
			BootLoader.getCurrentPlatformConfiguration();
		IPlatformConfiguration.ISiteEntry[] siteEntries =
			platformConfig.getConfiguredSites();

		// sites from the current configuration
		IConfiguredSite[] configured = new IConfiguredSite[0];
		if (this.getCurrentConfiguration() != null)
			configured = this.getCurrentConfiguration().getConfiguredSites();

		// sites from the platform			
		for (int siteIndex = 0; siteIndex < siteEntries.length; siteIndex++) {
			URL resolvedURL = getReconciler().resolveSiteEntry(siteEntries[siteIndex]);

			boolean found = false;
			for (int index = 0; index < configured.length && !found; index++) {

				// the array may have hole as we set found site to null
				if (configured[index] != null) {
					if (configured[index].getSite().getURL().equals(resolvedURL)) {
						found = true;
						String[] listOfPlugins = siteEntries[siteIndex].getSitePolicy().getList();
						((ConfiguredSite) configured[index]).setPreviousPluginPath(listOfPlugins);
						configured[index] = null;
					}
				}
			}
		}

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
		getReconciler().reconcile();
	}

	/*
	 * 
	 */
	private SiteReconciler getReconciler() {
		if (reconciler == null)
			reconciler = new SiteReconciler(this);
		return reconciler;
	}
	/*
	 * Get update state location relative to platform configuration
	 */
	private static URL getUpdateStateLocation(IPlatformConfiguration config)
		throws IOException {
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
		if ("file".equalsIgnoreCase(updateLocation.getProtocol())) {
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
			for (int i = list.size() - 1;
				i >= 0;
				i--) { // walk down to create missing dirs
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

	/**
	 * if we are unable to parse the SiteLoca we will attempt to parse the file system
	 */
	private static void recoverSiteLocal(URL url, SiteLocal site)
		throws CoreException, MalformedURLException {

		if (url == null)
			throw Utilities.newCoreException(Policy.bind("SiteLocal.SiteUrlIsNull"),
			//$NON-NLS-1$
			null);

		// parse site information
		site.setLabel(url.toExternalForm());

		//stamp
		long stamp = 0L;
		site.setStamp(stamp);

		// retrieve XML files
		File localXml = new File(url.getFile());
		File dir = localXml.getParentFile();
		File[] configFiles = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.startsWith("Config") && name.endsWith("xml"));
			}
		});
		File[] preservedFiles = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.startsWith("PreservedConfig") && name.endsWith("xml"));
			}
		});

		// history
		int history = 0;
		if (configFiles != null && configFiles.length > 0) {
			history = configFiles.length;
		} else {
			history = SiteLocalModel.DEFAULT_HISTORY;
		}
		site.setMaximumHistoryCount(history);

		// parse configuration information

		for (int i = 0; i < configFiles.length; i++) {
			URL configURL = configFiles[i].toURL();
			InstallConfigurationModel config =
				new BaseSiteLocalFactory().createInstallConfigurationModel();
			config.setLocationURLString(configURL.toExternalForm());
			config.resolve(configURL, getResourceBundle(url));
			try {
				config.initialize();
				config.setLabel(Utilities.format(config.getCreationDate()));
				site.addConfigurationModel(config);
			} catch (CoreException e) {
				UpdateManagerPlugin.getPlugin().getLog().log(e.getStatus());
			}

		}

		// parse preserved configuration information
		for (int i = 0; i < preservedFiles.length; i++) {
			URL configURL = configFiles[i].toURL();
			InstallConfigurationModel config =
				new BaseSiteLocalFactory().createInstallConfigurationModel();
			config.setLocationURLString(configURL.toExternalForm());
			config.resolve(configURL, getResourceBundle(url));
			try {
				config.initialize();
				config.setLabel(Utilities.format(config.getCreationDate()));
				site.addPreservedInstallConfigurationModel(config);
			} catch (CoreException e) {
				UpdateManagerPlugin.getPlugin().getLog().log(e.getStatus());
			}
		}
	}

	/**
	 * return the appropriate resource bundle for this sitelocal
	 */
	private static ResourceBundle getResourceBundle(URL url) throws CoreException {
		ResourceBundle bundle = null;
		try {
			ClassLoader l = new URLClassLoader(new URL[] { url }, null);
			bundle =
				ResourceBundle.getBundle(
					SiteLocalModel.SITE_LOCAL_FILE,
					Locale.getDefault(),
					l);
		} catch (MissingResourceException e) {
			//ok, there is no bundle, keep it as null
			//DEBUG:
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
				UpdateManagerPlugin.getPlugin().debug(
					e.getLocalizedMessage() + ":" + url.toExternalForm());
				//$NON-NLS-1$
			}
		}
		return bundle;
	}

	/**
	 * compare two feature references
	 * returns 0 if the feature are different
	 * returns 1 if the version of feature 1 is greater than version of feature 2
	 * returns 2 if opposite
	 */
	private int compare(
		IFeatureReference featureRef1,
		IFeatureReference featureRef2)
		throws CoreException {
		if (featureRef1 == null)
			return 0;

		IFeature feature1 = featureRef1.getFeature();
		IFeature feature2 = featureRef2.getFeature();

		if (feature1 == null || feature2 == null) {
			return 0;
		}

		VersionedIdentifier id1 = feature1.getVersionedIdentifier();
		VersionedIdentifier id2 = feature2.getVersionedIdentifier();

		if (id1 == null || id2 == null) {
			return 0;
		}

		if (id1.getIdentifier() != null
			&& id1.getIdentifier().equals(id2.getIdentifier())) {
			PluginVersionIdentifier version1 = id1.getVersion();
			PluginVersionIdentifier version2 = id2.getVersion();
			if (version1 != null) {
				boolean greaterOrEqual = (version1.isGreaterOrEqualTo(version2));
				if (greaterOrEqual) {
					return 1;
				} else {
					return 2;
				}
			} else {
				return 2;
			}
		}
		return 0;
	}

	/*
	 *  check if the Plugins of the feature are on the plugin path
	 *  If all the plugins are on the plugin path, and the version match and there is no other version -> HAPPY
	 *  If all the plugins are on the plugin path, and the version match and there is other version -> AMBIGUOUS
	 *  If some of the plugins are on the plugin path, but not all -> UNHAPPY
	 * 	Check on all ConfiguredSites
	 */
	public int getStatus(IFeature feature) {

		// check if broken first
		IConfiguredSite[] configuredSites =
			getCurrentConfiguration().getConfiguredSites();
		ISite featureSite = feature.getSite();
		if (featureSite == null) {
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_CONFIGURATION)
				UpdateManagerPlugin.getPlugin().debug(
					"Cannot determine status of feature:" + feature.getLabel() + ". Site is NULL.");
			return IFeature.STATUS_AMBIGUOUS;
		}

		for (int i = 0; i < configuredSites.length; i++) {
			if (featureSite.equals(configuredSites[i].getSite())) {
				if (configuredSites[i].isBroken(feature)) {
					if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_CONFIGURATION)
						UpdateManagerPlugin.getPlugin().debug(
							"Feature broken:"
								+ feature.getLabel()
								+ ".Site:"
								+ configuredSites[i].toString());
					return IFeature.STATUS_UNHAPPY;
				}
			}
		}

		// not broken, get all plugins of feature
		// and all plugins of configured features of all configured sites
		IPluginEntry[] featuresEntries = feature.getPluginEntries();
		IPluginEntry[] allPluginsEntries = getAllConfiguredPlugins();
		return status(featuresEntries, allPluginsEntries);
	}

	/*
	 * return all the configured plugins. Not recalculated when a install/remove/configure/unconfigure occurs
	 * as we expect the user to restart
	 */
	private IPluginEntry[] getAllConfiguredPlugins() {
		if (allConfiguredPlugins == null) {
			allConfiguredPlugins = new ArrayList();
			IConfiguredSite[] configuredSites;
			IFeatureReference[] configuredFeaturesRef;
			IFeature feature;
			configuredSites = getCurrentConfiguration().getConfiguredSites();
			for (int i = 0; i < configuredSites.length; i++) {
				configuredFeaturesRef = configuredSites[i].getConfiguredFeatures();
				for (int j = 0; j < configuredFeaturesRef.length; j++) {
					try {
						feature = configuredFeaturesRef[j].getFeature();
						allConfiguredPlugins.addAll(Arrays.asList(feature.getPluginEntries()));
					} catch (CoreException e) {
						// eat the exception and log it
						if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS)
							UpdateManagerPlugin.getPlugin().getLog().log(e.getStatus());
					}
				}
			}

		}

		if (allConfiguredPlugins == null || allConfiguredPlugins.isEmpty()) {
			return new IPluginEntry[0];
		}

		IPluginEntry[] result = new IPluginEntry[allConfiguredPlugins.size()];
		allConfiguredPlugins.toArray(result);
		return result;
	}

	/*
	 * compute the status based on getStatus() rules 
	 */
	private int status(IPluginEntry[] featurePlugins, IPluginEntry[] allPlugins) {
		VersionedIdentifier featureID;
		VersionedIdentifier compareID;
		
		// is Ambigous if we find a plugin from the feature
		// with a different version
		for (int i = 0; i < featurePlugins.length; i++) {
			featureID = featurePlugins[i].getVersionedIdentifier();
			for (int j = 0; j < allPlugins.length; j++) {
				compareID = allPlugins[j].getVersionedIdentifier();
				if (featureID.getIdentifier().equals(compareID.getIdentifier())) {
					if (!featureID.getVersion().equals(compareID.getVersion())) {
						// there is a plugin with a different version on the path
						if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS)
							UpdateManagerPlugin.getPlugin().debug(
								"Found 2 versions of the same plugin on the path:"
									+ featureID.toString()
									+ " & "
									+ compareID.toString());
						return IFeature.STATUS_AMBIGUOUS;
					}
				}
			}
		}

		// we return happy as we consider the isBroken verification has been done
		return IFeature.STATUS_HAPPY;
	}
}