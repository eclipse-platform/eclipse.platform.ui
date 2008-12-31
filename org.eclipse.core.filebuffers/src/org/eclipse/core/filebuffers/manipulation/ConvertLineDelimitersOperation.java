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
package org.eclipse.core.filebuffers.manipulation;

import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;
import org.eclipse.core.internal.filebuffers.Progress;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.filebuffers.IFileBufferStatusCodes;
import org.eclipse.core.filebuffers.ITextFileBuffer;

import org.eclipse.text.edits.ReplaceEdit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * A text file buffer operation that changes the line delimiters to a specified
 * line delimiter.
 *
 * @since 3.1
 */
public class ConvertLineDelimitersOperation extends TextFileBufferOperation {

	private String fLineDelimiter;

	/**
	 * Creates a new line delimiter conversion operation for the given target
	 * delimiter.
	 *
	 * @param lineDelimiter the target line delimiter
	 */
	public ConvertLineDelimitersOperation(String lineDelimiter) {
		super(FileBuffersMessages.ConvertLineDelimitersOperation_name);
		fLineDelimiter= lineDelimiter;
	}

	/*
	 * @see org.eclipse.core.internal.filebuffers.textmanipulation.TextFileBufferOperation#computeTextEdit(org.eclipse.core.filebuffers.ITextFileBuffer, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected MultiTextEditWithProgress computeTextEdit(ITextFileBuffer fileBuffer, IProgressMonitor progressMonitor) throws CoreException {
		IDocument document= fileBuffer.getDocument();
		int lineCount= document.getNumberOfLines();

		progressMonitor= Progress.getMonitor(progressMonitor);
		progressMonitor.beginTask(FileBuffersMessages.ConvertLineDelimitersOperation_task_generatingChanges, lineCount);
		try {

			MultiTextEditWithProgress multiEdit= new MultiTextEditWithProgress(FileBuffersMessages.ConvertLineDelimitersOperation_task_applyingChanges);

			for (int i= 0; i < lineCount; i++) {
				if (progressMonitor.isCanceled())
					throw new OperationCanceledException();

				final String delimiter= document.getLineDelimiter(i);
				if (delimiter != null && delimiter.length() > 0 && !delimiter.equals(fLineDelimiter)) {
					IRegion region= document.getLineInformation(i);
					multiEdit.addChild(new ReplaceEdit(region.getOffset() + region.getLength(), delimiter.length(), fLineDelimiter));
				}
				progressMonitor.worked(1);
			}

			return multiEdit.getChildrenSize() <= 0 ? null : multiEdit;

		} catch (BadLocationException x) {
			throw new CoreException(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IFileBufferStatusCodes.CONTENT_CHANGE_FAILED, "", x)); //$NON-NLS-1$
		} finally {
			progressMonitor.done();
		}
	}

	/*
	 * @see org.eclipse.core.filebuffers.manipulation.TextFileBufferOperation#getDocumentRewriteSessionType()
	 */
	protected DocumentRewriteSessionType getDocumentRewriteSessionType() {
		return DocumentRewriteSessionType.SEQUENTIAL;
	}
}
