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
package org.eclipse.core.internal.content;

import java.util.Hashtable;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.osgi.framework.*;

/**
 * The runtime contents plugin class.
 */
public class Activator implements BundleActivator {

	/**
	 * The bundle associated this plug-in
	 */
	private static BundleContext bundleContext;

	/**
	 * This plugin provides a JobManager service.
	 */
	private ServiceRegistration contentManagerService = null;

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		bundleContext = context;
		ContentTypeManager.startup();
		registerServices();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		unregisterServices();
		ContentTypeManager.shutdown();
		ContentOSGiUtils.getDefault().closeServices();
		bundleContext = null;
	}

	static BundleContext getContext() {
		return bundleContext;
	}

	private void registerServices() {
		// ContentTypeManager should be started first
		contentManagerService = bundleContext.registerService(IContentTypeManager.class.getName(), ContentTypeManager.getInstance(), new Hashtable());
	}

	private void unregisterServices() {
		if (contentManagerService != null) {
			contentManagerService.unregister();
			contentManagerService = null;
		}
	}
}
