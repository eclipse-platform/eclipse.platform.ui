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
 * An extension point declared in a host.
 * Except for the list of extensions plugged in to it, the information 
 * available for an extension point is obtained from the declaring host's extension 
 * manifest file.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @since 3.0
 */
public interface IExtensionPoint {
	/**
	 * Returns the extension with the given unique identifier configured into
	 * this extension point, or <code>null</code> if there is no such extension.
	 * Since an extension might not have an identifier, some extensions
	 * can only be found via the <code>getExtensions</code> method.
	 *
	 * @param extensionId the unique identifier of an extension 
	 *		(e.g. <code>"com.example.acme.main"</code>).
	 * @return an extension, or <code>null</code>
	 */
	public IExtension getExtension(String extensionId);
	/**
	 * Returns all extensions configured into this extension point.
	 * Returns an empty array if this extension point has no extensions.
	 *
	 * @return the extensions configured into this extension point
	 */
	public IExtension[] getExtensions();
	/** 
	 * Returns the identifier of the parent that declares this extension.
	 *
	 * @return the name of the parent that declares this extension
	 */
	public String getParentIdentifier();
	/**
	 * Returns reference to the extension point schema. The schema 
	 * reference is returned as a URL path relative to the parent 
	 * installation URL. 
	 * Returns the empty string if no schema for this extension point
	 * is specified in the extension manifest file.
	 *
	 * @return a relative URL path, or an empty string
	 */
	public String getSchemaReference();	
	/**
	 * Returns the simple identifier of this extension point.
	 * This identifier is a non-empty string containing no
	 * period characters (<code>'.'</code>) and is guaranteed
	 * to be unique within the defining host.
	 *
	 * @return the simple identifier of the extension point (e.g. <code>"builders"</code>)
	 */
	public String getSimpleIdentifier();
	/**
	 * Returns the unique identifier of this extension point.
	 * This identifier is unique within the extensions registry, and
	 * is composed of the identifier of the extension that declared
	 * this extension point and this extension point's simple identifier.
	 *
	 * @return the unique identifier of the extension point
	 *    (e.g. <code>"org.eclipse.core.resources.builders"</code>)
	 */
	public String getUniqueIdentifier();
	/**
	 * Returns a displayable label for this extension point.
	 * Returns the empty string if no label for this extension point
	 * is specified in the extension manifest file.
	 * <p> Note that any translation specified in the extension manifest
	 * file is automatically applied.
	 * </p>
	 *
	 * @return a displayable string label for this extension point,
	 *    possibly the empty string
	 */
	public String getLabel();

}
