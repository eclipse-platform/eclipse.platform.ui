/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.editors.text;

 
/**
 * Document provider for <code>IStorage</code> based domain elements.
 * Basically incorporates the concept of character encoding.
 * 
 * @since 2.0
 */ 
public interface IStorageDocumentProvider {
	
	/**
	 * Returns the default character encoding used by this provider for reading.
	 * 
	 * @return the default character encoding used  by this provider for reading
	 */
	String getDefaultEncoding();
	
	/**
	 * Returns the character encoding for reading the given element, or 
	 * <code>null</code> if the element is not managed by this provider.
	 * 
	 * @param element the element
	 * @return the encoding for reading the given element
	 */
	String getEncoding(Object element);
	
	/**
	 * Sets the encoding for reading the given element. If <code>encoding</code>
	 * is <code>null</code> the workbench's character encoding should be used.
	 * 
	 * @param element the element
	 * @param encoding the encoding to be used
	 */
	void setEncoding(Object element, String encoding);
}
