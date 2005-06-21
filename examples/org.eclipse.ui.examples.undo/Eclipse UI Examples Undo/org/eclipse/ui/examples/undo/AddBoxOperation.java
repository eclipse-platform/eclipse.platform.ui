/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.undo;

import org.eclipse.core.commands.ExecutionException;
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

	public AddBoxOperation(String label, IUndoContext context, Boxes boxes, Box box, Canvas canvas) {
		super(label, context, boxes, box, canvas);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#execute(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		boxes.add(box);
		canvas.redraw(box.x1, box.y1, box.x2, box.y2, false);
		return Status.OK_STATUS;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#canUndo()
	 */
	public boolean canUndo() {
		return boxes.contains(box);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#canRedo()
	 */
	public boolean canRedo() {
		return !boxes.contains(box);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#redo(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return execute(monitor, info);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#undo(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		boxes.remove(box);
		canvas.redraw(box.x1, box.y1, box.x2, box.y2, false);
		return Status.OK_STATUS;
	}

}
