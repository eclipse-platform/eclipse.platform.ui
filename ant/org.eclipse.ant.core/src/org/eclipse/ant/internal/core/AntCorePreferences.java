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
package org.eclipse.ant.internal.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.core.runtime.*;
/**
 * 
 */
public class AntCorePreferences implements IAntCoreConstants {

	protected List defaultTasks;
	protected List defaultTypes;
	protected List defaultURLs;
	protected Task[] customTasks;
	protected Type[] customTypes;
	protected URL[] customURLs;
	protected Map defaultObjects;
	protected List pluginClassLoaders;
	
public AntCorePreferences(Map defaultTasks, Map defaultObjects, Map defaultTypes) {
	initializePluginClassLoaders();
	defaultURLs = new ArrayList(20);
	this.defaultTasks = computeDefaultTasks(defaultTasks);
	this.defaultTypes = computeDefaultTypes(defaultTypes);
	this.defaultObjects = computeDefaultObjects(defaultObjects);
	restoreCustomObjects();
}

protected void restoreCustomObjects() {
	Preferences prefs = AntCorePlugin.getPlugin().getPluginPreferences();
	// tasks
	String tasks = prefs.getString(PREFERENCE_TASKS);
	if (tasks.equals(""))
		customTasks = new Task[0];
	else
		customTasks = extractTasks(prefs, getArrayFromString(tasks));
	// types
	String types = prefs.getString(PREFERENCE_TYPES);
	if (types.equals(""))
		customTypes = new Type[0];
	else
		customTypes = extractTypes(prefs, getArrayFromString(types));
	// urls
	String urls = prefs.getString(PREFERENCE_URLS);
	if (urls.equals(""))
		customURLs = getDefaultCustomURLs();
	else
		customURLs = extractURLs(prefs, getArrayFromString(urls));
}

protected Task[] extractTasks(Preferences prefs, String[] tasks) {
	List result = new ArrayList(10);
	for (int i = 0; i < tasks.length; i++) {
		try {
			String taskName = tasks[i];
			String[] values = getArrayFromString(prefs.getString(PREFIX_TASK + taskName));
			Task task = new Task();
			task.setTaskName(taskName);
			task.setClassName(values[0]);
			task.setLibrary(new URL(values[1]));
			result.add(task);
		} catch (MalformedURLException e) {
			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, ERROR_MALFORMED_URL, Policy.bind("exception.malformedURL"), e);
			AntCorePlugin.getPlugin().getLog().log(status);
		}
	}
	return (Task[]) result.toArray(new Task[result.size()]);
}

protected Type[] extractTypes(Preferences prefs, String[] types) {
	List result = new ArrayList(10);
	for (int i = 0; i < types.length; i++) {
		try {
			String typeName = types[i];
			String[] values = getArrayFromString(prefs.getString(PREFIX_TYPE + typeName));
			Type type = new Type();
			type.setTypeName(typeName);
			type.setClassName(values[0]);
			type.setLibrary(new URL(values[1]));
			result.add(type);
		} catch (MalformedURLException e) {
			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, ERROR_MALFORMED_URL, Policy.bind("exception.malformedURL"), e);
			AntCorePlugin.getPlugin().getLog().log(status);
		}
	}
	return (Type[]) result.toArray(new Type[result.size()]);
}

protected URL[] extractURLs(Preferences prefs, String[] urls) {
	List result = new ArrayList(10);
	for (int i = 0; i < urls.length; i++) {
		try {
			result.add(new URL(urls[i]));
		} catch (MalformedURLException e) {
			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, ERROR_MALFORMED_URL, Policy.bind("exception.malformedURL"), e);
			AntCorePlugin.getPlugin().getLog().log(status);
		}
	}
	return (URL[]) result.toArray(new URL[result.size()]);
}




public URL[] getDefaultCustomURLs() {
	List result = new ArrayList(10);
	IPluginDescriptor descriptor = Platform.getPlugin("org.apache.ant").getDescriptor();
	addLibraries(descriptor, result);
	descriptor = Platform.getPlugin("org.apache.xerces").getDescriptor();
	addLibraries(descriptor, result);
	addToolsJar(result);
	return (URL[]) result.toArray(new URL[result.size()]);
}

protected List computeDefaultTasks(Map tasks) {
	List result = new ArrayList(10);
	for (Iterator iterator = tasks.entrySet().iterator(); iterator.hasNext();) {
		Map.Entry entry = (Map.Entry) iterator.next();
		Task task = new Task();
		task.setTaskName((String) entry.getKey());
		IConfigurationElement element = (IConfigurationElement) entry.getValue();
		task.setClassName(element.getAttribute(AntCorePlugin.CLASS));
		String library = element.getAttribute(AntCorePlugin.LIBRARY);
		if (library == null) {
			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, ERROR_LIBRARY_NOT_SPECIFIED, Policy.bind("error.libraryNotSpecified", task.getTaskName()), null);
			AntCorePlugin.getPlugin().getLog().log(status);
			continue;
		}
		IPluginDescriptor descriptor = element.getDeclaringExtension().getDeclaringPluginDescriptor();
		try {
			URL url = Platform.asLocalURL(new URL(descriptor.getInstallURL(), library));
			task.setLibrary(url);
			defaultURLs.add(url);
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, ERROR_MALFORMED_URL, Policy.bind("exception.malformedURL"), e);
			AntCorePlugin.getPlugin().getLog().log(status);
			continue;
		}
		result.add(task);
		addPluginClassLoader(descriptor.getPluginClassLoader());
	}
	return result;
}

protected List computeDefaultTypes(Map types) {
	List result = new ArrayList(10);
	for (Iterator iterator = types.entrySet().iterator(); iterator.hasNext();) {
		Map.Entry entry = (Map.Entry) iterator.next();
		Type type = new Type();
		type.setTypeName((String) entry.getKey());
		IConfigurationElement element = (IConfigurationElement) entry.getValue();
		type.setClassName(element.getAttribute(AntCorePlugin.CLASS));
		String library = element.getAttribute(AntCorePlugin.LIBRARY);
		if (library == null) {
			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, ERROR_LIBRARY_NOT_SPECIFIED, Policy.bind("error.libraryNotSpecified", type.getTypeName()), null);
			AntCorePlugin.getPlugin().getLog().log(status);
			continue;
		}
		IPluginDescriptor descriptor = element.getDeclaringExtension().getDeclaringPluginDescriptor();
		try {
			URL url = Platform.asLocalURL(new URL(descriptor.getInstallURL(), library));
			type.setLibrary(url);
			defaultURLs.add(url);
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, ERROR_MALFORMED_URL, Policy.bind("exception.malformedURL"), e);
			AntCorePlugin.getPlugin().getLog().log(status);
			continue;
		}
		result.add(type);
		addPluginClassLoader(descriptor.getPluginClassLoader());
	}
	return result;
}

/**
 * It returns the same objects as passed in the arguments. The only difference
 * is that it does extract other useful information.
 */
protected Map computeDefaultObjects(Map objects) {
	for (Iterator iterator = objects.entrySet().iterator(); iterator.hasNext();) {
		Map.Entry entry = (Map.Entry) iterator.next();
		IConfigurationElement element = (IConfigurationElement) entry.getValue();
		String library = element.getAttribute(AntCorePlugin.LIBRARY);
		if (library == null) {
			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, ERROR_LIBRARY_NOT_SPECIFIED, Policy.bind("error.libraryNotSpecified", (String) entry.getKey()), null);
			AntCorePlugin.getPlugin().getLog().log(status);
			continue;
		}
		IPluginDescriptor descriptor = element.getDeclaringExtension().getDeclaringPluginDescriptor();
		try {
			URL url = Platform.asLocalURL(new URL(descriptor.getInstallURL(), library));
			defaultURLs.add(url);
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, ERROR_MALFORMED_URL, Policy.bind("exception.malformedURL"), e);
			AntCorePlugin.getPlugin().getLog().log(status);
			continue;
		}
		addPluginClassLoader(descriptor.getPluginClassLoader());
	}
	return objects;
}

/**
 * Ant running through the command line tries to find tools.jar to help the user. Try
 * emulating the same behaviour here.
 */
protected void addToolsJar(List destination) {
	IPath path = new Path(System.getProperty("java.home"));
	if (path.lastSegment().equalsIgnoreCase("jre"))
		path = path.removeLastSegments(1);
	path = path.append("lib").append("tools.jar");
	File tools = path.toFile();
	if (!tools.exists())
		return;
	try {
		destination.add(tools.toURL());
	} catch (MalformedURLException e) {
		IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, ERROR_MALFORMED_URL, Policy.bind("exception.malformedURL"), e);
		AntCorePlugin.getPlugin().getLog().log(status);
	}
}

protected void addLibraries(IPluginDescriptor source, List destination) {
	URL root = source.getInstallURL();
	ILibrary[] libraries = source.getRuntimeLibraries();
	for (int i = 0; i < libraries.length; i++) {
		try {
			URL url = new URL(root, libraries[i].getPath().toString());
			destination.add(Platform.asLocalURL(url));
		} catch (Exception e) {
			// FIXME: add error code and better message
			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, -1, e.getMessage(), e);
			AntCorePlugin.getPlugin().getLog().log(status);
			continue;
		}
	}
}



protected void addPluginClassLoader(ClassLoader loader) {
	if (!pluginClassLoaders.contains(loader))
		pluginClassLoaders.add(loader);
}



public URL[] getURLs() {
	List result = new ArrayList(10);
	if (defaultURLs != null)
		result.addAll(defaultURLs);
	if (customURLs != null)
		result.addAll(Arrays.asList(customURLs));
	return (URL[]) result.toArray(new URL[result.size()]);
}

public ClassLoader[] getPluginClassLoaders() {
	return (ClassLoader[]) pluginClassLoaders.toArray(new ClassLoader[pluginClassLoaders.size()]);
}

protected void initializePluginClassLoaders() {
	pluginClassLoaders = new ArrayList(20);
	// ant.core should always be present
	pluginClassLoaders.add(Platform.getPlugin(AntCorePlugin.PI_ANTCORE).getDescriptor().getPluginClassLoader());
}


/**
 * Returns default + custom tasks.
 */
public List getTasks() {
	List result = new ArrayList(10);
	if (defaultTasks != null)
		result.addAll(defaultTasks);
	if (customTasks != null)
		result.addAll(Arrays.asList(customTasks));
	return result;
}

public Task[] getCustomTasks() {
	return customTasks;
}

public Type[] getCustomTypes() {
	return customTypes;
}

public URL[] getCustomURLs() {
	return customURLs;
}

public void setCustomTasks(Task[] tasks) {
	this.customTasks = tasks;
}

public void setCustomTypes(Type[] types) {
	this.customTypes = types;
}

public void setCustomURLs(URL[] urls) {
	this.customURLs = urls;
}

/**
 * Returns default + custom types.
 */
public List getTypes() {
	List result = new ArrayList(10);
	if (defaultTypes != null)
		result.addAll(defaultTypes);
	if (customTypes != null)
		result.addAll(Arrays.asList(customTypes));
	return result;
}

/**
 * Convert a list of tokens into an array. The list separator has to be specified.
 */
public static String[] getArrayFromString(String list, String separator) {
	if (list == null || list.trim().equals(""))
		return new String[0];
	ArrayList result = new ArrayList();
	for (StringTokenizer tokens = new StringTokenizer(list, separator); tokens.hasMoreTokens();) {
		String token = tokens.nextToken().trim();
		if (!token.equals(""))
			result.add(token);
	}
	return (String[]) result.toArray(new String[result.size()]);
}

/**
 * convert a list of comma-separated tokens into an array
 */
public static String[] getArrayFromString(String list) {
	return getArrayFromString(list, ",");
}

public void updatePluginPreferences() {
	Preferences prefs = AntCorePlugin.getPlugin().getPluginPreferences();
	updateTasks(prefs);
	updateTypes(prefs);
	updateURLs(prefs);
}

protected void updateTasks(Preferences prefs) {
	if (customTasks.length == 0) {
		prefs.setValue(PREFERENCE_TASKS, "");
		return;
	}
	StringBuffer tasks = new StringBuffer();
	for (int i = 0; i < customTasks.length; i++) {
		tasks.append(customTasks[i].getTaskName());
		tasks.append(",");
		prefs.setValue(PREFIX_TASK + customTasks[i].getTaskName(), customTasks[i].getClassName() + "," + customTasks[i].getLibrary().toExternalForm());
	}
	prefs.setValue(PREFERENCE_TASKS, tasks.toString());
}

protected void updateTypes(Preferences prefs) {
	if (customTypes.length == 0) {
		prefs.setValue(PREFERENCE_TYPES, "");
		return;
	}
	StringBuffer types = new StringBuffer();
	for (int i = 0; i < customTypes.length; i++) {
		types.append(customTypes[i].getTypeName());
		types.append(",");
		prefs.setValue(PREFIX_TYPE + customTypes[i].getTypeName(), customTypes[i].getClassName() + "," + customTypes[i].getLibrary().toExternalForm());
	}
	prefs.setValue(PREFERENCE_TYPES, types.toString());
}

protected void updateURLs(Preferences prefs) {
	StringBuffer urls = new StringBuffer();
	for (int i = 0; i < customURLs.length; i++) {
		urls.append(customURLs[i].toExternalForm());
		urls.append(",");
	}
	prefs.setValue(PREFERENCE_URLS, urls.toString());
}
}