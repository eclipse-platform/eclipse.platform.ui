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
	public String[] getPluginPath(ISite site) throws CoreException {

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
		// patches indexed by patched features
		Map patches = getPatchedFeatures(configuredFeatures);	
		
		if (getPolicy() == IPlatformConfiguration.ISitePolicy.USER_EXCLUDE) {
			//	EXCLUDE: return unconfigured plugins MINUS any plugins that
			//           are configured
			
			if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_CONFIGURATION)
				UpdateCore.warn("UNCONFIGURED PLUGINS");
			
			// virtually unconfigured 
			String[] unconfiguredPlugins = getUnconfiguredPluginStrings(site, unconfiguredFeatures, patches);
			
			if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_CONFIGURATION)
				UpdateCore.warn("CONFIGURED PLUGINS");

			// virtually configured
			String[] configuredPlugins = getConfiguredPluginStrings(site, configuredFeatures, patches);
			if (isEnabled())
				pluginsToWrite = subtract(unconfiguredPlugins, configuredPlugins);
			else
				pluginsToWrite = union(configuredPlugins, unconfiguredPlugins);
		} else {
			// INCLUDE: return configured plugins
			if (isEnabled()) {
				pluginsToWrite = getConfiguredPluginStrings(site, configuredFeatures, patches);
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
	 * Obtains map of patch features indexed by patched features
	 * 
	 * @param configuredFeatures
	 *            array of features to operate with
	 * @return Map of IFeatureReference indexed by IFeatureReference
	 */
	private Map getPatchedFeatures(IFeatureReference[] features) {
		// Create a map of Identifiers for all feature references
		Map featureRefsById = new HashMap();
		for (int f = 0; f < features.length; f++) {
			IFeatureReference featureRef = features[f];
			try {
				VersionedIdentifier vi = featureRef.getVersionedIdentifier();
				featureRefsById.put(vi, features[f]);
			} catch (CoreException e) {
				UpdateCore.warn(null, e);
			}
		}

		// Create map of patch IFeatureReferences to patched IFeatureReference
		Map patches = new HashMap();
		for (int f = 0; f < features.length; f++) {
			IFeatureReference patchCandidate = features[f];
			try {
				IFeature feature = patchCandidate.getFeature(null);
				IImport[] imports = feature.getImports();
				for (int i = 0; i < imports.length; i++) {
					IImport oneImport = imports[i];
					if (!oneImport.isPatch())
						continue;
					VersionedIdentifier patchedIdentifier =
						oneImport.getVersionedIdentifier();
					if (featureRefsById.keySet().contains(patchedIdentifier)) {
						patches.put(
							featureRefsById.get(patchedIdentifier),
							patchCandidate);
					} else {
						// patched not enabled
					}
				}
			} catch (CoreException e) {
				UpdateCore.warn(null, e);
			}
		}
		return patches;
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
	 * @return an array of plugin path for the array of feature reference. For
	 *         features that are also in the patched map, plugin path will
	 *         point to plugin with the same ID provided by the patch if it
	 *         exists. Each plugin path only appears once [bug 21750]
	 */
	private String[] getConfiguredPluginStrings(ISite site, IFeatureReference[] featureRefs, Map patchedIFeatureRefsToPatchIFeatureRefs) throws CoreException {
		final Collection patchedFeatureRefs = patchedIFeatureRefsToPatchIFeatureRefs.keySet();

		// plugin entries with feature reference for each
		Map pluginEntriesToIFeatures = new HashMap();
		for (int i = 0; i < featureRefs.length; i++) {
			try {
				IFeature feature = featureRefs[i].getFeature(null);
				if (feature == null) {
					UpdateCore.warn("Null Feature", new Exception());
					continue;
				}

				IPluginEntry[] entries = feature.getPluginEntries();
				// add every plugin entry to the map
				for (int entr = 0; entr < entries.length; entr++) {
					// add either plugin entry from a feature or from its patch
					boolean pluginUpdated = false;
					if (patchedFeatureRefs.contains(featureRefs[i])) {
						// if patch contributes the same plug-in add it instead
						String pluginID = entries[entr].getVersionedIdentifier().getIdentifier();
						IFeatureReference patchFeatureRef = (IFeatureReference) patchedIFeatureRefsToPatchIFeatureRefs.get(featureRefs[i]);
						try {
							IFeature patchFeature = patchFeatureRef.getFeature(null);
							IPluginEntry[] newerEntries = patchFeature.getPluginEntries();
							for (int n = 0; n < newerEntries.length; n++) {
								if (pluginID.equals(newerEntries[n].getVersionedIdentifier().getIdentifier())) {
									// patch has an updated plugin
									pluginUpdated = true;
									pluginEntriesToIFeatures.put(newerEntries[n], patchFeature);
									break;
								}
							}
						} catch (CoreException e) {
							UpdateCore.warn(null, e);
						}

					}
					if (!pluginUpdated) {
						pluginEntriesToIFeatures.put(entries[entr], feature);
					}

				}

			} catch (CoreException e) {
				UpdateCore.warn(null, e);
			}
		}
		Set pluginStrings = getEntriesPath(site, pluginEntriesToIFeatures);

		// transform in String[]
		if (!pluginStrings.isEmpty()) {
			String[] result = new String[pluginStrings.size()];
			pluginStrings.toArray(result);
			return result;
		} else {
			return new String[0];
		}
	}
	/**
	 * @return an array of plugin path for the array of unconfigured feature
	 *         reference, and for map of patched features Each plugin path only
	 *         appears once [bug 21750]
	 */
	private String[] getUnconfiguredPluginStrings(ISite site, IFeatureReference[] featureRefs, Map patchedIFeatureRefsToPatchIFeatureRefs) throws CoreException {
		// Unconfgured and patched features
		Set virtuallyUnconfiguredFeatures = new HashSet();
		virtuallyUnconfiguredFeatures.addAll(Arrays.asList(featureRefs));
		virtuallyUnconfiguredFeatures.addAll(patchedIFeatureRefsToPatchIFeatureRefs.keySet());

		// plugin entries with feature reference for each
		Map pluginEntriesToIFeatures = new HashMap();
		for (Iterator it = virtuallyUnconfiguredFeatures.iterator(); it.hasNext();) {
			IFeatureReference featureRef = (IFeatureReference) it.next();
			try {
				IFeature feature = featureRef.getFeature(null);
				if (feature == null) {
					UpdateCore.warn("Null Feature", new Exception());
					continue;
				}

				IPluginEntry[] entries = feature.getPluginEntries();
				// add every plugin entry to the map
				for (int entr = 0; entr < entries.length; entr++) {
					pluginEntriesToIFeatures.put(entries[entr], feature);
				}

			} catch (CoreException e) {
				UpdateCore.warn(null, e);
			}
		}
		Set pluginStrings = getEntriesPath(site, pluginEntriesToIFeatures);

		// transform in String[]
		if (!pluginStrings.isEmpty()) {
			String[] result = new String[pluginStrings.size()];
			pluginStrings.toArray(result);
			return result;
		} else {
			return new String[0];
		}
	}
	/**
	 * @param site
	 * @param pluginEntriesToIFeatures plugin entries and corresponding IFeatures
	 * @param pluginEntriesToFeatureRefs map of IPluginEntry'ies to IFeatures
	 * @return valid string pointing to plugins in given features
	 * @throws CoreException
	 */
	private Set getEntriesPath(ISite site, Map pluginEntriesToIFeatures) throws CoreException {
		Set pluginStrings=new HashSet();
		for (Iterator it= pluginEntriesToIFeatures.keySet().iterator(); it.hasNext();) {
			IPluginEntry entry = (IPluginEntry)it.next();
			IFeature feature=(IFeature)pluginEntriesToIFeatures.get(entry);

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
						pluginStrings.add(path);
						if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_CONFIGURATION)
							UpdateCore.warn("Add plugin: " + path + " to the list");
					}
				}
			}
		}
		return pluginStrings;
	}

	/**
	 *	 we need to figure out which plugin SHOULD NOT be written and
	 *	 remove them from include
	 *	 we can compare the String of the URL
	 */
	private String[] subtract(String[] allPlugins, String[] pluginsToRemove) {
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

		String[] resultEntry = new String[resultList.size()];
		return (String[])resultList.toArray(resultEntry);
	}
	/**
	 * Returns and array with the union of plugins
	 */
	private String[] union(String[] array1, String[] array2) {

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

		String[] resultEntry = new String[resultList.size()];
		return (String[])resultList.toArray(resultEntry);
	}
}
