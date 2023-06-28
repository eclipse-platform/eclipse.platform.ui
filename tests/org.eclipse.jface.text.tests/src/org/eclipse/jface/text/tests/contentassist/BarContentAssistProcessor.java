/*******************************************************************************
 * Copyright (c) 2016, 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.)
 *     Stephan Wahlbrink <sw@wahlbrink.eu> - Bug 512251 - Fix IllegalArgumentException in ContextInformationPopup
 *******************************************************************************/

package org.eclipse.jface.text.tests.contentassist;

import java.util.Arrays;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessorExtension;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationExtension;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;


public class BarContentAssistProcessor implements IContentAssistProcessorExtension {

	public static final String PROPOSAL= "bars are good for a beer.";

	protected static class BarContextInformation implements IContextInformation, IContextInformationExtension {

		private String contextDisplayString;

		private String informationDisplayString;

		private int offset;

		public BarContextInformation(String contextDisplayString, String informationDisplayString, int offset) {
			this.contextDisplayString= contextDisplayString;
			this.informationDisplayString= informationDisplayString;
			this.offset= offset;
		}

		@Override
		public String getContextDisplayString() {
			return contextDisplayString;
		}

		@Override
		public Image getImage() {
			return null;
		}

		@Override
		public String getInformationDisplayString() {
			return informationDisplayString;
		}

		@Override
		public int getContextInformationPosition() {
			return offset;
		}

	}

	protected static class ContextInformationValidator implements IContextInformationValidator {

		protected BarContextInformation info;

		protected ITextViewer viewer;

		protected int offset;

		@Override
		public void install(IContextInformation info, ITextViewer viewer, int offset) {
			if (info instanceof BarContextInformation) {
				this.info= (BarContextInformation) info;
			}
			this.viewer= viewer;
			this.offset= offset;
		}

		@Override
		public boolean isContextInformationValid(int offset) {
			if (this.info == null) {
				return false;
			}
			try {
				IDocument document= viewer.getDocument();
				IRegion line= document.getLineInformationOfOffset(this.offset);
				int end= line.getOffset() + line.getLength();
				return (offset >= this.offset && offset < end);
			} catch (BadLocationException e) {
				return false;
			}
		}
	}


	private final String completeString;

	private char activationChar;

	public BarContentAssistProcessor() {
		this(PROPOSAL);
	}

	public BarContentAssistProcessor(String completeString) {
		this.completeString= completeString;
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		for (int offsetInProposal= Math.min(this.completeString.length(), viewer.getDocument().getLength()); offsetInProposal > 0; offsetInProposal--) {
			String maybeMatchingString= this.completeString.substring(0, offsetInProposal);
			try {
				int lastIndex= offset - offsetInProposal + this.completeString.length();
				if (offset >= offsetInProposal && viewer.getDocument().get(offset - offsetInProposal, maybeMatchingString.length()).equals(maybeMatchingString)) {
					CompletionProposal proposal= new CompletionProposal(this.completeString.substring(offsetInProposal), offset, 0, lastIndex);
					return new ICompletionProposal[] { proposal };
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		return new ICompletionProposal[0];
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	/**
	 * Creates context info "idx= <word index in #PROPOSAL>" at the end of a word.
	 **/
	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		try {
			IDocument document= viewer.getDocument();
			int wordBegin= offset;
			while (wordBegin > 0 && Character.isLetterOrDigit(document.getChar(wordBegin - 1))) {
				wordBegin--;
			}
			if (wordBegin < offset) {
				String word= document.get(wordBegin, offset - wordBegin);
				int wordIdx= Arrays.asList(completeString.split("\\W")).indexOf(word);
				if (wordIdx >= 0) {
					return new IContextInformation[] {
							new BarContextInformation(word, "idx= " + wordIdx, wordBegin)
					};
				}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return new ContextInformationValidator();
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public boolean isCompletionProposalAutoActivation(char c, ITextViewer viewer, int offset) {
		return activationChar == c;
	}

	@Override
	public boolean isContextInformationAutoActivation(char c, ITextViewer viewer, int offset) {
		return false;
	}

	public void setCompletionProposalAutoActivationChar(char c) {
		activationChar= c;
	}

}
