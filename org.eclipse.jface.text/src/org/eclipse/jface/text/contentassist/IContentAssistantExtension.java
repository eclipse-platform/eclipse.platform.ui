/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.contentassist;


/**
 * Extension interface for <code>IContentAssistant</code>.
 * Updates the content assistant to be aware of documents with multiple partitions.
 * 
 * @since 3.0
 */
public interface IContentAssistantExtension {
	
	/**
	 * Returns the document partitioning this content assistant is using.
	 * 
	 * @return the document partitioning this content assistant is using
	 */
	String getDocumentPartitioning();

	/**
	 * Complete the common prefix of the available choices.
	 *
	 * @return an optional error message if no proposals can be computed
	 */
	String completePrefix();
}
