package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.PrintWriter;
import java.util.Date;

import java.util.List;
import java.util.*;
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
	private List configuredFeatures;

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
	public void install(IFeature feature, IProgressMonitor monitor) throws CoreException {
		if (!installable) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.WARNING, id, IStatus.OK, "The site is not considered to be installable:" + site.getURL().toExternalForm(), null);
			throw new CoreException(status);
		}

		//Start UOW ?
		ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_FEATURE_INSTALL);
		activity.setLabel(feature.getIdentifier().toString());
		activity.setDate(new Date());

		try {
			IFeatureReference installedFeature = getSite().install(feature, monitor);
			configure(installedFeature);

			// everything done ok
			activity.setStatus(IActivity.STATUS_OK);

		} catch (CoreException e) {
			activity.setStatus(IActivity.STATUS_NOK);
			throw e;
		} finally {
			((InstallConfiguration) SiteManager.getLocalSite().getCurrentConfiguration()).addActivity(activity);
		}

	}

	/*
	 * @see IConfigurationSite#configure(IFeatureReference)
	 */
	public void configure(IFeatureReference feature) throws CoreException {
		
		//Start UOW ?
		ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_CONFIGURE);
		activity.setLabel(feature.getURL().toExternalForm());
		activity.setDate(new Date());
			
		addFeatureReference(feature);
		((ConfigurationPolicy)getConfigurationPolicy()).configure(feature);		

		// everything done ok
		activity.setStatus(IActivity.STATUS_OK);
		((InstallConfiguration) SiteManager.getLocalSite().getCurrentConfiguration()).addActivity(activity);
	}

	/**
	 * adds a feature in teh list
	 * also used by the parser to avoid creating another activity
	 */
	void addFeatureReference(IFeatureReference feature) {
		if (configuredFeatures==null) configuredFeatures = new ArrayList(0);
		configuredFeatures.add(feature);
	}

	/*
	 * @see IConfigurationSite#unconfigure(IFeatureReference)
	 */
	public void unconfigure(IFeatureReference feature) throws CoreException {
		if (configuredFeatures!=null){
			
				//Start UOW ?
			ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_UNCONFIGURE);
			activity.setLabel(feature.getURL().toExternalForm());
			activity.setDate(new Date());
			configuredFeatures.remove(feature);
			((ConfigurationPolicy)getConfigurationPolicy()).unconfigure(feature);

			// everything done ok
			activity.setStatus(IActivity.STATUS_OK);
			((InstallConfiguration) SiteManager.getLocalSite().getCurrentConfiguration()).addActivity(activity);
		}
	}

	/*
	 * @see IConfigurationSite#getConfiguredFeatures()
	 */
	public IFeatureReference[] getConfiguredFeatures() {
		IFeatureReference[] result = new IFeatureReference[0];
		if (configuredFeatures!=null && !configuredFeatures.isEmpty()){
			result = new IFeatureReference[configuredFeatures.size()];
			configuredFeatures.toArray(result);
		}
		
		return result;
	}

	/**
	 * process the delta with the configuration site
	 */
	/*package*/
	void deltaWith(IConfigurationSite currentConfiguration){
		// we keep our configured feature
		// we remove the unconfigured one
		// but we process teh delat between what was configured in the current
		// configuration that is not configured now
		
		//is it as simple as  get all configured, add configured
		// the do teh delat and add to unconfigured
		// what about history ? I have no idea about history...
		
		
	}

}