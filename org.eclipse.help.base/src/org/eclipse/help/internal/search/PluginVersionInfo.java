/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;
import java.io.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.base.util.*;
import org.osgi.framework.*;
/**
 * Table of plugins. Records all plugins, their version, corresponding fragments
 * versions The values are String in format:
 * pluginID\npluginVersion\nfragment1ID\nfragment1Version\nfragment2ID\nfragment2Version
 */
public class PluginVersionInfo extends HelpProperties {
	// Separates plugins and versions in value strings
	static final String SEPARATOR = "\n"; //$NON-NLS-1$
	File dir;
	boolean doComparison = true;
	boolean hasChanged = false;
	boolean ignoreSavedVersions;
	Collection added = new ArrayList();
	Collection removed = new ArrayList();
	/**
	 * Creates table of current contributing plugins and their fragments with
	 * versions.
	 * 
	 * @param name
	 *            the name of the file to serialize the data to
	 * @param docBundleIds
	 *            Collection of String
	 * @param dir
	 *            location to store the data
	 * @param ignoreSavedVersions
	 *            if true, will cause detect change to ignore saved plugin
	 *            version and behave like there was nothing saved
	 */
	public PluginVersionInfo(String name, Collection docBundleIds, File dir,
			boolean ignoreSavedVersions) {
		super(name, dir);
		this.dir = dir;
		this.ignoreSavedVersions = ignoreSavedVersions;
		// create table of current contributions
		for (Iterator it = docBundleIds.iterator(); it.hasNext();) {
			String bundleId = (String) it.next();
			Bundle pluginBundle = Platform.getBundle(bundleId);
			if (pluginBundle == null) {
				continue;
			}
			StringBuffer pluginVersionAndFragments = new StringBuffer();
			pluginVersionAndFragments.append(bundleId);
			pluginVersionAndFragments.append(SEPARATOR);
			pluginVersionAndFragments.append(pluginBundle.getHeaders().get(
					Constants.BUNDLE_VERSION));
			Bundle[] fragmentBundles = Platform.getFragments(pluginBundle);
			if (fragmentBundles != null) {
				for (int f = 0; f < fragmentBundles.length; f++) {
					if (fragmentBundles[f].getState() == Bundle.INSTALLED
							|| fragmentBundles[f].getState() == Bundle.UNINSTALLED)
						continue;
					pluginVersionAndFragments.append(SEPARATOR);
					pluginVersionAndFragments.append(fragmentBundles[f]
							.getSymbolicName());
					pluginVersionAndFragments.append(SEPARATOR);
					pluginVersionAndFragments.append(fragmentBundles[f]
							.getHeaders().get(Constants.BUNDLE_VERSION));
				}
			}
			this.put(bundleId, pluginVersionAndFragments.toString());
		}
	}
	/**
	 * Detects changes in contributions or their version since last time the
	 * contribution table was saved.
	 * 
	 * @return true if contributions have changed
	 */
	public boolean detectChange() {
		if (!doComparison)
			return hasChanged;
		// Create table of contributions present before last save()
		HelpProperties oldContrs = new HelpProperties(this.name, dir);
		if (!ignoreSavedVersions) {
			oldContrs.restore();
		}
		// check if contributions changed
		hasChanged = false;
		for (Enumeration keysEnum = this.keys(); keysEnum.hasMoreElements();) {
			String oneContr = (String) keysEnum.nextElement();
			if (!oldContrs.containsKey(oneContr)) {
				// plugin has been added
				added.add(oneContr);
			} else {
				String versions = (String) this.get(oneContr);
				String oldVersions = (String) oldContrs.get(oneContr);
				if (!compare(versions, oldVersions)) {
					// plugin version changed or fragments changed
					added.add(oneContr);
				}
			}
		}
		for (Enumeration keysEnum = oldContrs.keys(); keysEnum
				.hasMoreElements();) {
			String oneContr = (String) keysEnum.nextElement();
			if (!this.containsKey(oneContr)) {
				// plugin has been removed
				removed.add(oneContr);
			} else {
				String versions = (String) this.get(oneContr);
				String oldVersions = (String) oldContrs.get(oneContr);
				if (!compare(versions, oldVersions)) {
					// plugin version changed or fragments changed
					removed.add(oneContr);
				}
			}
		}
		hasChanged = added.size() > 0 || removed.size() > 0;
		doComparison = false;
		return hasChanged;
	}
	/**
	 * @return String - Collection of IDs of contributions that were added or
	 *         upgraded
	 */
	public Collection getAdded() {
		if (doComparison)
			detectChange();
		return added;
	}
	/**
	 * @return String - Collection of IDs of contributions that were removed or
	 *         upgraded
	 */
	public Collection getRemoved() {
		if (doComparison)
			detectChange();
		return removed;
	}
	/**
	 * Saves contributions to a file. After this method is called, calls to
	 * detectChange() will return false.
	 * 
	 * @return true if operation was successful
	 */
	public boolean save() {
		if (super.save()) {
			doComparison = false;
			hasChanged = false;
			ignoreSavedVersions = false;
			added = new ArrayList();
			removed = new ArrayList();
			return true;
		}
		return false;
	}
	/**
	 * Compares plugins and versions represented as a string for equality String
	 * have form id1\nverison1\nid2\nversion2 String are equal of they contain
	 * the same set of IDs and their corresponding version equal
	 * 
	 * @return true if plugins and versions match
	 */
	private boolean compare(String versions, String oldVersions) {
		Map versionMap = new HashMap();
		for (StringTokenizer t = new StringTokenizer(versions, SEPARATOR, false); t
				.hasMoreTokens();) {
			String pluginOrFragment = t.nextToken();
			if (t.hasMoreTokens()) {
				versionMap.put(pluginOrFragment, t.nextToken());
			}
		}
		Map oldVersionMap = new HashMap();
		for (StringTokenizer t = new StringTokenizer(oldVersions, SEPARATOR,
				false); t.hasMoreTokens();) {
			String pluginOrFragment = t.nextToken();
			if (t.hasMoreTokens()) {
				oldVersionMap.put(pluginOrFragment, t.nextToken());
			}
		}
		return versionMap.equals(oldVersionMap);
	}
}
