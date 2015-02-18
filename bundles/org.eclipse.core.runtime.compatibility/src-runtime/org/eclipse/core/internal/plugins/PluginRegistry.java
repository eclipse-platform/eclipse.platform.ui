/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.internal.plugins;

import java.util.*;
import java.util.ArrayList;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.osgi.framework.*;

/**
 * @deprecated Marking as deprecated to remove the warnings
 */
public class PluginRegistry implements IPluginRegistry {
	private IExtensionRegistry extRegistry;
	private RegistryListener listener;
	
	protected WeakHashMap descriptors = new WeakHashMap();	//key is a bundle object, value is a pluginDescriptor. The synchornization is required

	public PluginRegistry() {
		extRegistry = InternalPlatform.getDefault().getRegistry();
		listener = new RegistryListener();
		InternalPlatform.getDefault().getBundleContext().addBundleListener(listener);
	}

	public void close() {
		InternalPlatform.getDefault().getBundleContext().removeBundleListener(listener);
		listener = null; 
		descriptors = null;
	}
	
	/**
	 * @deprecated Marking as deprecated to remove the warnings
	 */
	public IConfigurationElement[] getConfigurationElementsFor(String uniqueId) {
		return extRegistry.getConfigurationElementsFor(uniqueId);
	}

	/**
	 * @deprecated Marking as deprecated to remove the warnings
	 */
	public IConfigurationElement[] getConfigurationElementsFor(String pluginId, String pointId) {
		return extRegistry.getConfigurationElementsFor(pluginId, pointId);
	}

	/**
	 * @deprecated Marking as deprecated to remove the warnings
	 */
	public IConfigurationElement[] getConfigurationElementsFor(String pluginId, String pointId, String extensionId) {
		return extRegistry.getConfigurationElementsFor(pluginId, pointId, extensionId);
	}

	/**
	 * @deprecated Marking as deprecated to remove the warnings
	 */
	public IExtension getExtension(String xptUniqueId, String extUniqueId) {
		return extRegistry.getExtension(xptUniqueId, extUniqueId);
	}

	/**
	 * @deprecated Marking as deprecated to remove the warnings
	 */
	public IExtension getExtension(String pluginId, String xptSimpleId, String extId) {
		return extRegistry.getExtension(pluginId, xptSimpleId, extId);
	}

	/**
	 * @deprecated Marking as deprecated to remove the warnings
	 */
	public IExtensionPoint getExtensionPoint(String xptUniqueId) {
		return extRegistry.getExtensionPoint(xptUniqueId);
	}

	/**
	 * @deprecated Marking as deprecated to remove the warnings
	 */
	public IExtensionPoint getExtensionPoint(String plugin, String xpt) {
		return extRegistry.getExtensionPoint(plugin, xpt);
	}

	/**
	 * @deprecated Marking as deprecated to remove the warnings
	 */
	public IExtensionPoint[] getExtensionPoints() {
		return extRegistry.getExtensionPoints();
	}
	
	/**
	 * @deprecated Marking as deprecated to remove the warnings
	 */
	public IPluginDescriptor getPluginDescriptor(String plugin) {
		Bundle correspondingBundle = InternalPlatform.getDefault().getBundle(plugin);
		if (correspondingBundle == null)
			return null;
		return getPluginDescriptor(correspondingBundle);
	}

	private PluginDescriptor getPluginDescriptor(Bundle bundle) {
		if (InternalPlatform.getDefault().isFragment(bundle) || descriptors == null) {
			return null;
		}
		synchronized(descriptors) {
			PluginDescriptor correspondingDescriptor = (PluginDescriptor) descriptors.get(bundle);
			if (bundle != null) {
				// we haven't created a plugin descriptor yet or it was for a different bundle
				if (correspondingDescriptor == null || correspondingDescriptor.getBundle() != bundle) {
					// create a new plugin descriptor and save it for the next time
					correspondingDescriptor = new PluginDescriptor(bundle);
					descriptors.put(bundle, correspondingDescriptor);
				}
				return correspondingDescriptor;
			}
			// if a bundle does not exist, ensure we don't keep a plugin descriptor for it
			if (correspondingDescriptor != null)
				descriptors.remove(bundle);
		}
		return null;
	}
	
	/**
	 * @deprecated Marking as deprecated to remove the warnings
	 */
	public IPluginDescriptor[] getPluginDescriptors(String plugin) {
		Bundle[] bundles = InternalPlatform.getDefault().getBundles(plugin, null);
		if (bundles == null)
			return new IPluginDescriptor[0];
		IPluginDescriptor[] results = new IPluginDescriptor[bundles.length];
		int added = 0;
		for (int i = 0; i < bundles.length; i++) {
			PluginDescriptor desc = getPluginDescriptor(bundles[i]);
			if (desc != null)
				results[added++] = desc;
		}
		if (added == bundles.length)
			return results;
		
		if (added == 0)
			return new IPluginDescriptor[0];
		
		IPluginDescriptor[] toReturn = new IPluginDescriptor[added];
		System.arraycopy(results, 0, toReturn, 0, added);
		return toReturn;
	}

	/**
	 * @deprecated Marking as deprecated to remove the warnings
	 */
	public IPluginDescriptor getPluginDescriptor(String pluginId, PluginVersionIdentifier version) {
		Bundle[] bundles = InternalPlatform.getDefault().getBundles(pluginId, version.toString());
		if (bundles == null)
			return null;
		
		return getPluginDescriptor(bundles[0]);
	}

	/**
	 * @deprecated Marking as deprecated to remove the warnings
	 */
	public IPluginDescriptor[] getPluginDescriptors() {
		Bundle[] bundles = InternalPlatform.getDefault().getBundleContext().getBundles();
		ArrayList pds = new ArrayList(bundles.length);
		for (int i = 0; i < bundles.length; i++) {
			boolean isFragment = InternalPlatform.getDefault().isFragment(bundles[i]);
			if (!isFragment && bundles[i].getSymbolicName() != null && (bundles[i].getState() == Bundle.RESOLVED || bundles[i].getState() == Bundle.STARTING || bundles[i].getState() == Bundle.ACTIVE))
				pds.add(getPluginDescriptor(bundles[i]));
		}
		IPluginDescriptor[] result = new IPluginDescriptor[pds.size()];
		return (IPluginDescriptor[]) pds.toArray(result);
	}

	void logError(IStatus status) {
		InternalPlatform.getDefault().log(status);
		if (InternalPlatform.DEBUG)
			System.out.println(status.getMessage());
	}

	public class RegistryListener implements BundleListener {
		public void bundleChanged(BundleEvent event) {
			if (descriptors == null)
				return;
			
			synchronized(descriptors) {
				if (event.getType() == BundleEvent.UNINSTALLED || event.getType() == BundleEvent.UNRESOLVED) {
					descriptors.remove(event.getBundle());
				}
			}
		}
	}
}
