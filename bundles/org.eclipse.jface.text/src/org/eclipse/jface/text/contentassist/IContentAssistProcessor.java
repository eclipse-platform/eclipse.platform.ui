/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Christoph Läubrich - Bug 508821 - [Content assist] More flexible API in IContentAssistProcessor to decide whether to auto-activate or not
 *     Dawid Pakuła - [#1102] isAutoActivated flag
 *******************************************************************************/
package org.eclipse.jface.text.contentassist;

import org.eclipse.jface.text.ITextViewer;


/**
 * A content assist processor proposes completions and computes context information for a particular
 * content type. A content assist processor is a
 * {@link org.eclipse.jface.text.contentassist.IContentAssistant} plug-in.
 * <p>
 * This interface must be implemented by clients. Implementers should be registered with a content
 * assistant in order to get involved in the assisting process.
 * </p>
 * <p>
 * In order to provide backward compatibility for clients of <code>IContentAssistProcessor</code>,
 * extension interfaces are used to provide a means of evolution. The following extension interfaces
 * exist:
 * </p>
 * <ul>
 * <li>{@link org.eclipse.jface.text.contentassist.IContentAssistProcessorExtension} since version
 * 3.17 introducing the following functions:
 * <ul>
 * <li>isCompletionProposalAutoActivation(char, ITextViewer, int) providing context information when
 * calculating auto activation</li>
 * <li>isContextInformationAutoActivation(char, ITextViewer, int) providing context information when
 * calculating auto activation</li>
 * </ul>
 * </li>
 * </ul>
 */
public interface IContentAssistProcessor {

	/**
	 * Returns a list of completion proposals based on the specified location within the document
	 * that corresponds to the current cursor position within the text viewer.
	 *
	 * @param viewer the viewer whose document is used to compute the proposals
	 * @param offset an offset within the document for which completions should be computed
	 * @return an array of completion proposals or <code>null</code> if no proposals are possible
	 * @deprecated Since 3.24
	 * @see #computeCompletionProposals(IContentAssistRequest)
	 */
	@Deprecated
	default ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		throw new UnsupportedOperationException("Use computeCompletionProposals(IContentAssistRequest)"); //$NON-NLS-1$
	}

	/**
	 * Returns a list of completion proposals based on the specified location within the document
	 * that corresponds to the current cursor position within the text viewer.
	 *
	 * @param request object with context information to compute the possible contexts
	 * @return an array of completion proposals or <code>null</code> if no proposals are possible
	 * @since 3.24
	 */
	default ICompletionProposal[] computeCompletionProposals(IContentAssistRequest request) {
		return computeCompletionProposals(request.getViewer(), request.getOffset());
	}

	/**
	 * Returns information about possible contexts based on the specified location within the
	 * document that corresponds to the current cursor position within the text viewer.
	 *
	 * @param viewer the viewer whose document is used to compute the possible contexts
	 * @param offset an offset within the document for which context information should be computed
	 * @return an array of context information objects or <code>null</code> if no context could be
	 *         found
	 * @deprecated Since 3.24
	 * @see #computeCompletionProposals(IContentAssistRequest)
	 */
	@Deprecated
	default IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		throw new UnsupportedOperationException("Use computeContextInformation(IContentAssistRequest)"); //$NON-NLS-1$
	}

	/**
	 * Returns information about possible contexts based on the specified location within the
	 * document that corresponds to the current cursor position within the text viewer.
	 *
	 * @param request object with context information to compute the possible contexts
	 * @return an array of context information objects or <code>null</code> if no context could be
	 *         found
	 * @since 3.24
	 */
	default IContextInformation[] computeContextInformation(IContentAssistRequest request) {
		return computeContextInformation(request);
	}

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
	 * Returns a validator used to determine when displayed context information should be dismissed.
	 * May only return <code>null</code> if the processor is incapable of computing context
	 * information.
	 *
	 * @return a context information validator, or <code>null</code> if the processor is incapable
	 *         of computing context information
	 */
	IContextInformationValidator getContextInformationValidator();
}
