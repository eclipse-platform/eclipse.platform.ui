/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.registry;

/**
 * The extension registry holds the master list of all
 * discovered hosts, extension points and extensions. 
 * <p>
 * The extension registry can be queried, by name, for 
 * extension points and extensions.  
 * </p>
 * <p>
 * Extensions and extension points are declared by generic entities called 
 * <cite>hosts</cite>. The only fact known about hosts is that they have unique 
 * string-based identifiers. 
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @since 3.0
 */
//TODO: check missing @param and @return and explain if returns null / empty array
public interface IExtensionRegistry {
	/**
	* Adds the given listener for registry change events related to the given host.
	* Has no effect if an identical listener is already registered. After 
	* completion of this method, the given listener will be registered for events 
	* related to the exactly the specified host. If no host identifier is specified,
	* the listener will receive notifications for changes to any host.  
	* <p>
	* Once registered, a listener starts receiving notification of changes to
	*  the registry. Registry change notifications are sent asynchronously.
	* The listener continues to receive notifications until it is removed. 
	* </p>
	* @param listener the listener
	* @param hostId the identifier of the host to which to listen for changes
	* @see IRegistryChangeListener
	* @see IRegistryChangeEvent
	* @see #removeRegistryChangeListener 
	*/
	public void addRegistryChangeListener(IRegistryChangeListener listener, String hostId);
	/**
	* Adds the given listener for registry change events.
	* Has no effect if an identical listener is already registered.
	* 
	* <p>
	* This method is equivalent to:
	* <pre>
	*     addRegistryChangeListener(listener,null);
	* </pre>
	* </p>
	* 
	* @param listener the listener
	* @see IRegistryChangeListener
	* @see IRegistryChangeEvent
	* @see #addRegistryChangeListener(IRegistryChangeListener, String)
	* @see #removeRegistryChangeListener
	*/	
	public void addRegistryChangeListener(IRegistryChangeListener listener);
	/**
	 * Returns all configuration elements from all extensions configured
	 * into the identified extension point. Returns an empty array if the extension 
	 * point does not exist, has no extensions configured, or none of the extensions 
	 * contain configuration elements.
	 *
	 * @param extensionPointId the unique identifier of the extension point
	 *		(e.g. <code>"org.eclipse.core.resources.builders"</code>)
	 * @return the configuration elements
	 */
	public IConfigurationElement[] getConfigurationElementsFor(String extensionPointId);
	/**
	 * Returns all configuration elements from all extensions configured
	 * into the identified extension point. Returns an empty array if the extension 
	 * point does not exist, has no extensions configured, or none of the extensions 
	 * contain configuration elements.
	 *
	 * @param hostId the unique identifier of the host 
	 *		(e.g. <code>"org.eclipse.core.resources"</code>)
	 * @param extensionPointName the simple identifier of the 
	 *		extension point (e.g. <code>"builders"</code>)
	 * @return the configuration elements
	 */
	public IConfigurationElement[] getConfigurationElementsFor(String hostId, String extensionPointName);
	/**
	 * Returns all configuration elements from the identified extension.
	 * Returns an empty array if the extension does not exist or 
	 * contains no configuration elements.
	 *
	 * @param hostId the unique identifier of the host 
	 *		(e.g. <code>"org.eclipse.core.resources"</code>)
	 * @param extensionPointName the simple identifier of the 
	 *		extension point (e.g. <code>"builders"</code>)
	 * @param extensionId the unique identifier of the extension 
	 *		(e.g. <code>"com.example.acme.coolbuilder</code>)
	 * @return the configuration elements
	 */
	public IConfigurationElement[] getConfigurationElementsFor(String hostId, String extensionPointName, String extensionId);
	/**
	 * Returns the specified extension in this extension registry, 
	 * or <code>null</code> if there is no such extension.
	 * The first parameter identifies the extension point, and the second
	 * parameter identifies an extension plugged in to that extension point.
	 *
	 * @param extensionPointId the unique identifier of the extension point
	 *		(e.g. <code>"org.eclipse.core.resources.builders"</code>)
	 * @param extensionId the unique identifier of the extension 
	 *		(e.g. <code>"com.example.acme.coolbuilder"</code>)
	 * @return the extension, or <code>null</code>
	 */
	public IExtension getExtension(String extensionPointId, String extensionId);
	/**
	 * Returns the specified extension in this extension registry, 
	 * or <code>null</code> if there is no such extension.
	 * The first two parameters identify the extension point, and the third
	 * parameter identifies an extension plugged in to that extension point.
	 *
	 * @param hostId the unique identifier of the host 
	 *		(e.g. <code>"org.eclipse.core.resources"</code>)
	 * @param extensionPointName the simple identifier of the 
	 *		extension point (e.g. <code>"builders"</code>)
	 * @param extensionId the unique identifier of the extension 
	 *		(e.g. <code>"com.example.acme.coolbuilder"</code>)
	 * @return the extension, or <code>null</code>
	 */
	public IExtension getExtension(String hostId, String extensionPointName, String extensionId);

	/**
	 * Returns the extension point with the given extension point identifier
	 * in this extension registry, or <code>null</code> if there is no such
	 * extension point.
	 *
	 * @param extensionPointId the unique identifier of the extension point 
	 *    (e.g., <code>"org.eclipse.core.resources.builders"</code>)
	 * @return the extension point, or <code>null</code>
	 */
	public IExtensionPoint getExtensionPoint(String extensionPointId);
	/**
	 * Returns the extension point in this extension registry
	 * with the given host identifier and extension point simple identifier,
	 * or <code>null</code> if there is no such extension point.
	 *
	 * @param hostId the unique identifier of the host 
	 *		(e.g. <code>"org.eclipse.core.resources"</code>)
	 * @param extensionPointName the simple identifier of the 
	 *		extension point (e.g. <code>" builders"</code>)
	 * @return the extension point, or <code>null</code>
	 */
	public IExtensionPoint getExtensionPoint(String hostId, String extensionPointName);

	/**
	 * Returns all extension points known to this extension registry.
	 * Returns an empty array if there are no extension points.
	 *
	 * @return the extension points known to this extension registry
	 */
	public IExtensionPoint[] getExtensionPoints();
	/**
	 * Returns all extension points declared by the given host.
	 * 
	 * @return the extension points in this registry declared by the given host 
	 */
	public IExtensionPoint[] getExtensionPoints(String hostId);
	/**
	 * Returns all extensions declared by the given host. 
	 * 
	 * @return the extensions in this registry declared by the given host 
	 */
	public IExtension[] getExtensions(String hostId);
	/**
	 * Returns all hosts that declare extensions and/or extension points.
	 * 
	 * @return the identifiers of all hosts known to this registry
	 */
	public String[] getHostIdentifiers();
	/** 
	 * Removes the given registry change listener from this registry.
	 * Has no effect if an identical listener is not registered.
	 *
	 * @param listener the listener
	 * @see IRegistryChangeListener
	 * @see #addRegistryChangeListener
	 */
	public void removeRegistryChangeListener(IRegistryChangeListener listener);
}