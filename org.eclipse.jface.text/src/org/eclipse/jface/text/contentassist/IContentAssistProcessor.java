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

import org.eclipse.jface.text.ITextViewer;


/**
 * A content assist processor proposes completions and
 * computes context information for a particular content type.
 * A content assist processor is a {@link org.eclipse.jface.text.contentassist.IContentAssistant}
 * plug-in.
 * <p>
 * This interface must be implemented by clients. Implementers should be
 * registered with a content assistant in order to get involved in the
 * assisting process.
 * </p>
*/
public interface IContentAssistProcessor {

	/**
	 * Returns a list of completion proposals based on the
	 * specified location within the document that corresponds
	 * to the current cursor position within the text viewer.
	 *
	 * @param viewer the viewer whose document is used to compute the proposals
	 * @param offset an offset within the document for which completions should be computed
	 * @return an array of completion proposals or <code>null</code> if no proposals are possible
	 */
	ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset);

	/**
	 * Returns information about possible contexts based on the
	 * specified location within the document that corresponds
	 * to the current cursor position within the text viewer.
	 *
	 * @param viewer the viewer whose document is used to compute the possible contexts
	 * @param offset an offset within the document for which context information should be computed
	 * @return an array of context information objects or <code>null</code> if no context could be found
	 */
	IContextInformation[] computeContextInformation(ITextViewer viewer, int offset);

	/**
	 * Returns the characters which when entered by the user should
	 * automatically trigger the presentation of possible completions.
	 *
	 * @return the auto activation characters for completion proposal or <code>null</code>
	 *		if no auto activation is desired
	 */
	char[] getCompletionProposalAutoActivationCharacters();

	/**
	 * Returns the characters which when entered by the user should
	 * automatically trigger the presentation of context information.
	 *
	 * @return the auto activation characters for presenting context information
	 *		or <code>null</code> if no auto activation is desired
	 */
	char[] getContextInformationAutoActivationCharacters();

	/**
	 * Returns the reason why this content assist processor
	 * was unable to produce any completion proposals or context information.
	 *
	 * @return an error message or <code>null</code> if no error occurred
	 */
	String getErrorMessage();

	/**
	 * Returns a validator used to determine when displayed context information
	 * should be dismissed. May only return <code>null</code> if the processor is
	 * incapable of computing context information. <p>
	 *
	 * @return a context information validator, or <code>null</code> if the processor
	 * 			is incapable of computing context information
	 */
	IContextInformationValidator getContextInformationValidator();
}
