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
package org.eclipse.core.internal.compatibility;

import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.internal.registry.Extension;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.osgi.framework.*;

public class PluginActivator implements BundleActivator {

	private static final String PI_APPLICATION_RUNNER = "org.eclipse.core.applicationrunner"; //$NON-NLS-1$
	private BundleContext context;
	private Plugin plugin;

	public BundleContext getBundleContext() {
		return context;
	}

	public PluginActivator() {
		super();
	}
	public void start(BundleContext context) throws Exception {
		// will bail if it is not time to start
		ensureNormalStartup(context);

		this.context = context;

		PluginDescriptor pd = (PluginDescriptor) CompatibilityHelper.getPluginDescriptor(context.getBundle().getGlobalName());
		plugin = pd.getPlugin();
		plugin.startup();
	}
	private void ensureNormalStartup(BundleContext context) throws BundleException {
		Bundle applicationRunnerBundle = context.getBundle(PI_APPLICATION_RUNNER);
		if (applicationRunnerBundle != null && (applicationRunnerBundle.getState() & (Bundle.ACTIVE | Bundle.STARTING)) == 0) {
			IStatus status = new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, 0, Policy.bind("activator.applicationNotStarted", context.getBundle().getGlobalName()), null); //$NON-NLS-1$
			InternalPlatform.getDefault().log(status);
			throw new BundleException(status.getMessage());
		}
	}

	public void stop(BundleContext context) throws Exception {
		try {
			plugin.shutdown();
		} finally {
			this.context = null;
		}
	}
}
