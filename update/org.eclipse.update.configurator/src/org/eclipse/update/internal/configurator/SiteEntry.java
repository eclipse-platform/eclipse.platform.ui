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
package org.eclipse.update.internal.configurator;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.internal.boot.*;
import org.eclipse.update.configurator.*;
import org.eclipse.update.configurator.IPlatformConfiguration.*;


public class SiteEntry implements IPlatformConfiguration.ISiteEntry {
	private static final String PLUGINS = "plugins"; //$NON-NLS-1$
	private static final String FEATURES = "features"; //$NON-NLS-1$
	private static final String PLUGIN_XML = "plugin.xml"; //$NON-NLS-1$
	private static final String FRAGMENT_XML = "fragment.xml"; //$NON-NLS-1$
	private static final String FEATURE_XML = "feature.xml"; //$NON-NLS-1$
	
	private URL url; // this is the external URL for the site
	private URL resolvedURL; // this is the resolved URL used internally
	private ISitePolicy policy;
	private boolean updateable = true;
	private ArrayList featureEntries = new ArrayList();
	private ArrayList pluginEntries = new ArrayList();
	private ArrayList features;
	private ArrayList plugins;
	private PlatformConfiguration parent;
	private long changeStamp;
	private boolean changeStampIsValid;
	private long lastFeaturesChangeStamp;
	private long featuresChangeStamp;
	private boolean featuresChangeStampIsValid;
	private long lastPluginsChangeStamp;
	private long pluginsChangeStamp;
	private boolean pluginsChangeStampIsValid;
	private String linkFileName;

	public SiteEntry() {
	}
	public SiteEntry(URL url, ISitePolicy policy, PlatformConfiguration parent) {
		if (url == null)
			throw new IllegalArgumentException();

		if (policy == null)
			throw new IllegalArgumentException();

		if (parent == null)
			throw new IllegalArgumentException();

		this.url = url;
		this.policy = policy;
		this.parent = parent;
		this.features = null;
		this.plugins = null;
		this.resolvedURL = this.url;
		if (url.getProtocol().equals(PlatformURLHandler.PROTOCOL)) {
			try {
				resolvedURL = PlatformConfiguration.resolvePlatformURL(url); // 19536
			} catch (IOException e) {
				// will use the baseline URL ...
			}
		}
	}

	/*
	 * @see ISiteEntry#getURL()
	 */
	public URL getURL() {
		return url;
	}

	/*
	* @see ISiteEntry#getSitePolicy()
	*/
	public ISitePolicy getSitePolicy() {
		return policy;
	}

	/*
	 * @see ISiteEntry#setSitePolicy(ISitePolicy)
	 */
	public synchronized void setSitePolicy(ISitePolicy policy) {
		if (policy == null)
			throw new IllegalArgumentException();
		this.policy = policy;
	}

	/*
	 * @see ISiteEntry#getFeatures()
	 */
	public String[] getFeatures() {
		return getDetectedFeatures();
	}

	/*
	 * @see ISiteEntry#getPlugins()
	 */
	public String[] getPlugins() {

		ISitePolicy policy = getSitePolicy();

		if (policy.getType() == ISitePolicy.USER_INCLUDE)
			return policy.getList();

		if (policy.getType() == ISitePolicy.USER_EXCLUDE) {
			ArrayList detectedPlugins = new ArrayList(Arrays.asList(getDetectedPlugins()));
			String[] excludedPlugins = policy.getList();
			for (int i = 0; i < excludedPlugins.length; i++) {
				if (detectedPlugins.contains(excludedPlugins[i]))
					detectedPlugins.remove(excludedPlugins[i]);
			}
			return (String[]) detectedPlugins.toArray(new String[0]);
		}

		// bad policy type
		return new String[0];
	}

	/*
	 * @see ISiteEntry#getChangeStamp()
	 */
	public long getChangeStamp() {
		if (!changeStampIsValid)
			computeChangeStamp();
		return changeStamp;
	}

	/*
	 * @see ISiteEntry#getFeaturesChangeStamp()
	 */
	public long getFeaturesChangeStamp() {
		if (!featuresChangeStampIsValid)
			computeFeaturesChangeStamp();
		return featuresChangeStamp;
	}

	/*
	 * @see ISiteEntry#getPluginsChangeStamp()
	 */
	public long getPluginsChangeStamp() {
		if (!pluginsChangeStampIsValid)
			computePluginsChangeStamp();
		return pluginsChangeStamp;
	}

	/*
	 * @see ISiteEntry#isUpdateable()
	 */
	public boolean isUpdateable() {
		return updateable;
	}
	
	public void setUpdateable(boolean updateable) {
		this.updateable = updateable;
	}

	/*
	 * @see ISiteEntry#isNativelyLinked()
	 */
	public boolean isNativelyLinked() {
		return isExternallyLinkedSite();
	}

	public URL getResolvedURL() {
		return resolvedURL;
	}
	
	private String[] detectFeatures() {

		// invalidate stamps ... we are doing discovery
		invalidateFeaturesChangeStamp();

		features = new ArrayList();

		if (!PlatformConfiguration.supportsDetection(resolvedURL))
			return new String[0];

		// locate feature entries on site
		File siteRoot = new File(resolvedURL.getFile().replace('/', File.separatorChar));
		File root = new File(siteRoot, FEATURES);

		String[] list = root.list();
		String path;
		File plugin;
		for (int i = 0; list != null && i < list.length; i++) {
			path = list[i] + File.separator + FEATURE_XML;
			plugin = new File(root, path);
			if (!plugin.exists()) {
				continue;
			}
			features.add(FEATURES + "/" + path.replace(File.separatorChar, '/')); //$NON-NLS-1$
		}
		Utils.debug(resolvedURL.toString() + " located  " + features.size() + " feature(s)"); //$NON-NLS-1$ //$NON-NLS-2$

		return (String[]) features.toArray(new String[0]);
	}

	private String[] detectPlugins() {

		// invalidate stamps ... we are doing discovery
		invalidatePluginsChangeStamp();

		plugins = new ArrayList();

		if (!PlatformConfiguration.supportsDetection(resolvedURL))
			return new String[0];

		// locate plugin entries on site
		File root = new File(resolvedURL.getFile().replace('/', File.separatorChar) + PLUGINS);
		String[] list = root.list();
		String path;
		File plugin;
		for (int i = 0; list != null && i < list.length; i++) {
			path = list[i] + File.separator + PLUGIN_XML;
			plugin = new File(root, path);
			if (!plugin.exists()) {
				path = list[i] + File.separator + FRAGMENT_XML;
				plugin = new File(root, path);
				if (!plugin.exists())
					continue;
			}
			plugins.add(PLUGINS + "/" + path.replace(File.separatorChar, '/')); //$NON-NLS-1$
		}
		Utils.debug(resolvedURL.toString() + " located  " + plugins.size() + " plugin(s)"); //$NON-NLS-1$ //$NON-NLS-2$

		return (String[]) plugins.toArray(new String[0]);
	}

	private synchronized String[] getDetectedFeatures() {
		if (features == null)
			return detectFeatures();
		else
			return (String[]) features.toArray(new String[0]);
	}

	private synchronized String[] getDetectedPlugins() {
		if (plugins == null)
			return detectPlugins();
		else
			return (String[]) plugins.toArray(new String[0]);
	}

	private void computeChangeStamp() {
		computeFeaturesChangeStamp();
		computePluginsChangeStamp();
		changeStamp = resolvedURL.hashCode() ^ featuresChangeStamp ^ pluginsChangeStamp;
		changeStampIsValid = true;
	}

	private synchronized void computeFeaturesChangeStamp() {
		if (featuresChangeStampIsValid)
			return;

		long start = 0;
		if (ConfigurationActivator.DEBUG)
			start = (new Date()).getTime();
		String[] features = getFeatures();
		featuresChangeStamp = computeStamp(features);
		featuresChangeStampIsValid = true;
		if (ConfigurationActivator.DEBUG) {
			long end = (new Date()).getTime();
			Utils.debug(resolvedURL.toString() + " feature stamp: " + featuresChangeStamp + ((featuresChangeStamp == lastFeaturesChangeStamp) ? " [no changes]" : " [was " + lastFeaturesChangeStamp + "]") + " in " + (end - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		}
	}

	private synchronized void computePluginsChangeStamp() {
		if (pluginsChangeStampIsValid)
			return;

		long start = 0;
		if (ConfigurationActivator.DEBUG)
			start = (new Date()).getTime();
		String[] plugins = getPlugins();
		pluginsChangeStamp = computeStamp(plugins);
		pluginsChangeStampIsValid = true;
		if (ConfigurationActivator.DEBUG) {
			long end = (new Date()).getTime();
			Utils.debug(resolvedURL.toString() + " plugin stamp: " + pluginsChangeStamp + ((pluginsChangeStamp == lastPluginsChangeStamp) ? " [no changes]" : " [was " + lastPluginsChangeStamp + "]") + " in " + (end - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		}
	}

	private long computeStamp(String[] targets) {

		long result = 0;
		if (!PlatformConfiguration.supportsDetection(resolvedURL)) {
			// NOTE:  this path should not be executed until we support running
			//        from an arbitrary URL (in particular from http server). For
			//        now just compute stamp across the list of names. Eventually
			//        when general URLs are supported we need to do better (factor
			//        in at least the existence of the target). However, given this
			//        code executes early on the startup sequence we need to be
			//        extremely mindful of performance issues.
			for (int i = 0; i < targets.length; i++)
				result ^= targets[i].hashCode();
			Utils.debug("*WARNING* computing stamp using URL hashcodes only"); //$NON-NLS-1$
		} else {
			// compute stamp across local targets
			String rootPath = resolvedURL.getFile().replace('/', File.separatorChar);
			if (!rootPath.endsWith(File.separator))
				rootPath += File.separator;
			File rootFile = new File(rootPath);
			if (rootFile.exists()) {
				File f = null;
				for (int i = 0; i < targets.length; i++) {
					f = new File(rootFile, targets[i]);
					if (f.exists())
						result ^= f.getAbsolutePath().hashCode() ^ f.lastModified() ^ f.length();
				}
			}
		}

		return result;
	}
	
	public void setLinkFileName(String linkFileName) {
		this.linkFileName = linkFileName;
	}
	
	public String getLinkFileName() {
		return linkFileName;
	}

	public boolean isExternallyLinkedSite() {
		return (linkFileName != null && !linkFileName.trim().equals("")); //$NON-NLS-1$
	}

	public synchronized void refresh() {
		// reset computed values. Will be updated on next access.
		lastFeaturesChangeStamp = featuresChangeStamp;
		lastPluginsChangeStamp = pluginsChangeStamp;
		changeStampIsValid = false;
		featuresChangeStampIsValid = false;
		pluginsChangeStampIsValid = false;
		features = null;
		plugins = null;
	}

	public void setLastFeaturesChangeStamp(long stamp) {
		this.lastFeaturesChangeStamp = stamp;
	}
	
	public void setLastPluginsChangeStamp(long stamp) {
		this.lastPluginsChangeStamp = stamp;
	}
	
	public void invalidateFeaturesChangeStamp() {
		changeStampIsValid = false;
		featuresChangeStampIsValid = false;
		parent.invalidateFeaturesChangeStamp();
	}
	
	public void invalidatePluginsChangeStamp() {
		changeStampIsValid = false;
		pluginsChangeStampIsValid = false;
		parent.invalidatePluginsChangeStamp();
	}
	
	public void addFeatureEntry(IFeatureEntry feature) {
		featureEntries.add(feature);
	}
	
	public IFeatureEntry[] getFeatureEntries() {
		return (IFeatureEntry[])featureEntries.toArray(new IFeatureEntry[featureEntries.size()]);
	}
	
	public void addPluginEntry(PluginEntry plugin) {
		pluginEntries.add(plugin);
	}
	
	public PluginEntry[] getPluginEntries() {
		return (PluginEntry[])pluginEntries.toArray(new PluginEntry[pluginEntries.size()]);
	}
}