/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.core;


import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ant.internal.core.AntClassLoader;
import org.eclipse.core.runtime.*;

/**
 * The plug-in runtime class for the Ant Core plug-in.
 */
public class AntCorePlugin extends Plugin {

	/**
	 * Status code indicating an unexpected internal error.
	 * @since 2.1
	 */
	public static final int INTERNAL_ERROR = 120;		
	
	/**
	 * The single instance of this plug-in runtime class.
	 */
	private static AntCorePlugin plugin;

	/**
	 * The preferences class for this plugin.
	 */
	private AntCorePreferences preferences;

	/**
	 * Unique identifier constant (value <code>"org.eclipse.ant.core"</code>)
	 * for the Ant Core plug-in.
	 */
	public static final String PI_ANTCORE = "org.eclipse.ant.core"; //$NON-NLS-1$

	/**
	 * Simple identifier constant (value <code>"antTasks"</code>)
	 * for the Ant tasks extension point.
	 */
	public static final String PT_TASKS = "antTasks"; //$NON-NLS-1$

	/**
	 * Simple identifier constant (value <code>"extraClasspathEntries"</code>)
	 * for the extra classpath entries extension point.
	 */
	public static final String PT_EXTRA_CLASSPATH = "extraClasspathEntries"; //$NON-NLS-1$

	/**
	 * Simple identifier constant (value <code>"antTypes"</code>)
	 * for the Ant types extension point.
	 */
	public static final String PT_TYPES = "antTypes"; //$NON-NLS-1$

	/**
	 * Simple identifier constant (value <code>"class"</code>)
	 * of a tag that appears in Ant extensions.
	 */
	public static final String CLASS = "class"; //$NON-NLS-1$

	/**
	 * Simple identifier constant (value <code>"name"</code>)
	 * of a tag that appears in Ant extensions.
	 */
	public static final String NAME = "name"; //$NON-NLS-1$

	/**
	 * Simple identifier constant (value <code>"library"</code>)
	 * of a tag that appears in Ant extensions.
	 */
	public static final String LIBRARY = "library"; //$NON-NLS-1$
	
	/**
	 * Simple identifier constant (value <code>"headless"</code>) of a tag
	 * that appears in Ant extensions.
	 * @since 2.1
	 */
	public static final String HEADLESS = "headless"; //$NON-NLS-1$

	/**
	 * Key to access the <code>IProgressMonitor</code> reference. When a
	 * progress monitor is passed to the <code>AntRunner.run(IProgressMonitor)</code>
	 * method, the object is available as a reference for the current
	 * Ant project.
	 */
	public static final String ECLIPSE_PROGRESS_MONITOR = "eclipse.progress.monitor"; //$NON-NLS-1$
	
	/**
	 * Status code indicating an error occurred running a build.
	 * @since 2.1
	 */
	public static final int ERROR_RUNNING_BUILD = 1;
	
	/**
	 * Status code indicating an error occurred due to a malformed URL.
	 * @since 2.1
	 */
	public static final int ERROR_MALFORMED_URL = 2;
	
	/**
	 * Status code indicating an error occurred as a library was not specified
	 * @since 2.1
	 */
	public static final int ERROR_LIBRARY_NOT_SPECIFIED = 3;

	/** 
	 * Constructs an instance of this plug-in runtime class.
	 * <p>
	 * An instance of this plug-in runtime class is automatically created 
	 * when the facilities provided by the Ant Core plug-in are required.
	 * <b>Clients must never explicitly instantiate a plug-in runtime class.</b>
	 * </p>
	 * 
	 * @param pluginDescriptor the plug-in descriptor for the
	 *   Ant Core plug-in
	 */
	public AntCorePlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}

	/**
	 * @see Plugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		if (preferences == null) {
			return;
		}
		preferences.updatePluginPreferences();
		savePluginPreferences();
	}

	/**
	 * Given an extension point name, extract its extensions and return them
	 * as a List.
	 */
	private List extractExtensions(String point) {
		IExtensionPoint extensionPoint = getDescriptor().getExtensionPoint(point);
		if (extensionPoint == null) {
			return null;
		}
		IConfigurationElement[] extensions = extensionPoint.getConfigurationElements();
		return Arrays.asList(extensions);
	}

	/**
	 * Returns an object representing this plug-in's preferences.
	 * 
	 * @return the Ant core object representing the preferences for this plug-in.
	 */
	public AntCorePreferences getPreferences() {
		if (preferences == null) {
			preferences = new AntCorePreferences(extractExtensions(PT_TASKS), extractExtensions(PT_EXTRA_CLASSPATH), extractExtensions(PT_TYPES), false);
		}
		return preferences;
	}
	
	/**
	 * Set this plug-in's preferences for running headless.
	 */
	protected void setRunningHeadless() {
		preferences = new AntCorePreferences(extractExtensions(PT_TASKS), extractExtensions(PT_EXTRA_CLASSPATH), extractExtensions(PT_TYPES), true);
	}

	/**
	 * Returns this plug-in instance.
	 *
	 * @return the single instance of this plug-in runtime class
	 */
	public static AntCorePlugin getPlugin() {
		return plugin;
	}
	
	/**
	 * Returns a new class loader to use when executing Ant builds.
	 * 
	 * @return the new class loader
	 */
	protected ClassLoader getNewClassLoader() {
		AntCorePreferences corePreferences = getPreferences();
		URL[] urls = corePreferences.getURLs();
		ClassLoader[] pluginLoaders = corePreferences.getPluginClassLoaders();
		return new AntClassLoader(urls, pluginLoaders);
	}
	
	/**
	 * Logs the specified throwable with this plug-in's log.
	 * 
	 * @param t throwable to log 
	 * @since 2.1
	 */
	public static void log(Throwable t) {
		IStatus status= new Status(IStatus.ERROR, PI_ANTCORE, INTERNAL_ERROR, "Error logged from Ant Core: ", t); //$NON-NLS-1$
		getPlugin().getLog().log(status);
	}
}
