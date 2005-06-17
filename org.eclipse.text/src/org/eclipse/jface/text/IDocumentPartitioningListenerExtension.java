/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text;


/**
 * Extension interface for
 * {@link org.eclipse.jface.text.IDocumentPartitioningListener}.
 * <p>
 * Replaces the original notification mechanism by telling the listener the
 * minimal region that comprises all partitioning changes.
 *
 * @see org.eclipse.jface.text.IDocumentPartitionerExtension
 * @since 2.0
 */
public interface IDocumentPartitioningListenerExtension {

	/**
	 * The partitioning of the given document changed in the given region.
	 * <p>
	 * In version 3.0, this method has been replaced with
	 * {@link IDocumentPartitioningListenerExtension2#documentPartitioningChanged(DocumentPartitioningChangedEvent)}.
	 *
	 * @param document the document whose partitioning changed
	 * @param region the region in which the partitioning changed
	 * @see IDocumentPartitioningListenerExtension2#documentPartitioningChanged(DocumentPartitioningChangedEvent)
	 * @see IDocument#addDocumentPartitioningListener(IDocumentPartitioningListener)
	 */
	void documentPartitioningChanged(IDocument document, IRegion region);
}
