/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.core.commands.operations.DefaultOperationHistory;
import org.eclipse.core.commands.operations.IOperationApprover;
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
 */
public class DefaultUndoManager implements IUndoManager, IUndoManagerExtension {
	
	/**
	 * Represents an undo-able edit command.
	 * <p>
	 * Since 3.1 this implements the interface for IUndoableOperation.
	 * </p>
	 */
	class TextCommand extends AbstractOperation {
		
		/** The start index of the replaced text */
		protected int fStart= -1;
		/** The end index of the replaced text */
		protected int fEnd= -1;
		/** The newly inserted text */
		protected String fText;
		/** The replaced text */
		protected String fPreservedText;
		
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
		 * FIXME: string is not user friendly.
		 * 
		 * @see org.eclipse.core.commands.operations.IUndoableOperation#getDescription()
		 * @since 3.1
		 */
		public String getDescription() {
		    StringBuffer text= new StringBuffer(this.toString()+" "); //$NON-NLS-1$
		    text.append(new Integer(fStart).toString());
		    text.append(":"); //$NON-NLS-1$
		    text.append(new Integer(fEnd).toString());
		    text.append(" "); //$NON-NLS-1$
		    text.append(fText);
		    text.append(" "); //$NON-NLS-1$
		    text.append(fPreservedText);
		    return text.toString();
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
				fTextViewer.getDocument().replace(fStart, fText.length(), fPreservedText);
			} catch (BadLocationException x) {
			}
		}
		
		/*
		 * @see org.eclipse.core.commands.operations.IUndoableOperation#canUndo()
		 * @since 3.1
		 */
		public boolean canUndo() {
		    return isConnected() && isValid();
		}
		
		/**
		 * Return whether the text command is redoable.
		 * In general, a text command is redoable and the operation history determines
		 * when redo is appropriate.  However, we have a special case involving fCurrent, 
		 * which may be the pending undo change but is not yet in the operation history.
		 * 
		 * We special case this here.  If fCurrent is a valid pending change, then
		 * redo should not be available.
		 * 
		 * @see #canUndo
		 * @return a boolean indicating whether the command can be undone.
		 * @since 3.1
		 */
		public boolean canRedo() {
			if (fCurrent != this && fCurrent.isValid())
				return false;
		    return isConnected();
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
				return Status.OK_STATUS;
			}
			return DefaultOperationHistory.OPERATION_INVALID_STATUS;
		}
		
		/**
		 * Re-applies the change described by this command.
		 * 
		 * @since 2.0
		 */
		protected void redoTextChange() {
			try {
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
				selectAndReveal(fStart, fText == null ? 0 : fText.length());
				return Status.OK_STATUS;
			}
			return DefaultOperationHistory.OPERATION_INVALID_STATUS;
		}
				
		/**
		 * Updates the command stack in response to committing
		 * the current change into this command.
		 */
		protected void updateCommandStack() {
		    addToCommandStack(this);
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
				reinitialize();
			} else {	
				
				fText= fTextBuffer.toString();
				fTextBuffer.setLength(0);
				fPreservedText= fPreservedTextBuffer.toString();
				fPreservedTextBuffer.setLength(0);
				
				if (!isPrimer())
					updateCommandStack();
			}
			
			setNewCurrent();
		}
		
		/**
		 * Updates the text from the buffers without resetting
		 * the buffers or adding anything to the stack.
		 *
		 * @since 3.1
		 */
		protected void pretendCommit() {
			if (fStart >= 0) {
				fText= fTextBuffer.toString();
				fPreservedText= fPreservedTextBuffer.toString();
			}
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
		
		/**
		 * Returns whether the receiver is a primer command.
		 * Primer commands are put on the stack in advance.
		 * 
		 * @return <code>true</code> if the command is a primer
		 * @since 3.1
		 */
		protected boolean isPrimer() {
		    return false;
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
			ITextViewerExtension extension= null;
			if (fTextViewer instanceof ITextViewerExtension)
				extension= (ITextViewerExtension) fTextViewer;
				
			if (extension != null)
				extension.setRedraw(false);
				
			try {
				
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
					
			} finally {
				if (extension != null)
					extension.setRedraw(true);
			}
			return Status.OK_STATUS;
		}
		
		/*
		 * @see org.eclipse.jface.text.DefaultUndoManager.TextCommand#redo()
		 */
		public IStatus redo(IProgressMonitor monitor, IAdaptable uiInfo) {
			
			ITextViewerExtension extension= null;
			if (fTextViewer instanceof ITextViewerExtension)
				extension= (ITextViewerExtension) fTextViewer;
				
			if (extension != null)
				extension.setRedraw(false);
			
			try {
				
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
				
			} finally {
				if (extension != null)
					extension.setRedraw(true);
			}
			return Status.OK_STATUS;
		}
		
		/*
		 * @see TextCommand#updateCommandStack
		 */
		protected void updateCommandStack() {
			TextCommand c= new TextCommand(fUndoContext);
			c.fStart= fStart;
			c.fEnd= fEnd;
			c.fText= fText;
			c.fPreservedText= fPreservedText;
			
			add(c);
			
			if (!fFoldingIntoCompoundChange)
				super.updateCommandStack();
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
			if (fStart < 0) {
				if (fCommands.size() > 0 && !fFoldingIntoCompoundChange) {
					if (!isPrimer())
						super.updateCommandStack();
					setNewCurrent();
					return;
				}
			}
			super.commit();
		}
		
		/**
		 * Checks whether the command is valid for undo or redo.
		 * 
		 * @return true if the command is valid.
		 * @since 3.1
		 */
		protected boolean isValid() {
			if (isConnected())
				return (fStart >= 0 || fCommands.size() > 0);
		    return false;
		}
	}
	
	/**
	 * Represents the text command used to prime the operation history.
	 * A primer command is added to the stack whenever the undo history
	 * is empty.  Its purpose is to give the history a command
	 * to consult when it's time to undo.
	 *
	 * @since 3.1
	 */
	class PrimerTextCommand extends TextCommand {
				
		/**
		 * Creates a new primer text command.
		 *
		 * @param context the undo context for this command
		 */
		PrimerTextCommand(IUndoContext context) {
		    super(context);
		}
		
		/*
		 * @see org.eclipse.jface.text.DefaultUndoManager.TextCommand#isPrimer()
		 */
		protected boolean isPrimer() {
			return true;
		}
		
		/*
		 * @see org.eclipse.core.commands.operations.IUndoableOperation#canUndo()
		 */
		public boolean canUndo() {
			// there is another pending change after the primer.  
			if (this != fCurrent && fCurrent != null) {
				fCurrent.pretendCommit();
				return fCurrent.canUndo();
			}
			// there is no pending change beyond the primer
			pretendCommit();
		    return super.canUndo();
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
			} catch (BadLocationException x) {
				fReplacedText= null;
			}
		}

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentChanged(DocumentEvent event) {
			// Check the special cases involving the primer command before
			// and after processing the change.
			
			IUndoableOperation op= fHistory.getUndoOperation(fUndoContext);
			if (op == null) {
				// there's been a change to the document but no primer was installed.  This can happen if there
				// is redo history when the current command was created.  This change should install a primer in
				// which will restore the proper canUndo() status and flush the redo history.
				op= new PrimerTextCommand(fUndoContext);
				addToCommandStack((TextCommand)op);
			}
			
			processChange(event.getOffset(), event.getOffset() + event.getLength(), event.getText(), fReplacedText);
			
			// If the current undo operation is one that we used to prime the history,
			// then we need to notify the history that the primer has changed.  This
			// ensures that the canUndo() and canRedo() status are updated properly.
			if (op instanceof TextCommand && ((TextCommand)op).isPrimer())
				fHistory.operationChanged(op);
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
		
		public void historyNotification(OperationHistoryEvent event) {
			switch (event.getEventType()) {
			case OperationHistoryEvent.ABOUT_TO_UNDO:
			case OperationHistoryEvent.ABOUT_TO_REDO:
				IUndoableOperation op;
				// if this is one of our operations
				if ((op= event.getOperation()).hasContext(fUndoContext) && op instanceof TextCommand) {
					if (fTextViewer instanceof TextViewer)
						((TextViewer)fTextViewer).ignoreAutoEditStrategies(true);
					listenToTextChanges(false);
					fFoldingIntoCompoundChange= false;
					fOperation= event.getOperation();
				}
				break;
			case OperationHistoryEvent.UNDONE:
			case OperationHistoryEvent.REDONE:
			case OperationHistoryEvent.OPERATION_NOT_OK:
				if (event.getOperation() == fOperation) {
					listenToTextChanges(true);
					setNewCurrent();
					fOperation= null;
					if (fTextViewer instanceof TextViewer)
						((TextViewer)fTextViewer).ignoreAutoEditStrategies(false);
				}
				break;
			}
		}

	}
	
	/*
	 * @see IOperationApprover
	 * @since 3.1
	 */
	class OperationApprover implements IOperationApprover {
		public IStatus proceedUndoing(IUndoableOperation op, IOperationHistory history, IAdaptable uiInfo ) {
			if (!(op instanceof TextCommand))
				return Status.OK_STATUS;
			/*
			 * IMPORTANT:  fCurrent actually holds the most recent undoable
			 * changes.  If it is valid, than we should undo it instead and cancel
			 * the undo of the proposed operation.
			 */
			fCurrent.pretendCommit();
			if (op != fCurrent && op.hasContext(fUndoContext) && fCurrent.isValid()) {
				commit();
				
				// now that fCurrent is on the stack, invoke undo.  Returning CANCEL_STATUS 
				// from this method will cause the original command to stay on the stack.
				try {
					history.undo(fUndoContext, null, uiInfo);
				} catch (ExecutionException ex) {
					openErrorDialog(JFaceTextMessages.getString("DefaultUndoManager.error.undoFailed.title"), ex); //$NON-NLS-1$
				}
				return Status.CANCEL_STATUS;	
			} 
			return Status.OK_STATUS;
		}
		public IStatus proceedRedoing(IUndoableOperation op, IOperationHistory history, IAdaptable uiInfo ) {
			return Status.OK_STATUS;
		}
	}
	
	/** Text buffer to collect text which is inserted into the viewer */
	private StringBuffer fTextBuffer= new StringBuffer();
	/** Text buffer to collect viewer content which has been replaced */
	private StringBuffer fPreservedTextBuffer= new StringBuffer(); 
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
	 * The operation approver.
	 * @since 3.1
	 */
	private IOperationApprover fOperationApprover= new OperationApprover();
	
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
			fHistory.addOperationApprover(fOperationApprover);
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
			fHistory.removeOperationApprover(fOperationApprover);
			fHistory.removeOperationHistoryListener(fHistoryListener);
		}
	}
	
	/**
	 * Adds the given command to the operation history.
	 * 
	 * @param command the command to be added
	 * @since 3.1
	 */
	private void addToCommandStack(TextCommand command){
	    fHistory.add(command);
	}

	/**
	 * Disposes the command stack.
	 * 
	 * @since 3.1
	 */
	private void disposeCommandStack() {
	    fHistory.dispose(fUndoContext, true, true);
	}
	
	/**
	 * Initializes the command stack.
	 * 
	 * @since 3.1
	 */
	private void initializeCommandStack() {
	    if (fHistory != null && fUndoContext != null)
			fHistory.dispose(fUndoContext, true, true);

	}
	
	/**
	 * Creates the new current, open text command and assigns it to <code>fCurrent</code>.
	 *
	 * @since 3.1
	 */
	private void setNewCurrent() {
		// if the history is empty, we need to prime it by adding
		// the new command
		if (fHistory.getUndoHistory(fUndoContext).length == 0 && 
				fHistory.getRedoHistory(fUndoContext).length == 0) {
			fCurrent= new PrimerTextCommand(fUndoContext);
			addToCommandStack(fCurrent);
		} else {
			if (fCurrent == null)
				fCurrent= new TextCommand(fUndoContext);
			else 
				fCurrent= fCurrent.createCurrent();
		}
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
		
		fInserting= false;
		fOverwriting= false;
		fPreviousDelete.reinitialize();
		
		fCurrent.commit();
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
	
	private void processChange(int modelStart, int modelEnd, String insertedText, String replacedText) {
		
		if (insertedText == null)
			insertedText= ""; //$NON-NLS-1$
			
		if (replacedText == null)
			replacedText= ""; //$NON-NLS-1$
		
		int length= insertedText.length();
		int diff= modelEnd - modelStart;
		
		// normalize
		if (diff < 0) {
			int tmp= modelEnd;
			modelEnd= modelStart;
			modelStart= tmp;
		}
				
		if (modelStart == modelEnd) {
			// text will be inserted
			if ((length == 1) || isWhitespaceText(insertedText)) {
				// by typing or model manipulation
				if (!fInserting || (modelStart != fCurrent.fStart + fTextBuffer.length())) {
					commit();
					fInserting= true;
				} 
				if (fCurrent.fStart < 0)
					fCurrent.fStart= fCurrent.fEnd= modelStart;
				if (length > 0)
					fTextBuffer.append(insertedText);
			} else if (length > 0) {
				// by pasting
				commit();
				fCurrent.fStart= fCurrent.fEnd= modelStart;
				fTextBuffer.append(insertedText);
				commit();
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
						
						commit();
						
						// as we can not decide whether it was DEL or backspace we initialize for backspace
						fPreservedTextBuffer.append(replacedText);
						fCurrent.fStart= modelStart;
						fCurrent.fEnd= modelEnd;
					}
					
					fPreviousDelete.set(modelStart, modelEnd);
					
				} else if (length > 0) {
					// whereby selection is not empty
					commit();
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
							commit();
							fOverwriting= true;
						}

						if (fCurrent.fStart < 0)
							fCurrent.fStart= modelStart;

						fCurrent.fEnd= modelEnd;
						fTextBuffer.append(insertedText);
						fPreservedTextBuffer.append(replacedText);
						return;
					}
				} 
				// because of typing or pasting whereby selection is not empty
				commit();
				fCurrent.fStart= modelStart;
				fCurrent.fEnd= modelEnd;
				fTextBuffer.append(insertedText);
				fPreservedTextBuffer.append(replacedText);
			}
		}		
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
		fUndoLevel= undoLevel;
		if (isConnected()) {
			fHistory.setLimit(fUndoContext, undoLevel);
		}
	}	

	/*
	 * @see org.eclipse.jface.text.IUndoManager#connect(org.eclipse.jface.text.ITextViewer)
	 */
	public void connect(ITextViewer textViewer) {
		if (fTextViewer == null && textViewer != null) {
			fTextViewer= textViewer;
		    if (fUndoContext == null)
		        fUndoContext= new ObjectUndoContext(this);
			
		    fHistory.setLimit(fUndoContext, fUndoLevel);
			
			initializeCommandStack();
			
			// open up the current command and add it to the history.
			setNewCurrent();
			
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
			disposeCommandStack();
			fTextBuffer= null;
			fPreservedTextBuffer= null;
			fTextViewer= null;
			fUndoContext= null;
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.IUndoManager#reset()
	 */
	public void reset() {
		if (isConnected()) {
			initializeCommandStack();
			if (fCurrent != null) {
				setNewCurrent();
			}
			fFoldingIntoCompoundChange= false;
			fInserting= false;
			fOverwriting= false;
			fTextBuffer.setLength(0);
			fPreservedTextBuffer.setLength(0);
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
