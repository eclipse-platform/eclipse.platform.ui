/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.content;

/**
 * Content types are objects that provide information on file types, such as 
 * default charset, line terminator, etc.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p> 
 * <p>
 * <b>Note</b>: This interface is part of early access API that may well 
 * change in incompatible ways until it reaches its finished form. 
 * </p>
 * 
 * @since 3.0
 */
public interface IContentType {
	/**
	 * Returns this content type's identifier. Each content type has an 
	 * identifier by which they can be retrieved from the content type catalog.
	 * 
	 * @return this content type's identifier
	 */
	public String getId();
	/**
	 * Returns a user-friendly name for this content type.
	 * 
	 * @return this content type's name  
	 */
	public String getName();
	/**
	 * Returns whether this content type is text-based, i.e., is descendant of
	 * the org.eclipse.core.runtime.text content type.
	 * 
	 * @return <code>true</code> if this content type is text-based, 
	 * <code>false</code> otherwise
	 */
	public boolean isText();
	/**
	 * Returns the MIME type for this content type, if one has been configured.
	 * 
	 * @return this content type's MIME type, or <code>null</code>
	 */
	public String getMIMEType();
	/**
	 * Returns the file extensions associated to this content type. Returns an 
	 * empty array if this content type has no file extensions associated to it.
	 *  
	 * @return the file extensions associated to this content type
	 */
	public String[] getFileExtensions();
	/**
	 * Returns the file names associated to this content type. Returns an 
	 * empty array if this content type has no file names associated to it.
	 *  
	 * @return the file names associated to this content type
	 */
	public String[] getFileNames();
	/**
	 * Returns a reference to this content type's base type. If this content type
	 * does not have a base type, returns <code>null</code>.
	 * 
	 * @return this content type's base type, or <code>null</code>
	 */
	public IContentType getBaseType();
	/**
	 * Returns the default charset for this content type if one has been defined, 
	 * <code>null</code> otherwise.
	 * 
	 * @return the default charset, or <code>null</code>
	 */
	public String getDefaultCharset();
	//TODO
	//boolean addAssociation(String fileSpec);
	//boolean removeAssociation(String fileSpec);
	/**
	 * Returns whether this content type is associated with the 
	 * given file name.
	 * 
	 * @return <code>true</code> if this content type is associated with
	 * the given file name, <code>false</code> otherwise 
	 */
	public boolean isAssociatedWith(String fileName);
}
