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
 * Extension interface for <code>IDocumentPartitioner</code>. Extends the original
 * concept of a document partitioner by returning the minimal region that includes all
 * partition changes causes by the invocation of the document partitioner.
 * 
 * @since 2.0

*/
public interface IDocumentPartitionerExtension {
		
	/**
	 * The document has been changed. The partitioner updates 
	 * the document's partitioning and returns in which region the
	 * partition types have changed. This method always returns
	 * the surrounding region. Will be called by the connected document
	 * and is not intended to be used by clients other than the connected
	 * document.
	 *
	 * @param event the event describing the document change
	 * @return the region of the document in which the partition type changed
	 */
	IRegion documentChanged2(DocumentEvent event);
}
