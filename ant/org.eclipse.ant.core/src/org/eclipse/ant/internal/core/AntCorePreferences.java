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
public class AntCorePreferences {

	protected List defaultTasks;
	protected List defaultTypes;
	protected List defaultURLs;
	protected Task[] customTasks;
	protected Type[] customTypes;
	protected URL[] customURLs;
	protected Map defaultObjects;
	protected List pluginClassLoaders;

	protected static final String PREFERENCES_FILE_NAME = ".preferences";

public AntCorePreferences(Map defaultTasks, Map defaultObjects, Map defaultTypes) {
	initializePluginClassLoaders();
	defaultURLs = new ArrayList(20);
	this.defaultTasks = computeDefaultTasks(defaultTasks);
	this.defaultTypes = computeDefaultTypes(defaultTypes);
	this.defaultObjects = computeDefaultObjects(defaultObjects);
	restoreCustomObjects();
}

protected void restoreCustomObjects() {
	customTasks = new Task[0];
	customTypes = new Type[0];
	customURLs = computeCustomURLs();
}

protected URL[] computeCustomURLs() {
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
			String message = "Library not specified for task: " + task.getTaskName(); // FIXME add to message.properties
			// FIXME: add error code
			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, -1, message, null);
			AntCorePlugin.getPlugin().getLog().log(status);
			continue;
		}
		IPluginDescriptor descriptor = element.getDeclaringExtension().getDeclaringPluginDescriptor();
		try {
			URL url = Platform.asLocalURL(new URL(descriptor.getInstallURL(), library));
			task.setLibrary(url);
			defaultURLs.add(url);
		} catch (Exception e) {
			// FIXME: add error code
			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, -1, e.getMessage(), e);
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
			String message = "Library not specified for type: " + type.getTypeName(); // FIXME add to message.properties
			// FIXME: add error code
			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, -1, message, null);
			AntCorePlugin.getPlugin().getLog().log(status);
			continue;
		}
		IPluginDescriptor descriptor = element.getDeclaringExtension().getDeclaringPluginDescriptor();
		try {
			URL url = Platform.asLocalURL(new URL(descriptor.getInstallURL(), library));
			type.setLibrary(url);
			defaultURLs.add(url);
		} catch (Exception e) {
			// FIXME: add error code
			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, -1, e.getMessage(), e);
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
			String message = "Library not specified for object: " + entry.getKey(); // FIXME add to message.properties
			// FIXME: add error code
			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, -1, message, null);
			AntCorePlugin.getPlugin().getLog().log(status);
			continue;
		}
		IPluginDescriptor descriptor = element.getDeclaringExtension().getDeclaringPluginDescriptor();
		try {
			URL url = Platform.asLocalURL(new URL(descriptor.getInstallURL(), library));
			defaultURLs.add(url);
		} catch (Exception e) {
			// FIXME: add error code
			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, -1, e.getMessage(), e);
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
		// FIXME: add error code
		IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, -1, e.getMessage(), e);
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
			// FIXME: add error code
			IStatus status = new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, -1, e.getMessage(), e);
			AntCorePlugin.getPlugin().getLog().log(status);
			continue;
		}
	}
}

protected void readCustomURLs() {
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

}