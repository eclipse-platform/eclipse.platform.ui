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
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.jface.text.IDocument;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Helper class for text file changes.
 */
public class TextChanges {
	
	private TextChanges() {
		// no instance
	}
	
	public static IDocument getDocument(IFile file) throws CoreException {
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		IPath path= file.getFullPath();
		ITextFileBuffer buffer= manager.getTextFileBuffer(path);
		if (buffer == null)
			return null;
		return buffer.getDocument();
		
	}

	public static RefactoringStatus isValid(IFile file, boolean existed, long lastModificationStamp, boolean fSave) throws CoreException {
		// the file did not exist anymore when initializing the
		// validation state. In this case we must ensure that it
		// still doesn't exist.
		if (!existed) {
			if (file.exists())
				return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.getFormattedString(
					"TextChanges.error.existing", //$NON-NLS-1$
					file.getFullPath().toString()
					));
		} else {
			if (!file.exists())
				return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.getFormattedString(
					"TextChanges.error.not_existing", //$NON-NLS-1$
					file.getFullPath().toString()
					));
			if (lastModificationStamp != file.getModificationStamp())
				return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.getFormattedString(
					"TextChanges.error.content_changed", //$NON-NLS-1$
					file.getFullPath().toString()
					)); 
			if (file.isReadOnly())
				return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.getFormattedString(
					"TextChanges.error.read_only", //$NON-NLS-1$
					file.getFullPath().toString()
					));
			if (!file.isSynchronized(IResource.DEPTH_ZERO)) 
				return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.getFormattedString(
					"TextChanges.error.outOfSync", //$NON-NLS-1$
					file.getFullPath().toString()
					));
			
			if (fSave) {
				ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
				// Don't connect. We want to check if the file is under modification right now
				ITextFileBuffer buffer= manager.getTextFileBuffer(file.getFullPath());
				if (buffer != null && buffer.isDirty()) {
					return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.getFormattedString(
						"TextChanges.error.unsaved_changes", //$NON-NLS-1$
						file.getFullPath().toString()
						)); 
				}
			}
		}
		return new RefactoringStatus();
	}
	
	public static RefactoringStatus isValid(IDocument document, int length) throws CoreException {
		RefactoringStatus result= new RefactoringStatus();
		if (length != document.getLength()) {
			result.addFatalError(RefactoringCoreMessages.getString("TextChanges.error.document_content_changed")); //$NON-NLS-1$
		}
		return result;
	}
	
}
