/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tools.resources;

import java.net.URL;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class CoreResourcesToolsPlugin extends AbstractUIPlugin {
	public static String PLUGIN_ID = "org.eclipse.core.tools.resources"; //$NON-NLS-1$
	private static CoreResourcesToolsPlugin instance;
	private BundleContext context;

	public CoreResourcesToolsPlugin() {
		super();
		instance = this;
	}

	public static CoreResourcesToolsPlugin getDefault() {
		return instance;
	}

	public BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		this.context = bundleContext;
	}

	public static void logProblem(Exception e) {
		IStatus status;
		if (e instanceof CoreException)
			status = ((CoreException) e).getStatus();
		else
			status = new Status(IStatus.ERROR, PLUGIN_ID, 0, e.getMessage(), e);
		getDefault().getLog().log(status);
	}

	public static void logProblem(String message, int severity) {
		IStatus status = new Status(severity, PLUGIN_ID, 0, message, null);
		getDefault().getLog().log(status);
		if (severity == IStatus.ERROR)
			ErrorDialog.openError(getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), "MarkerSpy Notification", message, status);
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
}