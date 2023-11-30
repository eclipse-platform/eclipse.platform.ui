/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
package org.eclipse.ui.examples.undo;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Canvas;

/**
 * An operation that adds a box.
 */
public class AddBoxOperation extends BoxOperation {

	/**
	 * Create a box
	 */
	public AddBoxOperation(String label, IUndoContext context, Boxes boxes, Box box, Canvas canvas) {
		super(label, context, boxes, box, canvas);
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
		boxes.add(box);
		canvas.redraw(box.x1, box.y1, box.x2, box.y2, false);
		return Status.OK_STATUS;
	}

	@Override
	public boolean canUndo() {
		return boxes.contains(box);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
		boxes.remove(box);
		canvas.redraw(box.x1, box.y1, box.x2, box.y2, false);
		return Status.OK_STATUS;
	}

	@Override
	public boolean canRedo() {
		return !boxes.contains(box);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) {
		return execute(monitor, info);
	}



}
