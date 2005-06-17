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
 * A document partitioner divides a document into a set
 * of disjoint text partitions. Each partition has a content type, an
 * offset, and a length. The document partitioner is connected to one document
 * and informed about all changes of this document before any of the
 * document's document listeners. A document partitioner can thus
 * incrementally update on the receipt of a document change event.<p>
 *
 * In order to provided backward compatibility for clients of <code>IDocumentPartitioner</code>, extension
 * interfaces are used to provide a means of evolution. The following extension interfaces
 * exist:
 * <ul>
 * <li> {@link org.eclipse.jface.text.IDocumentPartitionerExtension} since version 2.0 replacing
 *      the <code>documentChanged</code> method with a new one returning the minimal document region
 *      comprising all partition changes.</li>
 * <li> {@link org.eclipse.jface.text.IDocumentPartitionerExtension2} since version 3.0
 *      introducing zero-length partitions in conjunction with the distinction between
 *      open and closed partitions. Also provides inside in the implementation of the partitioner
 *      by exposing the position category used for managing the partitioning information.</li>
 * <li> {@link org.eclipse.jface.text.IDocumentPartitionerExtension3} since version 3.1 introducing
 *      rewrite session. It also replaces the existing {@link #connect(IDocument)} method with
 *      a new one: {@link org.eclipse.jface.text.IDocumentPartitionerExtension3#connect(IDocument, boolean)}.
 * </ul>
 * <p>
 * Clients may implement this interface and its extension interfaces or use the standard
 * implementation <code>DefaultPartitioner</code>.
 * </p>
 *
 * @see org.eclipse.jface.text.IDocumentPartitionerExtension
 * @see org.eclipse.jface.text.IDocumentPartitionerExtension2
 * @see org.eclipse.jface.text.IDocument
 */
public interface IDocumentPartitioner {

	/**
	 * Connects the partitioner to a document.
	 * Connect indicates the begin of the usage of the receiver
	 * as partitioner of the given document. Thus, resources the partitioner
	 * needs to be operational for this document should be allocated.<p>
	 *
	 * The caller of this method must ensure that this partitioner is
	 * also set as the document's document partitioner.<p>
	 *
	 * This method has been replaced with {@link IDocumentPartitionerExtension3#connect(IDocument, boolean)}.
	 * Implementers should default a call <code>connect(document)</code> to
	 * <code>connect(document, false)</code> in order to sustain the same semantics.
	 *
	 * @param document the document to be connected to
	 */
	void connect(IDocument document);

	/**
	 * Disconnects the partitioner from the document it is connected to.
	 * Disconnect indicates the end of the usage of the receiver as
	 * partitioner of the connected document. Thus, resources the partitioner
	 * needed to be operation for its connected document should be deallocated.<p>
	 * The caller of this method should also must ensure that this partitioner is
	 * no longer the document's partitioner.
	 */
	void disconnect();

	/**
	 * Informs about a forthcoming document change. Will be called by the
	 * connected document and is not intended to be used by clients
	 * other than the connected document.
	 *
	 * @param event the event describing the forthcoming change
	 */
	void documentAboutToBeChanged(DocumentEvent event);

	/**
	 * The document has been changed. The partitioner updates
	 * the document's partitioning and returns whether the structure of the
	 * document partitioning has been changed, i.e. whether partitions
	 * have been added or removed. Will be called by the connected document and
	 * is not intended to be used by clients other than the connected document.<p>
	 *
	 * This method has been replaced by {@link IDocumentPartitionerExtension#documentChanged2(DocumentEvent)}.
	 *
	 * @param event the event describing the document change
	 * @return <code>true</code> if partitioning changed
	 */
	boolean documentChanged(DocumentEvent event);

	/**
	 * Returns the set of all legal content types of this partitioner.
	 * I.e. any result delivered by this partitioner may not contain a content type
	 * which would not be included in this method's result.
	 *
	 * @return the set of legal content types
	 */
	String[] getLegalContentTypes();

	/**
	 * Returns the content type of the partition containing the
	 * given offset in the connected document. There must be a
	 * document connected to this partitioner.<p>
	 *
	 * Use {@link IDocumentPartitionerExtension2#getContentType(int, boolean)} when
	 * zero-length partitions are supported. In that case this method is
	 * equivalent:
	 * <pre>
	 *    IDocumentPartitionerExtension2 extension= (IDocumentPartitionerExtension2) partitioner;
	 *    return extension.getContentType(offset, false);
	 * </pre>
	 *
	 * @param offset the offset in the connected document
	 * @return the content type of the offset's partition
	 */
	String getContentType(int offset);

	/**
	 * Returns the partitioning of the given range of the connected
	 * document. There must be a document connected to this partitioner.<p>
	 *
	 * Use {@link IDocumentPartitionerExtension2#computePartitioning(int, int, boolean)} when
	 * zero-length partitions are supported. In that case this method is
	 * equivalent:
	 * <pre>
	 *    IDocumentPartitionerExtension2 extension= (IDocumentPartitionerExtension2) partitioner;
	 *    return extension.computePartitioning(offset, length, false);
	 * </pre>
	 *
	 * @param offset the offset of the range of interest
	 * @param length the length of the range of interest
	 * @return the partitioning of the range
	 */
	ITypedRegion[] computePartitioning(int offset, int length);

	/**
	 * Returns the partition containing the given offset of
	 * the connected document. There must be a document connected to this
	 * partitioner.<p>
	 *
	 * Use {@link IDocumentPartitionerExtension2#getPartition(int, boolean)} when
	 * zero-length partitions are supported. In that case this method is
	 * equivalent:
	 * <pre>
	 *    IDocumentPartitionerExtension2 extension= (IDocumentPartitionerExtension2) partitioner;
	 *    return extension.getPartition(offset, false);
	 * </pre>
	 *
	 * @param offset the offset for which to determine the partition
	 * @return the partition containing the offset
	 */
	ITypedRegion getPartition(int offset);
}
