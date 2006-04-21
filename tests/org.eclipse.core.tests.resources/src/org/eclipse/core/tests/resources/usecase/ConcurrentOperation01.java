/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.usecase;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This operation does not change any resource. It only has one
 * syncPoint in order to inform that it's running.
 */
public class ConcurrentOperation01 extends ConcurrentOperation {

	/** indicates if this operation has entered the run() method */
	protected boolean isRunning;

	public ConcurrentOperation01(IWorkspace workspace) {
		super(workspace);
		reset();
	}

	protected void assertRequisites() throws Exception {
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void reset() {
		super.reset();
		isRunning = false;
	}

	public void run(IProgressMonitor monitor) throws CoreException {
		isRunning = true;
		syncPoint();
	}
}
