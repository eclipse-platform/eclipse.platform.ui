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
package org.eclipse.core.internal.plugins;

import java.net.URL;
import org.eclipse.core.internal.model.RegistryLoader;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.Factory;
import org.eclipse.core.runtime.model.PluginRegistryModel;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

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
		MultiStatus result = new MultiStatus(Platform.PI_RUNTIME, 0, message, null); //$NON-NLS-1$
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
	 * @see Platform#parsePlugins
	 */
	public static PluginRegistryModel parsePlugins(URL[] pluginPath, Factory factory) {
		return parsePlugins(pluginPath, factory, false);
	}

	/**
	 * @see Platform#parsePlugins
	 */
	public synchronized static PluginRegistryModel parsePlugins(URL[] pluginPath, Factory factory, boolean debug) {
		return RegistryLoader.parseRegistry(pluginPath, factory, debug);
	}

}