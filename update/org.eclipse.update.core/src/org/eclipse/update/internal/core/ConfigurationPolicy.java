/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.core;
import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.boot.*;
import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.model.*;

/**
 * 
 */
public class ConfigurationPolicy extends ConfigurationPolicyModel {

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
		setConfiguredSiteModel(configPolicy.getConfiguredSiteModel());
	}

	/**
	 * @since 2.0
	 */
	private boolean isUnconfigured(IFeatureReference featureReference) {

		if (featureReference == null)
			return false;

		// returns true if the feature is part of the configured list
		IFeatureReference[] refs = getUnconfiguredFeatures();
		for (int i = 0; i < refs.length; i++) {
			if (featureReference.equals(refs[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @since 2.0
	 */
	public boolean isConfigured(IFeatureReference featureReference) {

		if (featureReference == null)
			return false;

		// returns true if the feature is part of the configured list
		IFeatureReference[] refs = getConfiguredFeatures();
		for (int i = 0; i < refs.length; i++) {
			if (featureReference.equals(refs[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * adds the feature to the list of features if the policy is USER_INCLUDE
	 */
	public void configure(IFeatureReference featureReference, boolean callInstallHandler, boolean createActivity) throws CoreException {

		if (isConfigured(featureReference)) // already configured
			return;

		if (featureReference == null) {
			UpdateCore.warn("The feature reference to configure is null");
			return;
		}

		IFeature feature = null;
		try {
			feature = featureReference.getFeature(null);
		} catch (CoreException e) {
			if (!UpdateManagerUtils.isOptional(featureReference)) {
				URL url = featureReference.getURL();
				String urlString = (url != null) ? url.toExternalForm() : "<no feature reference url>";
				UpdateCore.warn("Error retrieving feature:" + urlString, e);
				return;
			}
		}
		if (feature == null) {
			URL url = featureReference.getURL();
			String urlString = (url != null) ? url.toExternalForm() : "<no feature reference url>";
			UpdateCore.warn("The feature to unconfigure is null: feature reference is:" + urlString);
		}

		// Setup optional install handler
		InstallHandlerProxy handler = null;
		if (callInstallHandler && feature.getInstallHandlerEntry() != null)
			handler = new InstallHandlerProxy(IInstallHandler.HANDLER_ACTION_CONFIGURE, feature, feature.getInstallHandlerEntry(), null);
		boolean success = false;
		Throwable originalException = null;

		// do the configure action
		try {
			if (handler != null)
				handler.configureInitiated();

			ConfigurationActivity activity = null;
			if (createActivity) {
				activity = new ConfigurationActivity(IActivity.ACTION_CONFIGURE);
				activity.setLabel(feature.getVersionedIdentifier().toString());
				activity.setDate(new Date());
			}

			addConfiguredFeatureReference((FeatureReferenceModel) featureReference);

			// everything done ok
			if (activity != null) {
				InstallConfiguration installConfig = (InstallConfiguration) SiteManager.getLocalSite().getCurrentConfiguration();
				activity.setStatus(IActivity.STATUS_OK);
				installConfig.addActivity(activity);
			}

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
				throw Utilities.newCoreException(Policy.bind("InstallHandler.error", feature.getLabel()), originalException);
			if (newException != null)
				throw Utilities.newCoreException(Policy.bind("InstallHandler.error", feature.getLabel()), newException);
		}
	}

	/**
	 * check if the plugins to unconfigure are required by other configured feature and
	 * adds the feature to the list of unconfigured features 
	 */
	public boolean unconfigure(IFeatureReference featureReference, boolean callInstallHandler, boolean createActivity) throws CoreException {

		if (isUnconfigured(featureReference)) {
			UpdateCore.warn("Feature already unconfigured");
			return true;
		}

		if (featureReference == null) {
			UpdateCore.warn("The feature reference to unconfigure is null");
			return false;
		}

		IFeature feature = null;
		try {
			feature = featureReference.getFeature(null);
		} catch (CoreException e) {
			if (!UpdateManagerUtils.isOptional(featureReference)) {
				URL url = featureReference.getURL();
				String urlString = (url != null) ? url.toExternalForm() : "<no feature reference url>";
				UpdateCore.warn("Error retrieving feature:" + urlString, e);
				return false;
			}
		}

		if (feature == null) {
			URL url = featureReference.getURL();
			String urlString = (url != null) ? url.toExternalForm() : "<no feature reference url>";
			UpdateCore.warn("The feature to unconfigure is null: feature reference is:" + urlString);
			return false;
		}

		// Setup optional install handler
		InstallHandlerProxy handler = null;
		if (callInstallHandler && feature.getInstallHandlerEntry() != null) {
			handler = new InstallHandlerProxy(IInstallHandler.HANDLER_ACTION_UNCONFIGURE, feature, feature.getInstallHandlerEntry(), null);
		}

		boolean success = false;
		Throwable originalException = null;

		// do the unconfigure action
		try {

			ConfigurationActivity activity = null;
			if (createActivity) {
				activity = new ConfigurationActivity(IActivity.ACTION_UNCONFIGURE);
				activity.setLabel(feature.getVersionedIdentifier().toString());
				activity.setDate(new Date());
			}

			InstallConfiguration installConfig = null;

			// only ask for install config is activity created.
			// prevents loops during reconciliation
			if (activity != null)
				installConfig = ((InstallConfiguration) SiteManager.getLocalSite().getCurrentConfiguration());

			// Allow unconfigure if the feature is optional from all the parents
			// or if the feature is mandatory and non of its parent are configured
			// removed, not a core issue (so deep down)
			//if (validateNoConfiguredParents(feature)) {
			if (handler != null)
				handler.unconfigureInitiated();
			addUnconfiguredFeatureReference((FeatureReferenceModel) featureReference);
			if (handler != null)
				handler.completeUnconfigure();

			// everything done ok
			if (activity != null) {
				activity.setStatus(IActivity.STATUS_OK);
				installConfig.addActivity(activity);
			}
			success = true;
			//} else {
			//	if (activity != null) {
			//		activity.setStatus(IActivity.STATUS_NOK);
			//		installConfig.addActivityModel((ConfigurationActivityModel) activity);
			//	}
			//}
		} catch (Throwable t) {
			originalException = t;
		} finally {
			Throwable newException = null;
			try {
				if (handler != null)
					handler.unconfigureCompleted(success);
			} catch (Throwable t) {
				newException = t;
			}
			if (originalException != null) // original exception wins
				throw Utilities.newCoreException(Policy.bind("InstallHandler.error", feature.getLabel()), originalException);
			if (newException != null)
				throw Utilities.newCoreException(Policy.bind("InstallHandler.error", feature.getLabel()), newException);
		}

		if (!success) {
			URL url = featureReference.getURL();
			String urlString = (url != null) ? url.toExternalForm() : "<no feature reference url>";
			UpdateCore.warn("Unable to unconfigure:" + urlString);
		}
		return success;
	}

	/**
	 * Calculates the plugin list for the policy. 
	 * For "INCLUDE" policy, this corresponds to the plugins for 
	 * configured features. For "EXCLUDE" policy, this corresponds to the
	 * plugins for unconfigured features that are not referenced
	 * by any configured features.
	 */
	public String[] getPluginPath(ISite site, String[] pluginRead) throws CoreException {

		String[] pluginsToWrite;

		// Note: with the new changes for patches, we leave patched
		//       features configured, so we need to take this into
		//       account when computing configured/unconfigured plugins
		
		// Note: important: it is assumed that the patch lists all the plugins
		//       in the patched feature, including those that haven't changed.
		//       This limitation will be removed later.
		
		// all unconfigured features. Note that patched features are still configured
		IFeatureReference[] unconfiguredFeatures = getUnconfiguredFeatures();
		// all configured features, including patches and patched features
		IFeatureReference[] configuredFeatures = getConfiguredFeatures();
		// configured patched features
		IFeatureReference[] patched = getPatchedFeatures(configuredFeatures);
		
		if (getPolicy() == IPlatformConfiguration.ISitePolicy.USER_EXCLUDE) {
			//	EXCLUDE: return unconfigured plugins MINUS any plugins that
			//           are configured
			
			if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_CONFIGURATION)
				UpdateCore.warn("UNCONFIGURED PLUGINS");
			
			// virtually unconfigured 
			unconfiguredFeatures = (IFeatureReference[]) union(patched, unconfiguredFeatures);
			String[] unconfiguredPlugins = getPluginString(site, unconfiguredFeatures);
			
			if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_CONFIGURATION)
				UpdateCore.warn("CONFIGURED PLUGINS");
			
			// virtually configured
			configuredFeatures = (IFeatureReference[]) subtract(configuredFeatures, patched);
			String[] configuredPlugins = getPluginString(site, configuredFeatures);
			if (isEnabled())
				pluginsToWrite = (String[]) subtract(configuredPlugins, unconfiguredPlugins);
			else
				pluginsToWrite = (String[]) union(configuredPlugins, unconfiguredPlugins);
		} else {
			// INCLUDE: return configured plugins
			if (isEnabled()) {
				configuredFeatures = (IFeatureReference[]) subtract(patched, configuredFeatures);
				pluginsToWrite = getPluginString(site, configuredFeatures);
			} else
				pluginsToWrite = new String[0];
		}

		//TRACE
		if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER) {
			UpdateCore.debug("GetPluginPath for: " + ((site == null) ? "<No site>" : site.getURL().toString()));
			for (int i = 0; i < pluginsToWrite.length; i++) {
				UpdateCore.debug("To write:" + pluginsToWrite[i]);
			}
		}

		return pluginsToWrite;
	}
	
	/**
	 * @return the list of features that are patched.
	 */
	private IFeatureReference[] getPatchedFeatures(IFeatureReference[] features) {
		// Collect identifiers of all patched features
		ArrayList patchedIdentifiers = new ArrayList();
		for (int f = 0; f < features.length; f++) {
			IFeatureReference fRef = features[f];
			// if patches anything add the patched to the list
			try {
				IFeature feature = fRef.getFeature(null);
				IImport[] imports = feature.getImports();
				for (int i = 0; i < imports.length; i++) {
					IImport oneImport = imports[i];
					if (!oneImport.isPatch())
						continue;
					patchedIdentifiers.add(oneImport.getVersionedIdentifier());
				}
			} catch (CoreException e) {
				UpdateCore.warn(null, e);
			}
		}
		// Create a list of configured features which identifiers are in list of patched
		ArrayList patchedFeatureRefs = new ArrayList();
		for (int f = 0; f < features.length; f++) {
			IFeatureReference enabled = features[f];
			try {
				VersionedIdentifier vi=enabled.getVersionedIdentifier();
				if(patchedIdentifiers.contains(vi))
					patchedFeatureRefs.add(features[f]);
			} catch (CoreException e) {
				UpdateCore.warn(null, e);
			}
		}

		return (IFeatureReference[]) patchedFeatureRefs.toArray(new IFeatureReference[patchedFeatureRefs.size()]);
		
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
	 * Gets the configuredSite.
	 * @return Returns a IConfiguredSite
	 */
	public IConfiguredSite getConfiguredSite() {
		return (IConfiguredSite) getConfiguredSiteModel();
	}

	/**
	 * removes a feature reference
	 */
	public void removeFeatureReference(IFeatureReference featureRef) {
		if (featureRef instanceof FeatureReferenceModel) {
			removeFeatureReference((FeatureReferenceModel) featureRef);
		}
	}

	/**
	 * return an array of plugin path for the array of feature reference
	 * Each plugin path only appears once [bug 21750]
	 */
	private String[] getPluginString(ISite site, IFeatureReference[] arrayOfFeatureRef) throws CoreException {

		String[] result = new String[0];

		// obtain path for each feature
		if (arrayOfFeatureRef != null) {
			//[bug 21750] replace the List by a Set
			Set pluginsString = new HashSet();
			for (int i = 0; i < arrayOfFeatureRef.length; i++) {
				IFeatureReference element = arrayOfFeatureRef[i];
				IFeature feature = null;
				try {
					feature = element.getFeature(null);
				} catch (CoreException e) {
					UpdateCore.warn(null, e);
				}
				IPluginEntry[] entries = null;
				if (feature == null) {
					UpdateCore.warn("Null Feature", new Exception());
					entries = new IPluginEntry[0];
				} else {
					entries = feature.getPluginEntries();
				}

				for (int index = 0; index < entries.length; index++) {
					IPluginEntry entry = entries[index];

					// obtain the path of the plugin directories on the site
					ContentReference[] featureContentReference = null;
					try {
						featureContentReference = feature.getFeatureContentProvider().getPluginEntryArchiveReferences(entry, null /*IProgressMonitor*/
						);
					} catch (CoreException e) {
						UpdateCore.warn(null, e);
					}

					// transform into a valid String
					if (featureContentReference != null) {
						for (int j = 0; j < featureContentReference.length; j++) {
							URL url = site.getSiteContentProvider().getArchiveReference(featureContentReference[j].getIdentifier());
							if (url != null) {
								// make it relative to the site
								String path = UpdateManagerUtils.getURLAsString(site.getURL(), url);
								// add end "/"
								path += (path.endsWith(File.separator) || path.endsWith("/")) ? "" : "/";
								//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								// add plugin.xml or fragment.xml
								path += entry.isFragment() ? "fragment.xml" : "plugin.xml";
								//$NON-NLS-1$ //$NON-NLS-2$
								pluginsString.add(path);
								if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_CONFIGURATION)
									UpdateCore.warn("Add plugin: " + path + " to the list");
							}
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
	*	 we need to figure out which plugin SHOULD NOT be written and
	*	 remove them from include
	*	 we can compare the String of the URL
	*/
	private Object[] subtract(Object[] allPlugins, Object[] pluginsToRemove) {
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
		List resultList = new ArrayList();
		resultList.addAll(Arrays.asList(allPlugins));
		for (int i = 0; i < pluginsToRemove.length; i++) {
			if (resultList.contains(pluginsToRemove[i])) {
				resultList.remove(pluginsToRemove[i]);
			}
		}
		
		return arrayTypeFor(resultList);
	}

	/**
	 * Returns and array with the union of plugins
	*/
	private Object[] union(Object[] array1, Object[] array2) {

		// No string 
		if (array2 == null || array2.length == 0) {
			return array1;
		}

		// No string
		if (array1 == null || array1.length == 0) {
			return array2;
		}

		// if a String from sourceArray is NOT in
		// targetArray, add it to targetArray
		List resultList = new ArrayList();
		resultList.addAll(Arrays.asList(array1));
		for (int i = 0; i < array2.length; i++) {
			if (!resultList.contains(array2[i]))
				resultList.add(array2[i]);
		}

		return arrayTypeFor(resultList);
	}
}
