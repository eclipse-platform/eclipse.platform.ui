/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.examples.javaeditor.javadoc;


import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * Example Java doc completion processor.
 */
public class JavaDocCompletionProcessor implements IContentAssistProcessor {

	protected final static String[] fgProposals= { "@author", "@deprecated", "@exception", "@param", "@return", "@see", "@serial", "@serialData", "@serialField", "@since", "@throws", "@version" }; //$NON-NLS-12$ //$NON-NLS-11$ //$NON-NLS-10$ //$NON-NLS-7$ //$NON-NLS-9$ //$NON-NLS-8$ //$NON-NLS-6$ //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		ICompletionProposal[] result= new ICompletionProposal[fgProposals.length];
		for (int i= 0; i < fgProposals.length; i++)
			result[i]= new CompletionProposal(fgProposals[i], documentOffset, 0, fgProposals[i].length());
		return result;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
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
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}
}
