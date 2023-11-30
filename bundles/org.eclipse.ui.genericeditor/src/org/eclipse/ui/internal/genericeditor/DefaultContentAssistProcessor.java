/*******************************************************************************
 * Copyright (c) 2017, 2021 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Sopot Cela (Red Hat Inc.)
 * Christoph Läubrich - Bug 570488 - [genericeditor][DefaultContentAssistProcessor] IllegalStateException: No match available
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 *
 * This content assist processor is a default processor which will be present if
 * no other auto-completion is registered for a given content-type. It splits
 * the text into 'words' (which are defined as anything in-between
 * non-alphanumeric characters) and offers them as auto-complete alternatives to
 * the matching prefix.
 *
 * E.g. if your file contains "this is a t^" and you ask for auto-completion at
 * ^ you will get 'this' as an alternative.
 */
public class DefaultContentAssistProcessor implements IContentAssistProcessor {

	private static final String NON_ALPHANUMERIC_LAST_REGEXP = "[^\\w](?!.*[^\\w])"; //$NON-NLS-1$
	private static final String NON_ALPHANUMERIC_REGEXP = "[^a-zA-Z0-9]+"; //$NON-NLS-1$
	private static final Pattern NON_ALPHANUMERIC_LAST_PATTERN = Pattern.compile(NON_ALPHANUMERIC_LAST_REGEXP);


	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		String text = viewer.getDocument().get();
		//wordStartIndex is the index of the last non-alphanumeric before 'offset' in text 'text'
		int wordStartIndex = findStartingPoint(text, offset);
		if (wordStartIndex < 0) {
			// not possible
			return null;
		}
		String[] tokens = text.split(NON_ALPHANUMERIC_REGEXP);

		// remove duplicates
		Set<String> tokenSet = new HashSet<>(Arrays.asList(tokens));

		String prefix = text.substring(wordStartIndex, offset);

		List<ICompletionProposal> proposals = new ArrayList<>();
		for (String token : tokenSet) {
			if ((token==null)||(token.length()<2)) {
				continue;
			}
			if (token.equals(prefix)) {
				continue;
			}
			if (token.startsWith(prefix)) {
				String completion = token.substring(prefix.length());
				CompletionProposal proposal = new CompletionProposal(completion, offset, 0,
						completion.length(), null,  token, null, null);
				proposals.add(proposal);
			}
		}
		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}

	private static int findStartingPoint(String text, int offset) {
		String substring = text.substring(0, offset);
		Matcher m = NON_ALPHANUMERIC_LAST_PATTERN.matcher(substring);
		if (m.find()) {
			return m.end();
		}
		return -1;
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
