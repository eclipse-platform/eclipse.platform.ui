/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
	private String fDescription = "";

	private int fExecutionCount = 0;

	TestOperation(String label, String description) {
		super(label);
		fDescription = description;
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
	
	public String getDescription() {
		return fDescription;
	}

	public IStatus redo(IProgressMonitor monitor, IAdaptable uiInfo) {
		return execute(monitor, uiInfo);
	}

	public String toString() {
		return getLabel();
	}

	public IStatus undo(IProgressMonitor monitor, IAdaptable uiInfo) {
		fExecutionCount--;
		return Status.OK_STATUS;
	}
	
	public void dispose() {
		fExecutionCount = 0;
	}

}
