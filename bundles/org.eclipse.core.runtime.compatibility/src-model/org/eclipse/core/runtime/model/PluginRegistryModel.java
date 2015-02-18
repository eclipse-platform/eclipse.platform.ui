/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.model;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import org.eclipse.core.internal.model.PluginMap;
import org.eclipse.core.internal.model.RegistryResolver;
import org.eclipse.core.internal.plugins.InternalPlatform;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;

/**
 * A container for a collection of plug-in descriptors.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 * @deprecated In Eclipse 3.0 the runtime was refactored and all 
 * non-essential elements removed.  This class provides facilities primarily intended
 * for tooling.  As such it has been removed and no directly substitutable API provided.
 * This API will be deleted in a future release. See bug 370248 for details.
 */
public class PluginRegistryModel {

	// transient properties (not included in plug-in manifest)
	protected PluginMap plugins = new PluginMap(new HashMap(30), false, true);
	protected PluginMap fragments = new PluginMap(new HashMap(30), false, true);
	private boolean readOnly = false;
	private boolean resolved = false;

	/**
	 * Creates a new plug-in registry model which contains no plug-ins.
	 */
	public PluginRegistryModel() {
		super();
	}

	/**
	 * Adds the specified plug-in fragment to this registry.  An existing fragment
	 * with the same unique id and version is replaced by the new
	 * value.  
	 *
	 * @param fragment the plug-in fragment to add
	 */
	public void addFragment(PluginFragmentModel fragment) {
		assertIsWriteable();
		fragments.add(fragment);
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
		plugins.add(plugin);
	}

	/**
	 * Checks that this model object is writeable.  A runtime exception
	 * is thrown if it is not.
	 */
	protected void assertIsWriteable() {
		Assert.isTrue(!isReadOnly(), "Model is read-only"); //$NON-NLS-1$
	}

	/**
	 * Returns the plug-in fragment with the given identifier
	 * in this plug-in registry, or <code>null</code> if there is no such
	 * fragment.  If there are multiple versions of the identified fragment,
	 * one will be non-deterministically chosen and returned.  
	 *
	 * @param id the unique identifier of the plug-in fragment
	 *		(e.g. <code>"com.example.acme"</code>).
	 * @return the plug-in fragment, or <code>null</code>
	 */
	public PluginFragmentModel getFragment(String id) {
		return (PluginFragmentModel) fragments.getAny(id);
	}

	/**
	 * Returns the identified plug-in fragment or <code>null</code> if
	 * the fragment does not exist.
	 *
	 * @param id the unique identifier of the plug-in fragment
	 * @param version fragment version identifier. If <code>null</code> is
	 * specified, a non-deterministically chosen version of the identified fragment
	 * (if any) will be returned
	 * @return the matching fragment in this registry, or <code>null</code>
	 */
	public PluginFragmentModel getFragment(String id, String version) {
		return (PluginFragmentModel) fragments.get(id, version);
	}

	/**
	 * Returns the list of plug-in fragments managed by this registry.
	 *
	 * @return the fragments in this registry
	 */
	public PluginFragmentModel[] getFragments() {
		PluginFragmentModel[] result = new PluginFragmentModel[fragments.size()];
		fragments.copyToArray(result);
		return result;
	}

	/**
	 * Returns all versions of the identified plug-in fragment
	 * known to this plug-in registry.
	 * Returns an empty array if there are no fragments
	 * with the specified identifier.
	 *
	 * @param id the unique identifier of the plug-in fragment
	 *		(e.g. <code>"org.eclipse.core.resources"</code>).
	 * @return the fragments known to this plug-in registry with the given id
	 */
	public PluginFragmentModel[] getFragments(String id) {
		List versions = fragments.getVersions(id);
		if (versions == null || versions.isEmpty())
			return new PluginFragmentModel[0];
		return (PluginFragmentModel[]) versions.toArray(new PluginFragmentModel[versions.size()]);
	}

	/**
	 * Returns the plug-in descriptor with the given plug-in identifier
	 * in this plug-in registry, or <code>null</code> if there is no such
	 * plug-in.  If there are multiple versions of the identified plug-in,
	 * one will be non-deterministically chosen and returned.  
	 *
	 * @param pluginId the unique identifier of the plug-in 
	 *		(e.g. <code>"com.example.acme"</code>).
	 * @return the plug-in descriptor, or <code>null</code>
	 */
	public PluginDescriptorModel getPlugin(String pluginId) {
		return (PluginDescriptorModel) plugins.getAny(pluginId);
	}

	/**
	 * Returns the identified plug-in or <code>null</code> if
	 * the plug-in does not exist. 
	 *
	 * @param pluginId the unique identifier of the plug-in 
	 *		(e.g. <code>"org.eclipse.core.resources"</code>)
	 * @param version plug-in version identifier. If <code>null</code> is specified,
	 * a non-deterministically chosen version of the identified plug-in (if any)
	 * will be returned
	 * @return the matching plug-in in this registry or <code>null</code>
	 */
	public PluginDescriptorModel getPlugin(String pluginId, String version) {
		PluginDescriptorModel[] list = getPlugins(pluginId);
		if (list == null || list.length == 0)
			return null;
		if (version == null)
			// Just return the first one in the list (random)
			return list[0];

		for (int i = 0; i < list.length; i++) {
			PluginDescriptorModel element = list[i];
			if (element.getVersion().equals(version))
				return element;
		}
		return null;
	}

	/**
	 * Returns the list of plug-ins managed by this registry.
	 *
	 * @return the plug-ins in this registry
	 */
	public PluginDescriptorModel[] getPlugins() {
		PluginDescriptorModel[] result = new PluginDescriptorModel[plugins.size()];
		plugins.copyToArray(result);
		return result;
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
		List versions = plugins.getVersions(pluginId);
		if (versions == null || versions.isEmpty())
			return new PluginDescriptorModel[0];
		return (PluginDescriptorModel[]) versions.toArray(new PluginDescriptorModel[versions.size()]);

	}

	/**
	 * Returns whether or not this model object is read-only.
	 * 
	 * @return <code>true</code> if this model object is read-only,
	 *		<code>false</code> otherwise
	 * @see #markReadOnly()
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Returns whether or not this model object has been resolved.
	 * 
	 * @return <code>true</code> if this model object has been resolved,
	 *		<code>false</code> otherwise
	 */
	public boolean isResolved() {
		return resolved;
	}

	/**
	 * Sets this model object and all of its descendents to be read-only.
	 * Subclasses may extend this implementation.
	 *
	 * @see #isReadOnly()
	 */
	public void markReadOnly() {
		readOnly = true;
		plugins.markReadOnly();
		fragments.markReadOnly();
	}

	/**
	 * Sets this model object to be resolved.
	 */
	public void markResolved() {
		resolved = true;
	}

	/**
	 * Removes the fragment with id and version if it exists in this registry.
	 * This method has no effect if a fragment with the given id and version 
	 * cannot be found.
	 *
	 * @param id the unique identifier of the fragment to remove
	 * @param version the version of the fragment to remove
	 */
	public void removeFragment(String id, String version) {
		assertIsWriteable();
		fragments.remove(id, version);
	}

	/**
	 * Removes all versions of the identified plug-in fragment from this registry.
	 * This method has no effect if such a fragment cannot be found.
	 *
	 * @param id the unique identifier of the fragments to remove
	 */
	public void removeFragments(String id) {
		assertIsWriteable();
		fragments.removeVersions(id);
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
		plugins.remove(pluginId, version);
	}

	/**
	 * Removes all versions of the given plug-in from this registry.
	 * This method has no effect if such a plug-in cannot be found.
	 *
	 * @param pluginId the unique identifier of the plug-ins to remove
	 */
	public void removePlugins(String pluginId) {
		assertIsWriteable();
		plugins.removeVersions(pluginId);
	}

	/**
	 * Runs a resolve through the entire registry.  This resolve will
	 * mark any PluginDescriptorModels which do not have access to all
	 * of their prerequisites as disabled.  Prerequisites which cause
	 * cyclical dependencies will be marked as disabled.
	 * <p>
	 * If the parameter trimDisabledPlugins is set to true, all PluginDescriptorModels
	 * which are labelled as disabled will be removed from the registry.
	 * </p><p>
	 * If the paramter doCrossLinking is set to true, links will be
	 * created between ExtensionPointModels and their corresponding
	 * ExtensionModels.  Not that these links will include disabled
	 * plugins if trimDisabledPlugins was set to false.
	 * </p>
	 * @param trimDisabledPlugins if true, remove all disabled plugins
	 * from the registry (recommended value = true)
	 * @param doCrossLinking if true, link all ExtensionModels in the registry
	 * to their corresponding ExtensionPointModel (recommended value = true).
	 * @return a status object describing the result of resolving.
	 */
	public IStatus resolve(boolean trimDisabledPlugins, boolean doCrossLinking) {
		RegistryResolver resolver = new RegistryResolver();
		resolver.setTrimPlugins(trimDisabledPlugins);
		resolver.setCrossLink(doCrossLinking);
		return resolver.resolve(this);
	}

	/**
	 * Returns a plug-in registry containing all of the plug-ins discovered
	 * on the given plug-in path.  Any problems encountered are added to
	 * the status managed by the supplied factory.
	 * <p>
	 * The given plug-in path is the list of locations in which to look for plug-ins.
	 * If an entry identifies a directory (i.e., ends in a '/'), this method
	 * attempts to scan all sub-directories for plug-ins.  Alternatively, an
	 * entry may identify a particular plug-in manifest (<code>plugin.xml</code>) file.
	 * </p>
	 * <p>
	 * <b>Note:</b> this method does not affect the running platform.  It is intended
	 * for introspecting installed plug-ins on this and other platforms.  The returned
	 * registry is <b>not</b> the same as the platform's registry.
	 * </p>
	 * @param pluginPath the list of locations in which to look for plug-ins
	 * @param factory the factory to use to create runtime model objects
	 * @return the registry of parsed plug-ins
	 */
	public static PluginRegistryModel parsePlugins(URL[] pluginPath, Factory factory) {
		return InternalPlatform.parsePlugins(pluginPath, factory, false);
	}
}
