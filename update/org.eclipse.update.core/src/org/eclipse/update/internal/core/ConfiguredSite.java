package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.PrintWriter;
import java.util.*;

import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.model.*;
import org.eclipse.update.internal.core.Policy;

/**
 * 
 */
public class ConfiguredSite extends ConfiguredSiteModel implements IConfiguredSite, IWritable {

	private ListenersList listeners = new ListenersList();


	
	/**
 	 * 
 	 */
	public ConfiguredSite() {
	}

	 
	/**
	 * Copy Constructor
	 */
	public ConfiguredSite(IConfiguredSite configSite) {
		setSiteModel((SiteModel)configSite.getSite());
		ConfiguredSite cSite = (ConfiguredSite)configSite;
		setConfigurationPolicyModel(new ConfigurationPolicy(cSite.getConfigurationPolicy()));
		isUpdateable(configSite.isUpdateable());
		setPreviousPluginPath(cSite.getPreviousPluginPath());
		//
		if (configSite instanceof ConfiguredSiteModel){
			ConfiguredSiteModel siteModel = (ConfiguredSiteModel)configSite;
			setPlatformURLString(siteModel.getPlatformURLString());
		} else {
			setPlatformURLString(configSite.getSite().getURL().toExternalForm());
		}
	}

	/*
	 *  @see
	 */
	public void addConfiguredSiteChangedListener(IConfiguredSiteChangedListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
	/*
	 *  @see
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

		w.println(gap + "<" + InstallConfigurationParser.CONFIGURATION_SITE + " "); //$NON-NLS-1$ //$NON-NLS-2$
		w.println(gap + increment + "url=\"" + getSite().getURL().toExternalForm() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		w.println(gap + increment + "platformURL=\"" + getPlatformURLString() + "\"");		 //$NON-NLS-1$ //$NON-NLS-2$
		w.println(gap + increment + "policy=\"" + getConfigurationPolicyModel().getPolicy() + "\" "); //$NON-NLS-1$ //$NON-NLS-2$
		String install = isUpdateable() ? "true" : "false"; //$NON-NLS-1$ //$NON-NLS-2$
		w.print(gap + increment + "install=\"" + install + "\" "); //$NON-NLS-1$ //$NON-NLS-2$
		w.println(">"); //$NON-NLS-1$
		w.println(""); //$NON-NLS-1$

		// configured features ref
		IFeatureReference[] featuresReferences = getConfiguredFeatures();
		if (featuresReferences != null) {
			for (int index = 0; index < featuresReferences.length; index++) {
				IFeatureReference element = featuresReferences[index];
				w.print(gap + increment + "<" + InstallConfigurationParser.FEATURE + " "); //$NON-NLS-1$ //$NON-NLS-2$
				// configured = true
				w.print("configured = \"true\" "); //$NON-NLS-1$
				// feature URL
				String URLInfoString = null;
				if (element.getURL() != null) {
					ISite featureSite = (ISite) ((FeatureReference) element).getSite();
					URLInfoString = UpdateManagerUtils.getURLAsString(featureSite.getURL(), element.getURL());
					w.print("url=\"" + Writer.xmlSafe(URLInfoString) + "\" "); //$NON-NLS-1$ //$NON-NLS-2$
						}
				w.println("/>"); //$NON-NLS-1$
			}
		}

		// unconfigured features ref
		featuresReferences = ((ConfigurationPolicy) getConfigurationPolicyModel()).getUnconfiguredFeatures();
		if (featuresReferences != null) {
			for (int index = 0; index < featuresReferences.length; index++) {
				IFeatureReference element = featuresReferences[index];
				w.print(gap + increment + "<" + InstallConfigurationParser.FEATURE + " "); //$NON-NLS-1$ //$NON-NLS-2$
				// configured = true
				w.print("configured = \"false\" "); //$NON-NLS-1$
				// feature URL
				String URLInfoString = null;
				if (element.getURL() != null) {
					ISite featureSite = element.getSite();
					URLInfoString = UpdateManagerUtils.getURLAsString(featureSite.getURL(), element.getURL());
					w.print("url=\"" + Writer.xmlSafe(URLInfoString) + "\" "); //$NON-NLS-1$ //$NON-NLS-2$
				}
				w.println("/>"); //$NON-NLS-1$
			}
		}

		// end
		w.println(gap + "</" + InstallConfigurationParser.CONFIGURATION_SITE + ">"); //$NON-NLS-1$ //$NON-NLS-2$

	}

	/*
	 * @see IConfiguredSite#install(IFeature,IVerifier, IProgressMonitor)
	 */
	public IFeatureReference install(IFeature feature,IVerificationListener verificationListener, IProgressMonitor monitor) throws CoreException {
		if (!isUpdateable()) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.WARNING, id, IStatus.OK, Policy.bind("ConfiguredSite.NonInstallableSite", getSite().getURL().toExternalForm()), null); //$NON-NLS-1$
			throw new CoreException(status);
		}

		if (feature==null) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.WARNING, id, IStatus.OK, Policy.bind("ConfiguredSite.NullFeatureToInstall"), null); //$NON-NLS-1$
			throw new CoreException(status);
		}

		IFeatureReference installedFeature;
		//Start UOW ?
		ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_FEATURE_INSTALL);
		activity.setLabel(feature.getVersionedIdentifier().toString());
		activity.setDate(new Date());

		try {
			installedFeature = getSite().install(feature, verificationListener,monitor);

			// everything done ok
			activity.setStatus(IActivity.STATUS_OK);

			// notify listeners
			Object[] siteListeners = listeners.getListeners();
			for (int i = 0; i < siteListeners.length; i++) {
				((IConfiguredSiteChangedListener) siteListeners[i]).featureInstalled(installedFeature.getFeature());
			}
		} catch (CoreException e) {
			activity.setStatus(IActivity.STATUS_NOK);
			throw e;
		} finally {
			((InstallConfiguration) SiteManager.getLocalSite().getCurrentConfiguration()).addActivityModel((ConfigurationActivityModel)activity);
		}

		configure(installedFeature.getFeature());

		return installedFeature;
	}

	/*
	 * @see IConfiguredSite#remove(IFeature, IProgressMonitor)
	 */
	public void remove(IFeature feature, IProgressMonitor monitor) throws CoreException {
		if (!isUpdateable()) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.WARNING, id, IStatus.OK, Policy.bind("ConfiguredSite.NonUninstallableSite", getSite().getURL().toExternalForm()), null); //$NON-NLS-1$
			throw new CoreException(status);
		}

		ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_FEATURE_REMOVE);
		activity.setLabel(feature.getVersionedIdentifier().toString());
		activity.setDate(new Date());

		try {
			IFeatureReference referenceToUnconfigure = null;
			FeatureReferenceModel[] featureRef = getSiteModel().getFeatureReferenceModels();
			for (int i = 0; i < featureRef.length; i++) {
				if (featureRef[i].equals(feature)) {
					referenceToUnconfigure = (IFeatureReference) featureRef[i];
					break;
				}
			}
			
			// FIXME didn;t we say we had to first unconfigure then remove ?
			// the UI should check it is unconfigured b4 then ?
			if (referenceToUnconfigure!=null){
			//	unconfigure(referenceToUnconfigure);
			} else {
				//FIXME warn, no reference found for this feature
			}
			((ISite)getSite()).remove(feature, monitor);

			// everything done ok
			activity.setStatus(IActivity.STATUS_OK);

			// notify listeners
			Object[] siteListeners = listeners.getListeners();
			for (int i = 0; i < siteListeners.length; i++) {
				((IConfiguredSiteChangedListener) siteListeners[i]).featureUninstalled(feature);
			}

		} catch (CoreException e) {
			activity.setStatus(IActivity.STATUS_NOK);
			throw e;
		} finally {
			((InstallConfiguration) SiteManager.getLocalSite().getCurrentConfiguration()).addActivityModel((ConfigurationActivityModel)activity);
		}
	}

	/*
	 * @see IConfiguredSite#configure(IFeatureReference)
	 */
	public void configure(IFeature feature) throws CoreException {
		IFeatureReference featureReference = getSite().getFeatureReference(feature);
		((ConfigurationPolicy) getConfigurationPolicyModel()).configure(featureReference);
	}

	/*
	 * @see IConfiguredSite#unconfigure(IFeatureReference)
	 */
	public boolean unconfigure(IFeature feature, IProblemHandler handler) throws CoreException {
		IFeatureReference featureReference = getSite().getFeatureReference(feature);		
		return ((ConfigurationPolicy) getConfigurationPolicyModel()).unconfigure(featureReference,handler);
	}

	/*
	 * @see IConfiguredSite#getConfiguredFeatures()
	 */
	public IFeatureReference[] getConfiguredFeatures() {
		return ((ConfigurationPolicy) getConfigurationPolicyModel()).getConfiguredFeatures();
	}

	/**
	 * process the delta with the configuration site
	 * 
	 */
	/*package*/
	void deltaWith(IConfiguredSite currentConfiguration, IProgressMonitor monitor, IProblemHandler handler) throws CoreException, InterruptedException {

		// copy the plugins as they are transient
		this.setPreviousPluginPath(((ConfiguredSite)currentConfiguration).getPreviousPluginPath());			
		
		// retrieve the feature that were configured
		IFeatureReference[] configuredFeatures = processConfiguredFeatures(handler);

		// we only care about unconfigured features if the Site policy is USER_EXCLUDE
		// calculate all the features we have to unconfigure from the current state to this state
		// in the history. 
		if (getConfigurationPolicyModel().getPolicy() == IPlatformConfiguration.ISitePolicy.USER_EXCLUDE) {
			List featureToUnconfigure = processUnconfiguredFeatures();

			// we have all the unconfigured feature for this site config
			// for the history
			// remove the one that are configured 
			// (may have been unconfigured in the past, but the revert makes them configurd)
			featureToUnconfigure = remove(configuredFeatures, featureToUnconfigure);

			// for each unconfigured feature
			// check if it still exists
			Iterator iter = featureToUnconfigure.iterator();
			while (iter.hasNext()) {
				IFeatureReference element = (IFeatureReference) iter.next();
				try {
					element.getFeature();
					((ConfigurationPolicy) getConfigurationPolicyModel()).addUnconfiguredFeatureReference((FeatureReferenceModel)element);
				} catch (CoreException e) {
					// feature does not exist ?
					featureToUnconfigure.remove(element);
					// log no feature to unconfigure
					if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
						String feature = element.getFeature().getVersionedIdentifier().toString();
						ISite site = element.getFeature().getSite();
						String siteString = (site != null) ? site.getURL().toExternalForm() : Policy.bind("ConfiguredSite.NoSite"); //$NON-NLS-1$
						UpdateManagerPlugin.getPlugin().debug(Policy.bind("ConfiguredSite.CannotFindFeatureToUnconfigure", feature , siteString )); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}
			}
		}

	}

	/**
	 * Method processUnconfiguredFeatures.
	 * @param configuredFeatures
	 * @throws CoreException
	 */
	private List processUnconfiguredFeatures() throws CoreException {

		// but we process teh delta between what was configured in the current
		// configuration that is not configured now
		// we have to figure out what feature have been unconfigure for the whole
		// history between current and us... (based on the date ???)
		//is it as simple as  get all configured, add configured
		// the do teh delat and add to unconfigured
		// what about history ? I have no idea about history...

		List featureToUnconfigure = new ArrayList(0);

		// loop for all history
		// get the history I am interested in
		// try to see if teh site config exists
		// if it does, get the unconfigured features 
		// and the configured one
		IInstallConfiguration[] history = SiteManager.getLocalSite().getConfigurationHistory();
		for (int i = 0; i < history.length; i++) {
			IInstallConfiguration element = history[i];
			IConfiguredSite[] configSites = element.getConfiguredSites();
			for (int j = 0; j < configSites.length; j++) {
				ConfiguredSite configSite = (ConfiguredSite)configSites[j];
				if (configSite.getSite().getURL().equals(((ISite)getSite()).getURL())) {
					featureToUnconfigure.addAll(Arrays.asList(configSite.getConfigurationPolicy().getUnconfiguredFeatures()));
					featureToUnconfigure.addAll(Arrays.asList(configSite.getConfigurationPolicy().getConfiguredFeatures()));
				}
			}
		}
		
		return featureToUnconfigure;
	}

	/**
	 * Method processConfiguredFeatures.
	 * @param handler
	 * @return IFeatureReference[]
	 * @throws CoreException
	 */
	private IFeatureReference[] processConfiguredFeatures(IProblemHandler handler) throws CoreException, InterruptedException {
		// we keep our configured feature
		// check if they are all valid
		IFeatureReference[] configuredFeatures = getConfiguredFeatures();

		if (configuredFeatures != null) {
			for (int i = 0; i < configuredFeatures.length; i++) {
				IFeature feature = null;
				try {
					feature = configuredFeatures[i].getFeature();
				} catch (CoreException e) {
					// notify we cannot find the feature
					UpdateManagerPlugin.getPlugin().getLog().log(e.getStatus());
					String featureString =configuredFeatures[i].getURL().toExternalForm();			
					if (!handler.reportProblem(Policy.bind("ConfiguredSite.CannotFindFeatureToConfigure",featureString))) { //$NON-NLS-1$
						throw new InterruptedException();
					}
				}

				if (feature != null) {
					// get plugin identifier
					List siteIdentifiers = new ArrayList(0);
					ISite site = feature.getSite();
					IPluginEntry[] siteEntries = null;

					if (site != null) {
						siteEntries = site.getPluginEntries();
						for (int index = 0; index < siteEntries.length; index++) {
							IPluginEntry entry = siteEntries[index];
							siteIdentifiers.add(entry.getVersionedIdentifier());
						}
					}

					if (siteEntries != null) {
						IPluginEntry[] entries = feature.getPluginEntries();
						for (int index = 0; index < entries.length; index++) {
							IPluginEntry entry = entries[index];
							if (!contains(entry.getVersionedIdentifier(),siteIdentifiers)) {
								// the plugin defined by the feature
								// doesn't seem to exist on the site
								String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
								IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error verifying existence of plugin:" + entry.getVersionedIdentifier().toString(), null); //$NON-NLS-1$
								UpdateManagerPlugin.getPlugin().getLog().log(status);
								String siteString = (site != null) ? site.getURL().toExternalForm() : Policy.bind("ConfiguredSite.NoSite"); //$NON-NLS-1$
								if (!handler.reportProblem(Policy.bind("ConfiguredSite.CannotFindPluginEntry",entry.getVersionedIdentifier().toString(),siteString))) { //$NON-NLS-1$ //$NON-NLS-2$
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

	/**
	 * Remove an array of feature references
	 * from a list
	 * @param feature
	 * @param list
	 */
	private List remove(IFeatureReference[] featureRefs, List list) {
		
		List result= new ArrayList(0);
				
		if (list==null) return result;
		Iterator iter = list.iterator();
		
		// if an element of the list is not found in the array, add it to the result list
		while (iter.hasNext()) {
			IFeatureReference element = (IFeatureReference) iter.next();
			boolean found = false;
			for (int i = 0; i < featureRefs.length; i++) {
				if (element.getURL().toExternalForm().trim().equalsIgnoreCase(featureRefs[i].getURL().toExternalForm())) {
					found = true;
				}
			}
			if (!found)
				result.add(element);
		}
		
		return result;
	}

	/**
	 * I have issues when running list.contain(versionedIdentifier)
	 * The code runs teh Object.equals instead of teh VersionedIdentifier.equals
	 */
	private boolean contains (VersionedIdentifier id, List list){
		boolean found = false;
		if (list!=null && !list.isEmpty()){
			Iterator iter = list.iterator();
			while (iter.hasNext() && !found) {
				VersionedIdentifier element = (VersionedIdentifier) iter.next();
				if (element.equals(id)){
					found = true;
				}		
			}
		}
		return found;
	}


	/**
	 *
	 */
	public void setConfigurationPolicy(ConfigurationPolicy policy) {
		setConfigurationPolicyModel((ConfigurationPolicyModel)policy);
	}

	/**
	 * 
	 */
	public ConfigurationPolicy getConfigurationPolicy() {
		return (ConfigurationPolicy)getConfigurationPolicyModel();
	}

	/*
	 * @see IConfiguredSite#getSite()
	 */
	public ISite getSite() {
		return (ISite)getSiteModel();
	}

	/*
	 * @see IConfiguredSite#getInstallConfiguration()
	 */
	public IInstallConfiguration getInstallConfiguration() {
		return (IInstallConfiguration)getInstallConfigurationModel();
	}

	
	
	
	
	/*
	 * @see IConfiguredSite#isBroken(IFeature)
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
				listOfMissingPlugins = "\r\nplugin:" + missing[k].getVersionedIdentifier().toString(); //$NON-NLS-1$
			}
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			String featureString = (feature==null)?null:feature.getURL().toExternalForm();
			String siteString = (currentSite==null)?null:currentSite.getURL().toExternalForm();
			String[] values = new String[]{featureString,siteString,listOfMissingPlugins};
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, Policy.bind("ConfiguredSite.MissingPluginsBrokenFeature",values), null); //$NON-NLS-1$ 
			UpdateManagerPlugin.getPlugin().getLog().log(status);
			return true;
		}
		return false;
	}



}