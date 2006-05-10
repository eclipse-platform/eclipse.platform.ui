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

import java.net.URL;
import org.eclipse.core.internal.model.RegistryLoader;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.Factory;
import org.eclipse.core.runtime.model.PluginRegistryModel;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;


/**
 * @deprecated Marking as deprecated to remove the warnings
 */
public class InternalPlatform {
	private static IPluginRegistry registry = null;

	public static IPluginRegistry getPluginRegistry() {
		if (registry == null) {
			registry = new PluginRegistry();
		}
		return registry;
	}

	public static IPluginDescriptor getPluginDescriptor(String pluginId) {
		return getPluginRegistry().getPluginDescriptor(pluginId);
	}

	public static void installPlugins(URL[] installURLs) throws CoreException {
		String message = Policy.bind("platform.errorInstalling"); //$NON-NLS-1$
		MultiStatus result = new MultiStatus(Platform.PI_RUNTIME, 0, message, null);
		BundleContext context = org.eclipse.core.internal.runtime.InternalPlatform.getDefault().getBundleContext();
		for (int i = 0; i < installURLs.length; i++) {
			try {
				context.installBundle(installURLs[i].toExternalForm());
			} catch (BundleException e) {
				IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, 0, org.eclipse.core.internal.plugins.Policy.bind("platform.cannotInstallPlugin", installURLs[i].toExternalForm()), e); //$NON-NLS-1$
				result.merge(status);
			}
		}
		if (!result.isOK())
			throw new CoreException(result);
	}


	/**
	 * Convenience method equivalent to parsePlugins(URL[], Factory, boolean) where debug is set to false.
	 * @see #parsePlugins(URL[], Factory, boolean)
	 */
	public static PluginRegistryModel parsePlugins(URL[] pluginPath, Factory factory) {
		return parsePlugins(pluginPath, factory, false);
	}

	/**
	 * Returns a plug-in registry containing all of the plug-ins discovered
	 * on the given plug-in path.  Any problems encountered are added to
	 * the status managed by the supplied factory.
	 * <p>
	 * The given plug-in path is the list of locations in which to look for plug-ins.
	 * If an entry identifies a directory (i.e., ends in a '/'), this method
	 * attempts to scan all sub-directories for plug-ins.  Alternatively, an
	 * entry may identify a particular plug-in manifest (<code>plugin.xml</code>) file.
	 * </p>
	 * <p>
	 * <b>Note:</b> this method does not affect the running platform.  It is intended
	 * for introspecting installed plug-ins on this and other platforms.  The returned
	 * registry is <b>not</b> the same as the platform's registry.
	 * </p>
	 *
	 * @param pluginPath the list of locations in which to look for plug-ins
	 * @param factory the factory to use to create runtime model objects
	 * @param debug turn the debug information on or off
	 * @return the registry of parsed plug-ins
	 */
	public synchronized static PluginRegistryModel parsePlugins(URL[] pluginPath, Factory factory, boolean debug) {
		return RegistryLoader.parseRegistry(pluginPath, factory, debug);
	}

}
