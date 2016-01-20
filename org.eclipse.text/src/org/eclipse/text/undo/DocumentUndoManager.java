/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IContextReplacingOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.TextUtilities;

/**
 * A standard implementation of a document-based undo manager that
 * creates an undo history based on changes to its document.
 * <p>
 * Based on the 3.1 implementation of DefaultUndoManager, it was implemented
 * using the document-related manipulations defined in the original
 * DefaultUndoManager, by separating the document manipulations from the
 * viewer-specific processing.</p>
 * <p>
 * The classes representing individual text edits (formerly text commands)
 * were promoted from inner types to their own classes in order to support
 * reassignment to a different undo manager.<p>
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 *
 * @see IDocumentUndoManager
 * @see DocumentUndoManagerRegistry
 * @see IDocumentUndoListener
 * @see org.eclipse.jface.text.IDocument
 * @since 3.2
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DocumentUndoManager implements IDocumentUndoManager {


	/**
	 * Represents an undo-able text change, described as the
	 * replacement of some preserved text with new text.
	 * <p>
	 * Based on the DefaultUndoManager.TextCommand from R3.1.
	 * </p>
	 */
	private static class UndoableTextChange extends AbstractOperation {

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
		protected DocumentUndoManager fDocumentUndoManager;

		/**
		 * Creates a new text change.
		 *
		 * @param manager the undo manager for this change
		 */
		UndoableTextChange(DocumentUndoManager manager) {
			super(UndoMessages.getString("DocumentUndoManager.operationLabel")); //$NON-NLS-1$
			this.fDocumentUndoManager= manager;
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

		@Override
		public void dispose() {
			reinitialize();
		}

		/**
		 * Undo the change described by this change.
		 */
		protected void undoTextChange() {
			try {
				if (fDocumentUndoManager.fDocument instanceof IDocumentExtension4)
					((IDocumentExtension4) fDocumentUndoManager.fDocument).replace(fStart, fText
							.length(), fPreservedText, fUndoModificationStamp);
				else
					fDocumentUndoManager.fDocument.replace(fStart, fText.length(),
							fPreservedText);
			} catch (BadLocationException x) {
			}
		}

		@Override
		public boolean canUndo() {
			if (isValid()) {
				if (fDocumentUndoManager.fDocument instanceof IDocumentExtension4) {
					long docStamp= ((IDocumentExtension4) fDocumentUndoManager.fDocument)
							.getModificationStamp();

					// Normal case: an undo is valid if its redo will restore
					// document to its current modification stamp
					boolean canUndo= docStamp == IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP
							|| docStamp >= getRedoModificationStamp();

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
							&& this == fDocumentUndoManager.fHistory
									.getUndoOperation(fDocumentUndoManager.fUndoContext)
								// this is the latest operation
							&& this != fDocumentUndoManager.fCurrent
								// there is a more current operation not on the stack
							&& !fDocumentUndoManager.fCurrent.isValid()
							// the current operation is not a valid document
							// modification
							&& fDocumentUndoManager.fCurrent.fUndoModificationStamp !=
							// the invalid current operation has a document stamp
							IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP) {
						canUndo= fDocumentUndoManager.fCurrent.fRedoModificationStamp == docStamp;
					}
					/*
					 * When the composite is the current operation, it may hold the
					 * timestamp of a no-op change. We check this here rather than
					 * in an override of canUndo() in UndoableCompoundTextChange simply to
					 * keep all the special case checks in one place.
					 */
					if (!canUndo
							&& this == fDocumentUndoManager.fHistory
									.getUndoOperation(fDocumentUndoManager.fUndoContext)
							&& // this is the latest operation
							this instanceof UndoableCompoundTextChange
							&& this == fDocumentUndoManager.fCurrent
							&& // this is the current operation
							this.fStart == -1
							&& // the current operation text is not valid
							fDocumentUndoManager.fCurrent.fRedoModificationStamp != IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP) {
							// but it has a redo stamp
						canUndo= fDocumentUndoManager.fCurrent.fRedoModificationStamp == docStamp;
					}
					return canUndo;

				}
				// if there is no timestamp to check, simply return true per the
				// 3.0.1 behavior
				return true;
			}
			return false;
		}

		@Override
		public boolean canRedo() {
			if (isValid()) {
				if (fDocumentUndoManager.fDocument instanceof IDocumentExtension4) {
					long docStamp= ((IDocumentExtension4) fDocumentUndoManager.fDocument)
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

		@Override
		public boolean canExecute() {
			return fDocumentUndoManager.isConnected();
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable uiInfo) {
			// Text changes execute as they are typed, so executing one has no
			// effect.
			return Status.OK_STATUS;
		}

		/**
		 * {@inheritDoc}
		 * Notifies clients about the undo.
		 */
		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable uiInfo) {
			if (isValid()) {
				fDocumentUndoManager.fireDocumentUndo(fStart, fPreservedText, fText, uiInfo, DocumentUndoEvent.ABOUT_TO_UNDO, false);
				undoTextChange();
				fDocumentUndoManager.resetProcessChangeState();
				fDocumentUndoManager.fireDocumentUndo(fStart, fPreservedText, fText, uiInfo, DocumentUndoEvent.UNDONE, false);
				return Status.OK_STATUS;
			}
			return IOperationHistory.OPERATION_INVALID_STATUS;
		}

		/**
		 * Re-applies the change described by this change.
		 */
		protected void redoTextChange() {
			try {
				if (fDocumentUndoManager.fDocument instanceof IDocumentExtension4)
					((IDocumentExtension4) fDocumentUndoManager.fDocument).replace(fStart, fEnd - fStart, fText, fRedoModificationStamp);
				else
					fDocumentUndoManager.fDocument.replace(fStart, fEnd - fStart, fText);
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
		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable uiInfo) {
			if (isValid()) {
				fDocumentUndoManager.fireDocumentUndo(fStart, fText, fPreservedText, uiInfo, DocumentUndoEvent.ABOUT_TO_REDO, false);
				redoTextChange();
				fDocumentUndoManager.resetProcessChangeState();
				fDocumentUndoManager.fireDocumentUndo(fStart, fText, fPreservedText, uiInfo, DocumentUndoEvent.REDONE, false);
				return Status.OK_STATUS;
			}
			return IOperationHistory.OPERATION_INVALID_STATUS;
		}

		/**
		 * Update the change in response to a commit.
		 */

		protected void updateTextChange() {
			fText= fDocumentUndoManager.fTextBuffer.toString();
			fDocumentUndoManager.fTextBuffer.setLength(0);
			fPreservedText= fDocumentUndoManager.fPreservedTextBuffer.toString();
			fDocumentUndoManager.fPreservedTextBuffer.setLength(0);
		}

		/**
		 * Creates a new uncommitted text change depending on whether a compound
		 * change is currently being executed.
		 *
		 * @return a new, uncommitted text change or a compound text change
		 */
		protected UndoableTextChange createCurrent() {
			if (fDocumentUndoManager.fFoldingIntoCompoundChange)
				return new UndoableCompoundTextChange(fDocumentUndoManager);
			return new UndoableTextChange(fDocumentUndoManager);
		}

		/**
		 * Commits the current change into this one.
		 */
		protected void commit() {
			if (fStart < 0) {
				if (fDocumentUndoManager.fFoldingIntoCompoundChange) {
					fDocumentUndoManager.fCurrent= createCurrent();
				} else {
					reinitialize();
				}
			} else {
				updateTextChange();
				fDocumentUndoManager.fCurrent= createCurrent();
			}
			fDocumentUndoManager.resetProcessChangeState();
		}

		/**
		 * Updates the text from the buffers without resetting the buffers or adding
		 * anything to the stack.
		 */
		protected void pretendCommit() {
			if (fStart > -1) {
				fText= fDocumentUndoManager.fTextBuffer.toString();
				fPreservedText= fDocumentUndoManager.fPreservedTextBuffer.toString();
			}
		}

		/**
		 * Attempt a commit of this change and answer true if a new fCurrent was
		 * created as a result of the commit.
		 *
		 * @return <code>true</code> if the change was committed and created
		 *			a new <code>fCurrent</code>, <code>false</code> if not
		 */
		protected boolean attemptCommit() {
			pretendCommit();
			if (isValid()) {
				fDocumentUndoManager.commit();
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

		@Override
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


	/**
	 * Represents an undo-able text change consisting of several individual
	 * changes.
	 */
	private static class UndoableCompoundTextChange extends UndoableTextChange {

		/** The list of individual changes */
		private List<UndoableTextChange> fChanges= new ArrayList<>();

		/**
		 * Creates a new compound text change.
		 *
		 * @param manager the undo manager for this change
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

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable uiInfo) {

			int size= fChanges.size();
			if (size > 0) {
				UndoableTextChange c;

				c= fChanges.get(0);
				fDocumentUndoManager.fireDocumentUndo(c.fStart, c.fPreservedText, c.fText, uiInfo, DocumentUndoEvent.ABOUT_TO_UNDO, true);

				for (int i= size - 1; i >= 0; --i) {
					c= fChanges.get(i);
					c.undoTextChange();
				}
				fDocumentUndoManager.resetProcessChangeState();
				fDocumentUndoManager.fireDocumentUndo(c.fStart, c.fPreservedText, c.fText, uiInfo,
						DocumentUndoEvent.UNDONE, true);
			}
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable uiInfo) {

			int size= fChanges.size();
			if (size > 0) {

				UndoableTextChange c;
				c= fChanges.get(size - 1);
				fDocumentUndoManager.fireDocumentUndo(c.fStart, c.fText, c.fPreservedText, uiInfo, DocumentUndoEvent.ABOUT_TO_REDO, true);

				for (int i= 0; i <= size - 1; ++i) {
					c= fChanges.get(i);
					c.redoTextChange();
				}
				fDocumentUndoManager.resetProcessChangeState();
				fDocumentUndoManager.fireDocumentUndo(c.fStart, c.fText, c.fPreservedText, uiInfo, DocumentUndoEvent.REDONE, true);
			}

			return Status.OK_STATUS;
		}

		@Override
		protected void updateTextChange() {
			// first gather the data from the buffers
			super.updateTextChange();

			// the result of the update is stored as a child change
			UndoableTextChange c= new UndoableTextChange(fDocumentUndoManager);
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

		@Override
		protected UndoableTextChange createCurrent() {

			if (!fDocumentUndoManager.fFoldingIntoCompoundChange)
				return new UndoableTextChange(fDocumentUndoManager);

			reinitialize();
			return this;
		}

		@Override
		protected void commit() {
			// if there is pending data, update the text change
			if (fStart > -1)
				updateTextChange();
			fDocumentUndoManager.fCurrent= createCurrent();
			fDocumentUndoManager.resetProcessChangeState();
		}

		@Override
		protected boolean isValid() {
			return fStart > -1 || fChanges.size() > 0;
		}

		@Override
		protected long getUndoModificationStamp() {
			if (fStart > -1)
				return super.getUndoModificationStamp();
			else if (fChanges.size() > 0)
				return fChanges.get(0)
						.getUndoModificationStamp();

			return fUndoModificationStamp;
		}

		@Override
		protected long getRedoModificationStamp() {
			if (fStart > -1)
				return super.getRedoModificationStamp();
			else if (fChanges.size() > 0)
				return fChanges.get(fChanges.size() - 1)
						.getRedoModificationStamp();

			return fRedoModificationStamp;
		}
	}


	/**
	 * Internal listener to document changes.
	 */
	private class DocumentListener implements IDocumentListener {

		private String fReplacedText;

		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
			try {
				fReplacedText= event.getDocument().get(event.getOffset(),
						event.getLength());
				fPreservedUndoModificationStamp= event.getModificationStamp();
			} catch (BadLocationException x) {
				fReplacedText= null;
			}
		}

		@Override
		public void documentChanged(DocumentEvent event) {
			fPreservedRedoModificationStamp= event.getModificationStamp();

			// record the current valid state for the top operation in case it
			// remains the
			// top operation but changes state.
			IUndoableOperation op= fHistory.getUndoOperation(fUndoContext);
			boolean wasValid= false;
			if (op != null)
				wasValid= op.canUndo();
			// Process the change, providing the before and after timestamps
			processChange(event.getOffset(), event.getOffset()
					+ event.getLength(), event.getText(), fReplacedText,
					fPreservedUndoModificationStamp,
					fPreservedRedoModificationStamp);

			// now update fCurrent with the latest buffers from the document
			// change.
			fCurrent.pretendCommit();

			if (op == fCurrent) {
				// if the document change did not cause a new fCurrent to be
				// created, then we should
				// notify the history that the current operation changed if its
				// validity has changed.
				if (wasValid != fCurrent.isValid())
					fHistory.operationChanged(op);
			} else {
				// if the change created a new fCurrent that we did not yet add
				// to the
				// stack, do so if it's valid and we are not in the middle of a
				// compound change.
				if (fCurrent != fLastAddedTextEdit && fCurrent.isValid()) {
					addToOperationHistory(fCurrent);
				}
			}
		}
	}

	/*
	 * @see IOperationHistoryListener
	 */
	private class HistoryListener implements IOperationHistoryListener {

		private IUndoableOperation fOperation;

		@Override
		public void historyNotification(final OperationHistoryEvent event) {
			final int type= event.getEventType();
			switch (type) {
			case OperationHistoryEvent.ABOUT_TO_UNDO:
			case OperationHistoryEvent.ABOUT_TO_REDO:
				// if this is one of our operations
				if (event.getOperation().hasContext(fUndoContext)) {
					// if we are undoing/redoing an operation we generated, then
					// ignore
					// the document changes associated with this undo or redo.
					if (event.getOperation() instanceof UndoableTextChange) {
						listenToTextChanges(false);

						// in the undo case only, make sure compounds are closed
						if (type == OperationHistoryEvent.ABOUT_TO_UNDO) {
							if (fFoldingIntoCompoundChange) {
								endCompoundChange();
							}
						}
					} else {
						// the undo or redo has our context, but it is not one
						// of our edits. We will listen to the changes, but will
						// reset the state that tracks the undo/redo history.
						commit();
						fLastAddedTextEdit= null;
					}
					fOperation= event.getOperation();
				}
				break;
			case OperationHistoryEvent.UNDONE:
			case OperationHistoryEvent.REDONE:
			case OperationHistoryEvent.OPERATION_NOT_OK:
				if (event.getOperation() == fOperation) {
					listenToTextChanges(true);
					fOperation= null;
				}
				break;
			}
		}

	}


	/**
	 * The undo context for this document undo manager.
	 */
	private ObjectUndoContext fUndoContext;

	/**
	 * The document whose changes are being tracked.
	 */
	private IDocument fDocument;

	/**
	 * The currently constructed edit.
	 */
	private UndoableTextChange fCurrent;

	/**
	 * The internal document listener.
	 */
	private DocumentListener fDocumentListener;

	/**
	 * Indicates whether the current change belongs to a compound change.
	 */
	private boolean fFoldingIntoCompoundChange= false;

	/**
	 * The operation history being used to store the undo history.
	 */
	private IOperationHistory fHistory;

	/**
	 * The operation history listener used for managing undo and redo before and
	 * after the individual edits are performed.
	 */
	private IOperationHistoryListener fHistoryListener;

	/**
	 * The text edit last added to the operation history. This must be tracked
	 * internally instead of asking the history, since outside parties may be
	 * placing items on our undo/redo history.
	 */
	private UndoableTextChange fLastAddedTextEdit= null;

	/**
	 * The document modification stamp for redo.
	 */
	private long fPreservedRedoModificationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;

	/**
	 * Text buffer to collect viewer content which has been replaced
	 */
	private StringBuffer fPreservedTextBuffer;

	/**
	 * The document modification stamp for undo.
	 */
	private long fPreservedUndoModificationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;

	/**
	 * The last delete text edit.
	 */
	private UndoableTextChange fPreviousDelete;

	/**
	 * Text buffer to collect text which is inserted into the viewer
	 */
	private StringBuffer fTextBuffer;

	/** Indicates inserting state. */
	private boolean fInserting= false;

	/** Indicates overwriting state. */
	private boolean fOverwriting= false;

	/** The registered document listeners. */
	private ListenerList<IDocumentUndoListener> fDocumentUndoListeners;

	/** The list of clients connected. */
	private List<Object> fConnected;

	/**
	 *
	 * Create a DocumentUndoManager for the given document.
	 *
	 * @param document the document whose undo history is being managed.
	 */
	public DocumentUndoManager(IDocument document) {
		super();
		Assert.isNotNull(document);
		fDocument= document;
		fHistory= OperationHistoryFactory.getOperationHistory();
		fUndoContext= new ObjectUndoContext(fDocument);
		fConnected= new ArrayList<>();
		fDocumentUndoListeners= new ListenerList<>(ListenerList.IDENTITY);
	}

	@Override
	public void addDocumentUndoListener(IDocumentUndoListener listener) {
		fDocumentUndoListeners.add(listener);
	}

	@Override
	public void removeDocumentUndoListener(IDocumentUndoListener listener) {
		fDocumentUndoListeners.remove(listener);
	}

	@Override
	public IUndoContext getUndoContext() {
		return fUndoContext;
	}

	@Override
	public void commit() {
		// if fCurrent has never been placed on the history, do so now.
		// this can happen when there are multiple programmatically commits in a
		// single document change.
		if (fLastAddedTextEdit != fCurrent) {
			fCurrent.pretendCommit();
			if (fCurrent.isValid())
				addToOperationHistory(fCurrent);
		}
		fCurrent.commit();
	}

	@Override
	public void reset() {
		if (isConnected()) {
			shutdown();
			initialize();
		}
	}

	@Override
	public boolean redoable() {
		return OperationHistoryFactory.getOperationHistory().canRedo(fUndoContext);
	}

	@Override
	public boolean undoable() {
		return OperationHistoryFactory.getOperationHistory().canUndo(fUndoContext);
	}

	/*
	 * @see org.eclipse.text.undo.IDocumentUndoManager#undo()
	 */
	@Override
	public void redo() throws ExecutionException {
		if (isConnected() && redoable())
			OperationHistoryFactory.getOperationHistory().redo(getUndoContext(), null, null);
	}

	@Override
	public void undo() throws ExecutionException {
		if (undoable())
			OperationHistoryFactory.getOperationHistory().undo(fUndoContext, null, null);
	}

	@Override
	public void connect(Object client) {
		if (!isConnected()) {
			initialize();
		}
		if (!fConnected.contains(client))
			fConnected.add(client);
	}

	@Override
	public void disconnect(Object client) {
		fConnected.remove(client);
		if (!isConnected()) {
			shutdown();
		}
	}

	@Override
	public void beginCompoundChange() {
		if (isConnected()) {
			fFoldingIntoCompoundChange= true;
			commit();
		}
	}

	@Override
	public void endCompoundChange() {
		if (isConnected()) {
			fFoldingIntoCompoundChange= false;
			commit();
		}
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentUndoManager#setUndoLimit(int)
	 */
	@Override
	public void setMaximalUndoLevel(int undoLimit) {
		fHistory.setLimit(fUndoContext, undoLimit);
	}

	/**
	 * Fires a document undo event to all registered document undo listeners.
	 * Uses a robust iterator.
	 *
	 * @param offset the document offset
	 * @param text the text that was inserted
	 * @param preservedText the text being replaced
	 * @param source the source which triggered the event
	 * @param eventType the type of event causing the change
	 * @param isCompound a flag indicating whether the change is a compound change
	 * @see IDocumentUndoListener
	 */
	void fireDocumentUndo(int offset, String text, String preservedText, Object source, int eventType, boolean isCompound) {
		eventType= isCompound ? eventType | DocumentUndoEvent.COMPOUND : eventType;
		DocumentUndoEvent event= new DocumentUndoEvent(fDocument, offset, text, preservedText, eventType, source);
		for (IDocumentUndoListener listener : fDocumentUndoListeners) {
			listener.documentUndoNotification(event);
		}
	}

	/**
	 * Adds any listeners needed to track the document and the operations
	 * history.
	 */
	private void addListeners() {
		fHistoryListener= new HistoryListener();
		fHistory.addOperationHistoryListener(fHistoryListener);
		listenToTextChanges(true);
	}

	/**
	 * Removes any listeners that were installed by the document.
	 */
	private void removeListeners() {
		listenToTextChanges(false);
		fHistory.removeOperationHistoryListener(fHistoryListener);
		fHistoryListener= null;
	}

	/**
	 * Adds the given text edit to the operation history if it is not part of a compound change.
	 *
	 * @param edit the edit to be added
	 */
	private void addToOperationHistory(UndoableTextChange edit) {
		if (!fFoldingIntoCompoundChange
				|| edit instanceof UndoableCompoundTextChange) {
			fHistory.add(edit);
			fLastAddedTextEdit= edit;
		}
	}

	/**
	 * Disposes the undo history.
	 */
	private void disposeUndoHistory() {
		fHistory.dispose(fUndoContext, true, true, true);
	}

	/**
	 * Initializes the undo history.
	 */
	private void initializeUndoHistory() {
		if (fHistory != null && fUndoContext != null)
			fHistory.dispose(fUndoContext, true, true, false);

	}

	/**
	 * Checks whether the given text starts with a line delimiter and
	 * subsequently contains a white space only.
	 *
	 * @param text the text to check
	 * @return <code>true</code> if the text is a line delimiter followed by
	 *         whitespace, <code>false</code> otherwise
	 */
	private boolean isWhitespaceText(String text) {

		if (text == null || text.length() == 0)
			return false;

		String[] delimiters= fDocument.getLegalLineDelimiters();
		int index= TextUtilities.startsWith(delimiters, text);
		if (index > -1) {
			char c;
			int length= text.length();
			for (int i= delimiters[index].length(); i < length; i++) {
				c= text.charAt(i);
				if (c != ' ' && c != '\t')
					return false;
			}
			return true;
		}

		return false;
	}

	/**
	 * Switches the state of whether there is a text listener or not.
	 *
	 * @param listen the state which should be established
	 */
	private void listenToTextChanges(boolean listen) {
		if (listen) {
			if (fDocumentListener == null && fDocument != null) {
				fDocumentListener= new DocumentListener();
				fDocument.addDocumentListener(fDocumentListener);
			}
		} else if (!listen) {
			if (fDocumentListener != null && fDocument != null) {
				fDocument.removeDocumentListener(fDocumentListener);
				fDocumentListener= null;
			}
		}
	}

	private void processChange(int modelStart, int modelEnd,
			String insertedText, String replacedText,
			final long beforeChangeModificationStamp,
			final long afterChangeModificationStamp) {

		if (insertedText == null)
			insertedText= ""; //$NON-NLS-1$

		if (replacedText == null)
			replacedText= ""; //$NON-NLS-1$

		int length= insertedText.length();
		int diff= modelEnd - modelStart;

		if (fCurrent.fUndoModificationStamp == IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP)
			fCurrent.fUndoModificationStamp= beforeChangeModificationStamp;

		// normalize
		if (diff < 0) {
			int tmp= modelEnd;
			modelEnd= modelStart;
			modelStart= tmp;
		}

		if (modelStart == modelEnd) {
			// text will be inserted
			if ((length == 1) || isWhitespaceText(insertedText)) {
				// by typing or whitespace
				if (!fInserting
						|| (modelStart != fCurrent.fStart
								+ fTextBuffer.length())) {
					fCurrent.fRedoModificationStamp= beforeChangeModificationStamp;
					if (fCurrent.attemptCommit())
						fCurrent.fUndoModificationStamp= beforeChangeModificationStamp;

					fInserting= true;
				}
				if (fCurrent.fStart < 0)
					fCurrent.fStart= fCurrent.fEnd= modelStart;
				if (length > 0)
					fTextBuffer.append(insertedText);
			} else if (length > 0) {
				// by pasting or model manipulation
				fCurrent.fRedoModificationStamp= beforeChangeModificationStamp;
				if (fCurrent.attemptCommit())
					fCurrent.fUndoModificationStamp= beforeChangeModificationStamp;

				fCurrent.fStart= fCurrent.fEnd= modelStart;
				fTextBuffer.append(insertedText);
				fCurrent.fRedoModificationStamp= afterChangeModificationStamp;
				if (fCurrent.attemptCommit())
					fCurrent.fUndoModificationStamp= afterChangeModificationStamp;

			}
		} else {
			if (length == 0) {
				// text will be deleted by backspace or DEL key or empty
				// clipboard
				length= replacedText.length();
				String[] delimiters= fDocument.getLegalLineDelimiters();

				if ((length == 1)
						|| TextUtilities.equals(delimiters, replacedText) > -1) {

					// whereby selection is empty

					if (fPreviousDelete.fStart == modelStart
							&& fPreviousDelete.fEnd == modelEnd) {
						// repeated DEL

						// correct wrong settings of fCurrent
						if (fCurrent.fStart == modelEnd
								&& fCurrent.fEnd == modelStart) {
							fCurrent.fStart= modelStart;
							fCurrent.fEnd= modelEnd;
						}
						// append to buffer && extend edit range
						fPreservedTextBuffer.append(replacedText);
						++fCurrent.fEnd;

					} else if (fPreviousDelete.fStart == modelEnd) {
						// repeated backspace

						// insert in buffer and extend edit range
						fPreservedTextBuffer.insert(0, replacedText);
						fCurrent.fStart= modelStart;

					} else {
						// either DEL or backspace for the first time

						fCurrent.fRedoModificationStamp= beforeChangeModificationStamp;
						if (fCurrent.attemptCommit())
							fCurrent.fUndoModificationStamp= beforeChangeModificationStamp;

						// as we can not decide whether it was DEL or backspace
						// we initialize for backspace
						fPreservedTextBuffer.append(replacedText);
						fCurrent.fStart= modelStart;
						fCurrent.fEnd= modelEnd;
					}

					fPreviousDelete.set(modelStart, modelEnd);

				} else if (length > 0) {
					// whereby selection is not empty
					fCurrent.fRedoModificationStamp= beforeChangeModificationStamp;
					if (fCurrent.attemptCommit())
						fCurrent.fUndoModificationStamp= beforeChangeModificationStamp;

					fCurrent.fStart= modelStart;
					fCurrent.fEnd= modelEnd;
					fPreservedTextBuffer.append(replacedText);
				}
			} else {
				// text will be replaced

				if (length == 1) {
					length= replacedText.length();
					String[] delimiters= fDocument.getLegalLineDelimiters();

					if ((length == 1)
							|| TextUtilities.equals(delimiters, replacedText) > -1) {
						// because of overwrite mode or model manipulation
						if (!fOverwriting
								|| (modelStart != fCurrent.fStart
										+ fTextBuffer.length())) {
							fCurrent.fRedoModificationStamp= beforeChangeModificationStamp;
							if (fCurrent.attemptCommit())
								fCurrent.fUndoModificationStamp= beforeChangeModificationStamp;

							fOverwriting= true;
						}

						if (fCurrent.fStart < 0)
							fCurrent.fStart= modelStart;

						fCurrent.fEnd= modelEnd;
						fTextBuffer.append(insertedText);
						fPreservedTextBuffer.append(replacedText);
						fCurrent.fRedoModificationStamp= afterChangeModificationStamp;
						return;
					}
				}
				// because of typing or pasting whereby selection is not empty
				fCurrent.fRedoModificationStamp= beforeChangeModificationStamp;
				if (fCurrent.attemptCommit())
					fCurrent.fUndoModificationStamp= beforeChangeModificationStamp;

				fCurrent.fStart= modelStart;
				fCurrent.fEnd= modelEnd;
				fTextBuffer.append(insertedText);
				fPreservedTextBuffer.append(replacedText);
			}
		}
		// in all cases, the redo modification stamp is updated on the open
		// text edit
		fCurrent.fRedoModificationStamp= afterChangeModificationStamp;
	}

	/**
	 * Initialize the receiver.
	 */
	private void initialize() {
		initializeUndoHistory();

		// open up the current text edit
		fCurrent= new UndoableTextChange(this);
		fPreviousDelete= new UndoableTextChange(this);
		fTextBuffer= new StringBuffer();
		fPreservedTextBuffer= new StringBuffer();

		addListeners();
	}

	/**
	 * Reset processChange state.
	 *
	 * @since 3.2
	 */
	private void resetProcessChangeState() {
		fInserting= false;
		fOverwriting= false;
		fPreviousDelete.reinitialize();
	}

	/**
	 * Shutdown the receiver.
	 */
	private void shutdown() {
		removeListeners();

		fCurrent= null;
		fPreviousDelete= null;
		fTextBuffer= null;
		fPreservedTextBuffer= null;

		disposeUndoHistory();
	}

	/**
	 * Return whether or not any clients are connected to the receiver.
	 *
	 * @return <code>true</code> if the receiver is connected to
	 * 			clients, <code>false</code> if it is not
	 */
	boolean isConnected() {
		if (fConnected == null)
			return false;
		return !fConnected.isEmpty();
	}

	@Override
	public void transferUndoHistory(IDocumentUndoManager manager) {
		IUndoContext oldUndoContext= manager.getUndoContext();
		// Get the history for the old undo context.
		IUndoableOperation [] operations= OperationHistoryFactory.getOperationHistory().getUndoHistory(oldUndoContext);
		for (int i= 0; i < operations.length; i++) {
			// First replace the undo context
			IUndoableOperation op= operations[i];
			if (op instanceof IContextReplacingOperation) {
				((IContextReplacingOperation)op).replaceContext(oldUndoContext, getUndoContext());
			} else {
				op.addContext(getUndoContext());
				op.removeContext(oldUndoContext);
			}
			// Now update the manager that owns the text edit.
			if (op instanceof UndoableTextChange) {
				((UndoableTextChange)op).fDocumentUndoManager= this;
			}
		}

		IUndoableOperation op= OperationHistoryFactory.getOperationHistory().getUndoOperation(getUndoContext());
		if (op != null && !(op instanceof UndoableTextChange))
			return;

		// Record the transfer itself as an undoable change.
		// If the transfer results from some open operation, recording this change will
		// cause our undo context to be added to the outer operation.  If there is no
		// outer operation, there will be a local change to signify the transfer.
		// This also serves to synchronize the modification stamps with the documents.
		UndoableTextChange cmd= new UndoableTextChange(this);
		cmd.fStart= cmd.fEnd= 0;
		cmd.fText= cmd.fPreservedText= ""; //$NON-NLS-1$
		if (fDocument instanceof IDocumentExtension4) {
			cmd.fRedoModificationStamp= ((IDocumentExtension4)fDocument).getModificationStamp();
			if (op != null)
				cmd.fUndoModificationStamp= ((UndoableTextChange)op).fRedoModificationStamp;
		}
		addToOperationHistory(cmd);
	}


}
