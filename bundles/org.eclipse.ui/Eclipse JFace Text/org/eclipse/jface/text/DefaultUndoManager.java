package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;


/**
 * Standard implementation of <code>IUndoManager</code>. 
 * It registers with the connected text viewer as text listeners and logs all changes. 
 * It also monitors mouse and keyboard activities in order to partition the stream of
 * text changes into undoable edit commands. <p>
 * This class is not intended to be subclassed.
 * 
 * @see ITextViewer
 * @see ITextListener
 * @see MouseListener
 * @see KeyListener
 */
public class DefaultUndoManager implements IUndoManager {

	/**
	 * Represents an undoable edit command.
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
		 * Reinitializes this text command.
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
		 * @param text the text widget to be modified
		 */
		protected void undo(StyledText text) {
			text.replaceTextRange(fStart, fText.length(), fPreservedText);
			int length= fPreservedText == null ? 0 : fPreservedText.length();
			
			IRegion visible= fTextViewer.getVisibleRegion();
			int offset= fStart + visible.getOffset();
			fTextViewer.setSelectedRange(offset, length);
			fTextViewer.revealRange(offset, length);
		}
		
		/**
		 * Redo the change described by this command, and previously been 
		 * rolled back.
		 *
		 * @param text the text widget to be modified
		 */
		protected void redo(StyledText text) {
			text.replaceTextRange(fStart, fEnd - fStart, fText);
			int length= fText == null ? 0 : fText.length();
			
			IRegion visible= fTextViewer.getVisibleRegion();
			int offset= fStart + visible.getOffset();
			fTextViewer.setSelectedRange(offset, length);
			fTextViewer.revealRange(offset, length);
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
	};
	
	/**
	 * Represents an undoable edit command consisting of several
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
		 * @see TextCommand#undo
		 */
		protected void undo(StyledText text) {
			ListIterator e= fCommands.listIterator(fCommands.size());
			while (e.hasPrevious()) {
				TextCommand c= (TextCommand) e.previous();
				c.undo(text);
			}
		}
		
		/*
		 * @see TextCommand#redo
		 */
		protected void redo(StyledText text) {
			ListIterator e= fCommands.listIterator();
			while (e.hasNext()) {
				TextCommand c= (TextCommand) e.next();
				c.redo(text);
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
	};
	
	/**
	 * Represents pretended <code>UndoManager</code> state.
	 */
	class PretendedUndoManagerState {
		protected int cmdCounter= -1;
		protected int stackSize= -1;
	};
	
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
	};
	
	/**
	 * Internal listener to text changes.
	 */
	class TextListener implements ITextListener {
		
		/*
		 * @see ITextListener#textChanged
		 */
		public void textChanged(TextEvent e) {
			processTextEvent(e);
		}
	};
	 
	/** Text buffer to collect text which is inserted into the viewer */
	private StringBuffer fTextBuffer= new StringBuffer();
	/** Text buffer to collect viewer content which has been replaced */
	private StringBuffer fPreservedTextBuffer= new StringBuffer(); 
	/** Pretended undo manager state */
	private PretendedUndoManagerState fPretendedState= new PretendedUndoManagerState();
	
	/** The internal text listener */
	private ITextListener fTextListener;
	/** The internal key and mouse event listener */
	private KeyAndMouseListener fKeyAndMouseListener;
	
	
	/** Indicates inserting state */
	private boolean fInserting= false;
	/** Indicates deleteing state */
	private boolean fDeleting= false;
	/** Indicates overwriting state */
	private boolean fOverwriting= false;
	/** Indicates whether the current change belongs to a compound change */
	private boolean fFoldingIntoCompoundChange= false;
	
	/** The text viewer the undo manager is connected to */
	private ITextViewer fTextViewer;
	
	/** Supported undo level */
	private int fUndoLevel;
	/** The list of undoable edit commands */
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
	
	/*
	 * @see IUndoManager#beginCompoundChange
	 */
	public void beginCompoundChange() {
		fFoldingIntoCompoundChange= true;
		commit();
	}
	
	/*
	 * @see IUndoManager#endCompoundChange
	 */
	public void endCompoundChange() {
		fFoldingIntoCompoundChange= false;
		commit();
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
			listenToTextChanges(true);
		}
	}
	
	/**
	 * Deregister all previously installed listeners from the text viewer.
	 */
	private void removeListeners() {
		StyledText text= fTextViewer.getTextWidget();
		if (text != null && fKeyAndMouseListener != null) {
			text.removeMouseListener(fKeyAndMouseListener);
			text.removeKeyListener(fKeyAndMouseListener);
			listenToTextChanges(false);
		}
	}
	
	/**
	 * Switches the state of whether there is a text listener or not.
	 *
	 * @param listen the state which should be established
	 */
	private void listenToTextChanges(boolean listen) {
		if (listen && fTextListener == null) {
			fTextListener= new TextListener();
			fTextViewer.addTextListener(fTextListener);
		} else if (!listen && fTextListener != null) {
			fTextViewer.removeTextListener(fTextListener);
			fTextListener= null;
		}
	}
	
	/**
	 * Closes the current editing command and opens a new one.
	 */
	private void commit() {
		
		fInserting= false;
		fDeleting= false;
		fOverwriting= false;
		fPreviousDelete.reinitialize();
		
		fCurrent.commit();
	}
	
	/**
	 * Does redo the previously undone editing command.
	 */
	private void internalRedo() {
		StyledText text= fTextViewer.getTextWidget();
		if (text != null) {
			
			++fCommandCounter;
			TextCommand cmd= (TextCommand) fCommandStack.get(fCommandCounter);
			
			listenToTextChanges(false);
			cmd.redo(text);
			listenToTextChanges(true);
			
			fCurrent= new TextCommand();
		}
	}
	
	/**
	 * Does undo the last editing command.
	 */
	private void internalUndo() {
		StyledText text= fTextViewer.getTextWidget();
		if (text != null) {
		
			TextCommand cmd= (TextCommand) fCommandStack.get(fCommandCounter);
			-- fCommandCounter;
				
			listenToTextChanges(false);
			cmd.undo(text);
			listenToTextChanges(true);
			
			fCurrent= new TextCommand();
		}
	}
	
	/**
	 * Checks whether the given text starts with a line delimiter and
	 * subsequently contains a white space only.
	 *
	 * @param text the text to check
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
		}
		
		return true;
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
				
	/**
	 * Processes the given text event in order to determine editor command.
	 *
	 * @param e the text event
	 */
	private void processTextEvent(TextEvent e) {
	
		int start= e.getOffset();
		int end= e.getOffset() + e.getLength();
		String newText= e.getText();
		String oldText= e.getReplacedText();
		
		
		if (newText == null)
			newText= ""; //$NON-NLS-1$
			
		if (oldText == null)
			oldText= ""; //$NON-NLS-1$
		
		int length= newText.length();
		int diff= end - start;
		
		// normalize verify command
		if (diff < 0) {
			int tmp= end;
			end= start;
			start= tmp;
			diff= -diff;
		}
				
		if (start == end) {
			// text will be inserted
			if ((length == 1) || isWhitespaceText(newText)) {
				// by typing or model manipulation
				if (!fInserting || (start != fCurrent.fStart + fTextBuffer.length())) {
					commit();
					fInserting= true;
				} 
				if (fCurrent.fStart < 0)
					fCurrent.fStart= fCurrent.fEnd= start;
				if (length > 0)
					fTextBuffer.append(newText);
			} else if (length > 0) {
				// by pasting
				commit();
				fCurrent.fStart= fCurrent.fEnd= start;
				fTextBuffer.append(newText);
			}
		} else {
			if (length == 0) {
				// text will be deleted by backspace or DEL key or empty clipboard
				length= oldText.length();
				String[] delimiters= fTextViewer.getDocument().getLegalLineDelimiters();
				
				if ((length == 1) || TextUtilities.equals(delimiters, oldText) > -1) {
					
					// whereby selection is empty
					
					if (fPreviousDelete.fStart == start && fPreviousDelete.fEnd == end) {
						// repeated DEL
							
							// correct wrong settings of fCurrent
						if (fCurrent.fStart == end && fCurrent.fEnd == start) {
							fCurrent.fStart= start;
							fCurrent.fEnd= end;
						}
							// append to buffer && extend command range
						fPreservedTextBuffer.append(oldText);
						++fCurrent.fEnd;
						
					} else if (fPreviousDelete.fStart == end) {
						// repeated backspace
						
							// insert in buffer and extend command range
						fPreservedTextBuffer.insert(0, oldText);
						fCurrent.fStart= start;
					
					} else {
						// either DEL or backspace for the first time
						
						commit();
						fDeleting= true;
						
						// as we can not decide whether it was DEL or backspace we initialize for backspace
						fPreservedTextBuffer.append(oldText);
						fCurrent.fStart= start;
						fCurrent.fEnd= end;
					}
					
					fPreviousDelete.set(start, end);
					
				} else if (length > 0) {
					// whereby selection is not empty
					commit();
					fCurrent.fStart= start;
					fCurrent.fEnd= end;
					fPreservedTextBuffer.append(oldText);
				}
			} else {
				// text will be replaced
								
				if (length == 1) {
					length= oldText.length();
					String[] delimiters= fTextViewer.getDocument().getLegalLineDelimiters();

					if ((length == 1) || TextUtilities.equals(delimiters, oldText) > -1) {
						// because of overwrite mode or model bmanipulation
						if (!fOverwriting || (start != fCurrent.fEnd)) {
							commit();
							fOverwriting= true;
						}

						if (fCurrent.fStart < 0)
							fCurrent.fStart= start;

						fCurrent.fEnd= end;
						fTextBuffer.append(newText);
						fPreservedTextBuffer.append(oldText);
						return;
					}
				} 
				// because of typing or pasting whereby selection is not empty
				commit();
				fCurrent.fStart= start;
				fCurrent.fEnd= end;
				fTextBuffer.append(newText);
				fPreservedTextBuffer.append(oldText);
			}
		}
	}
	
	/*
	 * @see IUndoManager#setMaximalUndoLevel
	 */
	public void setMaximalUndoLevel(int undoLevel) {
		fUndoLevel= undoLevel;
	}

	/*
	 * @see IUndoManager#connect
	 */
	public void connect(ITextViewer textViewer) {
		if (fTextViewer == null) {
			fTextViewer= textViewer;	
			fCommandStack= new ArrayList();
			fCurrent= new TextCommand();
			fPreviousDelete= new TextCommand();
			addListeners();
		}
	}
	
	/*
	 * @see IUndoManager#disconnect
	 */
	public void disconnect() {
		if (fTextViewer != null) {
			
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
	 * @see IUndoManager#reset
	 */
	public void reset() {
		if (fCommandStack != null)
			fCommandStack.clear();
		fCommandCounter= -1;
		if (fCurrent != null)
			fCurrent.reinitialize();
		fFoldingIntoCompoundChange= false;
		fInserting= false;
		fDeleting= false;
		fOverwriting= false;
		fTextBuffer.setLength(0);
		fPreservedTextBuffer.setLength(0);		
	}
	
	/*
	 * @see IUndoManager#redoable
	 */
	public boolean redoable() {
		if (fCommandStack != null)  {
			PretendedUndoManagerState s= pretendCommit();
			return (0 <= s.cmdCounter + 1) && (s.cmdCounter + 1 < s.stackSize);
		}
		return false;
	}
	
	/*
	 * @see IUndoManager#undoable
	 */
	public boolean undoable() {
		if (fCommandStack != null) {
			PretendedUndoManagerState s= pretendCommit();
			return (0 <= s.cmdCounter) && (s.cmdCounter < s.stackSize);
		}
		return false;
	}
	
	/*
	 * @see IUndoManager#redo
	 */
	public void redo() {
		if (redoable()) {
			commit();
			internalRedo();
		}
	}
	
	/*
	 * @see IUndoManager#undo
	 */
	public void undo() {
		if (undoable()) {
			commit();
			internalUndo();
		}
	}
}
