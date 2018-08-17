/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
package org.eclipse.core.tests.runtime;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * @deprecated to suppress deprecation warnings
 */
public class SubProgressSubclass extends SubProgressMonitor {

	public int internalWorkedCalls = 0;

	public SubProgressSubclass(IProgressMonitor monitor, int ticks, int style) {
		super(monitor, ticks, style);
	}

	public SubProgressSubclass(IProgressMonitor monitor, int ticks) {
		super(monitor, ticks);
	}

	@Override
	public void internalWorked(double work) {
		internalWorkedCalls++;
		super.internalWorked(work);
	}

}
