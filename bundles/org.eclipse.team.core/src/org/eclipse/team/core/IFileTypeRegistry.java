/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.core;

/**
 * Provides a generic registry for keys and values based on file extensions.
 * 
 * File extensions should be considered without the usual "*." prefix.
 * 
 * @since 2.0
 */
public interface IFileTypeRegistry {
	public static final int UNKNOWN = 0;
	public static final int TEXT = 1;
	public static final int BINARY = 2;
	/**
	 * Return the type of files with the given file extension.
	 * 
	 * Valid return values are:
	 * IFileTypeRegistry.TEXT
	 * IFileTypeRegistry.BINARY
	 * IFileTypeRegistry.UNKNOWN
	 * 
	 * @param extension  the extension
	 * @return whether files with the given extension are TEXT, BINARY, or UNKNOWN
	 */
	public int getType(String extension);
	/**
	 * Return all extensions in the registry.
	 * 
	 * @return the extensions in the registry
	 */
	public String[] getExtensions();
	
	/**
	 * Set the file type for the give extension to the given type.
	 *
	 * Valid types are:
	 * IFileTypeRegistry.TEXT
	 * IFileTypeRegistry.BINARY
	 * IFileTypeRegistry.UNKNOWN
	 * 
	 * @param extension  the file extension
	 * @param type  the file type
	 */
	public void setValue(String extension, int type);
	
	/**
	 * Return whether the registry contains a value for the specified 
	 * extension.
	 * 
	 * @param extension  the file extension
	 */
	public boolean containsExtension(String extension);
}
