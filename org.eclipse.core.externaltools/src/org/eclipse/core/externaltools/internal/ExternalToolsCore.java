/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.externaltools.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * Controls the plug-in life cycle
 */
public class ExternalToolsCore extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.core.externaltools"; //$NON-NLS-1$
	
	/**
	 * Status code indicating an unexpected internal error.
	 * @since 2.1
	 */
	public static final int INTERNAL_ERROR = 120;	

	// The shared instance
	private static ExternalToolsCore plugin;
	
	/**
	 * The constructor
	 */
	public ExternalToolsCore() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ExternalToolsCore getDefault() {
		return plugin;
	}
	
	/**
	 * Logs the specified throwable with this plug-in's log.
	 * 
	 * @param t throwable to log 
	 */
	public static void log(Throwable t) {
		IStatus status= new Status(IStatus.ERROR, PLUGIN_ID, INTERNAL_ERROR, "Error logged from Ant UI: ", t); //$NON-NLS-1$
		log(status);
	}

	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status status 
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	/**
	 * Writes the message to the plug-in's log
	 * 
	 * @param message the text to write to the log
	 */
	public static void log(String message, Throwable exception) {
		IStatus status = newErrorStatus(message, exception);
		log(status);
	}
	
	/**
	 * Returns a new <code>IStatus</code> for this plug-in
	 */
	public static IStatus newErrorStatus(String message, Throwable exception) {
		if (message == null) {
			return new Status(IStatus.ERROR, PLUGIN_ID, 0, IExternalToolConstants.EMPTY_STRING, exception); 
		}		
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}
	
	/**
	 * Returns a new <code>CoreException</code> for this plug-in
	 */
	public static CoreException newError(String message, Throwable exception) {
		return new CoreException(new Status(IStatus.ERROR,
				IExternalToolConstants.PLUGIN_ID, 0, message, exception));
	}

}
