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
 * Replaces the original concept of the document partitioner by returning the
 * minimal region that includes all partition changes caused by the invocation
 * of the document partitioner.
 * The method <code>documentChanged2</code> is considered the replacement of
 * {@link org.eclipse.jface.text.IDocumentPartitioner#documentChanged(DocumentEvent)}.
 *
 * @since 2.0
 */
public interface IDocumentPartitionerExtension {

	/**
	 * The document has been changed. The partitioner updates the document's
	 * partitioning and returns the minimal region that comprises all partition
	 * changes caused in response to the given document event. This method
	 * returns <code>null</code> if the partitioning did not change.
	 * <p>
	 *
	 * Will be called by the connected document and is not intended to be used
	 * by clients other than the connected document.
	 * <p>
	 * Replaces {@link IDocumentPartitioner#documentChanged(DocumentEvent)}.
	 *
	 * @param event the event describing the document change
	 * @return the region of the document in which the partition type changed or <code>null</code>
	 */
	IRegion documentChanged2(DocumentEvent event);
}
