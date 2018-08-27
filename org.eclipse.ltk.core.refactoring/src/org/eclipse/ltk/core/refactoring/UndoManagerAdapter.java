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

/**
 * This adapter class provides default implementations for the
 * methods defined by the {@link IUndoManagerListener} interface.
 * <p>
 * This class may be subclassed by clients.
 * </p>
 * @since 3.0
 */
public class UndoManagerAdapter implements IUndoManagerListener {

	@Override
	public void undoStackChanged(IUndoManager manager) {
	}

	@Override
	public void redoStackChanged(IUndoManager manager) {
	}

	@Override
	public void aboutToPerformChange(IUndoManager manager, Change change) {
	}

	@Override
	public void changePerformed(IUndoManager manager, Change change) {
	}
}

