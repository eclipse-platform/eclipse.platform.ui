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

import org.eclipse.ltk.internal.core.refactoring.UndoManager;

/**
 * Central access point to access resources managed by the refactoring
 * core plug-in.
 * 
 * @since 3.0
 */
public class RefactoringCore {

	private static IUndoManager fgUndoManager= null;

	/**
	 * Creates a new empty undo manager.
	 * 
	 * @return a new undo manager
	 */
	public static IUndoManager createUndoManager() {
		return new UndoManager();
	}

	/**
	 * Returns the singleton undo manager for the refactoring undo
	 * stack.
	 * 
	 * @return the refactoring undo manager.
	 */
	public static IUndoManager getUndoManager() {
		if (fgUndoManager == null)
			fgUndoManager= createUndoManager();
		return fgUndoManager;
	}
}
