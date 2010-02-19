/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.dialogs.MessageDialog;


/**
 * Standard implementation of {@link org.eclipse.jface.text.IUndoManager}.
 * <p>
 * It registers with the connected text viewer as text input listener and
 * document listener and logs all changes. It also monitors mouse and keyboard
 * activities in order to partition the stream of text changes into undo-able
 * edit commands.
 * </p>
 * <p>
 * Since 3.1 this undo manager is a facade to the global operation history.
 * </p>
 * <p>
 * The usage of {@link org.eclipse.core.runtime.IAdaptable} in the JFace
 * layer has been approved by Platform UI, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=87669#c9
 * </p>
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 *
 * @see org.eclipse.jface.text.ITextViewer
 * @see org.eclipse.jface.text.ITextInputListener
 * @see org.eclipse.jface.text.IDocumentListener
 * @see org.eclipse.core.commands.operations.IUndoableOperation
 * @see org.eclipse.core.commands.operations.IOperationHistory
 * @see MouseListener
 * @see KeyListener
 * @deprecated As of 3.2, replaced by {@link TextViewerUndoManager}
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DefaultUndoManager implements IUndoManager, IUndoManagerExtension {

	/**
	 * Represents an undo-able edit command.
	 * <p>
	 * Since 3.1 this implements the interface for IUndoableOperation.
	 * </p>
	 */
	class TextCommand extends AbstractOperation {

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

		/**
		 * Creates a new text command.
		 *
		 * @param context the undo context for this command
		 * @since 3.1
		 */
		TextCommand(IUndoContext context) {
		    super(JFaceTextMessages.getString("DefaultUndoManager.operationLabel")); //$NON-NLS-1$
		    addContext(context);
		}

		/**
		 * Re-initializes this text command.
		 */
		protected void reinitialize() {
			fStart= fEnd= -1;
			fText= fPreservedText= null;
			fUndoModificationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
			fRedoModificationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
		}

		/**
		 * Sets the start and the end index of this command.
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
		 * @since 3.1
		 */
		public void dispose() {
		    reinitialize();
		}

		/**
		 * Undo the change described by this command.
		 *
		 * @since 2.0
		 */
		protected void undoTextChange() {
			try {
				IDocument document= fTextViewer.getDocument();
				if (document instanceof IDocumentExtension4)
					((IDocumentExtension4)document).replace(fStart, fText.length(), fPreservedText, fUndoModificationStamp);
				else
					document.replace(fStart, fText.length(), fPreservedText);
			} catch (BadLocationException x) {
			}
		}

		/*
		 * @see org.eclipse.core.commands.operations.IUndoableOperation#canUndo()
		 * @since 3.1
		 */
		public boolean canUndo() {

			if (isConnected() && isValid()) {
				IDocument doc= fTextViewer.getDocument();
				if (doc instanceof IDocumentExtension4) {
					long docStamp= ((IDocumentExtension4)doc).getModificationStamp();

					// Normal case: an undo is valid if its redo will restore document
					// to its current modification stamp
					boolean canUndo= docStamp == IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP ||
						docStamp == getRedoModificationStamp();

					/* Special case to check if the answer is false.
					 * If the last document change was empty, then the document's
					 * modification stamp was incremented but nothing was committed.
					 * The operation being queried has an older stamp.  In this case only,
					 * the comparison is different.  A sequence of document changes that
					 * include an empty change is handled correctly when a valid commit
					 * follows the empty change, but when #canUndo() is queried just after
					 * an empty change, we must special case the check.  The check is very
					 * specific to prevent false positives.
					 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=98245
					 */
					if (!canUndo &&
							this == fHistory.getUndoOperation(fUndoContext)  &&  // this is the latest operation
							this != fCurrent && // there is a more current operation not on the stack
							!fCurrent.isValid() &&  // the current operation is not a valid document modification
							fCurrent.fUndoModificationStamp != // the invalid current operation has a document stamp
								IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP) {
						canUndo= fCurrent.fRedoModificationStamp == docStamp;
					}
					/*
					 * When the composite is the current command, it may hold the timestamp
					 * of a no-op change.  We check this here rather than in an override of
					 * canUndo() in CompoundTextCommand simply to keep all the special case checks
					 * in one place.
					 */
					if (!canUndo &&
							this == fHistory.getUndoOperation(fUndoContext)  &&  // this is the latest operation
							this instanceof CompoundTextCommand &&
							this == fCurrent && // this is the current operation
							this.fStart == -1 &&  // the current operation text is not valid
							fCurrent.fRedoModificationStamp != IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP) {  // but it has a redo stamp
						canUndo= fCurrent.fRedoModificationStamp == docStamp;
					}

				}
				// if there is no timestamp to check, simply return true per the 3.0.1 behavior
				return true;
			}
			return false;
		}

		/*
		 * @see org.eclipse.core.commands.operations.IUndoableOperation#canRedo()
		 * @since 3.1
		 */
		public boolean canRedo() {
			if (isConnected() && isValid()) {
				IDocument doc= fTextViewer.getDocument();
				if (doc instanceof IDocumentExtension4) {
					long docStamp= ((IDocumentExtension4)doc).getModificationStamp();
					return docStamp == IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP ||
						docStamp == getUndoModificationStamp();
				}
				// if there is no timestamp to check, simply return true per the 3.0.1 behavior
				return true;
			}
			return false;
		}

		/*
		 * @see org.eclipse.core.commands.operations.IUndoableOperation#canExecute()
		 * @since 3.1
		 */
		public boolean canExecute() {
		    return isConnected();
		}

		/*
		 * @see org.eclipse.core.commands.operations.IUndoableOperation#execute(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
		 * @since 3.1
		 */
		public IStatus execute(IProgressMonitor monitor, IAdaptable uiInfo) {
			// Text commands execute as they are typed, so executing one has no effect.
		    return Status.OK_STATUS;
		}

		/*
		 * Undo the change described by this command. Also selects and
		 * reveals the change.
		 */

		/**
		 * Undo the change described by this command. Also selects and
		 * reveals the change.
		 *
		 * @param monitor	the progress monitor to use if necessary
		 * @param uiInfo	an adaptable that can provide UI info if needed
		 * @return the status
		 */
		public IStatus undo(IProgressMonitor monitor, IAdaptable uiInfo) {
			if (isValid()) {
				undoTextChange();
				selectAndReveal(fStart, fPreservedText == null ? 0 : fPreservedText.length());
				resetProcessChangeSate();
				return Status.OK_STATUS;
			}
			return IOperationHistory.OPERATION_INVALID_STATUS;
		}

		/**
		 * Re-applies the change described by this command.
		 *
		 * @since 2.0
		 */
		protected void redoTextChange() {
			try {
				IDocument document= fTextViewer.getDocument();
				if (document instanceof IDocumentExtension4)
					((IDocumentExtension4)document).replace(fStart, fEnd - fStart, fText, fRedoModificationStamp);
				else
					fTextViewer.getDocument().replace(fStart, fEnd - fStart, fText);
			} catch (BadLocationException x) {
			}
		}

		/**
		 * Re-applies the change described by this command that previously been
		 * rolled back. Also selects and reveals the change.
		 *
		 * @param monitor	the progress monitor to use if necessary
		 * @param uiInfo	an adaptable that can provide UI info if needed
		 * @return the status
		 */
		public IStatus redo(IProgressMonitor monitor, IAdaptable uiInfo) {
			if (isValid()) {
				redoTextChange();
				resetProcessChangeSate();
				selectAndReveal(fStart, fText == null ? 0 : fText.length());
				return Status.OK_STATUS;
			}
			return IOperationHistory.OPERATION_INVALID_STATUS;
		}

		/**
		 * Update the command in response to a commit.
		 *
		 * @since 3.1
		 */

		protected void updateCommand() {
			fText= fTextBuffer.toString();
			fTextBuffer.setLength(0);
			fPreservedText= fPreservedTextBuffer.toString();
			fPreservedTextBuffer.setLength(0);
		}

		/**
		 * Creates a new uncommitted text command depending on whether
		 * a compound change is currently being executed.
		 *
		 * @return a new, uncommitted text command or a compound text command
		 */
		protected TextCommand createCurrent() {
			return fFoldingIntoCompoundChange ? new CompoundTextCommand(fUndoContext) : new TextCommand(fUndoContext);
		}

		/**
		 * Commits the current change into this command.
		 */
		protected void commit() {
			if (fStart < 0) {
				if (fFoldingIntoCompoundChange) {
					fCurrent= createCurrent();
				} else {
					reinitialize();
				}
 			} else {
				updateCommand();
				fCurrent= createCurrent();
			}
			resetProcessChangeSate();
		}

		/**
		 * Updates the text from the buffers without resetting
		 * the buffers or adding anything to the stack.
		 *
		 * @since 3.1
		 */
		protected void pretendCommit() {
			if (fStart > -1) {
				fText= fTextBuffer.toString();
				fPreservedText= fPreservedTextBuffer.toString();
			}
		}

		/**
		 * Attempt a commit of this command and answer true if a new
		 * fCurrent was created as a result of the commit.
		 *
		 * @return true if the command was committed and created a
		 * new fCurrent, false if not.
		 * @since 3.1
		 */
		protected boolean attemptCommit() {
			pretendCommit();
			if (isValid()) {
				DefaultUndoManager.this.commit();
				return true;
			}
			return false;
		}

		/**
		 * Checks whether this text command is valid for undo or redo.
		 *
		 * @return <code>true</code> if the command is valid for undo or redo
		 * @since 3.1
		 */
		protected boolean isValid() {
		    return fStart > -1 &&
		    	fEnd > -1 &&
		    	fText != null;
		}

		/*
		 * @see java.lang.Object#toString()
		 * @since 3.1
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
		 * @return the undo modification stamp for this command
		 * @since 3.1
		 */
		protected long getUndoModificationStamp() {
			return fUndoModificationStamp;
		}

		/**
		 * Return the redo modification stamp
		 *
		 * @return the redo modification stamp for this command
		 * @since 3.1
		 */
		protected long getRedoModificationStamp() {
			return fRedoModificationStamp;
		}
	}

	/**
	 * Represents an undo-able edit command consisting of several
	 * individual edit commands.
	 */
	class CompoundTextCommand extends TextCommand {

		/** The list of individual commands */
		private List fCommands= new ArrayList();

		/**
		 * Creates a new compound text command.
		 *
		 * @param context the undo context for this command
		 * @since 3.1
		 */
		CompoundTextCommand(IUndoContext context) {
		    super(context);
		}

		/**
		 * Adds a new individual command to this compound command.
		 *
		 * @param command the command to be added
		 */
		protected void add(TextCommand command) {
			fCommands.add(command);
		}

		/*
		 * @see org.eclipse.jface.text.DefaultUndoManager.TextCommand#undo()
		 */
		public IStatus undo(IProgressMonitor monitor, IAdaptable uiInfo) {
			resetProcessChangeSate();

			int size= fCommands.size();
			if (size > 0) {

				TextCommand c;

				for (int i= size -1; i > 0;  --i) {
					c= (TextCommand) fCommands.get(i);
					c.undoTextChange();
				}

				c= (TextCommand) fCommands.get(0);
				c.undo(monitor, uiInfo);
			}

			return Status.OK_STATUS;
		}

		/*
		 * @see org.eclipse.jface.text.DefaultUndoManager.TextCommand#redo()
		 */
		public IStatus redo(IProgressMonitor monitor, IAdaptable uiInfo) {
			resetProcessChangeSate();

			int size= fCommands.size();
			if (size > 0) {

				TextCommand c;

				for (int i= 0; i < size -1;  ++i) {
					c= (TextCommand) fCommands.get(i);
					c.redoTextChange();
				}

				c= (TextCommand) fCommands.get(size -1);
				c.redo(monitor, uiInfo);
			}
			return Status.OK_STATUS;
		}

		/*
		 * @see TextCommand#updateCommand

		 */

		protected void updateCommand() {
			// first gather the data from the buffers
			super.updateCommand();

			// the result of the command update is stored as a child command
			TextCommand c= new TextCommand(fUndoContext);
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
		 * @see TextCommand#createCurrent
		 */
		protected TextCommand createCurrent() {

			if (!fFoldingIntoCompoundChange)
				return new TextCommand(fUndoContext);

			reinitialize();
			return this;
		}

		/*
		 * @see org.eclipse.jface.text.DefaultUndoManager.TextCommand#commit()
		 */
		protected void commit() {
			// if there is pending data, update the command
			if (fStart > -1)
				updateCommand();
			fCurrent= createCurrent();
			resetProcessChangeSate();
		}

		/**
		 * Checks whether the command is valid for undo or redo.
		 *
		 * @return true if the command is valid.
		 * @since 3.1
		 */
		protected boolean isValid() {
			if (isConnected())
				return (fStart > -1 || fCommands.size() > 0);
		    return false;
		}

		/**
		 * Returns the undo modification stamp.
		 *
		 * @return the undo modification stamp
		 * @since 3.1
		 */
		protected long getUndoModificationStamp() {
			if (fStart > -1)
				return super.getUndoModificationStamp();
			else if (fCommands.size() > 0)
				return ((TextCommand)fCommands.get(0)).getUndoModificationStamp();

			return fUndoModificationStamp;
		}

		/**
		 * Returns the redo modification stamp.
		 *
		 * @return the redo modification stamp
		 * @since 3.1
		 */
		protected long getRedoModificationStamp() {
			if (fStart > -1)
				return super.getRedoModificationStamp();
			else if (fCommands.size() > 0)
				return ((TextCommand)fCommands.get(fCommands.size()-1)).getRedoModificationStamp();

			return fRedoModificationStamp;
		}
	}

	/**
	 * Internal listener to mouse and key events.
	 */
	class KeyAndMouseListener implements MouseListener, KeyListener {

		/*
		 * @see MouseListener#mouseDoubleClick
		 */
		public void mouseDoubleClick(MouseEvent e) {
		}

		/*
		 * If the right mouse button is pressed, the current editing command is closed
		 * @see MouseListener#mouseDown
		 */
		public void mouseDown(MouseEvent e) {
			if (e.button == 1)
				commit();
		}

		/*
		 * @see MouseListener#mouseUp
		 */
		public void mouseUp(MouseEvent e) {
		}

		/*
		 * @see KeyListener#keyPressed
		 */
		public void keyReleased(KeyEvent e) {
		}

		/*
		 * On cursor keys, the current editing command is closed
		 * @see KeyListener#keyPressed
		 */
		public void keyPressed(KeyEvent e) {
			switch (e.keyCode) {
				case SWT.ARROW_UP:
				case SWT.ARROW_DOWN:
				case SWT.ARROW_LEFT:
				case SWT.ARROW_RIGHT:
					commit();
					break;
			}
		}
	}

	/**
	 * Internal listener to document changes.
	 */
	class DocumentListener implements IDocumentListener {

		private String fReplacedText;

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentAboutToBeChanged(DocumentEvent event) {
			try {
				fReplacedText= event.getDocument().get(event.getOffset(), event.getLength());
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

			// record the current valid state for the top operation in case it remains the
			// top operation but changes state.
			IUndoableOperation op= fHistory.getUndoOperation(fUndoContext);
			boolean wasValid= false;
			if (op != null)
				wasValid= op.canUndo();
			// Process the change, providing the before and after timestamps
			processChange(event.getOffset(), event.getOffset() + event.getLength(), event.getText(), fReplacedText, fPreservedUndoModificationStamp, fPreservedRedoModificationStamp);

			// now update fCurrent with the latest buffers from the document change.
			fCurrent.pretendCommit();

			if (op == fCurrent) {
				// if the document change did not cause a new fCurrent to be created, then we should
				// notify the history that the current operation changed if its validity has changed.
				if (wasValid != fCurrent.isValid())
					fHistory.operationChanged(op);
			}
			else {
				// if the change created a new fCurrent that we did not yet add to the
				// stack, do so if it's valid and we are not in the middle of a compound change.
				if (fCurrent != fLastAddedCommand && fCurrent.isValid()) {
					addToCommandStack(fCurrent);
				}
			}
		}
	}

	/**
	 * Internal text input listener.
	 */
	class TextInputListener implements ITextInputListener {

		/*
		 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
			if (oldInput != null && fDocumentListener != null) {
				oldInput.removeDocumentListener(fDocumentListener);
				commit();
			}
		}

		/*
		 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
			if (newInput != null) {
				if (fDocumentListener == null)
					fDocumentListener= new DocumentListener();
				newInput.addDocumentListener(fDocumentListener);
			}
		}

	}

	/*
	 * @see IOperationHistoryListener
	 * @since 3.1
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
					fTextViewer.getTextWidget().getDisplay().syncExec(new Runnable() {
						public void run() {
							// if we are undoing/redoing a command we generated, then ignore
							// the document changes associated with this undo or redo.
							if (event.getOperation() instanceof TextCommand) {
								if (fTextViewer instanceof TextViewer)
									((TextViewer)fTextViewer).ignoreAutoEditStrategies(true);
								listenToTextChanges(false);

								// in the undo case only, make sure compounds are closed
								if (type == OperationHistoryEvent.ABOUT_TO_UNDO) {
									if (fFoldingIntoCompoundChange) {
										endCompoundChange();
									}
								}
							} else {
								// the undo or redo has our context, but it is not one of
								// our commands.  We will listen to the changes, but will
								// reset the state that tracks the undo/redo history.
								commit();
								fLastAddedCommand= null;
							}
						}
				    });
					fOperation= event.getOperation();
				}
				break;
			case OperationHistoryEvent.UNDONE:
			case OperationHistoryEvent.REDONE:
			case OperationHistoryEvent.OPERATION_NOT_OK:
				if (event.getOperation() == fOperation) {
					fTextViewer.getTextWidget().getDisplay().syncExec(new Runnable() {
						public void run() {
							listenToTextChanges(true);
							fOperation= null;
							if (fTextViewer instanceof TextViewer)
								((TextViewer)fTextViewer).ignoreAutoEditStrategies(false);
				         }
				    });
				}
				break;
			}
		}

	}

	/** Text buffer to collect text which is inserted into the viewer */
	private StringBuffer fTextBuffer;
	/** Text buffer to collect viewer content which has been replaced */
	private StringBuffer fPreservedTextBuffer;
	/** The document modification stamp for undo. */
	protected long fPreservedUndoModificationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
	/** The document modification stamp for redo. */
	protected long fPreservedRedoModificationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
	/** The internal key and mouse event listener */
	private KeyAndMouseListener fKeyAndMouseListener;
	/** The internal document listener */
	private DocumentListener fDocumentListener;
	/** The internal text input listener */
	private TextInputListener fTextInputListener;


	/** Indicates inserting state */
	private boolean fInserting= false;
	/** Indicates overwriting state */
	private boolean fOverwriting= false;
	/** Indicates whether the current change belongs to a compound change */
	private boolean fFoldingIntoCompoundChange= false;

	/** The text viewer the undo manager is connected to */
	private ITextViewer fTextViewer;

	/** Supported undo level */
	private int fUndoLevel;
	/** The currently constructed edit command */
	private TextCommand fCurrent;
	/** The last delete edit command */
	private TextCommand fPreviousDelete;

	/**
	 * The undo context.
	 * @since 3.1
	 */
	private IOperationHistory fHistory;
	/**
	 * The operation history.
	 * @since 3.1
	 */
	private IUndoContext fUndoContext;
	/**
	 * The operation history listener used for managing undo and redo before
	 * and after the individual commands are performed.
	 * @since 3.1
	 */
	private IOperationHistoryListener fHistoryListener= new HistoryListener();

	/**
	 * The command last added to the operation history.  This must be tracked
	 * internally instead of asking the history, since outside parties may be placing
	 * items on our undo/redo history.
	 */
	private TextCommand fLastAddedCommand= null;

	/**
	 * Creates a new undo manager who remembers the specified number of edit commands.
	 *
	 * @param undoLevel the length of this manager's history
	 */
	public DefaultUndoManager(int undoLevel) {
	    fHistory= OperationHistoryFactory.getOperationHistory();
		setMaximalUndoLevel(undoLevel);
	}

	/**
	 * Returns whether this undo manager is connected to a text viewer.
	 *
	 * @return <code>true</code> if connected, <code>false</code> otherwise
	 * @since 3.1
	 */
	private boolean isConnected() {
		return fTextViewer != null;
	}

	/*
	 * @see IUndoManager#beginCompoundChange
	 */
	public void beginCompoundChange() {
		if (isConnected()) {
			fFoldingIntoCompoundChange= true;
			commit();
		}
	}


	/*
	 * @see IUndoManager#endCompoundChange
	 */
	public void endCompoundChange() {
		if (isConnected()) {
			fFoldingIntoCompoundChange= false;
			commit();
		}
	}

	/**
	 * Registers all necessary listeners with the text viewer.
	 */
	private void addListeners() {
		StyledText text= fTextViewer.getTextWidget();
		if (text != null) {
			fKeyAndMouseListener= new KeyAndMouseListener();
			text.addMouseListener(fKeyAndMouseListener);
			text.addKeyListener(fKeyAndMouseListener);
			fTextInputListener= new TextInputListener();
			fTextViewer.addTextInputListener(fTextInputListener);
			fHistory.addOperationHistoryListener(fHistoryListener);
			listenToTextChanges(true);
		}
	}

	/**
	 * Unregister all previously installed listeners from the text viewer.
	 */
	private void removeListeners() {
		StyledText text= fTextViewer.getTextWidget();
		if (text != null) {
			if (fKeyAndMouseListener != null) {
				text.removeMouseListener(fKeyAndMouseListener);
				text.removeKeyListener(fKeyAndMouseListener);
				fKeyAndMouseListener= null;
			}
			if (fTextInputListener != null) {
				fTextViewer.removeTextInputListener(fTextInputListener);
				fTextInputListener= null;
			}
			listenToTextChanges(false);
			fHistory.removeOperationHistoryListener(fHistoryListener);
		}
	}

	/**
	 * Adds the given command to the operation history if it is not part of
	 * a compound change.
	 *
	 * @param command the command to be added
	 * @since 3.1
	 */
	private void addToCommandStack(TextCommand command){
		if (!fFoldingIntoCompoundChange || command instanceof CompoundTextCommand) {
		    fHistory.add(command);
		    fLastAddedCommand= command;
		}
	}

	/**
	 * Disposes the command stack.
	 *
	 * @since 3.1
	 */
	private void disposeCommandStack() {
	    fHistory.dispose(fUndoContext, true, true, true);
	}

	/**
	 * Initializes the command stack.
	 *
	 * @since 3.1
	 */
	private void initializeCommandStack() {
	    if (fHistory != null && fUndoContext != null)
			fHistory.dispose(fUndoContext, true, true, false);

	}

	/**
	 * Switches the state of whether there is a text listener or not.
	 *
	 * @param listen the state which should be established
	 */
	private void listenToTextChanges(boolean listen) {
		if (listen) {
			if (fDocumentListener == null && fTextViewer.getDocument() != null) {
				fDocumentListener= new DocumentListener();
				fTextViewer.getDocument().addDocumentListener(fDocumentListener);
			}
		} else if (!listen) {
			if (fDocumentListener != null && fTextViewer.getDocument() != null) {
				fTextViewer.getDocument().removeDocumentListener(fDocumentListener);
				fDocumentListener= null;
			}
		}
	}

	/**
	 * Closes the current editing command and opens a new one.
	 */
	private void commit() {
		// if fCurrent has never been placed on the command stack, do so now.
		// this can happen when there are multiple programmatically commits in a single
		// document change.
		if (fLastAddedCommand != fCurrent) {
			fCurrent.pretendCommit();
			if (fCurrent.isValid())
				addToCommandStack(fCurrent);
		}
		fCurrent.commit();
	}

	/**
	 * Reset processChange state.
	 *
	 * @since 3.2
	 */
	private void resetProcessChangeSate() {
		fInserting= false;
		fOverwriting= false;
		fPreviousDelete.reinitialize();
	}

	/**
	 * Checks whether the given text starts with a line delimiter and
	 * subsequently contains a white space only.
	 *
	 * @param text the text to check
	 * @return <code>true</code> if the text is a line delimiter followed by whitespace, <code>false</code> otherwise
	 */
	private boolean isWhitespaceText(String text) {

		if (text == null || text.length() == 0)
			return false;

		String[] delimiters= fTextViewer.getDocument().getLegalLineDelimiters();
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

	private void processChange(int modelStart, int modelEnd, String insertedText, String replacedText, long beforeChangeModificationStamp, long afterChangeModificationStamp) {

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
				if (!fInserting || (modelStart != fCurrent.fStart + fTextBuffer.length())) {
					fCurrent.fRedoModificationStamp= beforeChangeModificationStamp;
					if (fCurrent.attemptCommit())
						fCurrent.fUndoModificationStamp= beforeChangeModificationStamp;

					fInserting= true;
				}
				if (fCurrent.fStart < 0)
					fCurrent.fStart= fCurrent.fEnd= modelStart;
				if (length > 0)
					fTextBuffer.append(insertedText);
			} else if (length >= 0) {
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
				// text will be deleted by backspace or DEL key or empty clipboard
				length= replacedText.length();
				String[] delimiters= fTextViewer.getDocument().getLegalLineDelimiters();

				if ((length == 1) || TextUtilities.equals(delimiters, replacedText) > -1) {

					// whereby selection is empty

					if (fPreviousDelete.fStart == modelStart && fPreviousDelete.fEnd == modelEnd) {
						// repeated DEL

							// correct wrong settings of fCurrent
						if (fCurrent.fStart == modelEnd && fCurrent.fEnd == modelStart) {
							fCurrent.fStart= modelStart;
							fCurrent.fEnd= modelEnd;
						}
							// append to buffer && extend command range
						fPreservedTextBuffer.append(replacedText);
						++fCurrent.fEnd;

					} else if (fPreviousDelete.fStart == modelEnd) {
						// repeated backspace

							// insert in buffer and extend command range
						fPreservedTextBuffer.insert(0, replacedText);
						fCurrent.fStart= modelStart;

					} else {
						// either DEL or backspace for the first time

						fCurrent.fRedoModificationStamp= beforeChangeModificationStamp;
						if (fCurrent.attemptCommit())
							fCurrent.fUndoModificationStamp= beforeChangeModificationStamp;

						// as we can not decide whether it was DEL or backspace we initialize for backspace
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
					String[] delimiters= fTextViewer.getDocument().getLegalLineDelimiters();

					if ((length == 1) || TextUtilities.equals(delimiters, replacedText) > -1) {
						// because of overwrite mode or model manipulation
						if (!fOverwriting || (modelStart != fCurrent.fStart +  fTextBuffer.length())) {
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
		// in all cases, the redo modification stamp is updated on the open command
		fCurrent.fRedoModificationStamp= afterChangeModificationStamp;
	}

	/**
	 * Shows the given exception in an error dialog.
	 *
	 * @param title the dialog title
	 * @param ex the exception
	 * @since 3.1
	 */
	private void openErrorDialog(final String title, final Exception ex) {
		Shell shell= null;
		if (isConnected()) {
			StyledText st= fTextViewer.getTextWidget();
			if (st != null && !st.isDisposed())
				shell= st.getShell();
		}
		if (Display.getCurrent() != null)
			MessageDialog.openError(shell, title, ex.getLocalizedMessage());
		else {
			Display display;
			final Shell finalShell= shell;
			if (finalShell != null)
				display= finalShell.getDisplay();
			else
				display= Display.getDefault();
			display.syncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(finalShell, title, ex.getLocalizedMessage());
				}
			});
		}
	}

	/*
	 * @see org.eclipse.jface.text.IUndoManager#setMaximalUndoLevel(int)
	 */
	public void setMaximalUndoLevel(int undoLevel) {
		fUndoLevel= Math.max(0, undoLevel);
		if (isConnected()) {
			fHistory.setLimit(fUndoContext, fUndoLevel);
		}
	}

	/*
	 * @see org.eclipse.jface.text.IUndoManager#connect(org.eclipse.jface.text.ITextViewer)
	 */
	public void connect(ITextViewer textViewer) {
		if (!isConnected() && textViewer != null) {
			fTextViewer= textViewer;
			fTextBuffer= new StringBuffer();
			fPreservedTextBuffer= new StringBuffer();
		    if (fUndoContext == null)
		        fUndoContext= new ObjectUndoContext(this);

		    fHistory.setLimit(fUndoContext, fUndoLevel);

			initializeCommandStack();

			// open up the current command
			fCurrent= new TextCommand(fUndoContext);

			fPreviousDelete= new TextCommand(fUndoContext);
			addListeners();
		}
	}

	/*
	 * @see org.eclipse.jface.text.IUndoManager#disconnect()
	 */
	public void disconnect() {
		if (isConnected()) {

			removeListeners();

			fCurrent= null;
			fTextViewer= null;
			disposeCommandStack();
			fTextBuffer= null;
			fPreservedTextBuffer= null;
			fUndoContext= null;
		}
	}

	/*
	 * @see org.eclipse.jface.text.IUndoManager#reset()
	 */
	public void reset() {
		if (isConnected()) {
			initializeCommandStack();
			fCurrent= new TextCommand(fUndoContext);
			fFoldingIntoCompoundChange= false;
			fInserting= false;
			fOverwriting= false;
			fTextBuffer.setLength(0);
			fPreservedTextBuffer.setLength(0);
			fPreservedUndoModificationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
			fPreservedRedoModificationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
		}
	}

	/*
	 * @see org.eclipse.jface.text.IUndoManager#redoable()
	 */
	public boolean redoable() {
	    return fHistory.canRedo(fUndoContext);
	}

	/*
	 * @see org.eclipse.jface.text.IUndoManager#undoable()
	 */
	public boolean undoable() {
	    return fHistory.canUndo(fUndoContext);
	}

	/*
	 * @see org.eclipse.jface.text.IUndoManager#redo()
	 */
	public void redo() {
		if (isConnected() && redoable()) {
			try {
				fHistory.redo(fUndoContext, null, null);
			} catch (ExecutionException ex) {
				openErrorDialog(JFaceTextMessages.getString("DefaultUndoManager.error.redoFailed.title"), ex); //$NON-NLS-1$
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.IUndoManager#undo()
	 */
	public void undo() {
		if (isConnected() && undoable()) {
			try {
				fHistory.undo(fUndoContext, null, null);
			} catch (ExecutionException ex) {
				openErrorDialog(JFaceTextMessages.getString("DefaultUndoManager.error.undoFailed.title"), ex); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Selects and reveals the specified range.
	 *
	 * @param offset the offset of the range
	 * @param length the length of the range
	 * @since 3.0
	 */
	protected void selectAndReveal(int offset, int length) {
		if (fTextViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) fTextViewer;
			extension.exposeModelRange(new Region(offset, length));
		} else if (!fTextViewer.overlapsWithVisibleRegion(offset, length))
			fTextViewer.resetVisibleRegion();

		fTextViewer.setSelectedRange(offset, length);
		fTextViewer.revealRange(offset, length);
	}

	/*
	 * @see org.eclipse.jface.text.IUndoManagerExtension#getUndoContext()
	 * @since 3.1
	 */
	public IUndoContext getUndoContext() {
		return fUndoContext;
	}

}
