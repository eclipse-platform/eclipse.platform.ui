/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.jface.text;

/**
 * Slave documents are documents that use a text store which is based on a
 * master document.
 */
public interface ISlaveDocumentManager {
	
	/**
	 * Creates a new slave document for the given master document.
	 * 
	 * @param master
	 * @return IDocument
	 */
	IDocument createSlaveDocument(IDocument master);
	
	/**
	 * Frees the given slave document.
	 * 
	 * @param slave
	 */
	void freeSlaveDocument(IDocument slave);
	
	/**
	 * Creates a new mapping between the given slave document and its master.
	 * 
	 * @param slaveDocument
	 * @return IDocumentInformationMapping
	 */
	IDocumentInformationMapping createMasterSlaveMapping(IDocument slave);
	
	/**
	 * Returns the master of the given slave document.
	 * 
	 * @param slave
	 * @return IDocument
	 */
	IDocument getMasterDocument(IDocument slave);
	
	/**
	 * Method isSlaveDocument.
	 * @param document
	 * @return boolean
	 */
	boolean isSlaveDocument(IDocument document);
}