package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.internal.boot.update.VersionIdentifier;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.core.*;
import org.eclipse.update.core.IFeature;

/**
 * 
 */
public class ConfigurationSite implements IConfigurationSite, IWritable {

	//Should have an ISite.getFeatureReference(versionnedIdentifier) b/c we save teh versionned id
	// in teh xml file
	private List featuresReferences;

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
	 * @see IConfigurationSite#getConfiguredFeatures()
	 */
	public IFeatureReference[] getConfiguredFeatures() {
		IFeatureReference[] result = new IFeatureReference[0];
		// FIXME:
		if (getConfigurationPolicy().getPolicy()==IPlatformConfiguration.ISitePolicy.USER_INCLUDE){
			result = getConfigurationPolicy().getFilteredFeatures(null);
		}
		return result;
	}

	/*
	 * @see IConfigurationSite#isConfigured(IFeatureReference)
	 */
	public boolean isConfigured(IFeatureReference feature) {
		return false;
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
	 * @see IConfigurationSite#configure(IFeatureReference)
	 */
	public void configure(IFeatureReference feature) {
		// FIXME:
		if (getConfigurationPolicy().getPolicy()==IPlatformConfiguration.ISitePolicy.USER_INCLUDE){
			if (featuresReferences==null) featuresReferences = new ArrayList(0);
			featuresReferences.add(feature);
			((ConfigurationPolicy)getConfigurationPolicy()).addFeatureReference(feature);
		}
	}

	/*
	 * @see IConfigurationSite#unconfigure(IFeatureReference)
	 */
	public void unconfigure(IFeatureReference feature) {
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
		w.println(gap+increment+"url=\""+getSite().getURL().toExternalForm()+"\"");
		w.println(gap+increment+"policy=\"" + getConfigurationPolicy().getPolicy() + "\" ");
		String install = installable ? "true" : "false";
		w.print(gap+increment+"install=\"" + install + "\" ");
		w.println(">");
		w.println("");

		// site configurations
		if (featuresReferences != null) {
			Iterator iter = featuresReferences.iterator();
			while (iter.hasNext()) {
				IFeatureReference element = (IFeatureReference) iter.next();
				w.print(gap+increment+"<"+InstallConfigurationParser.FEATURE+" ");
				// feature URL
				String URLInfoString = null;
				if(element.getURL()!=null) {
					URLInfoString = UpdateManagerUtils.getURLAsString(getSite().getURL(),element.getURL());
					w.print("url=\""+Writer.xmlSafe(URLInfoString)+"\"");
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
		if (!installable){
			//FIXME: throw error
		}
		IFeatureReference installedFeature = getSite().install(feature,monitor);
		configure(installedFeature);
		
	}

}