package org.eclipse.ant.core;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

import org.eclipse.ant.internal.core.IAntCoreConstants;
import org.eclipse.ant.internal.core.InternalCoreAntMessages;
import org.eclipse.core.runtime.*;


/**
 * Represents the Ant Core plug-in's preferences providing utilities for
 * extracting, changing and updating the underlying preferences.
 */
public class AntCorePreferences {

	protected List defaultTasks;
	protected List defaultTypes;
	protected List defaultURLs;
	protected Task[] customTasks;
	protected Type[] customTypes;
	protected URL[] customURLs;
	protected URL[] defaultCustomURLs;
	
	protected List pluginClassLoaders;

	protected AntCorePreferences(List defaultTasks, List defaultExtraClasspath, List defaultTypes) {
		initializePluginClassLoaders();
		defaultURLs = new ArrayList(20);
		this.defaultTasks = computeDefaultTasks(defaultTasks);
		this.defaultTypes = computeDefaultTypes(defaultTypes);
		computeDefaultExtraClasspathEntries(defaultExtraClasspath);
		restoreCustomObjects();
	}

	protected void restoreCustomObjects() {
		Preferences prefs = AntCorePlugin.getPlugin().getPluginPreferences();
		// tasks
		String tasks = prefs.getString(IAntCoreConstants.PREFERENCE_TASKS);
		if (tasks.equals("")) { //$NON-NLS-1$
			customTasks = new Task[0];
		} else {
			customTasks = extractTasks(prefs, getArrayFromString(tasks));
		}
		// types
		String types = prefs.getString(IAntCoreConstants.PREFERENCE_TYPES);
		if (types.equals("")) {//$NON-NLS-1$
			customTypes = new Type[0];
		} else {
			customTypes = extractTypes(prefs, getArrayFromString(types));
		}
		// urls
		String urls = prefs.getString(IAntCoreConstants.PREFERENCE_URLS);
		if (urls.equals("")) {//$NON-NLS-1$
			customURLs = getDefaultCustomURLs();
		} else {
			customURLs = extractURLs(getArrayFromString(urls));
		}
	}

	protected Task[] extractTasks(Preferences prefs, String[] tasks) {
		List result = new ArrayList(tasks.length);
		for (int i = 0; i < tasks.length; i++) {
			try {
				String taskName = tasks[i];
				String[] values = getArrayFromString(prefs.getString(IAntCoreConstants.PREFIX_TASK + taskName));
				Task task = new Task();
				task.setTaskName(taskName);
				task.setClassName(values[0]);
				task.setLibrary(new URL(values[1]));
				result.add(task);
			} catch (MalformedURLException e) {
				// if the URL does not have a valid format, just log and ignore the exception
				IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_MALFORMED_URL, InternalCoreAntMessages.getString("AntCorePreferences.Malformed_URL._1"), e); //$NON-NLS-1$
				AntCorePlugin.getPlugin().getLog().log(status);
			}
		}
		return (Task[]) result.toArray(new Task[result.size()]);
	}

	protected Type[] extractTypes(Preferences prefs, String[] types) {
		List result = new ArrayList(types.length);
		for (int i = 0; i < types.length; i++) {
			try {
				String typeName = types[i];
				String[] values = getArrayFromString(prefs.getString(IAntCoreConstants.PREFIX_TYPE + typeName));
				Type type = new Type();
				type.setTypeName(typeName);
				type.setClassName(values[0]);
				type.setLibrary(new URL(values[1]));
				result.add(type);
			} catch (MalformedURLException e) {
				// if the URL does not have a valid format, just log and ignore the exception
				IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_MALFORMED_URL, InternalCoreAntMessages.getString("AntCorePreferences.Malformed_URL._1"), e);  //$NON-NLS-1$
				AntCorePlugin.getPlugin().getLog().log(status);
			}
		}
		return (Type[]) result.toArray(new Type[result.size()]);
	}

	protected URL[] extractURLs(String[] urls) {
		List result = new ArrayList(urls.length);
		for (int i = 0; i < urls.length; i++) {
			try {
				result.add(new URL(urls[i]));
			} catch (MalformedURLException e) {
				// if the URL does not have a valid format, just log and ignore the exception
				IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_MALFORMED_URL, InternalCoreAntMessages.getString("AntCorePreferences.Malformed_URL._1"), e);  //$NON-NLS-1$
				AntCorePlugin.getPlugin().getLog().log(status);
			}
		}
		return (URL[]) result.toArray(new URL[result.size()]);
	}

	/**
	 * Returns the array of URLs that is the default set of URLs defining
	 * the Ant classpath.
	 * 	 * @return the default set of URLs defining the Ant classpath	 */
	public URL[] getDefaultCustomURLs() {
		if (defaultCustomURLs == null) {
			List result = new ArrayList(10);
			IPluginDescriptor descriptor = Platform.getPlugin("org.apache.ant").getDescriptor(); //$NON-NLS-1$
			addLibraries(descriptor, result);
			descriptor = Platform.getPlugin("org.apache.xerces").getDescriptor(); //$NON-NLS-1$
			addLibraries(descriptor, result);
			addToolsJar(result);
			defaultCustomURLs= (URL[]) result.toArray(new URL[result.size()]);
		}
		return defaultCustomURLs;
	}

	protected List computeDefaultTasks(List tasks) {
		List result = new ArrayList(tasks.size());
		for (Iterator iterator = tasks.iterator(); iterator.hasNext();) {
			IConfigurationElement element = (IConfigurationElement) iterator.next();
			Task task = new Task();
			task.setTaskName(element.getAttribute(AntCorePlugin.NAME));
			task.setClassName(element.getAttribute(AntCorePlugin.CLASS));
			String library = element.getAttribute(AntCorePlugin.LIBRARY);
			if (library == null) {
				IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_LIBRARY_NOT_SPECIFIED, MessageFormat.format(InternalCoreAntMessages.getString("AntCorePreferences.Library_not_specified_for__{0}_4"), new String[]{task.getTaskName()}), null); //$NON-NLS-1$
				AntCorePlugin.getPlugin().getLog().log(status);
				continue;
			}
			IPluginDescriptor descriptor = element.getDeclaringExtension().getDeclaringPluginDescriptor();
			try {
				URL url = Platform.asLocalURL(new URL(descriptor.getInstallURL(), library));
				task.setLibrary(url);
				if (!defaultURLs.contains(url)) {
					defaultURLs.add(url);
				}
			} catch (Exception e) {
				// if the URL does not have a valid format, just log and ignore the exception
				IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_MALFORMED_URL, InternalCoreAntMessages.getString("AntCorePreferences.Malformed_URL._1"), e); //$NON-NLS-1$
				AntCorePlugin.getPlugin().getLog().log(status);
				continue;
			}
			result.add(task);
			addPluginClassLoader(descriptor.getPluginClassLoader());
		}
		return result;
	}

	protected List computeDefaultTypes(List types) {
		List result = new ArrayList(types.size());
		for (Iterator iterator = types.iterator(); iterator.hasNext();) {
			IConfigurationElement element = (IConfigurationElement) iterator.next();
			Type type = new Type();
			type.setTypeName(element.getAttribute(AntCorePlugin.NAME));
			type.setClassName(element.getAttribute(AntCorePlugin.CLASS));
			String library = element.getAttribute(AntCorePlugin.LIBRARY);
			if (library == null) {
				IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_LIBRARY_NOT_SPECIFIED, MessageFormat.format(InternalCoreAntMessages.getString("AntCorePreferences.Library_not_specified_for__{0}_4"), new String[]{type.getTypeName()}), null); //$NON-NLS-1$
				AntCorePlugin.getPlugin().getLog().log(status);
				continue;
			}
			IPluginDescriptor descriptor = element.getDeclaringExtension().getDeclaringPluginDescriptor();
			try {
				URL url = Platform.asLocalURL(new URL(descriptor.getInstallURL(), library));
				type.setLibrary(url);
				if (!defaultURLs.contains(url)) {
					defaultURLs.add(url);
				}
			} catch (Exception e) {
				// if the URL does not have a valid format, just log and ignore the exception
				IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_MALFORMED_URL, InternalCoreAntMessages.getString("AntCorePreferences.Malformed_URL._1"), e); //$NON-NLS-1$
				AntCorePlugin.getPlugin().getLog().log(status);
				continue;
			}
			result.add(type);
			addPluginClassLoader(descriptor.getPluginClassLoader());
		}
		return result;
	}

	/**
	 * Computes the extra classpath entries defined plugins and fragments.
	 */
	protected void computeDefaultExtraClasspathEntries(List entries) {
		for (Iterator iterator = entries.iterator(); iterator.hasNext();) {
			IConfigurationElement element = (IConfigurationElement) iterator.next();
			String library = (String) element.getAttribute(AntCorePlugin.LIBRARY);
			IPluginDescriptor descriptor = element.getDeclaringExtension().getDeclaringPluginDescriptor();
			try {
				URL url = Platform.asLocalURL(new URL(descriptor.getInstallURL(), library));
				if (!defaultURLs.contains(url)) {
					defaultURLs.add(url);
				}
			} catch (Exception e) {
				// if the URL does not have a valid format, just log and ignore the exception
				IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_MALFORMED_URL, InternalCoreAntMessages.getString("AntCorePreferences.Malformed_URL._1"), e); //$NON-NLS-1$
				AntCorePlugin.getPlugin().getLog().log(status);
				continue;
			}
			addPluginClassLoader(descriptor.getPluginClassLoader());
		}
	}

	/**
	 * Ant running through the command line tries to find tools.jar to help the user. Try
	 * emulating the same behaviour here.
	 */
	protected void addToolsJar(List destination) {
		IPath path = new Path(System.getProperty("java.home")); //$NON-NLS-1$
		if (path.lastSegment().equalsIgnoreCase("jre")) { //$NON-NLS-1$
			path = path.removeLastSegments(1);
		}
		path = path.append("lib").append("tools.jar"); //$NON-NLS-1$ //$NON-NLS-2$
		File tools = path.toFile();
		if (!tools.exists()) {
			return;
		}
		try {
			destination.add(new URL("file:" + tools.getAbsolutePath())); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			// if the URL does not have a valid format, just log and ignore the exception
			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_MALFORMED_URL, InternalCoreAntMessages.getString("AntCorePreferences.Malformed_URL._1"), e);  //$NON-NLS-1$
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
				// if the URL does not have a valid format, just log and ignore the exception
				IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_MALFORMED_URL, InternalCoreAntMessages.getString("AntCorePreferences.Malformed_URL._1"), e);  //$NON-NLS-1$
				AntCorePlugin.getPlugin().getLog().log(status);
				continue;
			}
		}
	}

	protected void addPluginClassLoader(ClassLoader loader) {
		if (!pluginClassLoaders.contains(loader)) {
			pluginClassLoaders.add(loader);
		}
	}

	public URL[] getURLs() {
		List result = new ArrayList(10);
		if (defaultURLs != null) {
			result.addAll(defaultURLs);
		}
		if (customURLs != null) {
			result.addAll(Arrays.asList(customURLs));
		}
		return (URL[]) result.toArray(new URL[result.size()]);
	}

	protected ClassLoader[] getPluginClassLoaders() {
		return (ClassLoader[]) pluginClassLoaders.toArray(new ClassLoader[pluginClassLoaders.size()]);
	}

	protected void initializePluginClassLoaders() {
		pluginClassLoaders = new ArrayList(10);
		// ant.core should always be present
		pluginClassLoaders.add(Platform.getPlugin(AntCorePlugin.PI_ANTCORE).getDescriptor().getPluginClassLoader());
	}

	/**
	 * Returns the default and custom tasks.
	 * 
	 * @return the list of default and custom tasks.
	 */
	public List getTasks() {
		List result = new ArrayList(10);
		if (defaultTasks != null) {
			result.addAll(defaultTasks);
		}
		if (customTasks != null) {
			result.addAll(Arrays.asList(customTasks));
		}
		return result;
	}

	public Task[] getCustomTasks() {
		return customTasks;
	}

	public Type[] getCustomTypes() {
		return customTypes;
	}

	/**
	 * Returns the custom URLs specified for the Ant classpath
	 * 
	 * @return the urls defining the Ant classpath
	 */
	public URL[] getCustomURLs() {
		return customURLs;
	}

	public void setCustomTasks(Task[] tasks) {
		customTasks = tasks;
	}

	public void setCustomTypes(Type[] types) {
		customTypes = types;
	}

	/**
	 * Sets the custom URLs specified for the Ant classpath
	 * 
	 * @param the urls defining the Ant classpath
	 */
	public void setCustomURLs(URL[] urls) {
		customURLs = urls;
	}

	/**
	 * Returns the default and custom types.
	 * 
	 * @return all of the defined types
	 */
	public List getTypes() {
		List result = new ArrayList(10);
		if (defaultTypes != null) {
			result.addAll(defaultTypes);
		}
		if (customTypes != null) {
			result.addAll(Arrays.asList(customTypes));
		}
		return result;
	}

	/**
	 * Convert a list of tokens into an array using "," as the tokenizer.
	 */
	protected String[] getArrayFromString(String list) {
		String separator= ","; //$NON-NLS-1$
		if (list == null || list.trim().equals("")) { //$NON-NLS-1$
			return new String[0];
		}
		ArrayList result = new ArrayList();
		for (StringTokenizer tokens = new StringTokenizer(list, separator); tokens.hasMoreTokens();) {
			String token = tokens.nextToken().trim();
			if (!token.equals("")) { //$NON-NLS-1$
				result.add(token);
			}
		}
		return (String[]) result.toArray(new String[result.size()]);
	}

	/**
	 * Updates the underlying plugin preferences to the current state.	 */
	public void updatePluginPreferences() {
		Preferences prefs = AntCorePlugin.getPlugin().getPluginPreferences();
		updateTasks(prefs);
		updateTypes(prefs);
		updateURLs(prefs);
		AntCorePlugin.getPlugin().savePluginPreferences();
	}

	protected void updateTasks(Preferences prefs) {
		if (customTasks.length == 0) {
			prefs.setValue(IAntCoreConstants.PREFERENCE_TASKS, ""); //$NON-NLS-1$
			return;
		}
		StringBuffer tasks = new StringBuffer();
		for (int i = 0; i < customTasks.length; i++) {
			tasks.append(customTasks[i].getTaskName());
			tasks.append(',');
			prefs.setValue(IAntCoreConstants.PREFIX_TASK + customTasks[i].getTaskName(), customTasks[i].getClassName() + "," + customTasks[i].getLibrary().toExternalForm()); //$NON-NLS-1$
		}
		prefs.setValue(IAntCoreConstants.PREFERENCE_TASKS, tasks.toString());
	}

	protected void updateTypes(Preferences prefs) {
		if (customTypes.length == 0) {
			prefs.setValue(IAntCoreConstants.PREFERENCE_TYPES, ""); //$NON-NLS-1$
			return;
		}
		StringBuffer types = new StringBuffer();
		for (int i = 0; i < customTypes.length; i++) {
			types.append(customTypes[i].getTypeName());
			types.append(',');
			prefs.setValue(IAntCoreConstants.PREFIX_TYPE + customTypes[i].getTypeName(), customTypes[i].getClassName() + "," + customTypes[i].getLibrary().toExternalForm()); //$NON-NLS-1$
		}
		prefs.setValue(IAntCoreConstants.PREFERENCE_TYPES, types.toString());
	}

	protected void updateURLs(Preferences prefs) {
		//see if the custom URLS are just the default URLS
		URL[] dcUrls= getDefaultCustomURLs();
		boolean dflt= false;
		if (dcUrls.length == customURLs.length) {
			dflt= true;
			for (int i = 0; i < customURLs.length; i++) {
				if (!customURLs[i].equals(dcUrls[i])) {
					dflt= false;
					break;
				}
			}
		}
		if (dflt) {
			//always want to recalculate the default custom urls
			//to pick up any changes in the default classpath
			prefs.setValue(IAntCoreConstants.PREFERENCE_URLS, ""); //$NON-NLS-1$
			return;
		}
		StringBuffer urls = new StringBuffer();
		for (int i = 0; i < customURLs.length; i++) {
			urls.append(customURLs[i].toExternalForm());
			urls.append(',');
		}
		
		prefs.setValue(IAntCoreConstants.PREFERENCE_URLS, urls.toString());
	}
}