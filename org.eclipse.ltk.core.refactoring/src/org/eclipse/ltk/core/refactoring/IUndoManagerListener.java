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
 * 
 * @since 3.0
 */
public interface IUndoManagerListener {
	
	/**
	 * This method is called by the undo manager if an undo change has been 
	 * added.
	 * 
	 * @param manager the manager that has changed
	 */
	public void undoStackChanged(IUndoManager manager);
	
	/**
	 * This method is called by the undo manager if a redo change has been 
	 * added.
	 * 
	 * @param manager the manager that has changed
	 */
	public void redoStackChanged(IUndoManager manager);	
}
