package org.eclipse.core.runtime.model;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.internal.runtime.Assert;
import org.eclipse.core.internal.plugins.RegistryResolver;
import java.util.*;

/**
 * A container for a collection of plug-in descriptors.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 */
public class PluginRegistryModel {

	// transient properties (not included in plug-in manifest)
	private Map plugins = new HashMap(30);
	private boolean readOnly = false;
	private boolean resolved = false;
/**
 * Creates a new plug-in registry model which contains no plug-ins.
 */
public PluginRegistryModel() {
	super();
}
/**
 * Adds the specified plug-in to this registry.  An existing plug-in
 * with the same unique id and version is replaced by the new
 * value.  
 *
 * @param plugin the plug-in descriptor to add
 */
public void addPlugin(PluginDescriptorModel plugin) {
	assertIsWriteable();
	String key = plugin.getId();
	PluginDescriptorModel[] pluginList = getPlugins(key);
	if (pluginList == null) {
		pluginList = new PluginDescriptorModel[1];
		pluginList[0] = plugin;
		plugins.put(key, pluginList);
	} else {
		PluginDescriptorModel[] newPluginList = new PluginDescriptorModel[pluginList.length + 1];
		System.arraycopy(pluginList, 0, newPluginList, 0, pluginList.length);
		newPluginList[pluginList.length] = plugin;
		plugins.put(key, newPluginList);
	}
}
/**
 * Checks that this model object is writeable.  A runtime exception
 * is thrown if it is not.
 */
protected void assertIsWriteable() {
	Assert.isTrue(!isReadOnly(), "Model is read-only");
}
/**
 * Returns the plug-in descriptor with the given plug-in identifier
 * in this plug-in registry, or <code>null</code> if there is no such
 * plug-in.  If there are multiple versions of the identified plug-in,
 * one will be non-deterministically choosen and returned.  
 *
 * @param pluginId the unique identifier of the plug-in 
 *		(e.g. <code>"com.example.acme"</code>).
 * @return the plug-in descriptor, or <code>null</code>
 */
public PluginDescriptorModel getPlugin(String pluginId) {
	PluginDescriptorModel[] result = (PluginDescriptorModel[]) plugins.get(pluginId);
	return result == null ? null : result[0];
}
/**
 * Returns the identified plug-in or <code>null</code> if
 * the plug-in does not exist.
 *
 * @return the matching plug-in in this registry
 */
public PluginDescriptorModel getPlugin(String pluginId, String version) {

	PluginDescriptorModel[] plugins = getPlugins(pluginId);
	if (plugins == null || plugins.length == 0)
		return null;
	if (version == null)
		// Just return the first one in the list (random)
		return plugins[0];

	for (int i = 0; i < plugins.length; i++) {
		PluginDescriptorModel element = plugins[i];
		if (element.getVersion().equals(version))
			return element;
	}
	return null;
}
/**
 * Returns the of plug-ins managed by this registry.
 *
 * @return the plug-ins in this registry
 */
public PluginDescriptorModel[] getPlugins() {
	List result = new ArrayList(plugins.size());
	for (Iterator i = plugins.values().iterator(); i.hasNext();) {
		PluginDescriptorModel[] entries = (PluginDescriptorModel[]) i.next();
		for (int j = 0; j < entries.length; j++)
			result.add(entries[j]);
	}
	return (PluginDescriptorModel[]) result.toArray(new PluginDescriptorModel[result.size()]);
}
/**
 * Returns all versions of the identified plug-in descriptor
 * known to this plug-in registry.
 * Returns an empty array if there are no plug-ins
 * with the specified identifier.
 *
 * @param pluginId the unique identifier of the plug-in 
 *		(e.g. <code>"org.eclipse.core.resources"</code>).
 * @return the plug-in descriptors known to this plug-in registry
 */
public PluginDescriptorModel[] getPlugins(String pluginId) {
	return (PluginDescriptorModel[]) plugins.get(pluginId);
}
/**
 * Returns whether or not this model object is read-only.
 * 
 * @return <code>true</code> if this model object is read-only,
 *		<code>false</code> otherwise
 * @see #markReadOnly
 */
public boolean isReadOnly() {
	return readOnly;
}
/**
 * Returns whether or not this model object has been resolved.
 * 
 * @return <code>true</code> if this model object has been resolved,
 *		<code>false</code> otherwise
 * @see #resolve
 */
public boolean isResolved() {
	return resolved;
}
/**
 * Sets this model object and all of its descendents to be read-only.
 * Subclasses may extend this implementation.
 *
 * @see #isReadOnly
 */
public void markReadOnly() {
	readOnly = true;
	for (Iterator it = plugins.values().iterator(); it.hasNext();) {
		PluginDescriptorModel[] list = (PluginDescriptorModel[]) it.next();
		for (int i = 0; i < list.length; i++)
			list[i].markReadOnly();
	}
}
/**
 * Sets this model object to be resolved.
 */
public void markResolved() {
	resolved = true;
}
/**
 * Removes the plug-in with id and version if it exists in this registry.
 * This method has no effect if a plug-in with the given id and version 
 * cannot be found.
 *
 * @param pluginId the unique identifier of the plug-in to remove
 * @param version the version of the plug-in to remove
 */
public void removePlugin(String pluginId, String version) {
	assertIsWriteable();
	PluginDescriptorModel[] plugins = getPlugins(pluginId);
	if (plugins == null || plugins.length == 0)
		return;
	int removedCount = 0;
	for (int i = 0; i < plugins.length; i++) {
		if (version.equals(plugins[i].getVersion())) {
			plugins[i] = null;
			removedCount++;
		}
	}
	// If all were removed, toss the whole entry.  Otherwise, compact the array
	if (removedCount == plugins.length)
		removePlugins(pluginId);
	else {
		PluginDescriptorModel[] newList = new PluginDescriptorModel[plugins.length - removedCount];
		int index = 0;
		for (int i = 0; i < plugins.length; i++) {
			if (plugins[i] != null)
				newList[index++] = plugins[i];
		}
		this.plugins.put(pluginId,newList);
	}
}
/**
 * Removes all versions of the given plug-in from this registry.
 * This method has no effect if such a plug-in cannot be found.
 *
 * @param pluginId the unique identifier of the plug-ins to remove
 */
public void removePlugins(String pluginId) {
	assertIsWriteable();
	plugins.remove(pluginId);
}
/**
 * Runs a resolve through the entire registry.  This resolve will
 * mark any PluginDescriptorModels which do not have access to all
 * of their prerequisites as disabled.  Prerequisites which cause
 * cyclical dependencies will be marked as disabled.
 *
 * If the parameter trimDisabledPlugins is set to true, all PluginDescriptorModels
 * which are labelled as disabled will be removed from the registry.
 *
 * If the paramter doCrossLinking is set to true, links will be
 * created between ExtensionPointModels and their corresponding
 * ExtensionModels.  Not that these links will include disabled
 * plugins if trimDisabledPlugins was set to false.
 *
 * @param trimDisabledPlugins if true, remove all disabled plugins
 * from the registry (recommended value = true)
 *
 * @param doCrossLinking if true, link all ExtensionModels in the registry
 * to their corresponding ExtensionPointModel (recommended value = true).
 */
public IStatus resolve(boolean trimDisabledPlugins, boolean doCrossLinking) {
	RegistryResolver resolver = new RegistryResolver();
	resolver.setTrimPlugins(trimDisabledPlugins);
	resolver.setCrossLinking(doCrossLinking);
	return resolver.resolve(this);
}
}
