/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.undo;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Canvas;

/**
 * An operation that adds a box.
 */
public class ClearBoxesOperation extends BoxOperation {

	/*
	 * The boxes that are saved after clearing
	 */
	private List<Box> savedBoxes = new ArrayList<Box>();

	public ClearBoxesOperation(String label, IUndoContext context, Boxes boxes, Canvas canvas) {
		super(label, context, boxes, null, canvas);
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info){
		savedBoxes = boxes.getBoxes();
		boxes.clear();
		canvas.redraw();
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
		boxes.setBoxes(savedBoxes);
		canvas.redraw();
		return Status.OK_STATUS;
	}

}
