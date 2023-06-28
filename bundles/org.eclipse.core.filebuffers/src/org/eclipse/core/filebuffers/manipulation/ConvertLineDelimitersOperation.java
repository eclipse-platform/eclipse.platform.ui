/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *     Joerg Kubitz    - rewrite implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers.manipulation;

import java.util.Objects;

import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.filebuffers.IFileBufferStatusCodes;
import org.eclipse.core.filebuffers.ITextFileBuffer;

import org.eclipse.text.edits.ReplaceEdit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;

/**
 * A text file buffer operation that changes the line delimiters to a specified line delimiter.
 *
 * @since 3.1
 */
public class ConvertLineDelimitersOperation extends TextFileBufferOperation {

	private String fLineDelimiter;

	/**
	 * Creates a new line delimiter conversion operation for the given target delimiter.
	 *
	 * @param lineDelimiter the target line delimiter
	 */
	public ConvertLineDelimitersOperation(String lineDelimiter) {
		super(FileBuffersMessages.ConvertLineDelimitersOperation_name);
		fLineDelimiter= lineDelimiter;
	}

	@Override
	protected MultiTextEditWithProgress computeTextEdit(ITextFileBuffer fileBuffer, IProgressMonitor progressMonitor) throws CoreException {
		IDocument document= fileBuffer.getDocument();
		int lineCount= document.getNumberOfLines();
		try {
			String original= document.get();
			int newLengthGuess= original.length() + (fLineDelimiter.length() - 1) * lineCount;
			StringBuilder sb= new StringBuilder(newLengthGuess);
			for (int i= 0; i < lineCount; i++) {
				int offset= document.getLineOffset(i);
				int length= document.getLineLength(i);
				String delim= document.getLineDelimiter(i);
				sb.append(original, offset, offset + length - (delim == null ? 0 : delim.length()));
				if (delim != null) {
					sb.append(fLineDelimiter);
				}
			}
			String replaced= sb.toString();
			MultiTextEditWithProgress multiEdit= new MultiTextEditWithProgress(FileBuffersMessages.ConvertLineDelimitersOperation_task_applyingChanges);
			multiEdit.addChild(new ReplaceEdit(0, document.getLength(), replaced));
			return Objects.equals(replaced, original) ? null : multiEdit;
		} catch (BadLocationException | IndexOutOfBoundsException x) {
			throw new CoreException(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IFileBufferStatusCodes.CONTENT_CHANGE_FAILED, "", x)); //$NON-NLS-1$
		}
	}

	@Override
	protected DocumentRewriteSessionType getDocumentRewriteSessionType() {
		return DocumentRewriteSessionType.SEQUENTIAL;
	}
}
