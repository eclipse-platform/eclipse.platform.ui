/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	public void internalWorked(double work) {
		internalWorkedCalls++;
		super.internalWorked(work);
	}

}
