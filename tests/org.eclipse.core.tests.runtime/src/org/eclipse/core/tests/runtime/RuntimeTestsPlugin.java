/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class RuntimeTestsPlugin extends Plugin {	

	public static final String PI_RUNTIME_TESTS = "org.eclipse.core.tests.runtime"; //$NON-NLS-1$

	private static RuntimeTestsPlugin plugin;
	private BundleContext context;

	public static final String TEST_FILES_ROOT = "Plugin_Testing/";

	public RuntimeTestsPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		this.context = context;
	}

	public void stop(BundleContext context) throws Exception {
		context = null;
	}

	public static BundleContext getContext() {
		return plugin != null ? plugin.context : null;
	}

	public static Plugin getPlugin() {
		return plugin;
	}

}
