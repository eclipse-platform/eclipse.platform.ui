/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.core.resources.IFile;

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
		ITextFileBuffer buffer= manager.getTextFileBuffer(path, LocationKind.IFILE);
		if (buffer == null)
			return null;
		return buffer.getDocument();
		
	}
	
	public static RefactoringStatus isValid(IDocument document, int length) throws CoreException {
		RefactoringStatus result= new RefactoringStatus();
		if (length != document.getLength()) {
			result.addFatalError(RefactoringCoreMessages.TextChanges_error_document_content_changed); 
		}
		return result;
	}
}
