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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.ant.internal.core.IAntCoreConstants;
import org.eclipse.ant.internal.core.InternalCoreAntMessages;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ILibrary;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;

public class AntCorePreferences {

	protected List fDefaultTasks;
	protected List fDefaultTypes;
	protected List fDefaultURLs;
	protected Task[] fCustomTasks;
	protected Type[] fCustomTypes;
	protected URL[] fCustomURLs;
	protected URL[] fDefaultCustomURLs;
	
	protected Map fDefaultObjects;
	protected List fPluginClassLoaders;

	public AntCorePreferences(Map defaultTasks, Map defaultExtraClasspath, Map defaultTypes) {
		initializePluginClassLoaders();
		fDefaultURLs = new ArrayList(20);
		fDefaultTasks = computeDefaultTasks(defaultTasks);
		fDefaultTypes = computeDefaultTypes(defaultTypes);
		computeDefaultExtraClasspathEntries(defaultExtraClasspath);
		restoreCustomObjects();
	}

	protected void restoreCustomObjects() {
		Preferences prefs = AntCorePlugin.getPlugin().getPluginPreferences();
		// tasks
		String tasks = prefs.getString(IAntCoreConstants.PREFERENCE_TASKS);
		if (tasks.equals("")) { //$NON-NLS-1$
			fCustomTasks = new Task[0];
		} else {
			fCustomTasks = extractTasks(prefs, getArrayFromString(tasks));
		}
		// types
		String types = prefs.getString(IAntCoreConstants.PREFERENCE_TYPES);
		if (types.equals("")) {//$NON-NLS-1$
			fCustomTypes = new Type[0];
		} else {
			fCustomTypes = extractTypes(prefs, getArrayFromString(types));
		}
		// urls
		String urls = prefs.getString(IAntCoreConstants.PREFERENCE_URLS);
		if (urls.equals("")) {//$NON-NLS-1$
			fCustomURLs = getDefaultCustomURLs();
		} else {
			fCustomURLs = extractURLs(getArrayFromString(urls));
		}
	}

	protected Task[] extractTasks(Preferences prefs, String[] tasks) {
		List result = new ArrayList(10);
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
		List result = new ArrayList(10);
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
		List result = new ArrayList(10);
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

	public URL[] getDefaultCustomURLs() {
		if (fDefaultCustomURLs == null) {
			List result = new ArrayList(10);
			IPluginDescriptor descriptor = Platform.getPlugin("org.apache.ant").getDescriptor(); //$NON-NLS-1$
			addLibraries(descriptor, result);
			descriptor = Platform.getPlugin("org.apache.xerces").getDescriptor(); //$NON-NLS-1$
			addLibraries(descriptor, result);
			addToolsJar(result);
			fDefaultCustomURLs= (URL[]) result.toArray(new URL[result.size()]);
		}
		return fDefaultCustomURLs;
	}

	protected List computeDefaultTasks(Map tasks) {
		List result = new ArrayList(10);
		for (Iterator iterator = tasks.entrySet().iterator();iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			Task task = new Task();
			task.setTaskName((String) entry.getKey());
			IConfigurationElement element =(IConfigurationElement) entry.getValue();
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
				fDefaultURLs.add(url);
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

	protected List computeDefaultTypes(Map types) {
		List result = new ArrayList(10);
		for (Iterator iterator = types.entrySet().iterator();iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			Type type = new Type();
			type.setTypeName((String) entry.getKey());
			IConfigurationElement element =(IConfigurationElement) entry.getValue();
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
				fDefaultURLs.add(url);
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
	protected void computeDefaultExtraClasspathEntries(Map entries) {
		for (Iterator iterator = entries.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String library = (String) entry.getKey();
			IConfigurationElement element =(IConfigurationElement) entry.getValue();
			IPluginDescriptor descriptor = element.getDeclaringExtension().getDeclaringPluginDescriptor();
			try {
				URL url = Platform.asLocalURL(new URL(descriptor.getInstallURL(), library));
				fDefaultURLs.add(url);
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
		if (!fPluginClassLoaders.contains(loader)) {
			fPluginClassLoaders.add(loader);
		}
	}

	public URL[] getURLs() {
		List result = new ArrayList(10);
		if (fDefaultURLs != null) {
			result.addAll(fDefaultURLs);
		}
		if (fCustomURLs != null) {
			result.addAll(Arrays.asList(fCustomURLs));
		}
		return (URL[]) result.toArray(new URL[result.size()]);
	}

	public ClassLoader[] getPluginClassLoaders() {
		return (ClassLoader[]) fPluginClassLoaders.toArray(
			new ClassLoader[fPluginClassLoaders.size()]);
	}

	protected void initializePluginClassLoaders() {
		fPluginClassLoaders = new ArrayList(20);
		// ant.core should always be present
		fPluginClassLoaders.add(Platform.getPlugin(AntCorePlugin.PI_ANTCORE).getDescriptor().getPluginClassLoader());
	}

	/**
	 * Returns default + custom tasks.
	 */
	public List getTasks() {
		List result = new ArrayList(10);
		if (fDefaultTasks != null) {
			result.addAll(fDefaultTasks);
		}
		if (fCustomTasks != null) {
			result.addAll(Arrays.asList(fCustomTasks));
		}
		return result;
	}

	public Task[] getCustomTasks() {
		return fCustomTasks;
	}

	public Type[] getCustomTypes() {
		return fCustomTypes;
	}

	public URL[] getCustomURLs() {
		return fCustomURLs;
	}

	public void setCustomTasks(Task[] tasks) {
		fCustomTasks = tasks;
	}

	public void setCustomTypes(Type[] types) {
		fCustomTypes = types;
	}

	public void setCustomURLs(URL[] urls) {
		fCustomURLs = urls;
	}

	/**
	 * Returns default + custom types.
	 */
	public List getTypes() {
		List result = new ArrayList(10);
		if (fDefaultTypes != null) {
			result.addAll(fDefaultTypes);
		}
		if (fCustomTypes != null) {
			result.addAll(Arrays.asList(fCustomTypes));
		}
		return result;
	}

	/**
	 * Convert a list of tokens into an array. The list separator has to be specified.
	 */
	public static String[] getArrayFromString(String list, String separator) {
		if (list == null || list.trim().equals("")) { //$NON-NLS-1$
			return new String[0];
		}
		ArrayList result = new ArrayList();
		for (StringTokenizer tokens = new StringTokenizer(list, separator);tokens.hasMoreTokens();) {
			String token = tokens.nextToken().trim();
			if (!token.equals("")) { //$NON-NLS-1$
				result.add(token);
			}
		}
		return (String[]) result.toArray(new String[result.size()]);
	}

	/**
	 * Convert a list of comma-separated tokens into an array
	 */
	public static String[] getArrayFromString(String list) {
		return getArrayFromString(list, ","); //$NON-NLS-1$
	}

	public void updatePluginPreferences() {
		Preferences prefs = AntCorePlugin.getPlugin().getPluginPreferences();
		updateTasks(prefs);
		updateTypes(prefs);
		updateURLs(prefs);
	}

	protected void updateTasks(Preferences prefs) {
		if (fCustomTasks.length == 0) {
			prefs.setValue(IAntCoreConstants.PREFERENCE_TASKS, ""); //$NON-NLS-1$
			return;
		}
		StringBuffer tasks = new StringBuffer();
		for (int i = 0; i < fCustomTasks.length; i++) {
			tasks.append(fCustomTasks[i].getTaskName());
			tasks.append(',');
			prefs.setValue(IAntCoreConstants.PREFIX_TASK + fCustomTasks[i].getTaskName(), fCustomTasks[i].getClassName() + "," + fCustomTasks[i].getLibrary().toExternalForm()); //$NON-NLS-1$
		}
		prefs.setValue(IAntCoreConstants.PREFERENCE_TASKS, tasks.toString());
	}

	protected void updateTypes(Preferences prefs) {
		if (fCustomTypes.length == 0) {
			prefs.setValue(IAntCoreConstants.PREFERENCE_TYPES, ""); //$NON-NLS-1$
			return;
		}
		StringBuffer types = new StringBuffer();
		for (int i = 0; i < fCustomTypes.length; i++) {
			types.append(fCustomTypes[i].getTypeName());
			types.append(',');
			prefs.setValue(IAntCoreConstants.PREFIX_TYPE + fCustomTypes[i].getTypeName(), fCustomTypes[i].getClassName() + "," + fCustomTypes[i].getLibrary().toExternalForm()); //$NON-NLS-1$
		}
		prefs.setValue(IAntCoreConstants.PREFERENCE_TYPES, types.toString());
	}

	protected void updateURLs(Preferences prefs) {
		//see if the custom URLS are just the default URLS
		URL[] defaultUrls= getDefaultCustomURLs();
		boolean dflt= false;
		if (defaultUrls.length == fCustomURLs.length) {
			dflt= true;
			for (int i = 0; i < fCustomURLs.length; i++) {
				if (!fCustomURLs[i].equals(defaultUrls[i])) {
					dflt= false;
					break;
				}
			}
		}
		if (dflt) {
			//always want to recalculate the default custom urls
			//to pick up any changes in the default classpath
			prefs.setValue(IAntCoreConstants.PREFERENCE_URLS, "");
			return;
		}
		StringBuffer urls = new StringBuffer();
		for (int i = 0; i < fCustomURLs.length; i++) {
			urls.append(fCustomURLs[i].toExternalForm());
			urls.append(',');
		}
		
		prefs.setValue(IAntCoreConstants.PREFERENCE_URLS, urls.toString());
	}
}