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

import java.io.IOException;
import java.io.InputStream;

/**
 * The global access point for content type-related needs.
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
public interface IContentTypeManager {
	/**
	 * Applies content-based content type detection on the given input stream
	 * taking only the provided set of content types into account.
	 * Returns <code>null</code> if no appropriate content types are 
	 * found in the given set. If multiple content types are considered
	 * appropriate, returns the most appropriated one if it can be determined, 
	 * <code>null</code> otherwise.  
	 * <p>
	 * The provided input stream <i>must</i> be resettable, otherwise an 
	 * IllegalArgumentException will be thrown. 
	 * </p>
	 * 
	 * @param contents a resettable input stream (#markSupported must be <code>true</code>)
	 * @param set the content types to be taken into account, or <code>null</code>, 
	 * for all content types in the catalog 
	 * @return the most appropriate content type for the given contents, or <code>null</code>
	 */	
	IContentType findContentTypeFor(InputStream contents, IContentType[] set) throws IOException;	
	/**
	 * Returns the preferred content type for the given file name. If multiple content types 
	 * are associated with the given file name, the one considered the most appropriated will
	 * be returned. If there are no content types associated, <code>null</code> is returned.
	 * 
	 * @return the preferred content type associated to the given file name, or <code>null</code>
	 */
	IContentType findContentTypeForFileName(String fileName);	
	/**
	 * Executes content-based content type detection on the given contents
	 * taking only the provided content types into account 
	 * Returns an empty array if no associated content types are 
	 * found in the given set
	 * <p>
	 * If the set provided is <code>null</code>, all content types in the 
	 * catalog are taken into account.  
	 * </p>
	 * 
	 * @param contents a (preferrably) resettable input stream
	 * @param set the content types to be taken into account
	 * @return all content types associated to the given file spec
	 */	
	IContentType[] findContentTypesFor(InputStream contents, IContentType[] set) throws IOException;
	/**
	 * Returns all content types known by the platform that are associated to the given file name. 
	 * Returns an empty array if there are no content types associated.
	 * 
	 * @return all content types associated to the given file spec
	 */
	IContentType[] findContentTypesForFileName(String fileName);
	/**
	 * Returns all content types known by the platform. Returns an empty array 
	 * if there are no content types configured.
	 * 
	 * @return all content types known by the platform.
	 */
	IContentType[] getAllContentTypes();
	/**
	 * Returns the content type with the given identifier, or <code>null</code>
	 * if no such content type is known by the platform.
	 * 
	 * @param contentTypeIdentifier the identifier for the content type
	 * @return the content type, or <code>null</code>
	 */
	IContentType getContentType(String contentTypeIdentifier);
	/**
	 * Tries to obtain a description for the given contents. Any 
	 * IOExceptions that may occur while reading the given input stream will 
	 * flow to the caller.
	 *  
	 * @param contents the contents to be interpreted
	 * @param set the content types to be taken into account, or <code>null</code>,
	 * for all content types in the catalog
	 * @param optionsMask a bit-wise OR of all options that should be described
	 * @return a content description if one could be obtained, or <code>null</code>
	 * @see IContentDescription 
	 */
	IContentDescription getDescriptionFor(InputStream contents, IContentType[] set, int optionsMask) throws IOException;
	//TODO
	// user settings based
	//void setDefaultContentTypeForFile(String filespec, IContentType defaultType);
	//IContentType getDefaultContentTypeForFile(String filespec);
}
