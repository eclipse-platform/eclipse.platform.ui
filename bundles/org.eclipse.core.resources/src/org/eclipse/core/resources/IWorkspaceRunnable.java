/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This interface is structurally equivalent to {@link ICoreRunnable}. New code should use
 * {@link ICoreRunnable} instead of {@code IWorkspaceRunnable}.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see IWorkspace#run(ICoreRunnable, IProgressMonitor)
 */
public interface IWorkspaceRunnable extends ICoreRunnable {
	// No additional methods.
}
