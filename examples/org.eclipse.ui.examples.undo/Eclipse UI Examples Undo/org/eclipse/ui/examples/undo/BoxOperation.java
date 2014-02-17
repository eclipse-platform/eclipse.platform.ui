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

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Canvas;

/**
 * An undoable operation that represents a change to a box.
 */
public abstract class BoxOperation extends AbstractOperation {

	/*
	 * The box involved in the operation
	 */
	Box box;

	/*
	 * The group of boxes involved in the operation
	 */
	Boxes boxes;

	/*
	 * The canvas to update or draw on during an operation
	 */
	Canvas canvas;

	public BoxOperation(String label, IUndoContext undoContext, Boxes boxes, Box box, Canvas canvas) {
		super (label);
		addContext(undoContext);
		this.boxes = boxes;
		this.box = box;
		this.canvas = canvas;
	}

	/*
	 * Show the specified prompt in an info dialog.
	 */
	void showMessage(String message) {
		MessageDialog.openInformation(canvas.getShell(),
				UndoExampleMessages.BoxView_Title, message);
	}
}
