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
import org.eclipse.update.internal.core.Policy;

/**
 * 
 */
public class ConfigurationPolicy extends ConfigurationPolicyModel{


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
	 * adds teh feature to teh list of features if the policy is USER_INCLUDE
	 */
	/*package*/
	void configure(IFeatureReference featureReference) throws CoreException {


		if (featureReference==null) return;
		
		//Start UOW ?
		ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_CONFIGURE);
		IFeature feature = featureReference.getFeature();
		if (feature!=null){
			activity.setLabel(feature.getVersionedIdentifier().toString());
			activity.setDate(new Date());
	
			addConfiguredFeatureReference((FeatureReferenceModel)featureReference);
	
			// everything done ok
			activity.setStatus(IActivity.STATUS_OK);
			((InstallConfiguration) SiteManager.getLocalSite().getCurrentConfiguration()).addActivityModel((ConfigurationActivityModel)activity);
		} 
	}

	/**
	 * check if the plugins to unconfigure are required by other configured feature and
	 * adds teh feature to teh list of features if the policy is USER_EXCLUDE
	 */
	/*package*/
	boolean unconfigure(IFeatureReference featureReference,IProblemHandler handler) throws CoreException {

		if (featureReference==null) return false;
		
		boolean unconfigure = true;
		String uniqueId = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		MultiStatus multiStatus = new MultiStatus(uniqueId, IStatus.WARNING, Policy.bind("ConfigurationPolicy.RequiredPlugins"), null); //$NON-NLS-1$

		// plugins to remove	
		ISite site = configuredSite.getSite();
		IFeature feature = featureReference.getFeature();	
		IPluginEntry[] pluginsToRemove = site.getPluginEntriesOnlyReferencedBy(feature);

		// all other plugins that are configured
		IPluginDescriptor[] descriptors = Platform.getPluginRegistry().getPluginDescriptors();

		for (int i = 0; i < descriptors.length; i++) {
			if (require(descriptors[i], pluginsToRemove)) {
				Status status = new Status(IStatus.WARNING, uniqueId, IStatus.OK, descriptors[i].getUniqueIdentifier(), null);
				multiStatus.add(status);
			}
		}

		if (multiStatus.getChildren().length > 0 && handler!=null) {
			unconfigure = handler.reportProblem(Policy.bind("ConfigurationPolicy.DoYouWantToUnconfigure"), multiStatus); //$NON-NLS-1$
		}

		if (unconfigure) {
			ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_UNCONFIGURE);
			activity.setLabel(featureReference.getFeature().getVersionedIdentifier().toString());
			activity.setDate(new Date());

			addUnconfiguredFeatureReference((FeatureReferenceModel)featureReference);

			// everything done ok
			activity.setStatus(IActivity.STATUS_OK);
			((InstallConfiguration) SiteManager.getLocalSite().getCurrentConfiguration()).addActivityModel((ConfigurationActivityModel)activity);
			return true;
		}
		
		return false;
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
	 * It will include the include list providing the objects in the include list
	 * are not 'unconfigured' if the type is Exclude or 'configured' if the type is Include
	 */
	/*package*/
	String[] getPluginPath(ISite site,String[] include) throws CoreException {
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
					// obtain the path of the plugin directories on the site	
					ContentReference[] featureContentReference = feature.getFeatureContentProvider().getPluginEntryArchiveReferences(entry, null/*IProgressMonitor*/);
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
		
		// remove from include the plugins that should not be saved 
		String[] toInclude = null;
		if (getPolicy() == IPlatformConfiguration.ISitePolicy.USER_EXCLUDE) {
			if (getConfiguredFeatures() != null)
				toInclude = remove(getConfiguredFeatures(),include);
		} else {
			if (getUnconfiguredFeatures() != null)
				toInclude = remove(getUnconfiguredFeatures(),include);
		}		
		
		// FIXME 
		//result = union(toInclude,result);

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
		List list1 =  new ArrayList();
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

	String[] remove(IFeatureReference[] arrayOfFeatureRef, String[] include){
		if (arrayOfFeatureRef==null || arrayOfFeatureRef.length<1)
			return include;
		
		if (include==null || include.length<1)
			return include;
			
		String[] result = include;
		
		// we need to figure out which plugin SHOULD NOT be written and
		// remove them from include
		// we need to get a URL[] 
		
		return result;
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