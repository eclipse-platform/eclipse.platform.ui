package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;


/**
 * Extension to <code>IDocumentProvider</code>. Intention to be integrated with
 * <code>IDocumentProvider</code>. Should not yet be considered API.
 */
public interface IDocumentProviderExtension {
	
	/**
	 * Returns whether the given element is read-only. If this method returns
	 * <code>true</code>, <code>saveDocument</code> could fail. This method
	 * does not state anything about the document constructed from the given
	 * element. If the given element is not connected to this document provider,
	 * the return value is undefined.
	 * 
	 * @param element the element
	 * @return <code>true</code> if the given element is read-only, <code>false</code> otherwise
	 * @exception CoreException if retrieving the information fails
	 */
	boolean isReadOnly(Object element) throws CoreException;
	
	/**
	 * Returns whether the given element can persistently be modified. This method is
	 * orthogonal to <code>isReadOnly</code>. Read-only elements could be modifiable,
	 * and writable elements may not be modified. If the given element is not connected to this
	 * document provider, the return value is undefined.
	 * 
	 * @param element the element
	 * @return <code>true</code> if the given element is modifiable, <code>false</code> otherwise
	 * @exception CoreException if retrieving the information fails
	 */
	boolean isModifiable(Object element) throws CoreException;
}

