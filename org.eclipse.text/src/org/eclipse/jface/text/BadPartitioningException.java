/**********************************************************************
Copyright (c) 2000, 2004 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.jface.text;

/**
 * Represents the attempt to refer to a non-existing document partitioning.
 *
 * @see org.eclipse.jface.text.IDocument
 * @see org.eclipse.jface.text.IDocumentExtension3
 * @since 3.0
 */
public class BadPartitioningException extends Exception {

	/**
	 * Creates a new bad partitioning exception.
	 */
	public BadPartitioningException() {
	}

	/**
	 * Creates a new bad partitioning exception.
	 * 
	 * @param message message describing the exception
	 */	
	public BadPartitioningException(String message) {
		super(message);
	}
}
