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
package org.eclipse.core.filebuffers.manipulation;

import java.util.Map;

import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferStatusCodes;
import org.eclipse.core.filebuffers.ITextFileBuffer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.TextUtilities;

import org.eclipse.text.edits.TextEdit;

/**
 * Abstraction for a file buffer operation that works on text file buffers.
 * Subclasses have to override the <code>computeTextEdits</code> method in
 * order to provide a sequence of {@link org.eclipse.text.edits.TextEdit}
 * objects.
 * <p>
 * Not yet for public use. API under construction.
 * 
 * @since 3.1
 */
public abstract class TextFileBufferOperation implements IFileBufferOperation {	
	
	
	/**
	 * Computes and returns a list of text edits. Subclasses have to provide that method.
	 * 
	 * @param textFileBuffer the text file buffer to manipulate
	 * @param progressMonitor the progress monitor
	 * @return the text edits describing the content manipulation
	 * @throws CoreException in case the computation failed
	 * @throws OperationCanceledException in case the progress monitor has been set to canceled
	 */
	protected abstract TextEdit[] computeTextEdits(ITextFileBuffer textFileBuffer, IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException;
	
	
	private String fOperationName;
	private Object fRewriteSessionId;
	
	/**
	 * Creates a new operation with the given name.
	 * 
	 * @param operationName the name of the operation
	 */
	protected TextFileBufferOperation(String operationName) {
		fOperationName= operationName;
	}
	
	/*
	 * @see org.eclipse.core.internal.filebuffers.textmanipulation.IFileBufferOperation#getOperationName()
	 */
	public String getOperationName() {
		return fOperationName;
	}

	/*
	 * @see org.eclipse.core.internal.filebuffers.textmanipulation.IFileBufferOperation#run(org.eclipse.core.filebuffers.IFileBuffer, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IFileBuffer fileBuffer, IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {
		
		if (fileBuffer instanceof ITextFileBuffer) {
			ITextFileBuffer textFileBuffer= (ITextFileBuffer) fileBuffer;
			progressMonitor.beginTask(getOperationName(), 100);
			try {
				SubProgressMonitor subMonitor= new SubProgressMonitor(progressMonitor, 50);
				TextEdit[] edits= computeTextEdits(textFileBuffer, subMonitor);
				subMonitor.done();
				if (edits != null && edits.length > 0) {
					startRewriteSession(textFileBuffer);
					Object stateData= preProcess(textFileBuffer);
					try {
						subMonitor= new SubProgressMonitor(progressMonitor, 50);
						applyTextEdits(textFileBuffer, edits, subMonitor);
						subMonitor.done();
					} finally {
						postProcess(textFileBuffer, stateData);
						stopRewriteSession(textFileBuffer);
					}
				}
			} finally {
				progressMonitor.done();
			}
		}
	}
	
	private Object preProcess(ITextFileBuffer fileBuffer) {
		IDocument document= fileBuffer.getDocument();
		if (document instanceof IDocumentExtension)
			((IDocumentExtension) document).startSequentialRewrite(true);
		return TextUtilities.removeDocumentPartitioners(document);
	}
	
	private void postProcess(ITextFileBuffer fileBuffer, Object stateData) {
		IDocument document= fileBuffer.getDocument();
		if (stateData instanceof Map)
			TextUtilities.addDocumentPartitioners(document, (Map) stateData);			
		if (document instanceof IDocumentExtension)
			((IDocumentExtension) document).stopSequentialRewrite();
	}
	
	private void startRewriteSession(ITextFileBuffer fileBuffer) {
		IDocument document= fileBuffer.getDocument();
		if (document instanceof IDocumentExtension4) {
			fRewriteSessionId= new Object();
			((IDocumentExtension4) document).startRewriteSession(fRewriteSessionId);
		}
	}
	
	private void stopRewriteSession(ITextFileBuffer fileBuffer) {
		IDocument document= fileBuffer.getDocument();
		if (document instanceof IDocumentExtension4) {
			((IDocumentExtension4) document).stopRewriteSession(fRewriteSessionId);
			fRewriteSessionId= null;
		}
	}
	
	private void applyTextEdits(ITextFileBuffer fileBuffer, TextEdit[] textEdits, IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {
		progressMonitor.beginTask("applying changes", textEdits.length);
		try {
			
			IDocument document= fileBuffer.getDocument();
			for (int i= textEdits.length - 1; i >= 0; i--) {
				if (progressMonitor.isCanceled())
					throw new OperationCanceledException();
				TextEdit edit= textEdits[i];
				edit.apply(document, TextEdit.NONE);
				progressMonitor.worked(1);
			}
			
		} catch (BadLocationException x) {
			throw new CoreException(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IFileBufferStatusCodes.CONTENT_CHANGE_FAILED, "", x)); //$NON-NLS-1$
		} finally {
			progressMonitor.done();
		}
	}

}
