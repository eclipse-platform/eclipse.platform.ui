/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text;
 
 
/**
 * Extension interface for <code>IDocumentPartitioner</code>. Extends the original
 * concept of a document partitioner to answer the position categories that are used
 * to manage the partitioning information.
 * 
 * @since 3.0

*/
public interface IDocumentPartitionerExtension2 {
		
	/**
	 * Returns the position categories that this partitoners uses in order to manage
	 * the partitioning information of the documents. Returns <code>null</code> if
	 * no position category is used.
	 * 
	 * @return the position categories used to manage partitioning information
	 */
	String[] getManagingPositionCategories();
}
