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
package org.eclipse.ui.tests.operations;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 3.1
 */
public class TestOperation extends AbstractOperation {

	private int fExecutionCount = 0;

	TestOperation(String label) {
		super(label);
	}

	public boolean canRedo() {
		return fExecutionCount == 0;
	}

	public boolean canUndo() {
		return fExecutionCount > 0;
	}

	public IStatus execute(IProgressMonitor monitor, IAdaptable uiInfo) {
		fExecutionCount++;
		return Status.OK_STATUS;
	}
	
	public IStatus redo(IProgressMonitor monitor, IAdaptable uiInfo) {
		return execute(monitor, uiInfo);
	}

	public IStatus undo(IProgressMonitor monitor, IAdaptable uiInfo) {
		fExecutionCount--;
		return Status.OK_STATUS;
	}
	
	public void dispose() {
		fExecutionCount = 0;
	}

}
