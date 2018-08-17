/*******************************************************************************
 * Copyright (c) 2002, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tools;

import java.net.URL;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
//import org.eclipse.core.runtime.internal.stats.ClassloaderStats;
//import org.eclipse.core.runtime.internal.stats.StatsManager;
//import org.eclipse.core.tools.runtime.VMClassInfo;
//import org.eclipse.core.tools.runtime.VMClassloaderInfo;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class CoreToolsPlugin extends AbstractUIPlugin {
	private static CoreToolsPlugin instance;
	public static String PI_TOOLS = "org.eclipse.core.tools"; //$NON-NLS-1$
	private BundleContext context;

	public static CoreToolsPlugin getDefault() {
		return instance;
	}

	/**
	 * find an icon - caller must dispose of it
	 */
	public static ImageDescriptor createImageDescriptor(String imageName) {
		URL url = getDefault().getBundle().getEntry("icons/" + imageName); //$NON-NLS-1$
		if (url != null)
			return ImageDescriptor.createFromURL(url);
		return ImageDescriptor.getMissingImageDescriptor();
	}

	public CoreToolsPlugin() {
		super();
		instance = this;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		this.context = bundleContext;
	}

	public BundleContext getContext() {
		return context;
	}

	public void log(String message, Throwable exception) {
		getLog().log(new Status(IStatus.ERROR, PI_TOOLS, 0, message, exception));
	}
}
