/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

/**
 * An extension declared in a plug-in.
 * All information is obtained from the declaring plug-in's 
 * manifest (<code>plugin.xml</code>) file.
 * <p>
 * These registry objects are intended for relatively short-term use. Clients that 
 * need to retain an object must be aware that it may become invalid if the 
 * declaring plug-in is updated or uninstalled. If this happens, all methods except 
 * {@link #isValid()} will throw an {@link org.eclipse.core.runtime.InvalidRegistryObjectException}.
 *  Clients may check for invalid objects by calling {@link #isValid()}.
 * More generally, clients may registry a listener with the extension registry to receive
 * notification of changes.
 * Due to the concurrent nature of eclipse, an isValid() check does not save you from the exception 
 * checks, since the object you are using can be uninstalled while you are processing it.
 * 
 * A plug-in declaring that it is not dynamic aware can ignore the InvalidRegistryObjectExceptions.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IExtension {
	/**
	 * Returns all configuration elements declared by this extension.
	 * These elements are a direct reflection of the configuration 
	 * markup supplied in the manifest (<code>plugin.xml</code>)
	 * file for the plug-in that declares this extension.
	 * Returns an empty array if this extension does not declare any
	 * configuration elements.
	 *
	 * @return the configuration elements declared by this extension 
	 */
	public IConfigurationElement[] getConfigurationElements() throws InvalidRegistryObjectException;

	/**
	 * Returns the descriptor of the plug-in that declares this extension.
	 * 
	 * @return the plug-in that declares this extension
	 * @deprecated IPluginDescriptor is not part of the new runtime and its function
	 * has been split over several parts of the new runtime.  This method
	 * is not available (returns <tt>null</tt>) if the compatibility layer is not installed.  Use getNamespace()
	 * to get the symbolic id of the declaring plugin.  See {@link IPluginDescriptor} to see how to 
	 * update your usecases.
	 */
	public IPluginDescriptor getDeclaringPluginDescriptor() throws InvalidRegistryObjectException;

	/**
	 * Returns the namespace for this extension. This value can be used
	 * in various global facilities to discover this extension's provider.
	 * <p>
	 * <b>Note</b>: This is an early access API to the new OSGI-based Eclipse 3.0
	 * Platform Runtime. Because the APIs for the new runtime have not yet been fully
	 * stabilized, they should only be used by clients needing to take particular
	 * advantage of new OSGI-specific functionality, and only then with the understanding
	 * that these APIs may well change in incompatible ways until they reach
	 * their finished, stable form (post-3.0). </p>
	 * 
	 * @return the namespace for this extension
	 * @see Platform#getBundle(String)
	 * @see IExtensionRegistry
	 * @since 3.0
	 */
	public String getNamespace() throws InvalidRegistryObjectException;

	/**
	 * Returns the unique identifier of the extension point
	 * to which this extension should be contributed.
	 *
	 * @return the unique identifier of the relevant extension point
	 */
	public String getExtensionPointUniqueIdentifier() throws InvalidRegistryObjectException;

	/**
	 * Returns a displayable label for this extension.
	 * Returns the empty string if no label for this extension
	 * is specified in the plug-in manifest file.
	 * <p> Note that any translation specified in the plug-in manifest
	 * file is automatically applied.
	 * <p>
	 *
	 * @return a displayable string label for this extension,
	 *    possibly the empty string
	 */
	public String getLabel() throws InvalidRegistryObjectException;

	/**
	 * Returns the simple identifier of this extension, or <code>null</code>
	 * if this extension does not have an identifier.
	 * This identifier is specified in the plug-in manifest (<code>plugin.xml</code>) 
	 * file as a non-empty string containing no period characters 
	 * (<code>'.'</code>) and must be unique within the defining plug-in.
	 *
	 * @return the simple identifier of the extension (e.g. <code>"main"</code>)
	 *  or <code>null</code>
	 */
	public String getSimpleIdentifier() throws InvalidRegistryObjectException;

	/**
	 * Returns the unique identifier of this extension, or <code>null</code>
	 * if this extension does not have an identifier.
	 * If available, this identifier is unique within the plug-in registry, and
	 * is composed of the namespace where this extension 
	 * was declared and this extension's simple identifier.
	 *
	 * @return the unique identifier of the extension
	 *    (e.g. <code>"com.example.acme.main"</code>), or <code>null</code>
	 */
	public String getUniqueIdentifier() throws InvalidRegistryObjectException;
	
	/** 
	 * @see Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o);
	
	/**
	 * Indicates whether or not the object is valid.
	 * @return true if the object is still valid.
	 */
	public boolean isValid();
}