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
 * Extension interface for <code>IDocumentPartitioningListener</code>. Extends the original
 * partitioning listener concept by telling the listener the minimal region that comprises all
 * partitioning changes.
 * 
 * @see org.eclipse.jface.text.IDocumentPartitionerExtension
 * @since 2.0
 */
public interface IDocumentPartitioningListenerExtension {
		
	/**
	 * The partitioning of the given document changed in the given region.
	 *
	 * @param document the document whose partitioning changed
	 * @param region the region in which the partitioning changed
	 * @see IDocument#addDocumentPartitioningListener
	 */
	void documentPartitioningChanged(IDocument document, IRegion region);
}
