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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An undo manager keeps track of performed changes. Use the method <code>addUndo</code>
 * to add change objects to the undo stack and <code>performUndo</code> and <code>
 * performRedo</code> to undo or redo changes.
 * <p>
 * This interface is not intended to be implemented or extended. Use the method <code>
 * RefactoringCore#createUndoManager</code> to create a new undo manager or the method
 * <code>RefactoringCore#getUndoManager()</code> to access the refactoring undo manager.
 * </p>
 * 
 * @since 3.0
 */
public interface IUndoManager {

	/**
	 * Adds a listener to the undo manager.
	 * 
	 * @param listener the listener to be added to the undo manager
	 */
	public void addListener(IUndoManagerListener listener);
	
	/**
	 * Removes the given listener from this undo manager.
	 * 
	 * @param listener the listener to be removed
	 */
	public void removeListener(IUndoManagerListener listener);
	
	/**
	 * The infrastructure is going to perform the given change.
	 * 
	 * @param change the change to be performed.
	 */
	public void aboutToPerformChange(Change change);
	
	/**
	 * The infrastructure has performed the given change.
	 * 
	 * @param change the change that was performed
	 */
	public void changePerformed(Change change);

	/**
	 * Adds a new undo change to this undo manager.
	 * 
	 * @param name the name presented on the undo stack for the provided
	 *  undo change. The name must be human readable
	 * @param change the undo change
	 */
	public void addUndo(String name, Change change);

	/**
	 * Returns <code>true</code> if there is anything to undo, otherwise
	 * <code>false</code>.
	 * 
	 * @return <code>true</code> if there is anything to undo, otherwise
	 *  <code>false</code>
	 */
	public boolean anythingToUndo();
	
	/**
	 * Returns the name of the top most undo.
	 * 
	 * @return the top most undo name. The main purpose of the name is to
	 * render it in the UI. Returns <code>null</code> if there aren't any changes to undo
	 */
	public String peekUndoName();
	
	/**
	 * Undo the top most undo change.
	 * 
	 * @param pm a progress monitor to report progress during performing
	 *  the undo change. The progress monitor must not be <code>null</code>
	 * @return the validation status of the undone change.
	 */	
	public RefactoringStatus performUndo(IProgressMonitor pm) throws CoreException;

	/**
	 * Returns <code>true</code> if there is anything to redo, otherwise
	 * <code>false</code>.
	 * 
	 * @return <code>true</code> if there is anything to redo, otherwise
	 *  <code>false</code>
	 */
	public boolean anythingToRedo();
	
	/**
	 * Returns the name of the top most redo.
	 * 
	 * @return the top most redo name. The main purpose of the name is to
	 * render it in the UI. Returns <code>null</code> if there are no any changes to redo.
	 */
	public String peekRedoName();
	
	/**
	 * Redo the top most redo change.
	 * 
	 * @param pm a progress monitor to report progress during performing
	 *  the redo change. The progress monitor must not be <code>null</code>
	 * @return the validation status of the redone change.
	 */	
	public RefactoringStatus performRedo(IProgressMonitor pm) throws CoreException;
	
	/**
	 * Flushes the undo manager's undo and redo stacks.
	 */	
	public void flush();
	
	/**
	 * Shut down the undo manager. 
	 */
	public void shutdown();
}
