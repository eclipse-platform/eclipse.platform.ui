/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.UndoEdit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import org.eclipse.ltk.internal.core.refactoring.Lock;
import org.eclipse.ltk.internal.core.refactoring.TextChanges;
import org.eclipse.ltk.internal.core.refactoring.UndoDocumentChange;

/**
 * A text change that operates directly on instances of {@link IDocument}.
 * The document change uses a simple length compare to check if it
 * is still valid. So as long as its length hasn't changed the text edits
 * managed have a valid range and can be applied to the document. The
 * same applies to the undo change returned from the perform method.
 *
 * <p>
 * Note: this class is not intended to be extended by clients.
 * </p>
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DocumentChange extends TextChange {

	private IDocument fDocument;
	private int fLength;

	/**
	 * Creates a new <code>DocumentChange</code> for the given
	 * {@link IDocument}.
	 *
	 * @param name the change's name. Has to be a human readable name.
	 * @param document the document this change is working on
	 */
	public DocumentChange(String name, IDocument document) {
		super(name);
		Assert.isNotNull(document);
		fDocument= document;
	}

	@Override
	public Object getModifiedElement(){
		return fDocument;
	}

	@Override
	public void initializeValidationData(IProgressMonitor pm) {
		// as long as we don't have modification stamps on documents
		// we can only remember its length.
		fLength= fDocument.getLength();
	}

	@Override
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
		pm.beginTask("", 1); //$NON-NLS-1$
		RefactoringStatus result= TextChanges.isValid(fDocument, fLength);
		pm.worked(1);
		return result;
	}

	@Override
	protected IDocument acquireDocument(IProgressMonitor pm) throws CoreException {
		return fDocument;
	}

	@Override
	protected void commit(IDocument document, IProgressMonitor pm) throws CoreException {
		// do nothing
	}

	@Override
	protected void releaseDocument(IDocument document, IProgressMonitor pm) throws CoreException {
		//do nothing
	}

	/*
	 * @see org.eclipse.ltk.core.refactoring.TextChange#performEdits(org.eclipse.jface.text.IDocument)
	 * @since 3.6
	 */
	@Override
	protected UndoEdit performEdits(final IDocument document) throws BadLocationException, MalformedTreeException {
		ITextFileBufferManager fileBufferManager= FileBuffers.getTextFileBufferManager();

		ITextFileBuffer fileBuffer= fileBufferManager.getTextFileBuffer(document);
		if (fileBuffer == null || ! fileBuffer.isSynchronizationContextRequested()) {
			return super.performEdits(document);
		}

		/** The lock for waiting for computation in the UI thread to complete. */
		final Lock completionLock= new Lock();
		final UndoEdit[] result= new UndoEdit[1];
		final BadLocationException[] exception= new BadLocationException[1];
		Runnable runnable= () -> {
			synchronized (completionLock) {
				try {
					result[0]= DocumentChange.super.performEdits(document);
				} catch (BadLocationException e) {
					exception[0]= e;
				} finally {
					completionLock.fDone= true;
					completionLock.notifyAll();
				}
			}
		};

		synchronized (completionLock) {
			fileBufferManager.execute(runnable);
			while (! completionLock.fDone) {
				try {
					completionLock.wait(500);
				} catch (InterruptedException x) {
				}
			}
		}

		if (exception[0] != null) {
			throw exception[0];
		}

		return result[0];
	}

	@Override
	protected Change createUndoChange(UndoEdit edit) {
		return new UndoDocumentChange(getName(), fDocument, edit);
	}
}

