package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


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
