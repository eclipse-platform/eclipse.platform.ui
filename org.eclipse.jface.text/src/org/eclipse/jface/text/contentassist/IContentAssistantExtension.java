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
package org.eclipse.jface.text.contentassist;


/**
 * Extends {@link org.eclipse.jface.text.contentassist.IContentAssistant}
 * with the following functions:
 * <ul>
 * 	<li>handle documents with multiple partitions</li>
 * 	<li>insertion of common completion prefixes</li>
 * </ul>
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
	 * Inserts the common prefix of the available completions. If no common
	 * prefix can be computed it is identical to
	 * {@link IContentAssistant#showPossibleCompletions()}.
	 *
	 * @return an optional error message if no proposals can be computed
	 */
	String completePrefix();
}
