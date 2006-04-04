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
package org.eclipse.core.internal.compatibility;

import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class PluginActivator implements BundleActivator {
	private Plugin plugin;

	public PluginActivator() {
		super();
	}

	public void start(BundleContext context) throws Exception {
		PluginDescriptor pd = (PluginDescriptor) Platform.getPluginRegistry().getPluginDescriptor(context.getBundle().getSymbolicName());
		plugin = pd.getPlugin();
		try {
			plugin.start(context);
			plugin.startup();
		} catch(Exception e) {
			try {
				plugin.shutdown();
				plugin.stop(context);
				pd.markAsDeactivated();
			} catch(Exception e1) {
				// We are mostly interested in the original exception 
				e1.printStackTrace();
			}
			throw e;
		}
	}

	public void stop(BundleContext context) throws Exception {
		plugin.shutdown();
		plugin.stop(context);
		((PluginDescriptor) plugin.getDescriptor()).doPluginDeactivation();
	}
}
