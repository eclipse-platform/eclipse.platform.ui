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
 * Interface of objects which are interested in getting informed
 * about changes of a document's partitioning. Clients may
 * implement this interface.
 *
 * @see IDocument
 * @see IDocumentPartitioner
 */
public interface IDocumentPartitioningListener {
	
	/**
	 * The partitioning of the given document changed.
	 *
	 * @param document the document whose partitioning changed
	 *
	 * @see IDocument#addDocumentPartitioningListener
	 */
	void documentPartitioningChanged(IDocument document);
}
