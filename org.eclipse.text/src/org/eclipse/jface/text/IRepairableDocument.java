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
 * Tagging interface to be implemented by
 * {@link org.eclipse.jface.text.IDocument}implementers that offer a line
 * repair method on the documents.
 * 
 * @see org.eclipse.jface.text.IDocument
 * @since 3.0
 */
public interface IRepairableDocument {
	
	/**
	 * Repairs the line information of the document implementing this interface.
	 */
	void repairLineInformation();
}
