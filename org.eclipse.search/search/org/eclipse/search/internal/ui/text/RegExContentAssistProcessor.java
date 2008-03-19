/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor;
import org.eclipse.jface.contentassist.SubjectControlContextInformationValidator;
import org.eclipse.jface.fieldassist.IContentProposal;

import org.eclipse.jface.text.FindReplaceDocumentAdapterContentProposalProvider;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * Content assist processor for regular expressions.
 *
 * @since 3.0
 */
final class RegExContentAssistProcessor implements IContentAssistProcessor, ISubjectControlContentAssistProcessor {

	/**
	 * The context information validator.
	 */
	private IContextInformationValidator fValidator= new SubjectControlContextInformationValidator(this);

	/**
	 * <code>true</code> iff the processor is for the find field.
	 * <code>false</code> iff the processor is for the replace field.
	 */
	private final boolean fIsFind;
	
	public RegExContentAssistProcessor(boolean isFind) {
		fIsFind= isFind;
	}
	
	/*
	 * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		throw new UnsupportedOperationException("ITextViewer not supported"); //$NON-NLS-1$
	}

	/*
	 * @see IContentAssistProcessor#computeContextInformation(ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
		throw new UnsupportedOperationException("ITextViewer not supported"); //$NON-NLS-1$
	}

	/*
	 * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		if (fIsFind)
			return new char[] {'\\', '[', '('};
		
		return new char[] {'$'};
	}

	/*
	 * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return new char[] { };
	}

	/*
	 * @see IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator() {
		return fValidator;
	}

	/*
	 * @see IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage() {
		return null;
	}

	/*
	 * @see ISubjectControlContentAssistProcessor#computeCompletionProposals(IContentAssistSubjectControl, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
		FindReplaceDocumentAdapterContentProposalProvider proposalProvider= new FindReplaceDocumentAdapterContentProposalProvider(fIsFind);
		IContentProposal[] contentProposals= proposalProvider.getProposals(contentAssistSubjectControl.getDocument().get(), documentOffset);
		return adaptToCompletionProposals(contentProposals, documentOffset);
	}

	/**
	 * Adapts the given content proposals to completion proposals.
	 * 
	 * @param contentProposals the content proposals
	 * @param documentOffset the offset within the document for which the completions are computed
	 * @return the completion proposals
	 * @since 3.4
	 */
	private ICompletionProposal[] adaptToCompletionProposals(IContentProposal[] contentProposals, int documentOffset) {
		ICompletionProposal[] completionProposals= new ICompletionProposal[contentProposals.length];
		for (int i= 0; i < contentProposals.length; i++)
			completionProposals[i]= createCompletionProposal(contentProposals[i], documentOffset);
		return completionProposals;
	}

	/**
	 * Creates a completion proposal from the given content proposal.
	 * 
	 * @param contentProposal the content proposal
	 * @param documentOffset  the offset within the document for which the completions are computed
	 * @return the completion proposal
	 * @since 3.4
	 */
	private ICompletionProposal createCompletionProposal(IContentProposal contentProposal, int documentOffset) {
		String replacementString= contentProposal.getContent();
		int cursorPosition= contentProposal.getCursorPosition();
		String displayString= contentProposal.getLabel();
		String additionalInfo= contentProposal.getDescription();
		return new CompletionProposal(replacementString, documentOffset, 0, cursorPosition, null, displayString, null, additionalInfo);
	}

	/*
	 * @see ISubjectControlContentAssistProcessor#computeContextInformation(IContentAssistSubjectControl, int)
	 */
	public IContextInformation[] computeContextInformation(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
		return null;
	}
}
