package org.eclipse.team.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

/**
 * Provides a generic registry for keys and values based on file extensions.
 * 
 * File extensions should be considered without the usual "*." prefix.
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
