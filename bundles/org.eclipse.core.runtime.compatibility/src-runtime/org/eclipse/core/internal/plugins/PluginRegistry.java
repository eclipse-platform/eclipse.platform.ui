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

package org.eclipse.core.internal.plugins;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.osgi.framework.Bundle;

//TODO 1: this class must be made thread safe (query methods must be sync'd as well)
//TODO 2: only resolved bundles should appear in the plugin registry - this implementation exposes all known bundles
public class PluginRegistry implements IPluginRegistry {
	private IExtensionRegistry extRegistry;

	private HashMap descriptors = new HashMap();

	public PluginRegistry() {
		extRegistry = InternalPlatform.getDefault().getRegistry();
	}

	public IConfigurationElement[] getConfigurationElementsFor(String uniqueId) {
		return extRegistry.getConfigurationElementsFor(uniqueId);
	}

	public IConfigurationElement[] getConfigurationElementsFor(String pluginId, String pointId) {
		return extRegistry.getConfigurationElementsFor(pluginId, pointId);
	}

	public IConfigurationElement[] getConfigurationElementsFor(String pluginId, String pointId, String extensionId) {
		return extRegistry.getConfigurationElementsFor(pluginId, pointId, extensionId);
	}

	public IExtension getExtension(String xptUniqueId, String extUniqueId) {
		return extRegistry.getExtension(xptUniqueId, extUniqueId);
	}

	public IExtension getExtension(String pluginId, String xptSimpleId, String extId) {
		return extRegistry.getExtension(pluginId, xptSimpleId, extId);
	}

	public IExtensionPoint getExtensionPoint(String xptUniqueId) {
		return extRegistry.getExtensionPoint(xptUniqueId);
	}

	public IExtensionPoint getExtensionPoint(String plugin, String xpt) {
		return extRegistry.getExtensionPoint(plugin, xpt);
	}

	public IExtensionPoint[] getExtensionPoints() {
		return extRegistry.getExtensionPoints();
	}

	public synchronized IPluginDescriptor getPluginDescriptor(String plugin) {
		// first check to see if a bundle exists
		Bundle b = InternalPlatform.getDefault().getBundle(plugin);
		PluginDescriptor pd = (PluginDescriptor) descriptors.get(plugin);
		if (b != null) {
			// we haven't created a plugin descriptor yet or it was for a different bundle
			if (pd == null || pd.getBundle() != b) {
				// create a new plugin descriptor and save it for the next time
				pd = new PluginDescriptor(b);
				descriptors.put(plugin, pd);
			}
			return pd;
		}
		// if a bundle does not exist, ensure we don't keep a plugin descriptor for it
		if (pd != null)
			descriptors.remove(plugin);
		return null;
	}
	public IPluginDescriptor[] getPluginDescriptors(String plugin) {
		IPluginDescriptor pd = getPluginDescriptor(plugin);
		if (pd == null)
			return new IPluginDescriptor[0];
		return new IPluginDescriptor[] { pd };
	}

	public IPluginDescriptor getPluginDescriptor(String pluginId, PluginVersionIdentifier version) {
		return getPluginDescriptor(pluginId);
	}

	public IPluginDescriptor[] getPluginDescriptors() {
		Bundle[] bundles = InternalPlatform.getDefault().getBundleContext().getBundles();
		ArrayList pds = new ArrayList(bundles.length);
		for (int i = 0; i < bundles.length; i++)
			if (!bundles[i].isFragment() && bundles[i].getGlobalName() != null)
				pds.add(getPluginDescriptor(bundles[i].getGlobalName()));
		IPluginDescriptor[] result = new IPluginDescriptor[pds.size()];
		return (IPluginDescriptor[]) pds.toArray(result);
	}

	void logError(IStatus status) {
		InternalPlatform.getDefault().log(status);
		if (InternalPlatform.DEBUG)
			System.out.println(status.getMessage());
	}

}
