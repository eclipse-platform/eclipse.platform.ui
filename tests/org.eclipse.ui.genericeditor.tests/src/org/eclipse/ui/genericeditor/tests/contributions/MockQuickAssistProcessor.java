/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Obuchowicz (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests.contributions;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;

public class MockQuickAssistProcessor implements IQuickAssistProcessor{

	@Override
	public String getErrorMessage() {
		return "A MOCK ERROR MESSAGE";
	}

	@Override
	public boolean canFix(Annotation annotation) {
		return true;
	}

	@Override
	public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
		return true;
	}

	@Override
	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
		CompletionProposal proposal = new CompletionProposal("QUICK ASSIST PROPOSAL", 0, 10, 0);
		ICompletionProposal[] proposals = {proposal};
		return proposals;
	}

}
