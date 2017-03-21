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
import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;

/**
 * A quick assist processor that delegates all content assist
 * operations to children provided in constructor and aggregates
 * the results.
 * 
 * @since 1.0
 */
public class CompositeQuickAssistProcessor implements IQuickAssistProcessor {

	private List<IQuickAssistProcessor> fProcessors;

	public CompositeQuickAssistProcessor(List<IQuickAssistProcessor> processors) {
		this.fProcessors = processors;
	}

	@Override
	public String getErrorMessage() {
		StringBuilder res = new StringBuilder();
		for (IQuickAssistProcessor processor : this.fProcessors) {
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
	public boolean canFix(Annotation annotation) {
		for (IQuickAssistProcessor processor : this.fProcessors) {
			if (processor.canFix(annotation)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
		for (IQuickAssistProcessor processor : this.fProcessors) {
			if (processor.canAssist(invocationContext)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
		List<ICompletionProposal> res = new ArrayList<>();
		for (IQuickAssistProcessor processor : this.fProcessors) {
			ICompletionProposal[] proposals = processor.computeQuickAssistProposals(invocationContext);
			if (proposals != null) {
				res.addAll(Arrays.asList(proposals));
			}
		}
		return res.toArray(new ICompletionProposal[res.size()]);
	}

}
