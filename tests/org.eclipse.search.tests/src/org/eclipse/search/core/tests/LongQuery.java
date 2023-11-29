/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
package org.eclipse.search.core.tests;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class LongQuery extends NullQuery {

	private boolean fIsRunning= false;

	@Override
	public boolean canRunInBackground() {
		return true;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		fIsRunning= true;
		while (!monitor.isCanceled()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// should not happen
			}
		}
		fIsRunning= false;
		return Status.OK_STATUS;
	}

	public boolean isRunning() {
		return fIsRunning;
	}
}
