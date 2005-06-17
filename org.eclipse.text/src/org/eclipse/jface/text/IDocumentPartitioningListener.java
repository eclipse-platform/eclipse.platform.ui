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
 * Interface of objects which are interested in getting informed
 * about changes of a document's partitioning.
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * In order to provided backward compatibility for clients of <code>IDocumentPartitioningListener</code>, extension
 * interfaces are used to provide a means of evolution. The following extension interfaces
 * exist:
 * <ul>
 * <li> {@link org.eclipse.jface.text.IDocumentPartitioningListenerExtension} since version 2.0 replacing the original
 *      notification mechanism.</li>
 * <li> {@link org.eclipse.jface.text.IDocumentPartitioningListenerExtension2} since version 3.0 replacing all previous
 *      notification mechanisms. Thus, implementers up-to-date with version 3.0 do not have to implement
 *      {@link org.eclipse.jface.text.IDocumentPartitioningListenerExtension}.</li>
 * </ul>
 * </p>
 * @see org.eclipse.jface.text.IDocumentPartitioningListenerExtension
 * @see org.eclipse.jface.text.IDocumentPartitioningListenerExtension2
 * @see org.eclipse.jface.text.IDocument
 * @see org.eclipse.jface.text.IDocumentPartitioner
 */
public interface IDocumentPartitioningListener {

	/**
	 * The partitioning of the given document changed.
	 * <p>
	 * In version 2.0 this method has been replaces by
	 * {@link IDocumentPartitioningListenerExtension#documentPartitioningChanged(IDocument, IRegion)}.
	 * <p>
	 * In version 3.0 this method has been replaces by
	 * {@link IDocumentPartitioningListenerExtension2#documentPartitioningChanged(DocumentPartitioningChangedEvent)}<p>
	 *
	 * @param document the document whose partitioning changed
	 *
	 * @see IDocumentPartitioningListenerExtension#documentPartitioningChanged(IDocument, IRegion)
	 * @see IDocumentPartitioningListenerExtension2#documentPartitioningChanged(DocumentPartitioningChangedEvent)
	 * @see IDocument#addDocumentPartitioningListener(IDocumentPartitioningListener)
	 */
	void documentPartitioningChanged(IDocument document);
}
