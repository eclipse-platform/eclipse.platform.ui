package org.eclipse.team.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Provides a generic registry for keys and values based on file extensions.
 * 
 * File extensions should be considered without the usual "*." prefix.
 */
public interface IFileTypeRegistry {
	/**
	 * Return the value of the given key for the given file extension.
	 * <p>
	 * Example:
	 * <p>
	 * String value = getValue("txt", "isAscii");
	 * 
	 * @param extension  the extension
	 * @param key  the key
	 * @return the value for the given extension and key
	 */
	public String getValue(String extension, String key);
	/**
	 * Return all extensions for which the given key is defined.
	 * <p>
	 * Example:
	 * <p>
	 * String[] extensions = getValue("isAscii");
	 * 
	 * @param key  the key
	 * @return the extensions for which the given key is defined
	 */
	public String[] getExtensions(String key);
	
	/**
	 * Set the value of the given key, for files of type extension.
	 * <p>
	 * Example:
	 * <p>
	 * setValue("txt", "isAscii", "true"); 
	 *
	 * @param extension  the file extension
	 * @param key  the key
	 * @param value  the value
	 */
	public void setValue(String extension, String key, String value);
	
	/**
	 * Return whether the registry contains a value for the specified 
	 * extension and key.
	 * 
	 * @param extension  the file extension
	 * @param key  the key
	 * @return the value for the extension and key, if applicable
	 */
	public boolean containsKey(String extension, String key);
}
