package org.eclipse.ant.core;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ant.internal.core.AntCorePreferences;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;

/**
 * The plug-in runtime class for the Ant Core plug-in.
 */
public class AntCorePlugin extends Plugin {

	/**
	 * The single instance of this plug-in runtime class.
	 */
	private static AntCorePlugin plugin;

	/**
	 * Table of Ant tasks (IConfigurationElement) added through the tasks extension point
	 */
	private Map taskExtensions;

	/**
	 * Table of libraries (IConfigurationElement) added through the extraClasspathEntries extension point
	 */
	private Map extraClasspathExtensions;

	/**
	 * Table of Ant ypes (IConfigurationElement) added through the types extension point
	 */
	private Map typeExtensions;

	/**
	 * 
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
	 * Key to access the <code>IProgressMonitor</code> reference. When a
	 * progress monitor is passed to the <code>AntRunner.run(IProgressMonitor)</code>
	 * method, the object is available as a reference for the current
	 * Ant project.
	 */
	public static final String ECLIPSE_PROGRESS_MONITOR = "eclipse.progress.monitor"; //$NON-NLS-1$
	
	/**
	 * Status code indicating an error occurred running a script.
	 * @since 2.1	 */
	public static final int ERROR_RUNNING_SCRIPT = 1;
	
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
	 * @see Plugin#startup
	 */
	public void startup() throws CoreException {
		taskExtensions = extractExtensions(PT_TASKS, NAME);
		typeExtensions = extractExtensions(PT_TYPES, NAME);
		extraClasspathExtensions = extractExtensions(PT_EXTRA_CLASSPATH, LIBRARY);
	}

	/**
	 * @see Plugin#shutdown
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
	 * as a Map. It uses as keys the attribute specified by the key parameter.
	 */
	private Map extractExtensions(String point, String key) {
		IExtensionPoint extensionPoint = getDescriptor().getExtensionPoint(point);
		if (extensionPoint == null) {
			return null;
		}
		IConfigurationElement[] extensions = extensionPoint.getConfigurationElements();
		Map result = new HashMap(extensions.length);
		for (int i = 0; i < extensions.length; i++) {
			String name = extensions[i].getAttribute(key);
			result.put(name, extensions[i]);
		}
		return result;
	}

	/**
	 * Returns an object representing this plug-in's preferences.
	 * <p>
	 * This method is for internal use by the platform-related plug-ins.  
	 * Clients should not call this method.
	 * </p>
	 */
	public AntCorePreferences getPreferences() {
		if (preferences == null) {
			preferences = new AntCorePreferences(taskExtensions, extraClasspathExtensions, typeExtensions);
		}
		return preferences;
	}

	/**
	 * Returns this plug-in instance.
	 *
	 * @return the single instance of this plug-in runtime class
	 */
	public static AntCorePlugin getPlugin() {
		return plugin;
	}
}