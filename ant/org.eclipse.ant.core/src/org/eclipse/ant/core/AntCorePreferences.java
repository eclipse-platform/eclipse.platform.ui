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
	protected List extraClasspathURLs;
	protected URL[] defaultAntURLs;
	
	protected Task[] customTasks;
	protected Type[] customTypes;
	protected URL[] antURLs;
	protected URL[] customURLs;
	protected Property[] customProperties;
	protected String[] customPropertyFiles;
	
	protected List pluginClassLoaders;
	
	private String antHome;
	
	private boolean runningHeadless= false;

	protected AntCorePreferences(List defaultTasks, List defaultExtraClasspath, List defaultTypes, boolean headless) {
		runningHeadless= headless;
		initializePluginClassLoaders();
		extraClasspathURLs = new ArrayList(20);
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
		String urls = prefs.getString(IAntCoreConstants.PREFERENCE_ANT_URLS);
		if (urls.equals("")) {//$NON-NLS-1$
			antURLs = getDefaultAntURLs();
		} else {
			antURLs = extractURLs(getArrayFromString(urls));
		}
		urls = prefs.getString(IAntCoreConstants.PREFERENCE_URLS);
		if (urls.equals("")) {//$NON-NLS-1$
			customURLs = new URL[0];
		} else {
			customURLs = extractURLs(getArrayFromString(urls));
		}
		
		antHome= prefs.getString(IAntCoreConstants.PREFERENCE_ANT_HOME);
		
		// properties
		String properties = prefs.getString(IAntCoreConstants.PREFERENCE_PROPERTIES);
		if (properties.equals("")) {//$NON-NLS-1$
			customProperties = new Property[0];
		} else {
			customProperties = extractProperties(prefs, getArrayFromString(properties));
		}
		
		String propertyFiles= prefs.getString(IAntCoreConstants.PREFERENCE_PROPERTY_FILES);
		if (propertyFiles.equals("")) { //$NON-NLS-1$
			customPropertyFiles= new String[0];
		} else {
			customPropertyFiles= getArrayFromString(propertyFiles);
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
	
	protected Property[] extractProperties(Preferences prefs, String[] properties) {
		Property[] result = new Property[properties.length];
		for (int i = 0; i < properties.length; i++) {
			String propertyName = properties[i];
			String[] values = getArrayFromString(prefs.getString(IAntCoreConstants.PREFIX_PROPERTY + propertyName));
			Property property = new Property();
			property.setName(propertyName);
			property.setValue(values[0]);
			result[i]= property;
		}
		return result;
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
	 * Xerces JARs are not added here.
	 * There are included as the plugin class loader for org.eclipse.ant.core
	 * is set as on of the plugin classloaders of the AntClassLoader.
	 * @see initializePluginClassLoaders()
	 * 
	 * Ant running through the command line tries to find tools.jar to help the
	 * user. Try emulating the same behaviour here.
	 *	 * @return the default set of URLs defining the Ant classpath	 */
	public URL[] getDefaultAntURLs() {
		if (defaultAntURLs == null) {
			List result = new ArrayList(3);
			Plugin antPlugin= Platform.getPlugin("org.apache.ant"); //$NON-NLS-1$
			if (antPlugin != null) {
				IPluginDescriptor descriptor = antPlugin.getDescriptor(); 
				addLibraries(descriptor, result);
			}
			
			URL toolsURL= getToolsJarURL();
			if (toolsURL != null) {
				result.add(toolsURL);
			}
			defaultAntURLs= (URL[]) result.toArray(new URL[result.size()]);
		}
		return defaultAntURLs;
	}
	
	/**
	 * Returns the array of URLs that is the set of URLs defining the Ant
	 * classpath.
	 * 
	 * @return the set of URLs defining the Ant classpath
	 */
	public URL[] getAntURLs() {
		return antURLs;
	}

	protected List computeDefaultTasks(List tasks) {
		List result = new ArrayList(tasks.size());
		for (Iterator iterator = tasks.iterator(); iterator.hasNext();) {
			IConfigurationElement element = (IConfigurationElement) iterator.next();
			if (runningHeadless) {
				String headless = element.getAttribute(AntCorePlugin.HEADLESS);
				if (headless != null) {
					boolean headlessType= Boolean.valueOf(headless).booleanValue();
					if (!headlessType) {
						continue;
					}
				}
			}
			Task task = new Task();
			task.setIsDefault(true);
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
				if (new File(url.getPath()).exists()) {
					if (!extraClasspathURLs.contains(url)) {
						extraClasspathURLs.add(url);
					}
					result.add(task);
					addPluginClassLoader(descriptor.getPluginClassLoader());
					task.setLibrary(url);
				} else {
					//task specifies a library that does not exist
					IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_LIBRARY_NOT_SPECIFIED, MessageFormat.format(InternalCoreAntMessages.getString("AntCorePreferences.No_library_for_task"), new String[]{url.toExternalForm(), descriptor.getLabel()}), null); //$NON-NLS-1$
					AntCorePlugin.getPlugin().getLog().log(status);
					continue;
				}
			} catch (Exception e) {
				// if the URL does not have a valid format, just log and ignore the exception
				IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_MALFORMED_URL, InternalCoreAntMessages.getString("AntCorePreferences.Malformed_URL._1"), e); //$NON-NLS-1$
				AntCorePlugin.getPlugin().getLog().log(status);
				continue;
			}
		}
		return result;
	}

	protected List computeDefaultTypes(List types) {
		List result = new ArrayList(types.size());
		for (Iterator iterator = types.iterator(); iterator.hasNext();) {
			IConfigurationElement element = (IConfigurationElement) iterator.next();
			if (runningHeadless) {
				String headless = element.getAttribute(AntCorePlugin.HEADLESS);
				if (headless != null) {
					boolean headlessTask= Boolean.valueOf(headless).booleanValue();
					if (!headlessTask) {
						continue;
					}
				}
			}
			Type type = new Type();
			type.setIsDefault(true);
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
				if (new File(url.getPath()).exists()) {
					if (!extraClasspathURLs.contains(url)) {
						extraClasspathURLs.add(url);
					}
					result.add(type);
					addPluginClassLoader(descriptor.getPluginClassLoader());
					type.setLibrary(url);
				} else {
					//type specifies a library that does not exist
					IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_LIBRARY_NOT_SPECIFIED, MessageFormat.format(InternalCoreAntMessages.getString("AntCorePreferences.No_library_for_type"), new String[]{url.toExternalForm(), descriptor.getLabel()}), null); //$NON-NLS-1$
					AntCorePlugin.getPlugin().getLog().log(status);
					continue;
				}
			} catch (Exception e) {
				// if the URL does not have a valid format, just log and ignore the exception
				IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_MALFORMED_URL, InternalCoreAntMessages.getString("AntCorePreferences.Malformed_URL._1"), e); //$NON-NLS-1$
				AntCorePlugin.getPlugin().getLog().log(status);
				continue;
			}
		}
		return result;
	}

	/**
	 * Computes the extra classpath entries defined plugins and fragments.
	 */
	protected void computeDefaultExtraClasspathEntries(List entries) {
		for (Iterator iterator = entries.iterator(); iterator.hasNext();) {
			IConfigurationElement element = (IConfigurationElement) iterator.next();
			if (runningHeadless) {
				String headless = element.getAttribute(AntCorePlugin.HEADLESS);
				if (headless != null) {
					boolean headlessEntry= Boolean.valueOf(headless).booleanValue();
					if (!headlessEntry) {
						continue;
					}
				}
			}
			String library = (String) element.getAttribute(AntCorePlugin.LIBRARY);
			IPluginDescriptor descriptor = element.getDeclaringExtension().getDeclaringPluginDescriptor();
			try {
				URL url = Platform.asLocalURL(new URL(descriptor.getInstallURL(), library));
				
				if (new File(url.getPath()).exists()) {
					if (!extraClasspathURLs.contains(url)) {
						extraClasspathURLs.add(url);
					}
					addPluginClassLoader(descriptor.getPluginClassLoader());
				} else {
					//extra classpath entry that does not exist
					IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_LIBRARY_NOT_SPECIFIED, MessageFormat.format(InternalCoreAntMessages.getString("AntCorePreferences.No_library_for_extraClasspathEntry"), new String[]{url.toExternalForm(), descriptor.getLabel()}), null); //$NON-NLS-1$
					AntCorePlugin.getPlugin().getLog().log(status);
					continue;
				}
			} catch (Exception e) {
				// if the URL does not have a valid format, just log and ignore the exception
				IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_MALFORMED_URL, InternalCoreAntMessages.getString("AntCorePreferences.Malformed_URL._1"), e); //$NON-NLS-1$
				AntCorePlugin.getPlugin().getLog().log(status);
				continue;
			}
		}
	}

	/**
	 * Returns the URL for the tools.jar associated with the "java.home"
	 * location. May return <code>null</code> if no tools.jar is found (e.g. "java.home"
	 * points to a JRE install).
	 * 
	 * @return URL tools.jar URL or <code>null</code>
	 */
	public URL getToolsJarURL() {
		IPath path = new Path(System.getProperty("java.home")); //$NON-NLS-1$
		if (path.lastSegment().equalsIgnoreCase("jre")) { //$NON-NLS-1$
			path = path.removeLastSegments(1);
		}
		path = path.append("lib").append("tools.jar"); //$NON-NLS-1$ //$NON-NLS-2$
		File tools = path.toFile();
		if (!tools.exists()) {
			//attempt to find in the older 1.1.* 
			path= path.removeLastSegments(1);
			path= path.append("classes.zip"); //$NON-NLS-1$
			tools = path.toFile();
			if (!tools.exists()) {
				return null;
			}
		}
		try {
			return new URL("file:" + tools.getAbsolutePath()); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			// if the URL does not have a valid format, just log and ignore the exception
			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_MALFORMED_URL, InternalCoreAntMessages.getString("AntCorePreferences.Malformed_URL._1"), e);  //$NON-NLS-1$
			AntCorePlugin.getPlugin().getLog().log(status);
		}
		return null;
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

	/**
	 * Return the list of urls added to the classpath by the extra classpath
	 * entries extension point.
	 * 
	 * @return URL[]
	 */
	public URL[] getExtraClasspathURLs() {
		return (URL[])extraClasspathURLs.toArray(new URL[extraClasspathURLs.size()]);
	}
	
	public URL[] getURLs() {
		List result = new ArrayList(20);
		if (antURLs != null) {
			result.addAll(Arrays.asList(antURLs));
		}
		if (customURLs != null && customURLs.length > 0) {
			result.addAll(Arrays.asList(customURLs));
		}
		if (extraClasspathURLs != null) {
			result.addAll(extraClasspathURLs);
		}
		return (URL[]) result.toArray(new URL[result.size()]);
	}

	protected ClassLoader[] getPluginClassLoaders() {
		return (ClassLoader[]) pluginClassLoaders.toArray(new ClassLoader[pluginClassLoaders.size()]);
	}

	protected void initializePluginClassLoaders() {
		pluginClassLoaders = new ArrayList(10);
		// ant.core should always be present (provides access to Xerces as well)
		pluginClassLoaders.add(Platform.getPlugin(AntCorePlugin.PI_ANTCORE).getDescriptor().getPluginClassLoader());
	}

	/**
	 * Returns the default and custom tasks.
	 * 
	 * @return the list of default and custom tasks.
	 */
	public List getTasks() {
		List result = new ArrayList(10);
		if (defaultTasks != null && !defaultTasks.isEmpty()) {
			result.addAll(defaultTasks);
		}
		if (customTasks != null && customTasks.length != 0) {
			result.addAll(Arrays.asList(customTasks));
		}
		return result;
	}

	/**
	 * Returns the user defined custom tasks
	 * @return the user defined tasks
	 */
	public Task[] getCustomTasks() {
		return customTasks;
	}

	/**
	 * Returns the user defined custom types
	 * @return the user defined types
	 */
	public Type[] getCustomTypes() {
		return customTypes;
	}

	/**
	 * Returns the custom user properties specified for Ant builds.
	 * 
	 * @return the properties defined for Ant builds.
	 * @since 2.1
	 */
	public Property[] getCustomProperties() {
		return customProperties;
	}
	
	/**
	 * Returns the custom property files specified for Ant builds.
	 * 
	 * @return the property files defined for Ant builds.
	 * @since 2.1
	 */
	public String[] getCustomPropertyFiles() {
		return customPropertyFiles;
	}
	
	/**
	 * Returns the custom URLs specified for the Ant classpath
	 * 
	 * @return the urls defining the Ant classpath
	 */
	public URL[] getCustomURLs() {
		return customURLs;
	}

	/**
	 * Sets the user defined custom tasks
	 * @param tasks
	 */
	public void setCustomTasks(Task[] tasks) {
		customTasks = tasks;
	}

	/**
	 * Sets the user defined custom types
	 * @param tasks
	 */
	public void setCustomTypes(Type[] types) {
		customTypes = types;
	}

	/**
	 * Sets the custom URLs specified for the Ant classpath.
	 * To commit the changes, updatePluginPreferences must be
	 * called.
	 * 
	 * @param the urls defining the Ant classpath
	 */
	public void setCustomURLs(URL[] urls) {
		customURLs = urls;
	}
	
	/**
	 * Sets the Ant URLs specified for the Ant classpath. To commit the changes,
	 * updatePluginPreferences must be called.
	 * 
	 * @param the urls defining the Ant classpath
	 */
	public void setAntURLs(URL[] urls) {
		antURLs = urls;
	}
	
	/**
	 * Sets the custom property files specified for Ant builds. To commit the
	 * changes, updatePluginPreferences must be called.
	 * 
	 * @param the absolute paths defining the property files to use.
	 * @since 2.1
	 */
	public void setCustomPropertyFiles(String[] paths) {
		customPropertyFiles = paths;
	}
	
	/**
	 * Sets the custom user properties specified for Ant builds. To commit the
	 * changes, updatePluginPreferences must be called.
	 * 
	 * @param the properties defining the Ant properties
	 * @since 2.1
	 */
	public void setCustomProperties(Property[] properties) {
		customProperties = properties;
	}

	/**
	 * Returns the default and custom types.
	 * 
	 * @return all of the defined types
	 */
	public List getTypes() {
		List result = new ArrayList(10);
		if (defaultTypes != null && !defaultTypes.isEmpty()) {
			result.addAll(defaultTypes);
		}
		if (customTypes != null && customTypes.length != 0) {
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
		updateAntURLs(prefs);
		updateURLs(prefs);
		updateProperties(prefs);
		updatePropertyFiles(prefs);
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
	
	protected void updateProperties(Preferences prefs) {
		if (customProperties.length == 0) {
			prefs.setValue(IAntCoreConstants.PREFERENCE_PROPERTIES, ""); //$NON-NLS-1$
			return;
		}
		StringBuffer properties = new StringBuffer();
		for (int i = 0; i < customProperties.length; i++) {
			properties.append(customProperties[i].getName());
			properties.append(',');
			prefs.setValue(IAntCoreConstants.PREFIX_PROPERTY + customProperties[i].getName(), customProperties[i].getValue()); //$NON-NLS-1$
		}
		prefs.setValue(IAntCoreConstants.PREFERENCE_PROPERTIES, properties.toString());
	}

	protected void updateURLs(Preferences prefs) {
		StringBuffer urls = new StringBuffer();
		for (int i = 0; i < customURLs.length; i++) {
			urls.append(customURLs[i].toExternalForm());
			urls.append(',');
		}
		
		prefs.setValue(IAntCoreConstants.PREFERENCE_URLS, urls.toString());
		String prefAntHome= ""; //$NON-NLS-1$
		if (antHome != null) {
			prefAntHome= antHome;
		} 
		prefs.setValue(IAntCoreConstants.PREFERENCE_ANT_HOME, prefAntHome);
	}
	
	protected void updateAntURLs(Preferences prefs) {
		//see if the custom URLS are just the default URLS
		URL[] dcUrls= getDefaultAntURLs();
		boolean dflt= false;
		if (dcUrls.length == antURLs.length) {
			dflt= true;
			for (int i = 0; i < antURLs.length; i++) {
				if (!antURLs[i].equals(dcUrls[i])) {
					dflt= false;
					break;
				}
			}
		}
		if (dflt) {
			//always want to recalculate the default Ant urls
			//to pick up any changes in the default Ant classpath
			prefs.setValue(IAntCoreConstants.PREFERENCE_ANT_URLS, ""); //$NON-NLS-1$
			return;
		}
		StringBuffer urls = new StringBuffer();
		for (int i = 0; i < antURLs.length; i++) {
			urls.append(antURLs[i].toExternalForm());
			urls.append(',');
		}
		
		prefs.setValue(IAntCoreConstants.PREFERENCE_ANT_URLS, urls.toString());
	}
	
	protected void updatePropertyFiles(Preferences prefs) {
		StringBuffer files = new StringBuffer();
		for (int i = 0; i < customPropertyFiles.length; i++) {
			files.append(customPropertyFiles[i]);
			files.append(',');
		}
		
		prefs.setValue(IAntCoreConstants.PREFERENCE_PROPERTY_FILES, files.toString());
	}
	
	public void setAntHome(String antHome) {
		this.antHome= antHome;
	}
	
	public String getAntHome() {
		return antHome;
	}
}