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



/**
 * An undo manager is connected to at most one
 * {@link org.eclipse.jface.text.ITextViewer}.
 * <p>
 * It monitors the text viewer and keeps a history of the changes applied to the
 * viewer. The undo manager groups those changes into user interactions which on
 * an undo request are rolled back in one atomic change.
 * <p>
 * Clients may implement this interface or use the standard implementation
 * <code>DefaultUndoManager</code>.
 * 
 * @see org.eclipse.jface.text.DefaultUndoManager
 */
public interface IUndoManager {

	/**
	 * Connects this undo manager to the given text viewer.
	 *
	 * @param viewer the viewer the undo manager is connected to
	 */
	void connect(ITextViewer viewer);
	
	/**
	 * Disconnects this undo manager from its text viewer. 
	 * If this undo manager hasn't been connected before this
	 * operation has no effect.
	 */
	void disconnect();
	
	/**
	 * Signals the undo manager that all subsequent changes until
	 * <code>endCompoundChange</code> is called are to be undone in one piece.
	 */
	void beginCompoundChange();
	
	/**
	 * Signals the undo manager that the sequence of changes which started with 
	 * <code>beginCompoundChange</code> has been finished. All subsequent changes
	 * are considered to be individually undo-able.
	 */
	void endCompoundChange();
	
	/**
	 * Resets the history of the undo manager. After that call,
	 * there aren't any undo-able or redo-able text changes.
	 */
	void reset();
	
	/**
	 * The given parameter determines the maximal length of the history
	 * remembered by the undo manager.
	 *
	 * @param undoLevel the length of this undo manager's history
	 */
	void setMaximalUndoLevel(int undoLevel);
	
	/**
	 * Returns whether at least one text change can be rolled back.
	 *
	 * @return <code>true</code> if at least one text change can be rolled back
	 */
	boolean undoable();
	
	/**
	 * Returns whether at least one text change can be repeated. A text change
	 * can be repeated only if it was executed and rolled back.
	 *
	 * @return <code>true</code> if at least on text change can be repeated
	 */
	boolean redoable();
	
	/**
	 * Rolls back the most recently executed text change.
	 */
	void undo();
	
	/**
	 * Repeats the most recently rolled back text change.
	 */
	void redo();

}
