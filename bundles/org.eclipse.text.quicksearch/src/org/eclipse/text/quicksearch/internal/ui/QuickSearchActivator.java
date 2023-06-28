/*******************************************************************************
 * Copyright (c) 2013-2019 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.text.quicksearch.internal.core.preferences.QuickSearchPreferences;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class QuickSearchActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.text.quicksearch"; //$NON-NLS-1$

	// The shared instance
	private static QuickSearchActivator plugin;

	private QuickSearchPreferences prefs = null; //Lazy initialized

	/**
	 * The constructor
	 */
	public QuickSearchActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static QuickSearchActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static void log(Throwable exception) {
		log(createErrorStatus(exception));
	}

	public static void log(IStatus status) {
//		if (logger == null) {
			getDefault().getLog().log(status);
//		}
//		else {
//			logger.logEntry(status);
//		}
	}

	public static IStatus createErrorStatus(Throwable exception) {
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, exception.getMessage(), exception);
	}

	public QuickSearchPreferences getPreferences() {
		if (prefs==null) {
			prefs = new QuickSearchPreferences(QuickSearchActivator.getDefault().getPreferenceStore());
		}
		return prefs;
	}

}
