package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
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
import org.eclipse.update.core.*;
import org.eclipse.update.core.IFeatureReference;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.core.model.SiteModel;
import org.eclipse.update.internal.model.*;

/**
 * Manages ConfiguredSites
 * 
 */

public class InstallConfiguration
	extends InstallConfigurationModel
	implements IInstallConfiguration, IWritable {

	private ListenersList listeners = new ListenersList();

	/*
	 * default constructor. 
	 */
	public InstallConfiguration() {
	}

	/*
	 * default constructor. 
	 */
	public InstallConfiguration(URL newLocation, String label)
		throws MalformedURLException {
		setLocationURLString(newLocation.toExternalForm());
		setLabel(label);
		setCurrent(false);
		resolve(newLocation, null);
	}

	/*
	 * copy constructor
	 */
	public InstallConfiguration(
		IInstallConfiguration config,
		URL newLocation,
		String label)
		throws MalformedURLException {
		setLocationURLString(newLocation.toExternalForm());
		setLabel(label);
		// do not copy list of listeners nor activities
		// make a copy of the siteConfiguration object
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

	/*
	 * Returns the list of configured sites or an empty array 
	 */
	public IConfiguredSite[] getConfiguredSites() {
		ConfiguredSiteModel[] result = getConfigurationSitesModel();
		if (result.length == 0)
			return new IConfiguredSite[0];
		else
			return (IConfiguredSite[]) result;
	}

	/*
	 * Returns the default site policy
	 */
	public int getDefaultPolicy() {
		return IPlatformConfiguration.ISitePolicy.USER_EXCLUDE;
	}

	/**
	 * Creates a Configuration Site and a new Site
	 * The policy is from <code> org.eclipse.core.boot.IPlatformConfiguration</code>
	 */
	public IConfiguredSite createConfiguredSite(File file) throws CoreException {

		ISite site = InternalSiteManager.createSite(file);

		//create a config site around the site
		// even if the site == null
		BaseSiteLocalFactory factory = new BaseSiteLocalFactory();
		ConfiguredSite configSite =
			(ConfiguredSite) factory.createConfigurationSiteModel(
				(SiteModel) site,
				getDefaultPolicy());
		configSite.isUpdatable(canWrite(file));

		if (site != null) {
			configSite.setPlatformURLString(site.getURL().toExternalForm());

			// obtain the list of plugins
			IPlatformConfiguration runtimeConfiguration =
				BootLoader.getCurrentPlatformConfiguration();
			ConfigurationPolicy configurationPolicy =
				(ConfigurationPolicy) configSite.getConfigurationPolicy();
			String[] pluginPath = configurationPolicy.getPluginPath(site, null);
			IPlatformConfiguration.ISitePolicy sitePolicy =
				runtimeConfiguration.createSitePolicy(
					configurationPolicy.getPolicy(),
					pluginPath);

			// change runtime					
			IPlatformConfiguration.ISiteEntry siteEntry =
				runtimeConfiguration.createSiteEntry(site.getURL(), sitePolicy);
			runtimeConfiguration.configureSite(siteEntry);
		}

		return configSite;
	}

	/*
	 * 
	 */
	public void addConfiguredSite(IConfiguredSite site) {
		if (!isCurrent() && isReadOnly())
			return;

		ConfigurationActivity activity =
			new ConfigurationActivity(IActivity.ACTION_SITE_INSTALL);
		activity.setLabel(site.getSite().getURL().toExternalForm());
		activity.setDate(new Date());
		ConfiguredSiteModel configSiteModel = (ConfiguredSiteModel) site;
		addConfigurationSiteModel(configSiteModel);
		configSiteModel.setInstallConfigurationModel(this);

		// notify listeners
		Object[] configurationListeners = listeners.getListeners();
		for (int i = 0; i < configurationListeners.length; i++) {
			IInstallConfigurationChangedListener listener =
				((IInstallConfigurationChangedListener) configurationListeners[i]);
			listener.installSiteAdded(site);
		}

		// everything done ok
		activity.setStatus(IActivity.STATUS_OK);
		this.addActivityModel((ConfigurationActivityModel) activity);
	}

	/*
	 * 
	 */
	public void removeConfiguredSite(IConfiguredSite site) {

		if (removeConfigurationSiteModel((ConfiguredSiteModel) site)) {

			// notify listeners
			Object[] configurationListeners = listeners.getListeners();
			for (int i = 0; i < configurationListeners.length; i++) {
				IInstallConfigurationChangedListener listener =
					((IInstallConfigurationChangedListener) configurationListeners[i]);
				listener.installSiteAdded(site);
			}

			//activity
			ConfigurationActivity activity =
				new ConfigurationActivity(IActivity.ACTION_SITE_REMOVE);
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
			Writer writer = new Writer(exportFile, "UTF8"); //$NON-NLS-1$
			writer.write(this);
		} catch (FileNotFoundException e) {
			throw Utilities.newCoreException(
				Policy.bind(
					"InstallConfiguration.UnableToSaveConfiguration",
					exportFile.getAbsolutePath()),
				e);
			//$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			throw Utilities.newCoreException(
				Policy.bind(
					"InstallConfiguration.UnableToEncodeConfiguration",
					exportFile.getAbsolutePath()),
				e);
			//$NON-NLS-1$
		}
	}

	/*
	 * Deletes the configuration from its URL/location
	 */
	public void remove() {
		// save the configuration
		if ("file".equalsIgnoreCase(getURL().getProtocol())) { //$NON-NLS-1$
			// the location points to a file
			File file = new File(getURL().getFile());
			UpdateManagerUtils.removeFromFileSystem(file);
		}
	}

	/*
	 * Saves the configuration into its URL/location
	 * and changes the platform configuration
	 */
	public void save(boolean isTransient) throws CoreException {

		// save the configuration.xml file
		saveConfigurationFile(isTransient);

		// Write info  into platform for the next runtime
		IPlatformConfiguration runtimeConfiguration =
			BootLoader.getCurrentPlatformConfiguration();
		ConfiguredSiteModel[] configurationSites = getConfigurationSitesModel();

		// clean configured Entries
		IPlatformConfiguration.IFeatureEntry[] configuredFeatureEntries =
			runtimeConfiguration.getConfiguredFeatureEntries();
		for (int i = 0; i < configuredFeatureEntries.length; i++) {
			runtimeConfiguration.unconfigureFeatureEntry(configuredFeatureEntries[i]);
		}

		// Write the plugin path
		for (int i = 0; i < configurationSites.length; i++) {
			ConfiguredSite cSite = ((ConfiguredSite) configurationSites[i]);
			ConfigurationPolicy configurationPolicy = cSite.getConfigurationPolicy();
			String[] pluginPath =
				configurationPolicy.getPluginPath(
					cSite.getSite(),
					cSite.getPreviousPluginPath());

			IPlatformConfiguration.ISitePolicy sitePolicy =
				runtimeConfiguration.createSitePolicy(
					configurationPolicy.getPolicy(),
					pluginPath);

			URL urlToCheck = null;
			try {
				urlToCheck = new URL(cSite.getPlatformURLString());
			} catch (MalformedURLException e) {
				throw Utilities.newCoreException(
					Policy.bind(
						"InstallConfiguration.UnableToCreateURL",
						cSite.getPlatformURLString()),
					e);
				//$NON-NLS-1$
			} catch (ClassCastException e) {
				throw Utilities.newCoreException(
					Policy.bind("InstallConfiguration.UnableToCast"),
					e);
				//$NON-NLS-1$
			}

			// if the URL already exist, set the policy
			IPlatformConfiguration.ISiteEntry siteEntry =
				runtimeConfiguration.findConfiguredSite(urlToCheck);
			if (siteEntry != null) {
				siteEntry.setSitePolicy(sitePolicy);
			} else {
				throw Utilities.newCoreException(
					Policy.bind(
						"InstallConfiguration.UnableToFindConfiguredSite",
						urlToCheck.toExternalForm(),
						runtimeConfiguration.getConfigurationLocation().toExternalForm()),
					null);
				//$NON-NLS-1$
			}

			// IF primary feature URL or platform feature URL that we need to pass to runtime config
			// is part of platform:base:, write it as platform:base: URL
			IFeatureReference[] configuredFeaturesRef =
				configurationPolicy.getConfiguredFeatures();
			for (int j = 0; j < configuredFeaturesRef.length; j++) {
				IFeature feature = configuredFeaturesRef[j].getFeature();

				// write the primary features				
				if (feature.isPrimary()) {

					String id = feature.getVersionedIdentifier().getIdentifier();

					// get the URL of the plugin that corresponds to the feature (pluginid = featureid)					
					IPluginEntry[] entries = feature.getPluginEntries();
					URL url = null;
					for (int k = 0; k < entries.length; k++) {
						if (id.equalsIgnoreCase(entries[k].getVersionedIdentifier().getIdentifier())) {
							url = getRuntimeConfigurationURL(entries[k], cSite);
						}
					}

					String version = feature.getVersionedIdentifier().getVersion().toString();
					String application = feature.getApplication();
					IPlatformConfiguration.IFeatureEntry featureEntry =
						runtimeConfiguration.createFeatureEntry(id, version, application, url);
					runtimeConfiguration.configureFeatureEntry(featureEntry);
				}

				// write the platform features (features that contain special platform plugins)
				IPluginEntry[] platformPlugins =
					getPlatformPlugins(feature, runtimeConfiguration);
				for (int k = 0; k < platformPlugins.length; k++) {
					String id = platformPlugins[k].getVersionedIdentifier().getIdentifier();
					URL url = getRuntimeConfigurationURL(platformPlugins[k], cSite);
					runtimeConfiguration.setBootstrapPluginLocation(id, url);
				}
			}

		}

		try {
			runtimeConfiguration.save();
		} catch (IOException e) {
			throw Utilities.newCoreException(
				Policy.bind(
					"InstallConfiguration.UnableToSavePlatformConfiguration",
					runtimeConfiguration.getConfigurationLocation().toExternalForm()),
				e);
			//$NON-NLS-1$
		}
	}

	/*
	 * 
	 */
	public void saveConfigurationFile(boolean isTransient) throws CoreException {
		// save the configuration
		if ("file".equalsIgnoreCase(getURL().getProtocol())) { //$NON-NLS-1$
			// the location points to a file
			File file = new File(getURL().getFile());
			if (isTransient)
				file.deleteOnExit();
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

		//CONFIGURATION	
		w.print(gap + "<" + InstallConfigurationParser.CONFIGURATION + " ");
		//$NON-NLS-1$ //$NON-NLS-2$
		long time = (getCreationDate() != null) ? getCreationDate().getTime() : 0L;
		w.print("date=\"" + time + "\" "); //$NON-NLS-1$ //$NON-NLS-2$
		w.println(">"); //$NON-NLS-1$

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
		w.println(gap + "</" + InstallConfigurationParser.CONFIGURATION + ">");
		//$NON-NLS-1$ //$NON-NLS-2$
		w.println(""); //$NON-NLS-1$		
	}

	/*
	 * reverts this configuration to the match the new one
	 * remove any site that are in the current but not in the old state
	 * replace all the config sites of the current state with the old one
	 * for all the sites left in the current state, calculate the revert
	 * 
	 */
	public void revertTo(
		IInstallConfiguration configuration,
		IProgressMonitor monitor,
		IProblemHandler handler)
		throws CoreException, InterruptedException {

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
					// the Site existed before, calculate the delta between its current state and the
					// state we are reverting to
					((ConfiguredSite) oldSite).processDeltaWith(
						nowConfigSites[i],
						monitor,
						handler);
					nowConfigSites[i] = oldSite;
				} else {
					// the site didn't exist in the InstallConfiguration we are reverting to
					// unconfigure everything from this site so it is still present
					IFeatureReference[] featuresToUnconfigure =
						nowConfigSites[i].getSite().getFeatureReferences();
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
	 * @see IInstallConfiguration#getActivities()
	 */
	public IActivity[] getActivities() {
		if (getActivityModel().length == 0)
			return new IActivity[0];
		return (IActivity[]) getActivityModel();
	}

	/*
	 * Verify we can write on the file system
	 */
	private static boolean canWrite(File file) {
		if (!file.isDirectory() && file.getParentFile() != null) {
			file = file.getParentFile();
		}

		File tryFile = null;
		FileOutputStream out = null;
		try {
			tryFile = new File(file, "toDelete");
			out = new FileOutputStream(tryFile);
			out.write(0);
		} catch (IOException e) {
			return false;
		} finally {
			try {
				out.close();
				tryFile.delete();
			} catch (Exception e) {
			};
		}
		return true;
	}

	/*
	 * returns the list of platform plugins of the feature or an empty list 
	 * if the feature doesn't contain any platform plugins
	 */
	private IPluginEntry[] getPlatformPlugins(
		IFeature feature,
		IPlatformConfiguration runtimeConfiguration) {
		Map featurePlatformPlugins = new HashMap();
		String[] platformPluginID =
			runtimeConfiguration.getBootstrapPluginIdentifiers();
		IPluginEntry[] featurePlugins = feature.getPluginEntries();

		for (int i = 0; i < platformPluginID.length; i++) {
			for (int j = 0; j < featurePlugins.length; j++) {
				if (platformPluginID[i]
					.equals(featurePlugins[j].getVersionedIdentifier().getIdentifier())) {
					featurePlatformPlugins.put(platformPluginID[i], featurePlugins[j]);
				}
			}
		}

		Collection values = featurePlatformPlugins.values();
		if (values == null || values.size() == 0)
			return new IPluginEntry[0];

		IPluginEntry[] result = new IPluginEntry[values.size()];
		Iterator iter = values.iterator();
		int index = 0;
		while (iter.hasNext()) {
			result[index] = ((IPluginEntry) iter.next());
			index++;
		}
		return result;
	}

	/*
	 * returns the URL of the pluginEntry on the site
	 * resolve the URL to use platform: URL if needed
	 */
	private URL getRuntimeConfigurationURL(
		IPluginEntry entry,
		ConfiguredSite cSite)
		throws CoreException {

		String rootString = cSite.getPlatformURLString();
		String pluginPathID = getPathID(entry);
		try {
			ISiteContentProvider siteContentProvider =
				cSite.getSite().getSiteContentProvider();
			URL fullURL = siteContentProvider.getArchiveReference(pluginPathID);
			URL rootURL = Platform.resolve(new URL(rootString));
			String relativeString = UpdateManagerUtils.getURLAsString(rootURL, fullURL);
			URL result = new URL(new URL(rootString), relativeString);
			// DEBUG:
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_CONFIGURATION)
				UpdateManagerPlugin.getPlugin().debug("getRuntimeCOnfiguration Full URL:"+fullURL+" Relative:"+relativeString);
			return result;
		} catch (IOException e) {
			throw Utilities.newCoreException(
				Policy.bind(
					"InstallConfiguration.UnableToCreateURL",
					rootString),
				e);
			//$NON-NLS-1$
		}
	}

	/**
	 * Returns the path identifier for a plugin entry.
	 * <code>plugins/&lt;pluginId>_&lt;pluginVersion>.jar</code> 
	 * @return the path identifier
	 */
	private String getPathID(IPluginEntry entry) {
		return Site.DEFAULT_PLUGIN_PATH
			+ entry.getVersionedIdentifier().toString()
			+ FeatureContentProvider.JAR_EXTENSION;
	}

}