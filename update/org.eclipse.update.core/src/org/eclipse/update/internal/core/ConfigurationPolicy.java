package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.model.FeatureReferenceModel;
import org.eclipse.update.internal.model.*;
import org.eclipse.update.internal.model.ConfigurationActivityModel;
import org.eclipse.update.internal.model.ConfigurationPolicyModel;

/**
 * 
 */
public class ConfigurationPolicy extends ConfigurationPolicyModel {

	private IConfiguredSite configuredSite;

	/**
	 * Constructor for ConfigurationPolicyModel.
	 */
	public ConfigurationPolicy() {
	}

	/**
	 * Copy Constructor for ConfigurationPolicyModel.
	 */
	public ConfigurationPolicy(ConfigurationPolicy configPolicy) {
		super();
		setPolicy(configPolicy.getPolicy());
		setConfiguredFeatureReferences(configPolicy.getConfiguredFeatures());
		setUnconfiguredFeatureReferences(configPolicy.getUnconfiguredFeatures());
		setConfiguredSite(configPolicy.getConfiguredSite());
	}

	/**
	 * @since 2.0
	 */
	public boolean isConfigured(IFeatureReference feature) {
		return super.isConfigured((FeatureReferenceModel) feature);
	}

	/**
	 * adds the feature to the list of features if the policy is USER_INCLUDE
	 */
	public	void configure(IFeatureReference featureReference, boolean callInstallHandler) throws CoreException {

		if (featureReference == null)
			return;
		IFeature feature = featureReference.getFeature();
		if (feature == null)
			return;

		// Setup optional install handler
		InstallHandlerProxy handler = null;
		if (callInstallHandler)
			handler = new InstallHandlerProxy(IInstallHandler.HANDLER_ACTION_CONFIGURE, feature, feature.getInstallHandlerEntry(), null);
		boolean success = false;
		Throwable originalException = null;

		// do the configure action
		try {
			if (handler != null)
				handler.configureInitiated();

			ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_CONFIGURE);
			activity.setLabel(feature.getVersionedIdentifier().toString());
			activity.setDate(new Date());

			addConfiguredFeatureReference((FeatureReferenceModel) featureReference);

			// everything done ok
			activity.setStatus(IActivity.STATUS_OK);
			((InstallConfiguration) SiteManager.getLocalSite().getCurrentConfiguration()).addActivityModel((ConfigurationActivityModel) activity);

			if (handler != null)
				handler.completeConfigure();

			success = true;
		} catch (Throwable t) {
			originalException = t;
		} finally {
			Throwable newException = null;
			try {
				if (handler != null)
					handler.configureCompleted(success);
			} catch (Throwable t) {
				newException = t;
			}
			if (originalException != null) // original exception wins
				throw UpdateManagerUtils.newCoreException(Policy.bind("InstallHandler.error", feature.getLabel()), originalException);
			if (newException != null)
				throw UpdateManagerUtils.newCoreException(Policy.bind("InstallHandler.error", feature.getLabel()), newException);
		}
	}

	/**
	 * check if the plugins to unconfigure are required by other configured feature and
	 * adds teh feature to teh list of features if the policy is USER_EXCLUDE
	 */
	public	boolean unconfigure(IFeatureReference featureReference) throws CoreException {

		if (featureReference == null)
			return false;
		IFeature feature = featureReference.getFeature();
		if (feature == null)
			return false;

		// Setup optional install handler
		InstallHandlerProxy handler = new InstallHandlerProxy(IInstallHandler.HANDLER_ACTION_UNCONFIGURE, feature, feature.getInstallHandlerEntry(), null);
		boolean success = false;
		Throwable originalException = null;

		// do the unconfigure action
		try {
			handler.unconfigureInitiated();

			ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_UNCONFIGURE);
			activity.setLabel(featureReference.getFeature().getVersionedIdentifier().toString());
			activity.setDate(new Date());

			addUnconfiguredFeatureReference((FeatureReferenceModel) featureReference);

			// everything done ok
			activity.setStatus(IActivity.STATUS_OK);
			((InstallConfiguration) SiteManager.getLocalSite().getCurrentConfiguration()).addActivityModel((ConfigurationActivityModel) activity);

			handler.completeUnconfigure();

			success = true;
		} catch (Throwable t) {
			originalException = t;
		} finally {
			Throwable newException = null;
			try {
				handler.configureCompleted(success);
			} catch (Throwable t) {
				newException = t;
			}
			if (originalException != null) // original exception wins
				throw UpdateManagerUtils.newCoreException(Policy.bind("InstallHandler.error", feature.getLabel()), originalException);
			if (newException != null)
				throw UpdateManagerUtils.newCoreException(Policy.bind("InstallHandler.error", feature.getLabel()), newException);
		}

		return true;
	}

	/**
	 * returns an array of string corresponding to plugins file
	 * It will include the include list providing the objects in the include list
	 * are not 'unconfigured' if the type is Exclude or 'configured' if the type is Include
	 */
	public String[] getPluginPath(ISite site, String[] pluginRead) throws CoreException {


		// which features should we write based on the policy
		IFeatureReference[] arrayOfFeatureRef = null;
		if (getPolicy() == IPlatformConfiguration.ISitePolicy.USER_EXCLUDE) {
			if (getUnconfiguredFeatures() != null)
				arrayOfFeatureRef = getUnconfiguredFeatures();
		} else {
			if (getConfiguredFeatures() != null)
				arrayOfFeatureRef = getConfiguredFeatures();
		}
		String[] pluginToWrite = getPluginString(site, arrayOfFeatureRef);
		

		// remove from include the plugins that should not be saved 
		if (getPolicy() == IPlatformConfiguration.ISitePolicy.USER_EXCLUDE) {
			if (getConfiguredFeatures() != null)
				arrayOfFeatureRef = getConfiguredFeatures();
		} else {
			if (getUnconfiguredFeatures() != null)
				arrayOfFeatureRef = getUnconfiguredFeatures();
		}
		String[] pluginNotToWrite = getPluginString(site, arrayOfFeatureRef);

		String[] included = delta(pluginNotToWrite, pluginRead);
		String[] result = union(included, pluginToWrite);

		return result;
	}


	/**
	 * return an array of plugin path for the array of feature reference
	 * 
	 * 
	 */
	private String[] getPluginString(ISite site, IFeatureReference[] arrayOfFeatureRef) throws CoreException {
	
		String[] result = new String[0];
			
		// obtain path for each feature
		if (arrayOfFeatureRef != null) {
			List pluginsString = new ArrayList(0);
			for (int i = 0; i < arrayOfFeatureRef.length; i++) {
				IFeatureReference element = arrayOfFeatureRef[i];
				IFeature feature = element.getFeature();
				IPluginEntry[] entries = (feature == null) ? new IPluginEntry[0] : feature.getPluginEntries();
		
				for (int index = 0; index < entries.length; index++) {
					IPluginEntry entry = entries[index];
					// obtain the path of the plugin directories on the site	
					ContentReference[] featureContentReference = feature.getFeatureContentProvider().getPluginEntryArchiveReferences(entry, null /*IProgressMonitor*/
					);
					for (int j = 0; j < featureContentReference.length; j++) {
						URL url = site.getSiteContentProvider().getArchiveReference(featureContentReference[j].getIdentifier());
						if (url != null) {
							// make it relative to teh site
							String path = UpdateManagerUtils.getURLAsString(site.getURL(), url);
							// add end "/"
							path += (path.endsWith(File.separator) || path.endsWith("/")) ? "" : "/"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							// add plugin.xml or fragment.xml
							path += entry.isFragment() ? "fragment.xml" : "plugin.xml"; //$NON-NLS-1$ //$NON-NLS-2$
							pluginsString.add(path);
						}
					}
				}
			}
		
			// transform in String[]
			if (!pluginsString.isEmpty()) {
				result = new String[pluginsString.size()];
				pluginsString.toArray(result);
			}
		}
		return result;
	}

	/**
	 * @since 2.0
	 */
	public IFeatureReference[] getConfiguredFeatures() {
		FeatureReferenceModel[] result = getConfiguredFeaturesModel();
		if (result.length == 0)
			return new IFeatureReference[0];
		else
			return (IFeatureReference[]) result;
	}

	/**
	 * @since 2.0
	 */
	public IFeatureReference[] getUnconfiguredFeatures() {
		FeatureReferenceModel[] result = getUnconfiguredFeaturesModel();
		if (result.length == 0)
			return new IFeatureReference[0];
		else
			return (IFeatureReference[]) result;
	}

	/**
	 * Returns and array with the union of plugins
	*/
	private String[] union(String[] targetArray, String[] sourceArray) {

		// No string 
		if (sourceArray == null || sourceArray.length == 0) {
			return targetArray;
		}

		// No string
		if (targetArray == null || targetArray.length == 0) {
			return sourceArray;
		}

		// if a String from sourceArray is NOT in
		// targetArray, add it to targetArray
		List list1 = new ArrayList();
		list1.addAll(Arrays.asList(targetArray));
		for (int i = 0; i < sourceArray.length; i++) {
			if (!list1.contains(sourceArray[i]))
				list1.add(sourceArray[i]);
		}

		String[] resultEntry = new String[list1.size()];
		if (list1.size() > 0)
			list1.toArray(resultEntry);

		return resultEntry;
	}

	/**
	*	 we need to figure out which plugin SHOULD NOT be written and
	*	 remove them from include
	*	 we can compare the String of the URL
	*/
	private String[] delta(String[] pluginsToRemove, String[] allPlugins) {
		// No plugins to remove, return allPlugins 
		if (pluginsToRemove == null || pluginsToRemove.length == 0) {
			return allPlugins;
		}

		// We didn't read any plugins in platform.cfg
		if (allPlugins == null || allPlugins.length == 0) {
			return new String[0];
		}

		// if a String from pluginsToRemove IS in
		// allPlugins, remove it from allPlugins
		List list1 = new ArrayList();
		list1.addAll(Arrays.asList(allPlugins));
		for (int i = 0; i < pluginsToRemove.length; i++) {
			if (list1.contains(pluginsToRemove[i]))
				list1.remove(pluginsToRemove[i]);
		}

		String[] resultEntry = new String[list1.size()];
		if (list1.size() > 0)
			list1.toArray(resultEntry);

		return resultEntry;
	}

	/**
	 * Gets the configuredSite.
	 * @return Returns a IConfiguredSite
	 */
	public IConfiguredSite getConfiguredSite() {
		return configuredSite;
	}

	/**
	 * Sets the configuredSite.
	 * @param configuredSite The configuredSite to set
	 */
	public void setConfiguredSite(IConfiguredSite configuredSite) {
		this.configuredSite = configuredSite;
	}

}