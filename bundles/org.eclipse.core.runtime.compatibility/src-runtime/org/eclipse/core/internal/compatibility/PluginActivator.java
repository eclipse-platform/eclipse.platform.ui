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
package org.eclipse.core.internal.compatibility;

import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.osgi.framework.*;
import org.osgi.service.startlevel.StartLevel;
import org.osgi.util.tracker.ServiceTracker;

public class PluginActivator implements BundleActivator {
	private BundleContext context;
	private Plugin plugin;

	private static ServiceTracker startLevelTracker = null;

	public static void closeStartLevelTracker() {
		if (startLevelTracker == null)
			return;
		startLevelTracker.close();
		startLevelTracker = null;
	}
	public static StartLevel getStartLevel(BundleContext context) {
		if (startLevelTracker == null) {
			startLevelTracker = new ServiceTracker(context, StartLevel.class.getName(), null);
			startLevelTracker.open();
		}
		return (StartLevel) startLevelTracker.getService();
	}

	public BundleContext getBundleContext() {
		return context;
	}

	public PluginActivator() {
		super();
	}

	public void start(BundleContext ctx) throws Exception {
		// will bail if it is not time to start
		ensureNormalStartup(ctx);
		this.context = ctx;
		PluginDescriptor pd = (PluginDescriptor) Platform.getPluginRegistry().getPluginDescriptor(context.getBundle().getSymbolicName());
		plugin = pd.getPlugin();
		try {
			plugin.start(context);
			plugin.startup();
		} catch(Exception e) {
			plugin.shutdown();
			plugin.stop(context);
			pd.markAsDeactivated();
			throw e;
		}
	}

	private void ensureNormalStartup(BundleContext context) throws BundleException {
		// TODO look at other ways of doing this to make it faster (getService is not as fast 
		// as we might like but it is not horrible.  Also, we never close the tracker.
		StartLevel startLevel = getStartLevel(InternalPlatform.getDefault().getBundleContext());
		if (startLevel == null)
			return;
		if (startLevel.getStartLevel() <= startLevel.getBundleStartLevel(context.getBundle())) {
			IStatus status = new Status(IStatus.WARNING, Platform.PI_RUNTIME, 0, org.eclipse.core.internal.plugins.Policy.bind("activator.applicationNotStarted", context.getBundle().getSymbolicName()), null); //$NON-NLS-1$
			InternalPlatform.getDefault().log(status);
			throw new BundleException(status.getMessage());
		}
	}

	public void stop(BundleContext context) throws Exception {
		try {
			plugin.shutdown();
			plugin.stop(context);
			((PluginDescriptor) plugin.getDescriptor()).doPluginDeactivation();
		} finally {
			this.context = null;
		}
	}
}