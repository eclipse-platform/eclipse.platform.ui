/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;

/**
 * An operation that adds a box.
 */
public class MoveBoxOperation extends BoxOperation {

	/*
	 * The point the box should move to/from.
	 */
	private final Point origin;
	private final Point target;

	public MoveBoxOperation(String label, IUndoContext context, Box box, Canvas canvas, Point newOrigin) {
		super(label, context, null, box, canvas);
		origin = new Point(box.x1, box.y1);
		target = new Point(newOrigin.x, newOrigin.y);
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (box==null) {
			throw new ExecutionException("box ix null");
		}
		box.move(target);
		canvas.redraw();
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (box==null) {
			throw new ExecutionException("box ix null");
		}
		box.move(origin);
		canvas.redraw();
		return Status.OK_STATUS;
	}

	@Override
	public String getLabel() {
		final StringBuilder stringBuffer = new StringBuilder();
		stringBuffer.append(super.getLabel());
		stringBuffer.append("["); //$NON-NLS-1$
		stringBuffer.append("("); //$NON-NLS-1$
		stringBuffer.append(Integer.toString(origin.x));
		stringBuffer.append(", "); //$NON-NLS-1$
		stringBuffer.append(Integer.toString(origin.y));
		stringBuffer.append(')');
		stringBuffer.append(", "); //$NON-NLS-1$
		stringBuffer.append("("); //$NON-NLS-1$
		stringBuffer.append(Integer.toString(target.x));
		stringBuffer.append(", "); //$NON-NLS-1$
		stringBuffer.append(Integer.toString(target.y));
		stringBuffer.append(')');
		stringBuffer.append(']');
		return stringBuffer.toString();
	}

}
