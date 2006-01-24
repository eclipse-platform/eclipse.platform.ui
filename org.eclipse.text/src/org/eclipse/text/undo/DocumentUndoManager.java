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

import org.eclipse.core.commands.operations.IContextReplacingOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;

import org.eclipse.core.runtime.ListenerList;

import org.eclipse.jface.text.Assert;
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
 * <p>
 * XXX: This is work in progress and can change anytime until API for 3.2 is frozen.
 * </p>
 * 
 * @see IDocumentUndoManager
 * @see DocumentUndoManagerRegistry
 * @see IDocumentUndoListener
 * @see org.eclipse.jface.text.IDocument
 * @since 3.2
 */
public class DocumentUndoManager implements IDocumentUndoManager {

	/**
	 * Internal listener to document changes.
	 */
	private class DocumentListener implements IDocumentListener {

		private String fReplacedText;

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentAboutToBeChanged(DocumentEvent event) {
			try {
				fReplacedText= event.getDocument().get(event.getOffset(),
						event.getLength());
				fPreservedUndoModificationStamp= event.getModificationStamp();
			} catch (BadLocationException x) {
				fReplacedText= null;
			}
		}

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
		 */
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
	class HistoryListener implements IOperationHistoryListener {
		private IUndoableOperation fOperation;

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
	ObjectUndoContext fUndoContext;

	/**
	 * The document whose changes are being tracked.
	 */
	IDocument fDocument;

	/**
	 * The currently constructed edit.
	 */
	UndoableTextChange fCurrent;

	/**
	 * The internal document listener.
	 */
	private DocumentListener fDocumentListener;

	/**
	 * Indicates whether the current change belongs to a compound change.
	 */
	boolean fFoldingIntoCompoundChange= false;

	/**
	 * The operation history being used to store the undo history.
	 */
	IOperationHistory fHistory;

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
	protected long fPreservedRedoModificationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;

	/**
	 * Text buffer to collect viewer content which has been replaced
	 */
	StringBuffer fPreservedTextBuffer;

	/**
	 * The document modification stamp for undo.
	 */
	protected long fPreservedUndoModificationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;

	/**
	 * The last delete text edit.
	 */
	private UndoableTextChange fPreviousDelete;

	/**
	 * Text buffer to collect text which is inserted into the viewer
	 */
	StringBuffer fTextBuffer;

	/** Indicates inserting state. */
	private boolean fInserting= false;

	/** Indicates overwriting state. */
	private boolean fOverwriting= false;

	/** The registered document listeners. */
	private ListenerList fDocumentUndoListeners;

	/** The list of clients connected. */
	private List fConnected;

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
		fConnected= new ArrayList();
		fDocumentUndoListeners= new ListenerList();
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentUndoManager#addDocumentUndoListener(org.eclipse.jface.text.IDocumentUndoListener)
	 */
	public void addDocumentUndoListener(IDocumentUndoListener listener) {
		fDocumentUndoListeners.add(listener);
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentUndoManager#removeDocumentUndoListener(org.eclipse.jface.text.IDocumentUndoListener)
	 */
	public void removeDocumentUndoListener(IDocumentUndoListener listener) {
		fDocumentUndoListeners.remove(listener);
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentUndoManager#getUndoContext()
	 */
	public IUndoContext getUndoContext() {
		return fUndoContext;
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentUndoManager#commit()
	 */
	public void commit() {

		fInserting= false;
		fOverwriting= false;
		fPreviousDelete.reinitialize();

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

	/*
	 * @see org.eclipse.jface.text.IDocumentUndoManager#connect(java.lang.Object)
	 */
	public void connect(Object client) {
		if (!isConnected()) {
			initialize();
		}
		if (!fConnected.contains(client))
			fConnected.add(client);
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentUndoManager#disconnect(java.lang.Object)
	 */
	public void disconnect(Object client) {
		fConnected.remove(client);
		if (!isConnected()) {
			shutdown();
		}
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentUndoManager#beginCompoundChange()
	 */
	public void beginCompoundChange() {
		if (isConnected()) {
			fFoldingIntoCompoundChange= true;
			commit();
		}
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentUndoManager#endCompoundChange()
	 */
	public void endCompoundChange() {
		if (isConnected()) {
			fFoldingIntoCompoundChange= false;
			commit();
		}
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentUndoManager#setUndoLimit(int)
	 */
	public void setUndoLimit(int undoLimit) {
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
		Object[] listeners= fDocumentUndoListeners.getListeners();
		for (int i= 0; i < listeners.length; i++) {
			((IDocumentUndoListener)listeners[i]).documentUndoNotification(event);
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
	 * Adds the given text edit to the operation history if it is not part of a
	 * compound change.
	 * 
	 * @param edit
	 *            the edit to be added
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
			long beforeChangeModificationStamp,
			long afterChangeModificationStamp) {

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
	
	
	/*
	 * @see org.eclipse.jface.text.IDocumentUndoManager#transferUndoHistory(IDocumentUndoManager)
	 */
	public void transferUndoHistory(IDocumentUndoManager manager) {
		IUndoContext oldUndoContext= manager.getUndoContext();
		// Get the history for the old undo context.
		IUndoableOperation [] operations= OperationHistoryFactory.getOperationHistory().getUndoHistory(oldUndoContext);
		for (int i= 0; i< operations.length; i++) {
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
				((UndoableTextChange)op).manager= this;
			}
		}
		
		// Record the transfer itself as an undoable change.
		// If the transfer results from some open operation, recording this change will
		// cause our undo context to be added to the outer operation.  If there is no
		// outer operation, there will be a local change to signify the transfer.
		// This also serves to synchronize the modification stamps with the documents.
		IUndoableOperation op= OperationHistoryFactory.getOperationHistory().getUndoOperation(getUndoContext());
		UndoableTextChange cmd= new UndoableTextChange(this);
		cmd.fStart= cmd.fEnd= 0;
		cmd.fText= cmd.fPreservedText= ""; //$NON-NLS-1$
		if (fDocument instanceof IDocumentExtension4) {
			cmd.fRedoModificationStamp= ((IDocumentExtension4)fDocument).getModificationStamp();
			if (op instanceof UndoableTextChange) {
				cmd.fUndoModificationStamp= ((UndoableTextChange)op).fRedoModificationStamp;
			}
		}
		addToOperationHistory(cmd);
	}

}
