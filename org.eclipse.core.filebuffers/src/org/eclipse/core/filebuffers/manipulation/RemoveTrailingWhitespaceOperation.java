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

import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;
import org.eclipse.core.internal.filebuffers.Progress;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.filebuffers.IFileBufferStatusCodes;
import org.eclipse.core.filebuffers.ITextFileBuffer;

import org.eclipse.text.edits.DeleteEdit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * A text file buffer operations that removes all trailing whitespace.
 * <p>
 * Not yet for public use. API under construction.
 * 
 * @since 3.1
 */
public class RemoveTrailingWhitespaceOperation extends TextFileBufferOperation {
	
	/**
	 * Creates a remove trailing whitespace operation.
	 */
	public RemoveTrailingWhitespaceOperation() {
		super(Messages.getString("RemoveTrailingWhitespaceOperation.name")); //$NON-NLS-1$
	}
	
	/*
	 * @see org.eclipse.core.internal.filebuffers.textmanipulation.TextFileBufferOperation#computeTextEdit(org.eclipse.core.filebuffers.ITextFileBuffer, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected MultiTextEditWithProgress computeTextEdit(ITextFileBuffer fileBuffer, IProgressMonitor progressMonitor) throws CoreException {
		IDocument document= fileBuffer.getDocument();
		int lineCount= document.getNumberOfLines();
		
		progressMonitor= Progress.getMonitor(progressMonitor);
		progressMonitor.beginTask(Messages.getString("RemoveTrailingWhitespaceOperation.task.generatingChanges"), lineCount); //$NON-NLS-1$
		try {
			
			MultiTextEditWithProgress multiEdit= new MultiTextEditWithProgress(Messages.getString("RemoveTrailingWhitespaceOperation.task.applyingChanges")); //$NON-NLS-1$
			
			for (int i= 0; i < lineCount; i++) {
				if (progressMonitor.isCanceled())
					throw new OperationCanceledException();
				
				IRegion region= document.getLineInformation(i);
				if (region.getLength() == 0)
					continue;
				
				int lineStart= region.getOffset();
				int lineExclusiveEnd= lineStart + region.getLength();
				int j= lineExclusiveEnd -1;
				while (j >= lineStart && Character.isWhitespace(document.getChar(j))) --j;
				++j;
				if (j < lineExclusiveEnd)
					multiEdit.addChild(new DeleteEdit(j, lineExclusiveEnd - j));
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
