/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ant.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ant.internal.core.AntCorePreferences;
import org.eclipse.ant.internal.core.IAntCoreConstants;
import org.eclipse.core.runtime.*;
/**
 * The plug-in runtime class for the Ant Core plug-in.
 */
public class AntCorePlugin extends Plugin implements IAntCoreConstants {

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
	public static final String PI_ANTCORE= "org.eclipse.ant.core"; //$NON-NLS-1$

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
	 * progress monitor is passed to the AntRunner.run(IProgressMonitor)
	 * method, the object is available as a reference for the current
	 * project.
	 */
	public static final String ECLIPSE_PROGRESS_MONITOR = "eclipse.progress.monitor"; //$NON-NLS-1$

/** 
 * Constructs an instance of this plug-in runtime class.
 * <p>
 * An instance of this plug-in runtime class is automatically created 
 * when the facilities provided by the Ant Core plug-in are required.
 * <b>Cliens must never explicitly instantiate a plug-in runtime class.</b>
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
	if (preferences == null)
		return;
	preferences.updatePluginPreferences();
	savePluginPreferences();
}

/**
 * Given an extension point name, extract its extensions and return them
 * as a Map. It uses as keys the attribute specified by the key parameter.
 */
private Map extractExtensions(String point, String key) {
	IExtensionPoint extensionPoint = getDescriptor().getExtensionPoint(point);
	if (extensionPoint == null)
		return null;
	IConfigurationElement[] extensions = extensionPoint.getConfigurationElements();
	Map result = new HashMap(extensions.length);
	for (int i = 0; i < extensions.length; i++) {
		String name = extensions[i].getAttribute(key);
		result.put(name, extensions[i]);
	}
	return result;
}

public AntCorePreferences getPreferences() {
	if (preferences == null)
		preferences = new AntCorePreferences(taskExtensions, extraClasspathExtensions, typeExtensions);
	return preferences;
}

/**
 * Returns this plug-in.
 *
 * @return the single instance of this plug-in runtime class
 */
public static AntCorePlugin getPlugin() {
	return plugin;
}

}