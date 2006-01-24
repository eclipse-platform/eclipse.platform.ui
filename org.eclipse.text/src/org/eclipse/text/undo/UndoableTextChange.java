/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.undo;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocumentExtension4;

/**
 * Represents an undo-able text change, described as the
 * replacement of some preserved text with new text.  
 * <p>
 * Based on the DefaultUndoManager.TextCommand from R3.1.
 * </p>
 * 
 * @see org.eclipse.text.undo.DocumentUndoManager
 * @since 3.2
 */
class UndoableTextChange extends AbstractOperation {

	/** The start index of the replaced text. */
	protected int fStart= -1;

	/** The end index of the replaced text. */
	protected int fEnd= -1;

	/** The newly inserted text. */
	protected String fText;

	/** The replaced text. */
	protected String fPreservedText;

	/** The undo modification stamp. */
	protected long fUndoModificationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;

	/** The redo modification stamp. */
	protected long fRedoModificationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;

	/** The undo manager that generated the change. */
	protected DocumentUndoManager manager;

	/**
	 * Creates a new text change.
	 * 
	 * @param manager the undo manager for this change
	 */
	UndoableTextChange(DocumentUndoManager manager) {
		super(UndoMessages.getString("DocumentUndoManager.operationLabel")); //$NON-NLS-1$
		this.manager= manager;
		addContext(manager.getUndoContext());
	}

	/**
	 * Re-initializes this text change.
	 */
	protected void reinitialize() {
		fStart= fEnd= -1;
		fText= fPreservedText= null;
		fUndoModificationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
		fRedoModificationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
	}

	/**
	 * Sets the start and the end index of this change.
	 * 
	 * @param start the start index
	 * @param end the end index
	 */
	protected void set(int start, int end) {
		fStart= start;
		fEnd= end;
		fText= null;
		fPreservedText= null;
	}

	/*
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#dispose()
	 */
	public void dispose() {
		reinitialize();
	}

	/**
	 * Undo the change described by this change.
	 */
	protected void undoTextChange() {
		try {
			if (manager.fDocument instanceof IDocumentExtension4)
				((IDocumentExtension4) manager.fDocument).replace(fStart, fText
						.length(), fPreservedText, fUndoModificationStamp);
			else
				manager.fDocument.replace(fStart, fText.length(),
						fPreservedText);
		} catch (BadLocationException x) {
		}
	}

	/*
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#canUndo()
	 */
	public boolean canUndo() {
		if (isValid()) {
			if (manager.fDocument instanceof IDocumentExtension4) {
				long docStamp= ((IDocumentExtension4) manager.fDocument)
						.getModificationStamp();

				// Normal case: an undo is valid if its redo will restore
				// document to its current modification stamp
				boolean canUndo= docStamp == IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP
						|| docStamp == getRedoModificationStamp();

				/*
				 * Special case to check if the answer is false. If the last
				 * document change was empty, then the document's modification
				 * stamp was incremented but nothing was committed. The
				 * operation being queried has an older stamp. In this case
				 * only, the comparison is different. A sequence of document
				 * changes that include an empty change is handled correctly
				 * when a valid commit follows the empty change, but when
				 * #canUndo() is queried just after an empty change, we must
				 * special case the check. The check is very specific to prevent
				 * false positives. see
				 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=98245
				 */
				if (!canUndo
						&& this == manager.fHistory
								.getUndoOperation(manager.fUndoContext) 
							// this is the latest operation
						&& this != manager.fCurrent 
							// there is a more current operation not on the stack
						&& !manager.fCurrent.isValid() 
						// the current operation is not a valid document
						// modification
						&& manager.fCurrent.fUndoModificationStamp != 
						// the invalid current operation has a document stamp
						IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP) {
					canUndo= manager.fCurrent.fRedoModificationStamp == docStamp;
				}
				/*
				 * When the composite is the current operation, it may hold the
				 * timestamp of a no-op change. We check this here rather than
				 * in an override of canUndo() in UndoableCompoundTextChange simply to
				 * keep all the special case checks in one place.
				 */
				if (!canUndo
						&& this == manager.fHistory
								.getUndoOperation(manager.fUndoContext)
						&& // this is the latest operation
						this instanceof UndoableCompoundTextChange
						&& this == manager.fCurrent
						&& // this is the current operation
						this.fStart == -1
						&& // the current operation text is not valid
						manager.fCurrent.fRedoModificationStamp != IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP) { 
						// but it has a redo stamp
					canUndo= manager.fCurrent.fRedoModificationStamp == docStamp;
				}

			}
			// if there is no timestamp to check, simply return true per the
			// 3.0.1 behavior
			return true;
		}
		return false;
	}

	/*
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#canRedo()
	 */
	public boolean canRedo() {
		if (isValid()) {
			if (manager.fDocument instanceof IDocumentExtension4) {
				long docStamp= ((IDocumentExtension4) manager.fDocument)
						.getModificationStamp();
				return docStamp == IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP
						|| docStamp == getUndoModificationStamp();
			}
			// if there is no timestamp to check, simply return true per the
			// 3.0.1 behavior
			return true;
		}
		return false;
	}

	/*
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#canExecute()
	 */
	public boolean canExecute() {
		return manager.isConnected();
	}

	/*
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#execute(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus execute(IProgressMonitor monitor, IAdaptable uiInfo) {
		// Text changes execute as they are typed, so executing one has no
		// effect.
		return Status.OK_STATUS;
	}

	/**
	 * {@inheritDoc}
	 * Notifies clients about the undo.
	 */
	public IStatus undo(IProgressMonitor monitor, IAdaptable uiInfo) {
		if (isValid()) {
			manager.fireDocumentUndo(fStart, fPreservedText, fText, uiInfo, DocumentUndoEvent.ABOUT_TO_UNDO, false);
			undoTextChange();
			manager.fireDocumentUndo(fStart, fPreservedText, fText, uiInfo, DocumentUndoEvent.UNDONE, false);
			return Status.OK_STATUS;
		}
		return IOperationHistory.OPERATION_INVALID_STATUS;
	}

	/**
	 * Re-applies the change described by this change.
	 */
	protected void redoTextChange() {
		try {
			if (manager.fDocument instanceof IDocumentExtension4)
				((IDocumentExtension4) manager.fDocument).replace(fStart, fEnd
						- fStart, fText, fRedoModificationStamp);
			else
				manager.fDocument.replace(fStart, fEnd - fStart, fText);
		} catch (BadLocationException x) {
		}
	}

	/**
	 * Re-applies the change described by this change that was previously
	 * undone. Also notifies clients about the redo.
	 * 
	 * @param monitor the progress monitor to use if necessary
	 * @param uiInfo an adaptable that can provide UI info if needed
	 * @return the status
	 */
	public IStatus redo(IProgressMonitor monitor, IAdaptable uiInfo) {
		if (isValid()) {
			manager.fireDocumentUndo(fStart, fText, fPreservedText, uiInfo,
					DocumentUndoEvent.ABOUT_TO_REDO, false);
			redoTextChange();
			manager.fireDocumentUndo(fStart, fText, fPreservedText, uiInfo,
					DocumentUndoEvent.REDONE, false);
			return Status.OK_STATUS;
		}
		return IOperationHistory.OPERATION_INVALID_STATUS;
	}

	/**
	 * Update the change in response to a commit.
	 */

	protected void updateTextChange() {
		fText= manager.fTextBuffer.toString();
		manager.fTextBuffer.setLength(0);
		fPreservedText= manager.fPreservedTextBuffer.toString();
		manager.fPreservedTextBuffer.setLength(0);
	}

	/**
	 * Creates a new uncommitted text change depending on whether a compound
	 * change is currently being executed.
	 * 
	 * @return a new, uncommitted text change or a compound text change
	 */
	protected UndoableTextChange createCurrent() {
		return manager.fFoldingIntoCompoundChange ? new UndoableCompoundTextChange(
				manager) : new UndoableTextChange(manager);
	}

	/**
	 * Commits the current change into this one.
	 */
	protected void commit() {

		if (fStart < 0) {
			if (manager.fFoldingIntoCompoundChange) {
				manager.fCurrent= createCurrent();
			} else {
				reinitialize();
			}
		} else {
			updateTextChange();
			manager.fCurrent= createCurrent();
		}
	}

	/**
	 * Updates the text from the buffers without resetting the buffers or adding
	 * anything to the stack.
	 */
	protected void pretendCommit() {
		if (fStart > -1) {
			fText= manager.fTextBuffer.toString();
			fPreservedText= manager.fPreservedTextBuffer.toString();
		}
	}

	/**
	 * Attempt a commit of this change and answer true if a new fCurrent was
	 * created as a result of the commit.
	 * 
	 * @return <code>true</code> if the change was committed and created
	 * 			a new fCurrent, <code>false</code> if not
	 */
	protected boolean attemptCommit() {
		pretendCommit();
		if (isValid()) {
			manager.commit();
			return true;
		}
		return false;
	}

	/**
	 * Checks whether this text change is valid for undo or redo.
	 * 
	 * @return <code>true</code> if the change is valid for undo or redo
	 */
	protected boolean isValid() {
		return fStart > -1 && fEnd > -1 && fText != null;
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String delimiter= ", "; //$NON-NLS-1$
		StringBuffer text= new StringBuffer(super.toString());
		text.append("\n"); //$NON-NLS-1$
		text.append(this.getClass().getName());
		text.append(" undo modification stamp: "); //$NON-NLS-1$
		text.append(fUndoModificationStamp);
		text.append(" redo modification stamp: "); //$NON-NLS-1$
		text.append(fRedoModificationStamp);
		text.append(" start: "); //$NON-NLS-1$
		text.append(fStart);
		text.append(delimiter);
		text.append("end: "); //$NON-NLS-1$
		text.append(fEnd);
		text.append(delimiter);
		text.append("text: '"); //$NON-NLS-1$
		text.append(fText);
		text.append('\'');
		text.append(delimiter);
		text.append("preservedText: '"); //$NON-NLS-1$
		text.append(fPreservedText);
		text.append('\'');
		return text.toString();
	}

	/**
	 * Return the undo modification stamp
	 * 
	 * @return the undo modification stamp for this change
	 */
	protected long getUndoModificationStamp() {
		return fUndoModificationStamp;
	}

	/**
	 * Return the redo modification stamp
	 * 
	 * @return the redo modification stamp for this change
	 */
	protected long getRedoModificationStamp() {
		return fRedoModificationStamp;
	}
}
