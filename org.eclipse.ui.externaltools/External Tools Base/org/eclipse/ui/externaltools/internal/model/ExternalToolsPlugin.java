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
package org.eclipse.ui.externaltools.internal.model;


import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * External tools plug-in class
 */
public final class ExternalToolsPlugin extends AbstractUIPlugin {
	/**
	 * Status representing no problems encountered during operation.
	 */
	public static final IStatus OK_STATUS = new Status(IStatus.OK, IExternalToolConstants.PLUGIN_ID, 0, "", null); //$NON-NLS-1$

	private static ExternalToolsPlugin plugin;
	
	private static final String EMPTY_STRING= ""; //$NON-NLS-1$

	/**
	 * Create an instance of the External Tools plug-in.
	 */
	public ExternalToolsPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the default instance of the receiver.
	 * This represents the runtime plugin.
	 */
	public static ExternalToolsPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns a new <code>IStatus</code> for this plug-in
	 */
	public static IStatus newErrorStatus(String message, Throwable exception) {
		if (message == null) {
			message= EMPTY_STRING; 
		}		
		return new Status(IStatus.ERROR, IExternalToolConstants.PLUGIN_ID, 0, message, exception);
	}

	/**
	 * Returns a new <code>CoreException</code> for this plug-in
	 */
	public static CoreException newError(String message, Throwable exception) {
		return new CoreException(new Status(IStatus.ERROR, IExternalToolConstants.PLUGIN_ID, 0, message, exception));
	}

	/**
	 * Writes the message to the plug-in's log
	 * 
	 * @param message the text to write to the log
	 */
	public void log(String message, Throwable exception) {
		IStatus status = newErrorStatus(message, exception);
		getLog().log(status);
	}
	
	public void log(Throwable exception) {
		//this message is intentionally not internationalized, as an exception may
		// be due to the resource bundle itself
		getLog().log(newErrorStatus("Internal error logged from External Tools UI: ", exception)); //$NON-NLS-1$
	}

	/**
	 * Returns the ImageDescriptor for the icon with the given path
	 * 
	 * @return the ImageDescriptor object
	 */
	public ImageDescriptor getImageDescriptor(String path) {
		try {
			Bundle bundle= getDefault().getBundle();
			URL installURL = bundle.getEntry("/"); //$NON-NLS-1$
			URL url = new URL(installURL, path);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * Method declared in AbstractUIPlugin.
	 */
	protected void initializeDefaultPreferences(IPreferenceStore prefs) {
		prefs.setDefault(IPreferenceConstants.PROMPT_FOR_TOOL_MIGRATION, true);
		prefs.setDefault(IPreferenceConstants.PROMPT_FOR_PROJECT_MIGRATION, true);
	}

	/**
	 * Returns the active workbench window or <code>null</code> if none
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}
	
	/**
	 * Returns the active workbench page or <code>null</code> if none.
	 */
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window= getActiveWorkbenchWindow();
		if (window != null) {
			return window.getActivePage();
		}
		return null;
	}
	
	/**
	 * Returns the active workbench shell or <code>null</code> if none.
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}

	/**
	 * Returns the standard display to be used. The method first checks, if
	 * the thread calling this method has an associated display. If so, this
	 * display is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#createImageRegistry()
	 */
	protected ImageRegistry createImageRegistry() {
		return ExternalToolsImages.initializeImageRegistry();
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		try {
			ExternalToolsImages.disposeImageDescriptorRegistry();
		} finally {
			super.stop(context);
		}
	}
}