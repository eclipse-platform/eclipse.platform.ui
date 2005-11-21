/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.jobs;

import java.util.Hashtable;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.osgi.framework.*;

/**
 * The Jobs plugin class.
 */
public class Activator implements BundleActivator {

	/**
	 * The bundle associated this plug-in
	 */
	private static BundleContext bundleContext;

	/**
	 * This plugin provides a JobManager service.
	 */
	private ServiceRegistration jobManagerService = null;

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		bundleContext = context;
		registerServices();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		unregisterServices();
		JobManager.shutdown();
		JobsOSGiUtils.getDefault().closeServices();
		bundleContext = null;
	}

	static BundleContext getContext() {
		return bundleContext;
	}

	private void registerServices() {
		jobManagerService = bundleContext.registerService(IJobManager.class.getName(), JobManager.getInstance(), new Hashtable());
	}

	private void unregisterServices() {
		if (jobManagerService != null) {
			jobManagerService.unregister();
			jobManagerService = null;
		}
	}
}
