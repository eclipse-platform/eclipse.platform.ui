package org.eclipse.ui.editors.text;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Implemented by text editors supporting dynamic changes of their character encoding.
 */
public interface IEncodingSupport{
	
	/**
	 * Sets the character encoding for the editor's current  input element.
	 */
	void setEncoding(String encoding);
	
	/**
	 * Returns the character encoding of the editor's current input element.
	 */
	String getEncoding();
	
	/**
	 * Returns the default encoding.
	 */
	String getDefaultEncoding();
}
