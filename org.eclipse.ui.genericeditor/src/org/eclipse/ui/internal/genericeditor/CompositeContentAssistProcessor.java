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
package org.eclipse.ui.internal.genericeditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * A content assist processor that delegates all content assist
 * operations to children provided in constructor and aggregates
 * the results.
 * 
 * @since 1.0
 */
public class CompositeContentAssistProcessor implements IContentAssistProcessor {

	private List<IContentAssistProcessor> fContentAssistProcessors;

	/**
	 * Constructor
	 * @param contentAssistProcessors the children that will actually populate the output
	 *        of this content assist processor.
	 */
	public CompositeContentAssistProcessor(List<IContentAssistProcessor> contentAssistProcessors) {
		fContentAssistProcessors= contentAssistProcessors;
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		List<ICompletionProposal> res = new ArrayList<>();
		for (IContentAssistProcessor processor : this.fContentAssistProcessors) {
			res.addAll(Arrays.asList(processor.computeCompletionProposals(viewer, offset)));
		}
		return res.toArray(new ICompletionProposal[res.size()]);
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		List<IContextInformation> res = new ArrayList<>();
		for (IContentAssistProcessor processor : this.fContentAssistProcessors) {
			res.addAll(Arrays.asList(processor.computeContextInformation(viewer, offset)));
		}
		return res.toArray(new IContextInformation[res.size()]);
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		Set<Character> res = new HashSet<>();
		for (IContentAssistProcessor processor : this.fContentAssistProcessors) {
			char[] chars = processor.getCompletionProposalAutoActivationCharacters();
			if (chars != null) {
				for (char c : chars) {
					res.add(Character.valueOf(c));
				}
			}
		}
		return toCharArray(res);
	}

	private static char[] toCharArray(Set<Character> chars) {
		char[] res = new char[chars.size()];
		int i = 0;
		for (Character c : chars) {
			res[i] = c.charValue();
			i++;
		}
		return res;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		Set<Character> res = new HashSet<>();
		for (IContentAssistProcessor processor : this.fContentAssistProcessors) {
			char[] chars = processor.getContextInformationAutoActivationCharacters();
			if (chars != null) {
				for (char c : chars) {
					res.add(Character.valueOf(c));
				}
			}
		}
		return toCharArray(res);
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

}