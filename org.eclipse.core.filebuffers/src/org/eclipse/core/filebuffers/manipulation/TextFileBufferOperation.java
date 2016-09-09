/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers.manipulation;

import java.util.Map;

import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferStatusCodes;
import org.eclipse.core.filebuffers.ITextFileBuffer;

import org.eclipse.text.edits.TextEdit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextUtilities;

/**
 * Abstraction for a file buffer operation that works on text file buffers.
 * Subclasses have to override the <code>computeTextEdits</code> method in
 * order to provide a sequence of {@link org.eclipse.text.edits.TextEdit}
 * objects.
 *
 * @since 3.1
 */
public abstract class TextFileBufferOperation implements IFileBufferOperation {


	/**
	 * Computes and returns a text edit. Subclasses have to provide that method.
	 *
	 * @param textFileBuffer the text file buffer to manipulate
	 * @param progressMonitor the progress monitor
	 * @return the text edits describing the content manipulation
	 * @throws CoreException in case the computation failed
	 * @throws OperationCanceledException in case the progress monitor has been set to canceled
	 */
	protected abstract MultiTextEditWithProgress computeTextEdit(ITextFileBuffer textFileBuffer, IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException;

	/**
	 * Returns the rewrite session type that corresponds to the text edit sequence.
	 *
	 * @return the rewrite session type
	 */
	protected abstract DocumentRewriteSessionType getDocumentRewriteSessionType();


	private String fOperationName;
	private DocumentRewriteSession fDocumentRewriteSession;

	/**
	 * Creates a new operation with the given name.
	 *
	 * @param operationName the name of the operation
	 */
	protected TextFileBufferOperation(String operationName) {
		fOperationName= operationName;
	}

	@Override
	public String getOperationName() {
		return fOperationName;
	}

	@Override
	public void run(IFileBuffer fileBuffer, IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {

		if (fileBuffer instanceof ITextFileBuffer) {
			ITextFileBuffer textFileBuffer= (ITextFileBuffer) fileBuffer;
			IPath path= textFileBuffer.getLocation();
			String taskName= path == null ? getOperationName() : path.lastSegment();
			SubMonitor subMonitor= SubMonitor.convert(progressMonitor, taskName, 100);
			MultiTextEditWithProgress edit= computeTextEdit(textFileBuffer, subMonitor.split(10));
			if (edit != null) {
				Map<String, IDocumentPartitioner> stateData= startRewriteSession(textFileBuffer);
				try {
					applyTextEdit(textFileBuffer, edit, subMonitor.split(90));
				} finally {
					stopRewriteSession(textFileBuffer, stateData);
				}
			}
		}
	}

	private Map<String, IDocumentPartitioner> startRewriteSession(ITextFileBuffer fileBuffer) {
		Map<String, IDocumentPartitioner> stateData= null;

		IDocument document= fileBuffer.getDocument();
		if (document instanceof IDocumentExtension4) {
			IDocumentExtension4 extension= (IDocumentExtension4) document;
			fDocumentRewriteSession= extension.startRewriteSession(getDocumentRewriteSessionType());
		} else
			stateData= TextUtilities.removeDocumentPartitioners(document);

		return stateData;
	}

	private void stopRewriteSession(ITextFileBuffer fileBuffer, Map<String, IDocumentPartitioner> stateData) {
		IDocument document= fileBuffer.getDocument();
		if (document instanceof IDocumentExtension4) {
			IDocumentExtension4 extension= (IDocumentExtension4) document;
			extension.stopRewriteSession(fDocumentRewriteSession);
			fDocumentRewriteSession= null;
		} else if (stateData != null)
			TextUtilities.addDocumentPartitioners(document, stateData);
	}

	private void applyTextEdit(ITextFileBuffer fileBuffer, MultiTextEditWithProgress textEdit, IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {
		try {
			textEdit.apply(fileBuffer.getDocument(), TextEdit.NONE, progressMonitor);
		} catch (BadLocationException x) {
			throw new CoreException(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IFileBufferStatusCodes.CONTENT_CHANGE_FAILED, "", x)); //$NON-NLS-1$
		}
	}
}
