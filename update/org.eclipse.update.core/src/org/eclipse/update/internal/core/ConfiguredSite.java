package org.eclipse.update.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.PrintWriter;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.FeatureReferenceModel;
import org.eclipse.update.core.model.SiteModel;
import org.eclipse.update.internal.model.*;

/**
 * A Configured site manages the Configured and unconfigured features of a Site
 */
public class ConfiguredSite
	extends ConfiguredSiteModel
	implements IConfiguredSite, IWritable {

	// listeners	
	private ListenersList listeners = new ListenersList();

	/*
	 * Default Constructor
	 */
	public ConfiguredSite() {
	}

	/*
	 * Copy Constructor
	 * As of now, configSite can only be of type ConfiguredSite
	 */
	public ConfiguredSite(IConfiguredSite configSite) {
		ConfiguredSite cSite = (ConfiguredSite) configSite;
		setSiteModel((SiteModel) cSite.getSite());
		setConfigurationPolicyModel(
			new ConfigurationPolicy(cSite.getConfigurationPolicy()));
		isUpdatable(cSite.isUpdatable());
		setPreviousPluginPath(cSite.getPreviousPluginPath());
		setPlatformURLString(cSite.getPlatformURLString());
	}

	/*
	 *  Adds a listener
	 */
	public void addConfiguredSiteChangedListener(IConfiguredSiteChangedListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/*
	 * Removes a listener
	 */
	public void removeConfiguredSiteChangedListener(IConfiguredSiteChangedListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
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
		w.println(gap + "<" + InstallConfigurationParser.CONFIGURATION_SITE + " ");
		//$NON-NLS-1$ //$NON-NLS-2$
		w.println(
			gap + increment + "url=\"" + getSite().getURL().toExternalForm() + "\"");
		//$NON-NLS-1$ //$NON-NLS-2$
		w.println(gap + increment + "platformURL=\"" + getPlatformURLString() + "\"");
		//$NON-NLS-1$ //$NON-NLS-2$
		w.println(
			gap
				+ increment
				+ "policy=\""
				+ getConfigurationPolicyModel().getPolicy()
				+ "\" ");
		//$NON-NLS-1$ //$NON-NLS-2$
		String install = isUpdatable() ? "true" : "false";
		//$NON-NLS-1$ //$NON-NLS-2$
		w.print(gap + increment + "install=\"" + install + "\" ");
		//$NON-NLS-1$ //$NON-NLS-2$
		w.println(">"); //$NON-NLS-1$
		w.println(""); //$NON-NLS-1$
		// configured features ref
		IFeatureReference[] featuresReferences = getConfiguredFeatures();
		if (featuresReferences != null) {
			for (int index = 0; index < featuresReferences.length; index++) {
				IFeatureReference element = featuresReferences[index];
				w.print(gap + increment + "<" + InstallConfigurationParser.FEATURE + " ");
				//$NON-NLS-1$ //$NON-NLS-2$
				// configured = true
				w.print("configured = \"true\" "); //$NON-NLS-1$
				// feature URL
				String URLInfoString = null;
				if (element.getURL() != null) {
					ISite featureSite = element.getSite();
					URLInfoString =
						UpdateManagerUtils.getURLAsString(featureSite.getURL(), element.getURL());
					w.print("url=\"" + Writer.xmlSafe(URLInfoString) + "\" ");
					//$NON-NLS-1$ //$NON-NLS-2$
				}
				w.println("/>"); //$NON-NLS-1$
			}
		}
		// unconfigured features ref
		featuresReferences =
			((ConfigurationPolicy) getConfigurationPolicyModel()).getUnconfiguredFeatures();
		if (featuresReferences != null) {
			for (int index = 0; index < featuresReferences.length; index++) {
				IFeatureReference element = featuresReferences[index];
				w.print(gap + increment + "<" + InstallConfigurationParser.FEATURE + " ");
				//$NON-NLS-1$ //$NON-NLS-2$
				// configured = true
				w.print("configured = \"false\" "); //$NON-NLS-1$
				// feature URL
				String URLInfoString = null;
				if (element.getURL() != null) {
					ISite featureSite = element.getSite();
					URLInfoString =
						UpdateManagerUtils.getURLAsString(featureSite.getURL(), element.getURL());
					w.print("url=\"" + Writer.xmlSafe(URLInfoString) + "\" ");
					//$NON-NLS-1$ //$NON-NLS-2$
				}
				w.println("/>"); //$NON-NLS-1$
			}
		}
		// end
		w.println(gap + "</" + InstallConfigurationParser.CONFIGURATION_SITE + ">");
		//$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * @see IConfiguredSite#install(IFeature,IVerificationListener, IProgressMonitor)
	 */
	public IFeatureReference install(
		IFeature feature,
		IVerificationListener verificationListener,
		IProgressMonitor monitor)
		throws CoreException {

		// ConfigSite is read only
		if (!isUpdatable()) {
			String errorMessage =
				Policy.bind(
					"ConfiguredSite.NonInstallableSite",
					getSite().getURL().toExternalForm());
			//$NON-NLS-1$
			throw Utilities.newCoreException(errorMessage, null);
		}

		// feature is null
		if (feature == null) {
			String errorMessage = Policy.bind("ConfiguredSite.NullFeatureToInstall");
			//$NON-NLS-1$
			throw Utilities.newCoreException(errorMessage, null);
		}

		// feature reference to return
		IFeatureReference installedFeature;

		// create the Activity (INSTALL)
		ConfigurationActivity activity =
			new ConfigurationActivity(IActivity.ACTION_FEATURE_INSTALL);
		activity.setLabel(feature.getVersionedIdentifier().toString());
		activity.setDate(new Date());

		try {
			installedFeature = getSite().install(feature, verificationListener, monitor);

			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_INSTALL) {
				UpdateManagerPlugin.getPlugin().debug(
					"Sucessfully installed: " + installedFeature.getURL().toExternalForm());
			}

			// everything done ok
			activity.setStatus(IActivity.STATUS_OK);
			// notify listeners
			Object[] siteListeners = listeners.getListeners();
			for (int i = 0; i < siteListeners.length; i++) {
				((IConfiguredSiteChangedListener) siteListeners[i]).featureInstalled(
					installedFeature.getFeature());
			}
			// check if this is a primary feature
			// FIXME
		} catch (CoreException e) {
			// not ok, set Activity status
			activity.setStatus(IActivity.STATUS_NOK);
			throw e;
		} finally {
			IInstallConfiguration current =
				SiteManager.getLocalSite().getCurrentConfiguration();
			((InstallConfiguration) current).addActivityModel(activity);
		}

		// call the configure task		
		configure(installedFeature.getFeature(), false); /*callInstallHandler*/

		return installedFeature;
	}

	/*
	 * @see IConfiguredSite#remove(IFeature, IProgressMonitor)
	 */
	public void remove(IFeature feature, IProgressMonitor monitor)
		throws CoreException {

		// ConfigSite is read only
		if (!isUpdatable()) {
			String errorMessage =
				Policy.bind(
					"ConfiguredSite.NonUninstallableSite",
					getSite().getURL().toExternalForm());
			//$NON-NLS-1$
			throw Utilities.newCoreException(errorMessage, null);
		}

		// create the Activity
		ConfigurationActivity activity =
			new ConfigurationActivity(IActivity.ACTION_FEATURE_REMOVE);
		activity.setLabel(feature.getVersionedIdentifier().toString());
		activity.setDate(new Date());

		try {
			IFeatureReference referenceToRemove = null;
			IFeatureReference[] featureRef = getSite().getFeatureReferences();
			IFeatureReference ref = getSite().getFeatureReference(feature);
			for (int i = 0; i < featureRef.length; i++) {
				if (featureRef[i].equals(ref)) {
					referenceToRemove = featureRef[i];
					break;
				}
			}

			// we found a feature reference on the site matching the feature			
			if (referenceToRemove != null) {
				// Check if feature is unconfigured before we remove it
				// our UI will check.
				// For non-UI application, throw error is feature is configured
				if (getConfigurationPolicy().isConfigured(referenceToRemove)) {
					IFeature featureToRemove = ((IFeatureReference) referenceToRemove).getFeature();
					String featureLabel =
						(featureToRemove == null) ? null : featureToRemove.getLabel();
					throw Utilities
						.newCoreException(Policy.bind("ConfiguredSite.UnableToRemoveConfiguredFeature"
					//$NON-NLS-1$
					, featureLabel), null);
				}
			} else {
				throw Utilities
					.newCoreException(
						Policy.bind("ConfiguredSite.UnableToFindFeature", feature.getURL().toString()),
				//$NON-NLS-1$
				null);
			}

			// remove the feature
			getSite().remove(feature, monitor);
			getConfigurationPolicy().removeFeatureReference(referenceToRemove);
			// everything done ok
			activity.setStatus(IActivity.STATUS_OK);
			// notify listeners
			Object[] siteListeners = listeners.getListeners();
			for (int i = 0; i < siteListeners.length; i++) {
				((IConfiguredSiteChangedListener) siteListeners[i]).featureRemoved(feature);
			}
		} catch (CoreException e) {
			activity.setStatus(IActivity.STATUS_NOK);
			throw e;
		} finally {
			IInstallConfiguration current =
				SiteManager.getLocalSite().getCurrentConfiguration();
			((InstallConfiguration) current).addActivityModel(activity);
		}
	}

	/*
	 * @see IConfiguredSite#configure(IFeature) 
	 */
	public void configure(IFeature feature) throws CoreException {
		configure(feature, true /*callInstallHandler*/
		);
	}

	/*
	 * @see IConfiguredSite#configure(IFeatureReference,boolean)
	 */
	private void configure(IFeature feature, boolean callInstallHandler)
		throws CoreException {
		IFeatureReference featureReference = getSite().getFeatureReference(feature);
		((ConfigurationPolicy) getConfigurationPolicyModel()).configure(
			featureReference,
			callInstallHandler);

		// notify listeners
		Object[] siteListeners = listeners.getListeners();
		for (int i = 0; i < siteListeners.length; i++) {
			((IConfiguredSiteChangedListener) siteListeners[i]).featureConfigured(feature);
		}

	}

	/*
	 * @see IConfiguredSite#unconfigure(IFeature)
	 */
	public boolean unconfigure(IFeature feature) throws CoreException {
		IFeatureReference featureReference = getSite().getFeatureReference(feature);
		ConfigurationPolicy configPolicy =
			((ConfigurationPolicy) getConfigurationPolicyModel());
		if (configPolicy == null)
			return false;

		if (configPolicy.unconfigure(featureReference)) {
			// notify listeners
			Object[] siteListeners = listeners.getListeners();
			for (int i = 0; i < siteListeners.length; i++) {
				((IConfiguredSiteChangedListener) siteListeners[i]).featureUnconfigured(
					feature);
			}
			return true;
		} else {
			return false;
		}
	}

	/*
	 * @see IConfiguredSite#getConfiguredFeatures()
	 */
	public IFeatureReference[] getConfiguredFeatures() {
		ConfigurationPolicy configPolicy =
			((ConfigurationPolicy) getConfigurationPolicyModel());
		if (configPolicy == null)
			return new IFeatureReference[0];
		return configPolicy.getConfiguredFeatures();
	}

	/*
	 * adds configured and unconfigured feature references
	 */
	public IFeatureReference[] getFeatureReferences() {

		ConfigurationPolicy configPolicy =
			((ConfigurationPolicy) getConfigurationPolicyModel());
		if (configPolicy == null)
			return new IFeatureReference[0];

		IFeatureReference[] configuredFeatures = getConfiguredFeatures();
		int confLen = configuredFeatures.length;
		IFeatureReference[] unconfiguredFeatures =
			configPolicy.getUnconfiguredFeatures();
		int unconfLen = unconfiguredFeatures.length;

		IFeatureReference[] result = new IFeatureReference[confLen + unconfLen];
		if (confLen > 0) {
			System.arraycopy(configuredFeatures, 0, result, 0, confLen);
		}
		if (unconfLen > 0) {
			System.arraycopy(unconfiguredFeatures, 0, result, confLen, unconfLen);
		}

		return result;
	}

	/*
	 * Configure and unconfigure appropriate feature to
	 * become 'like' currentConfiguration which is the configuration
	 * the user wants to revert to.
	 * 
	 * All features from currentConfiguration should be configured
	 */
	public void processDeltaWith(
		IConfiguredSite currentConfiguration,
		IProgressMonitor monitor,
		IProblemHandler handler)
		throws CoreException, InterruptedException {

		ConfiguredSite cSite = (ConfiguredSite) currentConfiguration;
		ConfigurationPolicy cPolicy = cSite.getConfigurationPolicy();

		// copy the unmanaged plugins from platform.cfg
		// as they are transient
		this.setPreviousPluginPath(cSite.getPreviousPluginPath());

		// retrieve the feature that were configured
		IFeatureReference[] configuredFeatures = verifyConfiguredFeatures(handler);

		// we only care about unconfigured features if the Site policy is USER_EXCLUDE
		// otherwise we will only set the configured one
		//if (cPolicy.getPolicy() == IPlatformConfiguration.ISitePolicy.USER_EXCLUDE) {

		// calculate all the features we have to unconfigure from the current state to this state
		// in the history. 				
		List featureToUnconfigure = calculateUnconfiguredFeatures(configuredFeatures);

		// for each unconfigured feature check if it still exists
		// if so add as unconfigured
		Iterator iter = featureToUnconfigure.iterator();
		while (iter.hasNext()) {
			IFeatureReference element = (IFeatureReference) iter.next();
			try {
				element.getFeature(); // throws CoreException if Feature does not exist
				getConfigurationPolicy().addUnconfiguredFeatureReference(
					(FeatureReferenceModel) element);
			} catch (CoreException e) {
				// feature does not exist ?
				featureToUnconfigure.remove(element);
				// log no feature to unconfigure
				if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
					String url = element.getURL().toString();
					ISite site = element.getSite();
					String siteString =
						(site != null)
							? site.getURL().toExternalForm()
							: Policy.bind("ConfiguredSite.NoSite");
					//$NON-NLS-1$
					UpdateManagerPlugin.getPlugin().debug(
						Policy.bind("ConfiguredSite.CannotFindFeatureToUnconfigure", url, siteString));
					//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		}
		//} // end USER_EXCLUDE
	}

	/*
	 * We have to keep our configured feature
	 * check if they are all valid
	 */
	private IFeatureReference[] verifyConfiguredFeatures(IProblemHandler handler)
		throws InterruptedException {

		IFeatureReference[] configuredFeatures = getConfiguredFeatures();
		if (configuredFeatures != null) {
			for (int i = 0; i < configuredFeatures.length; i++) {
				IFeature feature = null;

				// attempt to access the feature
				try {
					feature = configuredFeatures[i].getFeature();
				} catch (CoreException e) {
					// notify we cannot find the feature
					UpdateManagerPlugin.getPlugin().getLog().log(e.getStatus());
					String featureString = configuredFeatures[i].getURL().toExternalForm();
					if (!handler
						.reportProblem(
							Policy.bind(
								"ConfiguredSite.CannotFindFeatureToConfigure",
								featureString))) { //$NON-NLS-1$
						throw new InterruptedException();
					}
				}

				// verify all the plugins still exist
				if (feature != null) {
					// get plugin identifier
					List sitePluginIdentifiers = new ArrayList();
					ISite site = feature.getSite();
					IPluginEntry[] sitePluginEntries = null;

					if (site != null) {
						sitePluginEntries = site.getPluginEntries();
						for (int index = 0; index < sitePluginEntries.length; index++) {
							IPluginEntry entry = sitePluginEntries[index];
							sitePluginIdentifiers.add(entry.getVersionedIdentifier());
						}
					}

					if (sitePluginEntries.length > 0) {
						IPluginEntry[] featurePluginEntries = feature.getPluginEntries();
						for (int index = 0; index < featurePluginEntries.length; index++) {
							IPluginEntry currentFeaturePluginEntry = featurePluginEntries[index];
							if (!contains(currentFeaturePluginEntry.getVersionedIdentifier(),
								sitePluginIdentifiers)) {
								// the plugin defined by the feature
								// doesn't seem to exist on the site
								String id =
									UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
								IStatus status =
									new Status(
										IStatus.ERROR,
										id,
										IStatus.OK,
										"Error verifying existence of plugin:"
											+ currentFeaturePluginEntry.getVersionedIdentifier().toString(),
										null);
								//$NON-NLS-1$
								UpdateManagerPlugin.getPlugin().getLog().log(status);
								String siteString =
									(site != null)
										? site.getURL().toExternalForm()
										: Policy.bind("ConfiguredSite.NoSite");
								//$NON-NLS-1$
								String errorLabel =
									Policy.bind(
										"ConfiguredSite.CannotFindPluginEntry",
										currentFeaturePluginEntry.getVersionedIdentifier().toString(),
										siteString);
								//$NON-NLS-1$ //$NON-NLS-2$
								if (handler == null) {
									throw new InterruptedException(errorLabel);
								}
								if (!handler.reportProblem(Policy.bind(errorLabel))) {
									//$NON-NLS-1$ //$NON-NLS-2$
									throw new InterruptedException();
								}
							} // end if not found in site
						} // end for
					}
				}
			} // end for configured feature
		}
		return configuredFeatures;
	}

	/*
	 * We are in the process of calculating the delta between what was configured in the current
	 * configuration that is not configured now
	 * 
	 * we have to figure out what feature have been unconfigured for the whole
	 * history between current and us... 
	 * 
	 * is it as simple as  get all configured, and unconfigured,
	 * the do the delta with what should be configured
	 * 
	 */
	private List calculateUnconfiguredFeatures(IFeatureReference[] configuredFeatures)
		throws CoreException {

		List featureToUnconfigure = new ArrayList(0);

		// loop for all history
		// try to see if the configured site existed
		// if it does, get the unconfigured features 
		// and the configured one
		IInstallConfiguration[] history =
			SiteManager.getLocalSite().getConfigurationHistory();

		for (int i = 0; i < history.length; i++) {
			IInstallConfiguration element = history[i];
			IConfiguredSite[] configSites = element.getConfiguredSites();
			for (int j = 0; j < configSites.length; j++) {
				ConfiguredSite configSite = (ConfiguredSite) configSites[j];
				if (configSite.getSite().equals(getSite())) {
					featureToUnconfigure.addAll(
						Arrays.asList(configSite.getConfigurationPolicy().getUnconfiguredFeatures()));
					featureToUnconfigure.addAll(
						Arrays.asList(configSite.getConfigurationPolicy().getConfiguredFeatures()));
				}
			}
		}

		// remove the unconfigured feature we found that are now to be configured 
		// (they may have been unconfigured in the past, but the revert makes them configured)
		featureToUnconfigure = remove(configuredFeatures, featureToUnconfigure);

		return featureToUnconfigure;
	}

	/*
	 * Utilities: Remove an array of feature references
	 * from a list
	 */
	private List remove(IFeatureReference[] featureRefs, List list) {
		List result = new ArrayList();

		if (list == null)
			return result;

		// if an element of the list is NOT found in the array,
		// add it to the result list			
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			IFeatureReference element = (IFeatureReference) iter.next();
			boolean found = false;
			for (int i = 0; i < featureRefs.length; i++) {
				if (element.equals(featureRefs[i])) {
					found = true;
				}
			}

			if (!found)
				result.add(element);
		}
		return result;
	}

	/*
	 * I have issues when running list.contain(versionedIdentifier)
	 * The code runs the Object.equals instead of the VersionedIdentifier.equals
	 */
	private boolean contains(VersionedIdentifier id, List list) {
		boolean found = false;
		if (list != null && !list.isEmpty()) {
			Iterator iter = list.iterator();
			while (iter.hasNext() && !found) {
				VersionedIdentifier element = (VersionedIdentifier) iter.next();
				if (element.equals(id)) {
					found = true;
				}
			}
		}
		return found;
	}

	/*
	 *
	 */
	public void setConfigurationPolicy(ConfigurationPolicy policy) {
		setConfigurationPolicyModel((ConfigurationPolicyModel) policy);
	}

	/*
	 * 
	 */
	public ConfigurationPolicy getConfigurationPolicy() {
		return (ConfigurationPolicy) getConfigurationPolicyModel();
	}

	/*
	 * 
	 */
	public ISite getSite() {
		return (ISite) getSiteModel();
	}

	/*
	 * 
	 */
	public IInstallConfiguration getInstallConfiguration() {
		return (IInstallConfiguration) getInstallConfigurationModel();
	}

	/*
	 * 
	 */
	public boolean isBroken(IFeature feature) {
		// check the Plugins of all the features
		// every plugin of the feature must be on the site
		ISite currentSite = getSite();
		IPluginEntry[] siteEntries = getSite().getPluginEntries();
		IPluginEntry[] featuresEntries = feature.getPluginEntries();
		IPluginEntry[] result = UpdateManagerUtils.diff(featuresEntries, siteEntries);
		if (result == null || (result.length != 0)) {
			IPluginEntry[] missing = UpdateManagerUtils.diff(featuresEntries, result);
			String listOfMissingPlugins = ""; //$NON-NLS-1$
			for (int k = 0; k < missing.length; k++) {
				listOfMissingPlugins =
					"\r\nplugin:" + missing[k].getVersionedIdentifier().toString();
				//$NON-NLS-1$
			}
			String id =
				UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			String featureString =
				(feature == null) ? null : feature.getURL().toExternalForm();
			String siteString =
				(currentSite == null) ? null : currentSite.getURL().toExternalForm();
			String[] values =
				new String[] { featureString, siteString, listOfMissingPlugins };
			IStatus status =
				new Status(
					IStatus.ERROR,
					id,
					IStatus.OK,
					Policy.bind("ConfiguredSite.MissingPluginsBrokenFeature", values),
					null);
			//$NON-NLS-1$
			UpdateManagerPlugin.getPlugin().getLog().log(status);
			return true;
		}
		return false;
	}

	/*
	 * 
	 */
	public boolean isConfigured(IFeature feature) {
		if (getConfigurationPolicy() == null)
			return false;
		IFeatureReference featureReference = getSite().getFeatureReference(feature);
		if (featureReference == null)
			return false;
		return getConfigurationPolicy().isConfigured(featureReference);
	}

}