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
import org.eclipse.ltk.internal.core.refactoring.ContentStamps;

/**
 * A special {@link TextChange} that operates on a <code>IFile</code>.
 * <p>
 * The class should be subclassed by clients which need to perform 
 * special operation when acquiring or releasing a document. 
 * </p>
 * @since 3.0 
 */
public class TextFileChange extends TextChange {
	
	/** 
	 * Flag (value 1) indicating that the file's save state has to be kept. This means an 
	 * unsaved file is still unsaved after performing the change and a saved one 
	 * will be saved. 
	 */
	public static final int KEEP_SAVE_STATE= 1 << 0;
	
	/**
	 * Flag (value 2) indicating that the file is to be saved after the change has been applied.
	 */
	public static final int FORCE_SAVE= 1 << 1;
	
	/**
	 * Flag (value 4) indicating that the file will not be saved after the change has been applied.
	 */
	public static final int LEAVE_DIRTY= 1 << 2;
	
	
	// the file to change
	private IFile fFile;
	private int fSaveMode= KEEP_SAVE_STATE;
	
	// the mapped text buffer
	private int fAquireCount;
	private ITextFileBuffer fBuffer;
	
	private boolean fDirty;
	private BufferValidationState fValidationState;
	private ContentStamp fContentStamp;
	
	/**
	 * Creates a new <code>TextFileChange</code> for the given file.
	 * 
	 * @param name the change's name mainly used to render the change in the UI
	 * @param file the file this text change operates on
	 */
	public TextFileChange(String name, IFile file) {
		super(name);
		Assert.isNotNull(file);
		fFile= file;
	}
	
	/**
	 * Sets the save state. Must be one of <code>KEEP_SAVE_STATE</code>,
	 * <code>FORCE_SAVE</code> or <code>LEAVE_DIRTY</code>.
	 * 
	 * @param saveMode indicating how save is handled when the document
	 *  gets committed
	 */
	public void setSaveMode(int saveMode) {
		fSaveMode= saveMode;
	}
	
	/**
	 * Returns the save state set via {@link #setSaveMode(int)}.
	 * 
	 * @return the save state
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
	 * Hook to create an undo change for the given undo edit and content stamp. 
	 * This hook gets called while performing the change to construct the 
	 * corresponding undo change object.
	 * 
	 * @param edit the {@link UndoEdit} to create an undo change for
	 * @param stampToRestore the content stamp to restore when the undo
	 *  edit is executed.
	 * 
	 * @return the undo change or <code>null</code> if no undo change can
	 *  be created. Returning <code>null</code> results in the fact that
	 *  the whole change tree can't be undone. So returning <code>null</code>
	 *  is only recommended if an exception occurred during creating the
	 *  undo change.
	 */
	protected Change createUndoChange(UndoEdit edit, ContentStamp stampToRestore) {
		return new UndoTextFileChange(getName(), fFile, edit, stampToRestore, fSaveMode);
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
	protected IDocument acquireDocument(IProgressMonitor pm) throws CoreException {
		if (fAquireCount > 0)
			return fBuffer.getDocument();
		
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		IPath path= fFile.getFullPath();
		manager.connect(path, pm);
		fAquireCount++;
		fBuffer= manager.getTextFileBuffer(path);
		fContentStamp= ContentStamps.get(fFile, true);
		return fBuffer.getDocument();
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void commit(IDocument document, IProgressMonitor pm) throws CoreException {
		if (needsSaving()) {
			fBuffer.commit(pm, false);
			ContentStamps.increment(fFile);
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
	protected final Change createUndoChange(UndoEdit edit) {
		return createUndoChange(edit, fContentStamp);
	}
	
	private boolean needsSaving() {
		return (fSaveMode & FORCE_SAVE) != 0 || (!fDirty && (fSaveMode & KEEP_SAVE_STATE) != 0);
	}
}

