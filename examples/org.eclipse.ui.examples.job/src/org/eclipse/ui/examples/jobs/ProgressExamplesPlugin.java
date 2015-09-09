/*******************************************************************************
 * Copyright (c) 2004,2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.jobs;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.examples.jobs.views.ProgressExampleAdapterFactory;
import org.eclipse.ui.examples.jobs.views.SlowElement;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class ProgressExamplesPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static ProgressExamplesPlugin plugin;
	public static String ID = "org.eclipse.ui.examples.job"; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public ProgressExamplesPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static ProgressExamplesPlugin getDefault() {
		return plugin;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		IAdapterManager m = Platform.getAdapterManager();
		IAdapterFactory f = new ProgressExampleAdapterFactory();
		m.registerAdapters(f, SlowElement.class);
	}
}
