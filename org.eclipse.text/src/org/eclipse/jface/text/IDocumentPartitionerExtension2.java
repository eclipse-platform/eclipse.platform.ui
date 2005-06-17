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
 * Extension interface for {@link org.eclipse.jface.text.IDocumentPartitioner}.
 * <p>
 * Extends the original concept of a document partitioner to answer the position
 * categories that are used to manage the partitioning information.
 * <p>
 * This extension also introduces the concept of open and delimited partitions.
 * A delimited partition has a predefined textual token delimiting its start and
 * end, while an open partition can fill any space between two delimited
 * partitions.
 * </p>
 * <p>
 * An open partition of length zero can occur between two delimited partitions,
 * thus having the same offset as the following delimited partition. The
 * document start and end are considered to be delimiters of open partitions,
 * i.e. there may be a zero-length partition between the document start and a
 * delimited partition starting at offset 0.
 * </p>
 *
 * @since 3.0
 */
public interface IDocumentPartitionerExtension2 {

	/**
	 * Returns the position categories that this partitioners uses in order to manage
	 * the partitioning information of the documents. Returns <code>null</code> if
	 * no position category is used.
	 *
	 * @return the position categories used to manage partitioning information or <code>null</code>
	 */
	String[] getManagingPositionCategories();


    /* zero-length partition support */

    /**
	 * Returns the content type of the partition containing the given offset in
	 * the connected document. There must be a document connected to this
	 * partitioner.
	 * <p>
	 * If <code>preferOpenPartitions</code> is <code>true</code>,
	 * precedence is given to an open partition ending at <code>offset</code>
	 * over a delimited partition starting at <code>offset</code>.
	 * <p>
	 * This method replaces {@link IDocumentPartitioner#getContentType(int)}and
	 * behaves like it when <code>prepreferOpenPartitions</code> is
	 * <code>false</code>, i.e. precedence is always given to the partition
	 * that does not end at <code>offset</code>.
	 * </p>
	 *
	 * @param offset the offset in the connected document
	 * @param preferOpenPartitions <code>true</code> if precedence should be
	 *            given to a open partition ending at <code>offset</code> over
	 *            a delimited partition starting at <code>offset</code>
	 * @return the content type of the offset's partition
	 */
    String getContentType(int offset, boolean preferOpenPartitions);

    /**
	 * Returns the partition containing the given offset of the connected
	 * document. There must be a document connected to this partitioner.
	 * <p>
	 * If <code>preferOpenPartitions</code> is <code>true</code>,
	 * precedence is given to an open partition ending at <code>offset</code>
	 * over a delimited partition starting at <code>offset</code>.
	 * <p>
	 * This method replaces {@link IDocumentPartitioner#getPartition(int)}and
	 * behaves like it when <preferOpenPartitions</code> is <code>false
	 * </code>, i.e. precedence is always given to the partition that does not
	 * end at <code>offset</code>.
	 * </p>
	 *
	 * @param offset the offset for which to determine the partition
	 * @param preferOpenPartitions <code>true</code> if precedence should be
	 *            given to a open partition ending at <code>offset</code> over
	 *            a delimited partition starting at <code>offset</code>
	 * @return the partition containing the offset
	 */
    ITypedRegion getPartition(int offset, boolean preferOpenPartitions);

    /**
	 * Returns the partitioning of the given range of the connected document.
	 * There must be a document connected to this partitioner.
	 * <p>
	 * If <code>includeZeroLengthPartitions</code> is <code>true</code>, a
	 * zero-length partition of an open partition type (usually the default
	 * partition) is included between two delimited partitions. If it is
	 * <code>false</code>, no zero-length partitions are included.
	 * </p>
	 * <p>
	 * This method replaces
	 * {@link IDocumentPartitioner#computePartitioning(int, int)}and behaves
	 * like it when <code>includeZeroLengthPartitions</code> is
	 * <code>false</code>.
	 * </p>
	 *
	 * @param offset the offset of the range of interest
	 * @param length the length of the range of interest
	 * @param includeZeroLengthPartitions <code>true</code> if zero-length
	 *            partitions should be returned as part of the computed
	 *            partitioning
	 * @return the partitioning of the range
	 */
    ITypedRegion[] computePartitioning(int offset, int length, boolean includeZeroLengthPartitions);
}
