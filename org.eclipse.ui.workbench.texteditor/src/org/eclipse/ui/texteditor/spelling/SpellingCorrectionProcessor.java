/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor.spelling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.TextInvocationContext;

import org.eclipse.ui.internal.texteditor.spelling.NoCompletionsProposal;


/**
 * Spelling correction processor used to show quick
 * fixes for spelling problems.
 *
 * @since 3.3
 */
public final class SpellingCorrectionProcessor implements IQuickAssistProcessor {


	private static final ICompletionProposal[] fgNoSuggestionsProposal=  new ICompletionProposal[] { new NoCompletionsProposal() };


	/*
	 * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
	 */
	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext quickAssistContext) {
		ISourceViewer viewer= quickAssistContext.getSourceViewer();
		int documentOffset= quickAssistContext.getOffset();

		int length= viewer != null ? viewer.getSelectedRange().y : -1;
		TextInvocationContext context= new TextInvocationContext(viewer, documentOffset, length);


		IAnnotationModel model= viewer.getAnnotationModel();
		if (model == null)
			return fgNoSuggestionsProposal;

		List proposals= computeProposals(context, model);
		if (proposals.isEmpty())
			return fgNoSuggestionsProposal;

		return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals.size()]);
	}

	private boolean isAtPosition(int offset, Position pos) {
		return (pos != null) && (offset >= pos.getOffset() && offset <= (pos.getOffset() +  pos.getLength()));
	}

	private List computeProposals(IQuickAssistInvocationContext context, IAnnotationModel model) {
		int offset= context.getOffset();
		ArrayList annotationList= new ArrayList();
		Iterator iter= model.getAnnotationIterator();
		while (iter.hasNext()) {
			Annotation annotation= (Annotation)iter.next();
			if (canFix(annotation)) {
				Position pos= model.getPosition(annotation);
				if (isAtPosition(offset, pos)) {
					collectSpellingProblems(annotation, annotationList);
				}
			}
		}
		SpellingProblem[] spellingProblems= (SpellingProblem[]) annotationList.toArray(new SpellingProblem[annotationList.size()]);
		return computeProposals(context, spellingProblems);
	}

	private void collectSpellingProblems(Annotation annotation, List problems) {
		if (annotation instanceof SpellingAnnotation)
			problems.add(((SpellingAnnotation)annotation).getSpellingProblem());
	}

	private List computeProposals(IQuickAssistInvocationContext context, SpellingProblem[] spellingProblems) {
		List proposals= new ArrayList();
		for (int i= 0; i < spellingProblems.length; i++)
			proposals.addAll(Arrays.asList(spellingProblems[i].getProposals(context)));

		return proposals;
	}

	/*
	 * @see IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage() {
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.quickassist.IQuickAssistProcessor#canFix(org.eclipse.jface.text.source.Annotation)
	 */
	public boolean canFix(Annotation annotation) {
		return annotation instanceof SpellingAnnotation && !annotation.isMarkedDeleted();
	}

	/*
	 * @see org.eclipse.jface.text.quickassist.IQuickAssistProcessor#canAssist(org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext)
	 */
	public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
		return false;
	}

}
