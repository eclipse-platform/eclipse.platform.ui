package org.eclipse.jface.text;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
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
