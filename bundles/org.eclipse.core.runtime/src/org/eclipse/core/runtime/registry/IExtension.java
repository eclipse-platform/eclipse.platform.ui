/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
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
 * An extension declared in a host.
 * All information is obtained from the declaring host 
 * extensions manifest file. 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @since 3.0
 */
public interface IExtension {
	/**
	 * Returns all configuration elements declared by this extension.
	 * These elements are a direct reflection of the configuration 
	 * markup supplied in the manifest file for the host that declares 
	 * this extension.
	 * Returns an empty array if this extension does not declare any
	 * configuration elements.
	 *
	 * @return the configuration elements declared by this extension 
	 */
	public IConfigurationElement[] getConfigurationElements();
	/** 
	 * Returns the identifier of the parent that declares this extension.
	 *
	 * @return the name of the parent that declares this extension
	 */
	public String getParentIdentifier();
	/**
	 * Returns the unique identifier of the extension point
	 * that this extension gets plugged into.
	 *
	 * @return the unique identifier of the relevant extension point
	 */
	public String getExtensionPointIdentifier();

	/**
	 * Returns the simple identifier of this extension, or <code>null</code>
	 * if this extension does not have an identifier.
	 * This identifier is specified in the extensions manifest  
	 * file as a non-empty string containing no period characters 
	 * (<code>'.'</code>) and must be unique within the defining host.
	 *
	 * @return the simple identifier of the extension (e.g. <code>"main"</code>)
	 *  or <code>null</code>
	 */
	public String getSimpleIdentifier();
	/**
	 * Returns the unique identifier of this extension, or <code>null</code>
	 * if this extension does not have an identifier.
	 * If available, this identifier is unique within the extension registry, and
	 * is composed of the identifier of the host that declared
	 * this extension and this extension's simple identifier.
	 *
	 * @return the unique identifier of the extension
	 *    (e.g. <code>"com.example.acme.main"</code>), or <code>null</code>
	 */
	public String getUniqueIdentifier();
	/**
	 * Returns a displayable label for this extension.
	 * Returns the empty string if no label for this extension
	 * is specified in the extension manifest file.
	 * <p> Note that any translation specified in the extension manifest
	 * file is automatically applied.
	 * <p>
	 *
	 * @return a displayable string label for this extension,
	 *    possibly the empty string
	 */
	public String getLabel();

}
