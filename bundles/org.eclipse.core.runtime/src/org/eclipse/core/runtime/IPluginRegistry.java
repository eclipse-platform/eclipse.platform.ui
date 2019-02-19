/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

/**
 * As the org.eclipse.core.runtime.compatibility plug-in has been removed in
 * Eclipse 4.6 this interface is not supported anymore.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noreference This interface is not intended to be referenced by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 *
 *              This interface is planned to be deleted, see
 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=544339
 *
 */
@Deprecated
public interface IPluginRegistry {
	/**
	 * As the org.eclipse.core.runtime.compatibility plug-in has been removed in
	 * Eclipse 4.6 this method is not supported anymore.
	 *
	 * @deprecated Replaced by {@link IExtensionRegistry#getConfigurationElementsFor(String)}
	 *             .
	 */
	@Deprecated
	public IConfigurationElement[] getConfigurationElementsFor(String extensionPointId);

	/**
	 * As the org.eclipse.core.runtime.compatibility plug-in has been removed in
	 * Eclipse 4.6 this method is not supported anymore.
	 *
	 * @deprecated Replaced by {@link IExtensionRegistry#getConfigurationElementsFor(String, String)}.
	 */
	@Deprecated
	public IConfigurationElement[] getConfigurationElementsFor(String pluginId, String extensionPointName);

	/**
	 * As the org.eclipse.core.runtime.compatibility plug-in has been removed in
	 * Eclipse 4.6 this method is not supported anymore.
	 *
	 * @deprecated Replaced by {@link IExtensionRegistry#getConfigurationElementsFor(String, String, String)}.
	 */
	@Deprecated
	public IConfigurationElement[] getConfigurationElementsFor(String pluginId, String extensionPointName, String extensionId);

	/**
	 * As the org.eclipse.core.runtime.compatibility plug-in has been removed in
	 * Eclipse 4.6 this method is not supported anymore.
	 *
	 * @deprecated Replaced by {@link IExtensionRegistry#getExtension(String, String)}.
	 */
	@Deprecated
	public IExtension getExtension(String extensionPointId, String extensionId);

	/**
	 * As the org.eclipse.core.runtime.compatibility plug-in has been removed in
	 * Eclipse 4.6 this method is not supported anymore.
	 *
	 * @deprecated Replaced by {@link IExtensionRegistry#getExtension(String, String, String)}.
	 */
	@Deprecated
	public IExtension getExtension(String pluginId, String extensionPointName, String extensionId);

	/**
	 * As the org.eclipse.core.runtime.compatibility plug-in has been removed in
	 * Eclipse 4.6 this method is not supported anymore.
	 * @deprecated Replaced by {@link IExtensionRegistry#getExtensionPoint(String)}.
	 */
	@Deprecated
	public IExtensionPoint getExtensionPoint(String extensionPointId);

	/**
	 * As the org.eclipse.core.runtime.compatibility plug-in has been removed in
	 * Eclipse 4.6 this method is not supported anymore.
	 *
	 * @deprecated Replaced by {@link IExtensionRegistry#getExtensionPoint(String, String)}.
	 */
	@Deprecated
	public IExtensionPoint getExtensionPoint(String pluginId, String extensionPointName);

	/**
	 * As the org.eclipse.core.runtime.compatibility plug-in has been removed in
	 * Eclipse 4.6 this method is not supported anymore.
	 *
	 * @deprecated Replaced by {@link IExtensionRegistry#getExtensionPoints()}.
	 */
	@Deprecated
	public IExtensionPoint[] getExtensionPoints();

	/**
	 * Returns the plug-in descriptor with the given plug-in identifier
	 * in this plug-in registry, or <code>null</code> if there is no such
	 * plug-in.  If there are multiple versions of the identified plug-in,
	 * one will be non-deterministically chosen and returned.
	 *
	 * @param pluginId the unique identifier of the plug-in
	 *		(e.g. <code>"com.example.acme"</code>).
	 * @return the plug-in descriptor, or <code>null</code>
	 * @deprecated
	 * <code>IPluginDescriptor</code> was refactored in Eclipse 3.0.
	 * The <code>getPluginDescriptor()</code> method may only be called by plug-ins
	 * which explicitly require the org.eclipse.core.runtime.compatibility plug-in.
	 * See the comments on {@link IPluginDescriptor} and its methods for details.
	 */
	@Deprecated
	public IPluginDescriptor getPluginDescriptor(String pluginId);

	/**
	 * As the org.eclipse.core.runtime.compatibility plug-in has been removed in
	 * Eclipse 4.6 this method is not supported anymore.
	 */
	@Deprecated
	public IPluginDescriptor getPluginDescriptor(String pluginId, PluginVersionIdentifier version);

	/**
	 * As the org.eclipse.core.runtime.compatibility plug-in has been removed in
	 * Eclipse 4.6 this method is not supported anymore.
	 */
	@Deprecated
	public IPluginDescriptor[] getPluginDescriptors();

	/**
	 * As the org.eclipse.core.runtime.compatibility plug-in has been removed in
	 * Eclipse 4.6 this method is not supported anymore.
	 */
	@Deprecated
	public IPluginDescriptor[] getPluginDescriptors(String pluginId);
}
