/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools;

import java.net.URL;
import org.eclipse.core.tools.runtime.VMClassInfo;
import org.eclipse.core.tools.runtime.VMClassloaderInfo;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.framework.stats.ClassloaderStats;
import org.eclipse.osgi.framework.stats.StatsManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class CoreToolsPlugin extends AbstractUIPlugin {
	private static CoreToolsPlugin instance;
	public static String PLUGIN_ID = "org.eclipse.core.tools"; //$NON-NLS-1$
	private BundleContext context;

	static {
		if (StatsManager.MONITOR_ACTIVATION)
			initializeBootClasses();
	}

	public static CoreToolsPlugin getDefault() {
		return instance;
	}

	private static void initializeBootClasses() {
		if (!VMClassloaderInfo.hasNatives)
			return;
		ClassloaderStats loader = ClassloaderStats.getLoader("org.eclipse.osgi"); //$NON-NLS-1$
		//class loading trace option not enabled
		if (loader == null)
			return;
		VMClassInfo[] classes = VMClassloaderInfo.getBaseClasses();
		String[] names = new String[classes.length];
		for (int i = 0; i < classes.length; i++)
			names[i] = classes[i].getName();
		loader.addBaseClasses(names);
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

	public void start(BundleContext context) throws Exception {
		super.start(context);
		this.context = context;
	}

	public BundleContext getContext() {
		return context;
	}
}