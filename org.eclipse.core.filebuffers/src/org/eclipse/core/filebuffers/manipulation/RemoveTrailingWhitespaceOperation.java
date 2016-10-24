/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.core.filebuffers.IFileBufferStatusCodes;
import org.eclipse.core.filebuffers.ITextFileBuffer;

import org.eclipse.text.edits.DeleteEdit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * A text file buffer operation that removes all trailing whitespace.
 *
 * @since 3.1
 */
public class RemoveTrailingWhitespaceOperation extends TextFileBufferOperation {

	/**
	 * Creates a remove trailing whitespace operation.
	 */
	public RemoveTrailingWhitespaceOperation() {
		super(FileBuffersMessages.RemoveTrailingWhitespaceOperation_name);
	}

	@Override
	protected MultiTextEditWithProgress computeTextEdit(ITextFileBuffer fileBuffer, IProgressMonitor progressMonitor) throws CoreException {
		IDocument document= fileBuffer.getDocument();
		int lineCount= document.getNumberOfLines();

		SubMonitor subMonitor= SubMonitor.convert(progressMonitor, FileBuffersMessages.RemoveTrailingWhitespaceOperation_task_generatingChanges, lineCount);
		try {

			MultiTextEditWithProgress multiEdit= new MultiTextEditWithProgress(FileBuffersMessages.RemoveTrailingWhitespaceOperation_task_applyingChanges);

			for (int i= 0; i < lineCount; i++) {
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
				subMonitor.split(1);
			}

			return multiEdit.getChildrenSize() <= 0 ? null : multiEdit;

		} catch (BadLocationException x) {
			throw new CoreException(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IFileBufferStatusCodes.CONTENT_CHANGE_FAILED, "", x)); //$NON-NLS-1$
		}
	}

	@Override
	protected DocumentRewriteSessionType getDocumentRewriteSessionType() {
		return DocumentRewriteSessionType.SEQUENTIAL;
	}
}
