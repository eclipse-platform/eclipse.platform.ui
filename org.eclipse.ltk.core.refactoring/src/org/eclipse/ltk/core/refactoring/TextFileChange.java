/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import org.eclipse.text.edits.UndoEdit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.jface.text.IDocument;

import org.eclipse.ltk.internal.core.refactoring.Assert;
import org.eclipse.ltk.internal.core.refactoring.BufferValidationState;
import org.eclipse.ltk.internal.core.refactoring.Changes;

/**
 * A special {@link TextChange} that operates on <code>IFile</code>s.
 * 
 * @since 3.0 
 */
public class TextFileChange extends TextChange {
	
	/** Flag indicating that the file's save state has to be kept. This means an unsaved file is still
	 *  unsaved after performing the change and a saved one will be saved. */
	public static final int KEEP_SAVE_STATE= 1 << 0;
	/** Flag indicating that the file is to be saved after the change has been applied. */
	public static final int FORCE_SAVE= 1 << 1;
	/** Flag indicating that the file will not be saved after the change has been applied. */
	public static final int LEAVE_DIRTY= 1 << 2;
	
	
	// the file to change
	private IFile fFile;
	private int fSaveMode= KEEP_SAVE_STATE;
	
	// the mapped text buffer
	private int fAquireCount;
	private ITextFileBuffer fBuffer;
	
	private boolean fDirty;
	private BufferValidationState fValidationState;
	
	/**
	 * Creates a new <code>TextFileChange</code> for the given file.
	 * s
	 * @param name the change's name mainly used to render the change in the UI
	 * @param file the file this text change operates on
	 */
	public TextFileChange(String name, IFile file) {
		super(name);
		Assert.isNotNull(file);
		fFile= file;
	}
	
	/**
	 * Sets the save state. If set to <code>true</code> the change will save the
	 * content of the file back to disk.
	 * 
	 * @param save whether or not the changes should be saved to disk
	 */
	public void setSaveMode(int saveMode) {
		fSaveMode= saveMode;
	}
	
	/**
	 * Returns whether the change saves the changes back to disk.
	 * 
	 * @return <code>true</code> if the change saves the modified
	 *  content back to disk; otherwise <code>false</code> is
	 *  returned
	 */
	public int getSaveMode() {
		return fSaveMode;
	}
	
	/**
	 * Returns the <code>IFile</code> this change is working on.
	 * 
	 * @return the file this change is working on
	 */
	public IFile getFile() {
		return fFile;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getModifiedElement(){
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
		if (needsSaving()) {
			result.merge(Changes.validateModifiesFiles(new IFile[] {fFile}));
		}
		pm.worked(1);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		fValidationState.dispose();
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected IDocument aquireDocument(IProgressMonitor pm) throws CoreException {
		if (fAquireCount > 0)
			return fBuffer.getDocument();
		
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		IPath path= fFile.getFullPath();
		manager.connect(path, pm);
		fAquireCount++;
		fBuffer= manager.getTextFileBuffer(path);
		return fBuffer.getDocument();
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void commit(IDocument document, IProgressMonitor pm) throws CoreException {
		if (needsSaving()) {
			fBuffer.commit(pm, false);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void releaseDocument(IDocument document, IProgressMonitor pm) throws CoreException {
		Assert.isTrue(fAquireCount > 0);
		if (fAquireCount == 1) {
			ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
			manager.disconnect(fFile.getFullPath(), pm);
		}
		fAquireCount--;
 	}
	
	/**
	 * {@inheritDoc}
	 */
	protected Change createUndoChange(UndoEdit edit) throws CoreException {
		return new UndoTextFileChange(getName(), fFile, edit, fSaveMode);
	}
	
	private boolean needsSaving() {
		return (fSaveMode & FORCE_SAVE) != 0 || (!fDirty && (fSaveMode & KEEP_SAVE_STATE) != 0);
	}
}

