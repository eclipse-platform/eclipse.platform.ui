/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
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
	 * Installs content assist support on the given subject.
	 * 
	 * @param contentAssistSubject the one who requests content assist
	 * @throws UnsupportedOperationException if the content assist does not support this method
	 */
	void install(IContentAssistSubject contentAssistSubject);
	
	/**
	 * Complete the common prefix of the available choices.
	 *
	 * @return an optional error message if no proposals can be computed
	 */
	String completePrefix();
}
