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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Represents an undo-able text change consisting of several individual
 * changes.
 * 
 * @see org.eclipse.text.undo.UndoableTextChange
 * @see org.eclipse.text.undo.DocumentUndoManager
 * @since 3.2
 */
class UndoableCompoundTextChange extends UndoableTextChange {

	/** The list of individual changes */
	private List fChanges= new ArrayList();

	/**
	 * Creates a new compound text change.
	 * 
	 * @param manager
	 *            the undo manager for this change
	 */
	UndoableCompoundTextChange(DocumentUndoManager manager) {
		super(manager);
	}

	/**
	 * Adds a new individual change to this compound change.
	 * 
	 * @param change the change to be added
	 */
	protected void add(UndoableTextChange change) {
		fChanges.add(change);
	}

	/*
	 * @see org.eclipse.jface.text.UndoableTextChange#undo()
	 */
	public IStatus undo(IProgressMonitor monitor, IAdaptable uiInfo) {

		int size= fChanges.size();
		if (size > 0) {
			UndoableTextChange c;

			c= (UndoableTextChange) fChanges.get(0);
			manager.fireDocumentUndo(c.fStart, c.fPreservedText, c.fText, uiInfo,
					DocumentUndoEvent.ABOUT_TO_UNDO, true);

			for (int i= size - 1; i >= 0; --i) {
				c= (UndoableTextChange) fChanges.get(i);
				c.undoTextChange();
			}
			manager.fireDocumentUndo(c.fStart, c.fPreservedText, c.fText, uiInfo,
					DocumentUndoEvent.UNDONE, true);
		}
		return Status.OK_STATUS;
	}

	/*
	 * @see org.eclipse.jface.text.UndoableTextChange#redo()
	 */
	public IStatus redo(IProgressMonitor monitor, IAdaptable uiInfo) {

		int size= fChanges.size();
		if (size > 0) {

			UndoableTextChange c;
			c= (UndoableTextChange) fChanges.get(size - 1);
			manager.fireDocumentUndo(c.fStart, c.fText, c.fPreservedText, uiInfo,
					DocumentUndoEvent.ABOUT_TO_REDO, true);

			for (int i= 0; i <= size - 1; ++i) {
				c= (UndoableTextChange) fChanges.get(i);
				c.redoTextChange();
			}
			manager.fireDocumentUndo(c.fStart, c.fText, c.fPreservedText, uiInfo,
					DocumentUndoEvent.REDONE, true);
		}

		return Status.OK_STATUS;
	}

	/*
	 * @see UndoableTextChange#updateUndoableTextChange
	 */
	protected void updateTextChange() {
		// first gather the data from the buffers
		super.updateTextChange();

		// the result of the update is stored as a child change
		UndoableTextChange c= new UndoableTextChange(manager);
		c.fStart= fStart;
		c.fEnd= fEnd;
		c.fText= fText;
		c.fPreservedText= fPreservedText;
		c.fUndoModificationStamp= fUndoModificationStamp;
		c.fRedoModificationStamp= fRedoModificationStamp;
		add(c);

		// clear out all indexes now that the child is added
		reinitialize();
	}

	/*
	 * @see UndoableTextChange#createCurrent
	 */
	protected UndoableTextChange createCurrent() {

		if (!manager.fFoldingIntoCompoundChange)
			return new UndoableTextChange(manager);

		reinitialize();
		return this;
	}

	/*
	 * @see org.eclipse.jface.text.UndoableTextChange#commit()
	 */
	protected void commit() {
		// if there is pending data, update the text change
		if (fStart > -1)
			updateTextChange();
		manager.fCurrent= createCurrent();
	}

	/**
	 * Checks whether the text change is valid for undo or redo.
	 * 
	 * @return true if the text change is valid
	 */
	protected boolean isValid() {
		return fStart > -1 || fChanges.size() > 0;
	}

	/**
	 * Returns the undo modification stamp.
	 * 
	 * @return the undo modification stamp
	 */
	protected long getUndoModificationStamp() {
		if (fStart > -1)
			return super.getUndoModificationStamp();
		else if (fChanges.size() > 0)
			return ((UndoableTextChange) fChanges.get(0))
					.getUndoModificationStamp();

		return fUndoModificationStamp;
	}

	/**
	 * Returns the redo modification stamp.
	 * 
	 * @return the redo modification stamp
	 */
	protected long getRedoModificationStamp() {
		if (fStart > -1)
			return super.getRedoModificationStamp();
		else if (fChanges.size() > 0)
			return ((UndoableTextChange) fChanges.get(fChanges.size() - 1))
					.getRedoModificationStamp();

		return fRedoModificationStamp;
	}
}
