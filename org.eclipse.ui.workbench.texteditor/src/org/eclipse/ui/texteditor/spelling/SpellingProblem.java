/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.IEditorInput;

import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;


/**
 * A spelling problem as reported by the {@link SpellingService} service to the
 * {@link ISpellingProblemCollector}.
 * <p>
 * This class is intended to be subclassed by clients.
 * </p>
 *
 * @see SpellingService
 * @see ISpellingProblemCollector
 * @since 3.1
 */
public abstract class SpellingProblem {

	/**
	 * Removes all spelling problems that are reported
	 * for the given <code>word</code> in the active editor.
	 * <p>
	 * <em>This a workaround to fix bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=134338
	 * for 3.2 at the time where spelling still resides in JDT Text.
	 * Once we move the spell check engine along with its quick fixes
	 * down to Platform Text we need to provide the proposals with
	 * a way to access the annotation model.</em>
	 * </p>
	 *
	 * @param editor the text editor, if <code>null</code> this method does nothing
	 * @param word the word for which to remove the problems or <code>null</code> to remove all
	 * @since 3.3
	 * @deprecated As of 3.4, replaced by {@link #removeAll(ISourceViewer, String)}
	 */
	public static void removeAllInActiveEditor(ITextEditor editor, String word) {
		if (editor == null)
			return;

		IDocumentProvider documentProvider= editor.getDocumentProvider();
		if (documentProvider == null)
			return;

		IEditorInput editorInput= editor.getEditorInput();
		if (editorInput == null)
			return;

		IAnnotationModel model= documentProvider.getAnnotationModel(editorInput);
		if (model == null)
			return;

		IDocument document= documentProvider.getDocument(editorInput);
		if (document == null)
			return;

		boolean supportsBatchReplace= (model instanceof IAnnotationModelExtension);
		List toBeRemovedAnnotations= new ArrayList();
		Iterator iter= model.getAnnotationIterator();
		while (iter.hasNext()) {
			Annotation annotation= (Annotation) iter.next();
			if (SpellingAnnotation.TYPE.equals(annotation.getType())) {
				boolean doRemove= word == null;
				if (word == null)
					doRemove= true;
				else {
					String annotationWord= null;
					Position pos= model.getPosition(annotation);
					try {
						annotationWord= document.get(pos.getOffset(), pos.getLength());
					} catch (BadLocationException e) {
						continue;
					}
					doRemove= word.equals(annotationWord);
				}
				if (doRemove) {
					if (supportsBatchReplace)
						toBeRemovedAnnotations.add(annotation);
					else
						model.removeAnnotation(annotation);
				}
			}
		}

		if (supportsBatchReplace && !toBeRemovedAnnotations.isEmpty()) {
			Annotation[] annotationArray= (Annotation[])toBeRemovedAnnotations.toArray(new Annotation[toBeRemovedAnnotations.size()]);
			((IAnnotationModelExtension)model).replaceAnnotations(annotationArray, null);
		}
	}

	/**
	 * Removes all spelling problems that are reported
	 * for the given <code>word</code> in the active editor.
	 *
	 * @param sourceViewer the source viewer
	 * @param word the word for which to remove the problems or <code>null</code> to remove all
	 * @since 3.4
	 */
	public static void removeAll(ISourceViewer sourceViewer, String word) {
		Assert.isNotNull(sourceViewer);

		IAnnotationModel model= sourceViewer.getAnnotationModel();
		if (model == null)
			return;

		IDocument document= sourceViewer.getDocument();
		if (document == null)
			return;

		boolean supportsBatchReplace= (model instanceof IAnnotationModelExtension);
		List toBeRemovedAnnotations= new ArrayList();
		Iterator iter= model.getAnnotationIterator();
		while (iter.hasNext()) {
			Annotation annotation= (Annotation) iter.next();
			if (SpellingAnnotation.TYPE.equals(annotation.getType())) {
				boolean doRemove= word == null;
				if (word == null)
					doRemove= true;
				else {
					String annotationWord= null;
					Position pos= model.getPosition(annotation);
					try {
						annotationWord= document.get(pos.getOffset(), pos.getLength());
					} catch (BadLocationException e) {
						continue;
					}
					doRemove= word.equals(annotationWord);
				}
				if (doRemove) {
					if (supportsBatchReplace)
						toBeRemovedAnnotations.add(annotation);
					else
						model.removeAnnotation(annotation);
				}
			}
		}

		if (supportsBatchReplace && !toBeRemovedAnnotations.isEmpty()) {
			Annotation[] annotationArray= (Annotation[])toBeRemovedAnnotations.toArray(new Annotation[toBeRemovedAnnotations.size()]);
			((IAnnotationModelExtension)model).replaceAnnotations(annotationArray, null);
		}
	}

	/**
	 * Returns the offset of the incorrectly spelled region.
	 *
	 * @return the offset of the incorrectly spelled region
	 */
	public abstract int getOffset();

	/**
	 * Returns the length of the incorrectly spelled region.
	 *
	 * @return the length of the incorrectly spelled region
	 */
	public abstract int getLength();

	/**
	 * Returns a localized, human-readable message string which describes the spelling problem.
	 *
	 * @return a localized, human-readable message string which describes the spelling problem
	 */
	public abstract String getMessage();

	/**
	 * Returns the proposals for the incorrectly spelled region.
	 *
	 * @return the proposals for the incorrectly spelled region
	 */
	public abstract ICompletionProposal[] getProposals();

	/**
	 * Returns the proposals for the incorrectly spelled region.
	 *
	 * @param context the invocation context or <code>null</code> if none
	 * @return the proposals for the incorrectly spelled region
	 * @since 3.4
	 */
	public ICompletionProposal[] getProposals(IQuickAssistInvocationContext context) {
		return getProposals();
	}
}
