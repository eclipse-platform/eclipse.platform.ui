/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

/**
 * Extension interface for <code>ISlaveDocumentManager</code>. Adds access to
 * the list of all slave documents for a given master document.
 * 
 * @since 3.0
 */
public interface ISlaveDocumentManagerExtension {
	
	/**
	 * Returns the list of slave documents for the given master document.
	 * 
	 * @param master the master document
	 * @return the list of slave documents or <code>null</code>
	 */
	IDocument[] getSlaveDocuments(IDocument master);
}
