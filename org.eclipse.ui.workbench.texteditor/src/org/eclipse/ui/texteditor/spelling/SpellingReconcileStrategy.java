/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.content.IContentType;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;


/**
 * Reconcile strategy used for spell checking.
 * <p>
 * <em>This API is provisional and may change any time before the 3.3 API freeze.</em>
 * </p>
 *
 * @since 3.3
 */
public class SpellingReconcileStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	private final String fSpellingAnnotationType;

	/**
	 * Spelling problem collector.
	 */
	private class SpellingProblemCollector implements ISpellingProblemCollector {

		/** Annotation model */
		private IAnnotationModel fAnnotationModel;

		/** Annotations to add */
		private Map fAddAnnotations;

		/**
		 * Initializes this collector with the given annotation model.
		 *
		 * @param annotationModel the annotation model
		 */
		public SpellingProblemCollector(IAnnotationModel annotationModel) {
			fAnnotationModel= annotationModel;
		}

		/*
		 * @see org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector#accept(org.eclipse.ui.texteditor.spelling.SpellingProblem)
		 */
		public void accept(SpellingProblem problem) {
//			int line= fDocument.getLineOfOffset(problem.getOffset()) + 1;
//			String word= fDocument.get(problem.getOffset(), problem.getLength());
//			boolean dictionaryMatch= false;
//			boolean sentenceStart= false;
//			if (problem instanceof SpellingProblem) {
//			dictionaryMatch= ((SpellingProblem)problem).isDictionaryMatch();
//			sentenceStart= ((SpellingProblem) problem).isSentenceStart();
//			}
//			// see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=81514
//			IEditorInput editorInput= fViewer.getEditorInput();
//			if (editorInput != null) {
//			CoreSpellingProblem iProblem= new CoreSpellingProblem(problem.getOffset(), problem.getOffset() + problem.getLength() - 1, line, problem.getMessage(), word, dictionaryMatch, sentenceStart, fDocument, editorInput.getName());
			fAddAnnotations.put(new Annotation(fSpellingAnnotationType, false, problem.getMessage()), new Position(problem.getOffset(), problem.getLength()));
		}

		/*
		 * @see org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector#beginCollecting()
		 */
		public void beginCollecting() {
			fAddAnnotations= new HashMap();
		}

		/*
		 * @see org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector#endCollecting()
		 */
		public void endCollecting() {
			List removeAnnotations= new ArrayList();
			for (Iterator iter= fAnnotationModel.getAnnotationIterator(); iter.hasNext();) {
				Annotation annotation= (Annotation) iter.next();
				if (fSpellingAnnotationType.equals(annotation.getType()))
					removeAnnotations.add(annotation);
			}

			if (fAnnotationModel instanceof IAnnotationModelExtension)
				((IAnnotationModelExtension) fAnnotationModel).replaceAnnotations((Annotation[]) removeAnnotations.toArray(new Annotation[removeAnnotations.size()]), fAddAnnotations);
			else {
				for (Iterator iter= removeAnnotations.iterator(); iter.hasNext();)
					fAnnotationModel.removeAnnotation((Annotation) iter.next());
				for (Iterator iter= fAddAnnotations.keySet().iterator(); iter.hasNext();) {
					Annotation annotation= (Annotation) iter.next();
					fAnnotationModel.addAnnotation(annotation, (Position) fAddAnnotations.get(annotation));
				}
			}

			fAddAnnotations= null;
		}
	}

	/** The id of the problem */
	public static final int SPELLING_PROBLEM_ID= 0x80000000;

	/** The text editor to operate on. */
	private ISourceViewer fViewer;

	/** The document to operate on. */
	private IDocument fDocument;

	/** The progress monitor. */
	private IProgressMonitor fProgressMonitor;

	private SpellingService fSpellingService;

	/**
	 * Creates a new comment reconcile strategy.
	 * 
	 * @param viewer the source viewer
	 * @param spellingService the spelling service to use
	 * @param spellingAnnotationType the spelling annotation type
	 */
	public SpellingReconcileStrategy(ISourceViewer viewer, SpellingService spellingService, String spellingAnnotationType) {
		Assert.isNotNull(viewer);
		Assert.isNotNull(spellingService);
		Assert.isNotNull(spellingAnnotationType);
		fViewer= viewer;
		fSpellingService= spellingService;
		fSpellingAnnotationType= spellingAnnotationType;
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#initialReconcile()
	 */
	public void initialReconcile() {
		reconcile(new Region(0, fDocument.getLength()));
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.reconciler.DirtyRegion,org.eclipse.jface.text.IRegion)
	 */
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		reconcile(subRegion);
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.IRegion)
	 */
	public void reconcile(IRegion region) {
		IAnnotationModel model= getAnnotationModel();
		if (model == null)
			return;

		try {
			SpellingContext context= new SpellingContext();
			context.setContentType(getContentType());
			SpellingReconcileStrategy.SpellingProblemCollector collector= new SpellingProblemCollector(model);
			fSpellingService.check(fDocument, context, collector, fProgressMonitor);
		} catch (CoreException x) {
			// swallow exception
		}
	}

	/**
	 * Returns the content type of the underlying editor input.
	 *
	 * @return the content type of the underlying editor input or
	 *         <code>null</code> if none could be determined
	 * @throws CoreException if reading or accessing the underlying store fails
	 */
	private IContentType getContentType() throws CoreException {
//		IDocumentProvider documentProvider= fViewer.getDocumentProvider();
//		if (documentProvider instanceof IDocumentProviderExtension4)
//		return ((IDocumentProviderExtension4) documentProvider).getContentType(fViewer.getEditorInput());
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#setDocument(org.eclipse.jface.text.IDocument)
	 */
	public void setDocument(IDocument document) {
		fDocument= document;
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor= monitor;
	}

	/**
	 * Returns the annotation model of the underlying editor input.
	 *
	 * @return the annotation model of the underlying editor input or
	 *         <code>null</code> if none could be determined
	 */
	private IAnnotationModel getAnnotationModel() {
		return fViewer.getAnnotationModel();
	}
}
