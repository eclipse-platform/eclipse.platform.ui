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
import org.eclipse.update.core.model.ConfigurationActivityModel;
import org.eclipse.update.core.model.ConfigurationPolicyModel;
import org.eclipse.update.core.model.FeatureReferenceModel;

/**
 * 
 */
public class ConfigurationPolicy extends ConfigurationPolicyModel implements IConfigurationPolicy {


	/**
	 * Constructor for ConfigurationPolicyModel.
	 */
	public ConfigurationPolicy() {
	}
	
	
	/**
	 * Copy Constructor for ConfigurationPolicyModel.
	 */
	public ConfigurationPolicy(IConfigurationPolicy configPolicy) {
		super();
		setPolicy(configPolicy.getPolicy());
		setConfiguredFeatureReferences(configPolicy.getConfiguredFeatures());
		setUnconfiguredFeatureReferences(configPolicy.getUnconfiguredFeatures());
	}


	/**
	 * @since 2.0
	 */
	public boolean isConfigured(IFeatureReference feature) {
		return super.isConfigured((FeatureReferenceModel) feature);
	}

	/**
	 * adds teh feature to teh list of features if the policy is USER_INCLUDE
	 */
	/*package*/
	void configure(IFeatureReference feature) throws CoreException {

		//Start UOW ?
		ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_CONFIGURE);
		activity.setLabel(feature.getFeature().getVersionIdentifier().toString());
		activity.setDate(new Date());

		addConfiguredFeatureReference((FeatureReferenceModel)feature);

		// everything done ok
		activity.setStatus(IActivity.STATUS_OK);
		((InstallConfiguration) SiteManager.getLocalSite().getCurrentConfiguration()).addActivityModel((ConfigurationActivityModel)activity);
	}

	/**
	 * check if the plugins to unconfigure are required by other configured feature and
	 * adds teh feature to teh list of features if the policy is USER_EXCLUDE
	 */
	/*package*/
	void unconfigure(IFeatureReference feature, IProblemHandler handler) throws CoreException {

		boolean unconfigure = true;
		String uniqueId = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		MultiStatus multiStatus = new MultiStatus(uniqueId, IStatus.WARNING, "Some plugin of this feature are required by the following running plugins", null);

		// plugins to remove		
		IPluginEntry[] pluginsToRemove = ((SiteLocal) SiteManager.getLocalSite()).getUnusedPluginEntries(feature.getFeature());

		// all other plugins that are configured
		IPluginDescriptor[] descriptors = Platform.getPluginRegistry().getPluginDescriptors();

		for (int i = 0; i < descriptors.length; i++) {
			if (require(descriptors[i], pluginsToRemove)) {
				Status status = new Status(IStatus.WARNING, uniqueId, IStatus.OK, descriptors[i].getUniqueIdentifier(), null);
				multiStatus.add(status);
			}
		}

		if (multiStatus.getChildren().length > 0 && handler!=null) {
			unconfigure = handler.reportProblem("Are you certain to want to unconfigure this feature ?", multiStatus);
		}

		if (unconfigure) {
			// FIXME: Start UOW ?
			ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_UNCONFIGURE);
			activity.setLabel(feature.getFeature().getVersionIdentifier().toString());
			activity.setDate(new Date());

			addUnconfiguredFeatureReference((FeatureReferenceModel)feature);

			// everything done ok
			activity.setStatus(IActivity.STATUS_OK);
			((InstallConfiguration) SiteManager.getLocalSite().getCurrentConfiguration()).addActivityModel((ConfigurationActivityModel)activity);
		}
	}

	/**
	 * Returns true if the pluginDescriptor requires one or more pluginEntry
	 * and the pluginDescriptor is not part of the pluginEntries
	 */
	private boolean require(IPluginDescriptor descriptor, IPluginEntry[] entries) {
		boolean result = false;
		if (descriptor != null && entries != null) {
			IPluginPrerequisite[] prereq = descriptor.getPluginPrerequisites();
			//FIXME: todo  list

		}
		return result;
	}

	/**
	 * returns an array of string corresponding to plugins file
	 */
	/*package*/
	String[] getPluginPath(ISite site) throws CoreException {
		String[] result = new String[0];

		// which features
		IFeatureReference[] arrayOfFeatureRef = null;
		if (getPolicy() == IPlatformConfiguration.ISitePolicy.USER_EXCLUDE) {
			if (getUnconfiguredFeatures() != null)
				arrayOfFeatureRef = getUnconfiguredFeatures();
		} else {
			if (getConfiguredFeatures() != null)
				arrayOfFeatureRef = getConfiguredFeatures();
		}

		// obtain path for each feature
		if (arrayOfFeatureRef != null) {
			List pluginsString = new ArrayList(0);
			for (int i = 0; i < arrayOfFeatureRef.length; i++) {
				IFeatureReference element = arrayOfFeatureRef[i];
				IFeature feature = element.getFeature();
				IPluginEntry[] entries = (feature==null)?new IPluginEntry[0]:feature.getPluginEntries();

				for (int index = 0; index < entries.length; index++) {
					IPluginEntry entry = entries[index];
					String id = entry.getVersionIdentifier().toString();
					// obtain the path of the plugin directories on the site	
					ContentReference[] featureContentReference = feature.getFeatureContentProvider().getPluginEntryArchiveReferences(entry, null/*IProgressMonitor*/);
					for (int j = 0; j < featureContentReference.length; j++) {
						URL url = site.getSiteContentProvider().getArchiveReference(featureContentReference[j].getIdentifier());
						if (url != null) {
							// make it relative to teh site
							String path = UpdateManagerUtils.getURLAsString(site.getURL(), url);
							// add end "/"
							path += (path.endsWith(File.separator) || path.endsWith("/")) ? "" : "/";
							// add plugin.xml or fragment.xml
							path += entry.isFragment() ? "fragment.xml" : "plugin.xml"; //FIXME: fragments
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
			return (IFeatureReference[])result;
	}

	/**
	 * @since 2.0
	 */
	public IFeatureReference[] getUnconfiguredFeatures() {
		FeatureReferenceModel[] result = getUnconfiguredFeaturesModel();
		if (result.length == 0)
			return new IFeatureReference[0];
		else
			return (IFeatureReference[])result;
	}

}