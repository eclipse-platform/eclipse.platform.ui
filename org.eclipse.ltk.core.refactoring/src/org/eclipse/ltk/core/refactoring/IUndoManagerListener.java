/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;


/**
 * Listener to monitor state changes of an {@link IUndoManager}.
 * <p>
 * Clients may implement this interface to listen to undo manger changes.
 * </p>
 * @since 3.0
 */
public interface IUndoManagerListener {
	
	/**
	 * This method is called by the undo manager if an undo change has been 
	 * added.
	 * 
	 * @param manager the manager this listener is registered to
	 */
	public void undoStackChanged(IUndoManager manager);
	
	/**
	 * This method is called by the undo manager if a redo change has been 
	 * added.
	 * 
	 * @param manager the manager this listener is registered to
	 */
	public void redoStackChanged(IUndoManager manager);
	
	/**
	 * This method gets called by the undo manager if a change gets
	 * executed for which a corresponding undo change will be pushed
	 * onto the undo or redo stack.
	 * 
	 * @param manager the manager this listener is registered to
	 * @param change the change to be executed
	 */
	public void aboutToPerformChange(IUndoManager manager, Change change);
	
	/**
	 * This method gets called by the undo manager when a change has 
	 * been executed. 
	 * 
	 * @param manager the manager this listener is registered to
	 * @param change the change that has been executed
	 */
	public void changePerformed(IUndoManager manager, Change change);	
}
