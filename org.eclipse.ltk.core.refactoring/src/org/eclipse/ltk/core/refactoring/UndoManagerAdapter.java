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
 * This adapter class provides default implementations for the
 * methods described by the {@link IUndoManagerListener} interface.
 * 
 * @since 3.0
 */
public class UndoManagerAdapter implements IUndoManagerListener {

	/* (non-Javadoc)
	 * Method declared in IUndoManagerListener
	 */
	public void undoStackChanged(IUndoManager manager) {
	}
	
	/* (non-Javadoc)
	 * Method declared in IUndoManagerListener
	 */
	public void redoStackChanged(IUndoManager manager) {
	}
}

