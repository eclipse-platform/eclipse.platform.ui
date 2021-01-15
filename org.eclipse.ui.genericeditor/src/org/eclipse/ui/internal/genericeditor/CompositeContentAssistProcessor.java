/*******************************************************************************
 * Copyright (c) 2016, 2021 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Mickael Istria (Red Hat Inc.) - Initial API and implementation
 *  Christoph Läubrich - Bug 508821 - [Content assist] More flexible API in IContentAssistProcessor to decide whether to auto-activate or not
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistProcessorExtension;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * A content assist processor that delegates all content assist operations to
 * children provided in constructor and aggregates the results.
 *
 * @since 1.0
 */
public class CompositeContentAssistProcessor implements IContentAssistProcessorExtension, IContentAssistProcessor {

	private List<IContentAssistProcessor> fContentAssistProcessors;

	/**
	 * Constructor
	 * 
	 * @param contentAssistProcessors the children that will actually populate the
	 *                                output of this content assist processor.
	 */
	public CompositeContentAssistProcessor(List<IContentAssistProcessor> contentAssistProcessors) {
		fContentAssistProcessors = contentAssistProcessors;
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		List<ICompletionProposal> res = new ArrayList<>();
		for (IContentAssistProcessor processor : this.fContentAssistProcessors) {
			ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, offset);
			if (proposals != null) {
				res.addAll(Arrays.asList(proposals));
			}
		}
		return res.toArray(new ICompletionProposal[res.size()]);
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		List<IContextInformation> res = new ArrayList<>();
		for (IContentAssistProcessor processor : this.fContentAssistProcessors) {
			IContextInformation[] contextInformation = processor.computeContextInformation(viewer, offset);
			if (contextInformation != null) {
				res.addAll(Arrays.asList(contextInformation));
			}
		}
		return res.toArray(new IContextInformation[res.size()]);
	}

	@Override
	public String getErrorMessage() {
		StringBuilder res = new StringBuilder();
		for (IContentAssistProcessor processor : this.fContentAssistProcessors) {
			String errorMessage = processor.getErrorMessage();
			if (errorMessage != null) {
				res.append(errorMessage);
				res.append('\n');
			}
		}
		if (res.length() == 0) {
			return null;
		}
		return res.toString();
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	@Override
	public boolean isCompletionProposalAutoActivation(char c, ITextViewer viewer, int offset) {
		for (IContentAssistProcessor processor : this.fContentAssistProcessors) {
			IContentAssistProcessorExtension adapt = IContentAssistProcessorExtension.adapt(processor);
			if (adapt.isCompletionProposalAutoActivation(c, viewer, offset)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isContextInformationAutoActivation(char c, ITextViewer viewer, int offset) {
		for (IContentAssistProcessor processor : this.fContentAssistProcessors) {
			IContentAssistProcessorExtension adapt = IContentAssistProcessorExtension.adapt(processor);
			if (adapt.isContextInformationAutoActivation(c, viewer, offset)) {
				return true;
			}
		}
		return false;
	}

}