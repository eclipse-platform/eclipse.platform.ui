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
package org.eclipse.ltk.core.refactoring;

import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import org.eclipse.ltk.internal.core.refactoring.Assert;
import org.eclipse.ltk.internal.core.refactoring.BufferValidationState;
import org.eclipse.ltk.internal.core.refactoring.Changes;

/**
 * A change to perform the reverse change of a {@link TextFileChange}.
 * <p>
 * This class is not intended to be instantiated by clients. It is
 * usually created by a <code>TextFileChange</code> object.
 * </p>
 * 
 * @since 3.0
 */
public class UndoTextFileChange extends Change {
	
	private String fName;
	private UndoEdit fUndo;
	private IFile fFile;
	private int fSaveMode;
	
	private boolean fDirty;
	private BufferValidationState fValidationState;
	
	protected UndoTextFileChange(String name, IFile file, UndoEdit undo, int saveMode) {
		Assert.isNotNull(name);
		Assert.isNotNull(undo);
		fName= name;
		fFile= file;
		fUndo= undo;
		fSaveMode= saveMode;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return fName;
	}
	
	public int getSaveMode() {
		return fSaveMode;
	}
	
	/**
	 * Hook to create an undo change for the given undo edit. This hook 
	 * gets called while performing the change to construct the corresponding 
	 * undo change object.
	 * <p>
	 * Subclasses may override it to create a different undo change.
	 * </p>
	 * 
	 * @param edit the {@link UndoEdit undo edit} to create a undo change for
	 * 
	 * @return the undo change
	 * 
	 * @throws CoreException if an undo change can't be created
	 */
	protected Change createUndoChange(UndoEdit edit) throws CoreException {
		return new UndoTextFileChange(getName(), fFile, edit, fSaveMode);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object getModifiedElement() {
		return fFile;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void initializeValidationData(IProgressMonitor pm) {
		pm.beginTask("", 1); //$NON-NLS-1$
		fValidationState= BufferValidationState.create(fFile);
		ITextFileBuffer buffer= FileBuffers.getTextFileBufferManager().getTextFileBuffer(fFile.getFullPath());
		fDirty= buffer != null && buffer.isDirty();
		pm.worked(1);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
		pm.beginTask("", 1); //$NON-NLS-1$
		RefactoringStatus result= fValidationState.isValid();
		pm.worked(1);
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		pm.beginTask("", 2); //$NON-NLS-1$
		ITextFileBuffer buffer= null;
		try {
			manager.connect(fFile.getFullPath(), new SubProgressMonitor(pm, 1));
			buffer= manager.getTextFileBuffer(fFile.getFullPath());
			IDocument document= buffer.getDocument();
			UndoEdit redo= fUndo.apply(document, TextEdit.CREATE_UNDO);
			if (needsSaving())
				buffer.commit(pm, false);
			return createUndoChange(redo);
		} catch (BadLocationException e) {
			throw Changes.asCoreException(e);
		} finally {
			if (buffer != null)
				manager.disconnect(fFile.getFullPath(), new SubProgressMonitor(pm, 1));
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		fValidationState.dispose();
	}
	
	private boolean needsSaving() {
		return (fSaveMode & TextFileChange.FORCE_SAVE) != 0 || (!fDirty && (fSaveMode & TextFileChange.KEEP_SAVE_STATE) != 0);
	}
}