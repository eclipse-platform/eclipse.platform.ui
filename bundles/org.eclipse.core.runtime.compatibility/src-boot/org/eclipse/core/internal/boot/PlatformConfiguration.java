/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.boot;

import java.io.IOException;
import java.net.URL;
import org.eclipse.core.boot.IPlatformConfiguration;

public class PlatformConfiguration implements IPlatformConfiguration {
	private org.eclipse.update.configurator.IPlatformConfiguration newConfig;

	public PlatformConfiguration(org.eclipse.update.configurator.IPlatformConfiguration config) {
		newConfig = config;
	}

	public ISiteEntry createSiteEntry(URL url, ISitePolicy policy) {
		return new SiteEntry(newConfig.createSiteEntry(url, ((SitePolicy) policy).getNewPolicy()));
	}

	public ISitePolicy createSitePolicy(int type, String[] list) {
		return new SitePolicy(newConfig.createSitePolicy(type, list));
	}

	public IFeatureEntry createFeatureEntry(String id, String version, String pluginVersion, boolean primary, String application, URL[] root) {
		return new FeatureEntry(newConfig.createFeatureEntry(id, version, pluginVersion, primary, application, root));
	}

	public IFeatureEntry createFeatureEntry(String id, String version, String pluginIdentifier, String pluginVersion, boolean primary, String application, URL[] root) {
		return new FeatureEntry(newConfig.createFeatureEntry(id, version, pluginIdentifier, pluginVersion, primary, application, root));
	}

	public void configureSite(ISiteEntry entry) {
		newConfig.configureSite(((SiteEntry) entry).getNewSiteEntry());
	}

	public void configureSite(ISiteEntry entry, boolean replace) {
		newConfig.configureSite(((SiteEntry) entry).getNewSiteEntry(), replace);
	}

	public void unconfigureSite(ISiteEntry entry) {
		newConfig.unconfigureSite(((SiteEntry) entry).getNewSiteEntry());
	}

	public ISiteEntry[] getConfiguredSites() {
		org.eclipse.update.configurator.IPlatformConfiguration.ISiteEntry[] sites = newConfig.getConfiguredSites();
		SiteEntry[] oldSites = new SiteEntry[sites.length];
		for (int i = 0; i < sites.length; i++)
			oldSites[i] = new SiteEntry(sites[i]);
		return oldSites;
	}

	public ISiteEntry findConfiguredSite(URL url) {
		org.eclipse.update.configurator.IPlatformConfiguration.ISiteEntry siteEntry = newConfig.findConfiguredSite(url);
		if (siteEntry == null)
			return null;
		return new SiteEntry(siteEntry);
	}

	public void configureFeatureEntry(IFeatureEntry entry) {
		newConfig.configureFeatureEntry(((FeatureEntry) entry).getNewFeatureEntry());
	}

	public void unconfigureFeatureEntry(IFeatureEntry entry) {
		newConfig.unconfigureFeatureEntry(((FeatureEntry) entry).getNewFeatureEntry());
	}

	public IFeatureEntry[] getConfiguredFeatureEntries() {
		org.eclipse.update.configurator.IPlatformConfiguration.IFeatureEntry[] entries = newConfig.getConfiguredFeatureEntries();
		FeatureEntry[] oldEntries = new FeatureEntry[entries.length];
		for (int i = 0; i < entries.length; i++)
			oldEntries[i] = new FeatureEntry(entries[i]);
		return oldEntries;
	}

	public IFeatureEntry findConfiguredFeatureEntry(String id) {
		return new FeatureEntry(newConfig.findConfiguredFeatureEntry(id));
	}

	public URL getConfigurationLocation() {
		return newConfig.getConfigurationLocation();
	}

	public long getChangeStamp() {
		return newConfig.getChangeStamp();
	}

	public long getFeaturesChangeStamp() {
		return newConfig.getFeaturesChangeStamp();
	}

	public long getPluginsChangeStamp() {
		return newConfig.getPluginsChangeStamp();
	}

	public String getPrimaryFeatureIdentifier() {
		return newConfig.getPrimaryFeatureIdentifier();
	}

	public URL[] getPluginPath() {
		return newConfig.getPluginPath();
	}

	public String[] getBootstrapPluginIdentifiers() {
		return newConfig.getBootstrapPluginIdentifiers();
	}

	public void setBootstrapPluginLocation(String id, URL location) {
		newConfig.setBootstrapPluginLocation(id, location);
	}

	public boolean isUpdateable() {
		return newConfig.isUpdateable();
	}

	public boolean isTransient() {
		return newConfig.isTransient();
	}

	public void isTransient(boolean value) {
		newConfig.isTransient(value);
	}

	public void refresh() {
		newConfig.refresh();
	}

	public void save() throws IOException {
		newConfig.save();
	}

	public void save(URL url) throws IOException {
		newConfig.save(url);
	}

	public boolean equals(Object o) {
		if (o instanceof PlatformConfiguration)
			return newConfig.equals(((PlatformConfiguration) o).newConfig);
		return false;
	}

	public int hashCode() {
		return newConfig.hashCode();
	}
}
