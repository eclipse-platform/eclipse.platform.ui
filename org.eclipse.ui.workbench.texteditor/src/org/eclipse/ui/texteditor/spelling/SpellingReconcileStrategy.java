/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor.spelling;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProviderExtension4;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A reconcile strategy for spelling that uses the {@link SpellingService} service
 * on the current document. This strategy has to be configured with a
 * {@link ISpellingProblemCollector}, which will be responsible for processing the
 * resulting {@link SpellingProblem}s.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p><p>
 * Not yet for public use. API under construction.
 * </p>
 * 
 * @see ISpellingProblemCollector
 * @see SpellingProblem
 * @since 3.1
 */
public class SpellingReconcileStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	/** Text editor to operate on */
	private ITextEditor fEditor;

	/** Document to operate on */
	private IDocument fDocument;

	/** Progress monitor */
	private IProgressMonitor fProgressMonitor;

	/** Spelling service */
	private SpellingService fSpellingService;
	
	/**
	 * Configures this strategy with the given editor and
	 * preferences.
	 * 
	 * @param editor the text editor to operate on
	 * @param spellingService the spelling service to use
	 */
	public SpellingReconcileStrategy(ITextEditor editor, SpellingService spellingService) {
		fEditor= editor;
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
		IAnnotationModel annotationModel= fEditor.getDocumentProvider().getAnnotationModel(fEditor.getEditorInput());
		if (annotationModel != null) {
			SpellingContext context= new SpellingContext();
			context.setContentType(getContentType());
			fSpellingService.check(fDocument, context, new DefaultSpellingProblemCollector(annotationModel), fProgressMonitor);
		}
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
	 * @return the detected content type of the current document's underlying element
	 */
	private IContentType getContentType() {
		IDocumentProvider documentProvider= fEditor.getDocumentProvider();
		if (documentProvider instanceof IDocumentProviderExtension4)
			try {
				IContentDescription desc= ((IDocumentProviderExtension4) documentProvider).getContentDescription(fEditor.getEditorInput());
				if (desc != null)
					return desc.getContentType();
			} catch (CoreException x) {
				TextEditorPlugin.getDefault().getLog().log(x.getStatus());
			}
		return null;
	}
}
