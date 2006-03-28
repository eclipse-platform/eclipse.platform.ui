/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.contentassist;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;


/**
 * Extension interface for {@link org.eclipse.jface.text.contentassist.IContentAssistProcessor}
 * which provides the context for the
 * {@linkplain org.eclipse.jface.contentassist.ISubjectControlContentAssistant subject control content assistant}.
 *
 * @since 3.0
 * @deprecated As of 3.2, replaced by Platform UI's field assist support
 */
public interface ISubjectControlContentAssistProcessor extends IContentAssistProcessor {

	/**
	 * Returns a list of completion proposals based on the specified location
	 * within the document that corresponds to the current cursor position
	 * within the text viewer.
	 *
	 * @param contentAssistSubjectControl the content assist subject control whose
	 *           document is used to compute the proposals
	 * @param documentOffset an offset within the document for which
	 *           completions should be computed
	 * @return an array of completion proposals or <code>null</code> if no
	 *         proposals are possible
	 */
	ICompletionProposal[] computeCompletionProposals(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset);

	/**
	 * Returns information about possible contexts based on the specified
	 * location within the document that corresponds to the current cursor
	 * position within the content assist subject control.
	 *
	 * @param contentAssistSubjectControl the content assist subject control whose
	 *           document is used to compute the possible contexts
	 * @param documentOffset an offset within the document for which context
	 *           information should be computed
	 * @return an array of context information objects or <code>null</code>
	 *         if no context could be found
	 */
	IContextInformation[] computeContextInformation(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset);
}
