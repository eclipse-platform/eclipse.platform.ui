package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.PrintWriter;
import java.util.Date;

import java.util.List;
import java.util.*;

import java.util.*;
import java.util.*;

import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.*;
import org.eclipse.update.core.IFeatureReference;

/**
 * 
 */
public class ConfigurationSite implements IConfigurationSite, IWritable {

	private ISite site;
	private IConfigurationPolicy policy;
	private boolean installable = false;
	/**
	 * Constructor
	 */
	public ConfigurationSite(ISite site, IConfigurationPolicy policy) {
		this.site = site;
		this.policy = policy;
	}

	/*
	 * @see IConfigurationSite#getSite()
	 */
	public ISite getSite() {
		return site;
	}

	/*
	 * @see IConfigurationSite#getPolicy()
	 */
	public IConfigurationPolicy getConfigurationPolicy() {
		return policy;
	}

	/*
	 * @see IConfigurationSite#setPolicy(IConfigurationPolicy)
	 */
	public void setConfigurationPolicy(IConfigurationPolicy policy) {
		this.policy = policy;
	}

	/*
	 * @see IConfigurationSite#isInstallSite()
	 */
	public boolean isInstallSite() {
		return installable;
	}

	/*
	 * @see IConfigurationSite#setInstallSite(booelan)
	 */
	public void setInstallSite(boolean installable) {
		this.installable = installable;
	}

	/*
	 * @see IWritable#write(int, PrintWriter)
	 */
	public void write(int indent, PrintWriter w) {

		String gap = "";
		for (int i = 0; i < indent; i++)
			gap += " ";
		String increment = "";
		for (int i = 0; i < IWritable.INDENT; i++)
			increment += " ";

		w.println(gap + "<" + InstallConfigurationParser.CONFIGURATION_SITE + " ");
		w.println(gap + increment + "url=\"" + getSite().getURL().toExternalForm() + "\"");
		w.println(gap + increment + "policy=\"" + getConfigurationPolicy().getPolicy() + "\" ");
		String install = installable ? "true" : "false";
		w.print(gap + increment + "install=\"" + install + "\" ");
		w.println(">");
		w.println("");

		// configured features ref
		IFeatureReference[] featuresReferences = getConfiguredFeatures();
		if (featuresReferences != null) {
			for (int index = 0; index < featuresReferences.length; index++) {
				IFeatureReference element = featuresReferences[index];
				w.print(gap + increment + "<" + InstallConfigurationParser.FEATURE + " ");
				// configured = true
				w.print("configured = \"true\" ");
				// feature URL
				String URLInfoString = null;
				if (element.getURL() != null) {
					ISite featureSite = ((FeatureReference) element).getSite();
					URLInfoString = UpdateManagerUtils.getURLAsString(featureSite.getURL(), element.getURL());
					w.print("url=\"" + Writer.xmlSafe(URLInfoString) + "\"");
				}
				w.println("/>");
			}
		}

		// unconfigured features ref
		featuresReferences = ((ConfigurationPolicy)getConfigurationPolicy()).getUnconfiguredFeatures();
		if (featuresReferences != null) {
			for (int index = 0; index < featuresReferences.length; index++) {
				IFeatureReference element = featuresReferences[index];
				w.print(gap + increment + "<" + InstallConfigurationParser.FEATURE + " ");
				// configured = true
				w.print("configured = \"false\" ");
				// feature URL
				String URLInfoString = null;
				if (element.getURL() != null) {
					ISite featureSite = ((FeatureReference) element).getSite();
					URLInfoString = UpdateManagerUtils.getURLAsString(featureSite.getURL(), element.getURL());
					w.print("url=\"" + Writer.xmlSafe(URLInfoString) + "\"");
				}
				w.println("/>");
			}
		}


		// end
		w.println(gap + "</" + InstallConfigurationParser.CONFIGURATION_SITE + ">");

	}

	/*
	 * @see IConfigurationSite#install(IFeature, IProgressMonitor)
	 */
	public IFeatureReference install(IFeature feature, IProgressMonitor monitor) throws CoreException {
		if (!installable) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.WARNING, id, IStatus.OK, "The site is not considered to be installable:" + site.getURL().toExternalForm(), null);
			throw new CoreException(status);
		}

		IFeatureReference installedFeature;
		//Start UOW ?
		ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_FEATURE_INSTALL);
		activity.setLabel(feature.getIdentifier().toString());
		activity.setDate(new Date());

		try {
			installedFeature = getSite().install(feature, monitor);
			configure(installedFeature);

			// everything done ok
			activity.setStatus(IActivity.STATUS_OK);

		} catch (CoreException e) {
			activity.setStatus(IActivity.STATUS_NOK);
			throw e;
		} finally {
			((InstallConfiguration) SiteManager.getLocalSite().getCurrentConfiguration()).addActivity(activity);
		}

		return installedFeature;
	}

	/*
	 * @see IConfigurationSite#configure(IFeatureReference)
	 */
	public void configure(IFeatureReference feature) throws CoreException {
		((ConfigurationPolicy)getConfigurationPolicy()).configure(feature);
	}

	
	/*
	 * @see IConfigurationSite#unconfigure(IFeatureReference)
	 */
	public void unconfigure(IFeatureReference feature) throws CoreException {
		((ConfigurationPolicy)getConfigurationPolicy()).unconfigure(feature);
	}

	/*
	 * @see IConfigurationSite#getConfiguredFeatures()
	 */
	public IFeatureReference[] getConfiguredFeatures() {
		return getConfigurationPolicy().getConfiguredFeatures();
	}

	/**
	 * process the delta with the configuration site
	 */
	/*package*/
	void deltaWith(IConfigurationSite currentConfiguration, IProgressMonitor monitor) throws CoreException {
		// we keep our configured feature
		// check if they are all valid
		IFeatureReference[] configuredFeatures = getConfiguredFeatures();
		if (configuredFeatures!=null){
			for (int i=0; i<configuredFeatures.length; i++) {
				IFeature feature = null;
				try {
					feature = configuredFeatures[i].getFeature();
				} catch (CoreException e){
					//FIXME notify we cannot find the feature
				}
				
				
				if (feature!=null){
					// get plugin identifier
					List siteIdentifiers = new ArrayList(0);
					IPluginEntry[] siteEntries = feature.getSite().getPluginEntries();
					for (int index = 0; index < siteEntries.length; index++) {
						IPluginEntry entry = siteEntries[index];
						siteIdentifiers.add(entry.getIdentifier());										
					}				
					
						
					if (siteEntries!=null){
						IPluginEntry[] entries = feature.getPluginEntries();
							for (int index = 0; index < entries.length; index++) {
								IPluginEntry entry = entries[index];
								if (!siteIdentifiers.contains(entry.getIdentifier())){
									// FIXME: teh plugin defined by teh feature
									// doesn't see to exist on the site
								}
						}
					}
				}
			}
		}
		
		if (getConfigurationPolicy().getPolicy()==IPlatformConfiguration.ISitePolicy.USER_EXCLUDE){
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
			IInstallConfiguration[] history = SiteManager.getLocalSite().getConfigurationHistory();
			for (int i = 0; i < history.length; i++) {
				IInstallConfiguration element = history[i];
				IConfigurationSite[] configSites = element.getConfigurationSites();
				for (int j = 0; j < configSites.length; j++) {
					IConfigurationSite configSite = configSites[j];
					if (configSite.getSite().getURL().equals(getSite().getURL())) {
						featureToUnconfigure.addAll(Arrays.asList(configSite.getConfigurationPolicy().getUnconfiguredFeatures()));
					}
				}	
			}
					
			// ok
			// we have all teh unconfigured feature for this site config
			// for the history
			// remove the one that are configured 
			for (int i=0; i<configuredFeatures.length; i++) {
				remove(configuredFeatures[i],featureToUnconfigure);				
			}			
			
			// for each unconfigured feature
			// check if it still exists
			Iterator iter = featureToUnconfigure.iterator();
			while (iter.hasNext()) {
				IFeatureReference element = (IFeatureReference) iter.next();
				try {
					element.getFeature();
					((ConfigurationPolicy)getConfigurationPolicy()).unconfigure(element);
				} catch (CoreException e){
					// feature does not exist ?
					featureToUnconfigure.remove(element);
				}
			}	
		}		
		
	}

private void remove(IFeatureReference feature, List list){
		String featureURLString = feature.getURL().toExternalForm();
		boolean found = false;
		Iterator iter = list.iterator();
		while (iter.hasNext() && !found) {
			IFeatureReference element = (IFeatureReference) iter.next();
				if (element.getURL().toExternalForm().trim().equalsIgnoreCase(featureURLString)) {
					list.remove(element);
					found = true;
			}
		}
	}

}