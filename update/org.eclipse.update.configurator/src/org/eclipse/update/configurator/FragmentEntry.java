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
package org.eclipse.update.configurator;

import org.eclipse.core.runtime.*;
import org.eclipse.update.internal.configurator.*;
import org.osgi.framework.*;


/**
 *<em> This is a temporary class and it will be removed later. It is used to retrieve the
 * fragments associated with a plugin. Since it uses osgi.framework classes we had to 
 * keep in this bundle. Later, this code will be moved elsewhere.</em>
 */
public class FragmentEntry {

	private static FragmentEntry[] noFragments = new FragmentEntry[0];
	
	private String pluginId;
	private String pluginVersion;
	private String name; 
	private String location;
	private boolean isFragment = false;
	
	public FragmentEntry() {
		super();
	}

	/**
	 * @return the plugin translatable name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the location of the plugin
	 */
	public String getLocation() {
		return location;
	}
	
	/**
	 * Returns the plug-in identifier for this entry.
	 * 
	 * @return the plug-in identifier, or <code>null</code>
	 */
	public String getPluginIdentifier() {
		return pluginId;
	}

	/**
	 * Returns the plug-in version for this entry.
	 * 
	 * @return the plug-in version, or <code>null</code>
	 */
	public String getPluginVersion() {
		return pluginVersion;
	}

	/**
	 * Indicates whether the entry describes a full plug-in, or 
	 * a plug-in fragment.
	 * 
	 * @return <code>true</code> if the entry is a plug-in fragment, 
	 * <code>false</code> if the entry is a plug-in
	 */
	public boolean isFragment() {
		return isFragment;
	}

	/**
	 * Sets the entry plug-in identifier.
	 * Throws a runtime exception if this object is marked read-only.
	 *
	 * @param pluginId the entry identifier.
	 */
	void setPluginIdentifier(String pluginId) {
		this.pluginId = pluginId;
	}

	/**
	 * Sets the entry plug-in version.
	 * Throws a runtime exception if this object is marked read-only.
	 *
	 * @param pluginVersion the entry version.
	 */
	void setPluginVersion(String pluginVersion) {
		this.pluginVersion = pluginVersion;
	}

	/**
	 * Indicates whether this entry represents a fragment or plug-in.
	 * Throws a runtime exception if this object is marked read-only.
	 *
	 * @param isFragment fragment setting
	 */
	public void isFragment(boolean isFragment) {
		this.isFragment = isFragment;
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		String msg = (getPluginIdentifier()!=null)?getPluginIdentifier().toString():"";
		msg += getPluginVersion()!=null?" "+getPluginVersion().toString():"";
		msg += isFragment()?" fragment":" plugin";
		return msg;
	}

	
	/**
	 * Returns a list of fragments. Zero length if no fragments.
	 * @param id the id of the plugin to get fragments for
	 */
	public static FragmentEntry[] getFragments(String id) {
		BundleContext context = ConfigurationActivator.getBundleContext();
		Bundle[] bundles = context.getBundles(id);
		if (bundles != null && bundles.length > 0) {
			Bundle[] fragmentBundles = bundles[0].getFragments();
			if (fragmentBundles != null) {
				FragmentEntry[] fragments = new FragmentEntry[fragmentBundles.length];
				for (int i=0; i<fragments.length; i++) {
					fragments[i] = new FragmentEntry();
					fragments[i].isFragment = true;
					fragments[i].location = fragmentBundles[i].getLocation();
					fragments[i].pluginId = (String)fragmentBundles[i].getHeaders().get(Constants.BUNDLE_GLOBALNAME);
					fragments[i].pluginVersion = (String)fragmentBundles[i].getHeaders().get(Constants.BUNDLE_VERSION);
					fragments[i].name = Platform.getResourceString(
							fragmentBundles[i],
							(String) fragmentBundles[i].getHeaders().get(
									Constants.BUNDLE_VERSION));
				}
				return fragments;
			} else
				return noFragments;
		} else {
			return noFragments;
		}	
	}
}