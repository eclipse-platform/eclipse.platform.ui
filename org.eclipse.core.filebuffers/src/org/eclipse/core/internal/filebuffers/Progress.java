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
package org.eclipse.core.internal.filebuffers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Helper for progress management.
 * @since 3.1
 */
public class Progress {

	public static IProgressMonitor getMonitor(IProgressMonitor monitor) {
		return monitor == null ? new NullProgressMonitor() : monitor;
	}

	public static IProgressMonitor getSubMonitor(IProgressMonitor parent, int ticks) {
		return new SubProgressMonitor(getMonitor(parent), ticks, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
	}

	public static IProgressMonitor getMonitor() {
		return new NullProgressMonitor();
	}
}
