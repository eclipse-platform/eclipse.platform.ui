/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.examples;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;

public class ExampleRefactoring extends Refactoring {

	private IFile fFile;
	private String fOldText;
	private String fNewText;

	private Change fChange;

	public ExampleRefactoring(IFile file) {
		fFile= file;
		fChange= null;
		fOldText= null;
		fNewText= null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Refactoring#getName()
	 */
	public String getName() {
		return "Make replaces";
	}

	public void setNewText(String text) {
		fNewText= text;
	}

	public void setOldText(String text) {
		fOldText= text;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Refactoring#checkInitialConditions(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (fFile == null || !fFile.exists()) {
			return RefactoringStatus.createFatalErrorStatus("File does not exist");
		}
		return new RefactoringStatus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Refactoring#checkFinalConditions(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (fOldText == null || fOldText.length() == 0) {
			return RefactoringStatus.createFatalErrorStatus("Old text must be set and not empty");
		}
		if (fNewText == null || fNewText.length() == 0) {
			return RefactoringStatus.createFatalErrorStatus("New text must be set and not empty");
		}

		TextFileChange change= new TextFileChange(getName(), fFile);
		change.setEdit(new MultiTextEdit());

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(fFile.getFullPath(), LocationKind.IFILE, null);
		try {
			ITextFileBuffer textFileBuffer= manager.getTextFileBuffer(fFile.getFullPath(), LocationKind.IFILE);
			String content= textFileBuffer.getDocument().get();

			int i= 0;
			int count= 1;
			while (i < content.length()) {
				int offset= content.indexOf(fOldText, i);
				if (offset != -1) {
					ReplaceEdit replaceEdit= new ReplaceEdit(offset, fOldText.length(), fNewText);
					change.addEdit(replaceEdit);
					change.addTextEditGroup(new TextEditGroup("Change " + count++, replaceEdit));
					i= offset + fOldText.length();
				} else {
					break;
				}
			}
			if (count == 1) {
				fChange= new NullChange(getName());
				return RefactoringStatus.createErrorStatus("No matches found for '" + fOldText +"'");
			}
			fChange= change;

		} finally {
			manager.disconnect(fFile.getFullPath(), LocationKind.IFILE, null);
		}
		return new RefactoringStatus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Refactoring#createChange(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return fChange;
	}
}
