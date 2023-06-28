/*******************************************************************************
 * Copyright (c) 2021 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.contentassist;

import org.eclipse.jface.text.ITextViewer;

/**
 * Extension interface of {@link IContentAssistProcessor} that allows to get additional information
 * when queried for auto activation
 *
 * @since 3.17
 */
public interface IContentAssistProcessorExtension extends IContentAssistProcessor {

	/**
	 * @deprecated use {@link #isCompletionProposalAutoActivation(char, ITextViewer, int)} instead
	 * @noreference use {@link #isCompletionProposalAutoActivation(char, ITextViewer, int)} instead
	 * @nooverride This default method is not intended to be re-implemented or extended by
	 *             clients.noimplement implement
	 *             {@link #isCompletionProposalAutoActivation(char, ITextViewer, int)} instead
	 */
	@Deprecated
	@Override
	default char[] getCompletionProposalAutoActivationCharacters() {
		throw new UnsupportedOperationException("use isCompletionProposalAutoActivation instead"); //$NON-NLS-1$
	}


	/**
	 * @deprecated use {@link #isContextInformationAutoActivation(char, ITextViewer, int)} instead
	 * @noreference use {@link #isContextInformationAutoActivation(char, ITextViewer, int)} instead
	 * @nooverride This default method is not intended to be re-implemented or extended by
	 *             clients.noimplement implement
	 *             {@link #isContextInformationAutoActivation(char, ITextViewer, int)} instead
	 */
	@Deprecated
	@Override
	default char[] getContextInformationAutoActivationCharacters() {
		throw new UnsupportedOperationException("use isContextInformationAutoActivation instead"); //$NON-NLS-1$
	}

	/**
	 * Check if the given event should trigger an automatic completion proposal activation
	 *
	 * @param c the character to check
	 * @param viewer the viewer
	 * @param offset the current offset
	 * @return <code>true</code> if auto activation is desired, <code>false</code> otherwise
	 */
	boolean isCompletionProposalAutoActivation(char c, ITextViewer viewer, int offset);
	/**
	 * Check if the given event should trigger an automatic context info activation
	 *
	 * @param c the character to check
	 * @param viewer the viewer
	 * @param offset the current offset
	 * @return <code>true</code> if auto activation is desired, <code>false</code> otherwise
	 */
	boolean isContextInformationAutoActivation(char c, ITextViewer viewer, int offset);

	static IContentAssistProcessorExtension adapt(IContentAssistProcessor processor) {
		if (processor == null) {
			return null;
		}
		if (processor instanceof IContentAssistProcessorExtension) {
			return (IContentAssistProcessorExtension) processor;
		}
		return new IContentAssistProcessorExtension() {

			@Override
			public String getErrorMessage() {
				return processor.getErrorMessage();
			}

			@Override
			public IContextInformationValidator getContextInformationValidator() {
				return processor.getContextInformationValidator();
			}

			@Override
			public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
				return processor.computeContextInformation(viewer, offset);
			}

			@Override
			public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
				return processor.computeCompletionProposals(viewer, offset);
			}

			@Deprecated
			@Override
			public char[] getCompletionProposalAutoActivationCharacters() {
				return processor.getCompletionProposalAutoActivationCharacters();
			}

			@Deprecated
			@Override
			public char[] getContextInformationAutoActivationCharacters() {
				return processor.getContextInformationAutoActivationCharacters();
			}

			@Override
			public boolean isCompletionProposalAutoActivation(char c, ITextViewer viewer, int offset) {
				char[] triggers= processor.getCompletionProposalAutoActivationCharacters();
				if (triggers != null) {
					for (char trigger : triggers) {
						if (c == trigger) {
							return true;
						}
					}
				}
				return false;
			}

			@Override
			public boolean isContextInformationAutoActivation(char c, ITextViewer viewer, int offset) {
				char[] triggers= processor.getContextInformationAutoActivationCharacters();
				if (triggers != null) {
					for (char trigger : triggers) {
						if (c == trigger) {
							return true;
						}
					}
				}
				return false;
			}
		};
	}

}
