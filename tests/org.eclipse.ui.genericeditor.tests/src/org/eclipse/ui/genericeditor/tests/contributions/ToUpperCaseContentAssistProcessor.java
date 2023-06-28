/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests.contributions;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension9;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class ToUpperCaseContentAssistProcessor implements IContentAssistProcessor {

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		if (!(viewer instanceof ITextViewerExtension9)) {
			return new ICompletionProposal[0];
		}
		ITextSelection selection= ((ITextViewerExtension9)viewer).getLastKnownSelection();
		if (selection.isEmpty() || selection.getLength() == 0) {
			return new ICompletionProposal[0];
		}
		String initialText;
		try {
			initialText= viewer.getDocument().get(selection.getOffset(), selection.getLength());
			return new ICompletionProposal[] {
				new CompletionProposal(initialText.toUpperCase(), selection.getOffset(), initialText.length(), selection.getOffset() + initialText.length())
			};
		} catch (BadLocationException e) {
			return new ICompletionProposal[0];
		}

	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return new IContextInformation[0];
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[0];
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return new char[0];
	}

	@Override
	public String getErrorMessage() {
		return getClass().getName();
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

}
