/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests.contributions;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class BarContentAssistProcessor implements IContentAssistProcessor {

	public static final String PROPOSAL = "bars are good for a beer.";
	private final String completeString;

	public BarContentAssistProcessor() {
		this(PROPOSAL);
	}

	public BarContentAssistProcessor(String completeString) {
		this.completeString = completeString;
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		for (int offsetInProposal = Math.min(this.completeString.length(), viewer.getDocument().getLength()); offsetInProposal > 0; offsetInProposal--) {
			String maybeMatchingString = this.completeString.substring(0, offsetInProposal);
			try {
				int lastIndex = offset - offsetInProposal + this.completeString.length();
				if (offset >= offsetInProposal && viewer.getDocument().get(offset - offsetInProposal, maybeMatchingString.length()).equals(maybeMatchingString)) {
					CompletionProposal proposal = new CompletionProposal(this.completeString.substring(offsetInProposal), offset, 0, lastIndex);
					return new ICompletionProposal[] { proposal };
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		return new ICompletionProposal[0];
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

}
