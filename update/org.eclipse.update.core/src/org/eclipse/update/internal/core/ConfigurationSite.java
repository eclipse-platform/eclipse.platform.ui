package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.internal.boot.update.VersionIdentifier;
import org.eclipse.update.core.*;

/**
 * 
 */
public class ConfigurationSite implements IConfigurationSite, IWritable {

	//Should have an ISite.getFeatureReference(versionnedIdentifier) b/c we save teh versionned id
	// in teh xml file
	private List features;

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
		return null;
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
		if (features != null) {
			Iterator iter = features.iterator();
			while (iter.hasNext()) {
				VersionedIdentifier element = (VersionedIdentifier) iter.next();
				w.print(gap + increment + "<" + InstallConfigurationParser.FEATURE + " ");
				w.print("id=\"" + Writer.xmlSafe(element.getIdentifier()) + "\" ");
				w.print("ver=\"" + Writer.xmlSafe(element.getVersion().toString()) + "\" ");
				w.println("/>");
				w.println("");

				((IWritable) element).write(indent + IWritable.INDENT, w);
			}
		}

		// end
		w.println(gap + "</" + InstallConfigurationParser.CONFIGURATION_SITE + ">");

	}

}