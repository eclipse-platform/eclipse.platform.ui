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

package org.eclipse.jface.text;


import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;


/**
 * Standard implementation of {@link org.eclipse.jface.text.IUndoManager}.
 * <p>
 * It registers with the connected text viewer as text input listener and
 * document listener and logs all changes. It also monitors mouse and keyboard
 * activities in order to partition the stream of text changes into undo-able
 * edit commands.
 * </p>
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * 
 * @see org.eclipse.jface.text.ITextViewer
 * @see org.eclipse.jface.text.ITextInputListener
 * @see org.eclipse.jface.text.IDocumentListener
 * @see MouseListener
 * @see KeyListener
 */
public class DefaultUndoManager implements IUndoManager {

	/**
	 * Represents an undo-able edit command.
	 */
	class TextCommand {
		
		/** The start index of the replaced text */
		protected int fStart= -1;
		/** The end index of the replaced text */
		protected int fEnd= -1;
		/** The newly inserted text */
		protected String fText;
		/** The replaced text */
		protected String fPreservedText;
		
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
		
		/**
		 * Undo the change described by this command. Also selects and
		 * reveals the change.
		 */
		protected void undo() {
			undoTextChange();
			selectAndReveal(fStart, fPreservedText == null ? 0 : fPreservedText.length());
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
		 */
		protected void redo() {
			redoTextChange();
			selectAndReveal(fStart, fText == null ? 0 : fText.length());
		}
				
		/**
		 * Updates the command stack in response to committing
		 * the current change into this command.
		 */
		protected void updateCommandStack() {
			
			int length= fCommandStack.size();
			for (int i= fCommandCounter + 1; i < length; i++)
				fCommandStack.remove(fCommandCounter + 1);
				
			fCommandStack.add(this);
			
			while (fCommandStack.size() > fUndoLevel)
				fCommandStack.remove(0);
			
			fCommandCounter= fCommandStack.size() - 1;
		}
		
		/**
		 * Creates a new uncommitted text command depending on whether
		 * a compound change is currently being executed.
		 *
		 * @return a new, uncommitted text command or a compound text command
		 */
		protected TextCommand createCurrent() {
			return fFoldingIntoCompoundChange ? new CompoundTextCommand() : new TextCommand();		
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
				
				updateCommandStack();
			}
			
			fCurrent= createCurrent();
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
		 * Adds a new individual command to this compound command.
		 *
		 * @param command the command to be added
		 */
		protected void add(TextCommand command) {
			fCommands.add(command);
		}
		
		/*
		 * @see org.eclipse.jface.text.DefaultUndoManager2.TextCommand#undo()
		 */
		protected void undo() {
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
					c.undo();
				}
					
			} finally {
				if (extension != null)
					extension.setRedraw(true);
			}
		}
		
		/*
		 * @see org.eclipse.jface.text.DefaultUndoManager2.TextCommand#redo()
		 */
		protected void redo() {
			
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
					c.redo();
				}
				
			} finally {
				if (extension != null)
					extension.setRedraw(true);
			}
		}
		
		/*
		 * @see TextCommand#updateCommandStack
		 */
		protected void updateCommandStack() {
			TextCommand c= new TextCommand();
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
				return new TextCommand();
			
			reinitialize();
			return this;
		}
		
		/*
		 * @see org.eclipse.jface.text.DefaultUndoManager.TextCommand#commit()
		 */
		protected void commit() {
			if (fStart < 0) {
				if (fCommands.size() > 0 && !fFoldingIntoCompoundChange) {
					super.updateCommandStack();
					fCurrent= createCurrent();
					return;
				}
			}
			super.commit();
		}
	}
	
	/**
	 * Represents pretended <code>UndoManager</code> state.
	 */
	class PretendedUndoManagerState {
		/** The counter in the undo stack */
		protected int cmdCounter= -1;
		/** The size of the undo stack */
		protected int stackSize= -1;
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
			processChange(event.getOffset(), event.getOffset() + event.getLength(), event.getText(), fReplacedText);
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
	
	
	/** Text buffer to collect text which is inserted into the viewer */
	private StringBuffer fTextBuffer= new StringBuffer();
	/** Text buffer to collect viewer content which has been replaced */
	private StringBuffer fPreservedTextBuffer= new StringBuffer(); 
	/** Pretended undo manager state */
	private PretendedUndoManagerState fPretendedState= new PretendedUndoManagerState();
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
	/** The list of undo-able edit commands */
	private List fCommandStack;
	/** The currently constructed edit command */
	private TextCommand fCurrent;
	/** The last delete edit command */
	private TextCommand fPreviousDelete;
	/** Command counter into the edit command stack */
	private int fCommandCounter= -1;
			
			 
	/**
	 * Creates a new undo manager who remembers the specified number of edit commands.
	 *
	 * @param undoLevel the length of this manager's history
	 */
	public DefaultUndoManager(int undoLevel) {
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
	 * Re-applies the previously undone editing command.
	 */
	private void internalRedo() {		
		++fCommandCounter;
		TextCommand cmd= (TextCommand) fCommandStack.get(fCommandCounter);
		
		listenToTextChanges(false);
		cmd.redo();
		listenToTextChanges(true);
		
		fCurrent= new TextCommand();
	}
	
	/**
	 * Does undo the last editing command.
	 */
	private void internalUndo() {		
		TextCommand cmd= (TextCommand) fCommandStack.get(fCommandCounter);
		-- fCommandCounter;
		
		listenToTextChanges(false);
		cmd.undo();
		listenToTextChanges(true);
		
		fCurrent= new TextCommand();
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
	
	/**
	 * Returns the state the would result if the current editing command would be closed.
	 *
	 * @return the pretended state after closing the current editing command
	 */
	private PretendedUndoManagerState pretendCommit() {
		if (fCurrent.fStart < 0) {
			fPretendedState.stackSize= fCommandStack.size();
			fPretendedState.cmdCounter= fCommandCounter;
		} else {
			int sz= Math.max(fCommandCounter, 0) + 1;
			if (sz > fUndoLevel)
				sz -= fUndoLevel;
			fPretendedState.stackSize= sz;
			fPretendedState.cmdCounter= sz - 1;
		}
		return fPretendedState;	
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
		
	/*
	 * @see org.eclipse.jface.text.IUndoManager#setMaximalUndoLevel(int)
	 */
	public void setMaximalUndoLevel(int undoLevel) {
		fUndoLevel= undoLevel;
	}

	/*
	 * @see org.eclipse.jface.text.IUndoManager#connect(org.eclipse.jface.text.ITextViewer)
	 */
	public void connect(ITextViewer textViewer) {
		if (fTextViewer == null && textViewer != null) {
			fTextViewer= textViewer;	
			fCommandStack= new ArrayList();
			fCurrent= new TextCommand();
			fPreviousDelete= new TextCommand();
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
			if (fCommandStack != null) {
				fCommandStack.clear();
				fCommandStack= null;
			}
			fTextBuffer= null;
			fPreservedTextBuffer= null;
			fTextViewer= null;
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.IUndoManager#reset()
	 */
	public void reset() {
		if (isConnected()) {
			if (fCommandStack != null)
				fCommandStack.clear();
			fCommandCounter= -1;
			if (fCurrent != null)
				fCurrent.reinitialize();
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
		if (fCommandStack != null)  {
			PretendedUndoManagerState s= pretendCommit();
			return (0 <= s.cmdCounter + 1) && (s.cmdCounter + 1 < s.stackSize);
		}
		return false;
	}
	
	/*
	 * @see org.eclipse.jface.text.IUndoManager#undoable()
	 */
	public boolean undoable() {
		if (fCommandStack != null) {
			PretendedUndoManagerState s= pretendCommit();
			return (0 <= s.cmdCounter) && (s.cmdCounter < s.stackSize);
		}
		return false;
	}
	
	/*
	 * @see org.eclipse.jface.text.IUndoManager#redo()
	 */
	public void redo() {
		if (isConnected() && redoable()) {
			commit();
			internalRedo();
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.IUndoManager#undo()
	 */
	public void undo() {
		if (isConnected() && undoable()) {
			fFoldingIntoCompoundChange= false;
			commit();
			internalUndo();
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
}
