/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;

/**
 * Listener to monitor state changes of an {@link IUndoManager}.
 * <p>
 * Clients may implement this interface to listen to undo manger changes.
 * </p>
 * <p>
 * As of 3.2 clients which need to examine refactorings which have been performed, undone or redone should use
 * {@link IRefactoringExecutionListener} for enhanced functionality.
 * </p>
 *
 * @since 3.0
 */
public interface IUndoManagerListener {

	/**
	 * This method is called by the undo manager if the undo stack has
	 * changed (for example a undo object got added or the undo stack
	 * got flushed).
	 *
	 * @param manager the manager this listener is registered to
	 */
	void undoStackChanged(IUndoManager manager);

	/**
	 * This method is called by the undo manager if the redo stack has
	 * changed (for example a redo object got added or the redo stack
	 * got flushed).
	 *
	 * @param manager the manager this listener is registered to
	 */
	void redoStackChanged(IUndoManager manager);

	/**
	 * This method gets called by the undo manager if a change gets
	 * executed in the context of the undo manager.
	 *
	 * @param manager the manager this listener is registered to
	 * @param change the change to be executed
	 */
	void aboutToPerformChange(IUndoManager manager, Change change);

	/**
	 * This method gets called by the undo manager when a change has
	 * been executed in the context of the undo manager.
	 *
	 * @param manager the manager this listener is registered to
	 * @param change the change that has been executed
	 */
	void changePerformed(IUndoManager manager, Change change);
}
