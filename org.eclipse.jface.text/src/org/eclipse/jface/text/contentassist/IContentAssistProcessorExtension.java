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

package org.eclipse.jface.text.contentassist;

/**
 * Extends <code>IContentAssit</code> with the concept of a
 * content assist subject which provides the context for
 * the content assistant.
 * <p>
 * XXX: This is work in progress and can change anytime until API for 3.0 is frozen.
 * </p>
 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject
 * @since 3.0
 */
public interface IContentAssistProcessorExtension {
	/**
	 * Returns a list of completion proposals based on the specified location
	 * within the document that corresponds to the current cursor position
	 * within the text viewer.
	 * 
	 * @param contentAssistSubject the content assist subject whose
	 *           document is used to compute the proposals
	 * @param documentPosition an offset within the document for which
	 *           completions should be computed
	 * @return an array of completion proposals or <code>null</code> if no
	 *         proposals are possible
	 */
	ICompletionProposal[] computeCompletionProposals(IContentAssistSubject contentAssistSubject, int documentOffset);
	
	/**
	 * Returns information about possible contexts based on the specified
	 * location within the document that corresponds to the current cursor
	 * position within the content assist subject.
	 * 
	 * @param contentAssistSubject the content assist subject whose
	 *           document is used to compute the possible contexts
	 * @param documentPosition an offset within the document for which context
	 *           information should be computed
	 * @return an array of context information objects or <code>null</code>
	 *         if no context could be found
	 */
	IContextInformation[] computeContextInformation(IContentAssistSubject contentAssistSubject, int documentOffset);
}
