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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

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
 * FIXME: must honor progress monitor.
 *
 * @since 3.3
 */
public class SpellingReconcileStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	/** Text content type */
	private static final IContentType TEXT_CONTENT_TYPE= Platform.getContentTypeManager().getContentType(IContentTypeManager.CT_TEXT);


	/**
	 * Spelling problem collector.
	 */
	private class SpellingProblemCollector implements ISpellingProblemCollector {

		/** Annotation model. */
		private IAnnotationModel fAnnotationModel;

		/** Annotations to add. */
		private Map fAddAnnotations;
		
		/** Annotations to remove. */
		private Annotation[] fAnnotationsToRemove= new Annotation[0];

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
			fAddAnnotations.put(new SpellingAnnotation(problem), new Position(problem.getOffset(), problem.getLength()));
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
			if (fAnnotationModel instanceof IAnnotationModelExtension)
				((IAnnotationModelExtension)fAnnotationModel).replaceAnnotations(fAnnotationsToRemove, fAddAnnotations);
			else {
				for (int i= 0; i < fAnnotationsToRemove.length; i++)
					fAnnotationModel.removeAnnotation(fAnnotationsToRemove[i]);
				for (Iterator iter= fAddAnnotations.keySet().iterator(); iter.hasNext();) {
					Annotation annotation= (Annotation)iter.next();
					fAnnotationModel.addAnnotation(annotation, (Position)fAddAnnotations.get(annotation));
				}
			}

			Set toRemove= fAddAnnotations.keySet();
			fAnnotationsToRemove= (Annotation[])toRemove.toArray(new Annotation[toRemove.size()]);
			fAddAnnotations= null;
		}
	}

	/** The text editor to operate on. */
	private ISourceViewer fViewer;

	/** The document to operate on. */
	private IDocument fDocument;

	/** The progress monitor. */
	private IProgressMonitor fProgressMonitor;

	private SpellingService fSpellingService;
	
	private SpellingProblemCollector fSpellingProblemCollector;
	

	/**
	 * Creates a new comment reconcile strategy.
	 * 
	 * @param viewer the source viewer
	 * @param spellingService the spelling service to use
	 */
	public SpellingReconcileStrategy(ISourceViewer viewer, SpellingService spellingService) {
		Assert.isNotNull(viewer);
		Assert.isNotNull(spellingService);
		fViewer= viewer;
		fSpellingService= spellingService;
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
		if (fViewer.getAnnotationModel() == null)
			return;

		try {
			SpellingContext context= new SpellingContext();
			context.setContentType(getContentType());
			fSpellingService.check(fDocument, context, fSpellingProblemCollector, fProgressMonitor);
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

		// XXX: find a better way to get content type
		
//		IDocumentProvider documentProvider= fViewer.getDocumentProvider();
//		if (documentProvider instanceof IDocumentProviderExtension4)
//		return ((IDocumentProviderExtension4) documentProvider).getContentType(fViewer.getEditorInput());
		return TEXT_CONTENT_TYPE;
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#setDocument(org.eclipse.jface.text.IDocument)
	 */
	public void setDocument(IDocument document) {
		fDocument= document;
		fSpellingProblemCollector= new SpellingProblemCollector(fViewer.getAnnotationModel());
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor= monitor;
	}

}
