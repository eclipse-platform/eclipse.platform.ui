/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.jface.text;

/**
 * Extension interface for <code>IDocument</code>.  Adds the concept of multiple partitionings.
 * 
 * @since 3.0
 */
public interface IDocumentExtension3 {
	
	/**
	 * The identifier of the default partitioning.
	 */
	final static String DEFAULT_PARTITIONING= "__dftl_partitioning"; //$NON-NLS-1$
	
	
	/**
	 * Returns the exisiting partitionings for this document. This includes
	 * the default partitioning.
	 * 
	 * @return the existing partitionings for this document
	 */
	String[] getPartitionings();
	
	/**
	 * Returns the set of legal content types of document partitions for the given partitioning
	 * This set can be empty. The set can contain more content types than  contained by the
	 * result of <code>getPartitioning(partitioning, 0, getLength())</code>.
	 *
	 * @param partitioning the partitioning for which to return the legal content types
	 * @return the set of legal content types
	 * @exception BadPartitioningException if partitioning is invalid for this document
	 */
	String[] getLegalContentTypes(String partitioning) throws BadPartitioningException;
	
	/**
	 * Returns the type of the document partition containing the given offset for the
	 * given partitioning. This is a convenience method for 
	 * <code>getPartition(partitioning, offset).getType()</code>.
	 *
	 * @param partitioning the partitioning
	 * @param offset the document offset
	 * @return the partition type
	 * @exception BadLocationException if offset is invalid in this document
	 * @exception BadPartitioningException if partitioning is invalid for this document
	 */
	String getContentType(String partitioning, int offset) throws BadLocationException, BadPartitioningException;
	
	/**
	 * Returns the document partition of the given partitioning in which the given offset is located.
	 *
	 * @param partitioning the document partitioning
	 * @param offset the document offset
	 * @return a specification of the partition
	 * @exception BadLocationException if offset is invalid in this document
	 * @exception BadPartitioningException if partitioning is invalid for this document
	 */
	ITypedRegion getPartition(String partitioning, int offset) throws BadLocationException, BadPartitioningException;
	
	/**
	 * Computes the partitioning of the given document range based on the given partitioning.
	 *
	 * @param partitioning the document's partitioning
	 * @param offset the document offset at which the range starts
	 * @param length the length of the document range
	 * @return a specification of the range's partitioning
	 * @exception BadLocationException if the range is invalid in this document
	 */
	ITypedRegion[] computePartitioning(String partitioning, int offset, int length) throws BadLocationException, BadPartitioningException;
	
	/**
	 * Sets this document's partitioner. The caller of this method is responsible for
	 * disconnecting the document's old partitioner from the document and to
	 * connect the new partitioner to the document. Informs all document partitioning
	 * listeners about this change.
	 *
	 * @param  partitioning the partitioning for which to set the partitioner
	 * @param the document's new partitioner
	 * @see IDocumentPartitioningListener
	 */
	void setDocumentPartitioner(String partitioning, IDocumentPartitioner partitioner);	
	
	/**
	 * Returns the partitioner for the given partitioning or <code>null</code> if
	 * no partitioner is registered.
	 *
	 * @return the partitioner for the given partitioning
	 */
	IDocumentPartitioner getDocumentPartitioner(String partitioning);
}
